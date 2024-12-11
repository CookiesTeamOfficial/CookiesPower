package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.TimeUnit;
import arkadarktime.interfaces.modules.ModuleTicker;
import arkadarktime.utils.CustomUtils;
import arkadarktime.utils.FileManager;
import arkadarktime.utils.MinecraftNameGenerator;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;

public class ServerMotdModule implements ModuleTicker {
    private final CookiesPower plugin;
    private final CustomUtils customUtils;
    // Server motd
    private boolean isMotdEnable;
    private boolean isMotdRandom;
    private boolean isMotdUpdateWhenPing;
    private long motdUpdateInterval;
    private List<String> motdTexts;
    private String motdText;
    private int motdIndex = 0;
    // Icons for MOTD
    private boolean isMotdIconsEnable;
    private boolean isMotdIconRandom;
    private final List<String> motdIcons = new ArrayList<>();
    private WrappedServerPing.CompressedImage motdIcon;
    private int iconIndex = 0;
    // Max players
    private boolean isMaxPlayersEnable;
    private int maxPlayersCount;
    // Version
    private boolean isVersionEnable;
    private boolean isVersionRandom;
    private int versionProtocol;
    private List<String> versionTexts;
    private String versionText;
    private int versionIndex = 0;
    // Fake players
    private boolean isFakePlayersEnable;
    private int fakePlayersCount;
    private boolean isFakePlayersGenerateRandomNames;
    private final List<WrappedGameProfile> fakePlayers = new ArrayList<>();
    // Other
    private int updateServerMotdTaskId = -1;

    public ServerMotdModule(CookiesPower plugin) {
        this.plugin = plugin;
        this.customUtils = new CustomUtils(plugin);
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.tablist.enable")) return;

        ModuleTicker.super.enable();

        if (plugin.getProtocolManager() == null || plugin.getProtocolManager().isClosed()) {
            this.disable();
            Console(ConsoleType.WARN, "ServerMotdModule is disabled because ProtocolLib is not installed!", LineType.SIDE_LINES);
        } else {
            loadMotdSettings();
            this.start();
        }
    }

    @Override
    public void disable() {
        ModuleTicker.super.disable();
        this.stop();
    }

    @Override
    public void restart() {
        this.disable(false);
        loadMotdSettings();
        this.enable(false);
    }

    @Override
    public void start() {
        if (!isMotdUpdateWhenPing) {
            updateServerMotdTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 0L, motdUpdateInterval).getTaskId();
        }

        MinecraftNameGenerator minecraftNameGenerator = new MinecraftNameGenerator(3);
        plugin.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Status.Server.SERVER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                WrappedServerPing serverPing = event.getPacket().getServerPings().read(0);

                if (isMotdUpdateWhenPing) {
                    update();
                }

                if (isMotdEnable && motdText != null) {
                    serverPing.setMotD(motdText);
                }

                if (isMotdIconsEnable && motdIcon != null) {
                    serverPing.setFavicon(motdIcon);
                }

                if (isMaxPlayersEnable) {
                    serverPing.setPlayersMaximum(maxPlayersCount);
                }

                if (isVersionEnable && versionText != null) {
                    serverPing.setVersionName(versionText);
                    serverPing.setVersionProtocol(versionProtocol);
                }

                if (isFakePlayersEnable) {
                    addFakePlayers(serverPing, minecraftNameGenerator);
                }

                event.getPacket().getServerPings().write(0, serverPing);
            }
        });
    }

    @Override
    public void stop() {
        if (updateServerMotdTaskId != -1) {
            Bukkit.getScheduler().cancelTask(updateServerMotdTaskId);
            updateServerMotdTaskId = -1;
        }
    }

    private void loadMotdSettings() {
        FileManager serverFileManager = new FileManager(plugin, plugin.getServerFile());
        boolean isServerFull = Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers();
        String motdPath = "server-motd." + (isServerFull ? "server-full" : "normal");

        // Motd
        isMotdEnable = serverFileManager.getBoolean(motdPath + ".motd.enable");
        isMotdRandom = serverFileManager.getBoolean(motdPath + ".motd.random");
        isMotdUpdateWhenPing = serverFileManager.getBoolean(motdPath + ".motd.update-when-ping");
        motdUpdateInterval = customUtils.parseTime(serverFileManager.getString(motdPath + ".motd.update-interval", "1s"), TimeUnit.TICKS);
        motdTexts = serverFileManager.getColoredStringList(null, motdPath + ".motd.texts");

        // Icons
        isMotdIconsEnable = serverFileManager.getBoolean(motdPath + ".icons.enable");
        isMotdIconRandom = serverFileManager.getBoolean(motdPath + ".icons.random");
        @NotNull List<String> iconsList = serverFileManager.getStringList(motdPath + ".icons.icons-list");
        for (String icon : iconsList) {
            if (icon.endsWith(".png")) {
                motdIcons.add(icon);
            } else {
                Console(ConsoleType.WARN, "Server icon must be png, your: " + icon);
            }
        }

        // Max players
        isMaxPlayersEnable = serverFileManager.getBoolean(motdPath + ".max-players.enable");
        maxPlayersCount = serverFileManager.getInt(motdPath + ".max-players.count");

        // Fake players
        isFakePlayersEnable = serverFileManager.getBoolean(motdPath + ".fake-players.enable");
        fakePlayersCount = serverFileManager.getInt(motdPath + ".fake-players.count");
        isFakePlayersGenerateRandomNames = serverFileManager.getBoolean(motdPath + ".fake-players.generate-random-names");

        // Version
        isVersionEnable = serverFileManager.getBoolean(motdPath + ".version.enable");
        isVersionRandom = serverFileManager.getBoolean(motdPath + ".version.random");
        versionProtocol = serverFileManager.getInt(motdPath + ".version.protocol");
        versionTexts = serverFileManager.getColoredStringList(null, motdPath + ".version.texts");

        if (motdTexts == null || motdTexts.isEmpty()) {
            motdTexts = List.of(" ");
        }

        if (versionTexts == null || versionTexts.isEmpty()) {
            versionTexts = List.of(" ");
        }
    }

    private void addFakePlayers(WrappedServerPing serverPing, MinecraftNameGenerator minecraftNameGenerator) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        for (Player player : onlinePlayers) {
            if (fakePlayers.stream().noneMatch(p -> p.getName().equals(player.getName()))) {
                fakePlayers.add(new WrappedGameProfile(player.getUniqueId(), player.getName()));
            }
        }

        while (fakePlayers.size() < fakePlayersCount) {
            String playerName = isFakePlayersGenerateRandomNames ? minecraftNameGenerator.generate(minecraftNameGenerator.generateRandomString(true)) : minecraftNameGenerator.generateRandomString(true);
            if (fakePlayers.stream().noneMatch(p -> p.getName().equals(playerName))) {
                fakePlayers.add(new WrappedGameProfile(UUID.randomUUID(), playerName));
            }
        }

        serverPing.setPlayers(fakePlayers);
        serverPing.setPlayersOnline(fakePlayersCount);
    }

    public void update() {
        if (motdTexts == null || motdTexts.isEmpty()) {
            motdText = "MOTD texts in lang file are empty!";
        } else {
            motdText = getRandomText(motdTexts, isMotdRandom, true);
        }

        if (!motdIcons.isEmpty()) {
            motdIcon = loadIcon(getRandomIcon());
        }

        if (versionTexts != null && !versionTexts.isEmpty()) {
            versionText = getRandomText(versionTexts, isVersionRandom, false);
        }
    }

    private String getRandomText(List<String> texts, boolean random, boolean isMotdIndex) {
        if (random) {
            return texts.get(new Random().nextInt(texts.size()));
        } else {
            if (isMotdIndex) {
                return texts.get(motdIndex++ % texts.size());
            } else {
                return texts.get(versionIndex++ % texts.size());
            }
        }
    }

    private String getRandomIcon() {
        if (isMotdIconRandom) {
            return motdIcons.get(new Random().nextInt(motdIcons.size()));
        } else {
            return motdIcons.get(iconIndex++ % motdIcons.size());
        }
    }

    private WrappedServerPing.CompressedImage loadIcon(String icon) {
        String iconBase64 = loadIconBase64(icon);
        if (iconBase64 != null) {
            return WrappedServerPing.CompressedImage.fromBase64Png(iconBase64);
        }
        return null;
    }

    private String loadIconBase64(String icon) {
        File iconFile = new File(plugin.getDataFolder(), "icons/" + icon);

        byte[] iconBytes = null;

        if (isValidUrl(icon)) {
            try {
                URL url = new URL(icon);
                InputStream in = url.openStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }
                in.close();
                iconBytes = byteArrayOutputStream.toByteArray();
            } catch ( IOException error ) {
                Console(ConsoleType.ERROR, "Error loading icon: Invalid URL.");
            }
        } else if (iconFile.exists()) {
            try (InputStream inFile = new FileInputStream(iconFile)) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inFile.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }
                iconBytes = byteArrayOutputStream.toByteArray();
            } catch ( IOException error ) {
                Console(ConsoleType.ERROR, "Error loading icon bytes from file: " + error.getMessage());
                error.printStackTrace();
            }
        }

        if (iconBytes != null) {
            return resizeIconTo64x64(iconBytes);
        }

        Console(ConsoleType.ERROR, "Error loading icon: File \"" + icon + "\" not found.");
        return null;
    }

    private String resizeIconTo64x64(byte[] imageBytes) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(inputStream);

            if (originalImage == null) {
                Console(ConsoleType.ERROR, "Error resizing icon: Invalid image format.");
                return null;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            if (originalWidth <= 64 && originalHeight <= 64) {
                return Base64.getEncoder().encodeToString(imageBytes);
            }

            BufferedImage resizedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, 64, 64, null);
            g2d.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch ( IOException error ) {
            Console(ConsoleType.ERROR, "Error resizing icon: " + error.getMessage());
            error.printStackTrace();
            return null;
        }
    }

    public static boolean isValidUrl(String urlString) {
        try {
            new URL(urlString).toURI();
            return true;
        } catch ( MalformedURLException | URISyntaxException e ) {
            return false;
        }
    }
}

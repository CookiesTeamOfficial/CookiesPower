package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.TimeUnit;
import arkadarktime.interfaces.ModuleTicker;
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
        if (plugin.protocolManager.isClosed() || plugin.protocolManager == null) {
            ModuleTicker.super.disable();
            Console(ConsoleType.WARN, "ServerMotdModule is disabled because ProtocolLib is not installed!", LineType.SIDE_LINES);
        } else {
            this.start();
        }
    }

    @Override
    public void disable() {
        ModuleTicker.super.disable();
        this.stop();
    }

    @Override
    public void start() {
        loadMotdSettings();

        if (!isMotdUpdateWhenPing) {
            updateServerMotdTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 0L, motdUpdateInterval).getTaskId();
        }

        MinecraftNameGenerator minecraftNameGenerator = new MinecraftNameGenerator(3);
        plugin.protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Status.Server.SERVER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                WrappedServerPing serverPing = event.getPacket().getServerPings().read(0);

                if (isMotdUpdateWhenPing) {
                    update();
                }

                if (isMotdEnable) {
                    serverPing.setMotD(motdText);
                }

                if (isMaxPlayersEnable) {
                    serverPing.setPlayersMaximum(maxPlayersCount);
                }

                if (isVersionEnable) {
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

        Console("Loading motd settings!!!");

        // Motd
        isMotdEnable = serverFileManager.getBoolean(motdPath + ".motd.enable");
        isMotdRandom = serverFileManager.getBoolean(motdPath + ".motd.random");
        isMotdUpdateWhenPing = serverFileManager.getBoolean(motdPath + ".motd.update-when-ping");
        motdUpdateInterval = customUtils.parseTime(serverFileManager.getString(motdPath + ".motd.update-interval", "1s"), TimeUnit.TICKS);
        motdTexts = serverFileManager.getColoredStringList(null, motdPath + ".motd.texts");
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
}

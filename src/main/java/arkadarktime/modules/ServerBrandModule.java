package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.enums.TimeUnit;
import arkadarktime.interfaces.BukkitConsole;
import arkadarktime.interfaces.ModuleTicker;
import arkadarktime.utils.ByteBufData;
import arkadarktime.utils.CustomUtils;
import arkadarktime.utils.FileManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ServerBrandModule implements ModuleTicker, BukkitConsole {
    private final CookiesPower plugin;
    private final CustomUtils customUtils;
    private static Field playerChannelsField;
    private String channel;
    private String brand;
    private int brandIndex = 0;
    private int brandUpdateTaskId = -1;

    public ServerBrandModule(CookiesPower plugin) {
        this.plugin = plugin;
        this.customUtils = new CustomUtils(plugin);
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.server-brand.enable")) return;

        ModuleTicker.super.enable();
        registerListener(this, plugin);
        this.start();
    }

    @Override
    public void disable() {
        ModuleTicker.super.disable();
        this.stop();
    }

    @Override
    public void start() {
        FileManager serverFileManager = new FileManager(plugin, plugin.getServerFile());
        updateServerBrand();
        long updateTime = customUtils.parseTime(serverFileManager.getString("server-brand.update-interval", "1s"), TimeUnit.TICKS);
        brandUpdateTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 0L, updateTime).getTaskId();
    }

    @Override
    public void stop() {
        if (brandUpdateTaskId != -1) {
            Bukkit.getScheduler().cancelTask(brandUpdateTaskId);
            brandUpdateTaskId = -1;
        }
    }

    @Override
    public void update() {
        FileManager serverFileManager = new FileManager(plugin, plugin.getServerFile());
        List<String> brandTexts = serverFileManager.getColoredStringList(null, "server-brand.texts");
        boolean brandTextsRandom = serverFileManager.getBoolean("server-brand.random");

        if (brandTexts.isEmpty()) {
            Console(ConsoleType.ERROR, "server-brand.texts doesn't found in lang file!", LineType.SIDE_LINES);
            return;
        }

        if (brandTextsRandom) {
            Random random = new Random();
            brand = brandTexts.get(random.nextInt(brandTexts.size()));
        } else {
            brand = brandTexts.get(brandIndex);
            brandIndex = (brandIndex + 1) % brandTexts.size();
        }

        Bukkit.getOnlinePlayers().forEach(this::updateBrand);
    }

    public void updateServerBrand() {
        try {
            Class.forName("org.bukkit.entity.Dolphin");
            channel = "minecraft:brand";
        } catch ( ClassNotFoundException ignored ) {
            channel = "MC|Brand";
        }

        try {
            Method registerMethod = plugin.getServer().getMessenger().getClass().getDeclaredMethod("addToOutgoing", Plugin.class, String.class);
            registerMethod.setAccessible(true);
            registerMethod.invoke(plugin.getServer().getMessenger(), plugin, channel);
        } catch ( ReflectiveOperationException e ) {
            Console(ConsoleType.ERROR, "Error while attempting to register plugin message channel: " + e, LineType.SIDE_LINES);
            throw new RuntimeException("Error while attempting to register plugin message channel: " + e);
        }

        this.update();
    }

    private void updateBrand(Player player) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());

        if (channel == null) {
            Console(ConsoleType.ERROR, "Brand channel is not set.", LineType.SIDE_LINES);
            return;
        }

        CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
        String coloredBrand = langFileManager.applyColorsAndPlaceholders(cookiesPlayer, this.brand, true);
        ByteBuf byteBuf = Unpooled.buffer();
        ByteBufData.writeString(coloredBrand + ChatColor.RESET, byteBuf);
//        cookiesPlayer.sendPluginMessage(plugin, channel, ByteBufData.toArray(byteBuf));
        byteBuf.release();
    }

//    public void changeServerBrand(String brand) {
//        // Создаем новый PacketContainer для изменения server brand
//        PacketContainer packet = new PacketContainer(PacketType.Status.Server.SERVER_INFO);
//
//        // Используем WrappedGameProfile для создания профиля
//        WrappedGameProfile profile = new WrappedGameProfile(null, brand);
//
//        // Устанавливаем данные профиля
//        packet.getGameProfiles().write(0, profile);
//
//        // Отправляем пакет всем онлайн игрокам
//        for (Player player : Bukkit.getOnlinePlayers()) {
//            try {
//                PacketEvent event = new PacketEvent(player, packet);
//                plugin.protocolLibAPI.sendServerPacket(player, packet);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (playerChannelsField == null) {
            try {
                playerChannelsField = event.getPlayer().getClass().getDeclaredField("channels");
                playerChannelsField.setAccessible(true);
            } catch ( ReflectiveOperationException e ) {
                e.printStackTrace();
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
        }

        try {
            Set<String> channels = (Set<String>) playerChannelsField.get(event.getPlayer());
            channels.add(channel);
        } catch ( ReflectiveOperationException e ) {
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        updateBrand(event.getPlayer());
    }
}

package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.enums.TimeUnit;
import arkadarktime.interfaces.BukkitConsole;
import arkadarktime.interfaces.modules.ModuleTicker;
import arkadarktime.utils.CustomUtils;
import arkadarktime.utils.FileManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

public class ServerBrandModule implements ModuleTicker, BukkitConsole {
    private final CookiesPower plugin;
    private final CustomUtils customUtils;
    private String channel;
    private String brand;
    private int brandIndex = 0;
    private int brandUpdateTaskId = -1;
    private final Class<?> pdscl;
    private boolean packetDataSerializerError = false;
    private boolean writeStringError = false;

    public ServerBrandModule(CookiesPower plugin) {
        this.plugin = plugin;
        this.customUtils = new CustomUtils(plugin);
        try {
            this.pdscl = Class.forName("net.minecraft.network.PacketDataSerializer");
        } catch ( ClassNotFoundException e ) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.server-brand.enable")) return;

        ModuleTicker.super.enable(plugin);

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
        initUpdateServerBrand();
        if (serverFileManager.getColoredStringList(null, "server-brand.texts").size() != 1) {
            long updateTime = customUtils.parseTime(serverFileManager.getString("server-brand.update-interval"), TimeUnit.TICKS);
            brandUpdateTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 0L, updateTime).getTaskId();
        }
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

        Bukkit.getOnlinePlayers().forEach(this::updateBrandForPlayer);
    }

    public void initUpdateServerBrand() {
        try {
            Class.forName("org.bukkit.entity.Dolphin");
            channel = "minecraft:brand";
        } catch ( ClassNotFoundException ignored ) {
            channel = "MC|Brand";
        }

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);

        this.update();
    }

    private void updateBrandForPlayer(Player player) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());

        if (channel == null) {
            return;
        }

        CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
        String coloredBrand = langFileManager.applyColorsAndPlaceholders(cookiesPlayer, this.brand, true);

        ByteBuf buf = getPacketDataSerializer();
        if (buf == null) return;

        if (writeString(buf, channel) || writeString(buf, coloredBrand)) return;

        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(0, data);

        try {
            WirePacket customPacket = new WirePacket(PacketType.Play.Server.CUSTOM_PAYLOAD, data);
            plugin.getProtocolManager().sendWirePacket(cookiesPlayer.getPlayer(), customPacket);
        } catch ( Throwable ignored ) {
        }
    }

    private ByteBuf getPacketDataSerializer() {
        try {
            Constructor<?> pdsclConstructor = pdscl.getConstructor(ByteBuf.class);
            return (ByteBuf) pdsclConstructor.newInstance(Unpooled.buffer());
        } catch ( Throwable t ) {
            if (!packetDataSerializerError) {
                packetDataSerializerError = true;
                Console(ConsoleType.ERROR, "Cannot create PacketDataSerializer ByteBuf: " + t.getMessage());
            }
            return null;
        }
    }

    private boolean writeString(Object buf, String data) {
        try {
            Method writeString = pdscl.getDeclaredMethod("a", String.class);
            writeString.invoke(buf, data);
            return false; // Success
        } catch ( Throwable t ) {
            if (!writeStringError) {
                writeStringError = true;
                Console(ConsoleType.ERROR, "<red>Cannot write string to PacketDataSerializer: " + t.getMessage());
            }
            return true; // Error
        }
    }

    @EventHandler()
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateBrandForPlayer(event.getPlayer());
    }
}

package arkadarktime.enums;

import arkadarktime.interfaces.BukkitConsole;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CookiesPlayer implements BukkitConsole {
    private final int id;
    private UUID uniqueId;
    private String name;
    private String displayName;
    private String ip;
    private boolean vanished;

    public CookiesPlayer(int id, UUID uniqueId, String name, String displayName, String ip, boolean isVanished) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.name = name;
        this.displayName = displayName;
        this.ip = ip;
        this.vanished = isVanished;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.getUniqueId());
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(this.getUniqueId());
    }

    public boolean isOnline() {
        try {
            return this.getPlayer() != null && this.getPlayer().isOnline();
        } catch ( Exception e ) {
            Console(ConsoleType.ERROR, "Error when check is online for player " + this.getDisplayName() + ": " + e.getMessage(), LineType.SIDE_LINES);
            e.printStackTrace();
        }
        return false;
    }

    public void sendMessage(@NotNull String... message) {
        try {
            this.getPlayer().sendMessage(message);
        } catch ( Exception e ) {
            Console(ConsoleType.ERROR, "Error when send message for player " + this.getDisplayName() + ": " + e.getMessage(), LineType.SIDE_LINES);
            e.printStackTrace();
        }
    }

    public void sendMessage(@NotNull BaseComponent... components) {
        try {
            this.getPlayer().spigot().sendMessage(components);
        } catch ( Exception e ) {
            Console(ConsoleType.ERROR, "Error when send message for player " + this.getDisplayName() + ": " + e.getMessage(), LineType.SIDE_LINES);
            e.printStackTrace();
        }
    }

    public void sendPluginMessage(Plugin plugin, String string, byte[] bytes) {
        try {
            this.getPlayer().sendPluginMessage(plugin, string, bytes);
        } catch ( Exception e ) {
            Console(ConsoleType.ERROR, "Error when send plugin message for player " + this.getDisplayName() + ": " + e.getMessage(), LineType.SIDE_LINES);
            e.printStackTrace();
        }
    }

    public int getPing() {
        CraftPlayer craftPlayer = (CraftPlayer) this.getOfflinePlayer();
        return craftPlayer.getPing();
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            this.getPlayer().sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } catch ( Exception e ) {
            Console(ConsoleType.ERROR, "Error when send title for player " + this.getDisplayName() + ": " + e.getMessage(), LineType.SIDE_LINES);
            e.printStackTrace();
        }
    }

    public boolean teleport(Location location) {
        try {
            return this.getPlayer().teleport(location);
        } catch ( Exception e ) {
            Console(ConsoleType.ERROR, "Error when teleport player " + this.getDisplayName() + ": " + e.getMessage(), LineType.SIDE_LINES);
            e.printStackTrace();
        }
        return false;
    }

    public void openInventory(Inventory inventory) {
        try {
            this.getPlayer().openInventory(inventory);
        } catch ( Exception e ) {
            Console(ConsoleType.ERROR, "Error when open inventory for player " + this.getDisplayName() + ": " + e.getMessage(), LineType.SIDE_LINES);
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
    }
}

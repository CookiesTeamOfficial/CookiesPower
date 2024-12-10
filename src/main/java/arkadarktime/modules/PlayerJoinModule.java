package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.interfaces.modules.ModuleListener;
import arkadarktime.utils.FileManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinModule implements ModuleListener {
    private final CookiesPower plugin;

    public PlayerJoinModule(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.join.enable")) return;

        ModuleListener.super.enable(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());

        String joinMessage = langFileManager.getColoredString(cookiesPlayer, "join.message");
        if (joinMessage == null || joinMessage.isEmpty()) {
            return;
        }

        joinMessage = joinMessage.replace("%player%", cookiesPlayer.getDisplayName());

        event.setJoinMessage(joinMessage);
    }
}
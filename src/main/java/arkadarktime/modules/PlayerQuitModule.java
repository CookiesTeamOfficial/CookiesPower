package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.interfaces.modules.ModuleListener;
import arkadarktime.utils.FileManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitModule implements ModuleListener {
    private final CookiesPower plugin;

    public PlayerQuitModule(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.quit.enable")) return;

        ModuleListener.super.enable(plugin);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());

        String quitMessage = langFileManager.getColoredString(cookiesPlayer, "quit.message");
        if (quitMessage == null || quitMessage.isEmpty()) {
            return;
        }

        quitMessage = quitMessage.replace("%player%", cookiesPlayer.getDisplayName());

        event.setQuitMessage(quitMessage);
    }
}

package arkadarktime.utils.pluginHooks;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.HookPlugin;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;

public class PlayerPointsHook implements HookPlugin {
    private final CookiesPower plugin;

    public PlayerPointsHook(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void hook() {
        String name = "PlayerPoints";
        if (Bukkit.getPluginManager().getPlugin(name) != null) {
            plugin.playerPointsAPI = PlayerPoints.getInstance().getAPI();
            sendMessage(name, true);
        } else {
            sendMessage(name, false);
        }
    }
}

package arkadarktime.utils.pluginHooks;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.HookPlugin;
import arkadarktime.utils.PlaceholderAPIManager;
import org.bukkit.Bukkit;

public class PlaceholderAPIHook implements HookPlugin {
    private final CookiesPower plugin;

    public PlaceholderAPIHook(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void hook() {
        String name = "PlaceholderAPI";
        if (Bukkit.getPluginManager().getPlugin(name) != null && Bukkit.getPluginManager().isPluginEnabled(name)) {
            plugin.setPlaceholderApiHooked(new PlaceholderAPIManager(plugin).register());

            if (plugin.isPlaceholderApiHooked()) {
                sendMessage(name, true);
                return;
            }
        }

        sendMessage(name, false);
    }
}

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
        if (Bukkit.getPluginManager().getPlugin(name) != null) {
            plugin.placeholderApiHooked = new PlaceholderAPIManager(plugin).register();
            sendMessage(name, true);
        } else {
            sendMessage(name, true);
        }
    }
}

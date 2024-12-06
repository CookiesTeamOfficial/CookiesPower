package arkadarktime.utils.pluginHooks;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.HookPlugin;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsHook implements HookPlugin {
    private final CookiesPower plugin;

    public LuckPermsHook(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void hook() {
        String name = "LuckPerms";
        if (Bukkit.getPluginManager().getPlugin(name) != null) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                plugin.luckPermsAPI = provider.getProvider();
            }
            sendMessage(name, true);
        } else {
            sendMessage(name, false);
        }
    }
}

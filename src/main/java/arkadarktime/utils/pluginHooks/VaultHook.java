package arkadarktime.utils.pluginHooks;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.HookPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook implements HookPlugin {
    private final CookiesPower plugin;

    public VaultHook(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void hook() {
        String name = "Vault";
        if (Bukkit.getPluginManager().getPlugin(name) != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                plugin.vaultAPI = rsp.getProvider();
                sendMessage(name, true);
            }
        } else {
            sendMessage(name, false);
        }
    }
}

package arkadarktime.utils.pluginHooks;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.HookPlugin;
import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;

public class ProtocolLibHook implements HookPlugin {
    private final CookiesPower plugin;

    public ProtocolLibHook(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void hook() {
        String name = "ProtocolLib";
        if (Bukkit.getPluginManager().getPlugin(name) != null && Bukkit.getPluginManager().isPluginEnabled(name)) {
            plugin.setProtocolManager(ProtocolLibrary.getProtocolManager());

            if (!plugin.getProtocolManager().isClosed() || plugin.getProtocolManager() != null) {
                sendMessage(name, true);
                return;
            }
        }

        sendMessage(name, false);
    }
}

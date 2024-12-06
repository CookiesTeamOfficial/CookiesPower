package arkadarktime.interfaces;

import arkadarktime.CookiesPower;
import org.bukkit.event.Listener;

public interface ModuleListener extends Module, Listener {
    default void registerListener(ModuleListener listener, CookiesPower plugin) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}

package arkadarktime.interfaces;

public interface HookPlugin extends BukkitConsole {
    void hook();

    default void sendMessage(String pluginName, boolean isFound) {
        String message;
        if (isFound) {
            message = "Plugin " + pluginName + " successfully found and hooked!";
            Console(ConsoleType.INFO, message, LineType.SIDE_LINES);
        } else {
            message = "Plugin " + pluginName + " is not installed, certain features may not be available.";
            Console(ConsoleType.WARN, message, LineType.SIDE_LINES);
        }
    }
}

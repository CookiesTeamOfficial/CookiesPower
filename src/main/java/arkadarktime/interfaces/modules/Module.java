package arkadarktime.interfaces.modules;

import arkadarktime.interfaces.BukkitConsole;

public interface Module extends BukkitConsole {
    default void enable() {
        this.enable(true);
    }

    default void disable() {
        this.disable(true);
    }

    default void enable(boolean log) {
        if (log) {
            Console(ConsoleType.INFO, "Module " + getFileName() + " enabled!", LineType.LINE);
        }
    }

    default void disable(boolean log) {
        if (log) {
            Console(ConsoleType.INFO, "Module " + getFileName() + " disabled!", LineType.LINE);
        }
    }

    default void restart() {
        this.disable(false);
        this.enable(false);
    }

    default String getFileName() {
        return getClass().getSimpleName();
    }
}

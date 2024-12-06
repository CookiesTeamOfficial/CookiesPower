package arkadarktime.interfaces;

import java.io.File;

public interface Module extends BukkitConsole {
    default void enable() {
        this.enable(true);
    }

    default void enable(boolean log) {
        if (log) {
            Console(ConsoleType.INFO, "Module " + getName() + " enabled!", LineType.LINE);
        }
    }

    default void disable() {
        this.disable(true);
    }

    default void disable(boolean log) {
        if (log) {
            Console(ConsoleType.INFO, "Module " + getName() + " disabled!", LineType.LINE);
        }
    }

    default void restart() {
        this.disable(false);
        this.enable(false);
    }

    default String getFileName() {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    default String getName() {
        return getClass().getSimpleName();
    }
}

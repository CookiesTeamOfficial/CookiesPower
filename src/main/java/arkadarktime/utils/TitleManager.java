package arkadarktime.utils;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TitleManager {
    private final CookiesPlayer cookiesPlayer;
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;
    private final CookiesPower plugin;

    public TitleManager(CookiesPlayer cookiesPlayer, String title, String subtitle, int fadeIn, int stay, int fadeOut, CookiesPower plugin) {
        this.cookiesPlayer = cookiesPlayer;
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
        this.plugin = plugin;
    }

    private BukkitTask scheduleTitleTask(long delay, long period, boolean isRepeating) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                cookiesPlayer.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            }
        };

        if (isRepeating) {
            return task.runTaskTimerAsynchronously(plugin, delay, period);
        } else if (delay > 0) {
            return task.runTaskLaterAsynchronously(plugin, delay);
        } else {
            return task.runTaskAsynchronously(plugin);
        }
    }

    public BukkitTask send() {
        return scheduleTitleTask(0, 0, false);
    }

    public BukkitTask send(long delay) {
        return scheduleTitleTask(delay, 0, false);
    }

    public BukkitTask send(long delay, long period) {
        return scheduleTitleTask(delay, period, true);
    }
}

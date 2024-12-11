package arkadarktime.enums;

import arkadarktime.CookiesPower;
import arkadarktime.utils.CustomUtils;
import arkadarktime.utils.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class TextAnimation {
    private final CookiesPower plugin;
    private final CustomUtils customUtils;
    private final String updateTime;
    private final List<String> text;
    private final Runnable onUpdate;
    private int currentIndex = -1;
    private BukkitTask task;

    public TextAnimation(CookiesPower plugin, String updateTime, List<String> text, Runnable onUpdate) {
        this.plugin = plugin;
        this.updateTime = updateTime;
        this.text = text;
        this.customUtils = new CustomUtils(plugin);
        this.onUpdate = onUpdate;
    }

    public void start() {
        stop();
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::update, 0, getUpdateTime());
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void update() {
        if (text.isEmpty()) {
            return;
        }

        if (currentIndex == -1) {
            currentIndex = 0;
        } else {
            currentIndex = (currentIndex + 1) % text.size();
        }

        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    public long getUpdateTime() {
        return customUtils.parseTime(updateTime, TimeUnit.TICKS);
    }

    public String getCurrentText() {
        FileManager textAnimationsFileManager = new FileManager(plugin, plugin.getAnimationsFile());
        if (text.isEmpty()) {
            return "";
        }

        if (currentIndex < 0 || currentIndex >= text.size()) {
            currentIndex = 0;
        }

        return textAnimationsFileManager.applyColorsAndPlaceholders(null, text.get(currentIndex), false);
    }
}

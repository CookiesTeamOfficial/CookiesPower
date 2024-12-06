package arkadarktime.utils;

import arkadarktime.CookiesPower;
import arkadarktime.enums.TextAnimation;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationsManager {
    private final CookiesPower plugin;
    private final Map<String, TextAnimation> animations = new ConcurrentHashMap<>();

    public AnimationsManager(CookiesPower plugin) {
        this.plugin = plugin;
        loadAnimations();
    }

    public void loadAnimations() {
        animations.clear();
        FileManager animationsFileManager = new FileManager(plugin, plugin.getAnimationsFile());
        ConfigurationSection animSection = animationsFileManager.getConfigurationSection("animations");
        if (animSection != null) {
            animSection.getKeys(false).forEach(anim -> {
                ConfigurationSection animConfig = animSection.getConfigurationSection(anim);
                if (animConfig != null) {
                    String updateTime = animConfig.getString("change-interval");
                    List<String> texts = animConfig.getStringList("texts");
                    if (!texts.isEmpty()) {
                        animations.put(anim, new TextAnimation(plugin, updateTime, texts));
                    }
                }
            });
        }
    }

    public String replaceAnimations(String text) {
        for (Map.Entry<String, TextAnimation> entry : getAnimations().entrySet()) {
            String currentText = entry.getValue().getCurrentText();
            text = text.replace("%anim:" + entry.getKey() + "%", currentText);
        }
        return text;
    }

    public Map<String, TextAnimation> getAnimations() {
        return animations;
    }

    public TextAnimation getAnimation(String name) {
        return animations.get(name);
    }

    public void startAnimations() {
        animations.forEach((name, anim) -> {
            anim.start();
            plugin.tablistModule.update();
            plugin.serverMotdModule.update();
            plugin.serverBrandModule.update();
        });
    }

    public void stopAnimations() {
        animations.values().forEach(TextAnimation::stop);
    }
}

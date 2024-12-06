package arkadarktime.utils;

import arkadarktime.CookiesPower;
import arkadarktime.enums.TextAnimation;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIManager extends PlaceholderExpansion {
    private final CookiesPower plugin;
    private final AnimationsManager animationsManager;

    public PlaceholderAPIManager(CookiesPower plugin) {
        this.plugin = plugin;
        this.animationsManager = plugin.getAnimationsManager();
    }

    @Override
    public @NotNull String getAuthor() {
        return "ArkaDarkTime";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cookiespower";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        if (player == null) {
            return "need a player";
        }

        if (placeholder.startsWith("anim:")) {
            String animationName = placeholder.substring(5);
            TextAnimation animation = animationsManager.getAnimation(animationName);

            if (animation != null) {
                return animation.getCurrentText();
            } else {
                return "Unknown animation";
            }
        }

        return placeholder;
    }
}
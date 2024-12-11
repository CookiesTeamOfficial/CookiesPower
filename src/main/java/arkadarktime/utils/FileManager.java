package arkadarktime.utils;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.interfaces.BukkitConsole;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileManager extends FileConfiguration implements BukkitConsole {
    private final CookiesPower plugin;
    private final FileConfiguration fileConfiguration;

    // Паттерны для HEX и градиентов
    private static final Pattern HEX_PATTERN = Pattern.compile("(#|&#|§#)([0-9A-Fa-f]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("(<gradient:)(#|&#|§#)([0-9A-Fa-f]{6})(>)(.*?)(<#|<&#|<§#)([0-9A-Fa-f]{6})(>)", Pattern.CASE_INSENSITIVE);

    public FileManager(CookiesPower plugin, FileConfiguration fileConfiguration) {
        this.plugin = plugin;
        this.fileConfiguration = fileConfiguration;
    }

    private boolean isValidConfiguration(String key) {
        return isFileConfigurationNotNull() && fileConfiguration.contains(key);
    }

    private void logInvalidKeyError(String key) {
        Console(ConsoleType.ERROR, "Key not found or invalid key: " + key, LineType.SIDE_LINES);
    }

    private List<String> getListFromConfig(String key) {
        if (isValidConfiguration(key)) {
            Object message = fileConfiguration.get(key);
            if (message instanceof List<?>) {
                return ((List<?>) message).stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .collect(Collectors.toList());
            } else if (message instanceof String) {
                return Collections.singletonList((String) message);
            }
        }
        logInvalidKeyError(key);
        return Collections.emptyList();
    }

    public int getInt(@NotNull String key) {
        return isValidConfiguration(key) ? fileConfiguration.getInt(key) : 0;
    }

    public double getDouble(@NotNull String key) {
        return isValidConfiguration(key) ? fileConfiguration.getDouble(key) : 0.0;
    }

    public boolean getBoolean(@NotNull String key) {
        return isValidConfiguration(key) && fileConfiguration.getBoolean(key);
    }

    public String getString(@NotNull String key) {
        if (isValidConfiguration(key)) {
            Object message = fileConfiguration.get(key);
            if (message instanceof String) {
                return (String) message;
            } else if (message instanceof List<?>) {
                List<String> formattedMessages = ((List<?>) message).stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .collect(Collectors.toList());
                return String.join("\n", formattedMessages);
            }
        }
        logInvalidKeyError(key);
        return "null";
    }

    public @NotNull List<String> getStringList(@NotNull String key) {
        return getListFromConfig(key);
    }

    public List<String> getColoredStringList(CookiesPlayer cookiesPlayer, String key) {
        return getListFromConfig(key).stream()
                .map(msg -> applyColorsAndPlaceholders(cookiesPlayer, msg, true))
                .collect(Collectors.toList());
    }

    public String getColoredString(CommandSender sender, String key) {
        CookiesPlayer cookiesPlayerOrNull = getCookiesPlayerOrNull(sender);
        return getColoredString(cookiesPlayerOrNull, key);
    }

    public String getColoredString(CookiesPlayer cookiesPlayer, String key) {
        return getColoredString(cookiesPlayer, key, true);
    }

    public String getColoredString(CookiesPlayer cookiesPlayer, String key, boolean animations) {
        if (isValidConfiguration(key)) {
            Object message = fileConfiguration.get(key);
            if (message instanceof String) {
                return applyColorsAndPlaceholders(cookiesPlayer, (String) message, animations);
            } else if (message instanceof List<?>) {
                return ((List<?>) message).stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(msg -> applyColorsAndPlaceholders(cookiesPlayer, msg, animations))
                        .collect(Collectors.joining("\n"));
            }
        }
        logInvalidKeyError(key);
        return "null";
    }

    public ConfigurationSection getConfigurationSection(@NotNull String key) {
        if (isValidConfiguration(key)) {
            Object section = fileConfiguration.get(key);
            if (section instanceof ConfigurationSection) {
                return (ConfigurationSection) section;
            } else {
                logInvalidKeyError(key);
            }
        }
        logInvalidKeyError(key);
        return null;
    }

    public String applyColorsAndPlaceholders(CookiesPlayer cookiesPlayer, String message, boolean animations) {
        String prefix = plugin.getLangFile().getString("prefix");
        if (prefix != null) {
            message = message.replace("%plugin-prefix%", prefix);
        }

        if (plugin.isPlaceholderApiHooked()) {
            Player player = cookiesPlayer != null ? cookiesPlayer.getPlayer() : null;
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        if (animations) {
            message = plugin.getAnimationsManager().replaceAnimations(message);
        }

        message = applyGradient(message);
        message = convertHexColors(message);

        return message;
    }

    private String convertHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String hexCode = matcher.group(2);
            try {
                String color = ChatColor.of("#" + hexCode).toString();
                message = message.replace(matcher.group(0), color);
            } catch ( IllegalArgumentException e ) {
                Console(ConsoleType.ERROR, "Invalid HEX color code: \"" + hexCode + "\"");
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String applyGradient(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        while (matcher.find()) {
            String startColorHex = matcher.group(3);
            String endColorHex = matcher.group(7);
            String gradientText = matcher.group(5);

            if (isInvalidHexColor("#" + startColorHex) || isInvalidHexColor("#" + endColorHex)) {
                Console(ConsoleType.ERROR, "Invalid HEX color format: \"" + startColorHex + "\" or \"" + endColorHex + "\"");
                continue;
            }

            String gradient = createGradient(gradientText, startColorHex, endColorHex);
            message = message.replace(matcher.group(0), gradient);
        }
        return message;
    }

    private String createGradient(String text, String startColor, String endColor) {
        StringBuilder gradientText = new StringBuilder();
        int textLength = text.length();
        for (int i = 0; i < textLength; i++) {
            String color = blendColors(startColor, endColor, i, textLength);
            gradientText.append(color).append(text.charAt(i));
        }
        return gradientText.toString();
    }

    private String blendColors(String startColor, String endColor, int index, int totalLength) {
        if (isInvalidHexColor("#" + startColor) || isInvalidHexColor("#" + endColor)) {
            Console(ConsoleType.ERROR, "Invalid HEX color format: \"" + startColor + "\" or \"" + endColor + "\"");
            return startColor;
        }

        float ratio = (float) index / totalLength;
        int startRed = Integer.parseInt(startColor.substring(0, 2), 16);
        int startGreen = Integer.parseInt(startColor.substring(2, 4), 16);
        int startBlue = Integer.parseInt(startColor.substring(4, 6), 16);

        int endRed = Integer.parseInt(endColor.substring(0, 2), 16);
        int endGreen = Integer.parseInt(endColor.substring(2, 4), 16);
        int endBlue = Integer.parseInt(endColor.substring(4, 6), 16);

        int red = (int) (startRed + (endRed - startRed) * ratio);
        int green = (int) (startGreen + (endGreen - startGreen) * ratio);
        int blue = (int) (startBlue + (endBlue - startBlue) * ratio);

        return ChatColor.of(String.format("#%02X%02X%02X", red, green, blue)).toString();
    }

    private boolean isInvalidHexColor(String color) {
        return !color.matches("^#([0-9A-Fa-f]{6})$");
    }

    public void reload() throws InvalidConfigurationException {
        loadFromString(saveToString());
    }

    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        fileConfiguration.loadFromString(contents);
    }

    public @NotNull String saveToString() {
        return fileConfiguration.saveToString();
    }

    private CookiesPlayer getCookiesPlayerOrNull(CommandSender sender) {
        if (sender instanceof Player player) {
            return plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
        }
        return null;
    }

    private boolean isFileConfigurationNotNull() {
        return fileConfiguration != null;
    }
}

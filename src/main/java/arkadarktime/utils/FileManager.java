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

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileManager extends FileConfiguration implements BukkitConsole {
    private final CookiesPower plugin;
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)([#&§])?#([a-fA-F0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<" + HEX_PATTERN.pattern() + ">(.*?)<" + HEX_PATTERN.pattern() + ">");
    private static final char COLOR_CHAR = '§';
    private final FileConfiguration fileConfiguration;

    public FileManager(CookiesPower plugin, FileConfiguration fileConfiguration) {
        this.plugin = plugin;
        this.fileConfiguration = fileConfiguration;
    }

    // Получение значения типа int из конфигурации по ключу
    public int getInt(@NotNull String key) {
        return isFileConfigurationNotNull() && fileConfiguration.contains(key) ? fileConfiguration.getInt(key) : 0;
    }

    // Получение значения типа double из конфигурации по ключу
    public double getDouble(@NotNull String key) {
        return isFileConfigurationNotNull() && fileConfiguration.contains(key) ? fileConfiguration.getDouble(key) : 0.0;
    }

    // Получение значения типа boolean из конфигурации по ключу
    public boolean getBoolean(@NotNull String key) {
        return isFileConfigurationNotNull() && fileConfiguration.contains(key) && fileConfiguration.getBoolean(key);
    }

    // Получение строки из конфигурации по ключу, обработка листов сообщений
    public String getString(@NotNull String key) {
        if (isFileConfigurationNotNull() && fileConfiguration.contains(key)) {
            Object message = fileConfiguration.get(key);
            if (message instanceof String) {
                return (String) message;
            } else if (message instanceof List<?> messagesList) {
                if (!messagesList.isEmpty() && messagesList.get(0) instanceof String) {
                    List<String> formattedMessages = messagesList.stream().map(msg -> (String) msg).collect(Collectors.toList());
                    return String.join("\n", formattedMessages);
                }
            }
        }
        Console(ConsoleType.ERROR, "Key not found or invalid key: " + key, LineType.SIDE_LINES);
        return "null";
    }

    // Получение списка строк с цветами и плейсхолдерами для игрока
    public List<String> getColoredStringList(CookiesPlayer cookiesPlayer, String key) {
        if (isFileConfigurationNotNull() && fileConfiguration.contains(key)) {
            Object message = fileConfiguration.get(key);
            if (message instanceof List<?> messagesList) {
                if (!messagesList.isEmpty() && messagesList.get(0) instanceof String) {
                    return messagesList.stream().map(msg -> applyColorsAndPlaceholders(cookiesPlayer, (String) msg, true)).collect(Collectors.toList());
                }
            } else if (message instanceof String singleMessage) {
                if (!singleMessage.trim().isEmpty()) {
                    return Collections.singletonList(applyColorsAndPlaceholders(cookiesPlayer, singleMessage, true));
                }
            }
        }
        Console(ConsoleType.ERROR, "Key not found or invalid key: " + key, LineType.SIDE_LINES);
        return Collections.emptyList();
    }

    // Получение строки с цветами и плейсхолдерами для отправителя
    public String getColoredString(CommandSender sender, String key) {
        CookiesPlayer cookiesPlayerOrNull = getCookiesPlayerOrNull(sender);
        return getColoredString(cookiesPlayerOrNull, key);
    }

    // Получение строки с цветами и плейсхолдерами для игрока
    public String getColoredString(CookiesPlayer cookiesPlayer, String key) {
        return getColoredString(cookiesPlayer, key, true);
    }

    // Получение строки с цветами и плейсхолдерами для игрока, с возможностью анимации
    public String getColoredString(CookiesPlayer cookiesPlayer, String key, boolean animations) {
        if (isFileConfigurationNotNull() && fileConfiguration.contains(key)) {
            Object message = fileConfiguration.get(key);
            if (message instanceof String) {
                return applyColorsAndPlaceholders(cookiesPlayer, (String) message, animations);
            } else if (message instanceof List<?> messagesList) {
                return messagesList.stream().filter(msg -> msg instanceof String).map(msg -> applyColorsAndPlaceholders(cookiesPlayer, (String) msg, animations)).collect(Collectors.joining("\n"));
            }
        }
        Console(ConsoleType.ERROR, "Key not found or invalid key: " + key, LineType.SIDE_LINES);
        return "null";
    }

    // Получение секции конфигурации по ключу
    public ConfigurationSection getConfigurationSection(@NotNull String key) {
        if (isFileConfigurationNotNull() && fileConfiguration.contains(key)) {
            Object section = fileConfiguration.get(key);
            if (section instanceof ConfigurationSection) {
                return (ConfigurationSection) section;
            } else {
                Console(ConsoleType.ERROR, "Key does not point to a ConfigurationSection: " + key, LineType.SIDE_LINES);
            }
        }
        Console(ConsoleType.ERROR, "Key not found or invalid key: " + key, LineType.SIDE_LINES);
        return null;
    }

    // Применение цветов и плейсхолдеров к строке с возможностью анимации
    public String applyColorsAndPlaceholders(CookiesPlayer cookiesPlayer, String message, boolean animations) {
        if (message == null) {
            return "";
        }

        if (plugin.placeholderApiHooked) {
            Player player = cookiesPlayer != null ? cookiesPlayer.getPlayer() : null;
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        String prefix = plugin.getLangFile().getString("prefix");
        if (prefix != null) {
            message = message.replace("%plugin-prefix%", prefix);
        }

        if (animations) {
            message = plugin.getAnimationsManager().replaceAnimations(message);
        }

        message = applyHexAndGradient(message);

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // Применение HEX цветов и градиентов к строке
    private String applyHexAndGradient(String message) {
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (gradientMatcher.find()) {
            String startColor = gradientMatcher.group(2);
            String content = gradientMatcher.group(3);
            String endColor = gradientMatcher.group(5);

            String gradientPart = createGradient(startColor, endColor, content);
            gradientMatcher.appendReplacement(buffer, gradientPart);
        }
        gradientMatcher.appendTail(buffer);

        StringBuilder finalMessage = getStringBuilder(buffer);

        return ChatColor.translateAlternateColorCodes('&', finalMessage.toString());
    }

    // Создание строки с градиентом
    private static @NotNull StringBuilder getStringBuilder(StringBuilder buffer) {
        Matcher hexMatcher = HEX_PATTERN.matcher(buffer.toString());
        StringBuilder finalMessage = new StringBuilder(buffer.length() + 4 * 8);
        while (hexMatcher.find()) {
            String hexCode = hexMatcher.group(2);
            hexMatcher.appendReplacement(finalMessage, COLOR_CHAR + "x" + COLOR_CHAR + hexCode.charAt(0) + COLOR_CHAR + hexCode.charAt(1) + COLOR_CHAR + hexCode.charAt(2) + COLOR_CHAR + hexCode.charAt(3) + COLOR_CHAR + hexCode.charAt(4) + COLOR_CHAR + hexCode.charAt(5));
        }
        hexMatcher.appendTail(finalMessage);
        return finalMessage;
    }

    // Создание градиента от одного цвета к другому
    public String createGradient(String startColor, String endColor, String content) {
        StringBuilder gradient = new StringBuilder();
        int contentLength = content.length();
        int startRed = Integer.parseInt(startColor.substring(0, 2), 16);
        int startGreen = Integer.parseInt(startColor.substring(2, 4), 16);
        int startBlue = Integer.parseInt(startColor.substring(4, 6), 16);
        int endRed = Integer.parseInt(endColor.substring(0, 2), 16);
        int endGreen = Integer.parseInt(endColor.substring(2, 4), 16);
        int endBlue = Integer.parseInt(endColor.substring(4, 6), 16);

        for (int i = 0; i < contentLength; i++) {
            double ratio = (double) i / (contentLength - 1);
            int red = (int) (startRed + ratio * (endRed - startRed));
            int green = (int) (startGreen + ratio * (endGreen - startGreen));
            int blue = (int) (startBlue + ratio * (endBlue - startBlue));

            ChatColor color = ChatColor.of(new Color(red, green, blue));
            gradient.append(color).append(content.charAt(i));
        }

        return gradient.toString();
    }

    // Перезапись конфигурации
    public void reload() throws InvalidConfigurationException {
        loadFromString(saveToString());
    }

    // Загрузка конфигурации из строки
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        fileConfiguration.loadFromString(contents);
    }

    // Сохранение конфигурации в строку
    public @NotNull String saveToString() {
        return fileConfiguration.saveToString();
    }

    // Получение объекта CookiesPlayer для игрока или null
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

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
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("(<gradient:)(#|&#|§#)([0-9A-Fa-f]{6})(>)(.*?)(</#|&#|§#)([0-9A-Fa-f]{6})(>)", Pattern.CASE_INSENSITIVE);

    public FileManager(CookiesPower plugin, FileConfiguration fileConfiguration) {
        this.plugin = plugin;
        this.fileConfiguration = fileConfiguration;
    }

    /**
     * Получает значение типа int из конфигурации по ключу.
     *
     * @param key ключ конфигурации
     * @return значение типа int или 0, если ключ не найден
     */
    public int getInt(@NotNull String key) {
        return isFileConfigurationNotNull() && fileConfiguration.contains(key) ? fileConfiguration.getInt(key) : 0;
    }

    /**
     * Получает значение типа double из конфигурации по ключу.
     *
     * @param key ключ конфигурации
     * @return значение типа double или 0.0, если ключ не найден
     */
    public double getDouble(@NotNull String key) {
        return isFileConfigurationNotNull() && fileConfiguration.contains(key) ? fileConfiguration.getDouble(key) : 0.0;
    }

    /**
     * Получает значение типа boolean из конфигурации по ключу.
     *
     * @param key ключ конфигурации
     * @return значение типа boolean или false, если ключ не найден
     */
    public boolean getBoolean(@NotNull String key) {
        return isFileConfigurationNotNull() && fileConfiguration.contains(key) && fileConfiguration.getBoolean(key);
    }

    /**
     * Получает строку из конфигурации по ключу, обработав её для отображения в виде списка строк с переносами.
     *
     * @param key ключ конфигурации
     * @return строка из конфигурации или "null", если ключ не найден или недействителен
     */
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

    /**
     * Получает список строк с цветами и плейсхолдерами для игрока из конфигурации.
     *
     * @param cookiesPlayer игрок
     * @param key           ключ конфигурации
     * @return список строк с цветами и плейсхолдерами
     */
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

    /**
     * Получает строку с цветами и плейсхолдерами для отправителя.
     *
     * @param sender отправитель команды
     * @param key    ключ конфигурации
     * @return строка с цветами и плейсхолдерами для отправителя
     */
    public String getColoredString(CommandSender sender, String key) {
        CookiesPlayer cookiesPlayerOrNull = getCookiesPlayerOrNull(sender);
        return getColoredString(cookiesPlayerOrNull, key);
    }

    /**
     * Получает строку с цветами и плейсхолдерами для игрока.
     *
     * @param cookiesPlayer игрок
     * @param key           ключ конфигурации
     * @return строка с цветами и плейсхолдерами для игрока
     */
    public String getColoredString(CookiesPlayer cookiesPlayer, String key) {
        return getColoredString(cookiesPlayer, key, true);
    }

    /**
     * Получает строку с цветами и плейсхолдерами для игрока с возможностью анимации.
     *
     * @param cookiesPlayer игрок
     * @param key           ключ конфигурации
     * @param animations    флаг для анимации
     * @return строка с цветами и плейсхолдерами, возможно с анимацией
     */
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

    /**
     * Получает секцию конфигурации по ключу.
     *
     * @param key ключ конфигурации
     * @return секция конфигурации или null, если ключ не найден
     */
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

    /**
     * Применяет цвета и плейсхолдеры к строке с возможностью анимации.
     *
     * @param cookiesPlayer игрок
     * @param message       строка для обработки
     * @param animations    флаг для анимации
     * @return обработанная строка
     */
    public String applyColorsAndPlaceholders(CookiesPlayer cookiesPlayer, String message, boolean animations) {
        message = applyGradient(message);
        message = convertHexColors(message);

        String prefix = plugin.getLangFile().getString("prefix");
        if (prefix != null) {
            message = message.replace("%plugin-prefix%", prefix);
        }

        if (plugin.placeholderApiHooked) {
            Player player = cookiesPlayer != null ? cookiesPlayer.getPlayer() : null;
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        if (animations) {
            message = plugin.getAnimationsManager().replaceAnimations(message);
        }

        return message;
    }

    /**
     * Преобразует строку с HEX цветами в формат, поддерживаемый Bukkit.
     *
     * @param message строка с HEX цветами
     * @return строка с преобразованными цветами
     */
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

    /**
     * Применяет градиентные цвета к строке.
     *
     * @param message строка с градиентом
     * @return строка с применённым градиентом
     */
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

    /**
     * Создаёт градиентный текст из строки.
     *
     * @param text       текст для градиента
     * @param startColor начальный цвет
     * @param endColor   конечный цвет
     * @return строка с градиентом
     */
    private String createGradient(String text, String startColor, String endColor) {
        StringBuilder gradientText = new StringBuilder();
        int textLength = text.length();

        for (int i = 0; i < textLength; i++) {
            String color = blendColors(startColor, endColor, i, textLength);
            gradientText.append(color).append(text.charAt(i));
        }

        return gradientText.toString();
    }

    /**
     * Смешивает два цвета на основе индекса и общего количества символов.
     *
     * @param startColor  начальный цвет
     * @param endColor    конечный цвет
     * @param index       текущий индекс символа
     * @param totalLength общая длина текста
     * @return смешанный цвет
     */
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

    /**
     * Перезагружает конфигурацию.
     *
     * @throws InvalidConfigurationException исключение, если конфигурация некорректна
     */
    public void reload() throws InvalidConfigurationException {
        loadFromString(saveToString());
    }

    /**
     * Загружает конфигурацию из строки.
     *
     * @param contents строка с конфигурацией
     * @throws InvalidConfigurationException исключение, если конфигурация некорректна
     */
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        fileConfiguration.loadFromString(contents);
    }

    /**
     * Сохраняет конфигурацию в строку.
     *
     * @return строка, представляющая конфигурацию
     */
    public @NotNull String saveToString() {
        return fileConfiguration.saveToString();
    }

    /**
     * Получает объект CookiesPlayer для игрока или null.
     *
     * @param sender отправитель команды
     * @return объект CookiesPlayer или null, если отправитель не игрок
     */
    private CookiesPlayer getCookiesPlayerOrNull(CommandSender sender) {
        if (sender instanceof Player player) {
            return plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
        }
        return null;
    }

    /**
     * Проверяет, не является ли конфигурация пустой.
     *
     * @return true, если конфигурация не пуста
     */
    private boolean isFileConfigurationNotNull() {
        return fileConfiguration != null;
    }
}
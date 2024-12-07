package arkadarktime.utils;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.enums.TimeUnit;
import arkadarktime.interfaces.BukkitConsole;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomUtils implements BukkitConsole {
    private final CookiesPower plugin;

    public CustomUtils(CookiesPower plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a "no permission" error message to the sender.
     *
     * @param sender The command sender.
     * @param perm   The required permission.
     */
    public void sendNoPermissionError(CommandSender sender, String perm) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());
        sender.sendMessage(langFileManager.getColoredString(sender, "commands.no-permission").replace("%perm%", perm));
    }

    /**
     * Sends a "player not found" error message to the sender by cookiesPlayer.
     *
     * @param sender        The command sender.
     * @param cookiesPlayer The cookiesPlayer object.
     */
    public void sendPlayerNotFoundError(CommandSender sender, CookiesPlayer cookiesPlayer) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());
        sender.sendMessage(langFileManager.getColoredString(sender, "commands.player-not-found").replace("%cookiesPlayer%", cookiesPlayer.getDisplayName()));
    }

    /**
     * Sends a "player not found" error message to the sender by player.
     *
     * @param sender The command sender.
     * @param player The player object.
     */
    public void sendPlayerNotFoundError(CommandSender sender, Player player) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());
        sender.sendMessage(langFileManager.getColoredString(sender, "commands.player-not-found").replace("%player%", player.getDisplayName()));
    }

    /**
     * Sends a "player not found" error message to the sender by player name.
     *
     * @param sender     The command sender.
     * @param playerName The player's name.
     */
    public void sendPlayerNotFoundError(CommandSender sender, String playerName) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());
        sender.sendMessage(langFileManager.getColoredString(sender, "commands.player-not-found").replace("%player%", playerName));
    }

    /**
     * Checks if the given string is numeric.
     *
     * @param str The string to check.
     * @return True if the string is numeric, false otherwise.
     */
    public boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Double.parseDouble(str);
            return true;
        } catch ( NumberFormatException e ) {
            return false;
        }
    }

    /**
     * Converts milliseconds to a Date, formatted as dd.MM.yy HH:mm:ss.
     *
     * @param milliseconds The time in milliseconds.
     * @return The Date object formatted as dd.MM.yy HH:mm:ss, or null if an error occurs.
     */
    public String formatTime(long milliseconds) {
        FileManager configFileManager = new FileManager(plugin, plugin.getConfig());
        Date date = new Date(milliseconds);
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(configFileManager.getString("time-format"));
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(configFileManager.getString("timezone")));
            return simpleDateFormat.format(date);
        } catch ( IllegalArgumentException e ) {
            e.printStackTrace();
            Console(ConsoleType.ERROR, "Invalid time-format in config!", LineType.SIDE_LINES);
        }
        return null;
    }

    /**
     * Converts milliseconds to a Date, formatted using the provided format pattern.
     *
     * @param formatPattern The date format pattern (e.g., "dd.MM.yy HH:mm:ss").
     * @param milliseconds  The time in milliseconds.
     * @return The Date object formatted according to the provided pattern, or null if an error occurs.
     */
    public String formatTime(String formatPattern, long milliseconds) {
        FileManager configFileManager = new FileManager(plugin, plugin.getConfig());
        Date date = new Date(milliseconds);
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatPattern);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(configFileManager.getString("timezone")));
            return simpleDateFormat.format(date);
        } catch ( IllegalArgumentException e ) {
            e.printStackTrace();
            Console(ConsoleType.ERROR, "Invalid format pattern in config!", LineType.SIDE_LINES);
        }
        return null;
    }

    /**
     * Parses a time string into the specified time unit.
     * Supports formats like "5 s, 10 m 1h 1d" (English / Russian / Ukrainian) and returns the total in the specified unit.
     *
     * @param time The time string to parse.
     * @param unit The unit to convert the time to.
     * @return The time in the specified unit.
     */
    public long parseTime(String time, TimeUnit unit) {
        double totalMilliseconds = 0;
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)(\\s*[a-zA-Zа-яА-Я]+)");
        Matcher matcher = pattern.matcher(time.toLowerCase());

        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            String unitString = matcher.group(2).trim();

            switch (unitString) {
                case "ms", "milliseconds", "мс", "миллисекунд", "миллисекунда", "миллисекунды", "мілісекунд",
                     "мілісекунда", "мілісекунди" -> totalMilliseconds += value; // milliseconds
                case "s", "sec", "second", "seconds", "с", "сек", "секунд", "секунда", "секунды", "секунди" ->
                        totalMilliseconds += value * 1000; // seconds
                case "m", "min", "minute", "minutes", "м", "мин", "минут", "хв", "хвилин", "хвилина", "хвилини" ->
                        totalMilliseconds += value * 60000; // minutes
                case "h", "hour", "hours", "ч", "час", "часа", "часов", "годин", "година", "години" ->
                        totalMilliseconds += value * 3600000; // hours
                case "d", "day", "days", "д", "дн", "дня", "дней" -> totalMilliseconds += value * 86400000; // days
                case "w", "week", "weeks", "нед", "неделя", "недель", "тиж", "тижні", "тиждень", "тижнів" ->
                        totalMilliseconds += value * 604800000; // weeks
                case "y", "year", "years", "г", "год", "года", "л", "лет", "р", "рік", "роки", "років" ->
                        totalMilliseconds += value * 31536000000L; // years
                default -> {
                    Console(ConsoleType.ERROR, "Unknown time format: " + unitString, LineType.SIDE_LINES);
                    return 0;
                }
            }
        }

        // Конвертация totalMilliseconds в нужный TimeUnit
        return switch (unit) {
            case NANOSECONDS -> (long) (totalMilliseconds * 1_000_000); // миллисекунды в наносекунды
            case MICROSECONDS -> (long) (totalMilliseconds * 1_000); // миллисекунды в микросекунды
            case MILLISECONDS -> (long) totalMilliseconds; // уже в миллисекундах
            case TICKS -> (long) (totalMilliseconds / 50); // миллисекунды в тики
            case SECONDS -> (long) (totalMilliseconds / 1_000); // миллисекунды в секунды
            case MINUTES -> (long) (totalMilliseconds / 60_000); // миллисекунды в минуты
            case HOURS -> (long) (totalMilliseconds / 3_600_000); // миллисекунды в часы
            case DAYS -> (long) (totalMilliseconds / 86_400_000); // миллисекунды в дни
            case YEARS -> (long) (totalMilliseconds / 31_536_000_000L); // миллисекунды в годы
        };
    }

    /**
     * Parses a location string and returns a Location object.
     *
     * @param locationStr The location string in the format "world,x,y,z,pitch,yaw".
     * @return The Location object, or null if the string is invalid.
     */
    public Location parseLocation(String locationStr) {
        if (locationStr == null) {
            return null;
        }
        String[] parts = locationStr.split(",");
        if (parts.length < 6) {
            return null;
        }

        try {
            String worldName = parts[0].replaceAll(".*CraftWorld\\{name=(.*)}.*", "$1").trim();
            double x = Double.parseDouble(parts[1].replaceAll("[^0-9.-]", "").trim());
            double y = Double.parseDouble(parts[2].replaceAll("[^0-9.-]", "").trim());
            double z = Double.parseDouble(parts[3].replaceAll("[^0-9.-]", "").trim());
            float pitch = Float.parseFloat(parts[4].replaceAll("[^0-9.-]", "").trim());
            float yaw = Float.parseFloat(parts[5].replaceAll("[^0-9.-]", "").trim());

            World world = Bukkit.getServer().getWorld(worldName);
            if (world == null) {
                Console(ConsoleType.INFO, "World not found: " + worldName, LineType.SIDE_LINES);
                return null;
            }
            return new Location(world, x, y, z, yaw, pitch);
        } catch ( NumberFormatException e ) {
            Console(ConsoleType.INFO, "Invalid number format: " + e.getMessage(), LineType.SIDE_LINES);
            return null;
        }
    }

    /**
     * Validates and sanitizes a namespaced key string.
     *
     * @param namespacedKeyStr The namespaced key string to validate.
     * @return The sanitized namespaced key string.
     */
    public String validateNamespacedKey(String namespacedKeyStr) {
        return namespacedKeyStr.replaceAll("[^a-z0-9/._-]", "_");
    }

    /**
     * Parses a BarColor from a string.
     *
     * @param str The string to parse.
     * @return The corresponding BarColor, or RED if the string is invalid.
     */
    public BarColor parseBarColor(String str) {
        try {
            return BarColor.valueOf(str);
        } catch ( IllegalArgumentException e ) {
            Console(ConsoleType.WARN, "Unsupported bossbar color: " + str + ". Check your config!", LineType.SIDE_LINES);
            return BarColor.RED;
        }
    }

    /**
     * Parses a BarStyle from a string.
     *
     * @param str The string to parse.
     * @return The corresponding BarStyle, or SOLID if the string is invalid.
     */
    public BarStyle parseBarStyle(String str) {
        try {
            return BarStyle.valueOf(str);
        } catch ( IllegalArgumentException e ) {
            Console(ConsoleType.WARN, "Unsupported bossbar style: " + str + ". Check your config!", LineType.SIDE_LINES);
            return BarStyle.SOLID;
        }
    }

    /**
     * Parses a BarStyle from a string.
     *
     * @param str The string to parse.
     * @return The corresponding BarStyle, or SOLID if the string is invalid.
     */
    public ClickEvent.Action parseClickEventAction(String str) {
        try {
            return ClickEvent.Action.valueOf(str);
        } catch ( IllegalArgumentException e ) {
            Console(BukkitConsole.ConsoleType.WARN, "Unsupported click event action: " + str + ". Check your chat.yml!", BukkitConsole.LineType.SIDE_LINES);
            return null;
        }
    }

    /**
     * Executes a command on behalf of the player.
     *
     * @param cookiesPlayer The player to execute the command for.
     * @param command       The command to execute.
     */
    public void executeCommand(CookiesPlayer cookiesPlayer, String command) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());

        Pattern pattern = Pattern.compile("\\[(message|title)]\\s+(.+)");
        Matcher matcher = pattern.matcher(command);

        if (matcher.find()) {
            String commandType = matcher.group(1);
            String commandText = matcher.group(2);

            switch (commandType) {
                case "message":
                    cookiesPlayer.sendMessage(langFileManager.applyColorsAndPlaceholders(cookiesPlayer, commandText, false));
                    break;
                case "title":
                    String[] titleParts = commandText.split(";");
                    if (titleParts.length == 5) {
                        String titleText = langFileManager.applyColorsAndPlaceholders(cookiesPlayer, titleParts[0], false);
                        String subtitleText = langFileManager.applyColorsAndPlaceholders(cookiesPlayer, titleParts[1], false);

                        int fadeIn = Integer.parseInt(titleParts[2]);
                        int stay = Integer.parseInt(titleParts[3]);
                        int fadeOut = Integer.parseInt(titleParts[4]);

                        new TitleManager(cookiesPlayer, titleText, subtitleText, fadeIn, stay, fadeOut, plugin).send();
                    } else {
                        Console(ConsoleType.ERROR, "Invalid title command format!", LineType.SIDE_LINES);
                    }
                    break;
            }
        }
    }

    /**
     * Returns the last color used in a string based on Minecraft's color formatting.
     * This method checks for both standard color codes (like §f) and HEX color codes (like #FFFFFF),
     * returning the appropriate {@link ChatColor}.
     *
     * @param string The string from which to extract the last color.
     * @return The {@link ChatColor} corresponding to the last color in the string,
     * or {@link ChatColor#WHITE} if no color is found.
     */
    public ChatColor getLastColor(String string) {
        ChatColor color;
        String lastColors = org.bukkit.ChatColor.getLastColors(string).replace("§x", "#").replace("§", "");
        if (lastColors.matches("^#[0-9a-fA-F]{6}$")) {
            color = ChatColor.of(lastColors);
        } else if (lastColors.length() == 2 && lastColors.startsWith("§")) {
            color = ChatColor.getByChar(org.bukkit.ChatColor.getLastColors(string).charAt(1));
        } else {
            color = ChatColor.WHITE;
        }
        return color;
    }
}

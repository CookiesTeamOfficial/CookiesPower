package arkadarktime.interfaces;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public interface BukkitConsole {
    default void Console(Object text) {
        this.Console(ConsoleType.INFO, text, LineType.SIDE_LINES);
    }

    default void Console(ConsoleType consoleType, Object text) {
        this.Console(consoleType, text, LineType.SIDE_LINES);
    }

    default void Console(Object text, LineType lineType) {
        this.Console(ConsoleType.INFO, text, lineType);
    }

    default void Console(ConsoleType consoleType, Object text, LineType lineType) {
        this.Console(consoleType, String.valueOf(text), lineType);
    }

    default void Console(ConsoleType consoleType, String text, LineType lineType) {
        String coloredText = ChatColor.translateAlternateColorCodes('&', text);
        String formattedText = consoleType.getFormated(coloredText);
        switch (lineType) {
            case LINE:
                Bukkit.getConsoleSender().sendMessage(formattedText);
                break;
            case TOP_SIDE_LINE:
                Bukkit.getConsoleSender().sendMessage(consoleType.getFormated(""));
                Bukkit.getConsoleSender().sendMessage(formattedText);
                break;
            case BOTTOM_SIDE_LINE:
                Bukkit.getConsoleSender().sendMessage(formattedText);
                Bukkit.getConsoleSender().sendMessage(consoleType.getFormated(""));
                break;
            case SIDE_LINES:
                Bukkit.getConsoleSender().sendMessage(consoleType.getFormated(""));
                Bukkit.getConsoleSender().sendMessage(formattedText);
                Bukkit.getConsoleSender().sendMessage(consoleType.getFormated(""));
                break;
        }
    }

    enum ConsoleType {
        INFO(ChatColor.GRAY.toString()), WARN(ChatColor.YELLOW.toString()), ERROR(ChatColor.RED.toString()), DEBUG(ChatColor.AQUA.toString());

        private final String colorCode;

        ConsoleType(String colorCode) {
            this.colorCode = colorCode;
        }

        public String getPrefix() {
            return ChatColor.GRAY + "[" + ChatColor.WHITE + "CookiesPower" + ChatColor.GRAY + "] ";
        }

        private String getColorCode() {
            return colorCode;
        }

        public String getFormated(String text) {
            return getPrefix() + ChatColor.GRAY + "[" + this.getColorCode() + this.name() + ChatColor.GRAY + "] " + ChatColor.RESET + text;
        }
    }

    enum LineType {
        LINE, TOP_SIDE_LINE, BOTTOM_SIDE_LINE, SIDE_LINES
    }
}

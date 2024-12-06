package arkadarktime.commands;

import arkadarktime.CookiesPower;
import arkadarktime.utils.CustomUtils;
import arkadarktime.utils.FileManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MainCommand implements CommandExecutor {
    private final CookiesPower plugin;
    private final CustomUtils customUtils;

    public MainCommand(CookiesPower plugin) {
        this.plugin = plugin;
        this.customUtils = new CustomUtils(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());
        String reloadCommandPermission = "cookiespower.commands.reload";
        String helpCommandPermission = "cookiespower.commands.help";

        if (command.getName().equalsIgnoreCase("cookiespower")) {
            if (args.length > 0) {
                String subCommand = args[0].toLowerCase();
                switch (subCommand) {
                    case "reload":
                        if (sender.hasPermission(reloadCommandPermission)) {
                            plugin.reloadConfig();
                            plugin.saveConfig();

                            boolean loadLangFileComplete = plugin.loadLangFile();
                            if (!loadLangFileComplete) {
                                return true;
                            }

                            boolean loadTablistFileComplete = plugin.loadTablistFile();
                            if (!loadTablistFileComplete) {
                                return true;
                            }

                            boolean loadChatFileComplete = plugin.loadChatFile();
                            if (!loadChatFileComplete) {
                                return true;
                            }

                            boolean loadServerFileComplete = plugin.loadServerFile();
                            if (!loadServerFileComplete) {
                                return true;
                            }

                            boolean loadAnimationsFileComplete = plugin.loadAnimationsFile();
                            if (!loadAnimationsFileComplete) {
                                return true;
                            }

                            plugin.tablistModule.restart();
                            plugin.serverBrandModule.restart();
                            plugin.serverMotdModule.restart();

                            try {
                                langFileManager.reload();
                            } catch ( InvalidConfigurationException e ) {
                                return true;
                            }

                            sender.sendMessage(langFileManager.getColoredString(sender, "commands.reload.success"));
                        } else {
                            customUtils.sendNoPermissionError(sender, reloadCommandPermission);
                        }
                        break;
                    case "//@de#b#ug@\\":
                        sender.sendMessage(langFileManager.getColoredString(sender, Arrays.toString(args)));
                    default:
                        if (sender.hasPermission(helpCommandPermission)) {
                            sender.sendMessage(langFileManager.getColoredString(sender, "commands.help").replace("%cmd%", label));
                        } else {
                            customUtils.sendNoPermissionError(sender, helpCommandPermission);
                        }
                        break;
                }
            } else {
                if (sender.hasPermission(helpCommandPermission)) {
                    sender.sendMessage(langFileManager.getColoredString(sender, "commands.help").replace("%cmd%", label));
                } else {
                    customUtils.sendNoPermissionError(sender, helpCommandPermission);
                }
            }
        }
        return true;
    }
}

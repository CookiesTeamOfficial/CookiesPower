package arkadarktime.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TabCompletor implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command cmd, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!cmd.getName().equalsIgnoreCase("cookiespower")) {
            return completions;
        }

        if (args.length == 1) {
            handleFirstArgument(completions);
        } else if (args.length == 2) {
            handleSecondArgument(args[0], completions);
        }

        return completions;
    }

    private void handleFirstArgument(List<String> completions) {
        completions.add("reload");
        completions.add("help");
    }

    private void handleSecondArgument(String command, List<String> completions) {
        if (command.equalsIgnoreCase("reload")) {
            completions.add("second");
        }
    }
}

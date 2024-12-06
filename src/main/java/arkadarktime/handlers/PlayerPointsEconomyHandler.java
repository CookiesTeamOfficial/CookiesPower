package arkadarktime.handlers;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.EconomyHandler;
import org.bukkit.entity.Player;

public class PlayerPointsEconomyHandler implements EconomyHandler {
    private final CookiesPower plugin;

    public PlayerPointsEconomyHandler(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean depositMoney(Player player, double amount) {
        return plugin.playerPointsAPI.give(player.getUniqueId(), (int) amount);
    }

    @Override
    public boolean withdrawMoney(Player player, double amount) {
        return plugin.playerPointsAPI.take(player.getUniqueId(), (int) amount);
    }

    @Override
    public double getBalance(Player player) {
        return plugin.playerPointsAPI.look(player.getUniqueId());
    }
}

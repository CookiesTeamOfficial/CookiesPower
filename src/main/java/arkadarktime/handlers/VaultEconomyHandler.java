package arkadarktime.handlers;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.EconomyHandler;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class VaultEconomyHandler implements EconomyHandler {
    private final CookiesPower plugin;

    public VaultEconomyHandler(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean depositMoney(Player player, double amount) {
        EconomyResponse response = plugin.vaultAPI.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    @Override
    public boolean withdrawMoney(Player player, double amount) {
        EconomyResponse response = plugin.vaultAPI.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    @Override
    public double getBalance(Player player) {
        return plugin.vaultAPI.getBalance(player);
    }
}

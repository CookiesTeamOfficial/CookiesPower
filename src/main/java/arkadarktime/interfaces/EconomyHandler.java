package arkadarktime.interfaces;

import org.bukkit.entity.Player;

public interface EconomyHandler {
    boolean depositMoney(Player player, double amount);

    boolean withdrawMoney(Player player, double amount);

    double getBalance(Player player);
}

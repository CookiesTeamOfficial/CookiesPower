package arkadarktime.utils.database;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDatabaseManager extends AbstractDatabaseManager implements Listener {
    public PlayerDatabaseManager(CookiesPower plugin) {
        super(plugin, "players", "players");
        createTable("id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, name TEXT, displayname TEXT, ip TEXT, vanished BOOLEAN DEFAULT FALSE");
        addPlayersOnline();
    }

    public CookiesPlayer getCookiesPlayer(UUID uuid) {
        try {
            ResultSet rs = get("*", "uuid = ?", uuid.toString());
            if (rs != null && rs.next()) {
                return parseCookiesPlayer(rs);
            }
        } catch ( SQLException e ) {
            handleException("Failed to retrieve player", e);
        }
        return null;
    }

    private CookiesPlayer parseCookiesPlayer(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String name = rs.getString("name");
        String displayname = rs.getString("displayname");
        String ip = rs.getString("ip");
        boolean vanished = rs.getBoolean("vanished");

        rs.close();

        return new CookiesPlayer(id, uuid, name, displayname, ip, vanished);
    }

    public boolean checkPlayerExists(UUID uuid) {
        return getCookiesPlayer(uuid) != null;
    }

    public boolean addPlayer(Player player) {
        if (!checkPlayerExists(player.getUniqueId())) {
            return insert(new String[]{"uuid", "name", "displayname", "ip", "vanished"}, player.getUniqueId().toString(), player.getName(), player.getDisplayName(), player.getAddress().toString(), false);
        }
        return true;
    }

    public boolean removePlayer(UUID uuid) {
        return delete("uuid = ?", uuid.toString());
    }

    public boolean updateVanished(UUID uuid, boolean vanished) {
        return update(new String[]{"vanished"}, "uuid = ?", vanished, uuid.toString());
    }

    public void addPlayersOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean playerAdded = addPlayer(player);
            if (!playerAdded) {
                player.kickPlayer("Error when add you to database!");
                Console(ConsoleType.ERROR, "Error when add player to database!", LineType.SIDE_LINES);
            }
        }
    }

    @EventHandler
    public void addPlayerToDatabaseIfNotExists(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean playerAdded = addPlayer(player);
        if (!playerAdded) {
            player.kickPlayer("Error when add you to database!");
            Console(ConsoleType.ERROR, "Error when add player to database!", LineType.SIDE_LINES);
        }
    }
}

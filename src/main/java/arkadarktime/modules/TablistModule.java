package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.enums.TimeUnit;
import arkadarktime.interfaces.BukkitConsole;
import arkadarktime.interfaces.ModuleTicker;
import arkadarktime.utils.CustomUtils;
import arkadarktime.utils.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TablistModule implements ModuleTicker, BukkitConsole {
    private final CookiesPower plugin;
    private final CustomUtils customUtils;
    private BukkitTask updateTablistTask;

    public TablistModule(CookiesPower plugin) {
        this.plugin = plugin;
        this.customUtils = new CustomUtils(plugin);
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.tablist.enable")) return;

        ModuleTicker.super.enable();
        registerListener(this, plugin);
        this.start();
    }

    @Override
    public void disable() {
        ModuleTicker.super.disable();
        this.stop();
    }

    @Override
    public void start() {
        FileManager tablistFileManager = new FileManager(plugin, plugin.getTablistFile());
        long refreshTimeTicks = customUtils.parseTime(tablistFileManager.getString("tablist.refresh-interval"), TimeUnit.TICKS);
        this.updateTablistTask = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 0L, refreshTimeTicks);
    }

    @Override
    public void stop() {
        if (updateTablistTask != null) {
            updateTablistTask.cancel();
            updateTablistTask = null;
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
            setTablistHeaderFooter(cookiesPlayer, new ArrayList<>(), new ArrayList<>());
        });
    }

    @Override
    public void update() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
            updateTablistForPlayer(cookiesPlayer);
        });
    }

    private void updateTablistForPlayer(CookiesPlayer cookiesPlayer) {
        FileManager tablistFileManager = new FileManager(plugin, plugin.getTablistFile());

        boolean globalTablistEnabled = tablistFileManager.getBoolean("tablist.global.enable");
        if (globalTablistEnabled) {
            List<String> header = tablistFileManager.getColoredStringList(cookiesPlayer, "tablist.global.header");
            List<String> footer = tablistFileManager.getColoredStringList(cookiesPlayer, "tablist.global.footer");
            updateTablistForPlayer(cookiesPlayer, header, footer);
        }

        updatePerWorldTablists();
    }

    private void updateTablistForPlayer(CookiesPlayer cookiesPlayer, List<String> header, List<String> footer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                setTablistHeaderFooter(cookiesPlayer, header, footer);
            }
        }.runTask(plugin);
    }

    private void updatePerWorldTablists() {
        FileManager tablistFileManager = new FileManager(plugin, plugin.getTablistFile());
        ConfigurationSection worldSection = tablistFileManager.getConfigurationSection("tablist.per-world");
        if (worldSection != null) {
            worldSection.getKeys(false).forEach(worldName -> {
                boolean worldTablistEnabled = tablistFileManager.getBoolean("tablist.per-world." + worldName + ".enable");
                if (worldTablistEnabled) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        world.getPlayers().forEach(player -> {
                            CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
                            List<String> header = tablistFileManager.getColoredStringList(cookiesPlayer, "tablist.per-world." + worldName + ".header");
                            List<String> footer = tablistFileManager.getColoredStringList(cookiesPlayer, "tablist.per-world." + worldName + ".footer");
                            updateTablistForPlayer(cookiesPlayer, header, footer);
                        });
                    }
                }
            });
        }
    }

    private String replacePlaceholders(CookiesPlayer cookiesPlayer, String text) {
        return text.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size())).replace("%max-online%", String.valueOf(Bukkit.getServer().getMaxPlayers())).replace("%ping%", String.valueOf(cookiesPlayer.getPing()));
    }

    public void setTablistHeaderFooter(CookiesPlayer cookiesPlayer, List<String> headerStrings, List<String> footerStrings) {
        String header = replacePlaceholders(cookiesPlayer, String.join("\n", headerStrings));
        String footer = replacePlaceholders(cookiesPlayer, String.join("\n", footerStrings));

        cookiesPlayer.getPlayer().setPlayerListHeaderFooter(header, footer);
    }
}

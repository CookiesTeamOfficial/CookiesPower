package arkadarktime;

import arkadarktime.commands.MainCommand;
import arkadarktime.commands.TabCompletor;
import arkadarktime.interfaces.BukkitConsole;
import arkadarktime.interfaces.HookPlugin;
import arkadarktime.interfaces.modules.ModuleListener;
import arkadarktime.modules.*;
import arkadarktime.utils.AnimationsManager;
import arkadarktime.utils.InventoryManager;
import arkadarktime.utils.Metrics;
import arkadarktime.utils.database.PlayerDatabaseManager;
import arkadarktime.utils.pluginHooks.*;
import com.comphenix.protocol.ProtocolManager;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class CookiesPower extends JavaPlugin implements BukkitConsole {
    // Other
    public final String pluginIdentifierId = "CookiesPowerPluginByArkaDarkTime-" + UUID.randomUUID();
    public String langCode;
    // Files
    public FileConfiguration langFileManager;
    public FileConfiguration tablistFileManager;
    public FileConfiguration chatFileManager;
    public FileConfiguration serverFileManager;
    public FileConfiguration animationsFileManager;
    // Managers
    private PlayerDatabaseManager playerDatabaseManager;
    private AnimationsManager animationsManager;
    // Plugin hooks
    public boolean placeholderApiHooked = false; // Is placeholder api hooked
    public ProtocolManager protocolManager; // Manage protocols
    public LuckPerms luckPermsAPI; // Manage permission
    public Economy vaultAPI; // Vault economy
    public PlayerPointsAPI playerPointsAPI; // PlayerPoints economy
    // Modules
    public final TablistModule tablistModule = new TablistModule(this);
    public final DeathModule deathModule = new DeathModule(this);
    public final AdvancementModule advancementModule = new AdvancementModule(this);
    public final ChatModule chatModule = new ChatModule(this);
    public final ServerMotdModule serverMotdModule = new ServerMotdModule(this);
    public final ServerBrandModule serverBrandModule = new ServerBrandModule(this);
    private final List<ModuleListener> allModules = Arrays.asList(tablistModule, deathModule, advancementModule, chatModule, serverMotdModule, serverBrandModule);

    // Plugin enable method
    @Override
    public void onEnable() {
        try {
            loadFiles();

            hookPlugins();

            playerDatabaseManager = new PlayerDatabaseManager(this);
            animationsManager = new AnimationsManager(this);
            animationsManager.startAnimations();

            enableModules();

            registerCommands();
            registerListeners();

            enableBStats();

            Console(BukkitConsole.ConsoleType.INFO, "Successful load and enable", BukkitConsole.LineType.TOP_SIDE_LINE);
            Console(BukkitConsole.ConsoleType.INFO, "Version: " + this.getDescription().getVersion(), BukkitConsole.LineType.BOTTOM_SIDE_LINE);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    // Plugin disable method
    @Override
    public void onDisable() {
        disableModules();

        if (protocolManager != null) protocolManager.removePacketListeners(this);
        playerDatabaseManager.disconnect();
        animationsManager.stopAnimations();

        Console(BukkitConsole.ConsoleType.INFO, "Successful unload and disable", BukkitConsole.LineType.TOP_SIDE_LINE);
        Console(BukkitConsole.ConsoleType.INFO, "Version: " + this.getDescription().getVersion(), BukkitConsole.LineType.BOTTOM_SIDE_LINE);
    }

    // Register listeners function
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new InventoryManager(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDatabaseManager(this), this);
    }

    // Register commands function
    private void registerCommands() {
        getCommand("cookiespower").setExecutor(new MainCommand(this));
        getCommand("cookiespower").setTabCompleter(new TabCompletor());
    }

    // Hook plugins function
    private void hookPlugins() {
        HookPlugin[] plugins = {new ProtocolLibHook(this), new PlaceholderAPIHook(this), new LuckPermsHook(this), new VaultHook(this), new PlayerPointsHook(this)};
        for (HookPlugin plugin : plugins) {
            plugin.hook();
        }
    }

    // Enable modules function
    private void enableModules() {
        Console(BukkitConsole.ConsoleType.INFO, "", BukkitConsole.LineType.LINE);
        allModules.forEach(ModuleListener::enable);
        Console(BukkitConsole.ConsoleType.INFO, "", BukkitConsole.LineType.LINE);
    }

    // Disable modules function
    private void disableModules() {
        Console(BukkitConsole.ConsoleType.INFO, "", BukkitConsole.LineType.LINE);
        allModules.forEach(ModuleListener::disable);
        Console(BukkitConsole.ConsoleType.INFO, "", BukkitConsole.LineType.LINE);
    }

    // Enable bstats function
    public void enableBStats() {
        if (getConfig().getBoolean("enable-bstats")) {
            Metrics bStats = new Metrics(this, 24098);
            bStats.addCustomChart(new Metrics.SimplePie("plugin_language", () -> getConfig().getString("lang")));
        }
    }

    // Get player database manager function
    public PlayerDatabaseManager getPlayerDatabaseManager() {
        return this.playerDatabaseManager;
    }

    // Get animations manager function
    public AnimationsManager getAnimationsManager() {
        return animationsManager;
    }

    // Load all files function
    private void loadFiles() {
        loadConfigFile();
        loadLangFile();
        loadTablistFile();
        loadChatFile();
        loadServerFile();
        loadAnimationsFile();
        loadIcons();
        // Load readme.txt
        loadFile("README.txt", "README.txt", null);
    }

    // Load a config file
    public void loadConfigFile() {
        loadFile("config.yml", "config.yml", null);
    }

    // Load a lang file
    public boolean loadLangFile() {
        langCode = getConfig().getString("lang", "lang/en");
        String langFile = "lang/" + langCode + ".yml";
        langFileManager = loadFile(langFile, langFile, "lang");
        return langFileManager != null;
    }

    // Load a tablist file
    public boolean loadTablistFile() {
        tablistFileManager = loadFileLang("tablist.yml");
        return tablistFileManager != null;
    }

    // Load a chat file
    public boolean loadChatFile() {
        chatFileManager = loadFileLang("chat.yml");
        return chatFileManager != null;
    }

    // Load a server file
    public boolean loadServerFile() {
        serverFileManager = loadFileLang("server.yml");
        return serverFileManager != null;
    }

    // Load an animation's file
    public boolean loadAnimationsFile() {
        animationsFileManager = loadFileLang("animations.yml");
        return animationsFileManager != null;
    }

    // Method to load the language file, considering the selected language code
    private FileConfiguration loadFileLang(String fileName) {
        return this.loadFile(fileName, "lang/" + langCode + "/" + fileName, null);
    }

    // Loads a file from plugin resources into a specified target folder, creating the folder if necessary
    private FileConfiguration loadFile(String fileName, String pathInPlugin, String inFolder) {
        try {
            if (inFolder != null) {
                File targetDirectory = new File(getDataFolder(), inFolder);
                if (!targetDirectory.exists()) {
                    targetDirectory.mkdirs();
                }
            }

            File file = new File(getDataFolder(), fileName);
            if (!file.exists()) {
                copyDefaultFile(pathInPlugin, file);
            }
            return YamlConfiguration.loadConfiguration(file);
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }

    // Load all icons into the icons folder
    private void loadIcons() {
        try {
            String iconsFolder = "icons";
            File iconsPath = new File(getDataFolder(), iconsFolder);
            if (!iconsPath.exists()) {
                iconsPath.mkdirs();
            }

            String[] icons = {"server-icon-1.png", "server-icon-2.png", "server-icon-3.png", "server-icon-4.png", "server-icon-5.png"};

            for (String icon : icons) {
                File iconFile = new File(iconsPath, icon);
                if (!iconFile.exists()) {
                    copyDefaultFile(iconsFolder + icon, iconFile);
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            Console(BukkitConsole.ConsoleType.ERROR, "Failed to load icons: " + e.getMessage(), BukkitConsole.LineType.SIDE_LINES);
        }
    }

    // Get files functions
    public FileConfiguration getLangFile() {
        return langFileManager;
    }

    public FileConfiguration getTablistFile() {
        return tablistFileManager;
    }

    public FileConfiguration getChatFile() {
        return chatFileManager;
    }

    public FileConfiguration getServerFile() {
        return serverFileManager;
    }

    public FileConfiguration getAnimationsFile() {
        return animationsFileManager;
    }

    // Copy default file from resources function
    private void copyDefaultFile(String resourcePath, File destFile) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                Console(BukkitConsole.ConsoleType.WARN, "Resource not found: " + resourcePath, BukkitConsole.LineType.SIDE_LINES);
                return;
            }
            Files.copy(inputStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch ( IOException e ) {
            e.printStackTrace();
            Console(BukkitConsole.ConsoleType.ERROR, "Failed to copy " + destFile.getName() + " file: " + e.getMessage(), BukkitConsole.LineType.SIDE_LINES);
        }
    }
}

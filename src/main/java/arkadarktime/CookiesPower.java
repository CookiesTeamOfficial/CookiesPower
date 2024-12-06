package arkadarktime;

import arkadarktime.commands.MainCommand;
import arkadarktime.commands.TabCompletor;
import arkadarktime.interfaces.BukkitConsole;
import arkadarktime.interfaces.HookPlugin;
import arkadarktime.interfaces.ModuleListener;
import arkadarktime.modules.*;
import arkadarktime.utils.AnimationsManager;
import arkadarktime.utils.InventoryManager;
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
    public final String pluginIdentifierId = "CookiesPowerPluginByArkaDarkTime-" + UUID.randomUUID();
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
    public ProtocolManager protocolLibAPI; // Manage protocols
    public Economy vaultAPI; // Vault economy
    public PlayerPointsAPI playerPointsAPI; // PlayerPoints economy
    public LuckPerms luckPermsAPI; // Manage permission
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

        if (protocolLibAPI != null) protocolLibAPI.removePacketListeners(this);
        playerDatabaseManager.disconnect();
        animationsManager.stopAnimations();

        Console(BukkitConsole.ConsoleType.INFO, "Successful unload and disable", BukkitConsole.LineType.TOP_SIDE_LINE);
        Console(BukkitConsole.ConsoleType.INFO, "Version: " + this.getDescription().getVersion(), BukkitConsole.LineType.BOTTOM_SIDE_LINE);
    }

    // Register listeners function
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new InventoryManager(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDatabaseManager(this), this);
        getServer().getPluginManager().registerEvents(new AdvancementModule(this), this);
    }

    // Register commands function
    private void registerCommands() {
        getCommand("cookiespower").setExecutor(new MainCommand(this));
        getCommand("cookiespower").setTabCompleter(new TabCompletor());
    }

    // Hook plugins function
    private void hookPlugins() {
        HookPlugin[] plugins = {
                new ProtocolLibHook(this),
                new PlaceholderAPIHook(this),
                new LuckPermsHook(this),
                new VaultHook(this),
                new PlayerPointsHook(this)
        };
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

    // Get player database manager function
    public PlayerDatabaseManager getPlayerDatabaseManager() {
        return this.playerDatabaseManager;
    }

    // Get animations manager function
    public AnimationsManager getAnimationsManager() {
        return animationsManager;
    }

    private void loadFiles() {
        loadConfigFile();
        loadLangFile();
        loadTablistFile();
        loadChatFile();
        loadServerFile();
        loadAnimationsFile();
    }

    // Load a config file
    public void loadConfigFile() {
        loadFile("config.yml", null);
    }

    // Load a lang file
    public boolean loadLangFile() {
        String langCode = getConfig().getString("lang", "en");
        String langFile = "lang/" + langCode + ".yml";
        langFileManager = loadFile(langFile, null);
        return langFileManager != null;
    }

    // Load a tablist file
    public boolean loadTablistFile() {
        tablistFileManager = loadFile("tablist.yml", null);
        return tablistFileManager != null;
    }

    // Load a chat file
    public boolean loadChatFile() {
        chatFileManager = loadFile("chat.yml", null);
        return chatFileManager != null;
    }

    // Load a server file
    public boolean loadServerFile() {
        serverFileManager = loadFile("server.yml", null);
        return serverFileManager != null;
    }

    // Load an animation's file
    public boolean loadAnimationsFile() {
        animationsFileManager = loadFile("animations.yml", null);
        return animationsFileManager != null;
    }

    // Load file from plugin resources function
    private FileConfiguration loadFile(String fileName, String defaultPath) {
        try {
            File file = new File(getDataFolder(), fileName);
            if (!file.exists()) {
                if (defaultPath != null) {
                    copyDefaultFile(defaultPath, file);
                } else {
                    saveResource(fileName, false);
                }
            }
            return YamlConfiguration.loadConfiguration(file);
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
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
}

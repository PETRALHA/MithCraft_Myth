package com.mithcraft.myth;

import com.mithcraft.myth.commands.MythCommand;
import com.mithcraft.myth.commands.MythCommandTabCompleter;
import com.mithcraft.myth.events.PlayerDeathListener;
import com.mithcraft.myth.managers.ConfigManager;
import com.mithcraft.myth.managers.LanguageManager;
import com.mithcraft.myth.managers.MythManager;
import com.mithcraft.myth.utils.MessageUtils;
import com.mithcraft.myth.utils.PlaceholderAPIHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MithCraftMyth extends JavaPlugin {

    private static MithCraftMyth instance;
    private ConfigManager configManager;
    private MythManager mythManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize core utilities
        MessageUtils.initialize(this);

        // Setup managers
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        this.mythManager = new MythManager(this);

        // Register commands
        getCommand("myth").setExecutor(new MythCommand(this));
        getCommand("myth").setTabCompleter(new MythCommandTabCompleter());

        // Register events
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        // Register PlaceholderAPI if available
        if (PlaceholderAPIHook.registerPlaceholders(this)) {
            getLogger().info("PlaceholderAPI integration enabled!");
        }

        // Load data
        configManager.loadConfig();
        languageManager.loadLanguages();
        mythManager.loadMythData();

        getLogger().info(() -> String.format(
            "%s v%s enabled!",
            getDescription().getName(),
            getDescription().getVersion()
        ));
    }

    @Override
    public void onDisable() {
        // Save data
        mythManager.saveMythData();

        getLogger().info(() -> String.format(
            "%s v%s disabled!",
            getDescription().getName(),
            getDescription().getVersion()
        ));
    }

    public void reloadPlugin() {
        configManager.reloadConfig();
        languageManager.reloadLanguages();
        mythManager.reloadMythData();
        MessageUtils.reloadConfig();
    }

    // Getters for managers
    public static MithCraftMyth getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MythManager getMythManager() {
        return mythManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
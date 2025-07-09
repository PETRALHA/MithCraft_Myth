package com.mithcraft.myth.managers;

import com.mithcraft.myth.MithCraftMyth;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    private final MithCraftMyth plugin;
    private FileConfiguration config;
    private final File configFile;
    private final int latestConfigVersion = 1;

    public ConfigManager(MithCraftMyth plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void loadConfig() {
        // Create plugin folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Load or create config
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
            plugin.getLogger().info("Default config file created");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        checkConfigVersion();
        validateConfig();
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        validateConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    private void checkConfigVersion() {
        int currentVersion = config.getInt("config-version", 0);
        
        if (currentVersion < latestConfigVersion) {
            plugin.getLogger().warning("Outdated config file detected! (Current: " + currentVersion + " | Latest: " + latestConfigVersion + ")");
            backupCurrentConfig();
            updateConfig(currentVersion);
        }
    }

    private void backupCurrentConfig() {
        File backupFile = new File(plugin.getDataFolder(), "config_old_v" + config.getInt("config-version", 0) + ".yml");
        
        try {
            config.save(backupFile);
            plugin.getLogger().info("Backed up old config to " + backupFile.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not backup old config file", e);
        }
    }

    private void updateConfig(int oldVersion) {
        // Future-proof config updates
        if (oldVersion < 1) {
            // Example update for version 1
            config.set("config-version", latestConfigVersion);
            config.set("settings.debug", config.getBoolean("settings.debug", false));
            saveConfig();
            plugin.getLogger().info("Updated config to version " + latestConfigVersion);
        }
    }

    private void validateConfig() {
        // Validate core settings
        if (!config.contains("settings.lang")) {
            config.set("settings.lang", "pt-BR");
        }

        // Validate reward commands
        if (!config.contains("rewards.on_become.commands")) {
            config.set("rewards.on_become.commands", new String[] {
                "lp user %myth_previous% parent remove myth",
                "lp user %myth_current% parent add myth",
                "eco give %myth_current% 100"
            });
        }

        // Validate PvP worlds
        if (!config.contains("pvp.worlds")) {
            config.set("pvp.worlds", new String[] {
                "world",
                "world_nether",
                "world_the_end"
            });
        }

        saveConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getLanguage() {
        return config.getString("settings.lang", "pt-BR");
    }

    public boolean isDebugEnabled() {
        return config.getBoolean("settings.debug", false);
    }

    public boolean isPrefixEnabled() {
        return config.getBoolean("settings.messages.prefix-enabled", true);
    }

    public String getPrefix() {
        return config.getString("settings.messages.prefix", "&8[&bMYTH&8] &7");
    }
}
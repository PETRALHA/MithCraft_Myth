package com.mithcraft.myth.managers;

import com.mithcraft.myth.MithCraftMyth;
import com.mithcraft.myth.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {
    private final MithCraftMyth plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private String currentLanguage;
    private final File languageFolder;

    private static final String FALLBACK_LANGUAGE = "en-US";

    public LanguageManager(MithCraftMyth plugin) {
        this.plugin = plugin;
        this.languageFolder = new File(plugin.getDataFolder(), "lang");
    }

    public void loadLanguages() {
        languages.clear();

        if (!languageFolder.exists()) {
            languageFolder.mkdirs();
        }

        saveDefaultLanguage("pt-BR");
        saveDefaultLanguage(FALLBACK_LANGUAGE);

        currentLanguage = plugin.getConfigManager().getLanguage();
        File languageFile = new File(languageFolder, currentLanguage + ".yml");

        if (!languageFile.exists()) {
            plugin.getLogger().warning("Language file '" + currentLanguage + ".yml' not found! Falling back to " + FALLBACK_LANGUAGE);
            currentLanguage = FALLBACK_LANGUAGE;
        }

        loadLanguage(currentLanguage);

        if (!currentLanguage.equals(FALLBACK_LANGUAGE)) {
            loadLanguage(FALLBACK_LANGUAGE);
        }
    }

    private void saveDefaultLanguage(String language) {
        File file = new File(languageFolder, language + ".yml");
        if (!file.exists()) {
            plugin.saveResource("lang/" + language + ".yml", false);
            plugin.getLogger().info("Saved default language file: " + language + ".yml");
        }
    }

    private void loadLanguage(String language) {
        File file = new File(languageFolder, language + ".yml");
        if (!file.exists()) {
            plugin.getLogger().warning("Language file not found: " + file.getName());
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        try (InputStream stream = plugin.getResource("lang/" + language + ".yml")) {
            if (stream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
                config.setDefaults(defaultConfig);
                config.options().copyDefaults(true);
                config.save(file);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load default language file for " + language, e);
        }

        languages.put(language, config);
    }

    public String getMessage(String path, String... placeholders) {
        // Obter mensagem do idioma atual ou fallback
        String message = getRawMessage(path);
        
        if (message == null) {
            plugin.getLogger().warning("Missing language key: " + path);
            return "[" + path + "]";
        }
        
        // Aplicar placeholders
        if (placeholders.length > 0) {
            message = applyPlaceholders(message, placeholders);
        }
        
        return message;
    }

    private String getRawMessage(String path) {
        // Tentar obter do idioma atual
        Object rawMessage = languages.get(currentLanguage).get(path);
        if (rawMessage != null) return processMultiLine(rawMessage);

        // Tentar fallback se diferente
        if (!currentLanguage.equals(FALLBACK_LANGUAGE)) {
            rawMessage = languages.get(FALLBACK_LANGUAGE).get(path);
            if (rawMessage != null) return processMultiLine(rawMessage);
        }

        return null;
    }

    private String processMultiLine(Object rawMessage) {
        if (rawMessage instanceof List) {
            List<String> lines = new ArrayList<>();
            for (Object line : (List<?>) rawMessage) {
                lines.add(line.toString());
            }
            return String.join("\n", lines);
        }
        return rawMessage.toString();
    }

    private String applyPlaceholders(String message, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            plugin.getLogger().warning("Invalid placeholders (key-value pairs required) for path");
            return message;
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        return message;
    }

    public void reloadLanguages() {
        loadLanguages();
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
package com.mithcraft.myth.utils;

import com.mithcraft.myth.MithCraftMyth;
import com.mithcraft.myth.managers.MythManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final MithCraftMyth plugin;
    private final MythManager mythManager;

    public PlaceholderAPIHook(MithCraftMyth plugin) {
        this.plugin = plugin;
        this.mythManager = plugin.getMythManager();
    }

    // Método auxiliar para registro seguro - removido o @Override
    public boolean register() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return super.register();
        }
        return false;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mithcraftmyth";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        switch (params.toLowerCase()) {
            case "current":
                return mythManager.getCurrentMythName() != null ? mythManager.getCurrentMythName() : "Nenhum";
            case "previous":
                if (mythManager.getPreviousMyth() != null) {
                    OfflinePlayer prev = Bukkit.getOfflinePlayer(mythManager.getPreviousMyth());
                    return prev.getName() != null ? prev.getName() : "Nenhum";
                }
                return "Nenhum";
            case "date":
                return mythManager.getMythDate();
            case "hours":
                return mythManager.getMythHours();
            case "time":
                return mythManager.getMythDurationFormatted();
            case "is_myth":
                return player != null && player.getUniqueId().equals(mythManager.getCurrentMyth()) ? "Sim" : "Não";
        }
        return null;
    }

    // Método estático simplificado para registro
    public static boolean registerPlaceholders(MithCraftMyth plugin) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return new PlaceholderAPIHook(plugin).register();
        }
        return false;
    }
}
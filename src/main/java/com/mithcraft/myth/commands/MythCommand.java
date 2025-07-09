package com.mithcraft.myth.commands;

import com.mithcraft.myth.MithCraftMyth;
import com.mithcraft.myth.managers.MythManager;
import com.mithcraft.myth.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MythCommand implements CommandExecutor {
    private final MithCraftMyth plugin;
    private final MythManager mythManager;

    public MythCommand(MithCraftMyth plugin) {
        this.plugin = plugin;
        this.mythManager = plugin.getMythManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return showCurrentMyth(sender);
        }

        switch (args[0].toLowerCase()) {
            case "set":
                return handleSetCommand(sender, args);
            case "setrandom":
                return handleSetRandomCommand(sender);
            case "unset":
                return handleUnsetCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "version":
                return handleVersionCommand(sender);
            case "help":
            default:
                return showHelp(sender);
        }
    }

    private boolean showCurrentMyth(CommandSender sender) {
        String currentMyth = mythManager.getCurrentMythName();
        if (currentMyth != null) {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.myth.current",
                    "myth_current", currentMyth,
                    "myth_date", mythManager.getMythDate(),
                    "myth_hours", mythManager.getMythHours(),
                    "myth_time", mythManager.getMythDurationFormatted()));
        } else {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.myth.none"));
        }
        return true;
    }

    private boolean handleSetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mithcraftmyth.set")) {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 2) {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.invalid-usage"));
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.player-offline"));
            return true;
        }

        mythManager.setMyth(target);
        return true;
    }

    private boolean handleSetRandomCommand(CommandSender sender) {
        if (!sender.hasPermission("mithcraftmyth.set")) {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        mythManager.setRandomMyth();
        return true;
    }

    private boolean handleUnsetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mithcraftmyth.set")) {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        if (args.length < 2) {
            // Auto-unset if no player specified and sender is a player
            if (sender instanceof Player) {
                mythManager.unsetMyth((Player) sender);
                return true;
            }
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.invalid-usage"));
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.player-offline"));
            return true;
        }

        mythManager.unsetMyth(target);
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("mithcraftmyth.reload")) {
            MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.no-permission"));
            return true;
        }

        plugin.reloadPlugin();
        MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.reload.success"));
        return true;
    }

    private boolean handleVersionCommand(CommandSender sender) {
        MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.version.message",
                "version", plugin.getDescription().getVersion()));
        return true;
    }

    private boolean showHelp(CommandSender sender) {
        String[] helpLines = plugin.getLanguageManager().getMessage("commands.help.lines").split("\n");
        MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.help.header"));
        
        for (String line : helpLines) {
            MessageUtils.send(sender, line);
        }
        
        MessageUtils.send(sender, plugin.getLanguageManager().getMessage("commands.help.footer"));
        return true;
    }
}
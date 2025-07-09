package com.mithcraft.myth.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MythCommandTabCompleter implements TabCompleter {
    
    private static final List<String> SUBCOMMANDS = List.of(
        "set", "setrandom", "unset", "reload", "version", "help"
    );

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Completar subcomandos
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
        } 
        else if (args.length == 2) {
            // Completar nomes de jogadores para comandos específicos
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("unset")) {
                if (sender.hasPermission("mithcraftmyth.set")) {
                    addOnlinePlayers(completions, args[1]);
                }
            }
        }

        Collections.sort(completions);
        return completions;
    }

    private void addOnlinePlayers(List<String> completions, String partialName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (StringUtil.startsWithIgnoreCase(player.getName(), partialName)) {
                completions.add(player.getName());
            }
        }
    }
}
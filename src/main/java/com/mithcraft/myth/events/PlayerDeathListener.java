package com.mithcraft.myth.events;

import com.mithcraft.myth.MithCraftMyth;
import com.mithcraft.myth.managers.MythManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    private final MithCraftMyth plugin;
    private final MythManager mythManager;

    public PlayerDeathListener(MithCraftMyth plugin) {
        this.plugin = plugin;
        this.mythManager = plugin.getMythManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Verificar se é uma morte PvP e se o killer é um jogador
        if (killer == null || !(killer instanceof Player)) {
            return;
        }

        // Verificar se a vítima era o Myth atual
        if (!victim.getUniqueId().equals(mythManager.getCurrentMyth())) {
            return;
        }

        // Verificar se o mundo está na lista permitida
        if (!isInAllowedWorld(victim)) {
            return;
        }

        // Verificar se o killer não é o próprio Myth (anti-farm)
        if (killer.getUniqueId().equals(mythManager.getCurrentMyth())) {
            return;
        }

        // Tudo verificado - trocar o Myth
        mythManager.setMyth(killer);

        // Cancelar a mensagem de morte padrão
        event.setDeathMessage(null);
    }

    private boolean isInAllowedWorld(Player player) {
        if (!plugin.getConfigManager().getConfig().getBoolean("pvp.check-world", true)) {
            return true;
        }

        String worldName = player.getWorld().getName();
        return plugin.getConfigManager().getConfig().getStringList("pvp.worlds").contains(worldName);
    }
}
package com.mithcraft.myth.managers;

import com.mithcraft.myth.MithCraftMyth;
import com.mithcraft.myth.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MythManager {
    private final MithCraftMyth plugin;
    private UUID currentMyth;
    private UUID previousMyth;
    private long lastChangeTime;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public MythManager(MithCraftMyth plugin) {
        this.plugin = plugin;
    }

    public void loadMythData() {
        String mythUuid = plugin.getConfig().getString("myth.current");
        if (mythUuid != null) {
            currentMyth = UUID.fromString(mythUuid);
        }
        
        String prevMythUuid = plugin.getConfig().getString("myth.previous");
        if (prevMythUuid != null) {
            previousMyth = UUID.fromString(prevMythUuid);
        }
        
        lastChangeTime = plugin.getConfig().getLong("myth.last-change", System.currentTimeMillis());
    }

    public void saveMythData() {
        plugin.getConfig().set("myth.current", currentMyth != null ? currentMyth.toString() : null);
        plugin.getConfig().set("myth.previous", previousMyth != null ? previousMyth.toString() : null);
        plugin.getConfig().set("myth.last-change", lastChangeTime);
        plugin.saveConfig();
    }

    public void reloadMythData() {
        loadMythData();
    }

    public void setMyth(Player player) {
        if (player.getUniqueId().equals(currentMyth)) {
            MessageUtils.send(player, plugin.getLanguageManager().getMessage("commands.set.already-myth"));
            return;
        }

        previousMyth = currentMyth;
        currentMyth = player.getUniqueId();
        lastChangeTime = System.currentTimeMillis();

        saveMythData();
        processRewards(player);

        if (previousMyth == null) {
            broadcastFirstMyth(player);
        } else {
            broadcastNewMyth(player);
        }
    }

    public void unsetMyth(Player player) {
        if (currentMyth == null || !currentMyth.equals(player.getUniqueId())) {
            MessageUtils.send(player, plugin.getLanguageManager().getMessage("commands.unset.not-myth"));
            return;
        }

        previousMyth = currentMyth;
        currentMyth = null;
        lastChangeTime = System.currentTimeMillis();
        saveMythData();

        MessageUtils.send(player, plugin.getLanguageManager().getMessage("commands.unset.success"));
    }

    public void setRandomMyth() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            plugin.getLogger().warning("No online players available to set as random Myth!");
            return;
        }

        Player randomPlayer = Bukkit.getOnlinePlayers()
                .stream()
                .skip((int) (Bukkit.getOnlinePlayers().size() * Math.random()))
                .findFirst()
                .orElse(null);

        if (randomPlayer != null) {
            setMyth(randomPlayer);
        }
    }

    // Métodos para placeholders
    public String getMythDate() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastChangeTime), ZoneId.systemDefault())
                .format(DATE_FORMAT);
    }

    public String getMythHours() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastChangeTime), ZoneId.systemDefault())
                .format(TIME_FORMAT);
    }

    public String getMythDurationFormatted() {
        if (lastChangeTime == 0) return "N/A";
        
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - lastChangeTime);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        
        if (days > 0) {
            return String.format("%dd %02d:%02d", days, hours, minutes);
        }
        return String.format("%02d:%02d", hours, minutes);
    }

    public String getFormattedTime(String format) {
        if (lastChangeTime == 0) return "N/A";
        
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - lastChangeTime);
        return format.replace("d", String.valueOf(duration.toDays()))
                    .replace("H", String.valueOf(duration.toHours() % 24))
                    .replace("m", String.valueOf(duration.toMinutes() % 60));
    }

    // Getters
    public String getCurrentMythName() {
        return currentMyth != null ? Bukkit.getOfflinePlayer(currentMyth).getName() : null;
    }

    public UUID getCurrentMyth() {
        return currentMyth;
    }

    public UUID getPreviousMyth() {
        return previousMyth;
    }

    public long getLastChangeTime() {
        return lastChangeTime;
    }

    // Métodos privados
    private void processRewards(Player newMyth) {
        if (plugin.getConfig().getBoolean("rewards.on_become.active")) {
            plugin.getConfig().getStringList("rewards.on_become.commands")
                .forEach(cmd -> executeRewardCommand(cmd, newMyth));
        }
    }

    private void executeRewardCommand(String command, Player newMyth) {
        String processedCmd = command
            .replace("%myth_current%", newMyth.getName())
            .replace("%myth_previous%", previousMyth != null ? 
                Bukkit.getOfflinePlayer(previousMyth).getName() : "");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
    }

    private void broadcastNewMyth(Player newMyth) {
        String message = plugin.getLanguageManager().getMessage("broadcasts.new-myth",
            "player", newMyth.getName(),
            "myth_previous", Bukkit.getOfflinePlayer(previousMyth).getName(),
            "myth_time", getMythDurationFormatted());
        Bukkit.broadcastMessage(MessageUtils.parseAdvancedFormatting(message).toString());
    }

    private void broadcastFirstMyth(Player newMyth) {
        String message = plugin.getLanguageManager().getMessage("broadcasts.first-myth",
            "player", newMyth.getName());
        Bukkit.broadcastMessage(MessageUtils.parseAdvancedFormatting(message).toString());
    }

    private boolean isInAllowedWorld(Player player) {
        if (!plugin.getConfig().getBoolean("pvp.check-world", true)) {
            return true;
        }
        return plugin.getConfig().getStringList("pvp.worlds").contains(player.getWorld().getName());
    }
}
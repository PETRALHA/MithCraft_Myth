package com.mithcraft.myth.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static JavaPlugin plugin;
    private static MiniMessage miniMessage;
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");
    private static String prefix;
    private static boolean prefixEnabled;

    public static void initialize(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        miniMessage = MiniMessage.miniMessage();
        reloadConfig();
    }

    public static void reloadConfig() {
        FileConfiguration config = plugin.getConfig();
        prefix = config.getString("settings.messages.prefix", "&8[&bMYTH&8] &7");
        prefixEnabled = config.getBoolean("settings.messages.prefix-enabled", true);
    }

    public static void send(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;

        // Handle [noprefix] tag
        boolean noPrefix = message.contains("[noprefix]");
        message = message.replace("[noprefix]", "");

        // Process message
        Component component = parseAdvancedFormatting(message);

        // Add prefix if enabled and not explicitly disabled
        if (prefixEnabled && !noPrefix) {
            Component prefixComponent = parseAdvancedFormatting(prefix);
            component = prefixComponent.append(component);
        }

        // Send the final message
        sender.sendMessage(component);
    }

    public static Component parseAdvancedFormatting(String text) {
        // Convert legacy (&) colors first
        text = LEGACY_SERIALIZER.serialize(LEGACY_SERIALIZER.deserialize(text));

        // Process gradients
        text = processGradients(text);

        // Process hex colors
        text = processHexColors(text);

        // Parse with MiniMessage for tags like <hover>, <click>
        return miniMessage.deserialize(text);
    }

    private static String processGradients(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String startColor = matcher.group(1);
            String endColor = matcher.group(2);
            String content = matcher.group(3);

            StringBuilder gradientText = new StringBuilder();
            int length = content.length();
            
            if (length > 0) {
                float step = 1.0f / (length - 1);
                
                for (int i = 0; i < length; i++) {
                    float ratio = i * step;
                    String interpolatedColor = interpolateColor(startColor, endColor, ratio);
                    gradientText.append("<#").append(interpolatedColor).append(">")
                               .append(content.charAt(i));
                }
            }

            matcher.appendReplacement(sb, gradientText.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String processHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(sb, "<color:#" + hexColor + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String interpolateColor(String startHex, String endHex, float ratio) {
        int startR = Integer.parseInt(startHex.substring(0, 2), 16);
        int startG = Integer.parseInt(startHex.substring(2, 4), 16);
        int startB = Integer.parseInt(startHex.substring(4, 6), 16);

        int endR = Integer.parseInt(endHex.substring(0, 2), 16);
        int endG = Integer.parseInt(endHex.substring(2, 4), 16);
        int endB = Integer.parseInt(endHex.substring(4, 6), 16);

        int r = (int) (startR + ratio * (endR - startR));
        int g = (int) (startG + ratio * (endG - startG));
        int b = (int) (startB + ratio * (endB - startB));

        return String.format("%02x%02x%02x", r, g, b);
    }

    public static String applyPlaceholders(String text, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            plugin.getLogger().warning("Invalid number of placeholders provided (must be even: key, value pairs)");
            return text;
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            String key = placeholders[i];
            String value = placeholders[i + 1];
            text = text.replace("%" + key + "%", value);
        }
        return text;
    }
}
package com.llewkcor.ares.core.chat;

import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.chat.data.ChatMessageType;
import com.llewkcor.ares.core.chat.listener.ChatListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class ChatManager {
    @Getter public Ares plugin;
    @Getter public final ChatHandler handler;

    public ChatManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new ChatHandler(this);

        Bukkit.getPluginManager().registerEvents(new ChatListener(plugin, this), plugin);
    }

    /**
     * Returns a List of Bukkit Players who are in range to receive the provided message type
     * @param location Location
     * @param type ChatMessageType
     * @return List of Bukkit Players
     */
    public List<Player> getRecipientsInRange(Location location, ChatMessageType type) {
        final double range = plugin.getConfigManager().getGeneralConfig().getChatRanges().getOrDefault(type, 1000.0);
        return Bukkit.getOnlinePlayers().stream().filter(player -> (player.getLocation().getWorld().getName().equals(location.getWorld().getName()) && player.getLocation().distance(location) <= range) || player.hasPermission("arescore.bypasschatrange")).collect(Collectors.toList());
    }
}

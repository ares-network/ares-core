package com.playares.core.chat;

import com.google.common.collect.Sets;
import com.playares.core.Ares;
import com.playares.core.chat.data.ChatMessageType;
import com.playares.core.chat.data.ChatSession;
import com.playares.core.chat.listener.ChatListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ChatManager {
    @Getter public Ares plugin;
    @Getter public final ChatHandler handler;
    @Getter public final Set<ChatSession> chatSessions;

    public ChatManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new ChatHandler(this);
        this.chatSessions = Sets.newConcurrentHashSet();

        Bukkit.getPluginManager().registerEvents(new ChatListener(plugin, this), plugin);
    }

    /**
     * Returns a Chat Session matching the provided Player
     * @param player Player
     * @return ChatSession
     */
    public ChatSession getChatSession(Player player) {
        return chatSessions.stream().filter(session -> session.getPlayerId().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    /**
     * Returns a List of Bukkit Players who are in range to receive the provided message type
     * @param location Location
     * @param type ChatMessageType
     * @return List of Bukkit Players
     */
    public List<Player> getRecipientsInRange(Location location, ChatMessageType type) {
        final double range = plugin.getConfigManager().getGeneralConfig().getChatRanges().getOrDefault(type, 1000.0);
        return Bukkit.getOnlinePlayers().stream().filter(player -> (player.getLocation().getWorld().getName().equals(location.getWorld().getName()) && player.getLocation().distance(location) <= range)).collect(Collectors.toList());
    }
}

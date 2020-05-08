package com.playares.core.chat.listener;

import com.google.common.collect.Lists;
import com.playares.commons.event.ProcessedChatEvent;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.bukkit.Players;
import com.playares.commons.util.general.Time;
import com.playares.core.Ares;
import com.playares.core.chat.ChatManager;
import com.playares.core.chat.data.ChatMessageType;
import com.playares.core.chat.data.ChatSession;
import com.playares.core.loggers.entity.CombatLogger;
import com.playares.core.loggers.event.LoggerDeathEvent;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import com.playares.core.prison.data.PrisonPearl;
import com.playares.core.prison.event.PrisonPearlCreateEvent;
import com.playares.core.prison.event.PrisonPearlReleaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public final class ChatListener implements Listener {
    @Getter public final Ares plugin;
    @Getter public ChatManager manager;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        manager.getHandler().leaveSession(player);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onNetworkChat(ProcessedChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final String message = event.getMessage();
        final boolean admin = player.hasPermission("arescore.admin");
        final ChatSession session = manager.getChatSession(player);

        if (session == null) {
            return;
        }

        if (message.startsWith("!")) {
            final String trimmed = message.substring(1);
            event.setMessage(trimmed);
            return;
        }

        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(session.getNetworkId());

        if (network == null) {
            return;
        }

        final NetworkMember member = network.getMember(player);

        if (member == null && !admin) {
            manager.getHandler().leaveSession(player);
            player.sendMessage(ChatColor.RED + "You have been removed from this chat channel because you are no longer a member of " + network.getName());
            return;
        }

        if (member != null && !admin && !(member.hasPermission(NetworkPermission.ADMIN) || member.hasPermission(NetworkPermission.ACCESS_CHAT))) {
            manager.getHandler().leaveSession(player);
            player.sendMessage(ChatColor.RED + "You have been removed from this chat channel because you no longer have permission to access it");
            return;
        }

        event.setCancelled(true);
        network.sendRawMessage(ChatColor.GREEN + "[" + network.getName() + "] " + event.getDisplayName() + ": " + message);
        Logger.print("[" + network.getName() + "] " + player.getName() + ": " + message);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final Set<Player> players = event.getRecipients();

        if (manager.getPlugin().getConfigManager().getGeneralConfig().isRangedChatEnabled()) {
            final List<Player> inRange = manager.getRecipientsInRange(player.getLocation(), ChatMessageType.PLAYER_CHAT);
            final List<Player> toRemove = Lists.newArrayList();

            for (Player p : players) {
                if (inRange.contains(p)) {
                    continue;
                }

                toRemove.add(p);
            }

            event.getRecipients().removeAll(toRemove);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final String deathMessage = event.getDeathMessage();
        final List<Player> recipients = Lists.newArrayList();

        if (manager.getPlugin().getConfigManager().getGeneralConfig().isRangedChatEnabled()) {
            recipients.addAll(manager.getRecipientsInRange(player.getLocation(), ChatMessageType.DEATH_MESSAGE));
        } else {
            recipients.addAll(Bukkit.getOnlinePlayers());
        }

        event.setDeathMessage(null);

        recipients.forEach(p -> {
            p.sendMessage(deathMessage);
            Players.playSound(p, Sound.AMBIENCE_THUNDER);
        });
    }

    @EventHandler
    public void onLoggerDeath(LoggerDeathEvent event) {
        final CombatLogger logger = event.getLogger();
        final List<Player> recipients = Lists.newArrayList();
        String deathMessage = "(Combat Logger) " + logger.getOwnerUsername() + " died";

        if (event.getKiller() != null) {
            deathMessage = "(Combat Logger) " + logger.getOwnerUsername() + " was slain by " + event.getKiller().getName();
        }

        if (manager.getPlugin().getConfigManager().getGeneralConfig().isRangedChatEnabled()) {
            recipients.addAll(manager.getRecipientsInRange(logger.getBukkitEntity().getLocation(), ChatMessageType.DEATH_MESSAGE));
        } else {
            recipients.addAll(Bukkit.getOnlinePlayers());
        }

        for (Player p : recipients) {
            p.sendMessage(deathMessage);
        }
    }

    @EventHandler
    public void onPlayerPearled(PrisonPearlCreateEvent event) {
        final PrisonPearl prisonPearl = event.getPrisonPearl();
        final Location location = prisonPearl.getBukkitLocation();
        final List<Player> recipients = Lists.newArrayList();
        final String time = Time.convertToRemaining(prisonPearl.getExpireTime() - Time.now());
        final String message = ChatColor.GOLD + prisonPearl.getKillerUsername() + ChatColor.DARK_RED + " has imprisoned " + ChatColor.GOLD + prisonPearl.getImprisonedUsername() + ChatColor.DARK_RED + " for " + ChatColor.RED + time;

        if (manager.getPlugin().getConfigManager().getGeneralConfig().isRangedChatEnabled()) {
            recipients.addAll(manager.getRecipientsInRange(location, ChatMessageType.PLAYER_IMPRISONED));
        } else {
            recipients.addAll(Bukkit.getOnlinePlayers());
        }

        recipients.forEach(p -> p.sendMessage(message));
    }

    @EventHandler
    public void onPlayerReleased(PrisonPearlReleaseEvent event) {
        final PrisonPearl prisonPearl = event.getPrisonPearl();
        final Location location = prisonPearl.getBukkitLocation();
        final List<Player> recipients = Lists.newArrayList();

        if (manager.getPlugin().getConfigManager().getGeneralConfig().isRangedChatEnabled()) {
            recipients.addAll(manager.getRecipientsInRange(location, ChatMessageType.PLAYER_RELEASED));
        } else {
            recipients.addAll(Bukkit.getOnlinePlayers());
        }

        recipients.forEach(p -> {
            p.sendMessage(ChatColor.GOLD + prisonPearl.getImprisonedUsername() + ChatColor.GREEN + " has been released from their Prison Pearl");
            p.sendMessage(ChatColor.GOLD + "Reason" + ChatColor.YELLOW + ": " + event.getReason());
        });
    }
}
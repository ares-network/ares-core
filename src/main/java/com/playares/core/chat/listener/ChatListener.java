package com.playares.core.chat.listener;

import com.google.common.collect.Lists;
import com.playares.commons.event.ProcessedChatEvent;
import com.playares.commons.logger.Logger;
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
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

        if (member != null && !admin && !(member.hasPermission(NetworkPermission.ADMIN) && member.hasPermission(NetworkPermission.ACCESS_CHAT))) {
            manager.getHandler().leaveSession(player);
            player.sendMessage(ChatColor.RED + "You have been removed from this chat channel because you no longer have permission to access it");
            return;
        }

        event.setCancelled(true);
        network.sendMessage(ChatColor.GREEN + "[" + network.getName() + "] " + event.getDisplayName() + ": " + message);
        Logger.print("[" + network.getName() + "] " + player.getName() + ": " + message);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final Set<Player> players = event.getRecipients();
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final String deathMessage = event.getDeathMessage();
        final List<Player> inRange = manager.getRecipientsInRange(player.getLocation(), ChatMessageType.DEATH_MESSAGE);

        event.setDeathMessage(null);

        inRange.forEach(p -> p.sendMessage(deathMessage));
    }

    @EventHandler
    public void onLoggerDeath(LoggerDeathEvent event) {
        final CombatLogger logger = event.getLogger();
        final List<Player> inRange = manager.getPlugin().getChatManager().getRecipientsInRange(logger.getBukkitEntity().getLocation(), ChatMessageType.DEATH_MESSAGE);
        String deathMessage = "(Combat Logger) " + logger.getOwnerUsername() + " died";

        if (event.getKiller() != null) {
            deathMessage = "(Combat Logger) " + logger.getOwnerUsername() + " was slain by " + event.getKiller().getName();
        }

        for (Player p : inRange) {
            p.sendMessage(deathMessage);
        }
    }

    @EventHandler
    public void onPlayerPearled(PrisonPearlCreateEvent event) {
        final PrisonPearl prisonPearl = event.getPrisonPearl();
        final Location location = prisonPearl.getBukkitLocation();
        final List<Player> inRange = manager.getRecipientsInRange(location, ChatMessageType.PLAYER_IMPRISONED);
        final String message = ChatColor.RED + prisonPearl.getImprisonedUsername() + ChatColor.GRAY + " has been " + ChatColor.RED + "imprisoned" + ChatColor.GRAY + " by " + ChatColor.DARK_AQUA + prisonPearl.getKillerUsername();

        inRange.forEach(p -> p.sendMessage(message));
    }

    @EventHandler
    public void onPlayerReleased(PrisonPearlReleaseEvent event) {
        final PrisonPearl prisonPearl = event.getPrisonPearl();
        final Location location = prisonPearl.getBukkitLocation();
        final List<Player> inRange = manager.getRecipientsInRange(location, ChatMessageType.PLAYER_RELEASED);
        final String message = ChatColor.RED + prisonPearl.getImprisonedUsername() + ChatColor.GRAY + " has been " + ChatColor.GREEN + "released" + ChatColor.GRAY + ", Reason: " + event.getReason();

        inRange.forEach(p -> p.sendMessage(message));
    }
}
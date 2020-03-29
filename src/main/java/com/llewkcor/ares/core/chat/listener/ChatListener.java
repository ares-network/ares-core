package com.llewkcor.ares.core.chat.listener;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.event.ProcessedChatEvent;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.chat.ChatManager;
import com.llewkcor.ares.core.chat.data.ChatMessageType;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import com.llewkcor.ares.core.prison.event.PrisonPearlCreateEvent;
import com.llewkcor.ares.core.prison.event.PrisonPearlReleaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public final class ChatListener implements Listener {
    @Getter public final Ares plugin;
    @Getter public ChatManager manager;


    @EventHandler
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
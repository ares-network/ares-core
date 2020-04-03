package com.llewkcor.ares.core.timers.listener;

import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.timers.TimerManager;
import com.llewkcor.ares.core.timers.data.PlayerTimer;
import com.llewkcor.ares.core.timers.data.type.EnderpearlTimer;
import com.llewkcor.ares.core.timers.data.type.PlayerTimerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class TimerListener implements Listener {
    @Getter public final TimerManager manager;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        final ItemStack hand = player.getItemInHand();

        if (hand == null || !hand.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PlayerTimer existingTimer = manager.getTimer(player, PlayerTimerType.ENDERPEARL);

        if (existingTimer != null && !existingTimer.isExpired()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Enderpearls locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(existingTimer.getExpire() - Time.now()) + ChatColor.RED + "s");
            return;
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        final Projectile projectile = event.getEntity();

        if (!(projectile instanceof EnderPearl)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)projectile.getShooter();
        final PlayerTimer existingTimer = manager.getTimer(player, PlayerTimerType.ENDERPEARL);

        if (existingTimer != null && !existingTimer.isExpired()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Enderpearls locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(existingTimer.getExpire() - Time.now()) + ChatColor.RED + "s");
            return;
        }

        manager.getHandler().addTimer(player, new EnderpearlTimer(player.getUniqueId(), 16));
    }
}
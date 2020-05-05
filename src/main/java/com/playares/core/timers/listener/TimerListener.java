package com.playares.core.timers.listener;

import com.playares.commons.event.PlayerBigMoveEvent;
import com.playares.commons.event.PlayerDamagePlayerEvent;
import com.playares.commons.event.PlayerSplashPlayerEvent;
import com.playares.commons.location.BLocatable;
import com.playares.commons.util.bukkit.Players;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Time;
import com.playares.core.bastion.data.Bastion;
import com.playares.core.claim.data.Claim;
import com.playares.core.loggers.event.PlayerDamageLoggerEvent;
import com.playares.core.network.data.Network;
import com.playares.core.prison.event.PrePrisonPearlEvent;
import com.playares.core.spawn.event.PlayerEnterWorldEvent;
import com.playares.core.timers.TimerManager;
import com.playares.core.timers.data.PlayerTimer;
import com.playares.core.timers.data.type.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

@AllArgsConstructor
public final class TimerListener implements Listener {
    @Getter public final TimerManager manager;

    @EventHandler
    public void onPlayerEnterWorld(PlayerEnterWorldEvent event) {
        final Player player = event.getPlayer();

        if (!event.getEntranceMethod().equals(PlayerEnterWorldEvent.PlayerEnterWorldMethod.RANDOM)) {
            return;
        }

        final PearlProtectionTimer timer = new PearlProtectionTimer(player.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getPearlProtectionDuration());
        manager.getHandler().addTimer(player, timer);

        new Scheduler(manager.getPlugin()).sync(() -> {
            if (player.isOnline()) {
                player.sendMessage(
                        ChatColor.GOLD + "You have been granted " + ChatColor.YELLOW +
                        Time.convertToHHMMSS(manager.getPlugin().getConfigManager().getGeneralConfig().getPearlProtectionDuration() * 1000L) +
                        ChatColor.GOLD + " of " + ChatColor.GREEN + "Prison Pearl Protection");

                Players.playSound(player, Sound.NOTE_STICKS);
            }
        }).delay(5 * 20L).run();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerImprison(PrePrisonPearlEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = Bukkit.getPlayer(event.getImprisoned());

        final PearlProtectionTimer timer = (PearlProtectionTimer)manager.getTimer(player, PlayerTimerType.PEARL_PROTECTION);

        if (timer == null || timer.isExpired()) {
            return;
        }

        event.getKiller().sendMessage(ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " had " + ChatColor.GREEN + "Prison Pearl Protection");
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final Set<PlayerTimer> timers = manager.getActiveTimers(player);

        if (!timers.isEmpty()) {
            timers.forEach(timer -> manager.getHandler().removeTimer(player, timer));
        }
    }

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
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) {
            return;
        }

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

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();

        if (damaged.getUniqueId().equals(damager.getUniqueId())) {
            return;
        }

        final CombatTagTimer damagedExistingTimer = (CombatTagTimer)manager.getTimer(damaged, PlayerTimerType.COMBAT);
        final CombatTagTimer damagerExistingTimer = (CombatTagTimer)manager.getTimer(damager, PlayerTimerType.COMBAT);

        if (damagedExistingTimer == null) {
            damaged.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration() + " seconds");
        }

        if (damagerExistingTimer == null) {
            damager.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration() + " seconds");
        }

        if (damagedExistingTimer == null || damagedExistingTimer.getRemaining() / 1000L < manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration()) {
            manager.getHandler().addTimer(damaged, new CombatTagTimer(damaged.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration()));
        }

        manager.getHandler().addTimer(damager, new CombatTagTimer(damager.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration()));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerSplashPlayer(PlayerSplashPlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();

        if (damaged.getUniqueId().equals(damager.getUniqueId())) {
            return;
        }

        final CombatTagTimer damagedExistingTimer = (CombatTagTimer)manager.getTimer(damaged, PlayerTimerType.COMBAT);
        final CombatTagTimer damagerExistingTimer = (CombatTagTimer)manager.getTimer(damager, PlayerTimerType.COMBAT);

        if (damagedExistingTimer == null) {
            damaged.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration() + " seconds");
        }

        if (damagerExistingTimer == null) {
            damager.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration() + " seconds");
        }

        if (damagedExistingTimer == null || damagedExistingTimer.getRemaining() / 1000L < manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration()) {
            manager.getHandler().addTimer(damaged, new CombatTagTimer(damaged.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackedDuration()));
        }

        manager.getHandler().addTimer(damager, new CombatTagTimer(damager.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration()));
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (item.getType().equals(Material.GOLDEN_APPLE)) {
            if (item.getDurability() == (short)0) {
                final CrappleTimer crappleTimer = (CrappleTimer)manager.getTimer(player, PlayerTimerType.CRAPPLE);

                if (crappleTimer != null && !crappleTimer.isExpired()) {
                    player.sendMessage(ChatColor.RED + "Your crapples are locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(crappleTimer.getExpire() - Time.now()) + ChatColor.RED + "s");
                    event.setCancelled(true);
                    return;
                }

                manager.getHandler().addTimer(player, new CrappleTimer(player.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCrappleDuration()));

                return;
            }

            if (item.getDurability() == (short)1) {
                final GappleTimer gappleTimer = (GappleTimer)manager.getTimer(player, PlayerTimerType.GAPPLE);

                if (gappleTimer != null && !gappleTimer.isExpired()) {
                    player.sendMessage(ChatColor.RED + "Your crapples are locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(gappleTimer.getExpire() - Time.now()) + ChatColor.RED + "s");
                    event.setCancelled(true);
                    return;
                }

                manager.getHandler().addTimer(player, new GappleTimer(player.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getGappleDuration()));
            }
        }
    }

    @EventHandler
    public void onLoggerDamage(PlayerDamageLoggerEvent event) {
        final Player player = event.getPlayer();

        if (!manager.hasTimer(player, PlayerTimerType.COMBAT)) {
            player.sendMessage(ChatColor.RED + "You are combat-tagged for " + manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration() + " seconds");
        }

        manager.getHandler().addTimer(player, new CombatTagTimer(player.getUniqueId(), manager.getPlugin().getConfigManager().getGeneralConfig().getCombatTagAttackerDuration()));
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final Location from = event.getFrom();
        final Location to = event.getTo();
        final PearlProtectionTimer timer = (PearlProtectionTimer) manager.getTimer(player, PlayerTimerType.PEARL_PROTECTION);

        if (timer == null) {
            return;
        }

        final Set<Bastion> inRangeBastions = manager.getPlugin().getBastionManager().getBastionInRange(new BLocatable(to.getBlock()), manager.getPlugin().getConfigManager().getBastionsConfig().getBastionRadius());

        if (inRangeBastions.isEmpty()) {
            return;
        }

        for (Bastion bastion : inRangeBastions) {
            final Network owner = manager.getPlugin().getNetworkManager().getNetworkByID(bastion.getOwnerId());

            if (owner == null) {
                continue;
            }

            if (!owner.isMember(player.getUniqueId())) {
                player.teleport(from);
                player.sendMessage(ChatColor.RED + "You can not enter this bastion field while you have Pearl Protection. Type " + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + " to remove your Pearl Protection");
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final PearlProtectionTimer timer = (PearlProtectionTimer) manager.getTimer(player, PlayerTimerType.PEARL_PROTECTION);

        if (timer == null) {
            return;
        }

        final Claim claim = manager.getPlugin().getClaimManager().getClaimByBlock(block);
        final Set<Bastion> inRangeBastions = manager.getPlugin().getBastionManager().getBastionInRange(new BLocatable(block), manager.getPlugin().getConfigManager().getBastionsConfig().getBastionRadius());

        if (claim != null) {
            final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(claim.getOwnerId());

            if (network != null) {
                if (!network.isMember(player)) {
                    player.sendMessage(ChatColor.RED + "You can not break fortified blocks while you have Pearl Protection. Type " + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + " to remove your Pearl Protection");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (!inRangeBastions.isEmpty()) {
            for (Bastion bastion : inRangeBastions) {
                final Network owner = manager.getPlugin().getNetworkManager().getNetworkByID(bastion.getOwnerId());

                if (owner == null) {
                    continue;
                }

                if (!owner.isMember(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You can not modify blocks in this bastion field while you have Pearl Protection. Type " + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + " to remove your Pearl Protection");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final PearlProtectionTimer timer = (PearlProtectionTimer) manager.getTimer(player, PlayerTimerType.PEARL_PROTECTION);

        if (timer == null) {
            return;
        }

        final Set<Bastion> inRangeBastions = manager.getPlugin().getBastionManager().getBastionInRange(new BLocatable(block), manager.getPlugin().getConfigManager().getBastionsConfig().getBastionRadius());

        if (inRangeBastions.isEmpty()) {
            return;
        }

        for (Bastion bastion : inRangeBastions) {
            final Network owner = manager.getPlugin().getNetworkManager().getNetworkByID(bastion.getOwnerId());

            if (owner == null) {
                continue;
            }

            if (!owner.isMember(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You can not modify blocks in this bastion field while you have Pearl Protection. Type " + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + " to remove your Pearl Protection");
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPearlProtectedAttack(PlayerDamagePlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getDamager();

        if (player.getUniqueId().equals(event.getDamaged().getUniqueId())) {
            return;
        }

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final PearlProtectionTimer timer = (PearlProtectionTimer) manager.getTimer(player, PlayerTimerType.PEARL_PROTECTION);

        if (timer == null) {
            return;
        }

        player.sendMessage(ChatColor.RED + "You can not attack others while you have Pearl Protection. Type " + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + " to remove your Pearl Protection");
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onCombatLoggerDamage(PlayerDamageLoggerEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final PearlProtectionTimer timer = (PearlProtectionTimer) manager.getTimer(player, PlayerTimerType.PEARL_PROTECTION);

        if (timer == null) {
            return;
        }

        player.sendMessage(ChatColor.RED + "You can not attack others while you have Pearl Protection. Type " + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + " to remove your Pearl Protection");
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPearlProtectedSplash(PlayerSplashPlayerEvent event) {
        if (event.isCancelled() || event.getDamaged().getUniqueId().equals(event.getDamager().getUniqueId())) {
            return;
        }

        final Player player = event.getDamager();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final PearlProtectionTimer timer = (PearlProtectionTimer) manager.getTimer(player, PlayerTimerType.PEARL_PROTECTION);

        if (timer == null) {
            return;
        }

        player.sendMessage(ChatColor.RED + "You can not attack others while you have Pearl Protection. Type " + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + " to remove your Pearl Protection");
        event.setCancelled(true);
    }
}
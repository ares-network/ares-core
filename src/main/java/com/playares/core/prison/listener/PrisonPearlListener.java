package com.playares.core.prison.listener;

import com.playares.commons.logger.Logger;
import com.playares.commons.services.alts.data.AccountSession;
import com.playares.commons.services.alts.event.AltDetectEvent;
import com.playares.commons.util.general.Time;
import com.playares.core.loggers.entity.CombatLogger;
import com.playares.core.loggers.event.LoggerDeathEvent;
import com.playares.core.prison.PrisonPearlManager;
import com.playares.core.prison.data.PrisonPearl;
import com.playares.core.prison.event.PrisonPearlCreateEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@AllArgsConstructor
public final class PrisonPearlListener implements Listener {
    @Getter public final PrisonPearlManager manager;

    @EventHandler
    public void onAltDetected(AltDetectEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        int pearledAccounts = 0;

        for (AccountSession session : event.getSessions()) {
            final UUID sessionUniqueId = session.getBukkitId();
            final PrisonPearl pearl = manager.getPrisonPearlByPlayer(sessionUniqueId);

            if (pearl == null) {
                continue;
            }

            pearledAccounts += 1;
        }

        if (pearledAccounts > manager.getPlugin().getConfigManager().getPrisonPearlConfig().getMaxPrisonPearledAccounts()) {
            event.setCancelled(true);
            event.setDenyMessage(ChatColor.RED + "Prison Pearl Evasion");

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tempban " + event.getPlayer().getName() + " " + manager.getPlugin().getConfigManager().getPrisonPearlConfig().getAltBanDuration() + " Prison Pearl Evasion");
            Logger.print(event.getPlayer().getName() + " was banned for having " + pearledAccounts + " Prison Pearled alt accounts");
            return;
        }

        if (pearledAccounts > 1) {
            Bukkit.getOnlinePlayers().stream().filter(player ->
                    player.hasPermission("arescore.admin")).forEach(staff ->
                    staff.sendMessage(ChatColor.DARK_RED + "[Prison Evasion] " +
                            ChatColor.DARK_AQUA + event.getPlayer().getName() + ChatColor.GRAY + " is evading a prison pearl on an alt account. Type " + ChatColor.RED + "/lookup " + event.getPlayer().getName() + ChatColor.GRAY + " to view their account history."));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final Location prison = manager.getPrisonLocation();
        final PrisonPearl prisonPearl = manager.getPrisonPearlByPlayer(player.getUniqueId());

        if (prisonPearl == null) {
            return;
        }

        if (prison != null && !world.getName().equals(prison.getWorld().getName())) {
            player.teleport(prison);
        }

        player.sendMessage(ChatColor.RED + "Your Prison Pearl will expire in " + Time.convertToRemaining(prisonPearl.getExpireTime() - Time.now()));
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GOLD + "/pp locate" + ChatColor.GRAY + " to track the location of your Prison Pearl");
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "/pp info" + ChatColor.GRAY + " to view more information about your Prison Pearl");
    }

    @EventHandler
    public void onPrisonPearl(PrisonPearlCreateEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final PrisonPearl prisonPearl = event.getPrisonPearl();
        final Player player = prisonPearl.getImprisoned();

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.RED + "Your Prison Pearl will expire in " + Time.convertToRemaining(prisonPearl.getExpireTime() - Time.now()));
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GOLD + "/pp locate" + ChatColor.GRAY + " to track the location of your Prison Pearl");
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "/pp info" + ChatColor.GRAY + " to view more information about your Prison Pearl");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        manager.getMutedPearlNotifications().remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerImprison(PlayerDeathEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final Player player = event.getEntity();
        final Player killer = player.getKiller();
        final PrisonPearl existing = manager.getPrisonPearlByPlayer(player.getUniqueId());

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        if (killer == null) {
            return;
        }

        if (existing != null) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            final ItemStack item = killer.getInventory().getItem(i);

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                continue;
            }

            if (manager.getPrisonPearlByItem(item) != null) {
                continue;
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                killer.getInventory().remove(item);
            }

            manager.getHandler().imprisonPlayer(player.getName(), player.getUniqueId(), killer, manager.getBanDuration(player));
            break;
        }
    }

    @EventHandler
    public void onLoggerDeath(LoggerDeathEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final CombatLogger logger = event.getLogger();
        final Player killer = event.getKiller();
        final PrisonPearl existing = manager.getPrisonPearlByPlayer(logger.getOwnerId());

        if (killer == null) {
            return;
        }

        if (existing != null) {
            return;
        }

        if (logger.isPearlProtected()) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            final ItemStack item = killer.getInventory().getItem(i);

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                continue;
            }

            if (manager.getPrisonPearlByItem(item) != null) {
                continue;
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                killer.getInventory().remove(item);
            }

            manager.getHandler().imprisonPlayer(logger.getOwnerUsername(), logger.getOwnerId(), killer, logger.getBanDuration());
            break;
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onThrowPearl(ProjectileLaunchEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof EnderPearl) || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity().getShooter();
        final ItemStack hand = player.getItemInHand();

        if (hand == null || !hand.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(hand);

        if (prisonPearl != null) {
            event.setCancelled(true);
            player.getInventory().removeItem(hand);

            if (prisonPearl.isExpired()) {
                player.sendMessage(ChatColor.YELLOW + "Prison Pearl has expired");
                return;
            }

            manager.getHandler().releasePearl(prisonPearl, "Released by " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        final PrisonPearl prisonPearl = manager.getPrisonPearlByPlayer(player.getUniqueId());

        if (prisonPearl == null) {
            return;
        }

        final Location prison = manager.getPrisonLocation();

        if (prison == null) {
            return;
        }

        event.setRespawnLocation(prison);
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByPlayer(player.getUniqueId());

        if (prisonPearl == null) {
            return;
        }

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL) || event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {
            player.sendMessage(ChatColor.RED + "You can not change worlds while imprisoned");
            event.setCancelled(true);
        }
    }
}
package com.llewkcor.ares.core.prison.listener;

import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.prison.PrisonPearlManager;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import com.llewkcor.ares.core.prison.event.PrisonPearlCreateEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public final class PrisonPearlListener implements Listener {
    @Getter public final PrisonPearlManager manager;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
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
    public void onPlayerImprison(PlayerDeathEvent event) {
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

            manager.getHandler().imprisonPlayer(player.getName(), player.getUniqueId(), killer);
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onThrowPearl(ProjectileLaunchEvent event) {
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

        if (prisonPearl == null) {
            return;
        }

        event.setCancelled(true);
        manager.getHandler().releasePearl(prisonPearl, "Released by " + player.getName());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
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
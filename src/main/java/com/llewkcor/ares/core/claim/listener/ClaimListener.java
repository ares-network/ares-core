package com.llewkcor.ares.core.claim.listener;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.bukkit.Blocks;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.claim.ClaimManager;
import com.llewkcor.ares.core.claim.data.Claim;
import com.llewkcor.ares.core.claim.data.ClaimDAO;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public final class ClaimListener implements Listener {
    @Getter public final ClaimManager manager;

    @EventHandler (priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Claim claim = manager.getClaimByBlock(block);

        if (claim == null) {
            return;
        }

        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(claim.getOwnerId());

        if (network == null) {
            Logger.error("A claim was found with no attached network");
            return;
        }

        final Runnable deleteTask = () -> ClaimDAO.deleteClaim(manager.getPlugin().getDatabaseInstance(), claim);

        if (network.isMember(player) && (network.getMember(player).hasPermission(NetworkPermission.ADMIN) || network.getMember(player).hasPermission(NetworkPermission.MODIFY_CLAIMS))) {
            final ItemStack reinforcement = new ItemStack(claim.getType().getMaterial());
            block.getWorld().dropItemNaturally(block.getLocation(), reinforcement);

            new Scheduler(manager.getPlugin()).async(deleteTask).run();

            manager.getClaimRepository().remove(claim);

            return;
        }

        if (claim.getHealth() <= 1 || !claim.isMatured()) {
            final ItemStack reinforcement = new ItemStack(claim.getType().getMaterial());
            block.getWorld().dropItemNaturally(block.getLocation(), reinforcement);

            new Scheduler(manager.getPlugin()).async(deleteTask).run();

            manager.getClaimRepository().remove(claim);

            return;
        }

        event.setCancelled(true);
        claim.setHealth(claim.getHealth() - 1);
        player.sendMessage(ChatColor.RED + "Locked " + claim.getHealthAsPercent() + " with " + claim.getType().getDisplayName() + ", " + (claim.isMatured() ? "is matured" : "matures in " + Time.convertToRemaining(claim.getMatureTime() - Time.now())));
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();
        final boolean admin = player.hasPermission("arescore.admin");

        if (admin) {
            return;
        }

        if (block == null || !action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (!Blocks.isInteractable(block.getType())) {
            return;
        }

        final Claim claim = manager.getClaimByBlock(block);

        if (claim == null || !claim.isMatured()) {
            return;
        }

        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(claim.getOwnerId());

        if (network == null) {
            Logger.error("A claim was found with no attached network");
            return;
        }

        final boolean canAccess = (network.isMember(player) && (network.getMember(player).hasPermission(NetworkPermission.ADMIN) || network.getMember(player).hasPermission(NetworkPermission.ACCESS_LAND)));

        if (!canAccess) {
            player.sendMessage(ChatColor.RED + "Locked " + claim.getHealthAsPercent() + " with " + claim.getType().getDisplayName() + ", " + (claim.isMatured() ? "is matured" : "matures in " + Time.convertToRemaining(claim.getMatureTime() - Time.now())));
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block block = event.getBlock();
        final Claim claim = manager.getClaimByBlock(block);

        if (claim == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final List<Block> toRemove = Lists.newArrayList();

        for (Block block : event.blockList()) {
            final Claim claim = manager.getClaimByBlock(block);

            if (claim == null) {
                continue;
            }

            toRemove.add(block);
        }

        event.blockList().removeAll(toRemove);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final List<Block> toRemove = Lists.newArrayList();

        for (Block block : event.getBlocks()) {
            final Claim claim = manager.getClaimByBlock(block);

            if (claim == null) {
                continue;
            }

            toRemove.add(block);
        }

        event.getBlocks().removeAll(toRemove);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block piston = event.getBlock();
        final Claim claim = manager.getClaimByBlock(piston);

        for (Block affected : event.getBlocks()) {
            final Claim affectedClaim = manager.getClaimByBlock(affected);

            if (claim == null && affectedClaim != null) {
                event.setCancelled(true);
                return;
            }

            if (claim != null && affectedClaim != null && !claim.getOwnerId().equals(affectedClaim.getOwnerId())) {
                event.setCancelled(true);
                return;
            }

            if (affectedClaim != null) {
                final Block newBlock = affected.getRelative(event.getDirection());
                affectedClaim.setLocation(new BLocatable(newBlock));
                affectedClaim.setChunkX(newBlock.getChunk().getX());
                affectedClaim.setChunkX(newBlock.getChunk().getZ());
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block piston = event.getBlock();
        final Claim claim = manager.getClaimByBlock(piston);

        for (Block affected : event.getBlocks()) {
            final Claim affectedClaim = manager.getClaimByBlock(affected);

            if (claim == null && affectedClaim != null) {
                event.setCancelled(true);
                return;
            }

            if (claim != null && affectedClaim != null && !claim.getOwnerId().equals(affectedClaim.getOwnerId())) {
                event.setCancelled(true);
                return;
            }

            if (affectedClaim != null) {
                final Block newBlock = affected.getRelative(event.getDirection());
                affectedClaim.setLocation(new BLocatable(newBlock));
                affectedClaim.setChunkX(newBlock.getChunk().getX());
                affectedClaim.setChunkX(newBlock.getChunk().getZ());
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block block = event.getBlock();
        final Claim claim = manager.getClaimByBlock(block);

        if (claim == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlockPlaced();

        if (block == null || !(block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST))) {
            return;
        }

        final Block east = block.getRelative(BlockFace.EAST);
        final Block west = block.getRelative(BlockFace.WEST);
        final Block north = block.getRelative(BlockFace.NORTH);
        final Block south = block.getRelative(BlockFace.SOUTH);

        final Claim cE = manager.getClaimByBlock(east);
        final Claim cW = manager.getClaimByBlock(west);
        final Claim cN = manager.getClaimByBlock(north);
        final Claim cS = manager.getClaimByBlock(south);

        if (cE != null && (east.getType().equals(Material.CHEST) || east.getType().equals(Material.TRAPPED_CHEST))) {
            player.sendMessage(ChatColor.RED + "You can not place this block because it bypasses a nearby chest reinforcement");
            event.setCancelled(true);
        }

        if (cW != null && (west.getType().equals(Material.CHEST) || west.getType().equals(Material.TRAPPED_CHEST))) {
            player.sendMessage(ChatColor.RED + "You can not place this block because it bypasses a nearby chest reinforcement");
            event.setCancelled(true);
        }

        if (cN != null && (north.getType().equals(Material.CHEST) || north.getType().equals(Material.TRAPPED_CHEST))) {
            player.sendMessage(ChatColor.RED + "You can not place this block because it bypasses a nearby chest reinforcement");
            event.setCancelled(true);
        }

        if (cS != null && (south.getType().equals(Material.CHEST) || south.getType().equals(Material.TRAPPED_CHEST))) {
            player.sendMessage(ChatColor.RED + "You can not place this block because it bypasses a nearby chest reinforcement");
            event.setCancelled(true);
        }
    }
}
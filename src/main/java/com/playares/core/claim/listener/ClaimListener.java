package com.playares.core.claim.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.location.BLocatable;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.bukkit.Blocks;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Time;
import com.playares.core.claim.ClaimManager;
import com.playares.core.claim.data.Claim;
import com.playares.core.claim.data.ClaimDAO;
import com.playares.core.claim.session.ClaimSession;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkPermission;
import com.playares.core.utils.BlockUtil;
import com.playares.humbug.event.FoundOreEvent;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ClaimListener implements Listener {
    @Getter public final ClaimManager manager;
    @Getter public final Set<UUID> physicalInteractCooldowns;
    @Getter public final Set<UUID> blockGlitchCooldowns;

    public ClaimListener(ClaimManager manager) {
        this.manager = manager;
        this.physicalInteractCooldowns = Sets.newConcurrentHashSet();
        this.blockGlitchCooldowns = Sets.newConcurrentHashSet();
    }

    /**
     * Returns true if this player has physically interacted with redstone within the last 20 ticks
     * @param player Player
     * @return True if on cooldown
     */
    private boolean hasPhysicalInteractCooldown(Player player) {
        return physicalInteractCooldowns.contains(player.getUniqueId());
    }

    /**
     * Returns true if this player has broken a claimed block within the last 5 ticks
     * @param player Player
     * @return True if on cooldown and should be disallowed block placing of any sort
     */
    private boolean hasBlockGlitchCooldown(Player player) {
        return blockGlitchCooldowns.contains(player.getUniqueId());
    }

    /**
     * Applies a cooldown to the provided player preventing them from receiving spam claim notifications
     * @param player Player
     */
    private void applyPhysicalInteractCooldown(Player player) {
        final UUID uniqueId = player.getUniqueId();

        physicalInteractCooldowns.add(uniqueId);
        new Scheduler(manager.getPlugin()).sync(() -> physicalInteractCooldowns.remove(uniqueId)).delay(20L).run();
    }

    /**
     * Applies a cooldown to the provided player preventing them from placing any blocks for 2 ticks
     * @param player Player
     */
    private void applyBlockGlitchCooldown(Player player) {
        final UUID uniqueId = player.getUniqueId();

        blockGlitchCooldowns.add(uniqueId);
        new Scheduler(manager.getPlugin()).sync(() -> blockGlitchCooldowns.remove(uniqueId)).delay(2L).run();
    }

    @EventHandler
    public void onBlockGlitchAttempt(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (block == null || block.getType().equals(Material.AIR)) {
            return;
        }

        if (hasBlockGlitchCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Please wait a moment before trying to place a block");
            Logger.warn(player.getName() + " attempted to block glitch using a " + block.getType().name());
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onFoundOre(FoundOreEvent event) {
        if (event.isCancelled() || event.getBlocks().isEmpty()) {
            return;
        }

        final List<Block> toRemove = Lists.newArrayList();

        event.getBlocks().forEach(block -> {
            final Claim claim = manager.getClaimByBlock(block);

            if (claim != null) {
                toRemove.add(block);
            }
        });

        if (toRemove.isEmpty()) {
            return;
        }

        event.getBlocks().removeAll(toRemove);
    }

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

        final List<Block> multiBlocks = BlockUtil.getMultiblockLocations(block);
        final List<Claim> otherClaims = Lists.newArrayList();
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(claim.getOwnerId());

        if (network == null) {
            Logger.error("A claim was found with no attached network");
            return;
        }

        if (multiBlocks.size() > 1) {
            multiBlocks.stream().filter(multiBlock -> !block.equals(multiBlock)).forEach(otherBlock -> {
                final Claim otherClaim = manager.getClaimByBlock(otherBlock);

                if (otherClaim != null) {
                    otherClaims.add(otherClaim);
                }
            });
        }

        if (network.isMember(player) && (network.getMember(player).hasPermission(NetworkPermission.ADMIN) || network.getMember(player).hasPermission(NetworkPermission.MODIFY_CLAIMS))) {
            final ItemStack reinforcement = new ItemBuilder().setMaterial(claim.getType().getMaterial()).setAmount(multiBlocks.size()).build();
            block.getWorld().dropItemNaturally(block.getLocation(), reinforcement);

            manager.getClaimRepository().remove(claim);
            new Scheduler(manager.getPlugin()).async(() -> ClaimDAO.deleteClaim(manager.getPlugin().getDatabaseInstance(), claim)).run();

            if (!otherClaims.isEmpty()) {
                otherClaims.forEach(otherClaim -> {
                    new Scheduler(manager.getPlugin()).async(() -> ClaimDAO.deleteClaim(manager.getPlugin().getDatabaseInstance(), otherClaim)).run();
                    manager.getClaimRepository().remove(otherClaim);
                });
            }

            return;
        }

        if (claim.getHealth() <= 1 || !claim.isMatured() || player.hasPermission("arescore.admin")) {
            final ItemStack reinforcement = new ItemBuilder().setMaterial(claim.getType().getMaterial()).setAmount(multiBlocks.size()).build();
            block.getWorld().dropItemNaturally(block.getLocation(), reinforcement);

            manager.getClaimRepository().remove(claim);
            new Scheduler(manager.getPlugin()).async(() -> ClaimDAO.deleteClaim(manager.getPlugin().getDatabaseInstance(), claim)).run();

            if (!otherClaims.isEmpty()) {
                otherClaims.forEach(otherClaim -> {
                    new Scheduler(manager.getPlugin()).async(() -> ClaimDAO.deleteClaim(manager.getPlugin().getDatabaseInstance(), otherClaim)).run();
                    manager.getClaimRepository().remove(otherClaim);
                });
            }

            return;
        }

        event.setCancelled(true);
        claim.setHealth(claim.getHealth() - 1);
        player.sendMessage(ChatColor.RED + "Locked " + claim.getHealthAsPercent() + " with " + claim.getType().getDisplayName() + ", " + (claim.isMatured() ? "is matured" : "matures in " + Time.convertToRemaining(claim.getMatureTime() - Time.now())));
        applyBlockGlitchCooldown(player);
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
        final List<Block> multiBlocks = BlockUtil.getMultiblockLocations(block);

        if (admin) {
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.PHYSICAL)) {
            return;
        }

        if (block == null) {
            return;
        }

        for (Block multiBlock : multiBlocks) {
            final Claim claim = manager.getClaimByBlock(multiBlock);

            if (claim == null || !claim.isMatured()) {
                continue;
            }

            final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(claim.getOwnerId());

            if (network == null) {
                Logger.error("Failed to obtain network for claim block at " + claim.getLocation().toString());
                continue;
            }

            boolean canAccess = true;

            if (!network.isMember(player)) {
                canAccess = false;
            }

            if (network.isMember(player) && !(network.getMember(player).hasPermission(NetworkPermission.ADMIN) || network.getMember(player).hasPermission(NetworkPermission.ACCESS_LAND))) {
                canAccess = false;
            }

            if (!canAccess) {
                if (action.equals(Action.PHYSICAL)) {
                    event.setCancelled(true);

                    if (hasPhysicalInteractCooldown(player)) {
                        continue;
                    }

                    applyPhysicalInteractCooldown(player);
                    player.sendMessage(ChatColor.RED + "Locked " + claim.getHealthAsPercent() + " with " + claim.getType().getDisplayName() + ", " + (claim.isMatured() ? "is matured" : "matures in " + Time.convertToRemaining(claim.getMatureTime() - Time.now())));
                    return;
                }

                else if (Blocks.isInteractable(block.getType())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Locked " + claim.getHealthAsPercent() + " with " + claim.getType().getDisplayName() + ", " + (claim.isMatured() ? "is matured" : "matures in " + Time.convertToRemaining(claim.getMatureTime() - Time.now())));
                    return;
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final InventoryHolder sourceHolder = event.getSource().getHolder();
        final InventoryHolder destHolder = event.getDestination().getHolder();
        final Location source = getInventoryLocation(sourceHolder);
        final Location dest = getInventoryLocation(destHolder);

        if (source != null && dest != null) {
            final Block sourceBlock = source.getBlock();
            final Block destBlock = dest.getBlock();

            final Claim sourceClaim = manager.getClaimByBlock(sourceBlock);
            final Claim destClaim = manager.getClaimByBlock(destBlock);

            if (sourceClaim != null) {
                if (destClaim == null) {
                    event.setCancelled(true);
                    return;
                }

                if (!destClaim.getOwnerId().equals(sourceClaim.getOwnerId())) {
                    event.setCancelled(true);
                    return;
                }
            }
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

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Block block = event.getBlock();

        if (block == null) {
            return;
        }

        final Material type = block.getType();

        if (!(type.equals(Material.SAND) || type.equals(Material.GRAVEL) || type.equals(Material.ANVIL))) {
            return;
        }

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

        if (!event.getReason().equals(PortalCreateEvent.CreateReason.OBC_DESTINATION)) {
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

        final ClaimSession session = manager.getSessionByPlayer(player);

        final Block east = block.getRelative(BlockFace.EAST);
        final Block west = block.getRelative(BlockFace.WEST);
        final Block north = block.getRelative(BlockFace.NORTH);
        final Block south = block.getRelative(BlockFace.SOUTH);

        final Claim cE = manager.getClaimByBlock(east);
        final Claim cW = manager.getClaimByBlock(west);
        final Claim cN = manager.getClaimByBlock(north);
        final Claim cS = manager.getClaimByBlock(south);

        if (cE != null && east.getType().equals(block.getType()) && (east.getType().equals(Material.CHEST) || east.getType().equals(Material.TRAPPED_CHEST)) && (session == null || !session.getNetworkId().equals(cE.getOwnerId()))) {
            player.sendMessage(ChatColor.RED + "You can not place this block because it bypasses a nearby chest reinforcement");
            event.setCancelled(true);
        }

        if (cW != null && west.getType().equals(block.getType()) && (west.getType().equals(Material.CHEST) || west.getType().equals(Material.TRAPPED_CHEST)) && (session == null || !session.getNetworkId().equals(cW.getOwnerId()))) {
            player.sendMessage(ChatColor.RED + "You can not place this block because it bypasses a nearby chest reinforcement");
            event.setCancelled(true);
        }

        if (cN != null && north.getType().equals(block.getType()) && (north.getType().equals(Material.CHEST) || north.getType().equals(Material.TRAPPED_CHEST)) && (session == null || !session.getNetworkId().equals(cN.getOwnerId()))) {
            player.sendMessage(ChatColor.RED + "You can not place this block because it bypasses a nearby chest reinforcement");
            event.setCancelled(true);
        }

        if (cS != null && south.getType().equals(block.getType()) && (south.getType().equals(Material.CHEST) || south.getType().equals(Material.TRAPPED_CHEST)) && (session == null || !session.getNetworkId().equals(cS.getOwnerId()))) {
            player.sendMessage(ChatColor.RED + "You can not place this block because it bypasses a nearby chest reinforcement");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        final Block block = event.getBlock();
        final Claim claim = manager.getClaimByBlock(block);

        if (claim == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        final Block block = event.getBlock();
        final List<Block> multiBlocks = BlockUtil.getMultiblockLocations(block);

        for (Block b : multiBlocks) {
            final Claim claim = manager.getClaimByBlock(b);

            if (claim == null) {
                continue;
            }

            if (!claim.isMatured()) {
                continue;
            }

            final Network owner = manager.getPlugin().getNetworkManager().getNetworkByID(claim.getOwnerId());

            if (owner == null) {
                continue;
            }

            boolean valid = false;

            for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 5, 5, 5)) {
                if (!(entity instanceof Player)) {
                    continue;
                }

                final Player player = (Player)entity;

                if (owner.isMember(player)) {
                    valid = true;
                    break;
                }
            }

            if (!valid) {
                event.setNewCurrent(0);
                return;
            }
        }
    }

    /**
     * Returns the location of the provided InventoryHolder
     * @param holder InventoryHolder
     * @return Bukkit Location
     */
    private Location getInventoryLocation(InventoryHolder holder) {
        if (holder instanceof Chest) {
            return ((Chest)holder).getLocation();
        } else if (holder instanceof DoubleChest) {
            return ((DoubleChest)holder).getLocation();
        } else if (holder instanceof Furnace) {
            return ((Furnace)holder).getLocation();
        } else if (holder instanceof Dispenser) {
            return ((Dispenser)holder).getLocation();
        } else if (holder instanceof BrewingStand) {
            return ((BrewingStand)holder).getLocation();
        } else if (holder instanceof Hopper) {
            return ((Hopper)holder).getLocation();
        } else if (holder instanceof Dropper) {
            return ((Dropper)holder).getLocation();
        } else {
            return null;
        }
    }
}
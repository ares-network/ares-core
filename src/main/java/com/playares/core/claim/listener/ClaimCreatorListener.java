package com.playares.core.claim.listener;

import com.google.common.collect.Lists;
import com.playares.commons.location.BLocatable;
import com.playares.commons.util.general.Time;
import com.playares.core.claim.ClaimManager;
import com.playares.core.claim.data.Claim;
import com.playares.core.claim.event.BlockReinforceEvent;
import com.playares.core.claim.session.ClaimSession;
import com.playares.core.claim.session.ClaimSessionType;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkPermission;
import com.playares.core.utils.BlockUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public final class ClaimCreatorListener implements Listener {
    @Getter public final ClaimManager manager;

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlockPlaced();
        final ClaimSession session = manager.getSessionByPlayer(player);

        if (session == null || session.getSessionType().equals(ClaimSessionType.INFO) || session.getSessionType().equals(ClaimSessionType.REINFORCE)) {
            return;
        }

        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(session.getNetworkId());
        final List<Block> blocks = BlockUtil.getMultiblockLocations(block);
        boolean merge = false;

        if (network == null || !network.isMember(player) || !(network.getMember(player).hasPermission(NetworkPermission.ADMIN) || network.getMember(player).hasPermission(NetworkPermission.MODIFY_CLAIMS))) {
            player.sendMessage(ChatColor.RED + "The network or your status in the network has been modified and you are no longer able to claim");
            manager.getActiveClaimSessions().remove(session);
            event.setCancelled(true);
            return;
        }

        if (manager.getPlugin().getConfigManager().getClaimsConfig().getNonReinforceables().contains(block.getType())) {
            player.sendMessage(ChatColor.RED + "This type of block can not be fortified");
            return;
        }

        if (block.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            player.sendMessage(ChatColor.RED + "Blocks in The End can not be fortified");
            return;
        }

        if (blocks.size() > 1 && (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST))) {
            for (Block  multiBlock : blocks) {
                final Claim claim = manager.getClaimByBlock(multiBlock);

                if (claim != null && claim.getOwnerId().equals(session.getNetworkId())) {
                    merge = true;
                    break;
                }
            }
        }

        final BlockReinforceEvent reinforceEvent = new BlockReinforceEvent(player, blocks);
        Bukkit.getPluginManager().callEvent(reinforceEvent);

        if (reinforceEvent.isCancelled()) {
            return;
        }

        final Material materialToSubtract = session.getClaimType().getMaterial();
        final int cost = (merge ? blocks.size() - 1 : blocks.size());
        boolean paid = false;

        for (ItemStack item : player.getInventory()) {
            if (item == null || !item.getType().equals(materialToSubtract)) {
                continue;
            }

            // Player has more reinforcement material than the total blocks being reinforced
            if (item.getAmount() > cost) {

                // Player is reinforcing with the same type as reinforcement material (stone)
                if (item.getType().equals(materialToSubtract) && item.getDurability() == (short)0) {

                    // Player now needs to have an additional reinforcement material since the game will override subtraction
                    if (item.getAmount() >= (cost + 1)) {
                        if (item.getAmount() > cost) {
                            // Player still has more
                            item.setAmount(item.getAmount() - (cost + 1));
                        } else {
                            // Player has the exact amount
                            player.getInventory().removeItem(item);
                        }

                        paid = true;
                        break;
                    }

                } else {
                    // Not reinforcing with the reinforcement material, just subtract the actual cost
                    item.setAmount(item.getAmount() - cost);
                    paid = true;
                    break;
                }

            } else if (item.getAmount() == cost) {

                // Player is reinforcing with the same type as reinforcement material (stone)
                if (block.getType().equals(materialToSubtract) && item.getDurability() == (short)0) {
                    continue;
                }

                player.getInventory().removeItem(item);
                paid = true;
                break;
            }
        }

        if (!paid) {
            player.sendMessage(ChatColor.RED + "You don't have enough materials");
            manager.getActiveClaimSessions().remove(session);
            event.setCancelled(true);
            return;
        }

        player.updateInventory();

        blocks.forEach(claimBlock -> {
            final Claim claim = new Claim(network.getUniqueId(), block.getChunk().getX(), block.getChunk().getZ(), new BLocatable(claimBlock), session.getClaimType());
            manager.getClaimRepository().add(claim);
            block.getWorld().spigot().playEffect(claimBlock.getLocation(), Effect.FLYING_GLYPH, 0, 0, (float)1.0, (float)0.5, (float)1.0, (float)0.01, 15, 8);
        });
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onReinforce(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        if (!action.equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        if (block == null || block.getType().equals(Material.AIR)) {
            return;
        }

        final ClaimSession session = getManager().getSessionByPlayer(player);

        if (session == null || session.getSessionType().equals(ClaimSessionType.INFO) || session.getSessionType().equals(ClaimSessionType.FORTIFY)) {
            return;
        }

        final List<Claim> existing = Lists.newArrayList();
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(session.getNetworkId());
        final List<Block> blocks = BlockUtil.getMultiblockLocations(block);

        blocks.forEach(multiBlock -> {
            final Claim existingClaim = manager.getClaimByBlock(multiBlock);

            if (existingClaim != null) {
                existing.add(existingClaim);
            }
        });

        if (network == null || !network.isMember(player) || !(network.getMember(player).hasPermission(NetworkPermission.ADMIN) || network.getMember(player).hasPermission(NetworkPermission.MODIFY_CLAIMS))) {
            player.sendMessage(ChatColor.RED + "The network or your status in the network has been modified and you are no longer able to claim");
            manager.getActiveClaimSessions().remove(session);
            event.setCancelled(true);
            return;
        }

        if (!existing.isEmpty()) {
            player.sendMessage(ChatColor.RED + "This block is already claimed");
            event.setCancelled(true);
            return;
        }

        if (manager.getPlugin().getConfigManager().getClaimsConfig().getNonReinforceables().contains(block.getType())) {
            player.sendMessage(ChatColor.RED + "This type of block can not be reinforced");
            return;
        }

        if (block.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            player.sendMessage(ChatColor.RED + "Blocks in The End can not be fortified");
            return;
        }

        final BlockReinforceEvent reinforceEvent = new BlockReinforceEvent(player, blocks);
        Bukkit.getPluginManager().callEvent(reinforceEvent);

        if (reinforceEvent.isCancelled()) {
            return;
        }

        final Material materialToSubtract = session.getClaimType().getMaterial();
        final ItemStack hand = player.getInventory().getItemInHand();

        if (hand == null || !hand.getType().equals(materialToSubtract)) {
            player.sendMessage(ChatColor.RED + "You are not holding the claim material");
            return;
        }

        if (hand.getAmount() > blocks.size()) {
            hand.setAmount(hand.getAmount() - blocks.size());
        } else if (hand.getAmount() == blocks.size()) {
            player.getInventory().removeItem(hand);
        } else {
            player.sendMessage(ChatColor.RED + "You do not have enough materials to reinforce this block");
            return;
        }

        blocks.forEach(claimBlock -> {
            final Claim claim = new Claim(network.getUniqueId(), block.getChunk().getX(), block.getChunk().getZ(), new BLocatable(claimBlock), session.getClaimType());
            manager.getClaimRepository().add(claim);
            block.getWorld().spigot().playEffect(claimBlock.getLocation(), Effect.FLYING_GLYPH, 0, 0, (float)1.0, (float)0.5, (float)1.0, (float)0.01, 20, 6);
        });
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onInformation(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (block == null || block.getType().equals(Material.AIR)) {
            return;
        }

        final ClaimSession session = getManager().getSessionByPlayer(player);

        if (session == null || !session.getSessionType().equals(ClaimSessionType.INFO)) {
            return;
        }

        final Claim claim = manager.getClaimByBlock(block);

        if (claim == null) {
            return;
        }

        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(claim.getOwnerId());
        final boolean canAccess = (network.isMember(player) && (network.getMember(player).hasPermission(NetworkPermission.ADMIN) || network.getMember(player).hasPermission(NetworkPermission.ACCESS_LAND)));

        if (canAccess) {
            player.sendMessage(ChatColor.YELLOW + "Protected " + claim.getHealthAsPercent() + " with " + claim.getType().getDisplayName() + " by " + network.getName() + ", " +
                    (claim.isMatured() ? "is matured" : "matures in " + Time.convertToRemaining(claim.getMatureTime() - Time.now())));
        } else {
            player.sendMessage(ChatColor.RED + "Locked " + claim.getHealthAsPercent() + " with " + claim.getType().getDisplayName() + ", " + (claim.isMatured() ? "is matured" : "matures in " + Time.convertToRemaining(claim.getMatureTime() - Time.now())));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final ClaimSession session = getManager().getSessionByPlayer(player);

        if (session == null) {
            return;
        }

        manager.getActiveClaimSessions().remove(session);
    }
}
package com.llewkcor.ares.core.claim.listener;

import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.claim.ClaimManager;
import com.llewkcor.ares.core.claim.data.Claim;
import com.llewkcor.ares.core.claim.event.BlockReinforceEvent;
import com.llewkcor.ares.core.claim.session.ClaimSession;
import com.llewkcor.ares.core.claim.session.ClaimSessionType;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkPermission;
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

        final BlockReinforceEvent reinforceEvent = new BlockReinforceEvent(player, block);
        Bukkit.getPluginManager().callEvent(reinforceEvent);

        if (reinforceEvent.isCancelled()) {
            return;
        }

        final Material materialToSubtract = session.getClaimType().getMaterial();
        boolean paid = false;

        for (ItemStack item : player.getInventory()) {
            if (item == null || !item.getType().equals(materialToSubtract)) {
                continue;
            }

            // FUCKING MESS AHHHHHH FAHGET UHBOUT IT
            if (item.getAmount() > 1) {
                // Player is using Stone reinforcement to reinforce stone, game overrides the subtraction meaning we need to double it
                if (block.getType().equals(materialToSubtract) && block.getTypeId() == (short)0) {
                    if (item.getAmount() > 2) {
                        item.setAmount(item.getAmount() - 2);
                    } else {
                        player.getInventory().remove(item);
                    }
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
            } else {
                player.getInventory().removeItem(item);
            }

            player.updateInventory();
            paid = true;
            break;
        }

        if (!paid) {
            player.sendMessage(ChatColor.RED + "You don't have enough materials");
            manager.getActiveClaimSessions().remove(session);
            event.setCancelled(true);
            return;
        }

        final Claim claim = new Claim(network.getUniqueId(), block.getChunk().getX(), block.getChunk().getZ(), new BLocatable(block), session.getClaimType());
        manager.getClaimRepository().add(claim);
        block.getWorld().spigot().playEffect(block.getLocation(), Effect.FLYING_GLYPH, 0, 0, (float)1.0, (float)0.5, (float)1.0, (float)0.01, 15, 8);
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

        final Claim existing = manager.getClaimByBlock(block);
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByID(session.getNetworkId());

        if (network == null || !network.isMember(player) || !(network.getMember(player).hasPermission(NetworkPermission.ADMIN) || network.getMember(player).hasPermission(NetworkPermission.MODIFY_CLAIMS))) {
            player.sendMessage(ChatColor.RED + "The network or your status in the network has been modified and you are no longer able to claim");
            manager.getActiveClaimSessions().remove(session);
            event.setCancelled(true);
            return;
        }

        if (existing != null) {
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

        final BlockReinforceEvent reinforceEvent = new BlockReinforceEvent(player, block);
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

        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().removeItem(hand);
        }

        final Claim claim = new Claim(network.getUniqueId(), block.getChunk().getX(), block.getChunk().getZ(), new BLocatable(block), session.getClaimType());
        manager.getClaimRepository().add(claim);
        block.getWorld().spigot().playEffect(block.getLocation(), Effect.FLYING_GLYPH, 0, 0, (float)1.0, (float)0.5, (float)1.0, (float)0.01, 15, 8);
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
            player.sendMessage(ChatColor.YELLOW + "Protected " + claim.getHealthAsPercent() + " with " + claim.getType().getDisplayName() + ", " +
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
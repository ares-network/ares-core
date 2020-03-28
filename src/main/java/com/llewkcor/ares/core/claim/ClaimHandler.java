package com.llewkcor.ares.core.claim;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.claim.data.ClaimDAO;
import com.llewkcor.ares.core.claim.data.ClaimType;
import com.llewkcor.ares.core.claim.session.ClaimSession;
import com.llewkcor.ares.core.claim.session.ClaimSessionType;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ClaimHandler {
    @Getter public ClaimManager manager;

    public ClaimHandler(ClaimManager manager) {
        this.manager = manager;
    }

    /**
     * Load all claims from the database to memory
     * @param blocking Block the current thread
     */
    public void loadAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to load all claims from the database");
            manager.getClaimRepository().addAll(ClaimDAO.getClaims(manager.getPlugin().getDatabaseInstance()));
            Logger.print("Loaded " + manager.getClaimRepository().size() + " Claims");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getClaimRepository().addAll(ClaimDAO.getClaims(manager.getPlugin().getDatabaseInstance()));
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Loaded " + manager.getClaimRepository().size() + " Claims")).run();
        }).run();
    }

    /**
     * Save all claims to the database from memory
     * @param blocking Block the current thread
     */
    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to save all networks to the database");
            ClaimDAO.saveClaims(manager.getPlugin().getDatabaseInstance(), manager.getClaimRepository());
            Logger.print("Saved " + manager.getClaimRepository().size() + " Claims");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            ClaimDAO.saveClaims(manager.getPlugin().getDatabaseInstance(), manager.getClaimRepository());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getClaimRepository().size() + " Claims")).run();
        }).run();
    }

    /**
     * Handles entering the reinforcement builder
     * @param player Player
     * @param networkName Network Name
     * @param promise Promise
     */
    public void startReinforcements(Player player, String networkName, SimplePromise promise) {
        final ClaimSession existing = manager.getSessionByPlayer(player);
        final Network network = getManager().getPlugin().getNetworkManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (existing != null) {
            final Network oldNetwork = getManager().getPlugin().getNetworkManager().getNetworkByID(existing.getNetworkId());

            if (oldNetwork != null) {
                player.sendMessage(ChatColor.YELLOW + "Finished claiming for " + oldNetwork.getName());
                Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") finished session for " + oldNetwork.getName() + "(" + oldNetwork.getUniqueId().toString() + ")");
            }

            manager.getActiveClaimSessions().remove(existing);
        }

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember == null && !admin) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (!admin && !(networkMember.hasPermission(NetworkPermission.ADMIN) || networkMember.hasPermission(NetworkPermission.MODIFY_CLAIMS))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final ItemStack hand = player.getInventory().getItemInHand();
        ClaimType claimType = null;

        if (hand == null) {
            promise.fail("You are not holding a claim material");
            return;
        }

        for (ClaimType type : ClaimType.values()) {
            if (type.getMaterial().equals(hand.getType())) {
                claimType = type;
                break;
            }
        }

        if (claimType == null) {
            promise.fail("You are not holding a claim material");
            return;
        }

        final ClaimSession session = new ClaimSession(player.getUniqueId(), network.getUniqueId(), ClaimSessionType.REINFORCE, claimType);
        manager.getActiveClaimSessions().add(session);
        Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") started a reinforcement session using " + claimType.getDisplayName() + " reinforcement for " + network.getName() + "(" + network.getUniqueId() + ")");

        promise.success();
    }

    /**
     * Handles starting the fortification process
     * @param player Player
     * @param networkName Network Name
     * @param promise Promise
     */
    public void startFortifications(Player player, String networkName, SimplePromise promise) {
        final ClaimSession existing = manager.getSessionByPlayer(player);
        final Network network = getManager().getPlugin().getNetworkManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (existing != null) {
            final Network oldNetwork = getManager().getPlugin().getNetworkManager().getNetworkByID(existing.getNetworkId());

            if (oldNetwork != null) {
                player.sendMessage(ChatColor.YELLOW + "Finished claiming for " + oldNetwork.getName());
                Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") finished session for " + oldNetwork.getName() + "(" + oldNetwork.getUniqueId().toString() + ")");
            }

            manager.getActiveClaimSessions().remove(existing);
        }

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember networkMember = network.getMember(player);

        if (networkMember == null && !admin) {
            promise.fail("You are not a member of this network");
            return;
        }

        if (!admin && !(networkMember.hasPermission(NetworkPermission.ADMIN) || networkMember.hasPermission(NetworkPermission.MODIFY_CLAIMS))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final ItemStack hand = player.getInventory().getItemInHand();
        ClaimType claimType = null;

        if (hand == null) {
            promise.fail("You are not holding a claim material");
            return;
        }

        for (ClaimType type : ClaimType.values()) {
            if (type.getMaterial().equals(hand.getType())) {
                claimType = type;
                break;
            }
        }

        if (claimType == null) {
            promise.fail("You are not holding a claim material");
            return;
        }

        final ClaimSession session = new ClaimSession(player.getUniqueId(), network.getUniqueId(), ClaimSessionType.FORTIFY, claimType);
        manager.getActiveClaimSessions().add(session);
        Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") started a fortification session using " + claimType.getDisplayName() + " reinforcement for " + network.getName() + "(" + network.getUniqueId() + ")");

        promise.success();
    }

    /**
     * Handles starting the information session
     * @param player Player
     * @param promise Promise
     */
    public void startInformation(Player player, SimplePromise promise) {
        final ClaimSession existing = manager.getSessionByPlayer(player);

        if (existing != null) {
            final Network oldNetwork = getManager().getPlugin().getNetworkManager().getNetworkByID(existing.getNetworkId());

            if (oldNetwork != null) {
                player.sendMessage(ChatColor.YELLOW + "Finished claiming for " + oldNetwork.getName());
                Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") finished session for " + oldNetwork.getName() + "(" + oldNetwork.getUniqueId().toString() + ")");
            }

            manager.getActiveClaimSessions().remove(existing);
        }

        final ClaimSession session = new ClaimSession(player.getUniqueId(), null, ClaimSessionType.INFO, null);
        manager.getActiveClaimSessions().add(session);

        promise.success();
    }

    /**
     * Handles removing a player from any Claim Session they may be in
     * @param player Player
     * @param promise Promise
     */
    public void disableSession(Player player, SimplePromise promise) {
        final ClaimSession session = manager.getSessionByPlayer(player);

        if (session == null) {
            promise.fail("You are not claiming anything");
            return;
        }

        final Network oldNetwork = getManager().getPlugin().getNetworkManager().getNetworkByID(session.getNetworkId());

        if (oldNetwork != null) {
            player.sendMessage(ChatColor.YELLOW + "Finished claiming for " + oldNetwork.getName());
            Logger.print(player.getName() + "(" + player.getUniqueId().toString() + ") finished session for " + oldNetwork.getName() + "(" + oldNetwork.getUniqueId().toString() + ")");
        }

        manager.getActiveClaimSessions().remove(session);
        promise.success();
    }
}

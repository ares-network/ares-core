package com.llewkcor.ares.core.network;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.acid.data.AcidBlock;
import com.llewkcor.ares.core.acid.data.AcidDAO;
import com.llewkcor.ares.core.bastion.data.Bastion;
import com.llewkcor.ares.core.bastion.data.BastionDAO;
import com.llewkcor.ares.core.claim.data.Claim;
import com.llewkcor.ares.core.claim.data.ClaimDAO;
import com.llewkcor.ares.core.factory.data.Factory;
import com.llewkcor.ares.core.factory.data.FactoryDAO;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkDAO;
import com.llewkcor.ares.core.network.handlers.*;
import com.llewkcor.ares.core.snitch.data.Snitch;
import com.llewkcor.ares.core.snitch.data.SnitchDAO;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class NetworkHandler {
    @Getter public final NetworkManager manager;

    @Getter public final NetworkCreateHandler createHandler;
    @Getter public final NetworkInviteHandler inviteHandler;
    @Getter public final NetworkManageHandler manageHandler;
    @Getter public final NetworkDisplayHandler displayHandler;
    @Getter public final NetworkMenuHandler menuHandler;
    @Getter public final NetworkChatHandler chatHandler;

    public NetworkHandler(NetworkManager manager) {
        this.manager = manager;
        this.createHandler = new NetworkCreateHandler(this);
        this.inviteHandler = new NetworkInviteHandler(this);
        this.manageHandler = new NetworkManageHandler(this);
        this.displayHandler = new NetworkDisplayHandler(this);
        this.menuHandler = new NetworkMenuHandler(this);
        this.chatHandler = new NetworkChatHandler(this);
    }

    /**
     * Loads all networks to memory from the database instance
     * @param blocking Block the thread
     */
    public void loadAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to load all networks from the database");
            manager.getNetworkRepository().addAll(NetworkDAO.getNetworks(manager.getPlugin().getDatabaseInstance()));
            Logger.print("Loaded " + manager.getNetworkRepository().size() + " Networks");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.getNetworkRepository().addAll(NetworkDAO.getNetworks(manager.getPlugin().getDatabaseInstance()));
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Loaded " + manager.getNetworkRepository().size() + " Networks")).run();
        }).run();
    }

    /**
     * Saves all networks in memory to the database instance
     * @param blocking Block the thread
     */
    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to save all networks to the database");
            NetworkDAO.saveNetworks(manager.getPlugin().getDatabaseInstance(), manager.getNetworkRepository());
            Logger.print("Saved " + manager.getNetworkRepository().size() + " Networks");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            NetworkDAO.saveNetworks(manager.getPlugin().getDatabaseInstance(), manager.getNetworkRepository());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getNetworkRepository().size() + " Networks")).run();
        }).run();
    }

    /**
     * Performs a scrub of the database to remove any network that is now considered inactive
     */
    public void performNetworkCleanup() {
        new Scheduler(manager.getPlugin()).async(() -> {
            final List<Network> expired = manager.getNetworkRepository().stream().filter(network -> (network.getLastSeen() - Time.now()) > (manager.getPlugin().getConfigManager().getGeneralConfig().getNetworkInactiveExpireSeconds() * 1000L)).collect(Collectors.toList());

            if (expired.isEmpty()) {
                return;
            }

            expired.forEach(network -> {
                final List<Snitch> snitches = manager.getPlugin().getSnitchManager().getSnitchByOwner(network);
                final List<Claim> claims = manager.getPlugin().getClaimManager().getClaimByOwner(network);
                final Set<Factory> factories = manager.getPlugin().getFactoryManager().getFactoryByOwner(network);
                final Set<Bastion> bastions = manager.getPlugin().getBastionManager().getBastionByOwner(network);
                final Set<AcidBlock> acids = manager.getPlugin().getAcidManager().getAcidBlockByOwner(network);

                manager.getPlugin().getSnitchManager().getSnitchRepository().removeAll(snitches);
                manager.getPlugin().getClaimManager().getClaimRepository().removeAll(claims);
                manager.getPlugin().getFactoryManager().getFactoryRepository().removeAll(factories);
                manager.getPlugin().getBastionManager().getBastionRepository().removeAll(bastions);
                manager.getPlugin().getAcidManager().getAcidRepository().removeAll(acids);

                network.getMembers().clear();
                network.getPendingMembers().clear();
                manager.getNetworkRepository().remove(network);

                NetworkDAO.deleteNetwork(manager.getPlugin().getDatabaseInstance(), network);

                for (Snitch snitch : snitches) {
                    SnitchDAO.deleteSnitch(manager.getPlugin().getDatabaseInstance(), snitch);
                }

                for (Claim claim : claims) {
                    ClaimDAO.deleteClaim(manager.getPlugin().getDatabaseInstance(), claim);
                }

                for (Factory factory : factories) {
                    FactoryDAO.deleteFactory(manager.getPlugin().getDatabaseInstance(), factory);
                }

                for (Bastion bastion : bastions) {
                    BastionDAO.deleteBastion(manager.getPlugin().getDatabaseInstance(), bastion);
                }

                for (AcidBlock acid : acids) {
                    AcidDAO.deleteAcidBlock(manager.getPlugin().getDatabaseInstance(), acid);
                }

                Logger.warn(network.getName() + "(" + network.getUniqueId().toString() + ") has been deleted due to inactivity");
            });
        }).run();
    }
}
package com.llewkcor.ares.core.network;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.network.data.NetworkDAO;
import com.llewkcor.ares.core.network.handlers.NetworkCreateHandler;
import com.llewkcor.ares.core.network.handlers.NetworkDisplayHandler;
import com.llewkcor.ares.core.network.handlers.NetworkInviteHandler;
import com.llewkcor.ares.core.network.handlers.NetworkManageHandler;
import lombok.Getter;

public final class NetworkHandler {
    @Getter public final NetworkManager manager;

    @Getter public final NetworkCreateHandler createHandler;
    @Getter public final NetworkInviteHandler inviteHandler;
    @Getter public final NetworkManageHandler manageHandler;
    @Getter public final NetworkDisplayHandler displayHandler;

    public NetworkHandler(NetworkManager manager) {
        this.manager = manager;
        this.createHandler = new NetworkCreateHandler(this);
        this.inviteHandler = new NetworkInviteHandler(this);
        this.manageHandler = new NetworkManageHandler(this);
        this.displayHandler = new NetworkDisplayHandler(this);
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
            Logger.warn("Blocking the thread while attempting to save all networks from the database");
            NetworkDAO.saveNetworks(manager.getPlugin().getDatabaseInstance(), manager.getNetworkRepository());
            Logger.print("Saved " + manager.getNetworkRepository().size() + " Networks");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            NetworkDAO.saveNetworks(manager.getPlugin().getDatabaseInstance(), manager.getNetworkRepository());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getNetworkRepository().size() + " Networks")).run();
        }).run();
    }
}

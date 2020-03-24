package com.llewkcor.ares.core;

import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.llewkcor.ares.core.bridge.BridgeManager;
import com.llewkcor.ares.core.listener.AresEventListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Ares extends JavaPlugin {
    @Getter protected MongoDB databaseInstance;
    @Getter protected BridgeManager bridgeManager;

    @Override
    public void onEnable() {
        this.bridgeManager = new BridgeManager(this);
        this.databaseInstance = new MongoDB("mongodb://localhost");

        databaseInstance.openConnection();

        // Listeners
        Bukkit.getPluginManager().registerEvents(new AresEventListener(this), this);
    }

    @Override
    public void onDisable() {
        databaseInstance.closeConnection();
    }
}
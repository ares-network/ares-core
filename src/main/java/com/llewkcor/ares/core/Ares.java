package com.llewkcor.ares.core;

import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.llewkcor.ares.core.bridge.BridgeManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Ares extends JavaPlugin {
    @Getter protected MongoDB databaseInstance;
    @Getter protected BridgeManager bridgeManager;

    @Override
    public void onLoad() {
        this.bridgeManager = new BridgeManager(this);
    }

    @Override
    public void onEnable() {
        this.databaseInstance = new MongoDB(""); // TODO: Add URI
    }

    @Override
    public void onDisable() {

    }
}
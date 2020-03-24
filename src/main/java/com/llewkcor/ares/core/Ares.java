package com.llewkcor.ares.core;

import com.llewkcor.ares.commons.connect.mongodb.MongoDB;
import com.llewkcor.ares.core.bridge.BridgeManager;
import com.llewkcor.ares.core.configs.ConfigManager;
import com.llewkcor.ares.core.listener.AresEventListener;
import com.llewkcor.ares.core.network.NetworkManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Ares extends JavaPlugin {
    @Getter public ConfigManager configManager;
    @Getter public NetworkManager networkManager;

    @Getter protected MongoDB databaseInstance;
    @Getter protected BridgeManager bridgeManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.networkManager = new NetworkManager(this);
        this.bridgeManager = new BridgeManager(this);

        configManager.load();

        this.databaseInstance = new MongoDB(configManager.getGeneralConfig().getDatabaseUri());
        databaseInstance.openConnection();

        // Listeners
        Bukkit.getPluginManager().registerEvents(new AresEventListener(this), this);
    }

    @Override
    public void onDisable() {
        databaseInstance.closeConnection();
    }
}
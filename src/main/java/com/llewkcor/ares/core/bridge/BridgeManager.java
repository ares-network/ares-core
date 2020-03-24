package com.llewkcor.ares.core.bridge;

import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.bridge.data.DataManager;
import lombok.Getter;

public final class BridgeManager {
    @Getter public final Ares plugin;
    @Getter public final DataManager dataManager;

    public BridgeManager(Ares plugin) {
        this.plugin = plugin;
        this.dataManager = new DataManager(this);
    }

    public void onLoad() {

    }

    public void onEnable() {

    }

    public void onDisable() {

    }
}

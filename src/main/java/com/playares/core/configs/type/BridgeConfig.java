package com.playares.core.configs.type;

import com.playares.commons.logger.Logger;
import com.playares.core.configs.AresConfig;
import com.playares.core.configs.ConfigManager;
import lombok.Getter;

public final class BridgeConfig implements AresConfig {
    @Getter public final ConfigManager configManager;

    public BridgeConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        Logger.print("Bridge configuration loaded");
    }

    @Override
    public void reload() {
        Logger.print("Bridge configuration reloaded");
    }
}

package com.llewkcor.ares.core.configs.type;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;

public final class PrisonPearlConfig implements AresConfig {
    @Getter public final ConfigManager configManager;

    public PrisonPearlConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        Logger.print("Prison Pearl configuration loaded");
    }

    @Override
    public void reload() {
        Logger.print("Prison Pearl configuration reloaded");
    }
}

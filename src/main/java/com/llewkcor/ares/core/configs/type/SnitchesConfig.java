package com.llewkcor.ares.core.configs.type;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;

public final class SnitchesConfig implements AresConfig {
    @Getter public final ConfigManager configManager;

    public SnitchesConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        Logger.print("Snitches configuration loaded");
    }

    @Override
    public void reload() {
        Logger.print("Snitches configuration reloaded");
    }
}

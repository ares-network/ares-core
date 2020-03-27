package com.llewkcor.ares.core.configs.type;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class SnitchesConfig implements AresConfig {
    @Getter public final ConfigManager configManager;
    @Getter public YamlConfiguration config;

    @Getter public int searchRadius;
    @Getter public int movementCheckInterval;
    @Getter public int logEntryExpireSeconds;
    @Getter public int expireTime;

    public SnitchesConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        config = Configs.getConfig(configManager.getPlugin(), "snitches");

        searchRadius = config.getInt("settings.cuboid_size");
        movementCheckInterval = config.getInt("settings.alert_check_interval");
        logEntryExpireSeconds = config.getInt("settings.log_entry_expire_seconds");
        expireTime = config.getInt("settings.mature");

        Logger.print("Snitches configuration loaded");
    }

    @Override
    public void reload() {
        load();

        Logger.print("Snitches configuration reloaded");
    }
}

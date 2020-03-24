package com.llewkcor.ares.core.configs.type;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class GeneralConfig implements AresConfig {
    @Getter public final ConfigManager configManager;
    @Getter public YamlConfiguration config;

    @Getter public String databaseUri;

    public GeneralConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        config = Configs.getConfig(configManager.getPlugin(), "general");

        databaseUri = config.getString("database");

        Logger.print("General configuration loaded");
    }

    @Override
    public void reload() {
        load();

        Logger.print("General configuration reloaded");
    }
}
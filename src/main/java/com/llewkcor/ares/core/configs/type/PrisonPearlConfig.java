package com.llewkcor.ares.core.configs.type;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PrisonPearlConfig implements AresConfig {
    @Getter public final ConfigManager configManager;
    @Getter public YamlConfiguration config;

    @Getter public String banWorldName;
    @Getter public int banDuration;

    public PrisonPearlConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        config = Configs.getConfig(configManager.getPlugin(), "prison-pearl");

        banWorldName = config.getString("settings.ban_world");
        banDuration = config.getInt("settings.ban_duration");

        Logger.print("Prison Pearl configuration loaded");
    }

    @Override
    public void reload() {
        load();

        Logger.print("Prison Pearl configuration reloaded");
    }
}

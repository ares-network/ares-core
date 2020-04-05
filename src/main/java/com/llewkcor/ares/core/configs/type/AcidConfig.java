package com.llewkcor.ares.core.configs.type;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class AcidConfig implements AresConfig {
    @Getter public final ConfigManager configManager;
    @Getter public YamlConfiguration config;

    @Getter public double acidRadius;
    @Getter public int acidTickInterval;
    @Getter public int acidMatureTime;
    @Getter public int acidExpireTime;

    public AcidConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        config = Configs.getConfig(configManager.getPlugin(), "acid");

        this.acidRadius = config.getDouble("settings.damage_radius");
        this.acidTickInterval = config.getInt("settings.damage_tick_interval");
        this.acidMatureTime = config.getInt("settings.mature_time");
        this.acidExpireTime = config.getInt("settings.expire_time");

        Logger.print("Acid configuration loaded");
    }

    @Override
    public void reload() {
        load();

        Logger.print("Acid configuration reloaded");
    }
}

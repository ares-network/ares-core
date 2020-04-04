package com.llewkcor.ares.core.configs.type;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class BastionsConfig implements AresConfig {
    @Getter public final ConfigManager configManager;
    @Getter public YamlConfiguration config;

    @Getter public double bastionRadius;
    @Getter public int bastionMatureTime;

    public BastionsConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        config = Configs.getConfig(configManager.getPlugin(), "bastions");

        this.bastionRadius = config.getDouble("settings.bastion-radius");
        this.bastionMatureTime = config.getInt("settings.bastion-mature");

        Logger.print("Snitches configuration loaded");
    }

    @Override
    public void reload() {
        load();

        Logger.print("Snitches configuration reloaded");
    }
}

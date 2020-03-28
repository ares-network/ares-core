package com.llewkcor.ares.core.configs.type;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public final class ClaimsConfig implements AresConfig {
    @Getter public final ConfigManager configManager;
    @Getter public YamlConfiguration config;

    @Getter public List<Material> nonReinforceables;

    public ClaimsConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @SuppressWarnings("unchecked") @Override
    public void load() {
        config = Configs.getConfig(configManager.getPlugin(), "claims");

        nonReinforceables = Lists.newArrayList();

        for (String materialName : (List<String>)config.getList("settings.non-reinforceables")) {
            try {
                final Material material = Material.valueOf(materialName);
                nonReinforceables.add(material);
            } catch (IllegalArgumentException ignored) {}
        }

        Logger.print("Claims configuration loaded");
    }

    @Override
    public void reload() {
        load();

        Logger.print("Claims configuration reloaded");
    }
}

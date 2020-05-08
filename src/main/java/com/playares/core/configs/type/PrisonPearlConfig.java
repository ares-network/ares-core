package com.playares.core.configs.type;

import com.google.common.collect.Maps;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.general.Configs;
import com.playares.core.configs.AresConfig;
import com.playares.core.configs.ConfigManager;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

public final class PrisonPearlConfig implements AresConfig {
    @Getter public final ConfigManager configManager;
    @Getter public YamlConfiguration config;

    @Getter public boolean enabled;
    @Getter public String banWorldName;
    @Getter public int banDuration;
    @Getter public int maxPrisonPearledAccounts;
    @Getter public String altBanDuration;
    @Getter public Map<String, Integer> premiumBanDurations;

    public PrisonPearlConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void load() {
        config = Configs.getConfig(configManager.getPlugin(), "prison-pearl");

        enabled = config.getBoolean("settings.enabled");
        banWorldName = config.getString("settings.ban_world");
        maxPrisonPearledAccounts = config.getInt("settings.max_pearl_accounts");
        altBanDuration = config.getString("settings.alt_ban_duration");

        banDuration = config.getInt("settings.ban_duration.default");
        premiumBanDurations = Maps.newHashMap();

        for (String rankId : config.getConfigurationSection("settings.ban_duration.premium").getKeys(false)) {
            final String permission = config.getString("settings.ban_duration.premium." + rankId + ".permission");
            final int duration = config.getInt("settings.ban_duration.premium." + rankId + ".duration");
            premiumBanDurations.put(permission, duration);
        }

        Logger.print("Prison Pearl configuration loaded");
    }

    @Override
    public void reload() {
        load();

        Logger.print("Prison Pearl configuration reloaded");
    }
}

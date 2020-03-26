package com.llewkcor.ares.core.configs.type;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.configs.AresConfig;
import com.llewkcor.ares.core.configs.ConfigManager;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public final class GeneralConfig implements AresConfig {
    @Getter public final ConfigManager configManager;
    @Getter public YamlConfiguration config;

    @Getter public String databaseUri;

    @Getter public int minNetworkNameLength;
    @Getter public int maxNetworkNameLength;
    @Getter public List<String> bannedNetworkNames;
    @Getter public int minPasswordLength;
    @Getter public int maxPasswordLength;
    @Getter public int maxNetworkMembers;
    @Getter public int maxJoinedNetworks;
    @Getter public int networkCreateCooldown;
    @Getter public int networkRenameCooldown;

    public GeneralConfig(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @SuppressWarnings("unchecked") @Override
    public void load() {
        config = Configs.getConfig(configManager.getPlugin(), "general");

        databaseUri = config.getString("database");

        minNetworkNameLength = config.getInt("network-settings.name.min-length");
        maxNetworkNameLength = config.getInt("network-settings.name.max-length");
        bannedNetworkNames = (List<String>)config.getList("network-settings.name.banned-names");
        minPasswordLength = config.getInt("network-settings.password.min-length");
        maxPasswordLength = config.getInt("network-settings.password.max-length");
        maxNetworkMembers = config.getInt("network-settings.max-members");
        maxJoinedNetworks = config.getInt("network-settings.max-joined-networks");
        networkCreateCooldown = config.getInt("network-settings.cooldowns.create");
        networkRenameCooldown = config.getInt("network-settings.cooldowns.rename");

        Logger.print("General configuration loaded");
    }

    @Override
    public void reload() {
        load();

        Logger.print("General configuration reloaded");
    }
}
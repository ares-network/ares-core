package com.llewkcor.ares.core.configs;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.configs.type.*;
import lombok.Getter;

public final class ConfigManager {
    @Getter public final Ares plugin;
    @Getter public GeneralConfig generalConfig;
    @Getter public BridgeConfig bridgeConfig;
    @Getter public ClaimsConfig claimsConfig;
    @Getter public PrisonPearlConfig prisonPearlConfig;
    @Getter public SnitchesConfig snitchesConfig;
    @Getter public BastionsConfig bastionsConfig;
    @Getter public AcidConfig acidConfig;

    public ConfigManager(Ares plugin) {
        this.plugin = plugin;
        this.generalConfig = new GeneralConfig(this);
        this.bridgeConfig = new BridgeConfig(this);
        this.claimsConfig = new ClaimsConfig(this);
        this.prisonPearlConfig = new PrisonPearlConfig(this);
        this.snitchesConfig = new SnitchesConfig(this);
        this.bastionsConfig = new BastionsConfig(this);
        this.acidConfig = new AcidConfig(this);
    }

    /**
     * Loads all configuration files
     */
    public void load() {
        generalConfig.load();
        bridgeConfig.load();
        claimsConfig.load();
        prisonPearlConfig.load();
        snitchesConfig.load();
        bastionsConfig.load();
        acidConfig.load();

        Logger.print("Finished loading config files");
    }

    /**
     * Reloads all configuration files
     */
    public void reload() {
        generalConfig.reload();
        bridgeConfig.reload();
        claimsConfig.reload();
        prisonPearlConfig.reload();
        snitchesConfig.reload();
        bastionsConfig.reload();
        acidConfig.reload();

        Logger.print("Finished reloading config files");
    }
}

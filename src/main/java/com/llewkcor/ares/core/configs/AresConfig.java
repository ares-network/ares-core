package com.llewkcor.ares.core.configs;

public interface AresConfig {
    /**
     * Call the load of this config
     */
    void load();

    /**
     * Reload the values of this config
     */
    void reload();
}
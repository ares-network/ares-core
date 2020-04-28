package com.playares.core.alts;

import com.playares.commons.logger.Logger;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.core.alts.data.AltDAO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class AltHandler {
    @Getter public final AltManager manager;

    /**
     * Handles performing an expired alt account cleanup
     */
    public void performExpiredAltCleanup() {
        Logger.warn("Preparing to clean up expired alt account entries...");
        new Scheduler(manager.getPlugin()).async(() -> {
            AltDAO.cleanupExpiredAlts(manager.getPlugin().getDatabaseInstance(), manager.getPlugin().getConfigManager().getGeneralConfig().getMaxAltLifespan());

            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Expired alt account entry cleanup completed")).run();
        }).run();
    }
}

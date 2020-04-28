package com.playares.core.alts;

import com.google.common.collect.ImmutableCollection;
import com.playares.commons.promise.Promise;
import com.playares.commons.promise.SimplePromise;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.core.Ares;
import com.playares.core.alts.data.AltDAO;
import com.playares.core.alts.data.AltEntry;
import com.playares.core.alts.listener.LoginListener;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.UUID;

public final class AltManager {
    @Getter public final Ares plugin;
    @Getter public final AltHandler handler;

    public AltManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new AltHandler(this);

        Bukkit.getPluginManager().registerEvents(new LoginListener(plugin), plugin);
    }

    /**
     * Returns all matching Alt Entries matching the provided UUID or IP Address in the form of a promise
     * @param uniqueId Bukkit UUID
     * @param address Converted Address
     * @param promise Promise
     */
    public void getAlts(UUID uniqueId, long address, Promise<ImmutableCollection<AltEntry>> promise) {
        new Scheduler(plugin).async(() -> {
            final ImmutableCollection<AltEntry> result = AltDAO.getAlts(plugin.getDatabaseInstance(), uniqueId, address);

            new Scheduler(plugin).sync(() -> promise.ready(result)).run();
        }).run();
    }

    /**
     * Blocks the current thread and returns all matching Alt Entries matching the provided
     * UUID or IP Address
     * @param uniqueId Bukkit UUID
     * @param address Converted Address
     * @return Immutable Collection of Alt Entry
     */
    public ImmutableCollection<AltEntry> getAlts(UUID uniqueId, long address) {
        return AltDAO.getAlts(plugin.getDatabaseInstance(), uniqueId, address);
    }

    /**
     * Saves the provided Alt Entry to the database
     * @param entry Alt Entry
     */
    public void saveAlt(AltEntry entry) {
        new Scheduler(plugin).async(() -> AltDAO.saveAlt(plugin.getDatabaseInstance(), entry)).run();
    }

    /**
     * Saves the provided Alt Entry to the database
     * @param entry Alt Entry
     * @param promise Promise
     */
    public void saveAlt(AltEntry entry, SimplePromise promise) {
        new Scheduler(plugin).async(() -> {
            AltDAO.saveAlt(plugin.getDatabaseInstance(), entry);

            new Scheduler(plugin).sync(promise::success).run();
        }).run();
    }

    /**
     * Deletes the provided Alt Entry from the database
     * @param entry Alt Entry
     */
    public void deleteAlt(AltEntry entry) {
        new Scheduler(plugin).async(() -> AltDAO.deleteAlt(plugin.getDatabaseInstance(), entry)).run();
    }
}

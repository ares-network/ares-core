package com.llewkcor.ares.core.factory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.factory.data.Factory;
import com.llewkcor.ares.core.factory.data.FactoryJob;
import com.llewkcor.ares.core.factory.data.FactoryRecipe;
import com.llewkcor.ares.core.factory.listener.FactoryListener;
import com.llewkcor.ares.core.factory.menu.FactoryMenuHandler;
import com.llewkcor.ares.core.network.data.Network;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class FactoryManager {
    @Getter public final Ares plugin;
    @Getter public final FactoryHandler handler;
    @Getter public final FactoryRecipeManager recipeManager;
    @Getter public final FactoryMenuHandler menuHandler;
    @Getter public final Set<Factory> factoryRepository;
    @Getter public final BukkitTask jobUpdateTask;

    public FactoryManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new FactoryHandler(this);
        this.recipeManager = new FactoryRecipeManager(this);
        this.menuHandler = new FactoryMenuHandler(this);
        this.factoryRepository = Sets.newConcurrentHashSet();

        this.jobUpdateTask = new Scheduler(plugin).async(() -> {
            for (Factory factory : factoryRepository) {
                for (FactoryJob job : factory.getActiveJobs()) {
                    if (!job.isReady()) {
                        continue;
                    }

                    final FactoryRecipe recipe = recipeManager.getRecipe(job.getRecipeName());

                    if (recipe == null) {
                        factory.getActiveJobs().remove(job);
                        Logger.error("Failed to obtain the recipe for Factory Job " + job.getRecipeName());
                        continue;
                    }

                    new Scheduler(plugin).sync(() -> {
                        factory.finishJob(recipe);
                        factory.getActiveJobs().remove(job);
                    }).run();
                }
            }
        }).repeat(20L, 20L).run();

        Bukkit.getPluginManager().registerEvents(new FactoryListener(this), plugin);
    }

    /**
     * Returns a Factory matching the provided Factory UUID
     * @param uniqueId Factory UUID
     * @return Factory
     */
    public Factory getFactoryByID(UUID uniqueId) {
        return factoryRepository.stream().filter(factory -> factory.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns an Immutable Set of Factories for the provided Network
     * @param network Network
     * @return Immutable Set of Factories
     */
    public ImmutableSet<Factory> getFactoryByOwner(Network network) {
        return ImmutableSet.copyOf(factoryRepository.stream().filter(factory -> factory.getOwnerId().equals(network.getUniqueId())).collect(Collectors.toSet()));
    }

    /**
     * Returns an Immutable Set of Factories for the provided Network ID
     * @param uniqueId Network UUID
     * @return Immutable Set of Factories
     */
    public ImmutableSet<Factory> getFactoryByOwner(UUID uniqueId) {
        return ImmutableSet.copyOf(factoryRepository.stream().filter(factory -> factory.getOwnerId().equals(uniqueId)).collect(Collectors.toSet()));
    }

    /**
     * Returns a Factory instance matching the provided BLocatable
     * @param block BLocatable
     * @return Factory
     */
    public Factory getFactoryByBlock(BLocatable block) {
        return factoryRepository.stream().filter(factory -> factory.match(block)).findFirst().orElse(null);
    }
}

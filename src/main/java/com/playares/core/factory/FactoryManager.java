package com.playares.core.factory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.location.BLocatable;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.bukkit.Scheduler;
import com.playares.commons.util.general.Configs;
import com.playares.core.Ares;
import com.playares.core.factory.data.Factory;
import com.playares.core.factory.data.FactoryJob;
import com.playares.core.factory.data.FactoryRecipe;
import com.playares.core.factory.listener.FactoryListener;
import com.playares.core.factory.menu.FactoryMenuHandler;
import com.playares.core.network.data.Network;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class FactoryManager {
    @Getter public final Ares plugin;
    @Getter public final FactoryHandler handler;
    @Getter public final FactoryRecipeManager recipeManager;
    @Getter public final FactoryMenuHandler menuHandler;
    @Getter public final Set<Factory> factoryRepository;
    @Getter public final Map<String, Double> premiumSpeedMultipliers;
    @Getter public final double expLevelBase;
    @Getter public final double expLevelMultiplier;
    @Getter public final BukkitTask jobUpdateTask;

    public FactoryManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new FactoryHandler(this);
        this.recipeManager = new FactoryRecipeManager(this);
        this.menuHandler = new FactoryMenuHandler(this);
        this.factoryRepository = Sets.newConcurrentHashSet();
        this.premiumSpeedMultipliers = Maps.newHashMap();

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
                        final double levelUp = getLevelRequirement(factory.getLevel() + 1);

                        factory.finishJob(recipe);
                        factory.getActiveJobs().remove(job);

                        if (factory.getExperience() >= levelUp) {
                            factory.setLevel(factory.getLevel() + 1);
                            factory.getFurnaceLocation().getBukkit().getWorld().playSound(factory.getFurnaceLocation().getBukkit().getLocation(), Sound.LEVEL_UP, 0.5F, 0.5F);
                        }
                    }).run();
                }
            }
        }).repeat(20L, 20L).run();

        Bukkit.getPluginManager().registerEvents(new FactoryListener(this), plugin);

        final YamlConfiguration config = Configs.getConfig(plugin, "factories");

        this.expLevelBase = config.getDouble("settings.exp_level_base");
        this.expLevelMultiplier = config.getDouble("settings.exp_level_multiplier");

        for (String rankId : config.getConfigurationSection("premium_modifiers").getKeys(false)) {
            final String permission = config.getString("premium_modifiers." + rankId + ".permission");
            final double multiplier = config.getDouble("premium_modifiers." + rankId + ".speed_multiplier");

            premiumSpeedMultipliers.put(permission, multiplier);
        }

        Logger.print("Loaded " + premiumSpeedMultipliers.size() + " Factory speed multipliers");
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

    /**
     * Returns the highest speed multiplier available to the provided Player
     * @param player Player
     * @return Speed multiplier
     */
    public double getSpeedMultiplier(Player player) {
        double highest = 1.0;

        for (String permission : premiumSpeedMultipliers.keySet().stream().filter(player::hasPermission).collect(Collectors.toList())) {
            final double multiplier = premiumSpeedMultipliers.getOrDefault(permission, 1.0);

            if (highest < multiplier) {
                highest = multiplier;
            }
        }

        return highest;
    }

    /**
     * Returns the experience required to reach the provided level
     * @param level Experience Level
     * @return Experience required
     */
    public double getLevelRequirement(int level) {
        return (expLevelMultiplier * expLevelBase * level) * level;
    }

    /**
     * Returns the required EXP to reach the next level
     * @param factory Factory
     * @return EXP to next level
     */
    public double getExpToNextLevel(Factory factory) {
        final double requiredExp = getLevelRequirement(factory.getLevel() + 1);
        return Math.floor((requiredExp - factory.getExperience()));
    }
}

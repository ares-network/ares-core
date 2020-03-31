package com.llewkcor.ares.core.factory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.factory.data.FactoryRecipe;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class FactoryRecipeManager {
    @Getter public final FactoryManager factoryManager;
    @Getter public final Set<FactoryRecipe> recipeRepository;

    public FactoryRecipeManager(FactoryManager factoryManager) {
        this.factoryManager = factoryManager;
        this.recipeRepository = Sets.newConcurrentHashSet();

        final YamlConfiguration factoryConfig = Configs.getConfig(factoryManager.getPlugin(), "factories");

        for (String recipeIdentifier : factoryConfig.getConfigurationSection("recipes").getKeys(false)) {
            final String recipeName = factoryConfig.getString("recipes." + recipeIdentifier + ".name");
            final int jobTime = factoryConfig.getInt("recipes." + recipeIdentifier + ".job_time");
            final Map<Material, Integer> inputMaterials = Maps.newHashMap();
            final Map<Material, Integer> outputMaterials = Maps.newHashMap();

            for (String materialName : factoryConfig.getConfigurationSection("recipes." + recipeIdentifier + ".input").getKeys(false)) {
                final Material material = Material.getMaterial(materialName);
                final int amount = factoryConfig.getInt("recipes." + recipeIdentifier + ".input." + materialName + ".amount");

                if (material == null) {
                    Logger.error("Failed to load input material for Factory Recipe (" + recipeName + ")");
                    continue;
                }

                inputMaterials.put(material, amount);
            }

            for (String materialName : factoryConfig.getConfigurationSection("recipes." + recipeIdentifier + ".output").getKeys(false)) {
                final Material material = Material.getMaterial(materialName);
                final int amount = factoryConfig.getInt("recipes." + recipeIdentifier + ".output." + materialName + ".amount");

                if (material == null) {
                    Logger.error("Failed to load output material for Factory Recipe (" + recipeName + ")");
                    continue;
                }

                outputMaterials.put(material, amount);
            }

            final FactoryRecipe recipe = new FactoryRecipe(recipeName, jobTime, inputMaterials, outputMaterials);
            recipeRepository.add(recipe);
        }

        Logger.print("Loaded " + recipeRepository.size() + " Factory Recipes");
    }

    /**
     * Returns a Factory Recipe matching the provided recipe name
     * @param name Recipe name
     * @return
     */
    public FactoryRecipe getRecipe(String name) {
        return recipeRepository.stream().filter(recipe -> recipe.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Returns an Immutable Set containing all Factory Recipes the provided Bukkit Player
     * has the resources to start
     * @param player Bukkit Player
     * @return Immutable Set of FactoryRecipe
     */
    public ImmutableSet<FactoryRecipe> getRecipes(Player player) {
        return ImmutableSet.copyOf(recipeRepository.stream().filter(recipe -> recipe.hasRequiredMaterials(player)).collect(Collectors.toSet()));
    }
}

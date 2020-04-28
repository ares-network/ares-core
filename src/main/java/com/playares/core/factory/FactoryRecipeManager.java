package com.playares.core.factory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.logger.Logger;
import com.playares.commons.remap.RemappedEnchantment;
import com.playares.commons.util.general.Configs;
import com.playares.core.factory.data.FactoryRecipe;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class FactoryRecipeManager {
    @Getter public final FactoryManager factoryManager;
    @Getter public final Set<FactoryRecipe> recipeRepository;

    public FactoryRecipeManager(FactoryManager factoryManager) {
        this.factoryManager = factoryManager;
        this.recipeRepository = Sets.newConcurrentHashSet();
    }

    /**
     * Handles loading the Factory recipes from file to memory
     */
    public void loadRecipes() {
        final YamlConfiguration factoryConfig = Configs.getConfig(factoryManager.getPlugin(), "factories");

        if (!recipeRepository.isEmpty()) {
            recipeRepository.clear();
        }

        for (String recipeIdentifier : factoryConfig.getConfigurationSection("recipes").getKeys(false)) {
            final String recipeName = factoryConfig.getString("recipes." + recipeIdentifier + ".name");
            final int jobTime = factoryConfig.getInt("recipes." + recipeIdentifier + ".job_time");
            final int requiredLevel = factoryConfig.getInt("recipes." + recipeIdentifier + ".required_level");
            final double expPerRun = factoryConfig.getDouble("recipes." + recipeIdentifier + ".exp_per_run");
            final List<ItemStack> inputMaterials = Lists.newArrayList();
            final List<ItemStack> outputMaterials = Lists.newArrayList();

            for (String materialIdentifier : factoryConfig.getConfigurationSection("recipes." + recipeIdentifier + ".input").getKeys(false)) {
                final String path = "recipes." + recipeIdentifier + ".input." + materialIdentifier + ".";
                Material material = null;
                String name = null;
                int amount = 1;
                int data = 0;
                final Map<Enchantment, Integer> enchantments = Maps.newHashMap();

                if (factoryConfig.get(path + "id") != null) {
                    material = Material.getMaterial(factoryConfig.getInt(path + "id"));
                }

                if (factoryConfig.get(path + "name") != null) {
                    name = ChatColor.translateAlternateColorCodes('&', factoryConfig.getString(path + "name"));
                }

                if (factoryConfig.get(path + "amount") != null) {
                    amount = factoryConfig.getInt(path + "amount");
                }

                if (factoryConfig.get(path + "durability") != null) {
                    data = factoryConfig.getInt(path + "durability");
                }

                if (factoryConfig.get(path + "enchantments") != null) {
                    for (String enchantmentName : factoryConfig.getConfigurationSection(path + "enchantments").getKeys(false)) {
                        final Enchantment enchantment = RemappedEnchantment.getEnchantmentByName(enchantmentName);
                        final int level = factoryConfig.getInt(path + "enchantments." + enchantmentName);

                        if (enchantment == null) {
                            Logger.error("Invalid enchantment: " + enchantmentName);
                            continue;
                        }

                        enchantments.put(enchantment, level);
                    }
                }

                if (material == null) {
                    Logger.error("Invalid material for " + recipeName + ": " + material);
                    continue;
                }

                final ItemStack item;

                if (name != null) {
                    item = new ItemBuilder()
                            .setMaterial(material)
                            .setName(name)
                            .setName(name)
                            .setAmount(amount)
                            .setData((short)data)
                            .addEnchant(enchantments)
                            .build();
                } else {
                    item = new ItemBuilder()
                            .setMaterial(material)
                            .setName(name)
                            .setAmount(amount)
                            .setData((short)data)
                            .addEnchant(enchantments)
                            .build();
                }

                if (material.equals(Material.ENCHANTED_BOOK)) {
                    final EnchantmentStorageMeta meta = (EnchantmentStorageMeta)item.getItemMeta();
                    enchantments.forEach((enchantment, level) -> meta.addStoredEnchant(enchantment, level, false));
                    item.setItemMeta(meta);
                }

                inputMaterials.add(item);
            }

            for (String materialIdentifier : factoryConfig.getConfigurationSection("recipes." + recipeIdentifier + ".output").getKeys(false)) {
                final String path = "recipes." + recipeIdentifier + ".output." + materialIdentifier + ".";
                Material material = null;
                String name = null;
                int amount = 1;
                int data = 0;
                final Map<Enchantment, Integer> enchantments = Maps.newHashMap();

                if (factoryConfig.get(path + "id") != null) {
                    material = Material.getMaterial(factoryConfig.getInt(path + "id"));
                }

                if (factoryConfig.get(path + "name") != null) {
                    name = ChatColor.translateAlternateColorCodes('&', factoryConfig.getString(path + "name"));
                }

                if (factoryConfig.get(path + "amount") != null) {
                    amount = factoryConfig.getInt(path + "amount");
                }

                if (factoryConfig.get(path + "durability") != null) {
                    data = factoryConfig.getInt(path + "durability");
                }

                if (factoryConfig.get(path + "enchantments") != null) {
                    for (String enchantmentName : factoryConfig.getConfigurationSection(path + "enchantments").getKeys(false)) {
                        final Enchantment enchantment = RemappedEnchantment.getEnchantmentByName(enchantmentName);
                        final int level = factoryConfig.getInt(path + "enchantments." + enchantmentName);

                        if (enchantment == null) {
                            Logger.error("Invalid enchantment: " + enchantmentName);
                            continue;
                        }

                        enchantments.put(enchantment, level);
                    }
                }

                if (material == null) {
                    Logger.error("Invalid material for " + recipeName + ": " + material);
                    continue;
                }

                final ItemBuilder builder = new ItemBuilder()
                        .setMaterial(material)
                        .setAmount(amount)
                        .setData((short)data);

                if (name != null) {
                    builder.setName(name);
                }

                if (!enchantments.isEmpty() && !material.equals(Material.ENCHANTED_BOOK)) {
                    builder.addEnchant(enchantments);
                }

                final ItemStack item = builder.build();

                if (material.equals(Material.ENCHANTED_BOOK)) {
                    final EnchantmentStorageMeta meta = (EnchantmentStorageMeta)item.getItemMeta();
                    enchantments.forEach((enchantment, level) -> meta.addStoredEnchant(enchantment, level, false));
                    item.setItemMeta(meta);
                }

                outputMaterials.add(item);
            }

            final FactoryRecipe recipe = new FactoryRecipe(recipeName, jobTime, requiredLevel, expPerRun, inputMaterials, outputMaterials);
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

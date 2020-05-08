package com.playares.core.factory.menu;

import com.google.common.collect.Lists;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.Menu;
import com.playares.commons.remap.RemappedEnchantment;
import com.playares.commons.util.bukkit.Players;
import com.playares.commons.util.general.Time;
import com.playares.core.Ares;
import com.playares.core.factory.data.Factory;
import com.playares.core.factory.data.FactoryJob;
import com.playares.core.factory.data.FactoryRecipe;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FactoryRecipeMenu extends Menu {
    @Getter public Factory factory;

    public FactoryRecipeMenu(Ares plugin, Player player, Factory factory) {
        super(
                plugin,
                player,
                    "Factory LVL " + factory.getLevel() +
                        " (" + Math.floor(factory.getExperience()) + "/" + plugin.getFactoryManager().getLevelRequirement(factory.getLevel() + 1) + ")",
                3);

        this.factory = factory;

        final int factoryLevel = factory.getLevel();
        int pos = 0;
        final double speedMultiplier = plugin.getFactoryManager().getSpeedMultiplier(player);
        final List<FactoryRecipe> sortedRecipes = Lists.newArrayList(plugin.getFactoryManager().getRecipeManager().getRecipeRepository());

        final Comparator<FactoryRecipe> passA = Comparator.comparing(factoryRecipe -> factoryRecipe.isUnlocked(factoryLevel));
        final Comparator<FactoryRecipe> passB = Comparator.comparingInt(factoryRecipe -> factoryRecipe.getRequiredLevel());
        final Comparator<FactoryRecipe> passC = Comparator.comparing(FactoryRecipe::getName);

        sortedRecipes.sort(passA.thenComparing(passB.reversed()).thenComparing(passC));
        Collections.reverse(sortedRecipes);

        for (FactoryRecipe recipe : sortedRecipes) {
            final List<String> lore = Lists.newArrayList();

            if (!recipe.isUnlocked(factoryLevel)) {
                lore.add(ChatColor.RED + "Recipe is locked");
                lore.add(ChatColor.GOLD + "Required Level" + ChatColor.YELLOW + ": " + recipe.getRequiredLevel());
                lore.add(ChatColor.DARK_RED + "Current Level" + ChatColor.RED + ": " + factoryLevel);
                lore.add(ChatColor.GREEN + "Experience Until Next Level" + ChatColor.WHITE + ": " + plugin.getFactoryManager().getExpToNextLevel(factory));
                lore.add(ChatColor.RESET + " ");
            }

            lore.add(ChatColor.GOLD + "Input Resources:");

            for (ItemStack input : recipe.getMaterials()) {
                final String itemName = ((input.hasItemMeta() && input.getItemMeta().hasDisplayName()) ? input.getItemMeta().getDisplayName() : StringUtils.capitalize(input.getType().name().toLowerCase().replace("_", " ")));
                lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + "x" + input.getAmount() + " " + itemName);

                if (input.hasItemMeta() && !input.getItemMeta().getEnchants().isEmpty()) {
                    lore.add(ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.GOLD + "Enchantments:");

                    for (Enchantment enchantment : input.getItemMeta().getEnchants().keySet()) {
                        final int level = input.getItemMeta().getEnchantLevel(enchantment);
                        final RemappedEnchantment remapped = RemappedEnchantment.getRemappedEnchantmentByBukkit(enchantment);
                        final String enchantmentName = StringUtils.capitalize(remapped.name().toLowerCase().replace("_", " "));
                        lore.add(ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.GRAY + " - " + ChatColor.AQUA + enchantmentName + " " + level);
                    }
                }
            }

            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GOLD + "Output Resources:");

            for (ItemStack output : recipe.getOutput()) {
                final String itemName = ((output.hasItemMeta() && output.getItemMeta().hasDisplayName()) ? output.getItemMeta().getDisplayName() : StringUtils.capitalize(output.getType().name().toLowerCase().replace("_", " ")));
                lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + "x" + output.getAmount() + " " + itemName);

                if (output.hasItemMeta() && !output.getItemMeta().getEnchants().isEmpty()) {
                    lore.add(ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.GOLD + "Enchantments:");

                    for (Enchantment enchantment : output.getItemMeta().getEnchants().keySet()) {
                        final int level = output.getItemMeta().getEnchantLevel(enchantment);
                        final RemappedEnchantment remapped = RemappedEnchantment.getRemappedEnchantmentByBukkit(enchantment);
                        final String enchantmentName = StringUtils.capitalize(remapped.name().toLowerCase().replace("_", " "));
                        lore.add(ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.GRAY + " - " + ChatColor.AQUA + enchantmentName + " " + level);
                    }
                }
            }

            lore.add(ChatColor.RESET + " ");

            if (speedMultiplier > 1.0) {
                lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.GRAY + Time.convertToRemaining(Math.round(recipe.getJobTime() / speedMultiplier) * 1000L));
                lore.add(ChatColor.GOLD + "Premium Speed Boost" + ChatColor.GRAY + ": " + speedMultiplier + "x faster");
            } else {
                lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.GRAY + Time.convertToRemaining(recipe.getJobTime() * 1000L));
            }

            lore.add(ChatColor.GREEN + "Experience Reward: " + ChatColor.WHITE + recipe.getExperience());

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(recipe.getOutput().get(0).getType())
                    .setName(ChatColor.DARK_GREEN + recipe.getName())
                    .addLore(lore)
                    .setAmount(recipe.getOutput().get(0).getAmount())
                    .build();

            addItem(new ClickableItem(icon, pos, click -> {
                if (!recipe.isUnlocked(factoryLevel)) {
                    player.sendMessage(ChatColor.RED + "This factory is not a high enough level to run this recipe yet");
                    return;
                }

                if (factory.getActiveJobs().size() >= 9) {
                    player.sendMessage(ChatColor.RED + "This factory is working at max capacity. Please wait for a job to finish before starting a new one.");
                    return;
                }

                if (!recipe.hasRequiredMaterials(player)) {
                    player.sendMessage(ChatColor.RED + "You do not have enough resources to start this job");
                    return;
                }

                final List<ItemStack> toSubtract = Lists.newArrayList();

                for (ItemStack requirement : recipe.getMaterials()) {
                    for (ItemStack content : player.getInventory().getContents()) {
                        if (
                                content == null ||
                                        !content.getType().equals(requirement.getType()) ||
                                        content.getDurability() != requirement.getDurability() ||
                                        content.getAmount() < requirement.getAmount()) {

                            continue;

                        }

                        if (toSubtract.contains(content)) {
                            continue;
                        }

                        toSubtract.add(content);
                    }
                }

                if (toSubtract.size() != recipe.getMaterials().size()) {
                    player.sendMessage(ChatColor.RED + "You do not have enough resources to start this job");
                    return;
                }

                toSubtract.forEach(item -> {
                    final ItemStack recipeItem = recipe.getMaterials().stream().filter(inputItem -> inputItem.getType().equals(item.getType()) && inputItem.getDurability() == item.getDurability()).findFirst().orElse(null);

                    if (recipeItem == null) {
                        player.closeInventory();
                        player.sendMessage(ChatColor.RED + "An unexpected error occured");
                        return;
                    }

                    final int amount = recipeItem.getAmount();

                    if (item.getAmount() > amount) {
                        item.setAmount(item.getAmount() - amount);
                    } else {
                        player.getInventory().removeItem(item);
                    }
                });

                player.updateInventory();

                final FactoryJob job = new FactoryJob(recipe, (int)Math.round(recipe.getJobTime() / speedMultiplier));
                factory.getActiveJobs().add(job);

                player.closeInventory();
                player.sendMessage(ChatColor.AQUA + "Starting job for " + recipe.getName());

                if (speedMultiplier > 1.0) {
                    player.sendMessage(ChatColor.GOLD + "Applied premium speed multiplier: " + ChatColor.YELLOW + speedMultiplier + "x faster!");
                    Players.playSound(player, Sound.NOTE_BASS_GUITAR);
                }
            }));

            pos += 1;
        }
    }
}

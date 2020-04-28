package com.llewkcor.ares.core.factory.menu;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.commons.remap.RemappedEnchantment;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.factory.data.FactoryRecipe;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FactoryRecipePreviewMenu extends Menu {
    public FactoryRecipePreviewMenu(Ares plugin, Player player) {
        super(
                plugin,
                player,
                "Factory Recipes",
                3);

        int pos = 0;
        final double speedMultiplier = plugin.getFactoryManager().getSpeedMultiplier(player);
        final List<FactoryRecipe> sortedRecipes = Lists.newArrayList(plugin.getFactoryManager().getRecipeManager().getRecipeRepository());

        sortedRecipes.sort(Comparator.comparing(FactoryRecipe::getRequiredLevel).thenComparing(FactoryRecipe::getName));
        Collections.reverse(sortedRecipes);

        for (FactoryRecipe recipe : sortedRecipes) {
            final List<String> lore = Lists.newArrayList();

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

            addItem(new ClickableItem(icon, pos, click -> {}));

            pos += 1;
        }
    }
}

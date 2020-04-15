package com.llewkcor.ares.core.factory.menu;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.commons.remap.RemappedEnchantment;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.factory.data.Factory;
import com.llewkcor.ares.core.factory.data.FactoryJob;
import com.llewkcor.ares.core.factory.data.FactoryRecipe;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class FactoryRecipeMenu extends Menu {
    @Getter public Factory factory;

    public FactoryRecipeMenu(Ares plugin, Player player, Factory factory) {
        super(plugin, player, "Factory Recipes", 3);
        this.factory = factory;
        int pos = 0;

        for (FactoryRecipe recipe : plugin.getFactoryManager().getRecipeManager().getRecipeRepository()) {
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
            lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.GRAY + Time.convertToRemaining(recipe.getJobTime() * 1000L));

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(recipe.getOutput().get(0).getType())
                    .setName(ChatColor.DARK_GREEN + recipe.getName())
                    .addLore(lore)
                    .setAmount(recipe.getOutput().get(0).getAmount())
                    .build();

            addItem(new ClickableItem(icon, pos, click -> {
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

                final FactoryJob job = new FactoryJob(recipe);
                factory.getActiveJobs().add(job);

                player.closeInventory();
                player.sendMessage(ChatColor.AQUA + "Starting job for " + recipe.getName());
            }));

            pos += 1;
        }
    }
}

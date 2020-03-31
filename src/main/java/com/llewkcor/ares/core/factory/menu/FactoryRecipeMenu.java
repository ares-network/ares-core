package com.llewkcor.ares.core.factory.menu;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.factory.data.Factory;
import com.llewkcor.ares.core.factory.data.FactoryJob;
import com.llewkcor.ares.core.factory.data.FactoryRecipe;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

            for (Material inputMaterial : recipe.getMaterials().keySet()) {
                final int amount = recipe.getMaterials().get(inputMaterial);
                lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + "x" + amount + " " + StringUtils.capitalize(inputMaterial.name().toLowerCase().replace("_", " ")));
            }

            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.GOLD + "Output Resources:");

            for (Material outputMaterial : recipe.getOutput().keySet()) {
                final int amount = recipe.getOutput().get(outputMaterial);
                lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + "x" + amount + " " + StringUtils.capitalize(outputMaterial.name().toLowerCase().replace("_", " ")));
            }

            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.GRAY + Time.convertToRemaining(recipe.getJobTime() * 1000L));

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(recipe.getOutputItems().get(0).getType())
                    .setName(ChatColor.DARK_GREEN + recipe.getName())
                    .addLore(lore)
                    .setAmount(recipe.getOutputItems().get(0).getAmount())
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
                final List<Material> fulfilled = Lists.newArrayList();

                for (ItemStack content : player.getInventory().getContents()) {
                    if (content == null || content.getType().equals(Material.AIR)) {
                        continue;
                    }

                    if (fulfilled.contains(content.getType())) {
                        continue;
                    }

                    final int amountNeeded = recipe.getMaterials().getOrDefault(content.getType(), 0);

                    if (amountNeeded <= 0 || amountNeeded > content.getAmount()) {
                        continue;
                    }

                    toSubtract.add(content);
                    fulfilled.add(content.getType());
                }

                if (toSubtract.size() != recipe.getMaterials().size()) {
                    player.sendMessage(ChatColor.RED + "You do not have enough resources to start this job");
                    return;
                }

                toSubtract.forEach(item -> {
                    final int amount = recipe.getMaterials().get(item.getType());

                    if (item.getAmount() > amount) {
                        item.setAmount(item.getAmount() - amount);
                    } else {
                        player.getInventory().removeItem(item);
                        player.updateInventory();
                    }
                });

                final FactoryJob job = new FactoryJob(recipe);
                factory.getActiveJobs().add(job);

                player.closeInventory();
                player.sendMessage(ChatColor.AQUA + "Starting job for " + recipe.getName());
            }));

            pos += 1;
        }
    }
}

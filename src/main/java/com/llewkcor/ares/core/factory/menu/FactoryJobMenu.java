package com.llewkcor.ares.core.factory.menu;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.factory.data.Factory;
import com.llewkcor.ares.core.factory.data.FactoryJob;
import com.llewkcor.ares.core.factory.data.FactoryRecipe;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FactoryJobMenu extends Menu {
    @Getter public Ares ares;
    @Getter public Factory factory;
    @Getter private Scheduler updateScheduler;
    private BukkitTask updateTask;

    public FactoryJobMenu(Ares plugin, Player player, Factory factory) {
        super(plugin, player, "Factory Jobs", 5);

        this.ares = plugin;
        this.factory = factory;
        this.updateScheduler = new Scheduler(plugin).sync(this::update).repeat(0L, 20L);
    }

    @Override
    public void open() {
        super.open();
        this.updateTask = updateScheduler.run();
    }

    private void update() {
        clear();

        if (factory.getActiveJobs().isEmpty()) {
            return;
        }

        final List<FactoryJob> jobs = Lists.newArrayList(factory.getActiveJobs());

        // Sort and flip
        jobs.sort(Comparator.comparing(FactoryJob::getReadyTime));
        Collections.reverse(jobs);

        int pos = 0;

        for (FactoryJob job : jobs) {
            final FactoryRecipe recipe = ares.getFactoryManager().getRecipeManager().getRecipe(job.getRecipeName());

            if (recipe == null) {
                continue;
            }

            final List<String> lore = Lists.newArrayList();

            lore.add(org.bukkit.ChatColor.GOLD + "Output Resources:");

            for (Material outputMaterial : recipe.getOutput().keySet()) {
                final int amount = recipe.getOutput().get(outputMaterial);
                lore.add(org.bukkit.ChatColor.GRAY + " - " + org.bukkit.ChatColor.AQUA + "x" + amount + " " + StringUtils.capitalize(outputMaterial.name().toLowerCase().replace("_", " ")));
            }

            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.DARK_AQUA + "Remaining Time: " + ChatColor.GRAY + Time.convertToRemaining(job.getReadyTime() - Time.now()));

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.BOOK)
                    .setName(ChatColor.AQUA + recipe.getName())
                    .addLore(lore)
                    .build();

            addItem(new ClickableItem(icon, pos, click -> {}));

            pos += 1;
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);

        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}
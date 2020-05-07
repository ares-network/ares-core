package com.playares.core.factory.menu;

import com.google.common.collect.Lists;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.PaginatedMenu;
import com.playares.core.Ares;
import com.playares.core.factory.data.Factory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class FactoryListMenu extends PaginatedMenu<Factory> {
    private final Ares plugin;

    public FactoryListMenu(Ares plugin, Player player, String title, Collection<Factory> entries) {
        super(plugin, player, title, 6, entries);
        this.plugin = plugin;
    }

    @Override
    public List<Factory> sort() {
        final List<Factory> factories = Lists.newArrayList(entries);
        factories.sort(Comparator.comparingDouble(factory -> factory.getExperience()));
        return null;
    }

    @Override
    public ClickableItem getItem(Factory factory, int i) {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.AQUA + factory.getFurnaceLocation().toString());
        lore.add(ChatColor.DARK_AQUA + "Current Job Queue" + ChatColor.AQUA + ": " + ChatColor.RESET + factory.getActiveJobs().size());

        final ItemStack icon = new ItemBuilder()
                .setMaterial(Material.FURNACE)
                .setName(ChatColor.RED + "Factory Level: " + ChatColor.RESET + factory.getLevel() + ChatColor.GOLD + " (" + ChatColor.YELLOW + factory.getExperience() + "exp" + ChatColor.GOLD + ")")
                .addLore(lore)
                .build();

        return new ClickableItem(icon, i, click -> {
            final FactoryJobMenu jobMenu = new FactoryJobMenu(plugin, player, factory);
            player.closeInventory();
            jobMenu.open();
        });
    }
}
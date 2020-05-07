package com.playares.core.snitch.menu;

import com.google.common.collect.Lists;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.PaginatedMenu;
import com.playares.core.Ares;
import com.playares.core.snitch.data.Snitch;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class SnitchListMenu extends PaginatedMenu<Snitch> {
    public SnitchListMenu(Ares plugin, Player player, String name, Collection<Snitch> snitches) {
        super(plugin, player, name, 6, snitches);
    }

    @Override
    public List<Snitch> sort() {
        final List<Snitch> entries = Lists.newArrayList(this.entries);
        entries.sort(Comparator.comparing(Snitch::getName));
        return entries;
    }

    @Override
    public ClickableItem getItem(Snitch snitch, int i) {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.AQUA + snitch.getLocation().toString());
        lore.add(ChatColor.GOLD + "" + snitch.getLogEntries().size() + " Logged Events");

        final ItemStack icon = new ItemBuilder()
                .setMaterial(Material.JUKEBOX)
                .setName(ChatColor.RED + snitch.getName())
                .addLore(lore)
                .build();

        return new ClickableItem(icon, i, click -> {});
    }
}
package com.playares.core.snitch.menu;

import com.google.common.collect.Lists;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.Menu;
import com.playares.core.Ares;
import com.playares.core.snitch.data.Snitch;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class SnitchListMenu extends Menu {
    @Getter public final Collection<Snitch> snitches;
    @Getter @Setter public int page;

    public SnitchListMenu(Ares plugin, Player player, String name, Collection<Snitch> snitches) {
        super(plugin, player, name, 6);
        this.snitches = snitches;
        this.page = 0;
    }

    @Override
    public void open() {
        super.open();
        update();
    }

    private void update() {
        clear();

        int cursor = 0;
        final int start = page * 52;
        final int end = start + 52;
        final boolean hasNextPage = snitches.size() > end;
        final boolean hasPrevPage = start > 0;

        final List<Snitch> entries = Lists.newArrayList(snitches);
        entries.sort(Comparator.comparing(Snitch::getName));

        for (int i = start; i < end; i++) {
            if (cursor >= 52 || entries.size() <= i) {
                break;
            }

            final Snitch snitch = entries.get(i);

            final List<String> lore = Lists.newArrayList();
            lore.add(ChatColor.AQUA + snitch.getLocation().toString());
            lore.add(ChatColor.GOLD + "" + snitch.getLogEntries().size() + " Logged Events");

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.JUKEBOX)
                    .setName(ChatColor.RED + snitch.getName())
                    .addLore(lore)
                    .build();

            addItem(new ClickableItem(icon, cursor, click -> {}));

            cursor += 1;
        }

        if (hasNextPage) {
            final ItemStack nextPageIcon = new ItemBuilder().setMaterial(Material.EMERALD_BLOCK).setName(ChatColor.GREEN + "Next Page").build();
            addItem(new ClickableItem(nextPageIcon, 53, click -> {
                setPage(page + 1);
                update();
            }));
        }

        if (hasPrevPage) {
            final ItemStack prevPageIcon = new ItemBuilder().setMaterial(Material.REDSTONE_BLOCK).setName(ChatColor.RED + "Previous Page").build();
            addItem(new ClickableItem(prevPageIcon, 52, click -> {
                setPage(page - 1);
                update();
            }));
        }

        player.updateInventory();
    }
}

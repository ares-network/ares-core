package com.llewkcor.ares.core.snitch.menu;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.menu.ClickableItem;
import com.llewkcor.ares.commons.menu.Menu;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.snitch.data.Snitch;
import com.llewkcor.ares.core.snitch.data.SnitchEntry;
import com.llewkcor.ares.core.snitch.data.SnitchEntryType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class SnitchLogMenu extends Menu {
    @Getter public final Snitch snitch;
    @Getter @Setter public int page;

    public SnitchLogMenu(Plugin plugin, Player player, Snitch snitch) {
        super(plugin, player, snitch.getName(), 6);
        this.snitch = snitch;
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
        final boolean hasNextPage = snitch.getLogEntries().size() > end;
        final boolean hasPrevPage = start > 0;

        final List<SnitchEntry> entries = Lists.newArrayList(snitch.getLogEntries());
        entries.sort(Comparator.comparing(SnitchEntry::getCreatedDate));
        Collections.reverse(entries);

        for (int i = start; i < end; i++) {
            if (cursor >= 52 || entries.size() <= i) {
                break;
            }

            final SnitchEntry entry = entries.get(i);

            if (entry == null) {
                continue;
            }

            final List<String> lore = Lists.newArrayList();

            lore.add(ChatColor.GOLD + entry.getDescription());
            lore.add(ChatColor.AQUA + entry.getBlockLocation().toString());
            lore.add(ChatColor.GRAY + Time.convertToInaccurateElapsed(Time.now() - entry.getCreatedDate()) + " ago");

            String name;
            Material material;

            if (entry.getEntity() == null || entry.getType().getDescriptor() == null) {
                Logger.error("Bad log entries");
                continue;
            }

            name = ChatColor.BLUE + entry.getEntity() + " " + entry.getType().getDescriptor();

            if (entry.getType().equals(SnitchEntryType.BLOCK_BREAK) || entry.getType().equals(SnitchEntryType.BLOCK_PLACE) || entry.getType().equals(SnitchEntryType.BLOCK_INTERACTION)) {
                material = Material.getMaterial(entry.getBlock());
            }

            else {
                material = Material.SKULL_ITEM;
            }

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(material)
                    .setName(name)
                    .setData((short)3)
                    .addLore(lore)
                    .build();

            if (icon.getType().equals(Material.SKULL_ITEM)) {
                final SkullMeta meta = (SkullMeta)icon.getItemMeta();
                meta.setOwner(entry.getEntity());
                icon.setItemMeta(meta);
            }

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
    }
}
package com.playares.core.acid.menu;

import com.google.common.collect.Lists;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.PaginatedMenu;
import com.playares.commons.util.general.Time;
import com.playares.core.Ares;
import com.playares.core.acid.data.AcidBlock;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class AcidListMenu extends PaginatedMenu<AcidBlock> {
    public AcidListMenu(Ares plugin, Player player, String title, Collection<AcidBlock> entries) {
        super(plugin, player, title, 6, entries);
    }

    @Override
    public List<AcidBlock> sort() {
        final List<AcidBlock> result = Lists.newArrayList(entries);
        result.sort(Comparator.comparingInt(AcidBlock::getDamageDealt));
        return result;
    }

    @Override
    public ClickableItem getItem(AcidBlock acidBlock, int i) {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.AQUA + acidBlock.getLocation().toString());
        lore.add(ChatColor.GOLD + "Damage Dealt" + ChatColor.YELLOW + ": " + ChatColor.RESET + acidBlock.getDamageDealt());

        if (!acidBlock.isMature()) {
            lore.add(ChatColor.RED + "Matures in " + Time.convertToRemaining(acidBlock.getMatureTime() - Time.now()));
        }

        lore.add(ChatColor.DARK_AQUA + "Expires in " + ChatColor.AQUA + Time.convertToRemaining(acidBlock.getExpireTime() - Time.now()));

        final ItemStack icon = new ItemBuilder()
                .setMaterial(Material.GOLD_BLOCK)
                .setName(ChatColor.RED + "Acid Block")
                .addLore(lore)
                .build();

        return new ClickableItem(icon, i, click -> {});
    }
}
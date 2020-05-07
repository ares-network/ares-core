package com.playares.core.bastion.menu;

import com.google.common.collect.Lists;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.location.PLocatable;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.PaginatedMenu;
import com.playares.commons.util.general.Time;
import com.playares.core.Ares;
import com.playares.core.bastion.data.Bastion;
import com.playares.core.network.data.Network;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class BastionListMenu extends PaginatedMenu<Bastion> {
    private final Ares plugin;

    public BastionListMenu(Ares plugin, Player player, String title, Collection<Bastion> entries) {
        super(plugin, player, title, 6, entries);
        this.plugin = plugin;
    }

    @Override
    public List<Bastion> sort() {
        final List<Bastion> entries = Lists.newArrayList(this.entries);
        entries.sort(Comparator.comparingDouble(bastion -> bastion.getLocation().distance(new PLocatable(player))));
        return entries;
    }

    @Override
    public ClickableItem getItem(Bastion bastion, int i) {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.AQUA + bastion.getLocation().toString());

        if (!bastion.isMature()) {
            lore.add(ChatColor.RED + "Matures in " + Time.convertToRemaining(bastion.getMatureTime() - Time.now()));
        }

        final Network owner = plugin.getNetworkManager().getNetworkByID(bastion.getOwnerId());

        final ItemStack icon = new ItemBuilder()
                .setMaterial(Material.SPONGE)
                .setName(owner != null ? ChatColor.RED + owner.getName() : "? ? ?")
                .addLore(lore)
                .build();

        return new ClickableItem(icon, i, click -> {});
    }
}

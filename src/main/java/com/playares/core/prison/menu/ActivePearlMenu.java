package com.playares.core.prison.menu;

import com.google.common.collect.Lists;
import com.playares.commons.location.BLocatable;
import com.playares.commons.menu.ClickableItem;
import com.playares.commons.menu.PaginatedMenu;
import com.playares.core.prison.data.PearlLocationType;
import com.playares.core.prison.data.PrisonPearl;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class ActivePearlMenu extends PaginatedMenu<PrisonPearl> {
    public ActivePearlMenu(Plugin plugin, Player player, Collection<PrisonPearl> entries) {
        super(plugin, player, "Active Prison Pearls", 6, entries);
    }

    @Override
    public List<PrisonPearl> sort() {
        final List<PrisonPearl> entries = Lists.newArrayList(this.entries);
        entries.sort(Comparator.comparingLong(PrisonPearl::getCreateTime));
        return entries;
    }

    @Override
    public ClickableItem getItem(PrisonPearl prisonPearl, int i) {
        return new ClickableItem(prisonPearl.getItem(), i, click -> {
            if (prisonPearl.getLocationType().equals(PearlLocationType.PLAYER)) {
                final Player locatePlayer = prisonPearl.getKiller();

                if (locatePlayer != null) {
                    prisonPearl.setLocation(new BLocatable(locatePlayer.getLocation().getBlock()));
                }
            }

            final Location location = prisonPearl.getBukkitLocation();
            final PearlLocationType locationType = prisonPearl.getLocationType();

            if (locationType.equals(PearlLocationType.PLAYER)) {
                player.sendMessage(ChatColor.GRAY + "Your pearl is being held by a player at " + ChatColor.DARK_AQUA + new BLocatable(location.getBlock()).toString());
            } else if (locationType.equals(PearlLocationType.CONTAINER)) {
                player.sendMessage(ChatColor.GRAY + "Your pearl is being held in a container at " + ChatColor.DARK_AQUA + new BLocatable(location.getBlock()).toString());
            } else {
                player.sendMessage(ChatColor.GRAY + "Your pearl is laying on the ground at " + ChatColor.DARK_AQUA + new BLocatable(location.getBlock()).toString());
            }
        });
    }
}

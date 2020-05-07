package com.playares.core.spawn.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.item.custom.CustomItem;
import com.playares.commons.promise.SimplePromise;
import com.playares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class SpawnCompass implements CustomItem {
    @Getter public final Ares plugin;

    @Override
    public Material getMaterial() {
        return Material.COMPASS;
    }

    @Override
    public String getName() {
        return ChatColor.GOLD + "Right-click to start playing";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.GRAY + "Right-click this item while holding");
        lore.add(ChatColor.GRAY + "it to access the spawn selector");
        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public Runnable getRightClick(Player who) {
        return () -> {
            plugin.getSpawnManager().getHandler().openSelectorMenu(who, new SimplePromise() {
                @Override
                public void success() {}

                @Override
                public void fail(String s) {
                    who.sendMessage(ChatColor.RED + s);
                }
            });
        };
    }
}

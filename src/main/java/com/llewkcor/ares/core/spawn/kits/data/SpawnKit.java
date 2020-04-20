package com.llewkcor.ares.core.spawn.kits.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

@AllArgsConstructor
public final class SpawnKit {
    @Getter public final String name;
    @Getter public final String displayName;
    @Getter public final String permission;
    @Getter public final List<PotionEffect> effects;
    @Getter public final List<ItemStack> items;
    @Getter public final int weight;

    /**
     * Returns true if this is a default kit
     * @return True if default
     */
    public boolean isDefault() {
        return permission == null;
    }

    /**
     * Gives provided player this spawn kit
     * @param player Player
     */
    public void give(Player player) {
        for (ItemStack item : items) {
            player.getInventory().addItem(item);
        }

        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }

        player.sendMessage(ChatColor.GOLD + "You have been granted the " + displayName + ChatColor.GOLD + " starter kit");
    }
}
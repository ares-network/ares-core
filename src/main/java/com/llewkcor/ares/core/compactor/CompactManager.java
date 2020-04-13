package com.llewkcor.ares.core.compactor;

import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.compactor.listener.CompactorListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class CompactManager {
    @Getter public final Ares plugin;
    @Getter public final CompactHandler handler;

    public CompactManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new CompactHandler(this);

        Bukkit.getPluginManager().registerEvents(new CompactorListener(this), plugin);
    }

    /**
     * Returns true if the provided ItemStack is compacted
     * @param item Item
     * @return True if compacted
     */
    public boolean isCompact(ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = meta.getLore();

        if (lore.isEmpty()) {
            return false;
        }

        for (String line : lore) {
            if (line.equals(ChatColor.DARK_PURPLE + "Compacted")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a Map of the Mat
     * @param item
     * @return
     */
    public int getCompactedAmount(ItemStack item) {
        if (!isCompact(item)) {
            return 1;
        }

        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = meta.getLore();

        if (lore.size() != 2) {
            Logger.error("Compacted ItemStack returned a lore size of " + lore.size() + " instead of the expected 2");
            return 1;
        }

        int amount = 1;

        try {
            amount = Integer.parseInt(ChatColor.stripColor(lore.get(1)));
        } catch (NumberFormatException ex) {
            Logger.error("Failed to get amount of compacted ItemStack: " + ex.getMessage());
            return 1;
        }

        return amount;
    }
}
package com.llewkcor.ares.core.compactor;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public final class CompactHandler {
    @Getter public CompactManager manager;

    /**
     * Handles compacting the item in the provided Players hand
     * @param player Player
     * @param promise Promise
     */
    public void compact(Player player, SimplePromise promise) {
        final ItemStack hand = player.getItemInHand();

        if (hand == null || hand.getType().equals(Material.AIR)) {
            promise.fail("You are not holding an item");
            return;
        }

        final List<ItemStack> merge = Lists.newArrayList();
        int amount = 1;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.getType().equals(hand.getType())) {
                continue;
            }

            merge.add(item);
            amount += item.getAmount();
        }

        if (amount < 64) {
            promise.fail("Compactor only works on items with more than 64 quantity");
            return;
        }

        final int cost = (amount / 64) * 5;

        if (player.getLevel() < cost) {
            promise.fail("Compacting " + amount + " " + StringUtils.capitalize(hand.getType().name().toLowerCase().replace("_", " ") + " will cost " + cost + " EXP Levels."));
            return;
        }

        player.setLevel(player.getLevel() - cost);
        player.getInventory().removeItem(hand);
        merge.forEach(item -> player.getInventory().remove(item));

        final ItemStack compacted = new ItemBuilder()
                .setMaterial(hand.getType())
                .setData(hand.getDurability())
                .setAmount(1)
                .addLore(Arrays.asList(ChatColor.DARK_PURPLE + "Compacted", ChatColor.DARK_PURPLE + "" + amount))
                .build();

        player.getInventory().setItemInHand(compacted);
        promise.success();
    }

    /**
     * Handles decompacting the item in the provided players hand
     * @param player Player
     * @param promise Promise
     */
    public void decompact(Player player, SimplePromise promise) {
        final ItemStack hand = player.getItemInHand();

        if (!manager.isCompact(hand)) {
            promise.fail("You are not holding a compacted item");
            return;
        }

        final int amount = manager.getCompactedAmount(hand);

        player.getInventory().setItemInHand(null);

        for (int i = 1; i < amount; i++) {
            final ItemStack item = new ItemStack(hand.getType());

            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
                continue;
            }

            player.getInventory().addItem(item);
        }

        promise.success();
    }
}
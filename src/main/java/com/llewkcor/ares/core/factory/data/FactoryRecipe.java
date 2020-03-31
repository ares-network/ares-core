package com.llewkcor.ares.core.factory.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class FactoryRecipe {
    @Getter public String name;
    @Getter public int jobTime;
    @Getter public Map<Material, Integer> materials;
    @Getter public Map<Material, Integer> output;

    /**
     * Returns an Immutable List containing all items required for this recipe
     * @return ImmutableList of Bukkit ItemStacks
     */
    public ImmutableList<ItemStack> getRecipeItems() {
        final List<ItemStack> result = Lists.newArrayList();

        materials.keySet().forEach(material -> {
            final int amount = materials.get(material);
            result.add(new ItemBuilder().setMaterial(material).setAmount(amount).build());
        });

        return ImmutableList.copyOf(result);
    }

    /**
     * Returns an Immutable List containing all items to be output when this recipe is complete
     * @return ImmutableList of Bukkit ItemStacks
     */
    public List<ItemStack> getOutputItems() {
        final List<ItemStack> result = Lists.newArrayList();

        output.keySet().forEach(material -> {
            final int amount = output.get(material);
            result.add(new ItemBuilder().setMaterial(material).setAmount(amount).build());
        });

        return ImmutableList.copyOf(result);
    }

    /**
     * Returns true if the provided Bukkit Player has the required resources in their inventory
     * to execute this Factory job
     * @param player Bukkit Player
     * @return True if required resources are reached
     */
    public boolean hasRequiredMaterials(Player player) {
        final List<ItemStack> toSubtract = Lists.newArrayList();
        final List<Material> fulfilled = Lists.newArrayList();

        for (ItemStack content : player.getInventory().getContents()) {
            if (content == null || content.getType().equals(Material.AIR)) {
                continue;
            }

            if (fulfilled.contains(content.getType())) {
                continue;
            }

            final int amountNeeded = materials.getOrDefault(content.getType(), 0);

            if (amountNeeded <= 0 || amountNeeded > content.getAmount()) {
                continue;
            }

            toSubtract.add(content);
            fulfilled.add(content.getType());
        }

        return toSubtract.size() == materials.size();
    }
}
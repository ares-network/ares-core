package com.llewkcor.ares.core.factory.data;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public final class FactoryRecipe {
    @Getter public String name;
    @Getter public int jobTime;
    @Getter public int requiredLevel;
    @Getter public double experience;
    @Getter public List<ItemStack> materials;
    @Getter public List<ItemStack> output;

    /**
     * Returns true if this recipe is unlocked for the provided level
     * @param currentLevel Current Factory Level
     * @return True if unlocked
     */
    public boolean isUnlocked(int currentLevel) {
        return currentLevel >= requiredLevel;
    }

    /**
     * Returns true if the provided ItemStack is a recipe item
     * @param item Bukkit ItemStack
     * @return True if it is an input resource
     */
    public boolean isRecipeItem(ItemStack item) {
        return materials
                .stream()
                .anyMatch(required -> required.getType().equals(item.getType()) &&
                        required.getAmount() <= item.getAmount() &&
                        required.getDurability() == item.getDurability());
    }

    /**
     * Returns true if the provided Bukkit Player has the required resources in their inventory
     * to execute this Factory job
     * @param player Bukkit Player
     * @return True if required resources are reached
     */
    public boolean hasRequiredMaterials(Player player) {
        final List<ItemStack> toSubtract = Lists.newArrayList();

        for (ItemStack requirement : materials) {
            for (ItemStack content : player.getInventory().getContents()) {
                if (
                        content == null ||
                        !content.getType().equals(requirement.getType()) ||
                        content.getDurability() != requirement.getDurability() ||
                        content.getAmount() < requirement.getAmount()) {

                    continue;

                }

                if (toSubtract.contains(content)) {
                    continue;
                }

                toSubtract.add(content);
            }
        }

        return toSubtract.size() == materials.size();
    }
}
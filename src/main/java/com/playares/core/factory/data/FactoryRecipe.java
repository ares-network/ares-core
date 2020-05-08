package com.playares.core.factory.data;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class FactoryRecipe {
    @Getter public final String name;
    @Getter public final int jobTime;
    @Getter public final int requiredLevel;
    @Getter public final double experience;
    private final List<ItemStack> materials;
    private final List<ItemStack> output;

    /**
     * Create a new Factory Recipe instance
     * @param name Recipe Name
     * @param jobTime Job Time
     * @param requiredLevel Required Level
     * @param experience Experience Reward
     * @param materials Input Materials
     * @param output Output Materials
     */
    public FactoryRecipe(String name, int jobTime, int requiredLevel, double experience, List<ItemStack> materials, List<ItemStack> output) {
        this.name = name;
        this.jobTime = jobTime;
        this.requiredLevel = requiredLevel;
        this.experience = experience;
        this.materials = materials;
        this.output = output;
    }

    /**
     * Returns a copy of the the input materials
     * @return Collection of Input Materials
     */
    public List<ItemStack> getMaterials() {
        return Lists.newArrayList(materials);
    }

    /**
     * Returns a copy of the output materials
     * @return Collection of Output Materials
     */
    public List<ItemStack> getOutput() {
        return Lists.newArrayList(output);
    }

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
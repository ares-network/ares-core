package com.llewkcor.ares.core.factory.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Factory implements MongoDocument<Factory> {
    @Getter public UUID uniqueId;
    @Getter public UUID ownerId;
    @Getter public Set<FactoryJob> activeJobs;
    @Getter public BLocatable chestLocation;
    @Getter public BLocatable furnaceLocation;
    @Getter public BLocatable benchLocation;

    public Factory() {
        this.uniqueId = UUID.randomUUID();
        this.ownerId = null;
        this.activeJobs = Sets.newConcurrentHashSet();
        this.chestLocation = null;
        this.furnaceLocation = null;
        this.benchLocation = null;
    }

    /**
     * Creates a new Factory instance
     * @param ownerId Factory Owner ID
     * @param chest Bukkit Chest Block
     * @param furnace Bukkit Furance Block
     * @param bench Bukkit Bench BLock
     */
    public Factory(UUID ownerId, Block chest, Block furnace, Block bench) {
        this.uniqueId = UUID.randomUUID();
        this.ownerId = ownerId;
        this.activeJobs = Sets.newConcurrentHashSet();
        this.chestLocation = new BLocatable(chest);
        this.furnaceLocation = new BLocatable(furnace);
        this.benchLocation = new BLocatable(bench);
    }

    /**
     * Handles placing output items in the chest
     * @param recipe Factory Recipe
     */
    public void finishJob(FactoryRecipe recipe) {
        final Map<Material, Integer> output = recipe.getOutput();
        final Block chestBlock = chestLocation.getBukkit();
        final BlockState blockState = chestBlock.getState();

        if (!(blockState instanceof InventoryHolder)) {
            Logger.error("Attempted to obtain InventoryHolder state for Factory (" + uniqueId.toString() + ")");
            return;
        }

        final InventoryHolder holder = (InventoryHolder)blockState;
        final Inventory inventory = holder.getInventory();

        for (Material material : output.keySet()) {
            final int amount = output.get(material);
            final ItemStack item = new ItemBuilder().setMaterial(material).setAmount(amount).build();

            if (inventory.firstEmpty() != -1) {
                inventory.addItem(item);
            } else {
                chestBlock.getWorld().dropItem(chestBlock.getLocation().add(0.0, 1.0, 0.0), item);
            }
        }

        final Block furnaceBlock = furnaceLocation.getBukkit();
        furnaceBlock.getWorld().playSound(furnaceLocation.getBukkit().getLocation(), Sound.CLICK, 1.0f, 1.0f);
    }

    /**
     * Returns true if the provided block location is one of the blocks connected to this factory
     * @param block BLocatable
     * @return True if connected
     */
    public boolean match(BLocatable block) {
        if (chestLocation.getWorldName().equals(block.getWorldName()) && chestLocation.getX() == block.getX() && chestLocation.getY() == block.getY() && chestLocation.getZ() == block.getZ()) {
            return true;
        }

        if (furnaceLocation.getWorldName().equals(block.getWorldName()) && furnaceLocation.getX() == block.getX() && furnaceLocation.getY() == block.getY() && furnaceLocation.getZ() == block.getZ()) {
            return true;
        }

        if (benchLocation.getWorldName().equals(block.getWorldName()) && benchLocation.getX() == block.getX() && benchLocation.getY() == block.getY() && benchLocation.getZ() == block.getZ()) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked") @Override
    public Factory fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.ownerId = (UUID)document.get("owner");
        this.activeJobs = Sets.newConcurrentHashSet();
        this.chestLocation = new BLocatable().fromDocument(document.get("chest_location", Document.class));
        this.furnaceLocation = new BLocatable().fromDocument(document.get("furnace_location", Document.class));
        this.benchLocation = new BLocatable().fromDocument(document.get("bench_location", Document.class));

        final List<Document> jobEntries = document.get("active_jobs", List.class);
        jobEntries.forEach(entryDocument -> activeJobs.add(new FactoryJob().fromDocument(entryDocument)));

        return this;
    }

    @Override
    public Document toDocument() {
        final List<Document> jobEntries = Lists.newArrayList();
        activeJobs.forEach(job -> jobEntries.add(job.toDocument()));

        return new Document()
                .append("id", uniqueId)
                .append("owner", ownerId)
                .append("active_jobs", jobEntries)
                .append("chest_location", chestLocation.toDocument())
                .append("furnace_location", furnaceLocation.toDocument())
                .append("bench_location", benchLocation.toDocument());
    }
}
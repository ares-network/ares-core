package com.llewkcor.ares.core.prison.listener;

import com.llewkcor.ares.core.prison.PrisonPearlManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

@AllArgsConstructor
public class PearlTrackerListener {
    @Getter public final PrisonPearlManager manager;

    @EventHandler
    public void onPlayerDropPearl(PlayerDropItemEvent event) {
        // TODO: Update location
    }

    @EventHandler
    public void onPlayerPickupPearl(PlayerPickupItemEvent event) {
        // TODO: Update location
    }

    @EventHandler
    public void onPlayerDisconnectWithPearl(PlayerQuitEvent event) {
        // TODO: Drop pearl on the ground
    }

    @EventHandler
    public void onChunkUnloadWithPearl(ChunkUnloadEvent event) {
        // TODO: Free pearl
    }

    @EventHandler
    public void onPearlCombust(EntityCombustEvent event) {
        // TODO: Free pearl
    }

    @EventHandler
    public void onInventoryClickPearl(InventoryClickEvent event) {
        // TODO: Update location
    }

    @EventHandler
    public void onPearlItemSpawn(ItemSpawnEvent event) {
        // TODO: Update location
    }

    @EventHandler
    public void onPearlItemDamage(PlayerItemDamageEvent event) {
        // TODO: Free pearl
    }

    @EventHandler
    public void onPearlDespawn(ItemDespawnEvent event) {
        // TODO: Free pearl
    }
}
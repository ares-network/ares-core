package com.llewkcor.ares.core.spawn;

import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.location.PLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.spawn.data.SpawnDAO;
import com.llewkcor.ares.core.spawn.data.SpawnData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

@AllArgsConstructor
public final class SpawnHandler {
    @Getter public final SpawnManager manager;

    /**
     * Save all spawn data in memory to the MongoDB instance
     * @param blocking Block the thread
     */
    public void saveAll(boolean blocking) {
        if (blocking) {
            Logger.warn("Blocking the thread while attempting to save all Spawn Data to the database");
            SpawnDAO.saveSpawnData(manager.getPlugin().getDatabaseInstance(), manager.getSpawnData());
            Logger.print("Saved " + manager.getSpawnData().size() + " Spawn Data Instances to the database");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            SpawnDAO.saveSpawnData(manager.getPlugin().getDatabaseInstance(), manager.getSpawnData());
            new Scheduler(manager.getPlugin()).sync(() -> Logger.print("Saved " + manager.getSpawnData().size() + " Spawn Data Instances")).run();
        }).run();
    }

    /**
     * Randomly place a player on the map
     * @param player Player
     * @param promise Promise
     */
    public void randomlySpawn(Player player, SimplePromise promise) {
        final SpawnData spawnData = manager.getSpawnData(player);

        if (spawnData == null) {
            promise.fail("Failed to obtain your spawn data");
            return;
        }

        if (spawnData.isSpawned()) {
            promise.fail("You have already randomly spawned");
            return;
        }

        manager.getTeleportRequests().remove(player.getUniqueId());

        preparePlayer(player);

        final BLocatable locatable = manager.getRandomSpawnLocation();
        final Location location = locatable.getBukkit().getLocation();

        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }

        player.teleport(manager.getRandomSpawnLocation().getBukkit().getLocation());

        spawnData.setSpawned(true);

        promise.success();
    }

    /**
     * Handles sending a teleport request
     * @param player Sending Player
     * @param username Receiving Username
     * @param promise Promise
     */
    public void sendTeleportRequest(Player player, String username, SimplePromise promise) {
        final SpawnData spawnData = manager.getSpawnData(player);
        final UUID existing = manager.getTeleportRequest(player);

        if (!player.hasPermission("arescore.spawn.ott")) {
            // TODO: Update premium link
            promise.fail("This feature is for premium members only");
            return;
        }

        if (spawnData == null) {
            promise.fail("Failed to obtain your spawn data");
            return;
        }

        if (spawnData.isSpawned()) {
            promise.fail("You have already randomly spawned");
            return;
        }

        if (existing != null) {
            promise.fail("You have already sent a teleport request");
            return;
        }

        final Player found = Bukkit.getPlayer(username);

        if (found == null) {
            promise.fail("Player not found");
            return;
        }

        manager.getTeleportRequests().put(player.getUniqueId(), found.getUniqueId());
        found.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GOLD + " has requested to teleport to your location from spawn. Type '" + ChatColor.YELLOW + "/spawn accept " + player.getName() + ChatColor.GOLD + "' to summon them.");

        promise.success();
    }

    /**
     * Handles accepting a teleport request
     * @param player Accepting player
     * @param username Accepted username
     * @param promise Promise
     */
    public void acceptTeleportRequest(Player player, String username, SimplePromise promise) {
        final Player summonPlayer = Bukkit.getPlayer(username);

        if (summonPlayer == null) {
            promise.fail("Player not found");
            return;
        }

        final SpawnData spawnData = manager.getSpawnData(player);
        final SpawnData summonData = manager.getSpawnData(summonPlayer);

        if (spawnData == null) {
            promise.fail("Failed to obtain your spawn data");
            return;
        }

        if (summonData == null) {
            promise.fail("Failed to obtain the summoning player's spawn data");
            return;
        }

        final UUID requestId = manager.getTeleportRequests().get(summonPlayer.getUniqueId());

        if (requestId == null || !player.getUniqueId().equals(requestId)) {
            promise.fail(summonPlayer.getName() + " has not requested to teleport to you");
            return;
        }

        if (!spawnData.isSpawned()) {
            promise.fail("You need to leave spawn to accept this players summon request");
            return;
        }

        if (summonData.isSpawned()) {
            promise.fail(summonPlayer.getName() + " has already randomly spawned");
            return;
        }

        manager.getTeleportRequests().remove(requestId);

        preparePlayer(summonPlayer);

        summonPlayer.teleport(player);
        summonPlayer.sendMessage(ChatColor.GREEN + "Your teleport request has been accepted");

        summonData.setSpawned(true);

        promise.success();
    }

    /**
     * Updates the spawn to the current location of the provided Player
     * @param player Bukkit Player
     */
    public void updateSpawn(Player player) {
        final YamlConfiguration spawnConfig = Configs.getConfig(manager.getPlugin(), "spawn");

        spawnConfig.set("spawn-location.x", player.getLocation().getX());
        spawnConfig.set("spawn-location.y", player.getLocation().getY());
        spawnConfig.set("spawn-location.z", player.getLocation().getZ());
        spawnConfig.set("spawn-location.yaw", player.getLocation().getYaw());
        spawnConfig.set("spawn-location.pitch", player.getLocation().getPitch());
        spawnConfig.set("spawn-location.world", player.getLocation().getWorld().getName());

        Configs.saveConfig(manager.getPlugin(), "spawn", spawnConfig);

        manager.setSpawnLocation(new PLocatable(player));

        player.sendMessage(ChatColor.GREEN + "Spawn location has been updated");
    }

    /**
     * Cleans up player and gives them the starter kit
     * @param player Bukkit Player
     */
    private void preparePlayer(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
        player.setFireTicks(0);
        player.setNoDamageTicks(20);
        player.setLevel(0);
        player.setFlying(false);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        final ItemStack cookie = new ItemBuilder()
                .setMaterial(Material.COOKIE)
                .setAmount(16)
                .build();

        final ItemStack fishingRod = new ItemBuilder()
                .setMaterial(Material.FISHING_ROD)
                .addEnchant(Enchantment.LURE, 1)
                .build();

        player.getInventory().addItem(fishingRod);
        player.getInventory().addItem(cookie);

        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60 * 20, 0, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 3, false));
    }
}
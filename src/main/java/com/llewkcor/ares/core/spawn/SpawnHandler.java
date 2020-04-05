package com.llewkcor.ares.core.spawn;

import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.location.PLocatable;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.player.data.account.AresAccount;
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
     * Randomly place a player on the map
     * @param player Player
     * @param promise Promise
     */
    public void randomlySpawn(Player player, SimplePromise promise) {
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (account.isSpawned()) {
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

        account.setSpawned(true);

        promise.success();
    }

    /**
     * Handles spawning the provided player at their Bed spawn location
     * @param player Player
     * @param promise Promise
     */
    public void spawnBed(Player player, SimplePromise promise) {
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());

        if (account == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (account.isSpawned()) {
            promise.fail("You have already randomly spawned");
            return;
        }

        final Location location = player.getBedSpawnLocation();

        if (location == null) {
            promise.fail("You do not have a bed spawn location");
            return;
        }

        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }

        manager.getTeleportRequests().remove(player.getUniqueId());

        preparePlayer(player);

        player.teleport(location);

        account.setSpawned(true);

        promise.success();
    }

    /**
     * Handles sending a teleport request
     * @param player Sending Player
     * @param username Receiving Username
     * @param promise Promise
     */
    public void sendTeleportRequest(Player player, String username, SimplePromise promise) {
        final AresAccount account = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());
        final UUID existing = manager.getTeleportRequest(player);

        if (!player.hasPermission("arescore.spawn.ott")) {
            // TODO: Update premium link
            promise.fail("This feature is for premium members only");
            return;
        }

        if (account == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (account.isSpawned()) {
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

        if (found.getUniqueId().equals(player.getUniqueId())) {
            promise.fail("You can not send a teleport request to yourself");
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

        final AresAccount playerAccount = manager.getPlugin().getPlayerManager().getAccountByBukkitID(player.getUniqueId());
        final AresAccount summonAccount = manager.getPlugin().getPlayerManager().getAccountByBukkitID(summonPlayer.getUniqueId());

        if (playerAccount == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (summonAccount == null) {
            promise.fail("Failed to obtain the summoning player's account");
            return;
        }

        final UUID requestId = manager.getTeleportRequests().get(summonPlayer.getUniqueId());

        if (requestId == null || !player.getUniqueId().equals(requestId)) {
            promise.fail(summonPlayer.getName() + " has not requested to teleport to you");
            return;
        }

        if (!summonAccount.isSpawned()) {
            promise.fail("You need to leave spawn to accept this players summon request");
            return;
        }

        if (playerAccount.isSpawned()) {
            promise.fail(summonPlayer.getName() + " has already randomly spawned");
            return;
        }

        manager.getTeleportRequests().remove(requestId);

        preparePlayer(summonPlayer);

        summonPlayer.teleport(player);
        summonPlayer.sendMessage(ChatColor.GREEN + "Your teleport request has been accepted");

        playerAccount.setSpawned(true);

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
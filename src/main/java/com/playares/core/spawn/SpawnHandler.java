package com.playares.core.spawn;

import com.playares.commons.location.BLocatable;
import com.playares.commons.location.PLocatable;
import com.playares.commons.logger.Logger;
import com.playares.commons.promise.SimplePromise;
import com.playares.commons.util.general.Configs;
import com.playares.core.player.data.AresPlayer;
import com.playares.core.prison.data.PrisonPearl;
import com.playares.core.spawn.event.PlayerEnterWorldEvent;
import com.playares.core.spawn.menu.SpawnSelectorMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());

        if (profile == null) {
            promise.fail("Failed to obtain your account");
            Logger.error("Account service not found while trying to randomly spawn " + player.getName());
            return;
        }

        if (prisonPearl != null && !prisonPearl.isExpired() && !prisonPearl.isReleased()) {
            promise.fail("You can not spawn again until your Prison Pearl expires or you are set free");
            return;
        }

        if (profile.isSpawned()) {
            promise.fail("You have already randomly spawned");
            return;
        }

        manager.getTeleportRequests().remove(player.getUniqueId());

        preparePlayer(player, PlayerEnterWorldEvent.PlayerEnterWorldMethod.RANDOM);

        final BLocatable locatable = manager.getRandomSpawnLocation();
        final Location location = locatable.getBukkit().getLocation();

        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }

        player.teleport(manager.getRandomSpawnLocation().getBukkit().getLocation());

        profile.setSpawned(true);
        manager.getPlugin().getPlayerManager().setPlayer(false, profile);

        promise.success();
    }

    /**
     * Handles spawning the provided player at their Bed spawn location
     * @param player Player
     * @param promise Promise
     */
    public void spawnBed(Player player, SimplePromise promise) {
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());

        if (profile == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (prisonPearl != null && !prisonPearl.isExpired() && !prisonPearl.isReleased()) {
            promise.fail("You can not spawn again until your Prison Pearl expires or you are set free");
            return;
        }

        if (profile.isSpawned()) {
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
        preparePlayer(player, PlayerEnterWorldEvent.PlayerEnterWorldMethod.BED);
        player.teleport(location);

        profile.setSpawned(true);
        manager.getPlugin().getPlayerManager().setPlayer(false, profile);

        promise.success();
    }

    /**
     * Handles sending a teleport request
     * @param player Sending Player
     * @param username Receiving Username
     * @param promise Promise
     */
    public void sendTeleportRequest(Player player, String username, SimplePromise promise) {
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final PrisonPearl prisonPearl = manager.getPlugin().getPrisonPearlManager().getPrisonPearlByPlayer(player.getUniqueId());
        final UUID existing = manager.getTeleportRequest(player);

        if (!player.hasPermission("arescore.spawn.ott")) {
            // TODO: Update premium link
            promise.fail("This feature is for premium members only");
            return;
        }

        if (profile == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (prisonPearl != null && !prisonPearl.isExpired() && !prisonPearl.isReleased()) {
            promise.fail("You can not spawn again until your Prison Pearl expires or you are set free");
            return;
        }

        if (profile.isSpawned()) {
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

        found.spigot().sendMessage(new ComponentBuilder(player.getName()).color(net.md_5.bungee.api.ChatColor.BLUE)
        .append(" has requested to teleport to your location from spawn. Type").color(net.md_5.bungee.api.ChatColor.YELLOW)
        .append("/spawn accept " + player.getName()).color(net.md_5.bungee.api.ChatColor.GOLD)
        .append("to summon them to your location or ").color(net.md_5.bungee.api.ChatColor.YELLOW)
        .append("[Click Here]").color(net.md_5.bungee.api.ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spawn accept " + player.getName())).create());

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

        final AresPlayer playerProfile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final AresPlayer summonProfile = manager.getPlugin().getPlayerManager().getPlayer(summonPlayer.getUniqueId());

        if (playerProfile == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (summonProfile == null) {
            promise.fail("Failed to obtain the summoning player's account");
            return;
        }

        final UUID requestId = manager.getTeleportRequests().get(summonPlayer.getUniqueId());

        if (requestId == null || !player.getUniqueId().equals(requestId)) {
            promise.fail(summonPlayer.getName() + " has not requested to teleport to you");
            return;
        }

        if (!playerProfile.isSpawned()) {
            promise.fail("You need to leave spawn to accept this players summon request");
            return;
        }

        if (summonProfile.isSpawned()) {
            promise.fail(summonPlayer.getName() + " has already randomly spawned");
            return;
        }

        manager.getTeleportRequests().remove(requestId);
        preparePlayer(summonPlayer, PlayerEnterWorldEvent.PlayerEnterWorldMethod.REQUEST);
        summonPlayer.teleport(player);
        summonPlayer.sendMessage(ChatColor.GREEN + "Your teleport request has been accepted");

        summonProfile.setSpawned(true);
        manager.getPlugin().getPlayerManager().setPlayer(false, summonProfile);

        promise.success();
    }

    /**
     * Handles opening the spawn selector menu
     * @param player
     * @param promise
     */
    public void openSelectorMenu(Player player, SimplePromise promise) {
        final AresPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            promise.fail("Failed to obtain your account");
            return;
        }

        if (profile.isSpawned()) {
            promise.fail("You have already spawned in");
            return;
        }

        final SpawnSelectorMenu menu = new SpawnSelectorMenu(manager.getPlugin(), player);
        menu.open();
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
    private void preparePlayer(Player player, PlayerEnterWorldEvent.PlayerEnterWorldMethod entranceMethod) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
        player.setFireTicks(0);
        player.setNoDamageTicks(20);
        player.setLevel(0);
        player.setFlying(false);

        final PlayerEnterWorldEvent event = new PlayerEnterWorldEvent(player, entranceMethod);
        Bukkit.getPluginManager().callEvent(event);
    }
}
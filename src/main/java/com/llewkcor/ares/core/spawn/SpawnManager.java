package com.llewkcor.ares.core.spawn;

import com.google.common.collect.Maps;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.location.PLocatable;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.general.Configs;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.spawn.listener.SpawnListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class SpawnManager {
    @Getter public final Ares plugin;
    @Getter public final SpawnHandler handler;
    @Getter public Map<UUID, UUID> teleportRequests;

    @Getter @Setter public PLocatable spawnLocation;
    @Getter public String mainWorldName;
    @Getter @Setter public int randomSpawnRadius;

    public SpawnManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new SpawnHandler(this);
        this.teleportRequests = Maps.newConcurrentMap();

        Bukkit.getPluginManager().registerEvents(new SpawnListener(this), plugin);

        final YamlConfiguration spawnConfig = Configs.getConfig(plugin, "spawn");

        final double x = spawnConfig.getDouble("spawn-location.x");
        final double y = spawnConfig.getDouble("spawn-location.y");
        final double z = spawnConfig.getDouble("spawn-location.z");
        final float yaw = (float)spawnConfig.getDouble("spawn-location.yaw");
        final float pitch = (float)spawnConfig.getDouble("spawn-location.pitch");
        final String worldName = spawnConfig.getString("spawn-location.world");
        this.spawnLocation = new PLocatable(worldName, x, y, z, yaw, pitch);

        this.randomSpawnRadius = spawnConfig.getInt("settings.random-spawn-radius");
        this.mainWorldName = spawnConfig.getString("settings.main-world");

        if (Bukkit.getWorld(mainWorldName) == null) {
            Logger.error("Failed to find main world! This is about to be FUCKED");
            return;
        }
    }

    /**
     * Returns a new Random Spawn position
     * @return Random Spawn Position
     */
    public BLocatable getRandomSpawnLocation() {
        final Random random = new Random();
        final World world = Bukkit.getWorld(mainWorldName);
        final int x = random.nextInt(randomSpawnRadius);
        final int z = random.nextInt(randomSpawnRadius);
        final int y = world.getHighestBlockYAt(x, z);

        return new BLocatable(world.getName(), x, y, z);
    }

    /**
     * Returns the UUID of the player the provided Bukkit Player requested to teleport to
     * @param player Bukkit Player
     * @return Bukkit UUID
     */
    public UUID getTeleportRequest(Player player) {
        return teleportRequests.get(player.getUniqueId());
    }
}

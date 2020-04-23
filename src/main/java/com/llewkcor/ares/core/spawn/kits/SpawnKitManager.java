package com.llewkcor.ares.core.spawn.kits;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.spawn.SpawnManager;
import com.llewkcor.ares.core.spawn.kits.data.SpawnKit;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class SpawnKitManager {
    @Getter public final SpawnManager manager;
    @Getter public final SpawnKitHandler handler;
    @Getter public final Set<SpawnKit> kitRepository;
    @Getter public Map<UUID, Long> kitCooldowns;

    @Getter @Setter protected boolean spawnKitsEnabled;
    @Getter @Setter protected int spawnKitObtainCooldown;

    public SpawnKitManager(SpawnManager manager) {
        this.manager = manager;
        this.handler = new SpawnKitHandler(this);
        this.kitRepository = Sets.newHashSet();
        this.kitCooldowns = Maps.newConcurrentMap();
    }

    /**
     * Returns the default kit
     * @return Default SpawnKit
     */
    public SpawnKit getDefaultKit() {
        return kitRepository.stream().filter(SpawnKit::isDefault).findFirst().orElse(null);
    }

    /**
     * Returns a kit matching the provided name
     * @param name Name
     * @return SpawnKit
     */
    public SpawnKit getKit(String name) {
        return kitRepository.stream().filter(kit -> kit.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Returns the highest SpawnKit a player can receive
     * Will return the default kit if they have no kits
     * @param player Player
     * @return SpawnKit
     */
    public SpawnKit getKit(Player player) {
        final List<SpawnKit> kits = kitRepository.stream().filter(kit -> kit.getPermission() != null && player.hasPermission(kit.getPermission())).collect(Collectors.toList());

        if (kits.isEmpty()) {
            return getDefaultKit();
        }

        kits.sort(Comparator.comparingInt(SpawnKit::getWeight));
        Collections.reverse(kits);

        return kits.get(0);
    }

    /**
     * Returns true if the provided player has a kit cooldown still
     * @param player Bukkit Player
     * @return True if on cooldown
     */
    public boolean hasKitCooldown(Player player) {
        return getKitCooldown(player) > Time.now();
    }

    /**
     * Returns the expiration time for the provided players kit cooldown
     * @param player Bukkit Player
     * @return Time in ms
     */
    public long getKitCooldown(Player player) {
        return kitCooldowns.getOrDefault(player.getUniqueId(), 0L);
    }
}
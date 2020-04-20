package com.llewkcor.ares.core.spawn.kits;

import com.google.common.collect.Sets;
import com.llewkcor.ares.core.spawn.SpawnManager;
import com.llewkcor.ares.core.spawn.kits.data.SpawnKit;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SpawnKitManager {
    @Getter public final SpawnManager manager;
    @Getter public final SpawnKitHandler handler;
    @Getter public final Set<SpawnKit> kitRepository;

    public SpawnKitManager(SpawnManager manager) {
        this.manager = manager;
        this.handler = new SpawnKitHandler(this);
        this.kitRepository = Sets.newHashSet();
    }

    /**
     * Returns the default kit
     * @return Default SpawnKit
     */
    public SpawnKit getDefaultKit() {
        return kitRepository.stream().filter(kit -> kit.getPermission() == null).findFirst().orElse(null);
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
}
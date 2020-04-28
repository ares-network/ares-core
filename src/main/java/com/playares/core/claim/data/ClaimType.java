package com.playares.core.claim.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@AllArgsConstructor
public enum ClaimType {
    STONE("Stone", 100, 600, Material.STONE),
    IRON("Iron", 600, 1800, Material.IRON_INGOT),
    DIAMOND("Diamond", 1200, 10800, Material.DIAMOND);

    @Getter public final String displayName;
    @Getter public final int durability;
    @Getter public final int matureTimeInSeconds;
    @Getter public final Material material;
}
package com.llewkcor.ares.core.loggers.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public final class CombatLogger extends EntityVillager {
    @Getter public final UUID ownerId;
    @Getter public final String ownerUsername;
    @Getter public final List<ItemStack> inventory;

    public CombatLogger(World world, Player player) {
        super(world);
        this.ownerId = player.getUniqueId();
        this.ownerUsername = player.getName();
        this.inventory = Lists.newArrayList();

        final CraftLivingEntity living = (CraftLivingEntity)getBukkitEntity();

        setCustomName(ChatColor.GRAY + "(Combat-Logger) " + ChatColor.RED + ownerUsername);
        setCustomNameVisible(true);
        setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            this.inventory.add(item);
        }

        living.setHealth(player.getHealth());
        living.setFallDistance(player.getFallDistance());
        living.setNoDamageTicks(player.getNoDamageTicks());
        living.setFireTicks(player.getFireTicks());
        living.setRemainingAir(player.getRemainingAir());

        if (living.getEquipment() != null && player.getEquipment() != null) {
            living.getEquipment().setHelmet(player.getEquipment().getHelmet());
            living.getEquipment().setChestplate(player.getEquipment().getChestplate());
            living.getEquipment().setLeggings(player.getEquipment().getLeggings());
            living.getEquipment().setBoots(player.getEquipment().getBoots());
        }

        player.getActivePotionEffects().forEach(living::addPotionEffect);

        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
    }

    @Override
    public void move(double d0, double d1, double d2) {
        super.move(0.0, d1, 0.0);

        if (this.motY > 0.0) {
            this.motY = 0.0;
        }
    }

    @Override
    public void g(double d0, double d1, double d2) {}

    @Override
    public void collide(Entity entity) {}

    public void reapply(Player player) {
        final CraftLivingEntity living = (CraftLivingEntity)getBukkitEntity();

        if (player.getEquipment() != null && living.getEquipment() != null) {
            player.getEquipment().setHelmet(living.getEquipment().getHelmet());
            player.getEquipment().setChestplate(living.getEquipment().getChestplate());
            player.getEquipment().setLeggings(living.getEquipment().getLeggings());
            player.getEquipment().setBoots(living.getEquipment().getBoots());
        }

        player.setHealth(living.getHealth());
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.teleport(living.getLocation());
        player.setFallDistance(living.getFallDistance());
        player.setFireTicks(living.getFireTicks());
        player.setRemainingAir(living.getRemainingAir());

        living.getActivePotionEffects().stream().filter(effect -> effect.getDuration() < 25000).forEach(player::addPotionEffect);
        living.remove();
    }

    public void spawn() {
        world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public void dropItems(Location location) {
        for (ItemStack item : inventory) {
            location.getWorld().dropItem(location, item);
        }
    }
}
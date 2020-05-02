package com.playares.core.listener;

import com.playares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

@AllArgsConstructor
public final class DeathMessageListener implements Listener {
    @Getter public final Ares plugin;

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player slain = event.getEntity();
        final Player killer = slain.getKiller();
        final EntityDamageEvent.DamageCause reason = slain.getLastDamageCause().getCause();
        final String prefix = ChatColor.RED + "RIP:" + ChatColor.RESET;
        final String slainUsername = ChatColor.GOLD + slain.getName() + ChatColor.RESET;
        final ChatColor cA = ChatColor.RED;
        final ChatColor cB = ChatColor.BLUE;

        slain.getWorld().strikeLightningEffect(slain.getLocation());

        if (killer != null) {
            final String killerUsername = ChatColor.GOLD + killer.getName() + ChatColor.RESET;
            String hand = ChatColor.RESET + "their fists";

            if (killer.getItemInHand() != null && !killer.getItemInHand().getType().equals(Material.AIR)) {
                if (killer.getItemInHand().hasItemMeta() && killer.getItemInHand().getItemMeta().hasDisplayName()) {
                    hand = ChatColor.GRAY + "[" + killer.getItemInHand().getItemMeta().getDisplayName() + ChatColor.GRAY + "]";
                } else {
                    hand = ChatColor.RESET + StringUtils.capitaliseAllWords(killer.getItemInHand().getType().name().replace("_", " ").toLowerCase());
                }
            }

            final String defaultDeathMessage = prefix + " " + slainUsername + cA + " slain by " + killerUsername + cA + " while using " + hand;

            if (reason.equals(EntityDamageEvent.DamageCause.FALL)) {
                final double distance = Math.floor(slain.getFallDistance());

                if (distance > 3.0) {
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " fell " + cB + distance + " blocks" + cA + " to their death while fighting " + killerUsername);
                } else {
                    event.setDeathMessage(defaultDeathMessage);
                }

                return;
            }

            if (reason.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                event.setDeathMessage(defaultDeathMessage);
                return;
            }

            if (reason.equals(EntityDamageEvent.DamageCause.PROJECTILE) && slain.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                final EntityDamageByEntityEvent pveEvent = (EntityDamageByEntityEvent)slain.getLastDamageCause();
                final Projectile projectile = (Projectile)pveEvent.getDamager();

                if (projectile.getShooter() instanceof LivingEntity) {
                    final LivingEntity shooter = (LivingEntity)projectile.getShooter();
                    final String distance = String.format("%.2f", shooter.getLocation().distance(slain.getLocation()));

                    event.setDeathMessage(prefix + " " + slainUsername + cA + " was shot and killed by " + killerUsername + cA + " from a distance of " + cB + distance + " blocks");

                    return;
                }
            }

            switch (reason) {
                case FIRE:
                case FIRE_TICK:
                case LAVA:
                case MELTING:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " burned to death while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case MAGIC:
                case CUSTOM:
                case SUICIDE:
                case POISON:
                case LIGHTNING:
                case THORNS:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " died by magic while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case DROWNING:
                case SUFFOCATION:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " suffocated while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " blew up while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case VOID: event.setDeathMessage(prefix + " " + slainUsername + cA + " fell in to the void while fighting " + killerUsername + cA + " while using " + hand); break;
                case WITHER: event.setDeathMessage(prefix + " " + slainUsername + cA + " withered away while fighting " + killerUsername + cA + " while using " + hand); break;
                case STARVATION: event.setDeathMessage(prefix + " " + slainUsername + cA + " starved to death while fighting " + killerUsername + cA + " while using " + hand); break;
                default: event.setDeathMessage(defaultDeathMessage); break;
            }

            return;
        }

        if (reason.equals(EntityDamageEvent.DamageCause.FALL)) {
            final double distance = Math.floor(slain.getFallDistance());

            if (distance < 1.0) {
                event.setDeathMessage(prefix + " " + slainUsername + cA + " fell " + cB + distance + " blocks" + cA + " to their death");
            } else {
                event.setDeathMessage(prefix + " " + slainUsername + cA + " died");
            }

            return;
        }

        switch (reason) {
            case FIRE:
            case FIRE_TICK:
            case LAVA:
            case MELTING:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " burned to death");
                break;
            case MAGIC:
            case CUSTOM:
            case POISON:
            case LIGHTNING:
            case THORNS:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " died by magic");
                break;
            case DROWNING:
            case SUFFOCATION:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " suffocated");
                break;
            case ENTITY_ATTACK:
            case ENTITY_EXPLOSION:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " blew up");
                break;
            case VOID: event.setDeathMessage(prefix + " " + slainUsername + cA + " fell in to the void"); break;
            case WITHER: event.setDeathMessage(prefix + " " + slainUsername + cA + " withered away"); break;
            case STARVATION: event.setDeathMessage(prefix + " " + slainUsername + cA + " starved to death"); break;
            default: event.setDeathMessage(prefix + " " + slainUsername + cA + " died"); break;
        }
    }
}

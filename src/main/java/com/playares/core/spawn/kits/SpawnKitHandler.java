package com.playares.core.spawn.kits;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.item.ItemBuilder;
import com.playares.commons.logger.Logger;
import com.playares.commons.remap.RemappedEffect;
import com.playares.commons.remap.RemappedEnchantment;
import com.playares.commons.util.general.Configs;
import com.playares.core.spawn.kits.data.SpawnKit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class SpawnKitHandler {
    @Getter public final SpawnKitManager manager;

    /**
     * Loads all spawn kits from file to memory
     */
    public void load() {
        manager.getKitRepository().clear();

        final YamlConfiguration config = Configs.getConfig(manager.getManager().getPlugin(), "starter-kits");

        manager.setSpawnKitsEnabled(config.getBoolean("settings.enabled"));
        manager.setSpawnKitObtainCooldown(config.getInt("settings.obtain_cooldown"));

        for (String kitIdentifier : config.getConfigurationSection("kits").getKeys(false)) {
            final String path = "kits." + kitIdentifier + ".";
            final String displayName;
            List<ItemStack> items = Lists.newArrayList();
            List<PotionEffect> effects = Lists.newArrayList();
            String permission = null;
            int weight = 0;

            if (config.get(path + "display_name") != null) {
                displayName = ChatColor.translateAlternateColorCodes('&', config.getString(path + "display_name"));
            } else {
                displayName = kitIdentifier;
            }

            if (config.get(path + "permission") != null) {
                permission = config.getString(path + "permission");
            }

            if (config.get(path + "weight") != null) {
                weight = config.getInt(path + "weight");
            }

            if (config.get(path + "items") != null) {
                items = Lists.newArrayList();

                for (String itemId : config.getConfigurationSection(path + "items").getKeys(false)) {
                    final Material material = Material.getMaterial(config.getInt(path + "items." + itemId + ".id"));
                    final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
                    String name = null;
                    int amount = 1;
                    int data = 0;
                    Color color = null;

                    if (config.get(path + "items." + itemId + ".name") != null) {
                        name = ChatColor.translateAlternateColorCodes('&', config.getString(path + "items." + itemId + ".name"));
                    }

                    if (config.get(path + "items." + itemId + ".amount") != null) {
                        amount = config.getInt(path + "items." + itemId + ".amount");
                    }

                    if (config.get(path + "items." + itemId + ".data") != null) {
                        data = config.getInt(path + "items." + itemId + ".data");
                    }

                    if (config.get(path + "items." + itemId + ".color") != null) {
                        final int r = config.getInt(path + "items." + itemId + ".color.r");
                        final int g = config.getInt(path + "items." + itemId + ".color.g");
                        final int b = config.getInt(path + "items." + itemId + ".color.b");
                        color = Color.fromRGB(r, g, b);
                    }

                    if (config.get(path + "items." + itemId + ".enchantments") != null) {
                        for (String enchantmentIdentifier : config.getConfigurationSection(path + "items." + itemId + ".enchantments").getKeys(false)) {
                            final Enchantment enchantment = RemappedEnchantment.getEnchantmentByName(enchantmentIdentifier);
                            final int level = config.getInt(path + "items." + itemId + ".enchantments." + enchantmentIdentifier);

                            if (enchantment == null) {
                                Logger.error("Invalid enchantment for " + kitIdentifier + ":" + itemId + ":" + enchantmentIdentifier);
                                continue;
                            }

                            enchantments.put(enchantment, level);
                        }
                    }

                    final ItemStack item;

                    if (name != null) {
                        item = new ItemBuilder()
                                .setMaterial(material)
                                .setName(name)
                                .setData((short)data)
                                .setAmount(amount)
                                .addEnchant(enchantments)
                                .build();
                    } else {
                        item = new ItemBuilder()
                                .setMaterial(material)
                                .setData((short)data)
                                .setAmount(amount)
                                .addEnchant(enchantments)
                                .build();
                    }

                    if (color != null) {
                        final ItemMeta meta = item.getItemMeta();
                        final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)meta;

                        leatherArmorMeta.setColor(color);
                        item.setItemMeta(leatherArmorMeta);
                    }

                    items.add(item);
                }
            }

            if (config.get(path + "effects") != null) {
                effects = Lists.newArrayList();

                for (String potionEffectId : config.getConfigurationSection(path + "effects").getKeys(false)) {
                    final PotionEffectType type = RemappedEffect.getEffectTypeByName(potionEffectId);
                    final int time = config.getInt(path + "effects." + potionEffectId + ".time");
                    final int amplifier = config.getInt(path + "effects." + potionEffectId + ".amplifier");

                    if (type == null) {
                        Logger.error("Invalid potion effect for " + kitIdentifier + ":" + potionEffectId);
                        continue;
                    }

                    final PotionEffect effect = new PotionEffect(type, (time * 20), amplifier);
                    effects.add(effect);
                }
            }

            final SpawnKit kit = new SpawnKit(kitIdentifier, displayName, permission, effects, items, weight);
            manager.getKitRepository().add(kit);
        }

        Logger.print("Loaded " + manager.getKitRepository().size() + " Spawn Kits");
    }
}

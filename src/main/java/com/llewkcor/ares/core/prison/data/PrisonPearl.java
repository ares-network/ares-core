package com.llewkcor.ares.core.prison.data;

import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.connect.mongodb.MongoDocument;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class PrisonPearl implements MongoDocument<PrisonPearl> {
    @Getter public UUID uniqueId;
    @Getter public String imprisonedUsername;
    @Getter public UUID imprisonedUUID;
    @Getter public String killerUsername;
    @Getter public UUID killerUUID;
    @Getter public long createTime;
    @Getter @Setter public long expireTime;
    @Getter @Setter public BLocatable location;
    @Getter @Setter public PearlLocationType locationType;
    @Getter @Setter public Item trackedItem;

    public PrisonPearl() {
        this.uniqueId = UUID.randomUUID();
        this.imprisonedUsername = null;
        this.imprisonedUUID = null;
        this.killerUUID = null;
        this.killerUsername = null;
        this.createTime = Time.now();
        this.expireTime = Time.now();
        this.location = null;
        this.locationType = null;
        this.trackedItem = null;
    }

    public PrisonPearl(String imprisonedUsername, UUID imprisonedUUID, String killerUsername, UUID killerUUID, long expireTime, BLocatable location, PearlLocationType locationType) {
        this.uniqueId = UUID.randomUUID();
        this.imprisonedUsername = imprisonedUsername;
        this.imprisonedUUID = imprisonedUUID;
        this.killerUsername = killerUsername;
        this.killerUUID = killerUUID;
        this.createTime = Time.now();
        this.expireTime = expireTime;
        this.location = location;
        this.locationType = locationType;
        this.trackedItem = null;
    }

    /**
     * Returns true if this Prison Pearl is expired
     * @return True if expired
     */
    public boolean isExpired() {
        return Time.now() >= expireTime;
    }

    /**
     * Returns the most up-to-date location for this Prison Pearl
     * @return Bukkit Location
     */
    public Location getBukkitLocation() {
        if (trackedItem != null) {
            return trackedItem.getLocation();
        }

        return location.getBukkit().getLocation();
    }

    /**
     * Returns a copy of the Bukkit ItemStack
     * @return Bukkit ItemStack
     */
    public ItemStack getItem() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.DARK_PURPLE + "Prison Pearl");
        lore.add(ChatColor.GOLD + "Imprisoned on: " + ChatColor.GRAY + Time.convertToSimpleDate(new Date(createTime)));
        lore.add(ChatColor.GOLD + "Imprisoned by: " + ChatColor.GRAY + killerUsername);
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.DARK_GRAY + uniqueId.toString());

        return new ItemBuilder()
                .setMaterial(Material.ENDER_PEARL)
                .setName(ChatColor.AQUA + "" + ChatColor.ITALIC + imprisonedUsername)
                .addFlag(ItemFlag.HIDE_ENCHANTS)
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                .addLore(lore)
                .build();
    }

    /**
     * Returns Bukkit Player instance for the killer if they're available
     * @return Bukkit Player
     */
    public Player getKiller() {
        return Bukkit.getPlayer(killerUUID);
    }

    /**
     * Returns Bukkit Player instance for the imprisoned if they're available
     * @return Bukkit Player
     */
    public Player getImprisoned() {
        return Bukkit.getPlayer(imprisonedUUID);
    }

    /**
     * Returns true if the provided ItemStack matches this Prison Pearl
     * @param item Bukkit ItemStack
     * @return True if matches
     */
    public boolean match(ItemStack item) {
        if (item == null || !item.getType().equals(Material.ENDER_PEARL) || !item.hasItemMeta()) {
            return false;
        }

        final ItemMeta meta = item.getItemMeta();

        if (!meta.hasLore()) {
            return false;
        }

        for (String loreLine : meta.getLore()) {
            if (ChatColor.stripColor(loreLine).equals(uniqueId.toString())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public PrisonPearl fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.imprisonedUsername = document.getString("imprisoned_username");
        this.imprisonedUUID = (UUID)document.get("imprisoned_id");
        this.killerUsername = document.getString("killer_username");
        this.killerUUID = (UUID)document.get("killer_id");
        this.createTime = document.getLong("created");
        this.expireTime = document.getLong("expire");
        this.location = new BLocatable().fromDocument(document.get("location", Document.class));
        this.locationType = PearlLocationType.valueOf(document.getString("location_type"));

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("username", imprisonedUsername)
                .append("imprisoned_id", imprisonedUUID)
                .append("killer_username", killerUsername)
                .append("killer_id", killerUUID)
                .append("created", createTime)
                .append("expire", expireTime)
                .append("location", location.toDocument())
                .append("location_type", locationType);
    }
}
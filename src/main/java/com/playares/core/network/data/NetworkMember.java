package com.playares.core.network.data;

import com.google.common.collect.Lists;
import com.playares.commons.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class NetworkMember implements MongoDocument<NetworkMember> {
    @Getter UUID uniqueId;
    @Getter @Setter public String username;
    @Getter public List<NetworkPermission> grantedPermissions;

    NetworkMember() {
        this.uniqueId = null;
        this.username = null;
        this.grantedPermissions = Collections.synchronizedList(Lists.newArrayList());

        for (NetworkPermission permission : NetworkPermission.values()) {
            if (permission.isDefaultValue()) {
                grantedPermissions.add(permission);
            }
        }
    }

    NetworkMember(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.grantedPermissions = Collections.synchronizedList(Lists.newArrayList());

        for (NetworkPermission permission : NetworkPermission.values()) {
            if (permission.isDefaultValue()) {
                grantedPermissions.add(permission);
            }
        }
    }

    NetworkMember(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.grantedPermissions = Collections.synchronizedList(Lists.newArrayList());

        for (NetworkPermission permission : NetworkPermission.values()) {
            if (permission.isDefaultValue()) {
                grantedPermissions.add(permission);
            }
        }
    }

    /**
     * Adds the provided Network Permission to this players granted permissions
     * @param permission Network Permission
     */
    public void grantPermission(NetworkPermission permission) {
        if (!grantedPermissions.contains(permission)) {
            grantedPermissions.add(permission);
        }
    }

    /**
     * Removes the provided Network Permission from this players granted permissions
     * @param permission Network Permission
     */
    public void revokePermission(NetworkPermission permission) {
        if (grantedPermissions.contains(permission)) {
            grantedPermissions.remove(permission);
        }
    }

    /**
     * Returns true if this member has access to the provided permission
     * @param permission Network Permission
     * @return True if granted
     */
    public boolean hasPermission(NetworkPermission permission) {
        return grantedPermissions.contains(permission);
    }

    /**
     * Returns the Bukkit Player instance for this member if they're online
     * @return Bukkit Player
     */
    public Player getBukkitPlayer() {
        if (Bukkit.getPlayer(uniqueId) != null && Bukkit.getPlayer(uniqueId).isOnline()) {
            return Bukkit.getPlayer(uniqueId);
        }

        return null;
    }

    @SuppressWarnings("unchecked") @Override
    public NetworkMember fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.username = document.getString("username");

        final List<String> grantedPermissionNames = (List<String>)document.get("permissions");

        for (String permissionName : grantedPermissionNames) {
            try {
                final NetworkPermission permission = NetworkPermission.valueOf(permissionName);

                if (!hasPermission(permission)) {
                    grantedPermissions.add(permission);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        return this;
    }

    @Override
    public Document toDocument() {
        final List<String> grantedPermissionNames = Lists.newArrayList();

        for (NetworkPermission permission : grantedPermissions) {
            grantedPermissionNames.add(permission.name());
        }

        return new Document()
                .append("id", uniqueId)
                .append("username", username)
                .append("permissions", grantedPermissionNames);
    }
}
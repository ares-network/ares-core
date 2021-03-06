package com.playares.core.network.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.connect.mongodb.MongoDocument;
import com.playares.commons.logger.Logger;
import com.playares.commons.util.general.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Network implements MongoDocument<Network> {
    @Getter public UUID uniqueId;
    @Getter @Setter public String name;
    @Getter public UUID creatorId;
    @Getter public long createDate;
    @Getter @Setter public long lastSeen;
    @Getter public Set<NetworkMember> members;
    @Getter public List<UUID> pendingMembers;
    @Getter public NetworkConfig configuration;

    public Network() {
        this.uniqueId = UUID.randomUUID();
        this.name = null;
        this.creatorId = null;
        this.createDate = Time.now();
        this.lastSeen = Time.now();
        this.members = Sets.newConcurrentHashSet();
        this.pendingMembers = Collections.synchronizedList(Lists.newArrayList());
        this.configuration = new NetworkConfig();
    }

    /**
     * Create a new network with a name and creator
     * @param name Network name
     * @param creator Bukkit Player
     */
    public Network(String name, Player creator) {
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.creatorId = creator.getUniqueId();
        this.createDate = Time.now();
        this.lastSeen = Time.now();
        this.members = Sets.newConcurrentHashSet();
        this.pendingMembers = Collections.synchronizedList(Lists.newArrayList());
        this.configuration = new NetworkConfig();

        final NetworkMember owner = new NetworkMember(creator);
        owner.getGrantedPermissions().add(NetworkPermission.ADMIN);

        members.add(owner);
    }

    /**
     * Returns true if the provided Bukkit Player is a member of this Network
     * @param player Bukkit Player
     * @return True if member
     */
    public boolean isMember(Player player) {
        return isMember(player.getUniqueId());
    }

    /**
     * Returns true if the provided Bukkit UUID is a member of this Network
     * @param uniqueId Bukkit UUID
     * @return True if member
     */
    public boolean isMember(UUID uniqueId) {
        return members.stream().anyMatch(member -> member.getUniqueId().equals(uniqueId));
    }

    /**
     * Returns true if the provided Bukkit username is a member of this Network
     *
     * Warning: This is not a perfectly trustworthy method of checking and should only be used for non-important features
     *
     * @param username Bukkit Username
     * @return True if member
     */
    public boolean isMember(String username) {
        return members.stream().anyMatch(member -> member.getUsername().equals(username));
    }

    /**
     * Returns a NetworkMember instance matching the provided Bukkit Player
     * @param player Bukkit Player
     * @return NetworkMember
     */
    public NetworkMember getMember(Player player) {
        return getMember(player.getUniqueId());
    }

    /**
     * Returns a NetworkMember instance matching the provided Bukkit UUID
     * @param uniqueId Bukkit UUID
     * @return NetworkMember
     */
    public NetworkMember getMember(UUID uniqueId) {
        return members.stream().filter(member -> member.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns a NetworkMember instance matching the provided Bukkit Username
     * @param username Bukkit Username
     * @return NetworkMember
     */
    public NetworkMember getMember(String username) {
        return members.stream().filter(member -> member.getUsername().equals(username)).findFirst().orElse(null);
    }

    /**
     * Returns an Immutable collection of all Network Members who are currently online
     * @return Immutable Collection of Members
     */
    public ImmutableList<NetworkMember> getOnlineMembers() {
        return ImmutableList.copyOf(members.stream().filter(member -> member.getBukkitPlayer() != null).collect(Collectors.toList()));
    }

    /**
     * Returns an Immutable collection of all Network members who match the provided permission
     * @param permission Network Permission
     * @return Immutable Collection of Members
     */
    public ImmutableList<NetworkMember> getMembersWithPermission(NetworkPermission permission) {
        return ImmutableList.copyOf(members.stream().filter(member -> member.hasPermission(permission)).collect(Collectors.toList()));
    }

    /**
     * Sends the provided message to all online members of this Network
     * @param message Message
     */
    public void sendMessage(String message) {
        getOnlineMembers().forEach(online -> {
            final Player player = online.getBukkitPlayer();

            if (player != null) {
                player.sendMessage(ChatColor.GOLD + "[" + ChatColor.YELLOW + name + ChatColor.GOLD + "] " + message);
            }
        });
    }

    /**
     * Sends a raw message to all online members of this Network
     * @param message Message
     */
    public void sendRawMessage(String message) {
        getOnlineMembers().forEach(online -> {
            final Player player = online.getBukkitPlayer();

            if (player != null) {
                player.sendMessage(message);
            }
        });
    }

    /**
     * Adds a new Bukkit Player to the members of this Network
     * @param player Bukkit Player
     */
    public void addMember(Player player) {
        final NetworkMember member = new NetworkMember(player);

        members.add(member);
        pendingMembers.remove(player.getUniqueId());

        Logger.print(player.getName() + " joined network " + name + "(" + uniqueId.toString() + ")");
    }

    /**
     * Adds a new member to the network using Bukkit UUID and Bukkit Username
     * @param uniqueId Bukkit UUID
     * @param username Bukkit Username
     */
    public void addMember(UUID uniqueId, String username) {
        final NetworkMember member = new NetworkMember(uniqueId, username);

        members.add(member);
        pendingMembers.remove(uniqueId);

        Logger.print(username + " joined network " + name + "(" + uniqueId.toString() + ")");
    }

    /**
     * Removes a player from this network
     * @param uniqueId Bukkit UUID
     */
    public void removeMember(UUID uniqueId) {
        final NetworkMember member = getMembers().stream().filter(members -> members.getUniqueId().equals(uniqueId)).findFirst().orElse(null);

        if (member == null) {
            return;
        }

        members.remove(member);
        Logger.print(member.getUsername() + "(" + member.getUniqueId().toString() + ") was removed from network " + name + "(" + uniqueId.toString() + ")");
    }

    @SuppressWarnings("unchecked") @Override
    public Network fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.name = document.getString("name");
        this.creatorId = (UUID)document.get("creator_id");
        this.createDate = document.getLong("create_date");
        this.lastSeen = document.getLong("last_seen");
        this.members = Sets.newConcurrentHashSet();
        this.pendingMembers = Collections.synchronizedList((List<UUID>)document.get("pending_members"));
        this.configuration = new NetworkConfig().fromDocument(document.get("config", Document.class));

        final List<Document> memberDocuments = document.get("members", List.class);
        memberDocuments.forEach(memberDocument -> members.add(new NetworkMember().fromDocument(memberDocument)));

        return this;
    }

    @Override
    public Document toDocument() {
        final List<Document> membersDocuments = Lists.newArrayList();
        members.forEach(member -> membersDocuments.add(member.toDocument()));

        return new Document()
                .append("id", uniqueId)
                .append("name", name)
                .append("creator_id", creatorId)
                .append("create_date", createDate)
                .append("last_seen", lastSeen)
                .append("members", membersDocuments)
                .append("pending_members", pendingMembers)
                .append("config", configuration.toDocument());
    }
}
package com.llewkcor.ares.core.network;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.llewkcor.ares.core.Ares;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.listener.NetworkListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class NetworkManager {
    @Getter public final Ares plugin;
    @Getter public final NetworkHandler handler;
    @Getter public final Set<Network> networkRepository;

    public NetworkManager(Ares plugin) {
        this.plugin = plugin;
        this.handler = new NetworkHandler(this);
        this.networkRepository = Sets.newConcurrentHashSet();

        Bukkit.getPluginManager().registerEvents(new NetworkListener(this), plugin);
    }

    /**
     * Returns a Network matching the provided Network UUID
     * @param uniqueId Network UUID
     * @return Network
     */
    public Network getNetworkByID(UUID uniqueId) {
        return networkRepository.stream().filter(network -> network.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns a Network matching the provided Network name
     * @param name Name
     * @return Network
     */
    public Network getNetworkByName(String name) {
        return networkRepository.stream().filter(network -> network.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Returns all networks the provided player is a member of
     * @param player Player
     * @return Immutable Collection of Networks
     */
    public ImmutableCollection<Network> getNetworksByPlayer(Player player) {
        return ImmutableList.copyOf(networkRepository.stream().filter(network -> network.isMember(player)).collect(Collectors.toList()));
    }

    /**
     * Returns all networks the provided player is a member of
     * @param uniqueId Bukkit UUID
     * @return Immutable Collection of Networks
     */
    public ImmutableCollection<Network> getNetworksByPlayer(UUID uniqueId) {
        return ImmutableList.copyOf(networkRepository.stream().filter(network -> network.isMember(uniqueId)).collect(Collectors.toList()));
    }

    /**
     * Returns all networks the provided player is a member of
     * @param username Bukkit Username
     * @return Immutable Collection of Networks
     */
    public ImmutableCollection<Network> getNetworksByPlayer(String username) {
        return ImmutableList.copyOf(networkRepository.stream().filter(network -> network.isMember(username)).collect(Collectors.toList()));
    }
}
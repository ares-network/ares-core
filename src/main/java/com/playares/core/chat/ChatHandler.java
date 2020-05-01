package com.playares.core.chat;

import com.playares.commons.promise.SimplePromise;
import com.playares.core.chat.data.ChatSession;
import com.playares.core.network.data.Network;
import com.playares.core.network.data.NetworkMember;
import com.playares.core.network.data.NetworkPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class ChatHandler {
    @Getter public final ChatManager manager;

    /**
     * Handles creating a new Chat Session
     * @param player Player
     * @param networkName Network Name
     * @param promise Promise
     */
    public void createSession(Player player, String networkName, SimplePromise promise) {
        final Network network = manager.getPlugin().getNetworkManager().getNetworkByName(networkName);
        final boolean admin = player.hasPermission("arescore.admin");

        if (network == null) {
            promise.fail("Network not found");
            return;
        }

        final NetworkMember member = network.getMember(player);

        if (!admin && member == null) {
            promise.fail("You are not in this network");
            return;
        }

        if (!admin && !(member.hasPermission(NetworkPermission.ADMIN) || member.hasPermission(NetworkPermission.ACCESS_CHAT))) {
            promise.fail("You do not have permission to perform this action");
            return;
        }

        final ChatSession existing = manager.getChatSession(player);

        if (existing != null && existing.getNetworkId().equals(network.getUniqueId())) {
            promise.fail("You are already speaking in " + network.getName());
            return;
        }

        final ChatSession session = new ChatSession(player.getUniqueId(), network.getUniqueId());
        manager.getChatSessions().add(session);

        if (existing != null) {
            manager.getChatSessions().remove(existing);
        }

        promise.success();
    }

    /**
     * Handles leaving a Chat Session for the provided Player
     * @param player Player
     */
    public void leaveSession(Player player) {
        final ChatSession session = manager.getChatSession(player);

        if (session != null) {
            manager.getChatSessions().remove(session);
        }
    }
}
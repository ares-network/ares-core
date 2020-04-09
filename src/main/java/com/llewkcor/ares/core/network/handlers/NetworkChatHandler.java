package com.llewkcor.ares.core.network.handlers;

import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.chat.data.ChatSession;
import com.llewkcor.ares.core.network.NetworkHandler;
import com.llewkcor.ares.core.network.data.Network;
import com.llewkcor.ares.core.network.data.NetworkMember;
import com.llewkcor.ares.core.network.data.NetworkPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class NetworkChatHandler {
    @Getter public final NetworkHandler handler;

    /**
     * Handles printing a rally point of the provided players location to their
     * currently active chat channel
     * @param player Player player
     * @param promise Promise
     */
    public void rally(Player player, SimplePromise promise) {
        final ChatSession session = handler.getManager().getPlugin().getChatManager().getChatSession(player);
        final boolean admin = player.hasPermission("arescore.admin");

        if (session == null) {
            promise.fail("You are not speaking in a network channel");
            return;
        }

        final Network network = handler.getManager().getPlugin().getNetworkManager().getNetworkByID(session.getNetworkId());

        if (network == null) {
            return;
        }

        final NetworkMember member = network.getMember(player);

        if (member == null && !admin) {
            handler.getManager().getPlugin().getChatManager().getHandler().leaveSession(player);
            player.sendMessage(ChatColor.RED + "You have been removed from this chat channel because you are no longer a member of " + network.getName());
            return;
        }

        if (member != null && !admin && !(member.hasPermission(NetworkPermission.ADMIN) && member.hasPermission(NetworkPermission.ACCESS_CHAT))) {
            handler.getManager().getPlugin().getChatManager().getHandler().leaveSession(player);
            player.sendMessage(ChatColor.RED + "You have been removed from this chat channel because you no longer have permission to access it");
            return;
        }

        network.sendMessage(ChatColor.GREEN + "[" + network.getName() + "] " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " is located at " + ChatColor.GOLD + new BLocatable(player.getLocation().getBlock()).toString());
    }
}

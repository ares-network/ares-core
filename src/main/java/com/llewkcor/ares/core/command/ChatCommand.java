package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("chat|c")
public final class ChatCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @CommandAlias("chat|c")
    @Description("Change your chat channel")
    @Syntax("<network name>")
    @CommandCompletion("@networks")
    public void onChat(Player player, String networkName) {
        plugin.getChatManager().getHandler().createSession(player, networkName, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You are now speaking in Network Chat");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("leave|public")
    @Description("Enter public chat")
    public void onLeave(Player player) {
        plugin.getChatManager().getHandler().leaveSession(player);
        player.sendMessage(ChatColor.GREEN + "You are speaking in Global Chat");
    }
}
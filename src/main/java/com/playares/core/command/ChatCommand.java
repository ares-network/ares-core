package com.playares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.promise.SimplePromise;
import com.playares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/" + help.getCommandName() + " help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}
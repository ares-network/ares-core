package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.llewkcor.ares.commons.promise.FailablePromise;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("prisonpearl|pp")
public final class PrisonPearlCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("locate")
    @Syntax("[player]")
    @Description("Locate your prison pearl")
    public void onLocate(Player player, @Optional String username) {
        plugin.getPrisonPearlManager().getHandler().locatePearl(player, username, new FailablePromise<String>() {
            @Override
            public void success(String s) {
                player.sendMessage(s);
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("free")
    @Syntax("<player>")
    @Description("Free a player from their prison pearl")
    @CommandPermission("arescore.prisonpearl.free")
    @CommandCompletion("@players")
    public void onFree(CommandSender sender, String username) {
        plugin.getPrisonPearlManager().getHandler().forceReleasePearl(sender, username, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Player has been released");
            }

            @Override
            public void fail(String s) {
                sender.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("info")
    @Syntax("[player]")
    @Description("View the information about your prison pearl")
    public void onInfo(Player player, @Optional String username) {
        plugin.getPrisonPearlManager().getHandler().lookupInfo(player, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("toggle|mute")
    @Description("Toggle Prison Pearl notification updates")
    public void onMute(Player player) {
        if (plugin.getPrisonPearlManager().isPearlNotificationsMuted(player)) {
            plugin.getPrisonPearlManager().getMutedPearlNotifications().remove(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY + "Prison Pearl notifications " + ChatColor.GREEN + "enabled");
        } else {
            plugin.getPrisonPearlManager().getMutedPearlNotifications().add(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY + "Prison Pearl notifications " + ChatColor.RED + "disabled");
        }
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/" + help.getCommandName() + " help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}
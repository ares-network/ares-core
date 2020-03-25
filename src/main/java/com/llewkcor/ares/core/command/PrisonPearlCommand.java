package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
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
    @Description("Locate your prison pearl")
    public void onLocate(Player player, @Optional String username) {

    }

    @Subcommand("free")
    @Syntax("<player>")
    @Description("Free a player from their prison pearl")
    @CommandPermission("arescore.prisonpearl.free")
    @CommandCompletion("@players")
    public void onFree(CommandSender sender, String username) {

    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/network help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}
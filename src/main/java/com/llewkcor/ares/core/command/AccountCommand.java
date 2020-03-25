package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("account|acc")
public final class AccountCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("create")
    @Description("Begin the account verification process")
    public void onCreate(Player player) {

    }

    @Subcommand("reset")
    @Description("Reset the password for your web account")
    public void onReset(Player player) {

    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/network help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}
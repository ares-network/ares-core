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
@CommandAlias("network|n|f|faction|t|team")
public final class NetworkCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("create")
    @Syntax("<network name>")
    @Description("Create a new network")
    public void onCreate(Player player, String name) {

    }

    @Subcommand("delete|del")
    @Syntax("<network name>")
    @Description("Delete an existing network")
    @CommandCompletion("@networks")
    public void onDelete(Player player, String name) {

    }

    @Subcommand("invite|inv")
    @Syntax("<player> <network name>")
    @Description("Invite a player to your network")
    @CommandCompletion("@players @networks")
    public void onInvite(Player player, String username, String network) {

    }

    @Subcommand("uninvite|uninv")
    @Syntax("<player> <network name>")
    @Description("Revoke an invitation to a network")
    @CommandCompletion("@players @networks")
    public void onUninvite(Player player, String username, String network) {

    }

    @Subcommand("accept|join")
    @Syntax("<network name> [password]")
    @Description("Join a network")
    public void onJoin(Player player, String network, @Optional String password) {

    }

    @Subcommand("leave")
    @Syntax("<network name>")
    @Description("Leave a network")
    @CommandCompletion("@networks")
    public void onLeave(Player player, String network) {

    }

    @Subcommand("show|who")
    @Syntax("<network name>")
    @Description("Print information for a network")
    public void onShow(Player player, String network) {

    }

    @Subcommand("kick")
    @Syntax("<player> <network name>")
    @Description("Kick a member from your network")
    @CommandCompletion("@players")
    public void onKick(Player player, String username, String network) {

    }

    @Subcommand("permission|permissions|perm|perms|p")
    @Syntax("<network name>")
    @Description("Access a networks permission settings")
    @CommandCompletion("@networks")
    public void onPermissions(Player player, String network) {

    }

    @Subcommand("rename")
    @Syntax("<network name> <new network name>")
    @Description("Rename a network")
    @CommandCompletion("@networks")
    public void onRename(Player player, String network, String newName) {

    }

    @Subcommand("disband")
    @Syntax("<network name>")
    @Description("Disband a network")
    @CommandCompletion("@networks")
    public void onDisband(Player player, String network) {

    }

    @Subcommand("password|pass|pw")
    @Syntax("<network name> <new password>")
    @Description("Update a networks password")
    @CommandCompletion("@networks")
    public void onPassword(Player player, String network, String password) {

    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/network help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}
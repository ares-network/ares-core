package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.llewkcor.ares.commons.promise.SimplePromise;
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
        plugin.getNetworkManager().getHandler().getCreateHandler().createNetwork(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Network has been created");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("delete|del|disband")
    @Syntax("<network name>")
    @Description("Delete an existing network")
    @CommandCompletion("@networks")
    public void onDelete(Player player, String name) {
        plugin.getNetworkManager().getHandler().getManageHandler().deleteNetwork(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Network has been deleted");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("invite|inv")
    @Syntax("<player> <network name>")
    @Description("Invite a player to your network")
    @CommandCompletion("@players @networks")
    public void onInvite(Player player, String username, String network) {
        plugin.getNetworkManager().getHandler().getInviteHandler().inviteMember(player, network, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("uninvite|uninv")
    @Syntax("<player> <network name>")
    @Description("Revoke an invitation to a network")
    @CommandCompletion("@players @networks")
    public void onUninvite(Player player, String username, String network) {
        plugin.getNetworkManager().getHandler().getInviteHandler().uninviteMember(player, network, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("accept|join")
    @Syntax("<network name> [password]")
    @Description("Join a network")
    public void onJoin(Player player, String network, @Optional String password) {
        if (password != null) {
            plugin.getNetworkManager().getHandler().getInviteHandler().acceptInvite(player, network, password, new SimplePromise() {
                @Override
                public void success() {}

                @Override
                public void fail(String s) {
                    player.sendMessage(ChatColor.RED + s);
                }
            });

            return;
        }

        plugin.getNetworkManager().getHandler().getInviteHandler().acceptInvite(player, network, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("leave")
    @Syntax("<network name>")
    @Description("Leave a network")
    @CommandCompletion("@networks")
    public void onLeave(Player player, String network) {
        plugin.getNetworkManager().getHandler().getManageHandler().leaveNetwork(player, network, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.YELLOW + "You have left the network");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("show|who")
    @Syntax("<network name>")
    @Description("Print information for a network")
    public void onShow(Player player, String network) {
        plugin.getNetworkManager().getHandler().getDisplayHandler().printDisplay(player, network, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("kick")
    @Syntax("<player> <network name>")
    @Description("Kick a member from your network")
    @CommandCompletion("@players")
    public void onKick(Player player, String username, String network) {

    }

    @Subcommand("config|conf|configure|settings")
    @Syntax("<network name>")
    @Description("Access a networks settings menu")
    @CommandCompletion("@networks")
    public void onConfig(Player player, String network) {

    }

    @Subcommand("rename")
    @Syntax("<network name> <new network name>")
    @Description("Rename a network")
    @CommandCompletion("@networks")
    public void onRename(Player player, String network, String newName) {
        plugin.getNetworkManager().getHandler().getManageHandler().renameNetwork(player, network, newName, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("password|pass|pw")
    @Syntax("<network name> <new password>")
    @Description("Update a networks password")
    @CommandCompletion("@networks")
    public void onPassword(Player player, String network, String password) {
        plugin.getNetworkManager().getHandler().getManageHandler().changePassword(player, network, password, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("list")
    @Description("Prints a list of all networks you are in")
    public void onList(Player player, @Optional String username) {
        if (username != null && !player.hasPermission("arescore.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to perform this action");
            return;
        }

        plugin.getNetworkManager().getHandler().getDisplayHandler().printList(player, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("permission|p|perm|perms")
    @Description("Edit a players permissions for your network")
    @Syntax("<network name> <username>")
    @CommandCompletion("@networks @players")
    public void onPermission(Player player, String networkName, String username) {
        plugin.getNetworkManager().getHandler().getMenuHandler().openPlayerEditMenu(player, networkName, username, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
        sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/" + help.getCommandName() + " help " + (help.getPage() + 1) + ChatColor.YELLOW + " to see the next page");
    }
}
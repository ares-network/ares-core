package com.llewkcor.ares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.llewkcor.ares.commons.promise.SimplePromise;
import com.llewkcor.ares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;

@CommandAlias("factory")
@AllArgsConstructor
public final class FactoryCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @Subcommand("create")
    @Syntax("<network name>")
    @Description("Create a new Factory")
    @CommandCompletion("@networks")
    public void onCreate(Player player, String networkName) {
        final Block target = player.getTargetBlock((Set<Material>)null, 4);

        if (target == null || !target.getType().equals(Material.FURNACE)) {
            player.sendMessage(ChatColor.RED + "You are not looking at a Furnace");
            return;
        }

        plugin.getFactoryManager().getHandler().createFactory(player, target, networkName, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Factory has been created. Right-click the crafting bench to access your list of recipes or the furnace to access your current jobs");
            }

            @Override
            public void fail(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }
}

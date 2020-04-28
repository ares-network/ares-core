package com.playares.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.playares.core.Ares;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class AHelpCommand extends BaseCommand {
    @Getter public final Ares plugin;

    @CommandAlias("help")
    @Description("View core help information for Ares")
    public void onHelp(Player player) {
        plugin.getConfigManager().getGeneralConfig().getHelpContext().forEach(player::sendMessage);
    }
}
package com.llewkcor.ares.core.factory.menu;

import com.llewkcor.ares.core.factory.FactoryManager;
import com.llewkcor.ares.core.factory.data.Factory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class FactoryMenuHandler {
    @Getter public FactoryManager manager;

    /**
     * Handles opening the Factory Recipe menu for the provided Factory
     * @param player Player
     * @param factory Factory
     */
    public void openFactoryRecipes(Player player, Factory factory) {
        final FactoryRecipeMenu menu = new FactoryRecipeMenu(manager.getPlugin(), player, factory);
        menu.open();
    }

    /**
     * Handles opening the Factory job menu for the provided Factory
     * @param player Player
     * @param factory Factory
     */
    public void openFactoryJobs(Player player, Factory factory) {
        final FactoryJobMenu menu = new FactoryJobMenu(manager.getPlugin(), player, factory);
        menu.open();
    }
}
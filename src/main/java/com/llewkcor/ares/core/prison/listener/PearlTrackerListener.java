package com.llewkcor.ares.core.prison.listener;

import com.llewkcor.ares.commons.location.BLocatable;
import com.llewkcor.ares.core.prison.PrisonPearlManager;
import com.llewkcor.ares.core.prison.data.PearlLocationType;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@AllArgsConstructor
public final class PearlTrackerListener implements Listener {
    @Getter public final PrisonPearlManager manager;

    @EventHandler
    public void onPlayerDropPearl(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final Item item = event.getItemDrop();
        final ItemStack itemStack = item.getItemStack();

        if (itemStack == null || !itemStack.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(itemStack);

        if (prisonPearl == null || prisonPearl.getLocationType().equals(PearlLocationType.GROUND)) {
            return;
        }

        prisonPearl.setLocation(new BLocatable(item.getLocation().getBlock()));
        prisonPearl.setLocationType(PearlLocationType.GROUND);
        prisonPearl.setTrackedItem(item);

        final Player imprisoned = prisonPearl.getImprisoned();

        if (imprisoned != null) {
            imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), PearlLocationType.GROUND, player.getName() + " dropped on the ground"));
        }
    }

    @EventHandler
    public void onPlayerPickupPearl(PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();
        final Item item = event.getItem();
        final ItemStack itemStack = item.getItemStack();

        if (itemStack == null || !itemStack.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(itemStack);

        if (prisonPearl == null) {
            return;
        }

        prisonPearl.setLocation(new BLocatable(item.getLocation().getBlock()));
        prisonPearl.setLocationType(PearlLocationType.PLAYER);
        prisonPearl.setTrackedItem(null);

        final Player imprisoned = prisonPearl.getImprisoned();

        if (imprisoned != null) {
            imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), PearlLocationType.PLAYER, player.getName() + " picked up"));
        }
    }

    @EventHandler
    public void onPlayerDisconnectWithPearl(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                continue;
            }

            final PrisonPearl prisonPearl = getManager().getPrisonPearlByItem(item);

            if (prisonPearl == null) {
                continue;
            }

            player.getInventory().remove(item);
            final Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), item);

            prisonPearl.setLocation(new BLocatable(droppedItem.getLocation().getBlock()));
            prisonPearl.setLocationType(PearlLocationType.GROUND);
            prisonPearl.setTrackedItem(droppedItem);

            final Player imprisoned = prisonPearl.getImprisoned();

            if (imprisoned != null) {
                imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), PearlLocationType.GROUND, player.getName() + " dropped upon disconnecting"));
            }
        }
    }

    @EventHandler
    public void onChunkUnloadWithPearl(ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();

        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof Item)) {
                continue;
            }

            final Item item = (Item)entity;
            final ItemStack itemStack = item.getItemStack();
            final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(itemStack);

            if (prisonPearl == null) {
                continue;
            }

            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPearlCombust(EntityCombustEvent event) {
        final Entity entity = event.getEntity();

        if (!(entity instanceof Item)) {
            return;
        }

        final Item item = (Item)entity;
        final ItemStack itemStack = item.getItemStack();
        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(itemStack);

        if (prisonPearl == null) {
            return;
        }

        manager.getHandler().releasePearl(prisonPearl, "Pearl was destroyed");
    }

    @EventHandler
    public void onPearlDamage(EntityDamageEvent event) {
        final Entity entity = event.getEntity();

        if (!(entity instanceof Item)) {
            return;
        }

        final Item item = (Item)entity;
        final ItemStack itemStack = item.getItemStack();
        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(itemStack);

        if (prisonPearl == null) {
            return;
        }

        manager.getHandler().releasePearl(prisonPearl, "Pearl was destroyed");
    }

    @EventHandler
    public void onInventoryClickPearl(InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();

        if (
                event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR) ||
                event.getAction().equals(InventoryAction.PICKUP_ALL) ||
                event.getAction().equals(InventoryAction.PICKUP_HALF) ||
                event.getAction().equals(InventoryAction.PICKUP_ONE)) {

            final ItemStack item = event.getCurrentItem();

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                return;
            }

            final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(item);

            if (prisonPearl == null) {
                return;
            }

            prisonPearl.setLocationType(PearlLocationType.PLAYER);
            prisonPearl.setLocation(new BLocatable(player.getLocation().getBlock()));
            prisonPearl.setTrackedItem(null);

            final Player imprisoned = prisonPearl.getImprisoned();

            if (imprisoned != null) {
                imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), player.getName() + " grabbed out of a container"));
            }

        } else if (
                event.getAction().equals(InventoryAction.PLACE_ALL) ||
                event.getAction().equals(InventoryAction.PLACE_SOME) ||
                event.getAction().equals(InventoryAction.PLACE_ONE)) {

            final ItemStack item = event.getCursor();

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                return;
            }

            final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(item);

            if (prisonPearl == null) {
                return;
            }

            final boolean clickedTop = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
            final InventoryHolder holder = clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();

            if (holder instanceof Player) {

                prisonPearl.setLocationType(PearlLocationType.PLAYER);
                prisonPearl.setLocation(new BLocatable(player.getLocation().getBlock()));
                prisonPearl.setTrackedItem(null);

                final Player imprisoned = prisonPearl.getImprisoned();

                if (imprisoned != null) {
                    imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), player.getName() + " grabbed out of a container"));
                }

            } else {

                final Location blockLocation = getInventoryLocation(holder);

                if (blockLocation == null) {
                    player.sendMessage(ChatColor.RED + "Prison Pearls can not be stored in this inventory type");
                    event.setCancelled(true);
                    return;
                }

                prisonPearl.setLocationType(PearlLocationType.CONTAINER);
                prisonPearl.setLocation(new BLocatable(blockLocation.getBlock()));
                prisonPearl.setTrackedItem(null);

                final Player imprisoned = prisonPearl.getImprisoned();

                if (imprisoned != null) {
                    imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), "Placed in a container"));
                }

            }
        } else if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            final ItemStack item = event.getCurrentItem();

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                return;
            }

            final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(item);

            if (prisonPearl == null) {
                return;
            }

            final boolean clickedTop = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
            final InventoryHolder holder = !clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();

            if (holder.getInventory().firstEmpty() >= 0) {
                if (holder instanceof Player) {

                    prisonPearl.setLocationType(PearlLocationType.PLAYER);
                    prisonPearl.setLocation(new BLocatable(player.getLocation().getBlock()));
                    prisonPearl.setTrackedItem(null);

                    final Player imprisoned = prisonPearl.getImprisoned();

                    if (imprisoned != null) {
                        imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), player.getName() + " grabbed out of a container"));
                    }

                } else {

                    final Location blockLocation = getInventoryLocation(holder);

                    if (blockLocation == null) {
                        player.sendMessage(ChatColor.RED + "Prison Pearls can not be stored in this inventory type");
                        event.setCancelled(true);
                        return;
                    }

                    prisonPearl.setLocationType(PearlLocationType.CONTAINER);
                    prisonPearl.setLocation(new BLocatable(blockLocation.getBlock()));
                    prisonPearl.setTrackedItem(null);

                    final Player imprisoned = prisonPearl.getImprisoned();

                    if (imprisoned != null) {
                        imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), "Placed in a container"));
                    }
                }
            }
        } else if (event.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            if (event.isCancelled()) {
                return;
            }

            final PlayerInventory playerInventory = event.getWhoClicked().getInventory();
            final ItemStack item = playerInventory.getItem(event.getHotbarButton());

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                return;
            }

            final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(item);

            if (prisonPearl == null) {
                return;
            }

            final boolean clickedTop = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
            final InventoryHolder holder = clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();

            if (holder instanceof Player) {
                prisonPearl.setLocationType(PearlLocationType.PLAYER);
                prisonPearl.setLocation(new BLocatable(player.getLocation().getBlock()));
                prisonPearl.setTrackedItem(null);

                final Player imprisoned = prisonPearl.getImprisoned();

                if (imprisoned != null) {
                    imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), player.getName() + " grabbed out of a container"));
                }
            } else {

                final Location blockLocation = getInventoryLocation(holder);

                if (blockLocation == null) {
                    player.sendMessage(ChatColor.RED + "Prison Pearls can not be stored in this inventory type");
                    event.setCancelled(true);
                    return;
                }

                prisonPearl.setLocationType(PearlLocationType.CONTAINER);
                prisonPearl.setLocation(new BLocatable(blockLocation.getBlock()));
                prisonPearl.setTrackedItem(null);

                final Player imprisoned = prisonPearl.getImprisoned();

                if (imprisoned != null) {
                    imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), "Placed in a container"));
                }

            }
        } else if (event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
            final ItemStack item = event.getCursor();

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                return;
            }

            final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(item);

            if (prisonPearl == null) {
                return;
            }

            final boolean clickedTop = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
            final InventoryHolder holder = clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();

            if (holder instanceof Player) {
                prisonPearl.setLocationType(PearlLocationType.PLAYER);
                prisonPearl.setLocation(new BLocatable(player.getLocation().getBlock()));
                prisonPearl.setTrackedItem(null);

                final Player imprisoned = prisonPearl.getImprisoned();

                if (imprisoned != null) {
                    imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), player.getName() + " grabbed out of a container"));
                }
            } else {

                final Location blockLocation = getInventoryLocation(holder);

                if (blockLocation == null) {
                    player.sendMessage(ChatColor.RED + "Prison Pearls can not be stored in this inventory type");
                    event.setCancelled(true);
                    return;
                }

                prisonPearl.setLocationType(PearlLocationType.CONTAINER);
                prisonPearl.setLocation(new BLocatable(blockLocation.getBlock()));
                prisonPearl.setTrackedItem(null);

                final Player imprisoned = prisonPearl.getImprisoned();

                if (imprisoned != null) {
                    imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), "Placed in a container"));
                }

            }
        } else if (
                event.getAction().equals(InventoryAction.DROP_ALL_CURSOR) ||
                event.getAction().equals(InventoryAction.DROP_ALL_SLOT) ||
                event.getAction().equals(InventoryAction.DROP_ONE_CURSOR) ||
                event.getAction().equals(InventoryAction.DROP_ONE_SLOT)) {

            // Handled by ItemSpawnEvent

        } else {
            final ItemStack currentItem = event.getCurrentItem();
            final ItemStack cursorItem = event.getCursor();

            if (currentItem != null && currentItem.getType().equals(Material.ENDER_PEARL)) {
                final PrisonPearl currentPearl = manager.getPrisonPearlByItem(currentItem);

                if (currentPearl != null) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Invalid inventory behavior");
                    return;
                }
            }

            if (cursorItem != null && cursorItem.getType().equals(Material.ENDER_PEARL)) {
                final PrisonPearl cursorPearl = manager.getPrisonPearlByItem(cursorItem);

                if (cursorPearl != null) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Invalid inventory behavior");
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        final Entity entity = event.getRightClicked();

        if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(item);

        if (prisonPearl == null) {
            return;
        }

        if (entity instanceof ItemFrame) {
            prisonPearl.setLocation(new BLocatable(entity.getLocation().getBlock()));
            prisonPearl.setLocationType(PearlLocationType.CONTAINER);
            prisonPearl.setTrackedItem(null);

            final Player imprisoned = prisonPearl.getImprisoned();

            if (imprisoned != null) {
                imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), "Placed in a container"));
            }
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryMoveItemEvent event) {
        final ItemStack item = event.getItem();
        final Inventory destination = event.getDestination();

        if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(item);

        if (prisonPearl == null) {
            return;
        }

        final Location location = getInventoryLocation(destination.getHolder());

        if (location == null) {
            return;
        }

        prisonPearl.setLocation(new BLocatable(location.getBlock()));
        prisonPearl.setLocationType(PearlLocationType.CONTAINER);
        prisonPearl.setTrackedItem(null);

        final Player imprisoned = prisonPearl.getImprisoned();

        if (imprisoned != null) {
            imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), "Moved inside a container"));
        }
    }

    @EventHandler
    public void onInventoryPickupPearl(InventoryPickupItemEvent event) {
        final Item item = event.getItem();
        final Inventory inventory = event.getInventory();
        final Location location = getInventoryLocation(inventory.getHolder());

        if (item == null || !item.getItemStack().getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        if (location == null) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(item.getItemStack());

        if (prisonPearl == null) {
            return;
        }

        prisonPearl.setLocation(new BLocatable(location.getBlock()));
        prisonPearl.setLocationType(PearlLocationType.CONTAINER);
        prisonPearl.setTrackedItem(null);

        final Player imprisoned = prisonPearl.getImprisoned();

        if (imprisoned != null) {
            imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), "Picked up by a hopper"));
        }
    }

    @EventHandler
    public void onPearlItemSpawn(ItemSpawnEvent event) {
        final Item item = event.getEntity();
        final ItemStack itemStack = item.getItemStack();

        if (itemStack == null || !itemStack.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(itemStack);

        if (prisonPearl == null || prisonPearl.getLocationType().equals(PearlLocationType.GROUND)) {
            return;
        }

        prisonPearl.setLocation(new BLocatable(item.getLocation().getBlock()));
        prisonPearl.setLocationType(PearlLocationType.GROUND);
        prisonPearl.setTrackedItem(item);

        final Player imprisoned = prisonPearl.getImprisoned();

        if (imprisoned != null) {
            imprisoned.sendMessage(getLocationUpdate(prisonPearl.getLocation(), prisonPearl.getLocationType(), "Item dropped on the ground"));
        }
    }

    @EventHandler
    public void onPearlDespawn(ItemDespawnEvent event) {
        final Item item = event.getEntity();

        if (item == null) {
            return;
        }

        final ItemStack itemStack = item.getItemStack();
        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(itemStack);

        if (prisonPearl == null) {
            return;
        }

        manager.getHandler().releasePearl(prisonPearl, "Pearl naturally despawned");
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        final Inventory inventory = event.getInventory();
        int removed = 0;

        for (ItemStack content : inventory.getContents()) {
            if (content == null || !content.getType().equals(Material.ENDER_PEARL)) {
                continue;
            }

            if (manager.isExpiredPrisonPearl(content)) {
                inventory.removeItem(content);
                removed += 1;
            }
        }

        if (removed >= 0) {
            for (HumanEntity viewer : inventory.getViewers()) {
                viewer.sendMessage(ChatColor.YELLOW + "Removed " + removed + " expired Prison Pearls from this inventory");
            }
        }
    }

    /**
     * Returns the location of the provided InventoryHolder
     * @param holder InventoryHolder
     * @return Bukkit Location
     */
    private Location getInventoryLocation(InventoryHolder holder) {
        if (holder instanceof Chest) {
            return ((Chest)holder).getLocation();
        } else if (holder instanceof DoubleChest) {
            return ((DoubleChest)holder).getLocation();
        } else if (holder instanceof Furnace) {
            return ((Furnace)holder).getLocation();
        } else if (holder instanceof Dispenser) {
            return ((Dispenser)holder).getLocation();
        } else if (holder instanceof BrewingStand) {
            return ((BrewingStand)holder).getLocation();
        } else if (holder instanceof Hopper) {
            return ((Hopper)holder).getLocation();
        } else if (holder instanceof Dropper) {
            return ((Dropper)holder).getLocation();
        } else {
            return null;
        }
    }

    /**
     * Returns a formatted notification sent to the imprisoned player
     * @param location New Location
     * @param type Location Type
     * @param description Description
     * @return Notification
     */
    private String getLocationUpdate(BLocatable location, PearlLocationType type, String description) {
        if (type == PearlLocationType.GROUND) {
            return ChatColor.GRAY + "Your pearl is now on the ground at " + ChatColor.DARK_AQUA + location.toString() + ChatColor.GRAY + ", Reason: " + ChatColor.AQUA + description;
        }

        else if (type == PearlLocationType.CONTAINER) {
            return ChatColor.GRAY + "Your pearl is now in a container at " + ChatColor.DARK_AQUA + location.toString() + ChatColor.GRAY + ", Reason: " + ChatColor.AQUA + description;
        }

        else {
            return ChatColor.GRAY + "Your pearl is now being held by a player at " + ChatColor.DARK_AQUA + location.toString() + ChatColor.GRAY + ", Reason: " + ChatColor.AQUA + description;
        }
    }
}
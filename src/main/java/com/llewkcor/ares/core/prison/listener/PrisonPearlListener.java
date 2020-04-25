package com.llewkcor.ares.core.prison.listener;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import com.llewkcor.ares.commons.item.ItemBuilder;
import com.llewkcor.ares.commons.logger.Logger;
import com.llewkcor.ares.commons.util.bukkit.Scheduler;
import com.llewkcor.ares.commons.util.general.IPS;
import com.llewkcor.ares.commons.util.general.Time;
import com.llewkcor.ares.core.alts.data.AltEntry;
import com.llewkcor.ares.core.loggers.entity.CombatLogger;
import com.llewkcor.ares.core.loggers.event.LoggerDeathEvent;
import com.llewkcor.ares.core.prison.PrisonPearlManager;
import com.llewkcor.ares.core.prison.data.PrisonPearl;
import com.llewkcor.ares.core.prison.event.PrisonPearlCreateEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public final class PrisonPearlListener implements Listener {
    @Getter public final PrisonPearlManager manager;

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLoginAttempt(AsyncPlayerPreLoginEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        if (!manager.getPlugin().getDatabaseInstance().isConnected()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "The server is still starting");
            return;
        }

        if (!event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {
            return;
        }

        final UUID uniqueId = event.getUniqueId();
        final long address = IPS.toLong(event.getAddress().getHostAddress());
        final ImmutableCollection<AltEntry> accountEntries = manager.getPlugin().getAltManager().getAlts(uniqueId, address);
        final List<AltEntry> pearledAlts = Lists.newArrayList();

        for (AltEntry entry : accountEntries) {
            final PrisonPearl pearl = manager.getPrisonPearlByPlayer(entry.getUniqueId());

            if (pearl == null) {
                continue;
            }

            pearledAlts.add(entry);
        }

        if (pearledAlts.size() > manager.getPlugin().getConfigManager().getPrisonPearlConfig().getMaxPrisonPearledAccounts()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "Prison Pearl Ban Evasion");

            new Scheduler(manager.getPlugin()).sync(() -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tempban " + event.getName() + " " + manager.getPlugin().getConfigManager().getPrisonPearlConfig().getAltBanDuration() + " Prison Pearl Ban Evasion");
                Logger.print(event.getName() + " was banned for having " + pearledAlts.size() + " Prison Pearled alt accounts");
            }).run();

            return;
        }

        if (pearledAlts.size() >= 1 && pearledAlts.size() < manager.getPlugin().getConfigManager().getPrisonPearlConfig().getMaxPrisonPearledAccounts()) {
            final AltEntry pearledAlt = pearledAlts.get(0);

            if (!pearledAlt.getUniqueId().equals(uniqueId)) {
                new Scheduler(manager.getPlugin()).sync(() ->
                        Bukkit.getOnlinePlayers().stream().filter(player ->
                                player.hasPermission("arescore.admin")).forEach(staff ->
                                staff.sendMessage(ChatColor.DARK_RED + "[Prison Evasion] " +
                                        ChatColor.DARK_AQUA + event.getName() + ChatColor.GRAY + " is evading a prison pearl on an alt account. If they are Prison Pearled again they will be banned."))).run();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        final World world = player.getWorld();
        final Location prison = manager.getPrisonLocation();
        final PrisonPearl prisonPearl = manager.getPrisonPearlByPlayer(player.getUniqueId());

        if (prisonPearl == null) {
            return;
        }

        if (prison != null && !world.getName().equals(prison.getWorld().getName())) {
            player.teleport(prison);
        }

        player.sendMessage(ChatColor.RED + "Your Prison Pearl will expire in " + Time.convertToRemaining(prisonPearl.getExpireTime() - Time.now()));
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GOLD + "/pp locate" + ChatColor.GRAY + " to track the location of your Prison Pearl");
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "/pp info" + ChatColor.GRAY + " to view more information about your Prison Pearl");
    }

    @EventHandler
    public void onPrisonPearl(PrisonPearlCreateEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final PrisonPearl prisonPearl = event.getPrisonPearl();
        final Player player = prisonPearl.getImprisoned();

        if (player == null) {
            return;
        }

        player.sendMessage(ChatColor.RED + "Your Prison Pearl will expire in " + Time.convertToRemaining(prisonPearl.getExpireTime() - Time.now()));
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GOLD + "/pp locate" + ChatColor.GRAY + " to track the location of your Prison Pearl");
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "/pp info" + ChatColor.GRAY + " to view more information about your Prison Pearl");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        manager.getMutedPearlNotifications().remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerImprison(PlayerDeathEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final Player player = event.getEntity();
        final Player killer = player.getKiller();
        final PrisonPearl existing = manager.getPrisonPearlByPlayer(player.getUniqueId());

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        if (killer == null) {
            return;
        }

        if (existing != null) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            final ItemStack item = killer.getInventory().getItem(i);

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                continue;
            }

            if (manager.getPrisonPearlByItem(item) != null) {
                continue;
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                killer.getInventory().remove(item);
            }

            manager.getHandler().imprisonPlayer(player.getName(), player.getUniqueId(), killer, manager.getBanDuration(player));
            break;
        }
    }

    @EventHandler
    public void onLoggerDeath(LoggerDeathEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final CombatLogger logger = event.getLogger();
        final Player killer = event.getKiller();
        final PrisonPearl existing = manager.getPrisonPearlByPlayer(logger.getOwnerId());

        if (killer == null) {
            return;
        }

        if (existing != null) {
            return;
        }

        if (logger.isPearlProtected()) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            final ItemStack item = killer.getInventory().getItem(i);

            if (item == null || !item.getType().equals(Material.ENDER_PEARL)) {
                continue;
            }

            if (manager.getPrisonPearlByItem(item) != null) {
                continue;
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                killer.getInventory().remove(item);
            }

            manager.getHandler().imprisonPlayer(logger.getOwnerUsername(), logger.getOwnerId(), killer, logger.getBanDuration());
            break;
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onThrowPearl(ProjectileLaunchEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof EnderPearl) || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity().getShooter();
        final ItemStack hand = player.getItemInHand();

        if (hand == null || !hand.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByItem(hand);
        final boolean isExpiredPearl = manager.isExpiredPrisonPearl(hand);

        if (prisonPearl == null && !isExpiredPearl) {
            return;
        }

        // Gives the freeing player the imprisoned players skull
        if (isExpiredPearl) {
            final String username = ChatColor.stripColor(hand.getItemMeta().getDisplayName());

            player.getInventory().removeItem(hand);

            final ItemStack head = new ItemBuilder()
                    .setMaterial(Material.SKULL_ITEM)
                    .setData((short)3)
                    .setName(ChatColor.GOLD + username)
                    .build();

            final SkullMeta meta = (SkullMeta)head.getItemMeta();
            meta.setOwner(username);
            head.setItemMeta(meta);

            player.getInventory().addItem(head);
        }

        // Cancel the throw event and run a release attempt
        event.setCancelled(true);
        manager.getHandler().releasePearl(prisonPearl, "Released by " + player.getName());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        final PrisonPearl prisonPearl = manager.getPrisonPearlByPlayer(player.getUniqueId());

        if (prisonPearl == null) {
            return;
        }

        final Location prison = manager.getPrisonLocation();

        if (prison == null) {
            return;
        }

        event.setRespawnLocation(prison);
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent event) {
        if (!manager.getPlugin().getConfigManager().getPrisonPearlConfig().isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (player.hasPermission("arescore.admin")) {
            return;
        }

        final PrisonPearl prisonPearl = manager.getPrisonPearlByPlayer(player.getUniqueId());

        if (prisonPearl == null) {
            return;
        }

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL) || event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)) {
            player.sendMessage(ChatColor.RED + "You can not change worlds while imprisoned");
            event.setCancelled(true);
        }
    }
}
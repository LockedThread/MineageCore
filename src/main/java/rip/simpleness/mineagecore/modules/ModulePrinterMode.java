package rip.simpleness.mineagecore.modules;

import com.google.common.collect.Sets;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import io.netty.util.internal.ConcurrentSet;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

public class ModulePrinterMode implements TerminableModule {

    private static final EnumSet<Material> BANNED_INTERACTABLES = EnumSet.of(Material.MONSTER_EGG, Material.EGG,
            Material.MOB_SPAWNER, Material.BEACON, Material.BEDROCK, Material.BOW, Material.POTION,
            Material.ENDER_PEARL, Material.SNOW_BALL, Material.EXP_BOTTLE, Material.ENDER_CHEST, Material.INK_SACK,
            Material.EYE_OF_ENDER, Material.ACACIA_DOOR_ITEM, Material.DARK_OAK_DOOR_ITEM, Material.BIRCH_DOOR_ITEM,
            Material.IRON_DOOR, Material.JUNGLE_DOOR_ITEM, Material.SPRUCE_DOOR_ITEM, Material.WOODEN_DOOR, Material.IRON_TRAPDOOR,
            Material.ARMOR_STAND, Material.RECORD_3, Material.RECORD_4, Material.RECORD_5, Material.RECORD_6, Material.RECORD_7,
            Material.RECORD_8, Material.RECORD_9, Material.RECORD_10, Material.RECORD_11, Material.RECORD_12, Material.GOLD_RECORD,
            Material.GREEN_RECORD, Material.BOAT, Material.MINECART, Material.MINECART, Material.COMMAND_MINECART, Material.EXPLOSIVE_MINECART,
            Material.HOPPER_MINECART, Material.POWERED_MINECART, Material.STORAGE_MINECART, Material.BED, Material.DOUBLE_PLANT, Material.LONG_GRASS);
    private ConcurrentSet<UUID> printingPlayers;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.printingPlayers = new ConcurrentSet<>();

        cancelEvents(terminableConsumer, new Class[]{PlayerPickupItemEvent.class, PlayerDropItemEvent.class, PlayerFishEvent.class, PlayerInteractEntityEvent.class, PlayerItemConsumeEvent.class, PlayerBucketEmptyEvent.class, PlayerBucketFillEvent.class, PlayerInteractAtEntityEvent.class, PlayerArmorStandManipulateEvent.class, PlayerShearEntityEvent.class, PlayerEditBookEvent.class, PlayerEggThrowEvent.class});
        cancelEvent(terminableConsumer, PlayerLeashEntityEvent::getPlayer, PlayerLeashEntityEvent.class);
        Commands.create()
                .assertPlayer()
                .assertPermission("mineage.printermode")
                .handler(commandContext -> {
                    Player player = commandContext.sender();
                    if (printingPlayers.contains(player.getUniqueId())) {
                        disablePrinter(player);
                    } else if (!playerInventoryIsEmpty(player)) {
                        player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&eYour inventory and armor must be empty to enable printer!"));
                    } else if (MineageCore.getInstance().canBuild(player, player.getLocation())) {
                        player.sendMessage(Text.colorize("&cYou must be in friendly territory to activate /printer!"));
                    } else {
                        enablePrinter(player);
                    }
                }).registerAndBind(terminableConsumer, "printer", "printermode");

        Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                .handler(event -> {
                    ItemStack item = event.getItem();
                    if (item != null && (BANNED_INTERACTABLES.contains(item.getType()) || (item.hasItemMeta() && (item.getItemMeta().hasLore() || item.getItemMeta().hasDisplayName()))) || event.getAction() == Action.LEFT_CLICK_AIR || event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof InventoryHolder) {
                        event.setCancelled(true);
                    }
                })
                .bindWith(terminableConsumer);

        Events.subscribe(PlayerQuitEvent.class)
                .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                .handler(event -> disablePrinter(event.getPlayer()))
                .bindWith(terminableConsumer);

        Events.subscribe(BlockPlaceEvent.class, EventPriority.LOWEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getBlockPlaced() != null)
                .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                .handler(event -> {
                    if (!chargePlayer(event.getPlayer(), event.getBlockPlaced().getType())) {
                        event.setCancelled(true);
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerExpChangeEvent.class)
                .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                .handler(event -> {
                    if (event.getAmount() > 0) event.setAmount(0);
                })
                .bindWith(terminableConsumer);

        Events.subscribe(PlayerChangedWorldEvent.class)
                .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                .handler(event -> disablePrinter(event.getPlayer()))
                .bindWith(terminableConsumer);

        Events.subscribe(PlayerMoveEvent.class)
                .filter(EventFilters.ignoreSameChunk())
                .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                .handler(event -> {
                    if (MineageCore.getInstance().canBuild(event.getPlayer(), event.getTo()))
                        disablePrinter(event.getPlayer());
                })
                .bindWith(terminableConsumer);

        Events.subscribe(HangingBreakByEntityEvent.class)
                .filter(event -> event.getRemover() instanceof Player)
                .filter(event -> printingPlayers.contains(event.getRemover().getUniqueId()))
                .handler(event -> event.setCancelled(true))
                .bindWith(terminableConsumer);

        Events.subscribe(ProjectileLaunchEvent.class, EventPriority.LOWEST)
                .filter(event -> event.getEntity().getShooter() instanceof Player)
                .filter(event -> printingPlayers.contains(((Player) event.getEntity().getShooter()).getUniqueId()))
                .handler(event -> {
                    event.getEntity().remove();
                    event.setCancelled(true);
                })
                .bindWith(terminableConsumer);

        Events.subscribe(InventoryClickEvent.class, EventPriority.LOWEST)
                .filter(event -> printingPlayers.contains(event.getWhoClicked().getUniqueId()))
                .filter(event -> event.getView().getType() != InventoryType.CREATIVE)
                .handler(event -> {
                    event.setCancelled(true);
                    event.getWhoClicked().closeInventory();
                })
                .bindWith(terminableConsumer);

        Events.subscribe(InventoryOpenEvent.class, EventPriority.LOWEST)
                .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                .filter(event -> event.getView().getType() != InventoryType.CREATIVE)
                .handler(event -> {
                    event.setCancelled(true);
                    event.getView().close();
                })
                .bindWith(terminableConsumer);

        final HashSet<String> allowedCmds = Sets.newHashSet("/printer", "/f ", "/faction", "/warp", "/spawn", "/tp", "/m ", "/msg",
                "/r ", "/reply", "/whisper", "/w ", "/t ", "/tell", "/sc ", "/mute", "/warn", "/punish", "/ban", "/history", "/dupeip",
                "/staffchat", "/bal");
        Events.subscribe(PlayerCommandPreprocessEvent.class, EventPriority.LOWEST)
                .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                .handler(event -> {
                    String message = event.getMessage().toLowerCase().trim();
                    for (String cmd : allowedCmds) {
                        if (message.startsWith(cmd) && !message.contains("vault") && !message.contains("tnt")) {
                            return;
                        }
                    }

                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Text.colorize("&cYou can't use that command in /printer!"));
                })
                .bindWith(terminableConsumer);

        Events.subscribe(EntityDamageByEntityEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getDamager() instanceof Player)
                .filter(event -> printingPlayers.contains(event.getDamager().getUniqueId()))
                .handler(event -> {
                    event.setCancelled(true);
                    disablePrinter((Player) event.getDamager());
                })
                .bindWith(terminableConsumer);

        Events.subscribe(PlayerJoinEvent.class)
                .filter(event -> !event.getPlayer().hasPermission("essentials.gamemode.creative"))
                .filter(event -> event.getPlayer().getGameMode() == GameMode.CREATIVE)
                .handler(event -> event.getPlayer().setGameMode(GameMode.SURVIVAL))
                .bindWith(terminableConsumer);

        Events.subscribe(PlayerInteractEntityEvent.class, EventPriority.LOWEST)
                .filter(event -> event.getRightClicked() instanceof Player)
                .filter(event -> printingPlayers.contains(event.getRightClicked().getUniqueId()))
                .handler(event -> {
                    event.setCancelled(true);
                    disablePrinter((Player) event.getRightClicked());
                })
                .bindWith(terminableConsumer);

        Events.subscribe(EntityDamageEvent.class)
                .filter(event -> event.getEntity() instanceof Player)
                .filter(event -> event.getCause() == EntityDamageEvent.DamageCause.FALL)
                .filter(event -> event.getEntity().hasMetadata("nofalldamage"))
                .handler(event -> event.setCancelled(true))
                .bindWith(terminableConsumer);

        Events.subscribe(InventoryCreativeEvent.class)
                .filter(event -> printingPlayers.contains(event.getWhoClicked().getUniqueId()))
                .filter(event -> (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) || (event.getCursor() != null && event.getCursor().hasItemMeta()))
                .handler(event -> {
                    Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("mineage.staff")).forEach(player -> player.sendMessage(Text.colorize("&c[Hacked-Client] &e" + event.getWhoClicked().getName() + " &ctried to use an item with NBT in creative! They're using a hacked client or exploit!")));
                    MineageCore.getInstance().getLogger().severe("[Hacked-Client] " + event.getWhoClicked().getName() + " tried to use an item with NBT in creative! They're using a hacked client or exploit!");
                    event.getWhoClicked().sendMessage(Text.colorize("&cYou're not allowed to interact with that item!"));
                    event.setCancelled(true);
                })
                .bindWith(terminableConsumer);

        Events.subscribe(PlayerDeathEvent.class)
                .filter(event -> printingPlayers.contains(event.getEntity().getUniqueId()))
                .handler(event -> {
                    event.getDrops().clear();
                    event.setDroppedExp(0);
                })
                .bindWith(terminableConsumer);


        Schedulers.bukkit().runTaskTimer(MineageCore.getInstance(), () -> {
            for (UUID printingPlayer : printingPlayers) {
                Player player = Bukkit.getPlayer(printingPlayer);
                player.getNearbyEntities(20, 20, 20)
                        .stream()
                        .filter(entity -> entity instanceof Player && !FPlayers.getInstance().getByPlayer((Player) entity).getRelationTo(FPlayers.getInstance().getByPlayer(player)).isAtLeast(Relation.MEMBER))
                        .forEach(entity -> {
                            player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&cThere's enemies nearby!"));
                            disablePrinter(player);
                        });
            }
        }, 0L, 100L);
    }

    private boolean playerInventoryIsEmpty(Player player) {
        return Arrays.stream(player.getInventory().getContents()).noneMatch(Objects::nonNull) &&
                Arrays.stream(player.getInventory().getArmorContents()).noneMatch(itemStack -> itemStack != null && itemStack.getType() != Material.AIR);
    }

    private boolean chargePlayer(Player player, Material material) {
        double price = MineageCore.getInstance().getPrices().getOrDefault(material, 0.0);
        if (price == 0.0) {
            player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&cYou can't place blocks not in shop!"));
            return false;
        }
        double balance = MineageCore.getInstance().getEconomy().getBalance(player);
        if (balance < price) {
            player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&cYou don't have enough money to place a " + MineageCore.getInstance().capitalizeEveryWord(material.name())));
            disablePrinter(player);
            return false;
        }
        Schedulers.async().run(() -> MineageCore.getInstance().getEconomy().withdrawPlayer(player, price));
        return true;
    }

    private void disablePrinter(Player player) {
        printingPlayers.remove(player.getUniqueId());
        player.sendMessage(Text.colorize("&eYou have successfully &c&ldisabled &ePrinterMode"));
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setMetadata("nofalldamage", new FixedMetadataValue(MineageCore.getInstance(), true));
        Schedulers.sync().runLater(() -> player.removeMetadata("nofalldamage", MineageCore.getInstance()), 200L);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ncp unexempt " + player.getName());
    }

    private void enablePrinter(Player player) {
        printingPlayers.add(player.getUniqueId());
        player.sendMessage(Text.colorize("&eYou have successfully &a&lenabled &ePrinterMode"));
        player.setGameMode(GameMode.CREATIVE);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.closeInventory();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ncp exempt " + player.getName() + " net");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ncp exempt " + player.getName() + " blockplace");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ncp exempt " + player.getName() + " blockinteract");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ncp exempt " + player.getName() + " moving_morepackets");
    }

    private <T extends PlayerEvent> void cancelEvents(TerminableConsumer terminableConsumer, Class<T>... clazzes) {
        Arrays.stream(clazzes)
                .forEach(clazz -> Events.subscribe(clazz, EventPriority.LOWEST)
                        .filter(event -> !((Cancellable) event).isCancelled())
                        .filter(event -> printingPlayers.contains(event.getPlayer().getUniqueId()))
                        .handler(event -> ((Cancellable) event).setCancelled(true))
                        .bindWith(terminableConsumer));
    }

    private <T extends Event> void cancelEvent(TerminableConsumer terminableConsumer, Function<T, Player> getPlayerFunc, Class<T> clazz) {
        Events.subscribe(clazz, EventPriority.LOWEST)
                .filter(event -> !((Cancellable) event).isCancelled())
                .filter(event -> printingPlayers.contains(getPlayerFunc.apply(event).getUniqueId()))
                .handler(event -> ((Cancellable) event).setCancelled(true))
                .bindWith(terminableConsumer);
    }
}

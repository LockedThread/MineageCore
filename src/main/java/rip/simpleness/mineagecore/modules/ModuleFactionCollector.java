package rip.simpleness.mineagecore.modules;

import com.google.common.reflect.TypeToken;
import com.massivecraft.factions.FPlayers;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.serialize.GsonStorageHandler;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.EnumDirection;
import net.techcable.tacospigot.event.entity.SpawnerPreSpawnEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.paperspigot.Title;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.customitems.CustomItem;
import rip.simpleness.mineagecore.enums.CollectionType;
import rip.simpleness.mineagecore.enums.FactionCollectorUpgrade;
import rip.simpleness.mineagecore.objs.FactionCollector;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ModuleFactionCollector implements TerminableModule {

    private static final MineageCore INSTANCE = MineageCore.getInstance();
    public static ItemStack upgradeItemStack, maxUpgradeItemStack, infoItemStack;
    private HashMap<String, FactionCollector> factionCollectorHashMap;
    private GsonStorageHandler<HashMap<String, FactionCollector>> gsonStorageHandler;
    private CustomItem factionCollectorCustomItem, sellWandCustomCustomItem;

    public static ItemStack buildUpgradeItemStack(FactionCollectorUpgrade upgrade) {
        ItemStack clone = upgradeItemStack.clone();
        ItemMeta itemMeta = clone.getItemMeta();
        itemMeta.setLore(itemMeta.getLore()
                .stream()
                .map(s -> s.replace("{level}", String.valueOf(upgrade.getRank())).replace("{price}", String.valueOf(upgrade.getPrice())))
                .collect(Collectors.toList()));
        clone.setItemMeta(itemMeta);
        return clone;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.gsonStorageHandler = new GsonStorageHandler<>("faction-collectors", ".json", MineageCore.getInstance().getDataFolder(), new TypeToken<HashMap<String, FactionCollector>>() {
        });
        this.factionCollectorHashMap = gsonStorageHandler.load().orElse(new HashMap<>());

        terminableConsumer.bind(() -> gsonStorageHandler.save(factionCollectorHashMap));

        setupItemStacks();

        Events.subscribe(BlockPlaceEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getBlockPlaced() != null)
                .filter(event -> event.getItemInHand() != null && event.getItemInHand().hasItemMeta() && event.getItemInHand().getItemMeta().hasDisplayName())
                .filter(event -> MineageCore.getInstance().canBuild(event.getPlayer(), event.getBlockPlaced().getLocation()))
                .filter(event -> factionCollectorCustomItem.isCustomItem(event.getItemInHand()))
                .handler(event -> {
                    Player player = event.getPlayer();
                    Chunk chunk = event.getBlockPlaced().getChunk();
                    if (factionCollectorHashMap.containsKey(chunkToString(chunk))) {
                        event.setCancelled(true);
                        player.sendTitle(Title.builder().title(Text.colorize("&cThere's already a collector in the chunk")).fadeIn(5).fadeOut(5).stay(25).build());
                    } else {
                        factionCollectorHashMap.put(chunkToString(chunk), new FactionCollector(event.getBlockPlaced().getLocation()));
                        player.sendTitle(Title.builder().title(Text.colorize("&aYou have placed a Collector")).fadeIn(5).fadeOut(5).stay(25).build());
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(BlockBreakEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getBlock() != null)
                .filter(event -> MineageCore.getInstance().canBuild(event.getPlayer(), event.getBlock().getLocation()))
                .handler(event -> {
                    Block block = event.getBlock();
                    String chunkToString = chunkToString(block.getChunk());
                    final FactionCollector factionCollector = factionCollectorHashMap.get(chunkToString);
                    if (factionCollector != null && me.lucko.helper.serialize.BlockPosition.of(block).equals(factionCollector.getBlockPosition())) {
                        factionCollectorHashMap.remove(chunkToString);
                        event.getPlayer().sendTitle(Title.builder().title(Text.colorize("&aSuccessfully &cremoved &aa collector")).fadeIn(5).fadeOut(5).stay(25).build());
                        block.getWorld().dropItemNaturally(block.getLocation(), factionCollectorCustomItem.getItemStack());
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerInteractEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getAction() == Action.RIGHT_CLICK_BLOCK)
                .filter(event -> event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BEACON)
                .handler(event -> {
                    final FactionCollector factionCollector = factionCollectorHashMap.get(chunkToString(event.getClickedBlock().getLocation().getChunk()));
                    if (factionCollector != null) {
                        event.setCancelled(true);
                        Player player = event.getPlayer();
                        if (sellWandCustomCustomItem.isCustomItem(event.getItem())) {
                            double shmoney = factionCollector.getAmounts()
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> entry.getKey() != CollectionType.TNT)
                                    .mapToDouble(entry -> entry.getKey().getValue() * entry.getValue())
                                    .sum();
                            INSTANCE.getEconomy().depositPlayer(player, shmoney);
                            player.sendTitle(Title.builder().title(Text.colorize("&a&l+$" + shmoney)).fadeIn(5).fadeOut(5).stay(25).build());
                            factionCollector.resetWithBlacklist(CollectionType.TNT);
                        } else if (!FPlayers.getInstance().getByPlayer(player).hasFaction()) {
                            player.sendMessage(Text.colorize("&cYou need a faction to open a collector!"));
                        } else {
                            player.openInventory(factionCollector.getMenuFactionCollector().getInventory());
                        }
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(BlockGrowEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getNewState().getType() == Material.CACTUS)
                .filter(event -> !canGrow(event.getBlock()))
                .handler(event -> {
                    final FactionCollector collector = factionCollectorHashMap.get(chunkToString(event.getBlock().getChunk()));
                    event.setCancelled(true);
                    if (collector != null) {
                        collector.addAmount(CollectionType.CACTUS, 1);
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(BlockExplodeEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .handler(event -> event.blockList()
                        .stream()
                        .filter(block -> block.getType() == Material.BEACON)
                        .forEach(block -> event.blockList().remove(block)))
                .bindWith(terminableConsumer);

        Events.subscribe(SpawnerPreSpawnEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .handler(event -> {
                    final FactionCollector collector = factionCollectorHashMap.get(chunkToString(event.getLocation().getChunk()));
                    event.setCancelled(true);
                    if (collector != null && collector.getFactionCollectorUpgrade().getApplicables().contains(CollectionType.fromEntityType(event.getSpawnedType()))) {
                        if (event.getSpawnedType() == EntityType.CREEPER) {
                            if (MineageCore.getInstance().getRandom().nextBoolean()) {
                                collector.addAmount(CollectionType.TNT, 1);
                            }
                        } else {
                            collector.addAmount(CollectionType.fromEntityType(event.getSpawnedType()), 1);
                        }
                    }
                }).bindWith(terminableConsumer);
    }

    public HashMap<String, FactionCollector> getFactionCollectorHashMap() {
        return factionCollectorHashMap;
    }

    private String chunkToString(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    private boolean canGrow(Block bukkitBlock) {
        final BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        final net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) bukkitBlock.getWorld()).getHandle();

        for (EnumDirection enumDirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            if (nmsWorld.getType(blockPosition.shift(enumDirection)).getBlock().getMaterial().isBuildable()) {
                return false;
            }
        }

        final net.minecraft.server.v1_8_R3.Block block = nmsWorld.getType(blockPosition.down()).getBlock();
        return block == Blocks.CACTUS || block == Blocks.SAND;
    }

    private void setupItemStacks() {
        ItemStackBuilder factionCollectorBuilder = ItemStackBuilder.of(Material.BEACON)
                .name(INSTANCE.getConfig().getString("faction-collectors.collector-item.name"))
                .lore(INSTANCE.getConfig().getStringList("faction-collectors.collector-item.lore"));
        if (INSTANCE.getConfig().getBoolean("faction-collectors.collector-item.enchanted")) {
            factionCollectorBuilder.enchant(Enchantment.ARROW_INFINITE, 1);
            factionCollectorBuilder.flag(ItemFlag.HIDE_ENCHANTS);
        }
        this.factionCollectorCustomItem = new CustomItem("factioncollector", factionCollectorBuilder.build());


        this.sellWandCustomCustomItem = INSTANCE.getConfig().getBoolean("faction-collectors.sellwand-item.enchanted") ? new CustomItem("sellwand", ItemStackBuilder.of(Material.matchMaterial(INSTANCE.getConfig().getString("faction-collectors.sellwand-item.material")))
                .name(INSTANCE.getConfig().getString("faction-collectors.sellwand-item.name"))
                .lore(INSTANCE.getConfig().getString("faction-collectors.sellwand-item.lore"))
                .enchant(Enchantment.ARROW_INFINITE)
                .flag(ItemFlag.HIDE_ENCHANTS)
                .build()) : new CustomItem("sellwand", ItemStackBuilder.of(Material.matchMaterial(INSTANCE.getConfig().getString("faction-collectors.sellwand-item.material")))
                .name(INSTANCE.getConfig().getString("faction-collectors.sellwand-item.name"))
                .lore(INSTANCE.getConfig().getString("faction-collectors.sellwand-item.lore"))
                .build());


        upgradeItemStack = ItemStackBuilder.of(Material.matchMaterial(INSTANCE.getConfig().getString("faction-collectors.gui.items.upgrade.material")))
                .name(INSTANCE.getConfig().getString("faction-collectors.gui.items.upgrade.name"))
                .lore(INSTANCE.getConfig().getStringList("faction-collectors.gui.items.upgrade.lore"))
                .build();

        maxUpgradeItemStack = ItemStackBuilder.of(Material.matchMaterial(INSTANCE.getConfig().getString("faction-collectors.gui.items.max-upgrade.material")))
                .name(INSTANCE.getConfig().getString("faction-collectors.gui.items.max-upgrade.name"))
                .lore(INSTANCE.getConfig().getStringList("faction-collectors.gui.items.max-upgrade.lore"))
                .build();

        infoItemStack = ItemStackBuilder.of(Material.matchMaterial(INSTANCE.getConfig().getString("faction-collectors.gui.items.info.material")))
                .name(INSTANCE.getConfig().getString("faction-collectors.gui.items.info.name"))
                .lore(INSTANCE.getConfig().getStringList("faction-collectors.gui.items.info.lore"))
                .build();

    }
}

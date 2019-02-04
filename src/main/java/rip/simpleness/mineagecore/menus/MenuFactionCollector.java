package rip.simpleness.mineagecore.menus;

import com.massivecraft.factions.FPlayers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.github.paperspigot.Title;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.enums.CollectionType;
import rip.simpleness.mineagecore.enums.FactionCollectorUpgrade;
import rip.simpleness.mineagecore.modules.ModuleFactionCollector;
import rip.simpleness.mineagecore.objs.FactionCollector;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;

public class MenuFactionCollector extends Menu {

    private static final MineageCore INSTANCE = MineageCore.getInstance();

    private HashSet<UUID> viewers;
    private FactionCollector factionCollector;

    public MenuFactionCollector(FactionCollector factionCollector) {
        super("&eFaction Collector", 27);
        this.viewers = new HashSet<>();
        this.factionCollector = factionCollector;
        this.setupConsumers();
        this.setup();
    }

    private void setupConsumers() {
        this.setInventoryOpenEventConsumer(event -> viewers.add(event.getPlayer().getUniqueId()));
        this.setInventoryCloseEventConsumer(event -> viewers.remove(event.getPlayer().getUniqueId()));
    }

    @Override
    public void setup() {
        this.clearIcons();
        MenuIcon menuIcon = factionCollector.getFactionCollectorUpgrade().getRank() < 4 ? new MenuIcon(ModuleFactionCollector.buildUpgradeItemStack(factionCollector.getFactionCollectorUpgrade().getNext()), event -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            final FactionCollectorUpgrade next = factionCollector.getFactionCollectorUpgrade().getNext();
            if (next.getPrice() <= INSTANCE.getEconomy().getBalance(player)) {
                INSTANCE.getEconomy().withdrawPlayer(player, next.getPrice());
                player.sendTitle(Title.builder().title(Text.colorize("&c&l-$" + next.getPrice())).fadeIn(5).fadeOut(5).stay(25).build());
                factionCollector.upgrade();
            } else {
                player.sendTitle(Title.builder().title(Text.colorize("&cYou don't have enough money!")).fadeIn(5).fadeOut(5).stay(25).build());
            }
        }) : new MenuIcon(ModuleFactionCollector.maxUpgradeItemStack, event -> event.setCancelled(true));

        setItem(0, menuIcon);
        setItem(4, ModuleFactionCollector.infoItemStack);

        IntStream.range(0, getSize())
                .filter(i -> (i >= 1 && i <= 3) || (i >= 5 && i <= 8) || (i >= 18 && i <= 20) || (i == 22) || (i >= 24 && i <= getSize()))
                .forEach(i -> setItem(i, ItemStackBuilder.of(Material.STAINED_GLASS_PANE)
                        .data(DyeColor.LIME.getData())
                        .name(" ")
                        .build()));

        factionCollector.getFactionCollectorUpgrade().getApplicables().forEach(collectionType -> setItem(getFirstEmpty(), new MenuIcon(collectionType.buildItemStack(factionCollector.getAmounts().getOrDefault(collectionType, 0)), event -> {
            event.setCancelled(true);
            int amount = factionCollector.getAmounts().get(collectionType);
            if (amount >= 0) {
                Player player = (Player) event.getWhoClicked();
                int remainder = sub10OrReturn0(amount, collectionType == CollectionType.TNT ? 64 : 100), amountToBeSubtracted = collectionType == CollectionType.TNT ? 64 : 100;
                if (remainder > 0) amountToBeSubtracted = remainder;
                if (collectionType == CollectionType.TNT) {
                    FPlayers.getInstance().getByPlayer(player).getFaction().depositTnt(amountToBeSubtracted);
                } else {
                    double shmoney = (collectionType.getValue() * amountToBeSubtracted);
                    INSTANCE.getEconomy().depositPlayer(player, shmoney);
                    player.sendTitle(Title.builder().title(Text.colorize("&a&l+$" + shmoney)).fadeIn(5).fadeOut(5).stay(25).build());
                }
                factionCollector.removeAmount(collectionType, amountToBeSubtracted);
                MenuFactionCollector.this.update(getFirstEmpty(), collectionType);
            }
        })));
        while (getFirstEmpty() > -1) {
            MenuIcon barrier = new MenuIcon(ItemStackBuilder.of(Material.BARRIER).name("&cLocked, upgrade your Faction Collector to unlock").build());
            barrier.setEvent(event -> event.setCancelled(true));
            setItem(getFirstEmpty(), barrier);
        }
    }

    public void update(CollectionType collectionType) {
        getMenuIcons().forEach((key, menuIcon) -> {
            if (menuIcon.getItemStack() != null) {
                if (menuIcon.getItemStack().getType() == Material.MONSTER_EGG) {
                    SpawnEgg spawnEgg = (SpawnEgg) menuIcon.getItemStack().getData();
                    EntityType spawnedType = spawnEgg.getSpawnedType();
                    if (spawnedType == collectionType.parseEntityType() && collectionType == CollectionType.fromEntityType(spawnedType)) {
                        update(key, collectionType);
                    }
                } else if (menuIcon.getItemStack().getType() == Material.CACTUS && collectionType == CollectionType.CACTUS) {
                    update(key, collectionType);
                } else if (menuIcon.getItemStack().getType() == Material.SUGAR_CANE && collectionType == CollectionType.SUGAR_CANE) {
                    update(key, collectionType);
                }
            }
        });
    }

    public void update(int slot, CollectionType collectionType) {
        final ItemStack itemStack = collectionType.buildItemStack(factionCollector.getAmounts().getOrDefault(collectionType, 0));
        getMenuIcon(slot).ifPresent(menuIcon -> menuIcon.setItemStack(itemStack));
        getViewers().forEach(viewer -> Bukkit.getPlayer(viewer).updateInventory());
    }

    public HashSet<UUID> getViewers() {
        return viewers;
    }

    private int sub10OrReturn0(int i, int divisor) {
        return i < 0 ? -1 : i % divisor > 0 && i < divisor ? i % divisor : 0;
    }
}

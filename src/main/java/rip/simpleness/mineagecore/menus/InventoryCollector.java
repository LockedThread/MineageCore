package rip.simpleness.mineagecore.menus;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.paperspigot.Title;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.enums.CollectionType;
import rip.simpleness.mineagecore.enums.FactionCollectorUpgrade;
import rip.simpleness.mineagecore.modules.ModuleFactionCollector;
import rip.simpleness.mineagecore.objs.FactionCollector;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InventoryCollector extends CustomInventory {

    public static final transient Int2ObjectMap<CollectionType> LOCATIONS = Int2ObjectMaps.emptyMap();
    private static final MineageCore INSTANCE = MineageCore.getInstance();
    private FactionCollector factionCollector;

    public InventoryCollector(FactionCollector factionCollector) {
        super(27, "      &eFaction Collector");
        this.factionCollector = factionCollector;
    }

    @Override
    public void setup() {
        clearInventory();

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
                player.closeInventory();
            }
        }) : new MenuIcon(ModuleFactionCollector.maxUpgradeItemStack, event -> event.setCancelled(true));

        setIcon(0, menuIcon);
        setIcon(4, ModuleFactionCollector.infoItemStack);

        IntStream.range(0, getSize())
                .filter(i -> (i >= 1 && i <= 3) || (i >= 5 && i <= 8) || (i >= 18 && i <= 20) || (i == 22) || (i >= 24 && i <= getSize()))
                .forEach(i -> setIcon(i, ItemStackBuilder.of(Material.STAINED_GLASS_PANE)
                        .data(DyeColor.LIME.getData())
                        .name(" ")
                        .build()));

        LOCATIONS.int2ObjectEntrySet().forEach(entry -> {
            CollectionType collectionType = entry.getValue();
            int slot = entry.getIntKey();
            if (factionCollector.getFactionCollectorUpgrade().getApplicables().contains(collectionType)) {
                int amount = factionCollector.getAmounts().getOrDefault(collectionType, 0);
                setIcon(getFirstEmpty(), new MenuIcon(collectionType.buildItemStack(amount), event -> {
                    event.setCancelled(true);
                    if (amount >= 0) {
                        int remainder = sub10OrReturn0(amount, collectionType == CollectionType.TNT ? 64 : 100), amountToBeSubtracted = collectionType == CollectionType.TNT ? 64 : 100;
                        if (remainder > 0) amountToBeSubtracted = remainder;
                        Player player = (Player) event.getWhoClicked();
                        if (collectionType == CollectionType.TNT) {
                            Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                            faction.setTntBankBalance(faction.getTntBankBalance() + amountToBeSubtracted);
                        } else {
                            double shmoney = (collectionType.getValue() * amountToBeSubtracted);
                            INSTANCE.getEconomy().depositPlayer(player, shmoney);
                            player.sendTitle(Title.builder().title(Text.colorize("&a&l+$" + shmoney)).fadeIn(5).fadeOut(5).stay(25).build());
                        }
                        factionCollector.removeAmount(collectionType, amountToBeSubtracted);
                        if (slot > -1) {
                            update(slot, collectionType);
                        }
                    }
                }));
            } else {
                setIcon(getFirstEmpty(), new MenuIcon(ItemStackBuilder.of(Material.BARRIER).name("&cLocked, upgrade your Faction Collector to unlock").build()).setEvent(event -> event.setCancelled(true)));
            }
        });
    }

    private void update(int slot, CollectionType collectionType) {
        updateIcon(slot, itemStack -> {
            ItemMeta meta = itemStack.getItemMeta();
            meta.setLore(INSTANCE.getConfig().getStringList("faction-collectors.gui.items.collection-type-format.lore")
                    .stream()
                    .map(s -> s.replace("{amount}", String.valueOf(factionCollector.getAmounts().getOrDefault(collectionType, 0))))
                    .collect(Collectors.toList()));
            itemStack.setItemMeta(meta);
        });
    }

    private int sub10OrReturn0(int i, int divisor) {
        return i < 0 ? -1 : i % divisor > 0 && i < divisor ? i % divisor : 0;
    }
}

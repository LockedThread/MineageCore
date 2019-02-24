package rip.simpleness.mineagecore.menus;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.paperspigot.Title;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.enums.CollectionType;
import rip.simpleness.mineagecore.enums.FactionCollectorUpgrade;
import rip.simpleness.mineagecore.modules.ModuleFactionCollector;
import rip.simpleness.mineagecore.objs.FactionCollector;
import rip.simpleness.outpost.SimpleOutpost;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MenuCollector extends Menu {

    public static final transient Object2IntOpenHashMap<CollectionType> LOCATIONS = new Object2IntOpenHashMap<>();
    private static final MineageCore INSTANCE = MineageCore.getInstance();
    private FactionCollector factionCollector;

    public MenuCollector(FactionCollector factionCollector) {
        super(27, "      &eFaction Collector");
        this.factionCollector = factionCollector;
        setup();
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

        LOCATIONS.object2IntEntrySet().forEach(entry -> {
            CollectionType collectionType = entry.getKey();
            int slot = entry.getIntValue();
            if (factionCollector.getFactionCollectorUpgrade().getApplicables().contains(collectionType)) {
                setIcon(slot, new MenuIcon(collectionType.buildItemStack(factionCollector.getAmounts().getOrDefault(collectionType, 0)), event -> {
                    int amount = factionCollector.getAmounts().getOrDefault(collectionType, 0);
                    event.setCancelled(true);
                    if (amount > 0) {
                        int remainder = sub10OrReturn0(amount, collectionType == CollectionType.TNT ? 64 : 100), amountToBeSubtracted = collectionType == CollectionType.TNT ? 64 : 100;
                        if (remainder > 0) amountToBeSubtracted = remainder;
                        Player player = (Player) event.getWhoClicked();
                        if (collectionType == CollectionType.TNT) {
                            Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                            faction.setTntBankBalance(faction.getTntBankBalance() + amountToBeSubtracted);
                        } else {
                            double shmoney = (collectionType.getValue() * amountToBeSubtracted);

                            shmoney *= SimpleOutpost.getInstance().getOutpostHashMap()
                                    .values()
                                    .stream()
                                    .filter(outpost -> outpost.getFaction().getOnlinePlayers().contains(player))
                                    .mapToDouble(outpost -> 2)
                                    .reduce(1, (a, b) -> a * b);

                            INSTANCE.getEconomy().depositPlayer(player, shmoney);
                            player.sendTitle(Title.builder().title(Text.colorize("&a&l+$" + shmoney)).fadeIn(5).fadeOut(5).stay(25).build());
                        }
                        update(slot, collectionType, amount - amountToBeSubtracted);
                        factionCollector.removeAmount(collectionType, amountToBeSubtracted);
                    }
                }));
            } else {
                setIcon(slot, new MenuIcon(ItemStackBuilder.of(Material.BARRIER).name("&cLocked, upgrade your Faction Collector to unlock").build()).setEvent(event -> event.setCancelled(true)));
            }
        });
    }

    public void update(int slot, CollectionType collectionType, int replace) {
        List<String> stringList = collectionType.getItemStack().getItemMeta().getLore().stream().map(s -> Text.colorize(s.replace("{amount}", String.valueOf(replace)))).collect(Collectors.toList());

        ItemStack item = getItem(slot).clone();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(stringList);
        item.setItemMeta(itemMeta);

        getInventory().setItem(slot, item);
        setIcon(slot, getIcon(slot).setItemStack(item));
        getInventory().getViewers().forEach(viewer -> ((Player) viewer).updateInventory());
    }

    private int sub10OrReturn0(int i, int divisor) {
        return i < 0 ? -1 : i % divisor > 0 && i < divisor ? i % divisor : 0;
    }
}

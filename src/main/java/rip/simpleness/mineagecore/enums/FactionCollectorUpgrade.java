package rip.simpleness.mineagecore.enums;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rip.simpleness.mineagecore.modules.ModuleFactionCollector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public enum FactionCollectorUpgrade {

    DEFAULT(1, CollectionType.CACTUS, CollectionType.SUGAR_CANE),
    _1(2, CollectionType.COW, CollectionType.PIG, CollectionType.CHICKEN),
    _2(3, CollectionType.PIG_ZOMBIE, CollectionType.ENDERMAN, CollectionType.CREEPER),
    _3(4, CollectionType.IRON_GOLEM, CollectionType.VILLAGER, CollectionType.WITCH);

    private double price;
    private int rank;
    private CollectionType[] collectionTypes, nonApplicables;
    private HashSet<CollectionType> applicables;

    FactionCollectorUpgrade(int rank, CollectionType... collectionTypes) {
        this.rank = rank;
        this.collectionTypes = collectionTypes;
    }

    public void init() {
        this.nonApplicables = Arrays.stream(values())
                .filter(value -> value.getRank() > getRank())
                .flatMap(value -> Arrays.stream(value.getCollectionTypes()))
                .toArray(CollectionType[]::new);
        this.applicables = Arrays.stream(values())
                .filter(value -> value.getRank() <= getRank())
                .flatMap(value -> Arrays.stream(value.getCollectionTypes()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public int getRank() {
        return rank;
    }

    public CollectionType[] getCollectionTypes() {
        return collectionTypes;
    }

    @Override
    public String toString() {
        return name();
    }

    public CollectionType[] getNonApplicables() {
        return nonApplicables;
    }

    public HashSet<CollectionType> getApplicables() {
        return applicables;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public FactionCollectorUpgrade getNext() {
        return Arrays.stream(FactionCollectorUpgrade.values())
                .filter(fcu -> fcu.getRank() - 1 == getRank())
                .findFirst()
                .orElse(null);
    }

    public ItemStack buildUpgradeItemStack() {
        ItemStack item = ModuleFactionCollector.upgradeItemStack.clone();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(itemMeta.getLore()
                .stream()
                .map(s -> s.replace("{level}", String.valueOf(getNext().getRank())).replace("{price}", String.valueOf(getNext().getPrice())))
                .collect(Collectors.toList()));
        item.setItemMeta(itemMeta);
        return item;
    }
}

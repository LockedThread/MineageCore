package rip.simpleness.mineagecore.objs;

import com.google.gson.annotations.SerializedName;
import me.lucko.helper.serialize.BlockPosition;
import org.bukkit.Location;
import rip.simpleness.mineagecore.enums.CollectionType;
import rip.simpleness.mineagecore.enums.FactionCollectorUpgrade;
import rip.simpleness.mineagecore.menus.InventoryCollector;

import java.util.HashMap;
import java.util.Map;

public final class FactionCollector {

    @SerializedName("position")
    private BlockPosition blockPosition;
    @SerializedName("upgrade")
    private FactionCollectorUpgrade factionCollectorUpgrade;

    private HashMap<CollectionType, Integer> amounts;

    private transient InventoryCollector inventoryCollector;

    public FactionCollector(Location location) {
        this(BlockPosition.of(location), FactionCollectorUpgrade.DEFAULT, new HashMap<>());
    }

    public FactionCollector(BlockPosition blockPosition, FactionCollectorUpgrade factionCollectorUpgrade, HashMap<CollectionType, Integer> amounts) {
        this.blockPosition = blockPosition;
        this.factionCollectorUpgrade = factionCollectorUpgrade;
        this.amounts = amounts;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public FactionCollectorUpgrade getFactionCollectorUpgrade() {
        return factionCollectorUpgrade;
    }

    public void setFactionCollectorUpgrade(FactionCollectorUpgrade factionCollectorUpgrade) {
        this.factionCollectorUpgrade = factionCollectorUpgrade;
    }

    public HashMap<CollectionType, Integer> getAmounts() {
        return amounts;
    }

    public void removeAmount(CollectionType collectionType, int amount) {
        getAmounts().computeIfPresent(collectionType, (collectionType1, integer) -> integer = integer - amount);
    }

    public void addAmount(CollectionType collectionType, int amount) {
        getAmounts().computeIfPresent(collectionType, (collectionType1, integer) -> integer = amount + integer);
        getAmounts().putIfAbsent(collectionType, 1);
    }

    public void reset(CollectionType collectionType) {
        amounts.put(collectionType, 0);
    }

    public void reset() {
        amounts.clear();
    }

    public void resetWithBlacklist(CollectionType collectionType) {
        amounts.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != collectionType)
                .map(Map.Entry::getKey)
                .forEach(this::reset);
    }

    public void upgrade() {
        this.factionCollectorUpgrade = factionCollectorUpgrade.getNext();
        getMenuFactionCollector().setup();
    }

    @Override
    public String toString() {
        return "FactionCollector{" + "blockPosition=" + blockPosition +
                ", factionCollectorUpgrade=" + factionCollectorUpgrade +
                ", amounts=" + amounts +
                '}';
    }

    public InventoryCollector getMenuFactionCollector() {
        if (inventoryCollector == null) {
            this.inventoryCollector = new InventoryCollector(this);
        }
        return this.inventoryCollector;
    }
}

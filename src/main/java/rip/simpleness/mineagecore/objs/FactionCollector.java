package rip.simpleness.mineagecore.objs;

import com.google.gson.annotations.SerializedName;
import me.lucko.helper.serialize.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import rip.simpleness.mineagecore.enums.CollectionType;
import rip.simpleness.mineagecore.enums.FactionCollectorUpgrade;
import rip.simpleness.mineagecore.menus.MenuFactionCollector;

import java.util.HashMap;
import java.util.Map;

public final class FactionCollector {

    @SerializedName("position")
    private BlockPosition blockPosition;
    @SerializedName("upgrade")
    private FactionCollectorUpgrade factionCollectorUpgrade;

    private HashMap<CollectionType, Integer> amounts;

    private transient MenuFactionCollector menuFactionCollector;

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
        getMenuFactionCollector().update(collectionType);
    }

    public void addAmount(CollectionType collectionType, int amount) {
        getAmounts().computeIfPresent(collectionType, (collectionType1, integer) -> integer = amount + integer);
        getAmounts().putIfAbsent(collectionType, 1);
        getMenuFactionCollector().update(collectionType);
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
        getMenuFactionCollector().getViewers().forEach(viewer -> Bukkit.getPlayer(viewer).updateInventory());
    }

    @Override
    public String toString() {
        return "FactionCollector{" + "blockPosition=" + blockPosition +
                ", factionCollectorUpgrade=" + factionCollectorUpgrade +
                ", amounts=" + amounts +
                '}';
    }

    public MenuFactionCollector getMenuFactionCollector() {
        if (menuFactionCollector == null) {
            this.menuFactionCollector = new MenuFactionCollector(this);
        }
        return this.menuFactionCollector;
    }
}

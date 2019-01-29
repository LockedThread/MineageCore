package rip.simpleness.mineagecore.utils;

import org.bukkit.Location;

public final class LocationUtils {

    private LocationUtils() {
    }

    public static boolean isSimilar(Location a, Location b) {
        return a.getWorld().getName().equals(b.getWorld().getName()) && a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }

}

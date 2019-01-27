package rip.simpleness.mineagecore.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ItemUtils {

    private ItemUtils() {
    }

    public static void useItemInHand(Player player) {
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand.getAmount() == 1) {
            player.setItemInHand(null);
        } else {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        }
    }
}

package rip.simpleness.mineagecore.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player &&
                event.getClickedInventory() != null &&
                event.getCurrentItem() != null &&
                event.getClickedInventory().getHolder() instanceof Menu) {
            final MenuIcon icon = ((Menu) event.getInventory().getHolder()).getIcon(event.getRawSlot());
            if (icon != null && icon.getEvent() != null) {
                icon.getEvent().accept(event);
            }
        }
    }
}

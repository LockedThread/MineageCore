package rip.simpleness.mineagecore.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player &&
                event.getInventory() != null &&
                event.getInventory().getHolder() instanceof Menu) {
            Menu menu = (Menu) event.getInventory().getHolder();
            if (menu.getInventoryCloseEventConsumer() != null) {
                menu.getInventoryCloseEventConsumer().accept(event);
            }
        }
    }
}

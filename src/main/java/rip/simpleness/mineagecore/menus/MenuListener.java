package rip.simpleness.mineagecore.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.function.Consumer;

public class MenuListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getClickedInventory() != null && event.getCurrentItem() != null && event.getClickedInventory().getHolder() instanceof Menu) {
            ((Menu) event.getInventory().getHolder()).getMenuIcon(event.getSlot()).ifPresent(menuIcon -> {
                Consumer<InventoryClickEvent> consumer = menuIcon.getEvent();
                if (consumer != null) {
                    consumer.accept(event);
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory() != null && event.getInventory().getHolder() instanceof Menu) {
            Consumer<InventoryOpenEvent> consumer = ((Menu) event.getInventory().getHolder()).getInventoryOpenEventConsumer();
            if (consumer != null) {
                consumer.accept(event);
                System.out.println("opened menu and called consumer");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != null && event.getInventory().getHolder() instanceof Menu) {
            Consumer<InventoryCloseEvent> consumer = ((Menu) event.getInventory().getHolder()).getInventoryCloseEventConsumer();
            if (consumer != null) {
                consumer.accept(event);
            }
        }
    }
}

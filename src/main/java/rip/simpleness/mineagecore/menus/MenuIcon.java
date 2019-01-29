package rip.simpleness.mineagecore.menus;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;

public class MenuIcon {

    private ItemStack itemStack;
    private Consumer<InventoryClickEvent> event;


    public MenuIcon(ItemStack itemStack, Consumer<InventoryClickEvent> event) {
        this.event = event;
        this.itemStack = itemStack;
    }

    public MenuIcon(ItemStack itemStack) {
        this(itemStack, inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Consumer<InventoryClickEvent> getEvent() {
        return event;
    }

    public void setEvent(Consumer<InventoryClickEvent> event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuIcon menuIcon = (MenuIcon) o;

        return Objects.equals(itemStack, menuIcon.itemStack) && Objects.equals(event, menuIcon.event);
    }

    @Override
    public int hashCode() {
        int result = itemStack != null ? itemStack.hashCode() : 0;
        result = 31 * result + (event != null ? event.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MenuIcon{" +
                "itemStack=" + itemStack +
                ", event=" + event +
                '}';
    }
}

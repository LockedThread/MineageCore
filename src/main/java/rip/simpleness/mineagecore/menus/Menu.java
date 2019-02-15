package rip.simpleness.mineagecore.menus;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public abstract class Menu implements InventoryHolder {

    private Inventory inventory;
    private Int2ObjectOpenHashMap<MenuIcon> menuIcons;
    private Consumer<InventoryCloseEvent> inventoryCloseEventConsumer;

    public Menu(int size, String name) {
        this.inventory = Bukkit.createInventory(this, size, Text.colorize(name));
        this.menuIcons = new Int2ObjectOpenHashMap<>();
    }

    public abstract void setup();

    public void clearInventory() {
        inventory.clear();
        menuIcons.clear();
    }

    public MenuIcon getIcon(int slot) {
        return menuIcons.get(slot);
    }

    public ItemStack getItem(int slot) {
        return inventory.getItem(slot);
    }

    public void setIcon(int slot, MenuIcon menuIcon) {
        menuIcons.put(slot, menuIcon);
        inventory.setItem(slot, menuIcon.getItemStack());
    }

    public void setIcon(int slot, ItemStack itemStack) {
        menuIcons.put(slot, new MenuIcon(itemStack));
        inventory.setItem(slot, itemStack);
    }

    public int getSize() {
        return inventory.getSize();
    }

    public int getFirstEmpty() {
        return inventory.firstEmpty();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Consumer<InventoryCloseEvent> getInventoryCloseEventConsumer() {
        return inventoryCloseEventConsumer;
    }

    public void setInventoryCloseEventConsumer(Consumer<InventoryCloseEvent> inventoryCloseEventConsumer) {
        this.inventoryCloseEventConsumer = inventoryCloseEventConsumer;
    }
}

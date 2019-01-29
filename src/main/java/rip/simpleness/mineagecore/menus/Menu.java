package rip.simpleness.mineagecore.menus;

import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class Menu implements InventoryHolder {

    private String name;
    private int size;
    private Consumer<InventoryCloseEvent> inventoryCloseEventConsumer;
    private Consumer<InventoryOpenEvent> inventoryOpenEventConsumer;
    private HashMap<Integer, MenuIcon> menuIcons;
    private Inventory inventory;

    public Menu(@Nonnull String name, int size) {
        this.name = name;
        this.size = size;
        this.menuIcons = new HashMap<>();
        this.inventory = Bukkit.createInventory(this, size, Text.colorize(name));
    }

    public Menu(String name, InventoryType inventoryType) {
        this(name, inventoryType.getDefaultSize());
    }

    public abstract void setup();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Consumer<InventoryOpenEvent> getInventoryOpenEventConsumer() {
        return inventoryOpenEventConsumer;
    }

    public void setInventoryOpenEventConsumer(Consumer<InventoryOpenEvent> inventoryOpenEventConsumer) {
        this.inventoryOpenEventConsumer = inventoryOpenEventConsumer;
    }

    public Consumer<InventoryCloseEvent> getInventoryCloseEventConsumer() {
        return inventoryCloseEventConsumer;
    }

    public void setInventoryCloseEventConsumer(Consumer<InventoryCloseEvent> inventoryCloseEventConsumer) {
        this.inventoryCloseEventConsumer = inventoryCloseEventConsumer;
    }

    public HashMap<Integer, MenuIcon> getMenuIcons() {
        return menuIcons;
    }

    public void setItem(int slot, MenuIcon menuIcon) {
        menuIcons.put(slot, menuIcon);
        inventory.setItem(slot, menuIcon.getItemStack());
    }

    public void setItem(int slot, ItemStack itemStack) {
        menuIcons.put(slot, new MenuIcon(itemStack));
        inventory.setItem(slot, itemStack);
    }

    public int getFirstEmpty() {
        return inventory.firstEmpty();
    }

    public Optional<MenuIcon> getMenuIcon(int slot) {
        return Optional.ofNullable(menuIcons.get(slot));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Menu menu = (Menu) o;

        return size == menu.size && Objects.equals(name, menu.name) && Objects.equals(inventoryCloseEventConsumer, menu.inventoryCloseEventConsumer) && Objects.equals(inventoryOpenEventConsumer, menu.inventoryOpenEventConsumer) && Objects.equals(menuIcons, menu.menuIcons);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + size;
        result = 31 * result + (inventoryCloseEventConsumer != null ? inventoryCloseEventConsumer.hashCode() : 0);
        result = 31 * result + (inventoryOpenEventConsumer != null ? inventoryOpenEventConsumer.hashCode() : 0);
        result = 31 * result + (menuIcons != null ? menuIcons.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", inventoryCloseEventConsumer=" + inventoryCloseEventConsumer +
                ", inventoryOpenEventConsumer=" + inventoryOpenEventConsumer +
                ", menuIcons=" + menuIcons +
                '}';
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public void clearIcons() {
        menuIcons.clear();
        inventory.clear();
    }
}

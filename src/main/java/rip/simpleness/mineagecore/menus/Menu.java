package rip.simpleness.mineagecore.menus;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public abstract class Menu implements InventoryHolder {

    private Inventory inventory;
    private Int2ObjectOpenHashMap<MenuIcon> menuIcons;

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
}

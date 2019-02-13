package rip.simpleness.mineagecore.menus;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public abstract class CustomInventory implements InventoryHolder {

    private Inventory inventory;
    private Int2ObjectMap<MenuIcon> menuIcons;

    public CustomInventory(int size, String name) {
        this.inventory = Bukkit.createInventory(this, size, Text.colorize(name));
        this.menuIcons = Int2ObjectMaps.emptyMap();
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

    public void updateIcon(int slot, Consumer<ItemStack> itemStackConsumer) {
        setIcon(slot, getIcon(slot).setItemStack(ItemStackBuilder.of(getItem(slot)).transform(itemStackConsumer).build()));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

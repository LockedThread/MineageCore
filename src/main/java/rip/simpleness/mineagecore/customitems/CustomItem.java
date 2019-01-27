package rip.simpleness.mineagecore.customitems;

import org.bukkit.inventory.ItemStack;
import rip.simpleness.mineagecore.utils.NMSUtils;

import java.util.HashMap;

public class CustomItem {

    private static HashMap<String, CustomItem> customItemHashMap = new HashMap<>();
    private String id;
    private ItemStack itemStack;

    public CustomItem(String id, ItemStack itemStack) {
        this.id = id;
        this.itemStack = NMSUtils.attachItemTag(itemStack, "customitem", id);
        customItemHashMap.put(id, this);
    }

    public static HashMap<String, CustomItem> getCustomItemHashMap() {
        return customItemHashMap;
    }

    public static CustomItem getCustomItem(ItemStack itemStack) {
        String customitem = NMSUtils.getItemTagString(itemStack, "customitem", "");
        return customitem.isEmpty() ? null : customItemHashMap.get(customitem);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public boolean isCustomItem(ItemStack itemStack) {
        if (itemStack != null) {
            String customitem = NMSUtils.getItemTagString(itemStack, "customitem", "");
            return !customitem.isEmpty() && this.id.equalsIgnoreCase(customitem);
        }
        return false;
    }

}

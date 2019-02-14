package rip.simpleness.mineagecore.menus;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import rip.simpleness.mineagecore.customitems.CustomItem;
import rip.simpleness.mineagecore.modules.ModuleGenBlock;

public class MenuGenBlock extends Menu {

    public MenuGenBlock() {
        super(9, "&cGenBlock Shop");
        setup();
    }

    @Override
    public void setup() {
        final ItemStack pane = ItemStackBuilder.of(Material.STAINED_GLASS_PANE)
                .data(DyeColor.BLACK.getData())
                .flag(ItemFlag.HIDE_ENCHANTS)
                .enchant(Enchantment.DURABILITY)
                .name(" ")
                .build();
        setIcon(0, pane);
        setIcon(8, pane);
        ModuleGenBlock.genBlockMap.forEach((key, value) -> setIcon(getFirstEmpty(), new MenuIcon(value.buildGuiItemStack()).setEvent(event -> {
            if (event.getWhoClicked().getInventory().firstEmpty() == -1) {
                event.getWhoClicked().sendMessage(Text.colorize("&cYour inventory is full!"));
                event.getWhoClicked().closeInventory();
            } else {
                event.getWhoClicked().getInventory().addItem(CustomItem.getCustomItemHashMap().get(key).getItemStack());
            }
            event.setCancelled(true);
        })));
    }
}

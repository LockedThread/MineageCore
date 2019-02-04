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
import rip.simpleness.mineagecore.objs.GenBlock;

import java.util.Map;

public class MenuGenBlock extends Menu {

    public MenuGenBlock() {
        super("&cGenBlock Shop", 9);
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
        setItem(0, pane);
        setItem(8, pane);
        for (Map.Entry<String, GenBlock> entry : ModuleGenBlock.genBlockMap.entrySet()) {
            setItem(getFirstEmpty(), new MenuIcon(entry.getValue().buildGuiItemStack()).setEvent(event -> {
                if (event.getWhoClicked().getInventory().firstEmpty() == -1) {
                    event.getWhoClicked().sendMessage(Text.colorize("&cYour inventory is full!"));
                    event.getWhoClicked().closeInventory();
                } else {
                    event.getWhoClicked().getInventory().addItem(CustomItem.getCustomItemHashMap().get(entry.getKey()).getItemStack());
                }
                event.setCancelled(true);
            }));
        }
    }
}

package rip.simpleness.mineagecore.menus;

import me.lucko.helper.text.Text;
import rip.simpleness.mineagecore.modules.ModuleGenBlock;

public class MenuGenBlock extends Menu {

    public MenuGenBlock() {
        super("&cGenBlock Shop", 9);
        setup();
    }

    @Override
    public void setup() {
        ModuleGenBlock.genBlockMap
                .values()
                .forEach(value -> setItem(getFirstEmpty(), new MenuIcon(value.buildGuiItemStack()).setEvent(event -> {
                    if (event.getWhoClicked().getInventory().firstEmpty() == -1) {
                        event.getWhoClicked().sendMessage(Text.colorize("&cYour inventory is full!"));
                        event.getWhoClicked().closeInventory();
                    } else {
                        event.getWhoClicked().getInventory().addItem(value.buildItemStack());
                    }
                    event.setCancelled(true);
                })));
    }
}

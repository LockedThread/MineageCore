package rip.simpleness.mineagecore.objs;

import me.lucko.helper.item.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.enums.Direction;

public final class GenBlock {

    private Material material;
    private Direction direction;
    private double price;

    public GenBlock(Material material, Direction direction, double price) {
        this.material = material;
        this.direction = direction;
        this.price = price;
    }

    public Material getMaterial() {
        return material;
    }

    public Direction getDirection() {
        return direction;
    }

    public ItemStack buildItemStack() {
        return ItemStackBuilder.of(material)
                .name("&e" + (direction == Direction.UP || direction == Direction.DOWN ? "Vertical" : "Horizontal") + " &e" + MineageCore.getInstance().capitalizeEveryWord(material.name().replace("_", " ")))
                .lore("", "&ePlace this genblock to start the generation process!", "")
                .enchant(Enchantment.DURABILITY)
                .flag(ItemFlag.HIDE_ENCHANTS)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenBlock genBlock = (GenBlock) o;
        return material == genBlock.material && direction == genBlock.direction;
    }

    @Override
    public int hashCode() {
        int result = material != null ? material.hashCode() : 0;
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GenBlock{" +
                "material=" + material +
                ", direction=" + direction +
                '}';
    }

    public double getPrice() {
        return price;
    }
}

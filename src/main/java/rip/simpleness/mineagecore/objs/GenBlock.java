package rip.simpleness.mineagecore.objs;

import me.lucko.helper.item.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.enums.Direction;

public final class GenBlock {

    private final boolean patch;
    private final Material material;
    private final Direction direction;
    private final double price;

    public GenBlock(Material material, Direction direction, double price) {
        this(material, direction, price, false);
    }

    public GenBlock(Material material, Direction direction, double price, boolean patch) {
        this.material = material;
        this.direction = direction;
        this.price = price;
        this.patch = patch;
    }


    public Material getMaterial() {
        return material;
    }

    public Direction getDirection() {
        return direction;
    }

    public ItemStack buildItemStack() {
        String directionString = this.direction == Direction.UP || this.direction == Direction.DOWN ? "Vertical" : "Horizontal";
        return ItemStackBuilder.of(material)
                .name("&e" + directionString + " " + MineageCore.getInstance().capitalizeEveryWord(material.name().replace("_", " ")) + (isPatch() ? " PatchBlock" : ""))
                .lore("", "&ePlace this genblock to start the generation process!", "")
                .enchant(Enchantment.DURABILITY)
                .flag(ItemFlag.HIDE_ENCHANTS)
                .build();
    }

    public ItemStack buildGuiItemStack() {
        String directionString = this.direction == Direction.UP || this.direction == Direction.DOWN ? "Vertical" : "Horizontal";
        return ItemStackBuilder.of(material)
                .name("&e" + directionString + " " + MineageCore.getInstance().capitalizeEveryWord(material.name().replace("_", " ")) + (isPatch() ? "PatchBlock" : ""))
                .lore("", "&cPrice per place: " + price, "")
                .enchant(Enchantment.DURABILITY)
                .flag(ItemFlag.HIDE_ENCHANTS)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenBlock genBlock = (GenBlock) o;

        return patch == genBlock.patch && Double.compare(genBlock.price, price) == 0 && material == genBlock.material && direction == genBlock.direction;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (patch ? 1 : 0);
        result = 31 * result + (material != null ? material.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "GenBlock{" +
                "patch=" + patch +
                ", material=" + material +
                ", direction=" + direction +
                ", price=" + price +
                '}';
    }

    public double getPrice() {
        return price;
    }

    public boolean isPatch() {
        return patch;
    }
}

package rip.simpleness.mineagecore.enums;

import me.lucko.helper.item.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import rip.simpleness.mineagecore.MineageCore;

import java.util.stream.Collectors;

public enum CollectionType {
    /*Items*/
    CACTUS(Material.CACTUS),
    SUGAR_CANE(Material.SUGAR_CANE),
    TNT(Material.TNT),

    /*Entity Types*/
    CREEPER(Material.TNT),
    SKELETON(EntityType.SKELETON),
    SPIDER(EntityType.SPIDER),
    GIANT(EntityType.GIANT),
    ZOMBIE(EntityType.ZOMBIE),
    SLIME(EntityType.SLIME),
    GHAST(EntityType.GHAST),
    PIG_ZOMBIE(EntityType.PIG_ZOMBIE),
    ENDERMAN(EntityType.ENDERMAN),
    CAVE_SPIDER(EntityType.CAVE_SPIDER),
    SILVERFISH(EntityType.SILVERFISH),
    BLAZE(EntityType.BLAZE),
    MAGMA_CUBE(EntityType.MAGMA_CUBE),
    ENDER_DRAGON(EntityType.ENDER_DRAGON),
    WITHER(EntityType.WITHER),
    BAT(EntityType.BAT),
    WITCH(EntityType.WITCH),
    ENDERMITE(EntityType.ENDERMITE),
    GUARDIAN(EntityType.GUARDIAN),
    PIG(EntityType.PIG),
    SHEEP(EntityType.SHEEP),
    COW(EntityType.COW),
    CHICKEN(EntityType.CHICKEN),
    SQUID(EntityType.SQUID),
    WOLF(EntityType.WOLF),
    MUSHROOM_COW(EntityType.MUSHROOM_COW),
    SNOWMAN(EntityType.SNOWMAN),
    OCELOT(EntityType.OCELOT),
    IRON_GOLEM(EntityType.IRON_GOLEM),
    HORSE(EntityType.HORSE),
    RABBIT(EntityType.RABBIT),
    VILLAGER(EntityType.VILLAGER),
    ENDER_CRYSTAL(EntityType.ENDER_CRYSTAL);

    private Material material = null;
    private EntityType entityType = null;
    private ItemStack itemStack;
    private ItemStack lockedItemStack;
    private double value;

    CollectionType(EntityType entityType) {
        this.entityType = entityType;
    }

    CollectionType(Material material) {
        this.material = material;
    }

    public static CollectionType fromEntityType(EntityType entityType) {
        try {
            return valueOf(entityType.name());
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public EntityType parseEntityType() {
        return entityType;
    }

    public Material parseMaterial() {
        return material;
    }

    @Override
    public String toString() {
        return name();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void init() {
        final ConfigurationSection section = MineageCore.getInstance().getConfig().getConfigurationSection("faction-collectors.gui.items");
        itemStack = parseMaterial() != null ? ItemStackBuilder.of(parseMaterial())
                .name(section.getString("collection-type-format.name").replace("{mob}", MineageCore.getInstance().capitalizeEveryWord(parseMaterial().name().replace("_", " "))))
                .lore(section.getStringList("collection-type-format.lore"))
                .amount(1)
                .build() : ItemStackBuilder.of(new SpawnEgg(parseEntityType()).toItemStack(1))
                .name(section.getString("collection-type-format.name").replace("{mob}", MineageCore.getInstance().capitalizeEveryWord(parseEntityType().name().replace("_", " "))))
                .lore(section.getStringList("collection-type-format.lore"))
                .build();

        lockedItemStack = parseMaterial() != null ? ItemStackBuilder.of(Material.IRON_FENCE)
                .name(section.getString("locked.name").replace("{mob}", MineageCore.getInstance().capitalizeEveryWord(parseMaterial().name().replace("_", " "))))
                .lore(section.getStringList("locked.lore"))
                .amount(1)
                .build() : ItemStackBuilder.of(Material.IRON_FENCE)
                .name(section.getString("locked.name").replace("{mob}", MineageCore.getInstance().capitalizeEveryWord(parseEntityType().name().replace("_", " "))))
                .lore(section.getStringList("locked.lore"))
                .amount(1)
                .build();
    }

    public ItemStack buildItemStack(int amount) {
        ItemStack item = itemStack.clone();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(itemMeta.getLore().stream().map(s -> s.replace("{amount}", String.valueOf(amount))).collect(Collectors.toList()));
        item.setItemMeta(itemMeta);
        return item;
    }

    public ItemStack buildLockedItemStack() {
        return lockedItemStack;
    }
}

package rip.simpleness.mineagecore.modules;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ModuleSilkSpawner implements TerminableModule {

    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Events.subscribe(BlockBreakEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getPlayer().getItemInHand() != null)
                .filter(event -> event.getPlayer().getItemInHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0)
                .filter(event -> MineageCore.getInstance().canBuild(event.getPlayer(), event.getBlock().getLocation()))
                .handler(event -> {
                    Player player = event.getPlayer();
                    if (!player.hasPermission("mineagepvp.silktouch")) {
                        player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&cYou must be the Knight rank or higher to silk touch spawners!"));
                        event.setCancelled(true);
                    } else {
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), buildSpawner(((CreatureSpawner) event.getBlock().getState()).getSpawnedType(), 1));
                    }
                }).bindWith(terminableConsumer);

        Commands.create()
                .assertPermission("mineagepvp.spawnergive")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        commandContext.reply("&e/givespawner [player] [entity-type] [amount]");
                    } else if (commandContext.args().size() >= 2) {
                        Player target = commandContext.arg(0).parse(Player.class).orElse(null);
                        if (target == null) {
                            commandContext.reply("&c" + commandContext.rawArg(0) + " can't be parsed as a Player!");
                        } else {
                            EntityType entityType = getEntityTypeFromString(commandContext.rawArg(1));
                            if (entityType == null) {
                                commandContext.reply("&c" + commandContext.rawArg(1) + " can't be parsed as an EntityType!");
                            } else {
                                target.getInventory().addItem(buildSpawner(entityType, commandContext.args().size() == 3 ? Integer.parseInt(commandContext.rawArg(2)) : 1));
                            }
                        }
                    }
                }).registerAndBind(terminableConsumer, "givespawner", "spawnergive");
    }

    private EntityType getEntityTypeFromString(String s) {
        EntityType entityType = EntityType.fromName(s);
        if (entityType != null) {
            return entityType;
        } else if (StringUtils.isNumeric(s)) {
            entityType = EntityType.fromId(Integer.parseInt(s));
            if (entityType != null) return entityType;
        }
        return Arrays.stream(EntityType.values())
                .filter(value -> value.name().replace("_", "").equalsIgnoreCase(s))
                .findFirst()
                .orElse(null);
    }

    private ItemStack buildSpawner(EntityType entityType, int amount) {
        ItemStack itemStack = new ItemStack(Material.MOB_SPAWNER, amount);
        BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
        blockStateMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e" + MineageCore.getInstance().capitalizeEveryWord(entityType.name().replace("_", " ")) + " Spawner"));
        CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
        creatureSpawner.setSpawnedType(entityType);
        blockStateMeta.setBlockState(creatureSpawner);
        itemStack.setItemMeta(blockStateMeta);
        return itemStack;
    }
}

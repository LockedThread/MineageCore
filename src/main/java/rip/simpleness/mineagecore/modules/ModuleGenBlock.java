package rip.simpleness.mineagecore.modules;

import com.google.common.collect.ImmutableMap;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.serialize.BlockPosition;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.customitems.CustomItem;
import rip.simpleness.mineagecore.enums.Direction;
import rip.simpleness.mineagecore.objs.GenBlock;
import rip.simpleness.mineagecore.objs.Generation;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ModuleGenBlock implements TerminableModule {

    private static final ImmutableMap<String, GenBlock> genBlockMap = ImmutableMap.of(
            "vertical-cobblestone", new GenBlock(Material.COBBLESTONE, Direction.DOWN, 50),
            "horizontal-cobblestone", new GenBlock(Material.COBBLESTONE, Direction.HORIZONTAL, 100),
            "vertical-obsidian", new GenBlock(Material.OBSIDIAN, Direction.DOWN, 2500),
            "horizontal-obsidian", new GenBlock(Material.OBSIDIAN, Direction.HORIZONTAL, 3000),
            "vertical-sand", new GenBlock(Material.SAND, Direction.UP, 2500));

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        genBlockMap.forEach((key, value) -> new CustomItem(key, value.buildItemStack()));
        Commands.create()
                .assertPermission("mineage.admin")
                .handler(commandContext -> {
                    Player player = (Player) commandContext.sender();
                    GenBlock genBlock = genBlockMap.get("vertical-cobblestone");
                    for (int x = -50; x < 50; x++) {
                        for (int z = -50; z < 50; z++) {
                            Block block = player.getLocation().add(x, 0, z).getBlock();
                            ((CraftBlock) block).setTypeIdAndData(genBlock.getMaterial().getId(), (byte) 0, false);
                            MineageCore.getInstance().getGenerationTask().getGenerations().put(UUID.randomUUID(), new Generation(genBlock, BlockPosition.of(block), BlockFace.DOWN));
                        }
                    }
                }).registerAndBind(terminableConsumer, "genblockbenchmark");

        Events.subscribe(BlockPlaceEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getItemInHand() != null)
                .filter(event -> event.getBlockPlaced() != null)
                .filter(event -> MineageCore.getInstance().canBuild(event.getPlayer(), event.getBlockPlaced().getLocation()))
                .handler(event -> {
                    CustomItem customItem = CustomItem.getCustomItem(event.getItemInHand());
                    if (customItem != null) {
                        GenBlock genBlock = genBlockMap.get(customItem.getId());
                        if (genBlock != null) {
                            Player player = event.getPlayer();
                            if (MineageCore.getInstance().getEconomy().getBalance(player) < genBlock.getPrice()) {
                                player.sendMessage(Text.colorize("&cYou don't have enough money to place this genblock!"));
                            } else {
                                MineageCore.getInstance().getEconomy().withdrawPlayer(player, genBlock.getPrice());
                                BlockFace blockFace = genBlock.getDirection().toBlockFace();
                                if (blockFace == null) {
                                    blockFace = genBlock.getDirection().toBlockFace(event.getBlockAgainst(), event.getBlockPlaced());
                                    MineageCore.getInstance()
                                            .getGenerationTask()
                                            .getGenerations()
                                            .put(UUID.randomUUID(), new Generation(genBlock, BlockPosition.of(event.getBlockPlaced()), blockFace));
                                }
                                MineageCore.getInstance()
                                        .getGenerationTask()
                                        .getGenerations()
                                        .put(UUID.randomUUID(), new Generation(genBlock, BlockPosition.of(event.getBlockPlaced()), blockFace));
                            }
                        }
                    }
                }).bindWith(terminableConsumer);
    }
}

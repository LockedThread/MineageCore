package rip.simpleness.mineagecore.modules;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.serialize.BlockPosition;
import me.lucko.helper.serialize.GsonStorageHandler;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.customitems.CustomItem;
import rip.simpleness.mineagecore.enums.Direction;
import rip.simpleness.mineagecore.objs.GenBlock;
import rip.simpleness.mineagecore.objs.Generation;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleGenBlock implements TerminableModule {

    private static final MineageCore INSTANCE = MineageCore.getInstance();
    private static final ImmutableMap<String, GenBlock> genBlockMap = ImmutableMap.<String, GenBlock>builder()
            .put("vertical-cobblestone", new GenBlock(Material.COBBLESTONE, Direction.DOWN, 50))
            .put("horizontal-cobblestone", new GenBlock(Material.COBBLESTONE, Direction.HORIZONTAL, 100))
            .put("vertical-obsidian", new GenBlock(Material.OBSIDIAN, Direction.DOWN, 2500))
            .put("horizontal-obsidian", new GenBlock(Material.OBSIDIAN, Direction.HORIZONTAL, 3000))
            .put("vertical-sand", new GenBlock(Material.SAND, Direction.UP, 2500))
            .put("vertical-cobblestone-patch", new GenBlock(Material.COBBLESTONE, Direction.DOWN, 1000, true))
            .put("vertical-obsidian-patch", new GenBlock(Material.OBSIDIAN, Direction.DOWN, 2500, true))
            .put("vertical-sand-cannon", new GenBlock(Material.SAND, Direction.UP, 5000, true))
            .build();

    private GsonStorageHandler<ConcurrentHashMap<UUID, Generation>> gsonStorageHandler;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.gsonStorageHandler = new GsonStorageHandler<>("genblocks", ".json", INSTANCE.getDataFolder(), new TypeToken<ConcurrentHashMap<UUID, Generation>>() {
        });
        INSTANCE.getGenerationTask().setGenerations(gsonStorageHandler.load().orElse(new ConcurrentHashMap<>()));
        terminableConsumer.bind(() -> gsonStorageHandler.save(INSTANCE.getGenerationTask().getGenerations()));

        genBlockMap.forEach((key, value) -> new CustomItem(key, value.buildItemStack()));
        Events.subscribe(BlockPlaceEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getItemInHand() != null)
                .filter(event -> event.getBlockPlaced() != null)
                .filter(event -> INSTANCE.canBuild(event.getPlayer(), event.getBlockPlaced().getLocation()))
                .handler(event -> {
                    CustomItem customItem = CustomItem.getCustomItem(event.getItemInHand());
                    if (customItem != null) {
                        GenBlock genBlock = genBlockMap.get(customItem.getId());
                        if (genBlock != null) {
                            Player player = event.getPlayer();
                            if (INSTANCE.getEconomy().getBalance(player) < genBlock.getPrice()) {
                                player.sendMessage(Text.colorize("&cYou don't have enough money to place this genblock!"));
                                event.setCancelled(true);
                            } else {
                                INSTANCE.getEconomy().withdrawPlayer(player, genBlock.getPrice());
                                BlockFace blockFace = genBlock.getDirection().toBlockFace();
                                if (blockFace == null) {
                                    blockFace = genBlock.getDirection().toBlockFace(event.getBlockAgainst(), event.getBlockPlaced());
                                    INSTANCE
                                            .getGenerationTask()
                                            .getGenerations()
                                            .put(UUID.randomUUID(), new Generation(genBlock, BlockPosition.of(event.getBlockPlaced()), blockFace));
                                }
                                INSTANCE
                                        .getGenerationTask()
                                        .getGenerations()
                                        .put(UUID.randomUUID(), new Generation(genBlock, BlockPosition.of(event.getBlockPlaced()), blockFace));
                            }
                        }
                    }
                }).bindWith(terminableConsumer);
    }
}

package rip.simpleness.mineagecore.modules;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.material.SpawnEgg;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;

public class ModulesFixes implements TerminableModule {

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Events.subscribe(EntityDamageEvent.class).filter(event -> event.getEntity().getType() == EntityType.DROPPED_ITEM).handler(event -> {
            if (((Item) event.getEntity()).getItemStack().getType() == Material.MOB_SPAWNER) event.setCancelled(true);
        }).bindWith(terminableConsumer);

        Events.subscribe(PlayerInteractEvent.class).filter(event -> event.hasItem() && event.getItem().getType() == Material.POTION).handler(event -> {
            switch (event.getItem().getDurability()) {
                case 8206:
                case 8270:
                case 16398:
                case 16462:
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&cInvisibility is Disabled."));
                    event.getItem().setDurability((short) 0);
            }
        }).bindWith(terminableConsumer);

        Events.subscribe(BlockPlaceEvent.class).filter(event -> event.getBlockPlaced().getType() == Material.BED_BLOCK).handler(event -> event.setCancelled(true)).bindWith(terminableConsumer);

        Events.subscribe(PlayerInteractEvent.class).filter(event -> event.getAction() == Action.RIGHT_CLICK_BLOCK)
                .filter(event -> event.hasItem() && event.getItem().getType() == Material.MONSTER_EGG && event.getItem().getData() instanceof SpawnEgg && event.getClickedBlock().getType() == Material.MOB_SPAWNER)
                .handler(event -> {
                    SpawnEgg egg = (SpawnEgg) event.getItem().getData();
                    if (egg.getSpawnedType() == EntityType.CREEPER) {
                        event.getClickedBlock().getWorld().spawnEntity(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), EntityType.CREEPER);
                        if (event.getItem().getAmount() > 1) {
                            event.getItem().setAmount(event.getItem().getAmount() - 1);
                        } else {
                            event.getPlayer().getInventory().removeItem(event.getItem());
                        }
                    }
                }).bindWith(terminableConsumer);

        Events.subscribe(PortalCreateEvent.class).handler(event -> event.setCancelled(true)).bindWith(terminableConsumer);

        Events.subscribe(InventoryClickEvent.class).filter(event -> event.getInventory().getType() == InventoryType.ANVIL && event.getSlotType() != InventoryType.SlotType.OUTSIDE)
                .filter(event -> event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.MOB_SPAWNER).handler(event -> event.setCancelled(true)).bindWith(terminableConsumer);

        Events.subscribe(PlayerDeathEvent.class).handler(event -> Schedulers.sync().runLater(() -> event.getEntity().spigot().respawn(), 1)).bindWith(terminableConsumer);

    }
}

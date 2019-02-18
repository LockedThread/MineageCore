package rip.simpleness.mineagecore.modules;

import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;

public class ModuleSOTW implements TerminableModule {

    private boolean sotw;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        sotw = MineageCore.getInstance().getConfig().getBoolean("sotw");

        Events.subscribe(BlockExplodeEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> sotw)
                .handler(event -> event.setCancelled(true));

        Events.subscribe(EntityExplodeEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> sotw)
                .handler(event -> event.setCancelled(true));

        Events.subscribe(EntitySpawnEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> sotw)
                .filter(event -> event.getEntityType() == EntityType.GHAST || event.getEntityType() == EntityType.CREEPER || event.getEntityType() == EntityType.PRIMED_TNT)
                .handler(event -> event.setCancelled(true));

        Events.subscribe(BlockDispenseEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> sotw)
                .filter(event -> event.getItem() != null && (event.getItem().getType() == Material.MONSTER_EGG || event.getItem().getType() == Material.MONSTER_EGGS))
                .handler(event -> event.setCancelled(true));

        Commands.create()
                .assertPermission("mineage.sotw")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 1) {
                        String root = commandContext.arg(0).parseOrFail(String.class);
                        if (root.equalsIgnoreCase("on")) {
                            commandContext.reply("&cYou have turned SOTW " + root);
                            Bukkit.broadcastMessage(" ");
                            Bukkit.broadcastMessage(" ");
                            Bukkit.broadcastMessage("&cTNT, Creeper Eggs, and Explosions are now &a&lENABLED");
                            Bukkit.broadcastMessage(" ");
                            Bukkit.broadcastMessage(" ");
                            this.sotw = true;
                        } else if (root.equalsIgnoreCase("off")) {
                            commandContext.reply("&cYou have turned SOTW " + root);
                            Bukkit.broadcastMessage(" ");
                            Bukkit.broadcastMessage(" ");
                            Bukkit.broadcastMessage("&cTNT, Creeper Eggs, and Explosions are now &c&lDISABLED");
                            Bukkit.broadcastMessage(" ");
                            Bukkit.broadcastMessage(" ");
                            this.sotw = false;
                        } else {
                            commandContext.reply("&e/sotw [on/off]");
                        }
                    }
                    commandContext.reply("&e/sotw [on/off]");
                }).registerAndBind(terminableConsumer, "sotw");
    }
}

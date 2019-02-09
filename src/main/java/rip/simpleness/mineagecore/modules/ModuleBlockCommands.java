package rip.simpleness.mineagecore.modules;

import com.google.common.collect.ImmutableSet;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;

public class ModuleBlockCommands implements TerminableModule {

    private ImmutableSet<String> blockedCommands = ImmutableSet.<String>builder().add("/plugins", "/pl", "/?", "/icanhasbukkit", "/version", ":", "minecraft", "bukkit").build();

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Events.subscribe(PlayerCommandPreprocessEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> !event.getPlayer().hasPermission("mineagepvp.admin"))
                .handler(event -> {
                    String root = event.getMessage().split(" ")[0];
                    if (blockedCommands.contains(root)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&cNo no no...."));
                    }
                }).bindWith(terminableConsumer);
    }
}

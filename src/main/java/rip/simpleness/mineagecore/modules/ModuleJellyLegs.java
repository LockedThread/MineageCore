package rip.simpleness.mineagecore.modules;

import com.google.common.reflect.TypeToken;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.serialize.GsonStorageHandler;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;

public class ModuleJellyLegs implements TerminableModule {

    private GsonStorageHandler<HashSet<UUID>> jellyLegsStorage;
    private HashSet<UUID> jellyLegsUUIDs;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.jellyLegsStorage = new GsonStorageHandler<>("jellylegs", ".json", MineageCore.getInstance().getDataFolder(), new TypeToken<HashSet<UUID>>() {
        });
        this.jellyLegsUUIDs = jellyLegsStorage.load().orElse(new HashSet<>());

        terminableConsumer.bind(() -> jellyLegsStorage.save(jellyLegsUUIDs));

        Commands.create()
                .assertPlayer()
                .assertPermission("mineagepvp.jellylegs")
                .handler(command -> {
                    Player player = command.sender();
                    if (jellyLegsUUIDs.contains(player.getUniqueId())) {
                        jellyLegsUUIDs.remove(player.getUniqueId());
                        player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&eJellyLegs &cdisabled&e."));
                    } else {
                        jellyLegsUUIDs.add(player.getUniqueId());
                        player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&eJellyLegs &aenabled&e."));
                    }
                }).registerAndBind(terminableConsumer, "jellylegs", "jl", "nofall", "nf");

        Events.subscribe(EntityDamageEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getEntityType() == EntityType.PLAYER)
                .filter(event -> event.getCause() == EntityDamageEvent.DamageCause.FALL)
                .filter(event -> jellyLegsUUIDs.contains(event.getEntity().getUniqueId()))
                .handler(event -> event.setCancelled(true))
                .bindWith(terminableConsumer);
    }
}

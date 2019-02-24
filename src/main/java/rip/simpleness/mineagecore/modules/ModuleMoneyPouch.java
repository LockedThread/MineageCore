package rip.simpleness.mineagecore.modules;

import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.objs.MoneyPouch;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class ModuleMoneyPouch implements TerminableModule {

    private static final MineageCore INSTANCE = MineageCore.getInstance();
    private HashSet<MoneyPouch> moneyPouches;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.moneyPouches = new HashSet<>();
        for (int i = 1; i < 6; i++) {
            moneyPouches.add(new MoneyPouch(i, INSTANCE.getConfig().getDouble("moneypouches." + i + ".max"), INSTANCE.getConfig().getDouble("moneypouches." + i + ".min")));
        }

        Events.subscribe(PlayerInteractEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(event -> event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                .filter(event -> event.getItem() != null)
                .handler(event -> {
                    for (MoneyPouch moneyPouch : moneyPouches) {
                        if (moneyPouch.getCustomItem().isCustomItem(event.getItem())) {
                            moneyPouch.open(event.getPlayer());
                            event.setCancelled(true);
                            return;
                        }
                    }
                }).bindWith(terminableConsumer);
    }
}

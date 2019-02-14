package rip.simpleness.mineagecore.modules;

import me.lucko.helper.Events;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.TimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ModuleGoldenApples implements TerminableModule {

    private CooldownMap<UUID> goldenAppleCooldownMap = CooldownMap.create(Cooldown.of(30, TimeUnit.SECONDS));
    private CooldownMap<UUID> godAppleCooldownMap = CooldownMap.create(Cooldown.of(120, TimeUnit.SECONDS));

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Events.subscribe(PlayerItemConsumeEvent.class)
                .filter(event -> event.getItem().getType() == Material.GOLDEN_APPLE)
                .handler(event -> {
                    Player player = event.getPlayer();
                    if (event.getItem().getDurability() == 0) {
                        final boolean b = goldenAppleCooldownMap.testSilently(player.getUniqueId());
                        System.out.println(b);
                        System.out.println("checked for not" + b);
                        if (!player.hasPermission("mineage.goldenapplebypass"))
                            if (!goldenAppleCooldownMap.testSilently(player.getUniqueId())) {
                                event.setCancelled(true);
                                player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&cYou cannot eat another golden apple for " + TimeUtil.toLongForm(goldenAppleCooldownMap.remainingTime(player.getUniqueId(), TimeUnit.SECONDS))));
                            } else {
                                goldenAppleCooldownMap.put(player.getUniqueId(), goldenAppleCooldownMap.getBase());
                            }
                    } else if (!player.hasPermission("mineage.godapplebypass"))
                        if (!godAppleCooldownMap.testSilently(player.getUniqueId())) {
                            event.setCancelled(true);
                            player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&cYou cannot eat another god apple for " + TimeUtil.toLongForm(godAppleCooldownMap.remainingTime(event.getPlayer().getUniqueId(), TimeUnit.SECONDS))));
                        } else {
                            godAppleCooldownMap.put(player.getUniqueId(), godAppleCooldownMap.getBase());
                        }
                }).bindWith(terminableConsumer);
    }
}

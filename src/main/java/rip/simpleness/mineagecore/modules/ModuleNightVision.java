package rip.simpleness.mineagecore.modules;

import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;

public class ModuleNightVision implements TerminableModule {

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        MineageCore.getInstance().getLogger().info("Registered ModuleNightVision");
        Commands.create()
                .assertPlayer()
                .assertPermission("mineagepvp.nightvision")
                .handler(command -> {
                    Player player = command.sender();
                    if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&eNight Vision &cDisabled&e."));
                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
                        player.sendMessage(Text.colorize(MineageCore.SERVER_PREFIX + "&eNight Vision &aEnabled&e."));
                    }
                }).registerAndBind(terminableConsumer, "nightvision", "nv");
    }
}

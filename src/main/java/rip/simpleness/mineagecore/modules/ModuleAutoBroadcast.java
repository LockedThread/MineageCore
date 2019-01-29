package rip.simpleness.mineagecore.modules;

import me.lucko.helper.Schedulers;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ModuleAutoBroadcast implements TerminableModule {

    private ArrayList<String[]> broadcasts = new ArrayList<>();

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        for (String key : MineageCore.getInstance().getConfig().getConfigurationSection("broadcasts").getKeys(false)) {
            broadcasts.add(MineageCore.getInstance().getConfig().getStringList(key).toArray(new String[0]));
        }

        Schedulers.async().runRepeating(() -> {
            for (String s : RandomSelector.uniform(broadcasts).pick()) {
                Bukkit.broadcastMessage(Text.colorize(s));
            }
        }, 0L, 12000L);
    }
}

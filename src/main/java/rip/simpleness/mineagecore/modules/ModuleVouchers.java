package rip.simpleness.mineagecore.modules;

import com.google.common.base.Joiner;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.objs.Voucher;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ModuleVouchers implements TerminableModule {

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        for (String key : MineageCore.getInstance().getConfig().getConfigurationSection("vouchers").getKeys(false)) {
            Voucher.getVouchers().put(key, new Voucher(key));
        }

        Commands.create()
                .assertPermission("mineage.admin")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        commandContext.reply("&e&lMineage&6&lPVP &e&lVouchers", "&e/vouchers give [player] [id] [amount]", "&e/vouchers list");
                    } else if (commandContext.args().size() == 1) {
                        commandContext.reply("&eVouchers: &f" + Joiner.on(", ").skipNulls().join(Voucher.getVouchers().keySet()));
                    } else if (commandContext.args().size() >= 3) {
                        if (commandContext.rawArg(0).equalsIgnoreCase("give")) {
                            int amount = commandContext.arg(3).parse(int.class).orElse(1);
                            Optional<Player> playerOptional = commandContext.arg(1).parse(Player.class);
                            if (!playerOptional.isPresent()) {
                                commandContext.reply(MineageCore.SERVER_PREFIX + "&c" + commandContext.rawArg(1) + " cannot be parsed as a Player.");
                            } else {
                                final String id = commandContext.rawArg(2);
                                final Voucher voucher = Voucher.getVouchers().get(id);
                                Player player = playerOptional.get();
                                for (int i = 0; i < amount; i++) {
                                    player.getInventory().addItem(voucher.getItemStack());
                                }
                                commandContext.reply(MineageCore.SERVER_PREFIX + "&eYou have given &f " + player.getName() + " " + amount + " " + id + "(s)");
                            }
                        }
                    } else {
                        commandContext.reply(MineageCore.SERVER_PREFIX + "&cError executing command /" + commandContext.label());
                    }
                }).registerAndBind(terminableConsumer, "vouchers");

        Events.subscribe(PlayerInteractEvent.class)
                .filter(event -> event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                .filter(PlayerInteractEvent::hasItem)
                .handler(event -> {
                    Voucher voucher = Voucher.getVoucher(event.getItem());
                    if (voucher != null) {
                        voucher.execute(event.getPlayer());
                    }
                });
    }

}

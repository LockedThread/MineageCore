package rip.simpleness.mineagecore.modules;

import com.google.common.base.Joiner;
import me.lucko.helper.Commands;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import org.bukkit.entity.Player;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.customitems.CustomItem;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ModuleCustomItem implements TerminableModule {

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        Commands.create()
                .assertPermission("mineage.admin")
                .handler(commandContext -> {
                    if (commandContext.args().size() == 0) {
                        commandContext.reply("", MineageCore.SERVER_PREFIX + "&e/customitem give [player] [id] [amount]", MineageCore.SERVER_PREFIX + "&e/customitem list", "");
                    } else if (commandContext.args().size() == 1) {
                        if (commandContext.rawArg(0).equalsIgnoreCase("list")) {
                            commandContext.reply(MineageCore.SERVER_PREFIX + "&eCustomItems: " + Joiner.on(", ").skipNulls().join(CustomItem.getCustomItemHashMap().keySet()));
                        }
                    } else if (commandContext.args().size() >= 3) {
                        if (commandContext.rawArg(0).equalsIgnoreCase("give")) {
                            int amount = commandContext.arg(3).parse(int.class).orElse(1);
                            Optional<Player> playerOptional = commandContext.arg(1).parse(Player.class);
                            if (!playerOptional.isPresent()) {
                                commandContext.reply(MineageCore.SERVER_PREFIX + "&c" + commandContext.rawArg(1) + " cannot be parsed as a Player.");
                            } else {
                                final String id = commandContext.rawArg(2);
                                final CustomItem customItem = CustomItem.getCustomItemHashMap().get(id);
                                if (customItem == null) {
                                    commandContext.reply(MineageCore.SERVER_PREFIX + "&cNo custom item with id &e" + id + " &cfound!");
                                } else {
                                    Player player = playerOptional.get();
                                    for (int i = 0; i < amount; i++) {
                                        player.getInventory().addItem(customItem.getItemStack());
                                    }
                                    commandContext.reply(MineageCore.SERVER_PREFIX + "&eYou have given &f " + player.getName() + " " + amount + " " + id + "(s)");
                                }
                            }
                        }
                    } else {
                        commandContext.reply(MineageCore.SERVER_PREFIX + "&cError executing command /" + commandContext.label());
                    }
                }).registerAndBind(terminableConsumer, "customitems", "customitem");
    }
}

package rip.simpleness.mineagecore.objs;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.utils.ItemUtils;
import rip.simpleness.mineagecore.utils.NMSUtils;

import java.util.HashMap;
import java.util.List;

public final class Voucher {

    private static HashMap<String, Voucher> vouchers = new HashMap<>();

    private String id;
    private List<String> commands;
    private ItemStack itemStack;

    public Voucher(String id) {
        this.id = id;

        ConfigurationSection section = MineageCore.getInstance().getConfig().getConfigurationSection("vouchers." + id);
        this.commands = section.getStringList("commands");
        this.itemStack = NMSUtils.attachItemTag(ItemStackBuilder.of(Material.matchMaterial(section.getString("item.material")))
                .name(section.getString("item.name"))
                .lore(section.getStringList("item.lore"))
                .build(), "vouchers", id);
        vouchers.put(id, this);
    }

    public static Voucher getVoucher(ItemStack itemStack) {
        final String voucherID = NMSUtils.getItemTagString(itemStack, "vouchers", "");
        return !voucherID.isEmpty() ? vouchers.get(voucherID) : null;
    }

    public static HashMap<String, Voucher> getVouchers() {
        return vouchers;
    }

    public String getId() {
        return id;
    }

    public List<String> getCommands() {
        return commands;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void execute(Player player) {
        ItemUtils.useItemInHand(player);
        commands.stream().map(command -> command.replace("{player}", player.getName())).forEach(command -> {
            if (command.startsWith("@broadcast")) {
                Bukkit.broadcastMessage(Text.colorize(command.substring(11)));
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        });
    }
}

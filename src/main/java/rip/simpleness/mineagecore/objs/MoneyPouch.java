package rip.simpleness.mineagecore.objs;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.github.paperspigot.Title;
import rip.simpleness.mineagecore.MineageCore;
import rip.simpleness.mineagecore.customitems.CustomItem;

import java.util.concurrent.ThreadLocalRandom;

public class MoneyPouch {

    private static final ChatColor[] chatColors = {ChatColor.GREEN, ChatColor.RED, ChatColor.GOLD, ChatColor.DARK_RED, ChatColor.LIGHT_PURPLE};

    private final double maxMoney;
    private final double minMoney;
    private CustomItem customItem;

    public MoneyPouch(int level, double maxMoney, double minMoney) {
        this.maxMoney = maxMoney;
        this.minMoney = minMoney;
        customItem = new CustomItem("moneypouch-" + (level), ItemStackBuilder.of(Material.ENDER_CHEST)
                .name(chatColors[level - 1] + "Tier " + toRomanNumeral(level) + " Money Pouch &r&7(Right Click)")
                .lore("", "&dMinimum: &f" + maxMoney, "&dMaximum: &f" + minMoney, "")
                .enchant(Enchantment.DURABILITY, 1)
                .flag(ItemFlag.HIDE_ENCHANTS)
                .build());
    }

    public double getRandomMoney() {
        return ThreadLocalRandom.current().nextDouble(minMoney, maxMoney);
    }

    public void open(Player player) {
        ItemStack hand = player.getItemInHand();
        if (hand.getAmount() == 1) {
            player.setItemInHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
        }
        final double money = (int) getRandomMoney();
        player.sendTitle(Title.builder()
                .fadeOut(20)
                .fadeIn(20)
                .title(Text.colorize("&a+" + money))
                .build());

        MineageCore.getInstance().getEconomy().depositPlayer(player, money);
    }

    public String toRomanNumeral(int i) {
        return i == 1 ? "I" : i == 2 ? "II" : i == 3 ? "III" : i == 4 ? "IV" : i == 5 ? "V" : i == 6 ? "VI" : i == 7 ? "VII" : i == 8 ? "VIII" : i == 9 ? "IX" : i == 10 ? "X" : "Expand the roman numeral util!";
    }

    public CustomItem getCustomItem() {
        return customItem;
    }
}

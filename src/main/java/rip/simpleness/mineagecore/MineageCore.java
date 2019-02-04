package rip.simpleness.mineagecore;

import com.massivecraft.factions.*;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.lucko.helper.Schedulers;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import me.lucko.helper.plugin.ap.PluginDependency;
import me.lucko.helper.scoreboard.PacketScoreboardProvider;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLoadOrder;
import rip.simpleness.mineagecore.enums.FactionCollectorUpgrade;
import rip.simpleness.mineagecore.menus.MenuListener;
import rip.simpleness.mineagecore.modules.*;
import rip.simpleness.mineagecore.task.GenerationTask;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Plugin(name = "MineageCore",
        version = "1.0",
        description = "MineagePVP's core plugin",
        load = PluginLoadOrder.POSTWORLD,
        authors = "Simpleness",
        website = "www.simpleness.rip",
        depends = {@PluginDependency("Vault"),
                @PluginDependency(value = "Factions"),
                @PluginDependency(value = "WorldGuard"),
                @PluginDependency("ShopGUIPlus"),
                @PluginDependency("ProtocolLib"),
                @PluginDependency("helper")})
public final class MineageCore extends ExtendedJavaPlugin {

    public static final String SERVER_PREFIX = "&8[&e&lMineage&6&lPVP&8] ";

    private WorldGuardPlugin worldGuardPlugin;
    private Economy economy;
    private ThreadLocalRandom random = ThreadLocalRandom.current();
    private GenerationTask generationTask;
    private EnumMap<Material, Double> prices;
    private PacketScoreboardProvider packetScoreboardProvider;

    public static MineageCore getInstance() {
        return getPlugin(MineageCore.class);
    }

    @Override
    protected void enable() {
        getLogger().info("Enabled MineageCore");
        saveDefaultConfig();
        this.economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        this.worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
        this.generationTask = new GenerationTask();
        this.prices = new EnumMap<>(Material.class);

        Arrays.stream(FactionCollectorUpgrade.values()).forEach(factionCollectorUpgrade -> {
            if (factionCollectorUpgrade != FactionCollectorUpgrade.DEFAULT) {
                factionCollectorUpgrade.setPrice(getConfig().getDouble("upgrade-prices." + (factionCollectorUpgrade.getRank() - 1)));
            }
            factionCollectorUpgrade.init();
        });

        FactionCollectorUpgrade._3.getApplicables().forEach(collectionType -> {
            collectionType.init();
            if (getConfig().isSet("sell-values." + collectionType.name().toLowerCase().replace("_", "-"))) {
                collectionType.setValue(getConfig().getDouble("sell-values." + collectionType.name().toLowerCase().replace("_", "-")));
            }
        });

        Schedulers.async().runRepeating(generationTask, 10L, 10L);


        ShopGuiPlugin.getInstance()
                .getShopManager()
                .shops
                .values()
                .stream()
                .flatMap(shop -> shop.getShopItems().stream())
                .forEach(item -> {
                    if (item.getItem().getType() == Material.STRING) {
                        prices.put(Material.TRIPWIRE, item.getBuyPriceForAmount(1));
                    } else if (item.getItem().getType().name().startsWith("DIODE")) {
                        prices.put(Material.DIODE, item.getBuyPriceForAmount(1));
                        prices.put(Material.DIODE_BLOCK_OFF, item.getBuyPriceForAmount(1));
                        prices.put(Material.DIODE_BLOCK_ON, item.getBuyPriceForAmount(1));
                    } else if (item.getItem().getType() == Material.REDSTONE) {
                        prices.put(Material.REDSTONE_WIRE, item.getBuyPriceForAmount(1));
                    } else if (item.getItem().getType() == Material.REDSTONE_COMPARATOR) {
                        prices.put(Material.REDSTONE_COMPARATOR, item.getBuyPriceForAmount(1));
                        prices.put(Material.REDSTONE_COMPARATOR_OFF, item.getBuyPriceForAmount(1));
                        prices.put(Material.REDSTONE_COMPARATOR_ON, item.getBuyPriceForAmount(1));
                    } else {
                        prices.put(item.getItem().getType(), item.getBuyPriceForAmount(1));
                    }
                });

        this.packetScoreboardProvider = provideService(PacketScoreboardProvider.class, new PacketScoreboardProvider(this));

        bindModule(new ModuleSilkSpawner());
        bindModule(new ModuleJellyLegs());
        bindModule(new ModuleNightVision());
        bindModule(new ModuleGoldenApples());
        bindModule(new ModuleBlockCommands());
        bindModule(new ModuleVouchers());
        bindModule(new ModuleFactionCollector());
        bindModule(new ModuleCustomItem());
        bindModule(new ModuleGenBlock());
        bindModule(new ModulePrinterMode());
        bindModule(new ModuleScoreboard());

        getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    @Override
    protected void disable() {

    }

    public String capitalizeEveryWord(String s) {
        return !s.contains(" ") ?
                StringUtils.capitalize(s.toLowerCase()) :
                Arrays.stream(s.split(" ")).map(word -> StringUtils.capitalize(word.toLowerCase())).collect(Collectors.joining());
    }

    public boolean canBuild(Player player, Location location) {
        final Faction factionAt = Board.getInstance().getFactionAt(new FLocation(player.getLocation()));
        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        return (factionAt.getAccess(fPlayer, PermissableAction.BUILD) == Access.ALLOW || factionAt.getAccess(fPlayer, PermissableAction.BUILD) == Access.UNDEFINED)
                && getWorldGuardPlugin().canBuild(player, location);
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ThreadLocalRandom getRandom() {
        return random;
    }

    public GenerationTask getGenerationTask() {
        return generationTask;
    }

    public EnumMap<Material, Double> getPrices() {
        return prices;
    }

    public PacketScoreboardProvider getPacketScoreboardProvider() {
        return packetScoreboardProvider;
    }
}

package rip.simpleness.mineagecore.modules;

import com.google.common.collect.Lists;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.scoreboard.PacketScoreboard;
import me.lucko.helper.scoreboard.ScoreboardObjective;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import me.lucko.helper.utils.Tps;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ModuleScoreboard implements TerminableModule {

    public static HashSet<UUID> disabledScordboard;
    private static final MetadataKey<ScoreboardObjective> SCOREBOARD_OBJECTIVE_METADATA_KEY = MetadataKey.create("scoreboard", ScoreboardObjective.class);
    private static final MineageCore INSTANCE = MineageCore.getInstance();
    private PacketScoreboard scoreboard;
    private List<String> scoreboardLines;
    private String scoreboardDisplayName;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        disabledScordboard = new HashSet<>();
        this.scoreboard = INSTANCE.getPacketScoreboardProvider().getScoreboard();
        this.scoreboardLines = INSTANCE.getConfig().getStringList("scoreboard.lines").stream().map(Text::colorize).collect(Collectors.toList());
        this.scoreboardDisplayName = INSTANCE.getConfig().getString("scoreboard.display-name");

        BiConsumer<Player, ScoreboardObjective> updater = (player, scoreboardObjective) -> {
            if (disabledScordboard.contains(player.getUniqueId())) {
                scoreboardObjective.unsubscribe(player);
                return;
            }
            FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
            scoreboardObjective.setDisplayName(Text.colorize(scoreboardDisplayName));
            HashMap<String, Integer> map = new HashMap<>();
            final List<String> reversed = Lists.reverse(scoreboardLines);
            for (int i = 0; i < reversed.size(); i++) {
                String line = reversed.get(i);
                if (line.isEmpty()) {
                    map.put(StringUtils.repeat(" ", i), i);
                } else {
                    map.put(line.replace("{faction_name}", fPlayer.hasFaction() ? fPlayer.getFaction().getTag() : "&2Wilderness").replace("{balance}", new BigDecimal(INSTANCE.getEconomy().getBalance(player)).setScale(2, RoundingMode.DOWN).toString()).replace("{tps}", new DecimalFormat("#.##").format(Tps.read().avg1())), i);
                }
            }
            scoreboardObjective.applyScores(map);
        };

        Events.subscribe(PlayerChangedWorldEvent.class)
                .handler(event -> updater.accept(event.getPlayer(), Metadata.provideForPlayer(event.getPlayer()).getOrNull(SCOREBOARD_OBJECTIVE_METADATA_KEY))).bindWith(terminableConsumer);

        Events.subscribe(PlayerJoinEvent.class, EventPriority.HIGHEST)
                .handler(event -> Schedulers.sync().runLater(() -> {
                    final Player player = event.getPlayer();
                    final ScoreboardObjective scoreboardObjective = scoreboard.createPlayerObjective(player, "null", DisplaySlot.SIDEBAR);
                    Metadata.provideForPlayer(player).put(SCOREBOARD_OBJECTIVE_METADATA_KEY, scoreboardObjective);
                    updater.accept(player, scoreboardObjective);
                }, 4)).bindWith(terminableConsumer);

        Events.subscribe(PlayerQuitEvent.class)
                .handler(event -> Metadata.provideForPlayer(event.getPlayer()).remove(SCOREBOARD_OBJECTIVE_METADATA_KEY)).bindWith(terminableConsumer);

        Schedulers.async().runRepeating(() -> Players.all().forEach(player -> {
            updater.accept(player, Metadata.provideForPlayer(player).getOrNull(SCOREBOARD_OBJECTIVE_METADATA_KEY));
        }), 100L, 100L);
    }

    public HashSet<UUID> getDisabledScordboard() {
        return disabledScordboard;
    }
}

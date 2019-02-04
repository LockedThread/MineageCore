package rip.simpleness.mineagecore.modules;

import com.google.common.collect.Lists;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import me.clip.placeholderapi.PlaceholderAPI;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ModuleScoreboard implements TerminableModule {

    private static final MetadataKey<ScoreboardObjective> SCOREBOARD_OBJECTIVE_METADATA_KEY = MetadataKey.create("scoreboard", ScoreboardObjective.class);
    private static final MineageCore INSTANCE = MineageCore.getInstance();
    private PacketScoreboard scoreboard;
    private List<String> scoreboardLines;
    private String scoreboardDisplayName;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.scoreboard = INSTANCE.getPacketScoreboardProvider().getScoreboard();
        this.scoreboardLines = INSTANCE.getConfig().getStringList("scoreboard.lines").stream().map(Text::colorize).collect(Collectors.toList());
        this.scoreboardDisplayName = Text.colorize(INSTANCE.getConfig().getString("scoreboard.display-name"));

        BiConsumer<Player, ScoreboardObjective> updater = (player, scoreboardObjective) -> {
            FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
            scoreboardObjective.setDisplayName(scoreboardDisplayName);
            HashMap<String, Integer> map = new HashMap<>();
            final List<String> reversed = Lists.reverse(scoreboardLines);
            for (int i = 0; i < reversed.size(); i++) {
                String line = reversed.get(i);
                if (line.isEmpty()) {
                    map.put(StringUtils.repeat(" ", i), i);
                } else {
                    map.put(PlaceholderAPI.setPlaceholders(player, line.replace("{faction_name}", fPlayer.hasFaction() ? fPlayer.getFaction().getTag() : "&2Wilderness").replace("{tps}", new DecimalFormat("#.##").format(Tps.read().avg1()))), i);
                }
            }
            scoreboardObjective.applyScores(map);
        };

        Events.subscribe(PlayerJoinEvent.class)
                .handler(event -> {
                    Player player = event.getPlayer();
                    ScoreboardObjective scoreboardObjective = scoreboard.createPlayerObjective(player, "null", DisplaySlot.SIDEBAR);
                    Metadata.provideForPlayer(player).put(SCOREBOARD_OBJECTIVE_METADATA_KEY, scoreboardObjective);
                    updater.accept(player, scoreboardObjective);
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerQuitEvent.class)
                .handler(event -> Metadata.provideForPlayer(event.getPlayer()).remove(SCOREBOARD_OBJECTIVE_METADATA_KEY)).bindWith(terminableConsumer);

        Schedulers.async().runRepeating(() -> Players.all().forEach(player -> {
            updater.accept(player, Metadata.provideForPlayer(player).getOrNull(SCOREBOARD_OBJECTIVE_METADATA_KEY));
        }), 200L, 200L);
    }
}

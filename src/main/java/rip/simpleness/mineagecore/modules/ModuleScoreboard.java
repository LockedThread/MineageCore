package rip.simpleness.mineagecore.modules;

import com.massivecraft.factions.entity.MPlayer;
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
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
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
            final MPlayer mPlayer = MPlayer.get(player);
            scoreboardObjective.setDisplayName(scoreboardDisplayName);
            scoreboardObjective.applyLines(PlaceholderAPI.setPlaceholders(player, scoreboardLines.stream()
                    .map(s -> s.replace("{factions_name}", mPlayer.getFaction().isNone() ? "&2Wilderness" : mPlayer.getFaction().getName()))
                    .collect(Collectors.toList())));
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

package rip.simpleness.mineagecore.modules;

import com.google.common.collect.ImmutableList;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.config.objectmapping.Setting;
import me.lucko.helper.config.objectmapping.serialize.ConfigSerializable;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.scoreboard.PacketScoreboard;
import me.lucko.helper.scoreboard.ScoreboardObjective;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import rip.simpleness.mineagecore.MineageCore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;

public class ModuleScoreboard implements TerminableModule {

    private static final MetadataKey<ScoreboardObjective> SCOREBOARD_OBJECTIVE_METADATA_KEY = MetadataKey.create("scoreboard", ScoreboardObjective.class);
    private static final MineageCore INSTANCE = MineageCore.getInstance();
    private PacketScoreboard scoreboard;
    private ScoreboardConfig scoreboardConfig;

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        this.scoreboard = INSTANCE.getPacketScoreboardProvider().getScoreboard();
        this.scoreboardConfig = INSTANCE.setupConfig("scoreboard.yml", new ScoreboardConfig());

        BiConsumer<Player, ScoreboardObjective> updater = (player, scoreboardObjective) -> {
            scoreboardObjective.setDisplayName(scoreboardConfig.displayName);
            scoreboardObjective.applyLines(PlaceholderAPI.setPlaceholders(player, scoreboardConfig.lines));
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
            ScoreboardObjective scoreboardObjective = Metadata.provideForPlayer(player).getOrNull(SCOREBOARD_OBJECTIVE_METADATA_KEY);
            if (scoreboardObjective != null) {
                updater.accept(player, scoreboardObjective);
            }
        }), 20L, 20L);
    }

    @ConfigSerializable
    private class ScoreboardConfig {

        @Setting(value = "display-name")
        private String displayName = "&e&lMineage&6&lPVP";

        @Setting(value = "lines")
        private List<String> lines = ImmutableList.of(
                "Line 1",
                "Line 2"
        );
    }
}

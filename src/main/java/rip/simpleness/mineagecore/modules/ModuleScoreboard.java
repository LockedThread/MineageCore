package rip.simpleness.mineagecore.modules;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.Services;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.scoreboard.Scoreboard;
import me.lucko.helper.scoreboard.ScoreboardObjective;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.BiConsumer;

public class ModuleScoreboard implements TerminableModule {

    @Override
    public void setup(@Nonnull TerminableConsumer terminableConsumer) {
        final String[] strings = new String[]{"", "&eName: &6%player_name%", "", "&eBalance: &6%vault_eco_balance_formatted%", "", "&eRank: &6%vault_rank%"};

        MetadataKey<ScoreboardObjective> scoreboardObjectiveMetadataKey = MetadataKey.create("scoreboard", ScoreboardObjective.class);

        BiConsumer<Player, ScoreboardObjective> updater = (player, scoreboardObjective) -> {
            scoreboardObjective.setDisplayName("&e&lMineage&6&lPVP");
            scoreboardObjective.applyLines(PlaceholderAPI.setPlaceholders(player, Arrays.asList(strings)));
        };

        Scoreboard scoreboard = Services.load(Scoreboard.class);

        Events.subscribe(PlayerJoinEvent.class)
                .handler(event -> {
                    Player player = event.getPlayer();
                    ScoreboardObjective scoreboardObjective = scoreboard.createPlayerObjective(player, "null", DisplaySlot.SIDEBAR);
                    Metadata.provideForPlayer(player).put(scoreboardObjectiveMetadataKey, scoreboardObjective);
                    updater.accept(player, scoreboardObjective);
                }).bindWith(terminableConsumer);

        Events.subscribe(PlayerQuitEvent.class)
                .handler(event -> Metadata.provideForPlayer(event.getPlayer()).remove(scoreboardObjectiveMetadataKey)).bindWith(terminableConsumer);

        Schedulers.async().runRepeating(() -> Players.all().forEach(player -> {
            ScoreboardObjective scoreboardObjective = Metadata.provideForPlayer(player).getOrNull(scoreboardObjectiveMetadataKey);
            if (scoreboardObjective != null) {
                updater.accept(player, scoreboardObjective);
            }
        }), 3L, 3L);
    }
}

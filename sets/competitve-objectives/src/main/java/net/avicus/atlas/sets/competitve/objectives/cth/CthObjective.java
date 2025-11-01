package net.avicus.atlas.sets.competitve.objectives.cth;

import com.google.common.collect.ArrayListMultimap;
import lombok.Getter;
import lombok.Setter;
import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.module.groups.Competitor;
import net.avicus.atlas.core.module.groups.GroupsModule;
import net.avicus.atlas.core.module.locales.LocalizedXmlString;
import net.avicus.atlas.core.module.objectives.Objective;
import net.avicus.atlas.core.module.objectives.ObjectivesModule;
import net.avicus.atlas.core.module.shop.PlayerEarnPointEvent;
import net.avicus.atlas.core.util.Events;
import net.avicus.atlas.core.util.region.Region;
import net.avicus.atlas.sets.competitve.objectives.cth.event.CthAwardPointsEvent;
import org.bukkit.entity.Player;
import org.joda.time.Duration;

import java.util.Optional;
import java.util.Random;

public class CthObjective implements Objective {
    /**
     * Match this objective exists in.
     **/
    @Getter
    private final Match match;

    /**
     * Name of the hill.
     **/
    @Setter
    private LocalizedXmlString name;

    /**
     * Region the competitors must be standing in to capture this hill.
     **/
    @Getter
    private Region capture;

    /**
     * Number of points to award per player standing on this hill.
     */
    @Getter
    private final Integer score;

    /**
     * How often this hill awards points to players standing on it.
     **/
    @Getter
    private Duration scoreInterval;

    @Getter
    private boolean lightning;

    @Getter
    private boolean broadcast;

    /**
     * Competitors standing on the hill.
     **/
    private ArrayListMultimap<Competitor, Player> capturing;

    public CthObjective(Match match,
                        LocalizedXmlString name,
                        Region capture,
                        Optional<Integer> score,
                        Optional<Duration> scoreInterval,
                        Optional<Boolean> lightning,
                        Optional<Boolean> broadcast) {
        this.match = match;
        this.name = name;
        this.capture = capture;
        this.score = score.orElse(10);
        this.scoreInterval = scoreInterval.orElse(new Duration(1000 * 60));
        this.capturing = ArrayListMultimap.create();
        this.lightning = lightning.orElse(true);
        this.broadcast = broadcast.orElse(true);
    }

    /**
     * Get a map of competitor -> player standing on the hill at this time.
     */
    public ArrayListMultimap<Competitor, Player> getCapturingPlayers() {
        return this.capturing;
    }

    /**
     * Reward any players currently standing on the hill
     */
    public void reward() {
        if(this.lightning) {
            this.match.getWorld().strikeLightningEffect(
                    this.capture.getRandomPosition(new Random()).toLocation(match.getWorld()));
        }

        ObjectivesModule objectivesModule = this.match.getRequiredModule(ObjectivesModule.class);
        for (var key : this.capturing.keySet()) {
            for (int i = 0; i < this.capturing.get(key).size(); i++) {
                objectivesModule.score(key, this.score);
            }
        }

        var players = this.capturing.values().stream().toList();
        CthAwardPointsEvent awardPointsEvent = new CthAwardPointsEvent(this, players);
        Events.call(awardPointsEvent);
        players.forEach(p -> Events.call(new PlayerEarnPointEvent(p, "cth-hill-capture")));
    }

    /**
     * Check if the player is capturing the hill.
     */
    public boolean isCapturing(Player player) {
        return this.capturing.values().contains(player);
    }

    /**
     * Add a player to the list of currently capturing players.
     */
    public void add(Player player) {
        if (!isCapturing(player)) {
            Competitor competitor = this.match.getRequiredModule(GroupsModule.class)
                    .getCompetitorOf(player).orElse(null);
            if (competitor != null) {
                this.capturing.put(competitor, player);
            }
        }
    }

    /**
     * Remove a player from the list of currently capturing players.
     */
    public void remove(Player player) {
        this.capturing.values().remove(player);
    }

    @Override
    public void initialize() {

    }

    @Override
    public LocalizedXmlString getName() {
        return this.name;
    }

    @Override
    public void setName(LocalizedXmlString name) {
        this.name = name;
    }

    @Override
    public boolean canComplete(Competitor competitor) {
        return !competitor.getGroup().isSpectator();
    }

    @Override
    public boolean isCompleted(Competitor competitor) {
        return false;
    }

    @Override
    public double getCompletion(Competitor competitor) {
        return 0;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }
}

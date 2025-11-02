package net.avicus.atlas.sets.competitve.objectives.cth;

import com.google.common.collect.ArrayListMultimap;
import lombok.Getter;
import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.module.groups.Competitor;
import net.avicus.atlas.core.module.groups.GroupsModule;
import net.avicus.atlas.core.module.locales.LocalizedXmlString;
import net.avicus.atlas.core.module.objectives.Objective;
import net.avicus.atlas.core.util.region.Region;
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
    private LocalizedXmlString name;

    /**
     * Region the competitors must be standing in to capture this hill.
     **/
    @Getter
    private final Region capture;

    /**
     * Number of points to award per player standing on this hill.
     */
    @Getter
    private final Integer score;

    /**
     * How often this hill awards points to players standing on it.
     **/
    @Getter
    private final Optional<Duration> scoreInterval;

    @Getter
    private final boolean lightning;

    @Getter
    private final boolean fireworks;

    @Getter
    private final boolean shouldBroadcast;

    @Getter
    private final Optional<String> countdownId;

    private final Random random;

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
                        Optional<Boolean> fireworks,
                        Optional<Boolean> shouldBroadcast,
                        Optional<String> countdownId) {
        this.match = match;
        this.name = name;
        this.capture = capture;
        this.score = score.orElse(10);
        this.scoreInterval = countdownId.isPresent()
                ? Optional.empty()
                : Optional.of(scoreInterval.orElse(new Duration(1000 * 60)));
        this.lightning = lightning.orElse(true);
        this.fireworks = fireworks.orElse(true);
        this.shouldBroadcast = shouldBroadcast.orElse(true);
        this.countdownId = countdownId;

        this.capturing = ArrayListMultimap.create();
        this.random = new Random();
    }

    /**
     * Get a map of competitor -> player standing on the hill at this time.
     */
    public ArrayListMultimap<Competitor, Player> getCapturingPlayers() {
        return this.capturing;
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
        // no-op
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

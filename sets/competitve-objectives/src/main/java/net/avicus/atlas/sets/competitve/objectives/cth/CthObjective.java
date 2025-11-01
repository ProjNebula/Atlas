package net.avicus.atlas.sets.competitve.objectives.cth;

import com.google.common.collect.ArrayListMultimap;
import lombok.Getter;
import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.module.groups.Competitor;
import net.avicus.atlas.core.module.groups.GroupsModule;
import net.avicus.atlas.core.module.locales.LocalizedXmlString;
import net.avicus.atlas.core.module.objectives.Objective;
import net.avicus.atlas.core.module.objectives.ObjectivesModule;
import net.avicus.atlas.core.module.shop.PlayerEarnPointEvent;
import net.avicus.atlas.core.util.Events;
import net.avicus.atlas.core.util.Messages;
import net.avicus.atlas.core.util.Players;
import net.avicus.atlas.core.util.region.Region;
import net.avicus.atlas.sets.competitve.objectives.cth.event.CthAwardPointsEvent;
import net.avicus.compendium.TextStyle;
import net.avicus.compendium.locale.text.LocalizedFormat;
import net.avicus.compendium.locale.text.UnlocalizedText;
import net.avicus.compendium.utils.Strings;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
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
    private final Duration scoreInterval;

    @Getter
    private final boolean lightning;

    @Getter
    private final boolean fireworks;

    @Getter
    private final boolean shouldBroadcast;

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
                        Optional<Boolean> shouldBroadcast) {
        this.match = match;
        this.name = name;
        this.capture = capture;
        this.score = score.orElse(10);
        this.scoreInterval = scoreInterval.orElse(new Duration(1000 * 60));
        this.lightning = lightning.orElse(true);
        this.fireworks = fireworks.orElse(true);
        this.shouldBroadcast = shouldBroadcast.orElse(true);

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
     * Reward any players currently standing on the hill
     */
    public void reward() {
        if(this.lightning) {
            this.match.getWorld().strikeLightningEffect(
                    this.capture.getRandomPosition(random).toLocation(match.getWorld()));
        }

        ObjectivesModule objectivesModule = this.match.getRequiredModule(ObjectivesModule.class);
        for (var key : this.capturing.keySet()) {
            var teamScore = this.score * this.capturing.get(key).size();
            objectivesModule.score(key, teamScore);

            this.match.broadcast(Messages.CTH_POINTS.with(ChatColor.GRAY, key.getColoredName(),
                    new UnlocalizedText(String.valueOf(teamScore), ChatColor.GREEN)));

            if(this.fireworks) {
                spawnFirework(key);
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

    /**
     * Spawn firework above the hill region with the color of the provided competitor.
     *
     * @return the spawned firework.
     */
    private Firework spawnFirework(Competitor competitor) {
        Location location = this.capture.getRandomPosition(random).toLocation(this.match.getWorld()).add(0, 2, 0);
        Firework firework = (Firework) this.match.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);

        FireworkEffect.Builder builder = FireworkEffect.builder();
        builder.with(FireworkEffect.Type.BURST);
        builder.withColor(competitor.getFireworkColor());
        builder.withTrail();

        meta.addEffect(builder.build());
        firework.setFireworkMeta(meta);

        firework.setVelocity(firework.getVelocity().multiply(0.7));

        // 1.8-1.9 Support
        Players.playFireworkSound();

        return firework;
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

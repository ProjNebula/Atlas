package net.avicus.atlas.sets.competitve.objectives.cth;

import lombok.Getter;
import net.avicus.atlas.core.countdown.MatchCountdown;
import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.module.groups.Competitor;
import net.avicus.atlas.core.module.objectives.ObjectivesModule;
import net.avicus.atlas.core.module.shop.PlayerEarnPointEvent;
import net.avicus.atlas.core.util.Events;
import net.avicus.atlas.core.util.Messages;
import net.avicus.atlas.core.util.Players;
import net.avicus.atlas.sets.competitve.objectives.cth.event.CthAwardPointsEvent;
import net.avicus.compendium.StringUtil;
import net.avicus.compendium.locale.text.Localizable;
import net.avicus.compendium.locale.text.UnlocalizedText;
import net.avicus.compendium.sound.SoundEvent;
import net.avicus.compendium.sound.SoundLocation;
import net.avicus.compendium.sound.SoundType;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.joda.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CthCountdown extends MatchCountdown {
    @Getter
    private final List<CthObjective> hills;

    private final Random random;

    public CthCountdown(Match match, Duration duration, List<CthObjective> hills)
    {
        super(match, duration);
        this.hills = hills;

        this.random = new Random();
    }

    public Duration getDuration() {
        return this.duration;
    }

    @Override
    public Localizable getName() {
        return Messages.CTH_COUNTDOWN_TITLE.with();
    }

    @Override
    protected void onTick(Duration elapsedTime, Duration remainingTime) {
        final int remainingSeconds = (int) remainingTime.getStandardSeconds();
        final Localizable message = Messages.CTH_COUNTDOWN_TIME.with(ChatColor.AQUA,
                new UnlocalizedText(StringUtil.secondsToClock((int) remainingTime.getStandardSeconds()),
                        this.determineTimeColor(elapsedTime)));

        this.updateBossBar(message, elapsedTime);

        if (this.shouldBroadcast(remainingSeconds)) {
            this.match.broadcast(message);
            this.match.getPlayers().forEach((player) -> {
                Events.call(new SoundEvent(player, SoundType.PIANO, SoundLocation.MATCH_DING)).getSound()
                        .play(player, 1F);
            });
        }
    }

    @Override
    protected void onEnd() {
        this.clearBossBars();
        this.match.importantBroadcast(Messages.CTH_COUNTDOWN_AWARDED.with(ChatColor.GREEN));
        this.reward();
    }

    /**
     * Reward any players currently standing on the hill
     */
    private void reward() {
        Map<Competitor, Integer> scoresMap = new HashMap<>();

        for (var hill : hills) {
            var capturing = hill.getCapturingPlayers();

            if (hill.isLightning()) {
                this.match.getWorld().strikeLightningEffect(
                        hill.getCapture().getRandomPosition(random).toLocation(match.getWorld()));
            }

            ObjectivesModule objectivesModule = this.match.getRequiredModule(ObjectivesModule.class);
            for (var key : capturing.keySet()) {
                var teamScore = hill.getScore() * capturing.get(key).size();
                objectivesModule.score(key, teamScore);

                var currentScore = scoresMap.getOrDefault(key, 0);
                currentScore += teamScore;
                scoresMap.put(key, currentScore);

                if(hill.isFireworks()) {
                    spawnFirework(hill.getCapture().getRandomPosition(random).toLocation(this.match.getWorld()).add(0, 2, 0), key);
                }
            }

            var players = hill.getCapturingPlayers().values().stream().toList();
            CthAwardPointsEvent awardPointsEvent = new CthAwardPointsEvent(hill, players);
            Events.call(awardPointsEvent);
            players.forEach(p -> Events.call(new PlayerEarnPointEvent(p, "cth-hill-capture")));
        }

        for (var entry : scoresMap.entrySet()) {
            this.match.broadcast(Messages.CTH_POINTS.with(ChatColor.GRAY, entry.getKey().getColoredName(),
                    new UnlocalizedText(String.valueOf(entry.getValue()), ChatColor.GREEN)));
        }
    }

    /**
     * Spawn firework above the hill region with the color of the provided competitor.
     *
     * @return the spawned firework.
     */
    private Firework spawnFirework(Location location, Competitor competitor) {
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
    protected void onCancel() {
        this.clearBossBars();
    }
}

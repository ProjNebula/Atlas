package net.avicus.atlas.sets.competitve.objectives.cth;

import lombok.Getter;
import net.avicus.atlas.core.countdown.MatchCountdown;
import net.avicus.atlas.core.util.Events;
import net.avicus.atlas.core.util.Messages;
import net.avicus.compendium.StringUtil;
import net.avicus.compendium.locale.text.Localizable;
import net.avicus.compendium.locale.text.UnlocalizedText;
import net.avicus.compendium.sound.SoundEvent;
import net.avicus.compendium.sound.SoundLocation;
import net.avicus.compendium.sound.SoundType;
import org.bukkit.ChatColor;
import org.joda.time.Duration;

public class CthCountdown extends MatchCountdown {
    @Getter
    private final CthObjective hill;

    public CthCountdown(CthObjective hill)
    {
        super(hill.getMatch(), hill.getScoreInterval());
        this.hill = hill;
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

        this.hill.reward();
        this.match.importantBroadcast(Messages.CTH_COUNTDOWN_AWARDED.with(ChatColor.GREEN));
    }

    @Override
    protected void onCancel() {
        this.clearBossBars();
    }
}

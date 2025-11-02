package net.avicus.atlas.sets.competitve.objectives.cth;

import net.avicus.atlas.core.Atlas;
import net.avicus.atlas.core.event.group.PlayerChangedGroupEvent;
import net.avicus.atlas.core.event.match.MatchStateChangeEvent;
import net.avicus.atlas.core.module.groups.GroupsModule;
import net.avicus.atlas.core.module.objectives.ObjectivesModule;
import net.avicus.compendium.countdown.CountdownEndEvent;
import net.avicus.compendium.countdown.CountdownManager;
import net.avicus.compendium.plugin.CompendiumPlugin;
import net.avicus.grave.event.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.joda.time.Duration;
import tc.oc.tracker.event.PlayerCoarseMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CthListener implements Listener {
    private final ObjectivesModule module;
    private final List<CthObjective> hills;
    private final List<CthCountdown> countdowns;
    private final CountdownManager countdownManager;

    public CthListener(ObjectivesModule module, List<CthObjective> hills) {
        this.module = module;
        this.hills = hills;
        this.countdownManager = CompendiumPlugin.getInstance().getCountdownManager();
        this.countdowns = new ArrayList<>();

        Atlas.get().getLogger().log(Level.INFO, "Hills in constructor: " + hills.size());

        for (var hill : hills.stream().filter(h -> h.getCountdownId().isEmpty()).toList()) {
            countdowns.add(
                    new CthCountdown(
                        module.getMatch(),
                        hill.getScoreInterval().orElse(Duration.standardSeconds(60)),
                        List.of(hill)));
        }

        this.initializeRegisteredCountdowns();
    }

    @EventHandler
    public void onPlayerCoarseMove(PlayerCoarseMoveEvent event) {
        if (this.module.getMatch().getRequiredModule(GroupsModule.class)
                .isObservingOrDead(event.getPlayer())) {
            return;
        }

        Player player = event.getPlayer();

        for (var hill : this.hills) {
            boolean inside = hill.getCapture().contains(event.getTo().getBlock());

            if (inside) {
                hill.add(player);
            } else {
                hill.remove(player);
            }
        }
    }

    @EventHandler
    public void onCountdownEnd(CountdownEndEvent event) {
        if (event.getEnded() instanceof CthCountdown countdown) {
            this.countdowns.remove(countdown);
            var newCountdown = new CthCountdown(module.getMatch(), countdown.getDuration(), countdown.getHills());

            this.countdowns.add(newCountdown);
            this.countdownManager.start(newCountdown);
        }
    }

    @EventHandler
    public void onStateChange(MatchStateChangeEvent event) {
        for (var countdown : this.countdowns) {
            this.countdownManager.cancel(countdown);

            if (event.isToPlaying()) {
                this.countdownManager.start(countdown);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (var hill : this.hills) {
            hill.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onChangeTeam(PlayerChangedGroupEvent event) {
        if (event.getGroupFrom().isEmpty()) {
            return;
        }

        for (var hill : this.hills) {
            hill.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        for (var hill : this.hills) {
            hill.remove(event.getPlayer());
        }
    }

    private void initializeRegisteredCountdowns() {
        for (var hill : this.hills.stream().filter(h -> h.getCountdownId().isPresent()).toList()) {
            var countdownOpt = this.module.getMatch().getRegistry().get(
                    CthCountdown.class,
                    hill.getCountdownId().orElseThrow(),
                    true);

            var countdown = countdownOpt.orElseThrow();

            countdown.getHills().add(hill);

            if (!this.countdowns.contains(countdown)) {
                this.countdowns.add(countdown);
            }
        }
    }
}

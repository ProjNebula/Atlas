package net.avicus.atlas.sets.competitve.objectives.cth;

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
import tc.oc.tracker.event.PlayerCoarseMoveEvent;

import java.util.ArrayList;
import java.util.List;

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

        for (var hill : hills) {
            countdowns.add(new CthCountdown(hill));
        }
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
            var newCountdown = new CthCountdown(countdown.getHill());

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
}

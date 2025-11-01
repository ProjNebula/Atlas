package net.avicus.atlas.sets.competitve.objectives.cth;

import net.avicus.atlas.core.event.group.PlayerChangedGroupEvent;
import net.avicus.atlas.core.event.match.MatchStateChangeEvent;
import net.avicus.atlas.core.module.groups.GroupsModule;
import net.avicus.atlas.core.module.objectives.ObjectivesModule;
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
    private final List<CthTask> tasks;

    public CthListener(ObjectivesModule module, List<CthObjective> hills) {
        this.module = module;
        this.hills = hills;
        this.tasks = new ArrayList<>();

        for (var hill : hills) {
            tasks.add(new CthTask(module, hill));
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
    public void onStateChange(MatchStateChangeEvent event) {
        for (var task : this.tasks) {
            task.cancel0();

            if (event.isToPlaying()) {
                task.start();
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
        if (!event.getGroupFrom().isPresent()) {
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

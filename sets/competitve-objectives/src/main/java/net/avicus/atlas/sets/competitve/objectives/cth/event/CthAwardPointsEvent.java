package net.avicus.atlas.sets.competitve.objectives.cth.event;

import lombok.Getter;
import net.avicus.atlas.core.event.objective.ObjectiveCompleteEvent;
import net.avicus.atlas.core.module.objectives.Objective;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;

public class CthAwardPointsEvent extends ObjectiveCompleteEvent {

    private static final HandlerList handlers = new HandlerList();

    public CthAwardPointsEvent(Objective objective, List<Player> players) {
        super(objective, players);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}

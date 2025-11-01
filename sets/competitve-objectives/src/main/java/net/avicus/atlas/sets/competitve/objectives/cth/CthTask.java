package net.avicus.atlas.sets.competitve.objectives.cth;

import net.avicus.atlas.core.Atlas;
import net.avicus.atlas.core.module.objectives.ObjectivesModule;
import net.avicus.atlas.core.util.AtlasTask;

import java.util.logging.Level;

public class CthTask extends AtlasTask {
    private final ObjectivesModule manager;
    private final CthObjective hill;

    public CthTask(ObjectivesModule manager, CthObjective hill) {
        super();
        this.manager = manager;
        this.hill = hill;
    }

    public void start() {
        repeat(0, (int)(this.hill.getScoreInterval().getStandardSeconds()*20));
    }

    @Override
    public void run() {
        Atlas.get().getLogger().log(Level.INFO, "Rewarding points");
        hill.reward();
    }
}

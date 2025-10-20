package net.avicus.atlas.sets.walls;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Setter;
import net.avicus.atlas.core.Atlas;
import net.avicus.atlas.core.GameType;
import net.avicus.atlas.core.component.ComponentManager;
import net.avicus.atlas.core.component.visual.SidebarComponent;
import net.avicus.atlas.core.external.ModuleSet;
import net.avicus.atlas.core.map.AtlasMap;
import net.avicus.atlas.core.map.AtlasMapFactory;
import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.match.MatchFactory;
import net.avicus.atlas.core.module.executors.ExecutionDispatch;
import net.avicus.atlas.core.module.groups.GroupsModule;
import net.avicus.atlas.core.module.groups.ffa.FFAModule;
import net.avicus.atlas.core.module.groups.teams.TeamsModule;
import net.avicus.atlas.core.module.shop.PointEarnConfig;
import net.avicus.atlas.core.util.MapGenre;
import net.avicus.atlas.sets.walls.ability.Parser;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class Main extends ModuleSet {

    @Setter
    private Atlas atlas;
    @Setter
    private MatchFactory matchFactory;
    @Setter
    private Logger logger;

    @Override
    public void onEnable() {
        registerExecutionListeners();
        this.matchFactory.register(WallFactory.class);

        new Parser().loadParsers();

        this.logger.info("Enabled walls set.");
        AtlasMapFactory.TYPE_DETECTORS.add(new AtlasMap.TypeDetector() {
            @Override
            public Optional<MapGenre> detectGenre(Match match) {
                return match.hasModule(WallsModule.class) ? Optional.of(MapGenre.WALLS) : Optional.empty();
            }

            @Override
            public Set<GameType> detectGameTypes(Match match) {
                Set<GameType> types = Sets.newHashSet();

                if (match.hasModule(WallsModule.class)) {
                    types.add(GameType.WALLS);
                }

                return types;
            }
        });

        PointEarnConfig.CONFIGURABLES.add("wall-fall");

        GroupsModule.BRIDGES.putIfAbsent(TeamsModule.class, Lists.newArrayList());
        GroupsModule.BRIDGES.get(TeamsModule.class).add(GroupsBridge.class);
        GroupsModule.BRIDGES.putIfAbsent(FFAModule.class, Lists.newArrayList());
        GroupsModule.BRIDGES.get(FFAModule.class).add(GroupsBridge.class);
    }

    @Override
    public void onDisable() {
        this.logger.info("Disabled walls set.");
    }

    @Override
    public void onComponentsEnable(ComponentManager componentManager) {
        if (componentManager.hasModule(SidebarComponent.class)) {
            SidebarComponent.HOOKS.add(new SBHook());
        }
    }

    private void registerExecutionListeners() {
        ExecutionDispatch.registerListener("wall-fall", WallsFallEvent.class, (e) -> {
            WallsFallEvent event = (WallsFallEvent) e;
            ExecutionDispatch
                    .whenDispatcherExists(dispatcher -> dispatcher.handleEvent(event, null, null));
        });
    }
}

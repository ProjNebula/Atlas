package net.avicus.atlas.sets.competitve.objectives.cth;

import net.avicus.atlas.core.documentation.ModuleDocumentation;
import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.match.MatchFactory;
import net.avicus.atlas.core.match.registry.RegisteredObject;
import net.avicus.atlas.core.module.Module;
import net.avicus.atlas.core.module.ModuleBuildException;
import net.avicus.atlas.core.module.ModuleFactory;
import net.avicus.atlas.core.util.xml.XmlElement;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CthCountdownFactory implements ModuleFactory<Module> {
    @Override
    public Optional<Module> build(Match match, MatchFactory factory, XmlElement root) throws ModuleBuildException {
        List<XmlElement> elements = root.getChildren("cth-countdown");

        if (elements.isEmpty()) {
            return Optional.empty();
        }

        elements.forEach(element -> {
            String id = element.getAttribute("id").asRequiredString();
            Duration pointInterval = element.getAttribute("point-award-interval").asRequiredDuration();
            CthCountdown countdown = new CthCountdown(match, pointInterval, new ArrayList<>());

            match.getRegistry().add(new RegisteredObject<>(id, countdown));
        });

        return Optional.empty();
    }

    @Override
    public ModuleDocumentation getDocumentation() {
        return null;
    }
}

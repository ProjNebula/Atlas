package net.avicus.atlas.sets.competitve.objectives.cth;

import net.avicus.atlas.core.documentation.FeatureDocumentation;
import net.avicus.atlas.core.documentation.attributes.Attributes;
import net.avicus.atlas.core.documentation.attributes.GenericAttribute;
import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.match.MatchFactory;
import net.avicus.atlas.core.module.FactoryUtils;
import net.avicus.atlas.core.module.locales.LocalesModule;
import net.avicus.atlas.core.module.locales.LocalizedXmlString;
import net.avicus.atlas.core.module.objectives.ObjectiveFactory;
import net.avicus.atlas.core.util.region.Region;
import net.avicus.atlas.core.util.xml.XmlElement;
import org.joda.time.Duration;

import java.util.Optional;

public class CthFactory implements ObjectiveFactory<CthObjective> {
    @Override
    public CthObjective build(Match match, MatchFactory factory, XmlElement element) {
        // Grabs attributes from parent <cth-hills/>
        element.inheritAttributes("cth-hills");

        // name
        String rawName = element.getAttribute("name").asRequiredString();
        LocalizedXmlString name = match.getRequiredModule(LocalesModule.class).parse(rawName);

        // countdown-id
        Optional<String> countdownId = element.getAttribute("countdown-id").asString();

        // capture
        Region capture = FactoryUtils
                .resolveRequiredRegionAs(match, Region.class, element.getAttribute("capture"),
                        element.getChild("capture"));

        // points
        Optional<Integer> points = element.getAttribute("points").asInteger();

        // scoreInterval
        Optional<Duration> scoreInterval = element.getAttribute("point-award-interval").asDuration();

        // lightning
        Optional<Boolean> lightning = element.getAttribute("lightning").asBoolean();

        // fireworks
        Optional<Boolean> fireworks = element.getAttribute("fireworks").asBoolean();

        Optional<Boolean> broadcast = element.getAttribute("broadcast").asBoolean();

        return new CthObjective(
                match,
                name,
                capture,
                points,
                scoreInterval,
                lightning,
                fireworks,
                broadcast,
                countdownId);
    }

    @Override
    public FeatureDocumentation getDocumentation() {
        return FeatureDocumentation.builder()
                .name("CTH Hill")
                .tagName("cth-hills").tagName("cth-hill")
                .description("Hills are areas that teams fight over for control.")
                .attribute("id", Attributes.id(false))
                .attribute("name", new GenericAttribute(LocalizedXmlString.class, true,
                        "The name of the objective (for the UI)."))
                .attribute("capture",
                        Attributes.region(true, "The region that contains the area that must be captured."))
                .build();
    }
}

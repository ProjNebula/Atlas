package net.avicus.atlas.sets.generator;

import net.avicus.atlas.core.Atlas;
import net.avicus.atlas.core.documentation.FeatureDocumentation;
import net.avicus.atlas.core.documentation.InfoTable;
import net.avicus.atlas.core.documentation.ModuleDocumentation;
import net.avicus.atlas.core.documentation.SpecInformation;
import net.avicus.atlas.core.documentation.attributes.Attribute;
import net.avicus.atlas.core.module.ModuleFactory;
import net.avicus.compendium.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MarkdownUtils {

  public static String createTable(String[] header, LinkedList<String[]> rows) {
    StringBuilder table = new StringBuilder();

    // Header
    table.append("|");
    for (String h : header) {
      table.append(" ").append(h).append(" |");
    }
    table.append("\n");

    // Separator
    table.append("|");
    for (int i = 0; i < header.length; i++) {
      table.append(" --- |");
    }
    table.append("\n");

    // Rows
    rows.forEach(r -> {
      table.append("|");
      for (String s : r) {
        table.append(" ").append(s).append(" |");
      }
      table.append("\n");
    });

    return table.toString();
  }

  public static String generateModule(ModuleDocumentation documentation,
                                      HashMap<FeatureDocumentation, Element> examples) {
    StringBuilder md = new StringBuilder();

    // Title
    md.append("# ").append(documentation.getName());

    if (documentation.getTagNames() != null && !documentation.getTagNames().isEmpty()) {
      md.append(" ");
      documentation.getTagNames().forEach(t -> {
        md.append("`<").append(t).append(">`");
      });
    }
    md.append("\n\n");

    // Description
    if (documentation.getDescription() != null && !documentation.getDescription().isEmpty()) {
      documentation.getDescription().forEach(d -> md.append(d).append("\n\n"));
    }

    // Requirements
    documentation.getRequirements().forEach(r -> {
      ModuleFactory f = Atlas.get().getMatchFactory().getFactory(r);
      md.append("**REQUIREMENT:** This module requires the [")
          .append(f.getDocumentation().getName())
          .append(" Component](")
          .append(f.getDocumentation().getLink())
          .append(").\n\n");
    });

    // Spec Information
    if (documentation.getSpecInformation() != null) {
      md.append(specInfoTable(documentation.getSpecInformation()));
    }

    // Tables
    if (documentation.getTables() != null) {
      documentation.getTables().forEach(t -> md.append(toMarkdown(t)));
    }

    // Features
    documentation.getFeatures().forEach(f -> md.append(generateSection(f, false, examples)));

    return md.toString();
  }

  public static String toMarkdown(InfoTable table) {
    StringBuilder md = new StringBuilder();
    md.append("##### ").append(table.getTitle()).append("\n\n");
    md.append(createTable(table.getHeader(), table.getRows()));
    md.append("\n");
    return md.toString();
  }

  public static String generateSection(FeatureDocumentation documentation, boolean sub,
                                       HashMap<FeatureDocumentation, Element> examples) {
    StringBuilder md = new StringBuilder();

    // Section Title
    if (sub) {
      md.append("### ");
    } else {
      md.append("## ");
    }
    md.append(documentation.getName());

    if (documentation.getTagNames() != null && !documentation.getTagNames().isEmpty()) {
      md.append(" ");
      documentation.getTagNames().forEach(t -> {
        md.append("`<").append(t).append(">`");
      });
    }
    md.append("\n\n");

    // Requirements
    if (documentation.getRequirements() != null) {
      documentation.getRequirements().forEach(r -> {
        ModuleFactory f = Atlas.get().getMatchFactory().getFactory(r);
        md.append("**REQUIREMENT:** This feature requires the [")
            .append(f.getDocumentation().getName())
            .append(" Component](")
            .append(f.getDocumentation().getLink())
            .append(").\n\n");
      });
    }

    // Description
    documentation.getDescription().forEach(d -> md.append(d).append("\n\n"));

    // Spec Information
    if (documentation.getSpecInformation() != null) {
      md.append(specInfoTable(documentation.getSpecInformation()));
    }

    // Example
    Element example = examples.get(documentation);

    if (example != null) {
      XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
      md.append("```xml\n");
      md.append(StringUtil.join(example.getContent(), "\n", new StringUtil.Stringify<Content>() {
        @Override
        public String on(Content object) {
          return outputter.outputString(Arrays.asList(object));
        }
      }));
      md.append("\n```\n\n");
    } else {
      Bukkit.getLogger().warning(documentation.getName() + " has no example.");
    }

    // Element Text
    if (documentation.getText() != null) {
      md.append("#### Element Text\n\n");
      md.append(textTable(documentation.getText()));
    }

    // Attributes
    if (documentation.getAttributes() != null && !documentation.getAttributes().isEmpty()) {
      md.append(attributesTable(documentation));
    }

    // Tables
    if (documentation.getTables() != null && !documentation.getTables().isEmpty()) {
      documentation.getTables().forEach(t -> md.append(toMarkdown(t)));
    }

    // Sub Features
    if (documentation.getSubFeatures() != null) {
      documentation.getSubFeatures()
          .forEach(f -> md.append(generateSection(f, true, examples)));
    }

    return md.toString();
  }

  public static String attributesTable(FeatureDocumentation documentation) {
    StringBuilder md = new StringBuilder();
    md.append("##### ").append(documentation.getName()).append(" Attributes\n\n");

    boolean hasDef = false;
    for (Pair<Attribute, Object> pair : documentation.getAttributes().values()) {
      if (pair.getValue() != null) {
        hasDef = true;
        break;
      }
    }

    // Header
    md.append("| Attribute | Description | Type |");
    if (hasDef) {
      md.append(" Default |");
    }
    md.append("\n");

    // Separator
    md.append("| --- | --- | --- |");
    if (hasDef) {
      md.append(" --- |");
    }
    md.append("\n");

    // Rows
    final boolean useDef = hasDef;
    documentation.getAttributes().entrySet().forEach(e -> md.append(tableRow(e, useDef)));

    md.append("\n");
    return md.toString();
  }

  public static String textTable(Pair<Attribute, Object> text) {
    StringBuilder md = new StringBuilder();

    // Header
    md.append("| Description | Type |");
    if (text.getValue() != null) {
      md.append(" Default |");
    }
    md.append("\n");

    // Separator
    md.append("| --- | --- |");
    if (text.getValue() != null) {
      md.append(" --- |");
    }
    md.append("\n");

    // Row
    md.append("| ");
    for (String d : text.getKey().getDescription()) {
      md.append(d).append(" ");
    }
    md.append("| `").append(text.getKey().getName()).append("`");

    if (!text.getKey().getLink().isEmpty()) {
      md.append("<br />[Possible Values](").append(text.getKey().getLink()).append(")");
    }

    if (text.getKey().getValues().length > 0) {
      md.append("<br />");
      for (int i = 0; i < text.getKey().getValues().length; i++) {
        String v = text.getKey().getValues()[i];
        md.append("`").append(v).append("`");
        if (i < text.getKey().getValues().length - 1) {
          md.append(", ");
        }
      }
    }
    md.append(" |");

    if (text.getValue() != null) {
      md.append(" `").append(text.getValue().toString()).append("` |");
    }
    md.append("\n\n");

    return md.toString();
  }

  public static String specInfoTable(SpecInformation information) {
    StringBuilder md = new StringBuilder();
    md.append("| Specification | Changes |\n");
    md.append("| --- | --- |\n");

    if (information.getAdded() != null) {
      md.append("| ").append(information.getAdded().toString()).append(" | ADDED |\n");
    }

    if (information.getBreakingChanges() != null && !information.getBreakingChanges().isEmpty()) {
      information.getBreakingChanges().forEach((v, c) ->
          c.forEach(aC -> md.append("| ").append(v.toString()).append(" | ").append(aC).append(" |\n"))
      );
    }

    if (information.getChanges() != null && !information.getChanges().isEmpty()) {
      information.getChanges().forEach((v, c) ->
          c.forEach(aC -> md.append("| ").append(v.toString()).append(" | ").append(aC).append(" |\n"))
      );
    }

    if (information.getDeprecated() != null) {
      md.append("| ").append(information.getDeprecated().toString()).append(" | DEPRECATED |\n");
    }

    if (information.getRemoved() != null) {
      md.append("| ").append(information.getRemoved().toString()).append(" | REMOVED |\n");
    }

    md.append("\n");
    return md.toString();
  }

  public static String tableRow(Map.Entry<String, Pair<Attribute, Object>> row, boolean showDef) {
    StringBuilder md = new StringBuilder();

    Attribute attribute = row.getValue().getKey();
    Object def = row.getValue().getValue();

    md.append("| `").append(row.getKey()).append("` | ");

    for (String d : attribute.getDescription()) {
      md.append(d).append(" ");
    }

    md.append("| `").append(attribute.getName()).append("`");

    if (!attribute.getLink().isEmpty()) {
      md.append("<br />[Possible Values](").append(attribute.getLink()).append(")");
    }

    if (attribute.getValues().length > 0) {
      md.append("<br />");
      for (int i = 0; i < attribute.getValues().length; i++) {
        String v = attribute.getValues()[i];
        md.append("`").append(v).append("`");
        if (i < attribute.getValues().length - 1) {
          md.append(", ");
        }
      }
    }
    md.append(" |");

    if (showDef) {
      if (def != null) {
        md.append(" `").append(def.toString()).append("` |");
      } else {
        md.append(" |");
      }
    }
    md.append("\n");

    return md.toString();
  }
}

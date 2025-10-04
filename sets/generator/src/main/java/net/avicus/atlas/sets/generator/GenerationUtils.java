package net.avicus.atlas.sets.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.avicus.atlas.core.Atlas;
import net.avicus.atlas.core.SpecificationVersionHistory;
import net.avicus.atlas.core.documentation.FeatureDocumentation;
import net.avicus.atlas.core.documentation.ModuleDocumentation;
import net.avicus.atlas.core.documentation.SpecInformation;
import net.avicus.atlas.core.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.yaml.snakeyaml.Yaml;

public class GenerationUtils {

  public static void generateConfig(File where) throws IOException {
    HashMap<String, Object> data = Maps.newHashMap();

    data.put("spec", SpecificationVersionHistory.CURRENT.toString());

    HashMap<ModuleDocumentation.ModuleCategory, List<ModuleDocumentation>> byCat = Maps
        .newHashMap();
    Atlas.get().getMatchFactory().getDocumentation().forEach(d -> {
      if (d.getCategory() == null) {
        Bukkit.getConsoleSender()
            .sendMessage(ChatColor.RED + d.getName() + " does not have a module category!");
        return;
      }

      byCat.putIfAbsent(d.getCategory(), Lists.newArrayList());
      byCat.get(d.getCategory()).add(d);
    });
    List<Object> content = Lists.newArrayList();
    byCat.entrySet().stream()
        .sorted(
            new Comparator<Map.Entry<ModuleDocumentation.ModuleCategory, List<ModuleDocumentation>>>() {
              @Override
              public int compare(
                  Map.Entry<ModuleDocumentation.ModuleCategory, List<ModuleDocumentation>> o1,
                  Map.Entry<ModuleDocumentation.ModuleCategory, List<ModuleDocumentation>> o2) {
                return o1.getKey().ordinal() - o2.getKey().ordinal();
              }
            })
        .forEach((c) -> {
          content.add(c.getKey().getHuman());
          c.getValue().stream().sorted(new Comparator<ModuleDocumentation>() {
            @Override
            public int compare(ModuleDocumentation o1, ModuleDocumentation o2) {
              return o1.getName().compareTo(o2.getName());
            }
          }).forEach((d) -> {
            HashMap<String, String> module = Maps.newHashMap();
            module.put("name", d.getName());
            module.put("path", d.getLink());
            content.add(module);
          });
        });
    data.put("content", content);
    Yaml yaml = new Yaml();
    FileWriter out = new FileWriter(where);

    yaml.dump(data, out);
  }

  public static void writeFile(File modules, ModuleDocumentation documentation,
      CommandSender sender, HashMap<FeatureDocumentation, Element> ex) throws IOException {
    File md = new File(modules, documentation.getSafeName() + ".md");
    sender.sendMessage(ChatColor.GREEN + "Writing markdown file at " + md.getPath());
    md.createNewFile();

    FileWriter writer = new FileWriter(md);
    writer.append("---\n" +
        "layout: module\n" +
        "title: " + documentation.getName() + "\n" +
        "permalink: " + documentation.getLink() + "\n" +
        "---");
    writer.append("\n\n");
    writer.append(MarkdownUtils.generateModule(documentation, ex));
    writer.close();
    sender.sendMessage(ChatColor.GREEN + "Wrote markdown file at " + md.getPath());
  }

  public static void populateExamples(File examples, List<ModuleDocumentation> documentations)
      throws IOException {
    examples.createNewFile();
    Element root = new Element("examples");

    documentations.forEach(d -> {
      Element documentation = new Element(d.getSafeName());
      d.getFeatures().forEach(f -> {
        GenerationUtils.createExamplePaths(f, documentation);
      });
      root.addContent(documentation);
    });

    Document document = new Document(root);
    XMLOutputter xmlOutput = new XMLOutputter();

    // display nice nice
    xmlOutput.setFormat(Format.getPrettyFormat());
    xmlOutput.output(document, new FileWriter(examples));
  }

  public static void writeHistory(File root, List<SpecInformation> info) throws IOException {
    File histFile = new File(root, "spec-history.md");
    histFile.createNewFile();

    FileWriter writer = new FileWriter(histFile);
    writer.append("---\n" +
        "layout: module\n" +
        "title: Version History" + "\n" +
        "permalink: " + "/spec-history" + "\n" +
        "---");
    writer.append("\n\n");

    HashMap<Version, List<String>> added = Maps.newHashMap();
    HashMap<Version, List<String>> removed = Maps.newHashMap();
    HashMap<Version, List<String>> deprecated = Maps.newHashMap();
    HashMap<Version, List<String>> changes = Maps.newHashMap();
    HashMap<Version, List<String>> breaking = Maps.newHashMap();

    info.forEach(i -> {
      if (i.getAdded() != null) {
        added.putIfAbsent(i.getAdded(), Lists.newArrayList());
        added.get(i.getAdded()).add(i.getName());
      }
      if (i.getDeprecated() != null) {
        deprecated.putIfAbsent(i.getDeprecated(), Lists.newArrayList());
        deprecated.get(i.getDeprecated()).add(i.getName());
      }
      if (i.getRemoved() != null) {
        removed.putIfAbsent(i.getRemoved(), Lists.newArrayList());
        removed.get(i.getRemoved()).add(i.getName());
      }
      if (i.getChanges() != null) {
        i.getChanges().forEach((v, c) -> {
          changes.putIfAbsent(v, Lists.newArrayList());
          c.forEach(aC -> changes.get(v).add(aC));
        });
      }
      if (i.getBreakingChanges() != null) {
        i.getBreakingChanges().forEach((v, c) -> {
          breaking.putIfAbsent(v, Lists.newArrayList());
          c.forEach(aC -> breaking.get(v).add(aC));
        });
      }
    });

    LinkedHashMap<Version, List<String>> changeLog = Maps.newLinkedHashMap();
    added.forEach((v, c) -> {
      changeLog.putIfAbsent(v, Lists.newArrayList());
      c.stream().sorted(String::compareTo).forEach(aC -> {
        changeLog.get(v).add("**ADDED:** " + aC);
      });
    });
    deprecated.forEach((v, c) -> {
      changeLog.putIfAbsent(v, Lists.newArrayList());
      c.stream().sorted(String::compareTo).forEach(aC -> {
        changeLog.get(v).add("**DEPRECATED:** " + aC);
      });
    });
    removed.forEach((v, c) -> {
      changeLog.putIfAbsent(v, Lists.newArrayList());
      c.stream().sorted(String::compareTo).forEach(aC -> {
        changeLog.get(v).add("**REMOVED:** " + aC);
      });
    });
    breaking.forEach((v, c) -> {
      changeLog.putIfAbsent(v, Lists.newArrayList());
      c.stream().sorted(String::compareTo).forEach(aC -> {
        changeLog.get(v).add("**BREAKING:** " + aC);
      });
    });
    changes.forEach((v, c) -> {
      changeLog.putIfAbsent(v, Lists.newArrayList());
      c.stream().sorted(String::compareTo).forEach(aC -> {
        changeLog.get(v).add("**CHANGE:** " + aC);
      });
    });

    LinkedHashMap<Version, List<String>> sortedNodes = changeLog.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .collect(Collectors
            .toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

    StringBuilder page = new StringBuilder();
    page.append("# Specification Version History\n\n");
    page.append("_Track all the changes that have happened with each specification update._\n\n");

    sortedNodes.forEach((v, n) -> {
      page.append("### ").append(v.toString());
      if (v == SpecificationVersionHistory.CURRENT) {
        page.append(" `LATEST`");
      }
      page.append("\n\n");
      n.forEach(item -> page.append("- ").append(item).append("\n"));
      page.append("\n");
    });

    writer.append(page.toString());
    writer.close();
  }

  public static void createExamplePaths(FeatureDocumentation documentation, Element parent) {
    Element main = new Element(documentation.getSafeName());

    Element example = new Element("example");
    example.addContent(new Comment("TODO: Add example for " + documentation.getName()));
    main.addContent(example);

    documentation.getSubFeatures().forEach(f -> createExamplePaths(f, main));

    parent.addContent(main);
  }

  public static HashMap<FeatureDocumentation, Element> mapExamples(
      FeatureDocumentation documentation, Element parent) {
    HashMap<FeatureDocumentation, Element> res = Maps.newHashMap();

    if (parent == null) {
      throw new RuntimeException("Could not find parent element for " + documentation.getName());
    }

    Element example = parent.getChild("example");
    if (example == null) {
      Bukkit.getLogger().warning(documentation.getName() + " does not have an example!");
    } else {
      res.put(documentation, example);
    }

    if (documentation.getSubFeatures() != null) {
      documentation.getSubFeatures().forEach(f -> {
        Element child = parent.getChild(f.getSafeName());
        if (child == null) {
          throw new RuntimeException("Could not find child element for " + f.getSafeName());
        }
        res.putAll(mapExamples(f, child));
      });
    }

    return res;
  }
}

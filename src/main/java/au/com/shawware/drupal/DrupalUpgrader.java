/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Facilitates the upgrade of a Drupal website.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class DrupalUpgrader
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("usage: DrupalUpgrader <raw data>");
            System.exit(1);
        }

        DrupalUpgrader program = new DrupalUpgrader();

        System.exit(program.run(args[0]));
    }

    private DrupalUpgrader()
    {

    }

    private int run(String filename)
    {
        RawDataLoader loader = new RawDataLoader();

        Map<String, RawTable> rawData = loader.loadData(Paths.get(filename));

        System.err.format("Tables: %d%n", rawData.size());

        DataExtractor extractor = new DataExtractor("uhm_", rawData);

        Map<String, Tag> tags = extractor.getTags();

        System.err.format("Tags: %d%n", tags.size());

        Map<String, Node> nodes = extractor.getNodes(tags);

        System.err.format("Nodes: %d%n", nodes.size());

        Map<String, ContentType> types = extractor.getContentTypes();

        int numFields = types.values().stream().mapToInt(type -> type.getFields().size()).sum();

        System.err.format("Content Types: %d%n", types.size());
        System.err.format("Fields: %d%n", numFields);

        Map<String, Alias> aliases = extractor.getAliases();
        System.err.format("Aliases: %d%n", aliases.size());

        Map<String, List<FieldValue>> values = extractor.getFieldValues(types, nodes);

        int numValues = values.values().stream().mapToInt(List::size).sum();
        System.err.format("Values: %d%n", numValues);

        Map<String, File> files = extractor.getFiles();

        System.err.format("Files: %d%n", files.size());

        Map<String, String> imageMap = extractor.getImageMap(files, nodes);
        System.err.format("Images: %d%n", imageMap.size());
        
        DataRenumberer renumberer = new DataRenumberer();
        
        renumberer.renumberFiles(files, imageMap);
        renumberer.renumberAliases(aliases);

        DataGenerator generator = new DataGenerator("sw_uhm", "d9c_");

        Map<String, Tag> tagsSubset = new LinkedHashMap<>();
        tagsSubset.put("1", tags.get("1"));
        tagsSubset.put("9", tags.get("9"));
        tagsSubset.put("10", tags.get("10"));
        tagsSubset.put("56", tags.get("56"));

        Map<String, Node> nodesSubset = new LinkedHashMap<>();
        nodesSubset.put("97", nodes.get("97"));
        nodesSubset.put("98", nodes.get("98"));

        nodesSubset = nodes.values().stream()
                .filter(node -> "image".equals(node.getType()))
                .filter(node -> Integer.valueOf(node.getId()) > 13)
                .collect(toMap(Node::getId, Function.identity()));

        Map<String, Alias> aliasesSubset = aliases.values().stream()
//                .filter(alias -> alias.getAlias().contains("football"))
                .collect(toMap(Alias::getId, Function.identity()));

        generator.generate(types, nodesSubset, tags, imageMap, files, aliasesSubset, values);

        SqlGenerator sql = new SqlGenerator();

        sql.generate(generator.getData(), System.out);

        return 0;
    }
}

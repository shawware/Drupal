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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.groupingBy;

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
       
        aliases.values()
            .stream()
            .collect(groupingBy(Alias::getPath))
            .entrySet()
            .stream()
            .filter((e) -> e.getValue().size() > 1)
            .map(Entry::getValue)
            .collect(toList())
            .forEach(System.err::println);

        DataRenumberer renumberer = new DataRenumberer();
        
        renumberer.renumberContent(3, nodes, values, files, imageMap, aliases);

        DataGenerator generator = new DataGenerator("sw_uhm", "d9c_");

        generator.generate(types, nodes, tags, imageMap, files, aliases, values);

        SqlGenerator sql = new SqlGenerator();

        sql.generate(generator.getData(), System.out);

        return 0;
    }
}

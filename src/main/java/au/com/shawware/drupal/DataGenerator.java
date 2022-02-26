/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Generates the raw D9 data from our model.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class DataGenerator extends TableWorker
{
    private static final String NULL = "NULL";
    private static final String LANG_CODE = "en";

    private final String database;
    private final Map<String, Table<? extends Entity>> data;
    private final Map<String, String> htmlFormats;

    public DataGenerator(String database, String prefix)
    {
        super(prefix);

        this.database = database;
        this.data = new LinkedHashMap<>(); // The order of addition is important here.
        this.htmlFormats = initHtmlFormats();
    }

    // TODO: map to more appropriate type, eg. restricted_html.
    private Map<String, String> initHtmlFormats()
    {
        Map<String, String> formats = new HashMap<>();
        
        formats.put("1", "basic_html");
        formats.put("3", "full_html");
        
        return formats;
    }

    /**
     * @return The generated data, ready to be converted into SQL (or whatever).
     */
    public Map<String, Table<? extends Entity>> getData()
    {
        return data;
    }

    /**
     * Generates the data from the given inputs.
     * 
     * @param types the content types to generate data for
     * @param nodes the nodes to generate data for
     * @param imageMap the map of (image) node ID to file ID
     * @param files the files to generate data for  
     * @param tags the tags to generate data for
     * @param aliases the aliases to generate data for
     * @param values the values to generate data for
     */
    public void generate(
            Map<String, ContentType> types,
            Map<String, Node> nodes,
            Map<String, Tag> tags,
            Map<String, String> imageMap,
            Map<String, File> files,
            Map<String, Alias> aliases,
            Map<String, List<FieldValue>> values
    ) {
        
        preparePictures(types, nodes, values, imageMap, files);

        addTag(tags);
        addTagRevision(tags);
        addTagData(tags);
        addTagDataRevision(tags);
        addTagParent(tags);

        addNode(nodes);
        addNodeRevision(nodes);
        addNodeData(nodes);
        addNodeDataRevision(nodes);
        addNodeBody(nodes);
        addNodeTags(nodes);
        addNodeFieldTags(nodes);
        addCounts(nodes);

        addAliases(aliases);
        
        addFiles(files);
        addNodeFiles(imageMap, nodes, files);

        addValues(types, values);
    }

    private void preparePictures(
            Map<String, ContentType> types,
            Map<String, Node> nodes,
            Map<String, List<FieldValue>> values,
            Map<String, String> imageMap,
            Map<String, File> files
    ) {

        Field pictureField = new Field("picture", "image");
        ContentType picture = new ContentType("picture");
        picture.addField(pictureField);
        types.put(picture.getName(), picture);
        
        List<FieldValue> pictures = new ArrayList<>();
        
        imageMap.forEach((nid, fid) -> {
            File file = files.get(fid);
            FieldValue value = new FieldValue(nid, "0", pictureField, file.getId());
            value.addExtra("title", nodes.get(nid).getTitle());
            pictures.add(value);
        });
    
        values.put("picture", pictures);
    }

    private void addTag(Map<String, Tag> tags)
    {
        List<Column<Tag>> columns = new Columns<Tag>()
                .addNumeric("tid", Tag::getId)
                .addNumeric("revision_id", Tag::getId)
                .addText("vid", (t) -> "tags")
                .addText("uuid", (t) -> generateUUID())
                .addText("langcode", (t) -> LANG_CODE)
                .build();

        createAndStoreTable("taxonomy_term_data", tags, columns);
    }

    private void addTagRevision(Map<String, Tag> tags)
    {
        List<Column<Tag>> columns = new Columns<Tag>()
                .addNumeric("tid", Tag::getId)
                .addNumeric("revision_id", Tag::getId)
                .addText("langcode", (t) -> LANG_CODE)
                .addNumeric("revision_user", (t) -> NULL)
                .addNumeric("revision_created", (t) -> "0") // Hmmm, can we get a value?
                .addNumeric("revision_log_message", (n) -> NULL)
                .addNumeric("revision_default", (n) -> "1")
                .build();

        createAndStoreTable("taxonomy_term_revision", tags, columns);
    }

    private void addTagData(Map<String, Tag> tags)
    {
        List<Column<Tag>> columns = new Columns<Tag>()
                .addNumeric("tid", Tag::getId)
                .addNumeric("revision_id", Tag::getId)
                .addText("vid", (t) -> "tags")
                .addText("langcode", (t) -> LANG_CODE)
                .addNumeric("status", (t) -> "1")
                .addText("name", Tag::getName)
                .addText("description__value", Tag::getDescription)
                .addText("description__format", (t) -> NULL)
                .addNumeric("weight", (t) -> "1")
                .addNumeric("changed", (t) -> "0") // Hmmm, can we get a value?
                .addNumeric("default_langcode", (n) -> "1")
                .addNumeric("revision_translation_affected", (n) -> "1")
                .build();

        createAndStoreTable("taxonomy_term_field_data", tags, columns);
    }

    private void addTagDataRevision(Map<String, Tag> tags)
    {
        List<Column<Tag>> columns = new Columns<Tag>()
                .addNumeric("tid", Tag::getId)
                .addNumeric("revision_id", Tag::getId)
                .addText("langcode", (t) -> LANG_CODE)
                .addNumeric("status", (t) -> "1")
                .addText("name", Tag::getName)
                .addText("description__value", Tag::getDescription)
                .addText("description__format", (t) -> NULL)
                .addNumeric("changed", (t) -> "0") // Hmmm, can we get a value?
                .addNumeric("default_langcode", (n) -> "1")
                .build();

        createAndStoreTable("taxonomy_term_field_revision", tags, columns);
    }

    private void addTagParent(Map<String, Tag> tags)
    {
        List<Column<Tag>> columns = new Columns<Tag>()
                .addText("bundle", (t) -> "tags")
                .addNumeric("deleted", (t) -> "0")
                .addNumeric("entity_id", Tag::getId)
                .addNumeric("revision_id", Tag::getId)
                .addText("langcode", (t) -> LANG_CODE)
                .addNumeric("delta", (t) -> "0")
                .addNumeric("parent_target_id", (t) -> "0")
                .build();

        createAndStoreTable("taxonomy_term__parent", tags, columns);
        createAndStoreTable("taxonomy_term_revision__parent", tags, columns);
    }

    private void addNode(Map<String, Node> nodes)
    {
        List<Column<Node>> columns = new Columns<Node>()
                .addNumeric("nid", Node::getId)
                .addNumeric("vid", Node::getId)
                .addText("type", (node) -> mapNodeType(node.getType()))
                .addText("uuid", (n) -> generateUUID())
                .addText("langcode", (n) -> LANG_CODE)
                .build();

        createAndStoreTable("node", nodes, columns);
    }

    private void addNodeRevision(Map<String, Node> nodes)
    {
        List<Column<Node>> columns = new Columns<Node>()
                .addNumeric("nid", Node::getId)
                .addNumeric("vid", Node::getId)
                .addText("langcode", (n) -> LANG_CODE)
                .addNumeric("revision_uid", Node::getUid)
                .addNumeric("revision_timestamp", Node::getCreated)
                .addNumeric("revision_log", (n) -> NULL)
                .addNumeric("revision_default", (n) -> "1")
                .build();

        createAndStoreTable("node_revision", nodes, columns);
    }

    private void addNodeData(Map<String, Node> nodes)
    {
        List<Column<Node>> columns = new Columns<Node>()
                .addNumeric("nid", Node::getId)
                .addNumeric("vid", Node::getId)
                .addText("type", (node) -> mapNodeType(node.getType()))
                .addText("langcode", (n) -> LANG_CODE)
                .addNumeric("status", (n) -> "1")
                .addNumeric("uid", Node::getUid)
                .addHTML("title", Node::getTitle)
                .addNumeric("created", Node::getCreated)
                .addNumeric("changed", Node::getChanged)
                .addNumeric("promote", (n) -> "1")
                .addNumeric("sticky", (n) -> "0")
                .addNumeric("default_langcode", (n) -> "1")
                .addNumeric("revision_translation_affected", (n) -> "1")
                .build();

        createAndStoreTable("node_field_data", nodes, columns);
    }

    private void addNodeDataRevision(Map<String, Node> nodes)
    {
        List<Column<Node>> columns = new Columns<Node>()
                .addNumeric("nid", Node::getId)
                .addNumeric("vid", Node::getId)
                .addText("langcode", (n) -> LANG_CODE)
                .addNumeric("status", (n) -> "1")
                .addNumeric("uid", Node::getUid)
                .addHTML("title", Node::getTitle)
                .addNumeric("created", Node::getCreated)
                .addNumeric("changed", Node::getChanged)
                .addNumeric("promote", (n) -> "1")
                .addNumeric("sticky", (n) -> "0")
                .addNumeric("default_langcode", (n) -> "1")
                .addNumeric("revision_translation_affected", (n) -> "1")
                .build();

        createAndStoreTable("node_field_revision", nodes, columns);
    }

    private void addNodeBody(Map<String, Node> nodes)
    {
        List<Column<Node>> columns = new Columns<Node>()
                .addText("bundle", (node) -> mapNodeType(node.getType()))
                .addNumeric("deleted", (n) -> "0")
                .addNumeric("entity_id", Node::getId)
                .addNumeric("revision_id", Node::getId)
                .addText("langcode", (n) -> LANG_CODE)
                .addNumeric("delta", (n) -> "0")
                .addHTML("body_value", Node::getBody)
                .addHTML("body_summary", (node) -> node.getSummary().orElse(NULL))
                .addText("body_format", (node) -> htmlFormats.get(node.getBodyFormat()))
                .build();

        createAndStoreTable("node__body", nodes, columns);
        createAndStoreTable("node_revision__body", nodes, columns);
    }

    private void addNodeTags(Map<String, Node> nodes)
    {
        List<Column<Entity>> columns = new Columns<>()
                .addNumeric("nid")
                .addNumeric("tid")
                .addNumeric("status")
                .addNumeric("sticky")
                .addNumeric("created")
                .build();

        createAndStoreTagAssociationTable("taxonomy_index", nodes, columns, this::populateNodeTagsRow);
    }

    private void populateNodeTagsRow(Map<String, String> row, Node node, Tag tag, Integer tagIndex)
    {
        row.put("nid", node.getId());
        row.put("tid", tag.getId());
        row.put("status", "1");
        row.put("sticky", "0");
        row.put("created", node.getCreated());
    }

    private void addNodeFieldTags(Map<String, Node> nodes)
    {
        List<Column<Entity>> columns = new Columns<>()
                .addText("bundle").addNumeric("deleted")
                .addNumeric("entity_id")
                .addNumeric("revision_id")
                .addText("langcode")
                .addNumeric("delta")
                .addNumeric("field_tags_target_id")
                .build();

        createAndStoreTagAssociationTable("node__field_tags", nodes, columns, this::populateNodeFieldTagsRow);
    }

    private void populateNodeFieldTagsRow(Map<String, String> row, Node node, Tag tag, Integer tagIndex)
    {
        row.put("bundle", node.getType());
        row.put("deleted", "0");
        row.put("entity_id", node.getId());
        row.put("revision_id", node.getId());
        row.put("langcode", LANG_CODE);
        row.put("delta", String.valueOf(tagIndex));
        row.put("field_tags_target_id", tag.getId());
    }

    private void addCounts(Map<String, Node> nodes)
    {
        List<Column<Node>> columns = new Columns<Node>()
                .addNumeric("nid", Node::getId)
                .addNumeric("totalcount", (node) -> node.getReads().orElse("0"))
                .addNumeric("daycount", (n) -> "0")
                .addNumeric("timestamp", (n) -> "0")
                .build();

        createAndStoreTable("node_counter", nodes, columns);
    }

    // D9 aliases have a leading /, which D7 aliases do not.
    private void addAliases(Map<String, Alias> aliases)
    {
        List<Column<Alias>> columns = new Columns<Alias>()
                .addNumeric("id", Alias::getId)
                .addNumeric("revision_id", Alias::getId)
                .addText("uuid", (a) -> generateUUID())
                .addText("langcode", (a) -> LANG_CODE)
                .addText("path", (alias) -> "/" + alias.getPath())
                .addText("alias", (alias) -> "/" + alias.getAlias())
                .addNumeric("status", (a) -> "1")
                .build();

        List<Column<Alias>> revisionColumns = new Columns<Alias>()
                .addNumeric("id", Alias::getId)
                .addNumeric("revision_id", Alias::getId)
                .addText("langcode", (a) -> LANG_CODE)
                .addText("path", (alias) -> "/" + alias.getPath())
                .addText("alias", (alias) -> "/" + alias.getAlias())
                .addNumeric("status", (a) -> "1")
                .addNumeric("revision_default", (a) -> "1")
                .build();

        createAndStoreTable("path_alias", aliases, columns);
        createAndStoreTable("path_alias_revision", aliases, revisionColumns);
    }

    private void addFiles(Map<String, File> files)
    {
        List<Column<File>> columns = new Columns<File>()
                .addNumeric("fid", File::getId)
                .addText("uuid", (f) -> generateUUID())
                .addText("langcode", (f) -> LANG_CODE)
                .addNumeric("uid", File::getUid)
                .addText("filename", file -> fileName(file.getPath()))
                .addText("uri", file -> uri(file.getPath()))
                .addText("filemime", File::getMimeType)
                .addNumeric("filesize", File::getSize)
                .addNumeric("status", (f) -> "1")
                .addNumeric("created", (f) -> "0") // TODO: get from node?
                .addNumeric("changed", (f) -> "0") // TODO: get from node?
                .build();

        createAndStoreTable("file_managed", files, columns);
    }

    private String fileName(String path)
    {
        String name;
        if (path.contains("/"))
        {
            name = path.substring(path.lastIndexOf('/') + 1); // Assume not at end
        }
        else
        {
            name = path;
        }
        return name;
    }

    private String uri(String path)
    {
        return "public://" + path.substring(4); // Skip the leading "uhm/"
    }

    private void addNodeFiles(Map<String, String> imageMap, Map<String, Node> nodes, Map<String, File> files)
    {
        List<Column<Entity>> columns = new Columns<>()
                .addNumeric("fid")
                .addText("module")
                .addText("type")
                .addNumeric("id")
                .addNumeric("count")
                .build();

        createAndStoreFileAssociationTable("file_usage", imageMap, nodes, files, columns, this::populateFileUsageRow);
    }
    
    private void populateFileUsageRow(Map<String, String> row, Node node, File file)
    {
        row.put("fid", file.getId());
        row.put("module", "file");
        row.put("type", "node");
        row.put("id", node.getId());
        row.put("count", "1");
    }

    private void addValues(Map<String, ContentType> types, Map<String, List<FieldValue>> values)
    {
        types.values().forEach(type ->
        {

            type.getFields().forEach(field ->
            {

                List<Column<FieldValue>> columns = new Columns<FieldValue>()
                        .addText("bundle", (v) -> type.getName())
                        .addNumeric("deleted", (v) -> "0")
                        .addNumeric("entity_id", FieldValue::getId)
                        .addNumeric("revision_id", FieldValue::getId)
                        .addText("langcode", (v) -> LANG_CODE)
                        .addNumeric("delta", FieldValue::getDelta)
                        .build();

                String fieldName = field.getName();
                String outputFieldName = fieldName.endsWith("_scor") ? fieldName + "e" : fieldName;

                String fieldType = field.getType();

                String prefix = "field_" + outputFieldName + "_";

                if (fieldType.equals("link"))
                {
                    columns.add(new Column<>(prefix + "uri", Columns.HTML, FieldValue::getValue));
                    columns.add(new Column<>(prefix + "title", Columns.HTML, value -> value.getExtra("title")));
                    columns.add(new Column<>(prefix + "options", Columns.TEXT, (v) -> "a:0:{}"));
                }
                else if (fieldType.equals("image"))
                {
                    columns.add(new Column<>(prefix + "target_id", Columns.NUMBER, FieldValue::getValue));
                    columns.add(new Column<>(prefix + "alt", Columns.HTML, value -> value.getExtra("title")));
                    columns.add(new Column<>(prefix + "title", Columns.HTML, value -> value.getExtra("title")));
                    columns.add(new Column<>(prefix + "width", Columns.NUMBER, (v) -> NULL));
                    columns.add(new Column<>(prefix + "height", Columns.NUMBER, (v) -> NULL));
                }
                else
                {
                    String suffix = "value";
                    String columnType;
                    if (fieldType.equals("number"))
                    {
                        columnType = Columns.NUMBER;
                    }
                    else if (fieldType.equals("date"))
                    {
                        columnType = Columns.TEXT;
                    }
                    else if (fieldType.equals("text"))
                    {
                        columnType = Columns.HTML;
                    }
                    else if (fieldType.equals("nodereference"))
                    {
                        suffix = "target_id";
                        columnType = Columns.NUMBER;
                    }
                    else
                    {
                        columnType = "error";
                        System.err.format("Unknown field type: %s%n", fieldType);
                    }
                    columns.add(new Column<>(prefix + suffix, columnType, FieldValue::getValue));
                }

                List<FieldValue> fieldValues = values.get(fieldName);

                Map<String, FieldValue> entities = fieldValues.stream()
                        .collect(toMap(value -> createValueKey(value), identity()));

                createAndStoreTable("node__field_" + outputFieldName, entities, columns);

            });

        });
    }

    private String createValueKey(FieldValue value)
    {
        return value.getId() + "::" + value.getDelta();
    }

    private <T extends Entity> void createAndStoreTable(String tableName, Map<String, T> entities, List<Column<T>> columns)
    {
        tableName = getTableName(tableName);

        Table<T> table = new Table<>(database, tableName, columns);

        fillTable(table, entities);

        data.put(tableName, table);
    }

    private void createAndStoreTagAssociationTable(String tableName, Map<String, Node> nodes, List<Column<Entity>> columns,
            QuadFunction<Map<String, String>, Node, Tag, Integer> rowPopulator)
    {
        tableName = getTableName(tableName);

        Table<Entity> table = new Table<>(database, tableName, columns);

        fillTagAssociationTable(table, nodes, rowPopulator);

        data.put(tableName, table);
    }

    private void createAndStoreFileAssociationTable(String tableName, Map<String, String> imageMap, Map<String, Node> nodes, Map<String, File> files, List<Column<Entity>> columns,
            TernaryFunction<Map<String, String>, Node, File> rowPopulator)
    {
        tableName = getTableName(tableName);

        Table<Entity> table = new Table<>(database, tableName, columns);

        fillFileAssociationTable(table, imageMap, nodes, files, rowPopulator);

        data.put(tableName, table);
    }

    private <T extends Entity> void fillTable(Table<T> table, Map<String, T> entities)
    {
        List<Column<T>> columns = table.getColumns();

        entities.keySet()
            .stream()
            .sorted(this::comparator)
            .forEach(id -> {
                T entity = entities.get(id);
                Map<String, String> row = createRow();
                columns.forEach(column -> {
                    row.put(column.getName(), column.getValue(entity));
                });
                table.addRow(row);
            });
    }

    private int comparator(String id1, String id2)
    {
        int result;
        if (id1.contains(":"))
        {
            String left1 = id1.substring(0, id1.indexOf(':'));
            String left2 = id2.substring(0, id2.indexOf(':'));
            result = Integer.compare(Integer.valueOf(left1), Integer.valueOf(left2));
            if (result == 0)
            {
                String right1 = id1.substring(id1.lastIndexOf(':') + 1);
                String right2 = id2.substring(id2.lastIndexOf(':') + 1);
                result = Integer.compare(Integer.valueOf(right1), Integer.valueOf(right2));
            }
        }
        else
        {
            result = Integer.compare(Integer.valueOf(id1), Integer.valueOf(id2));
        }
        return result;
    }

    private void fillTagAssociationTable(Table<Entity> table, Map<String, Node> nodes, QuadFunction<Map<String, String>, Node, Tag, Integer> rowPopulator)
    {
        nodes.values().forEach(node ->
        {
            List<Tag> tags = node.getTags();

            for (int tagIndex = 0; tagIndex < tags.size(); tagIndex++)
            {
                Tag tag = tags.get(tagIndex);

                Map<String, String> row = createRow();

                rowPopulator.apply(row, node, tag, tagIndex);

                table.addRow(row);
            }
        });
    }

    private void fillFileAssociationTable(Table<Entity> table, Map<String, String> imageMap, Map<String, Node> nodes, Map<String, File> files, TernaryFunction<Map<String, String>, Node, File> rowPopulator)
    {
        imageMap.forEach((nid, fid) ->
        {
            Map<String, String> row = createRow();

            rowPopulator.apply(row, nodes.get(nid), files.get(fid));

            table.addRow(row);
        });
    }

    private String generateUUID()
    {
        return UUID.randomUUID().toString();
    }

    private String mapNodeType(String type)
    {
        String result = type;
        if ("story".equals(type))
        {
            result = "article";
        }
        else if ("image".equals(type))
        {
            result = "picture";
        }
        return result;
    }

    private Map<String, String> createRow()
    {
        return new HashMap<>();
    }
}

@FunctionalInterface
interface QuadFunction<T1, T2, T3, T4>
{
    void apply(T1 t1, T2 t2, T3 t3, T4 t4);
}

@FunctionalInterface
interface TernaryFunction<T1, T2, T3>
{
    void apply(T1 t1, T2 t2, T3 t3);
}

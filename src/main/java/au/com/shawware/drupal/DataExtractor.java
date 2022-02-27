/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Extracts the data from the raw D7 data and converts it into our model.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class DataExtractor extends TableWorker
{
    private static final String BREAK = "<!--break-->"; // Old-school summary/body separator

    private final Map<String, RawTable> rawData;
    private final Set<String> htmlFormats;

    public DataExtractor(String prefix, Map<String, RawTable> rawData)
    {
        super(prefix);

        this.rawData = rawData;
        this.htmlFormats = initHtmlFormats();
    }

    private Set<String> initHtmlFormats()
    {
        Set<String> formats = new HashSet<>();
        
        formats.add("1");
        formats.add("3");
        
        return formats;
    }

    /**
     * Load the raw tag data.
     * 
     * @return A map of tag IDs to tags.
     */
    public Map<String, Tag> getTags()
    {
        RawTable rawTags = getTable("taxonomy_term_data");

        Map<String, Tag> tags = rawTags.getData()
                .stream()
                .map(row -> new Tag(
                        row.get("tid"),
                        row.get("name"),
                        row.get("description")
                    )
                )
                .collect(
                    toMap(Tag::getId, identity())
                );
        
        return tags;
    }
    
    /**
     * Load the raw content type data.
     * 
     * @return A map of content types to their data.
     */
    public Map<String, ContentType> getContentTypes()
    {
        RawTable rawCTs = getTable("node_type");

        Map<String, ContentType> types = rawCTs.getData()
                .stream()
                .map(row -> new ContentType(
                        row.get("type")
                    )
                )
                .collect(
                    toMap(ContentType::getName, identity())
                );
        
        addFields(types);

        return types;
    }
    
    private void addFields(Map<String, ContentType> types)
    {
        RawTable rawFields = getTable("content_node_field_instance");
        
        rawFields.getData()
            .forEach(row -> {
                String type = row.get("type_name");
                if (types.containsKey(type))
                {
                    Field field = new Field(
                        fieldName(row.get("field_name")),
                        row.get("widget_module")
                    );
                    types.get(type).addField(field);
                }
                else
                {
                    System.err.format("Unknown content type found: %s%n", type);
                }
        });
    }
    
    private String fieldName(String fieldName) {
        return fieldName.substring(6); // Remove "field_" prefix.
    }
    
    /**
     * Load the raw node data.
     * 
     * @param tags the existing tag definitions
     *
     * @return A map of node IDs to nodes.
     */
    public Map<String, Node> getNodes(Map<String, Tag> tags)
    {
        RawTable rawNodes = getTable("node");

        Map<String, Node> nodes = rawNodes.getData()
                .stream()
                .map(row -> new Node(
                        row.get("nid"),
                        row.get("type"),
                        row.get("uid"),
                        row.get("created"),
                        row.get("changed"),
                        row.get("title")
                    )
                )
                .collect(
                    toMap(Node::getId, identity())
                );
        
        addContent(nodes);
        addCounts(nodes);
        addAliases(nodes);
        addTags(nodes, tags);
        
        return nodes;
    }

    /**
     * Extracts the node summary and body and adds it to the given nodes.
     * 
     * @param nodes the existing nodes
     */
    private void addContent(Map<String, Node> nodes)
    {
        RawTable rawContent = getTable("field_data_body");
        
        rawContent.getData()
            .forEach(row -> {
                String nid = row.get("entity_id");
                if (nodes.containsKey(nid))
                {
                    Node node = nodes.get(nid);
                    // If body is contains old-school summary marker, handle it.
                    String body = row.get("body_value");
                    String summary;
                    if (body.contains(BREAK))
                    {
                        int index = body.indexOf(BREAK);
                        summary = body.substring(0, index);
                        if (summary.endsWith("\r\n"))
                        {
                            summary = summary.substring(0, summary.length() - 2);
                        }
                        body = body.substring(index + BREAK.length());
                        if (body.startsWith("\r\n"))
                        {
                            body = body.substring(2);
                        }
                    }
                    else
                    {
                        summary = row.get("summary_value");
                    }
                    node.setSummary(summary);
                    node.setBody(body);
                    if (body == null)
                    {
                        System.err.format("Node %s has null body%n", nid);
                    }
                    String format = row.get("body_format");
                    if (format == null)
                    {
                        format = "1";
                    }
                    if (!htmlFormats.contains(format))
                    {
                        System.err.format("Unknown body content type \"%s\" found for node: %s%n", format, nid);
                    }
                    node.setBodyFormat(format);
                }
                else
                {
                    System.err.format("Content found for unknown node: %s%n", nid);
                }
            });
    }

    /**
     * Extracts the node count data and adds it to the given nodes.
     * 
     * @param nodes the existing nodes
     */
    private void addCounts(Map<String, Node> nodes)
    {
        RawTable rawCounts = getTable("node_counter");

        rawCounts.getData()
            .forEach(row -> {
                String nid = row.get("nid");
                if (nodes.containsKey(nid))
                {
                    Node node = nodes.get(nid);
                    node.setReads(row.get("totalcount"));
                    node.setLastRead(row.get("timestamp"));
                }
                else
                {
                    System.err.format("Count found for unknown node: %s%n", nid);
                }
            });
    }

    /**
     * Extracts the alias data and adds it to the given nodes.
     * 
     * @param nodes the existing nodes
     */
    private void addAliases(Map<String, Node> nodes)
    {
        RawTable rawAliases = getTable("url_alias");
        
        rawAliases.getData()
            .forEach(row -> {
                String source = row.get("source");
                if (source.startsWith("node/"))
                {
                    String nid = source.substring(5); // Skip over "node/"
                    if (nodes.containsKey(nid))
                    {
                        Node node = nodes.get(nid);
                        node.setAlias(row.get("alias"));
                    }
                    else
                    {
                        System.err.format("Alias found for unknown node: %s%n", nid);
                    }
                }
            });
    }

    /**
     * Extracts the mapping between nodes and tags.
     * 
     * @param nodes the existing nodes
     * @param tags the existing tags
     */
    private void addTags(Map<String, Node> nodes, Map<String, Tag> tags)
    {
        RawTable rawTagMappings = getTable("taxonomy_index");

        rawTagMappings.getData()
            .forEach(row -> {
                String nid = row.get("nid");
                if (nodes.containsKey(nid))
                {
                    String tid = row.get("tid");
                    if (tags.containsKey(tid))
                    {
                        nodes.get(nid).addTag(tags.get(tid));
                    }
                    else
                    {
                        System.err.format("Tag mapping found for node %s with unknown tag %s%n", nid, tid);
                        System.err.format("Node: %s%n", nodes.get(nid));
                    }
                }
                else
                {
                    System.err.format("Tafg mapping found for unknown node: %s%n", nid);
                }
            });
    }

    /**
     * Extracts the field values for the given content types.
     * As a side-effect, also updates each node's values.
     * 
     * @param types the existing content types
     * @param nodes the existing nodes
     *
     * @return A map of field name to the corresponding values.
     */
    public Map<String, List<FieldValue>> getFieldValues(Map<String, ContentType> types, Map<String, Node> nodes)
    {
        Map<String, List<FieldValue>> values = new HashMap<>();

        types.values().forEach(type -> {

            type.getFields().forEach(field -> {
                
                RawTable rawValues = getTable("field_data_field_" + field.getName());
                
                List<FieldValue> fieldValues = new ArrayList<>();
                
                rawValues.getData().forEach(row -> {

                    FieldValue value = getFieldValue(field, row);
                    
                    if (nodes.containsKey(value.getId()))
                    {
                        fieldValues.add(value);
                        nodes.get(value.getId()).addValue(value);
                    }
                    else
                    {
                        System.err.format("Value found in %s for unknown node %s%n", rawValues.getTableName(), value.getId());
                    }

                });
                
                values.put(field.getName(), fieldValues);
            });
        });
        
        return values;
    }

    private FieldValue getFieldValue(Field field, Map<String, String> row)
    {
        String id = row.get("entity_id");
        String delta = row.get("delta");
        String prefix = "field_" + field.getName() + '_';
        String type = field.getType();

        String key;
        if (type.equals("link"))
        {
            key = "url";
        }
        else if (type.equals("nodereference"))
        {
            key = "nid";
        }
        else
        {
            key = "value";
        }

        FieldValue value = new FieldValue(id, delta, field, row.get(prefix + key));

        if (type.equals("link"))
        {
            String title = row.get(prefix + "title");
            value.addExtra("title", title);
        }

        return value;
    }

    public Map<String, Alias> getAliases()
    {
        RawTable rawAliases = getTable("url_alias");

        Map<String, Alias> tags = rawAliases.getData()
                .stream()
                .map(row -> new Alias(
                        row.get("pid"),
                        row.get("source"),
                        row.get("alias")
                    )
                )
                .collect(
                    toMap(Alias::getId, identity())
                );
        
        return tags;
    }

    public Map<String, File> getFiles()
    {
        RawTable rawFiles = getTable("files");
        
        Map<String, File> files = rawFiles.getData()
                .stream()
                .filter(row -> "_original".equals(row.get("filename")))
                .map(row -> new File(
                        row.get("fid"),
                        row.get("uid"),
                        row.get("filepath"),
                        row.get("filemime"),
                        row.get("filesize")
                    )
                )
                .collect(
                    toMap(File::getId, identity())
                );
        
        return files;
    }
    
    public Map<String, String> getImageMap(Map<String, File> files, Map<String, Node> nodes)
    {
        RawTable rawImages = getTable("image");
        
        Map<String, String> imageMap = new HashMap<>();
        
        rawImages.getData()
                .forEach(row -> {
                    String size = row.get("image_size");
                    if ("_original".equals(size)) // Skip thumbnails and previews
                    {
                        String nid = row.get("nid");
                        String fid = row.get("fid");
                        
                        if (!nodes.containsKey(nid))
                        {
                            System.err.format("Image with unknown node %s found%n", nid);
                        }
                        else if (!files.containsKey(fid))
                        {
                            System.err.format("Image with unknown file %s found%n", fid);
                        }
                        else
                        {
                            imageMap.put(nid, fid);
                        }
                    }
                });
        
        return imageMap;
        
    }

    private RawTable getTable(String tableName)
    {
        tableName = getTableName(tableName);

        RawTable table = rawData.get(tableName);

        if (table == null)
        {
            throw new IllegalStateException(tableName + " not found");
        }
        
        return table;
    }
}

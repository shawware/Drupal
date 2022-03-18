/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Renumbers data entities.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class DataRenumberer
{

    public void renumberContent(
            int startingNode,
            Map<String, Node> nodes,
            Map<String, List<FieldValue>> values,
            Map<String, File> files,
            Map<String, String> imageMap,
            Map<String, Alias> aliases
    ) {
        List<String> nids = getSortedIds(nodes);

        Map<String, String> nidMap = new HashMap<>();
        Map<String, Node> newNodes = new HashMap<>();
        for (int nid = 0; nid < nids.size(); nid++)
        {
            String oldNid = nids.get(nid);
            String newNid = String.valueOf(startingNode + nid);
            nidMap.put(oldNid, newNid);
            Node oldNode = nodes.remove(oldNid);
            String alias = convertAlias(nidMap, oldNode.getAlias());
            Node newNode = new Node(newNid, oldNode);
            newNode.setAlias(alias);
            addValues(newNode, oldNode.getValues());
            newNodes.put(newNid, newNode);
        }
        
        newNodes.values().forEach(node -> {
            node.setSummary(node.getSummary().map((s) -> renumberPaths(nidMap, s)).orElse(null));
            node.setBody(renumberPaths(nidMap, node.getBody()));
        });

        nodes.clear();
        nodes.putAll(newNodes);

        renumberValues(values, nidMap);
        renumberImageMap(imageMap, nidMap);
        renumberFiles(files, imageMap);
        renumberAliases(aliases, nidMap);
    }

    private void addValues(Node newNode, List<FieldValue> values)
    {
        values.forEach(oldValue -> {
            FieldValue newValue = new FieldValue(newNode.getId(), oldValue);
            newNode.addValue(newValue);
        });
    }
    
    private void renumberValues(Map<String, List<FieldValue>> values, Map<String, String> nidMap)
    {
        values.keySet().forEach(type -> {
            values.put(
                type,
                values.get(type)
                      .stream()
                      .map(value -> renumberValue(value, nidMap))
                      .collect(toList())
            );
        });
    }

    private FieldValue renumberValue(FieldValue oldValue, Map<String, String> nidMap)
    {
        String newNid = nidMap.get(oldValue.getId());

        FieldValue newValue;
        if (oldValue.getField().getType().equals("nodereference"))
        {
            if (!nidMap.containsKey(oldValue.getValue()))
            {
                System.err.format("Unknown nid %s in field %s%n", oldValue.getValue(), oldValue);
            }
            newValue = new FieldValue(newNid, oldValue.getDelta(), oldValue.getField(), nidMap.get(oldValue.getValue()));
            oldValue.getExtra().forEach(newValue::addExtra);
        }
        else
        {
            newValue = new FieldValue(newNid, oldValue);
        }
        return newValue;
    }

    /*
     * Image map (oldNid -> fid)
     * NID map (oldNid -> newNid)
     */
    private void renumberImageMap(Map<String, String> imageMap, Map<String, String> nidMap)
    {
        Map<String, String> newImageMap = new HashMap<>();
        imageMap.keySet().forEach(oldNid -> newImageMap.put(nidMap.get(oldNid), imageMap.get(oldNid)));
        imageMap.clear();
        imageMap.putAll(newImageMap);
    }

    /*
     * Renumber the files and update the image map (nid -> fid).
     */
    private void renumberFiles(Map<String, File> files, Map<String, String> imageMap)
    {
        Map<String, String> reverseImageMap = imageMap.entrySet()
                .stream()
                .collect(toMap(
                        Entry::getValue,
                        Entry::getKey
                    )
                );
        
        List<String> fids = getSortedIds(files);
        
        for (int id = 0; id < fids.size(); id++)
        {
            String fid = fids.get(id);
            File file = files.remove(fid);
            file = new File(String.valueOf(id + 1), file);
            files.put(file.getId(), file);
            
            if (!reverseImageMap.containsKey(fid))
            {
                System.err.format("Unknown fid %s%n", fid);
            }
            
            imageMap.put(
                    reverseImageMap.get(fid),
                    file.getId()
            );
        }
    }

    private void renumberAliases(Map<String, Alias> aliases, Map<String, String> nidMap)
    {
        List<String> ids = getSortedIds(aliases);
        
        for (int id = 0; id < ids.size(); id++)
        {
            String oldId = ids.get(id);
            Alias alias = aliases.remove(oldId);
            alias = new Alias(String.valueOf(id + 1), convertPath(nidMap, alias.getPath()), convertAlias(nidMap, alias.getAlias()));
            aliases.put(alias.getId(), alias);
        }
    }

    private String convertAlias(Map<String, String> nidMap, Optional<String> aliasHolder)
    {
        if (!aliasHolder.isPresent())
        {
            return null;
        }
        return convertAlias(nidMap, aliasHolder.get());
    }

    private String convertAlias(Map<String, String> nidMap, String alias)
    {
        return convertPath(nidMap, alias, "thing");
    }

    private String convertPath(Map<String, String> nidMap, String path)
    {
        return convertPath(nidMap, path, "node");
    }

    private String convertPath(Map<String, String> nidMap, String path, String prefix)
    {
        if (path.startsWith(prefix + "/"))
        {
            String oldNid = path.substring(prefix.length() + 1); // Skip over "prefix/"
            
            if (!nidMap.containsKey(oldNid))
            {
                System.err.format("Unknown nid %s in path %s%n", oldNid, path);
            }

            path = prefix + "/" + nidMap.get(oldNid);
        }
        return path;
    }
    
    /*package*/ String renumberPaths(Map<String, String> nidMap, String text)
    {
        // Replace "internal:" links as D9 does not have this module.
        String marker = "<a href=\"internal:";
        if (text.contains(marker))
        {
            text = text.replaceAll(marker, "<a href=\"/");
        }

        marker = "<a href=\"/thing/";
        if (text.contains(marker))
        {
            String[] elts = text.split(marker);

            List<String> modifiedElements = new ArrayList<>();
            modifiedElements.add(elts[0]);

            for (int i = 1; i < elts.length; i++)
            {
                int index = elts[i].indexOf('"');
                String oldNid = elts[i].substring(0, index);
                if (nidMap.containsKey(oldNid))
                {
                    String newNid = nidMap.get(oldNid);
                    String newLink = marker + newNid + elts[i].substring(index);
                    modifiedElements.add(newLink);
                }
                else
                {
                    System.err.format("Unknown nid %s in text: %s%n", oldNid, text);
                }
            }
            
            text = modifiedElements.stream().collect(joining(""));
        }
        return text;
    }

    private List<String> getSortedIds(Map<String, ? extends Entity> entities)
    {
        return entities.keySet()
                .stream()
                .map(Integer::parseInt)
                .sorted()
                .map(Object::toString)
                .collect(toList());
    }
}

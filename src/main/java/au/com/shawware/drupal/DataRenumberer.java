/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toList;

/**
 * Renumbers data entities.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class DataRenumberer
{
    /*
     * Renumber the files and update the image map (nid -> fid).
     */
    public void renumberFiles(Map<String, File> files, Map<String, String> imageMap)
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
            
            file = new File(String.valueOf(id + 1), file.getUid(), file.getPath(), file.getMimeType(), file.getSize());
            
            files.put(file.getId(), file);
            
            imageMap.put(
                    reverseImageMap.get(fid),
                    file.getId()
            );
        }
    }

    public void renumberAliases(Map<String, Alias> aliases)
    {
        List<String> ids = getSortedIds(aliases);
        
        for (int id = 0; id < ids.size(); id++)
        {
            String oldId = ids.get(id);
            
            Alias alias = aliases.remove(oldId);
            
            alias = new Alias(String.valueOf(id + 1), alias.getPath(), alias.getAlias());
            
            aliases.put(alias.getId(), alias);
        }
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

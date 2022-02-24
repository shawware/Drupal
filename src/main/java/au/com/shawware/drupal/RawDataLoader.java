/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.stream.Collectors.toMap;

/**
 * Loads the raw Drupal data (to be upgraded).
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class RawDataLoader
{
    public Map<String, RawTable> loadData(Path path)
    {
        ObjectMapper mapper = new ObjectMapper();
        
        try
        {
            List<RawTable> rawData = mapper.readValue(path.toFile(), new TypeReference<List<RawTable>>(){});
            
            return rawData
                    .stream()
                    .collect(
                       toMap(
                           RawTable::getTableName,
                           Function.identity()
                       )
                    );
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to load data from: " + path, e);
        }
    }
}

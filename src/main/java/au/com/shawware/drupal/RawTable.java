/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds the raw data from a single Drupal table.
 * 
 * A table is created in one of two ways:
 * (a) Directly from raw JSON data (during extraction); and
 * (b) Row-by-row from the model (during generation).
 * 
 * Validation of columns in only performed for (b).
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class RawTable
{
    private final String database;
    private final String tableName;
    private final String type;
    private final List<Map<String, String>> data;

    public RawTable(
            @JsonProperty("database") String database,
            @JsonProperty("name") String tableName,
            @JsonProperty("type") String type,
            @JsonProperty("data") List<Map<String, String>> data
    ) {
        this.type = type;
        this.tableName = tableName;
        this.database = database;
        this.data = data;
    }

    public String getDatabase()
    {
        return database;
    }

    public String getTableName()
    {
        return tableName;
    }

    public String getType()
    {
        return type;
    }

    public List<Map<String, String>> getData()
    {
        return data;
    }

    protected void addRow(Map<String, String> row)
    {
        data.add(row);
    }
    
    @Override
    public String toString()
    {
        return database + "::" + tableName + " (" + data.size() + ")";
    }
}

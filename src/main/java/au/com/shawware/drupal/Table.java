/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Extends a raw table with column definition.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class Table<T extends Entity> extends RawTable
{
    private final List<Column<T>> columns;
    private final List<String> columnNames;
    
    public Table(String database, String tableName, List<Column<T>> columns)
    {
        super("table", tableName, database, new ArrayList<>());

        this.columns = columns;
        this.columnNames = columns.stream().map(Column::getName).collect(toList());
    }

    public List<Column<T>> getColumns()
    {
        return columns;
    }

    public List<String> getColumnNames()
    {
        return columnNames;
    }

    @Override
    public void addRow(Map<String, String> row)
    {
        if (columnNames.size() != row.size())
        {
            throw new IllegalArgumentException("row contains wrong number of values: " + row.size());
        }
        columnNames.forEach(name -> {
            if (!row.containsKey(name)) {
                throw new IllegalArgumentException("row does not contain value for: " + name);
            }
        });
        super.addRow(row);
    }

}

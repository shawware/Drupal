/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Generates SQL to insert the raw data into a Drupal DB.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class SqlGenerator
{
    /**
     * Generates the necessary SQL.
     * 
     * @param data the data to generate the SQL for
     * @param stream the stream onto which to emit the SQL
     */
    public void generate(Map<String, Table<? extends Entity>> data, PrintStream stream)
    {
        generateHeader(stream);
        
        data.keySet().forEach(tableName -> {
            Table<?> table = data.get(tableName);
            stream.println(generateTable(table));
            stream.println();
        });

        generateFooter(stream);
    }
    
    private String generateTable(Table<? extends Entity> table)
    {
        if (table.getData().isEmpty())
        {
            System.err.println("Empty table found: " + table.getTableName());
            return "\n";
        }
        String prefix = "INSERT INTO `" + table.getTableName() + "` " + generateColumns(table.getColumnNames()) + " VALUES\n";

        return table.getData().stream()
                .map(row -> generateValues(table.getColumns(), row))
                .collect(joining(",\n", prefix, ";\n"));
    }

    private <T extends Entity> String generateValues(List<Column<T>> columns, Map<String, String> row)
    {
        return columns.stream()
                .map(column -> generateValue(
                        column.getType(),
                        row.get(column.getName())
                    )
                )
                .collect(joining(", ", "(", ")"));
    }

    private String generateValue(String type, String value)
    {
        if (value == null)
        {
            System.err.format("Null value for type: %s%n", type);
            value = "NULL";
        }
        String result = value;
        if (type.equals(Columns.TEXT) && !"NULL".equals(value))
        {
            result = "'" + value + "'";
        }
        else if (type.equals(Columns.HTML) && !"NULL".equals(value))
        {
            value = value.replaceAll("\u200E", ""); // Clean out some odd characters
            result = "'" + value.replaceAll("'", "\\\\\'") + "'";
        }
        return result;
    }

    private String generateColumns(List<String> columnNames)
    {
        return columnNames.stream()
                .map(name -> "`" + name + "`")
                .collect(joining(", ", "(", ")")); 
    }

    private void generateHeader(PrintStream stream)
    {
        stream.println("SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";");
        stream.println("SET AUTOCOMMIT = 0;");
        stream.println("START TRANSACTION;");
        stream.println("SET time_zone = \"+00:00\";");
        stream.println();
    }

    private void generateFooter(PrintStream stream)
    {
        stream.println("COMMIT;");
    }
}

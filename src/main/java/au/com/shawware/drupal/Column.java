/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.function.Function;

/**
 * Holds the basic data about a table column.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class Column<T extends Entity>
{
    private final String name;
    private final String type;
    private final Function<T, String> value;

    public Column(String name, String type)
    {
        this(name, type, String::valueOf); // Use any function really
    }

    public Column(String name, String type, Function<T, String> value)
    {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }
    
    public String getValue(T object)
    {
        return value.apply(object);
    }

    @Override
    public String toString()
    {
        return "{ " + name + ", " + type + " }";
    }
}

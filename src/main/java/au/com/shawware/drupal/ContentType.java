/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the basic content type data.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class ContentType
{
    private final String name;
    private final List<Field> fields;

    public ContentType(String name)
    {
        this.name = name;
        this.fields = new ArrayList<>();
    }

    public String getName()
    {
        return name;
    }
    
    public void addField(Field field)
    {
        fields.add(field);
    }

    public List<Field> getFields()
    {
        return fields;
    }

    @Override
    public String toString()
    {
        return "{ " + name + ", " + fields + " }";
    }
}

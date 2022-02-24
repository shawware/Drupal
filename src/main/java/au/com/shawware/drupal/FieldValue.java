/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a single value of a particular field. Most fields have just
 * a single value, however links have two, ie. the url and title.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class FieldValue extends Entity
{
    private final String delta;
    private final Field field;
    private final String value;
    private final Map<String, String> extra;

    public FieldValue(String id, String delta, Field field, String value)
    {
        super(id);
        this.delta = delta;
        this.field = field;
        this.value = value;
        this.extra = new HashMap<>();
    }

    public String getDelta()
    {
        return delta;
    }

    public Field getField()
    {
        return field;
    }

    public String getValue()
    {
        return value;
    }

    public Map<String, String> getExtra()
    {
        return extra;
    }
    
    public void addExtra(String key, String value)
    {
        extra.put(key, value);
    }

    public String getExtra(String key)
    {
        return extra.get(key);
    }

    @Override
    public String toString()
    {
        String result = "{ " + getId() + ", " + delta + ", "+ field + ", " + value;
        if (!extra.isEmpty())
        {
            result += ", ";
            result += extra;
        }
        result += " }";
        return result;
    }
}

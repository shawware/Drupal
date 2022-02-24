/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

/**
 * Holds the basic data for a Tag.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class Tag extends Entity
{
    private final String name;
    private final String description;

    public Tag(String id, String name, String description)
    {
        super(id);
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return "{ " + getId() + ", " + name + ", " + description + " }";
    }

}

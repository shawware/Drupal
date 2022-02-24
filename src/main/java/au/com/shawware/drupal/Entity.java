/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

/**
 * Base class for model entities.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public abstract class Entity
{
    private final String id;

    public Entity(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }
}

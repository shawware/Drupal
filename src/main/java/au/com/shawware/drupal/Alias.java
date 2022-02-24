/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

/**
 * Holds the basic data for a path alias.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class Alias extends Entity
{
    private final String path;
    private final String alias;

    public Alias(String id, String path, String alias)
    {
        super(id);
        this.path = path;
        this.alias = alias;
    }

    public String getPath()
    {
        return path;
    }

    public String getAlias()
    {
        return alias;
    }

    @Override
    public String toString()
    {
        return "{ " + getId() + ", " + path + ", " + alias + " }";
    }
}

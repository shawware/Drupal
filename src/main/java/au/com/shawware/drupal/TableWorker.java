/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

/**
 * Common code for services that manipulate tables.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 *
 */
abstract class TableWorker
{
    private final String prefix;

    public TableWorker(String prefix)
    {
        this.prefix = prefix;
    }
    
    public String getTableName(String tableName)
    {
        return prefix + tableName;
    }
}

/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

/**
 * Holds the basic data for a file. 
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class File extends Entity
{
    private final String uid;
    private final String path;
    private final String mimeType;
    private final String size;

    public File(String id, String uid, String path, String mimeType, String size)
    {
        super(id);
        this.uid = uid;
        this.path = path;
        this.mimeType = mimeType;
        this.size = size;
    }

    public String getUid()
    {
        return uid;
    }

    public String getPath()
    {
        return path;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public String getSize()
    {
        return size;
    }

    @Override
    public String toString()
    {
        return "{ " + getId() + ", " + uid + ", " + path + ", " + mimeType + ", " + size + " }";
    }
}

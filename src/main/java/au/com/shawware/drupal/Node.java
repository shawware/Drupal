/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Holds the basic data for a node. 
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class Node extends Entity
{
    private final String type;
    private final String uid;
    private final String created;
    private final String changed;
    private final String title;

    private String summary;
    private String body;
    private String bodyFormat;
    private String alias;
    private String reads;
    private String lastRead;
    private final List<Tag> tags;
    private final List<FieldValue> values;

    public Node(String nid, String type, String uid, String created, String changed, String title)
    {
        super(nid);
        this.type = type;
        this.uid = uid;
        this.created = created;
        this.changed = changed;
        this.title = title;
        
        this.tags = new ArrayList<>();
        this.values = new ArrayList<>();
    }

    /*
     * Create a new node with the given ID from the given old node.
     * We only set the attributes that do not contain the old ID.
     */
    public Node(String nid, Node oldNode)
    {
        this(nid, oldNode.type, oldNode.uid, oldNode.created, oldNode.changed, oldNode.title);
        
        this.summary = oldNode.summary;
        this.body = oldNode.body;
        this.bodyFormat = oldNode.bodyFormat;
        this.reads = oldNode.reads;
        this.lastRead = oldNode.lastRead;
        this.tags.addAll(oldNode.getTags());
    }

    public String getType()
    {
        return type;
    }

    public String getUid()
    {
        return uid;
    }

    public String getCreated()
    {
        return created;
    }

    public String getChanged()
    {
        return changed;
    }

    public String getTitle()
    {
        return title;
    }

    public Optional<String> getSummary()
    {
        return Optional.ofNullable(summary);
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public String getBodyFormat()
    {
        return bodyFormat;
    }

    public void setBodyFormat(String bodyFormat)
    {
        this.bodyFormat = bodyFormat;
    }

    public Optional<String> getAlias()
    {
        return Optional.ofNullable(alias);
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    Optional<String> getReads()
    {
        return Optional.ofNullable(reads);
    }

    public void setReads(String reads)
    {
        this.reads = reads;
    }

    public Optional<String> getLastRead()
    {
        return  Optional.ofNullable(lastRead);
    }

    public void setLastRead(String lastRead)
    {
        this.lastRead = lastRead;
    }

    public List<Tag> getTags()
    {
        return tags;
    }

    public void addTag(Tag tag)
    {
        tags.add(tag);
    }

    public List<FieldValue> getValues()
    {
        return values;
    }

    public void addValue(FieldValue value)
    {
        // TODO: check matches
        values.add(value);
    }

    @Override
    public String toString()
    {
        return "{ " + getId() + ", " + type + ", " + uid + ", " + created + ", " + changed +
                ", \"" + title + "\", \"" + trim(summary) + "\", \"" + trim(body) + "\" (" + bodyFormat + "), " +
                alias + ", " + reads + ", " + lastRead + ", " +
                values + " }";
    }
    
    private static String trim(String s)
    {
        String result;
        if (s == null)
        {
            result = "null";
        }
        else if (s.length() < 10)
        {
            result = s;
        }
        else
        {
            result = s.substring(0, 10) + " ...";
        }
        return result;
    }
}

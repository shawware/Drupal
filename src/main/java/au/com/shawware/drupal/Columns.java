/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Builds a table's columns.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class Columns<T extends Entity>
{
    /*package*/ static final String NUMBER = "number";
    /*package*/ static final String TEXT = "text";
    /*package*/ static final String HTML = "html";
    
    private final List<Column<T>> columns;

    public Columns()
    {
        columns = new ArrayList<>();
    }
    
    public Columns<T> addNumeric(String name, Function<T, String> value)
    {
        columns.add(new Column<>(name, NUMBER, value));
        return this;
    }

    public Columns<T> addNumeric(String name)
    {
        columns.add(new Column<>(name, NUMBER));
        return this;
    }

    public Columns<T> addText(String name, Function<T, String> value)
    {
        columns.add(new Column<>(name, TEXT, value));
        return this;
    }

    public Columns<T> addText(String name)
    {
        columns.add(new Column<>(name, TEXT));
        return this;
    }

    public Columns<T> addHTML(String name, Function<T, String> value)
    {
        columns.add(new Column<>(name, HTML, value));
        return this;
    }

    public List<Column<T>> build()
    {
        return columns;
    }
}

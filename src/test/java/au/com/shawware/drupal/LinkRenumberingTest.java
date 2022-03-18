/*
 * Copyright (C) 2022 shawware.com.au
 *
 * License: GNU General Public License V3 (or later)
 * http://www.gnu.org/copyleft/gpl.html
 */

package au.com.shawware.drupal;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Before;
import org.junit.Test;

/**
 * Verify the link renumbering algorithm.
 *
 * @author <a href="mailto:david.shaw@shawware.com.au">David Shaw</a>
 */
public class LinkRenumberingTest
{
    private Map<String, String> nidMap;
    private DataRenumberer renumberer;


    @Before
    public void setUp()
    {
        nidMap = Map.of(
                "100", "101",
                "103", "104",
                "104", "105"
            );

        renumberer = new DataRenumberer();
    }

    @Test
    public void testPlainText()
    {
        String text = "This is summary or body content with no links";
        
        String result = renumberer.renumberPaths(nidMap, text);
        
        assertThat(result, is("This is summary or body content with no links"));
    }

    @Test
    public void testLinkRequiringNoChange()
    {
        String text = "This is summary or body content with a link that should not change, eg. <a href=\"/drwho/ns1ep01\" title=\"Rose\">Rose</a>.";
        
        String result = renumberer.renumberPaths(nidMap, text);
        
        assertThat(result, is("This is summary or body content with a link that should not change, eg. <a href=\"/drwho/ns1ep01\" title=\"Rose\">Rose</a>."));
    }

    @Test
    public void testRenumberingSingleLink()
    {
        String text = "This is summary or body content containing links, eg. <a href=\"/thing/100\" title=\"Blah\">Blah</a>.";
        
        String result = renumberer.renumberPaths(nidMap, text);
        
        assertThat(result, is("This is summary or body content containing links, eg. <a href=\"/thing/101\" title=\"Blah\">Blah</a>."));
    }

    @Test
    public void testRenumberingMultipleLinks()
    {
        String text = "Links: <a href=\"/thing/100\" title=\"Blah\">Blah</a>, <a href=\"/drwho/ns1ep01\" title=\"Rose\">Rose</a>, <a href=\"/thing/104\" title=\"Blah\">Blah</a>.";
        
        String result = renumberer.renumberPaths(nidMap, text);
        
        assertThat(result, is("Links: <a href=\"/thing/101\" title=\"Blah\">Blah</a>, <a href=\"/drwho/ns1ep01\" title=\"Rose\">Rose</a>, <a href=\"/thing/105\" title=\"Blah\">Blah</a>."));
    }
}

/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.ows.kvp;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.XMLConstants;

import junit.framework.TestCase;

import org.geoserver.ows.kvp.NamespaceKvpParser;
import org.geoserver.platform.ServiceException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.helpers.NamespaceSupport;

public class NamespaceKvpParserTest {

    private NamespaceKvpParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new NamespaceKvpParser("namespace");
    }

    @Test
    public void testEmpty() throws Exception {
        NamespaceSupport ctx = parser.parse("");
        assertNotNull(ctx);
        List<String> prefixes = getPrefixes(ctx);
        assertTrue(prefixes.contains("xml"));// this one is always present
        assertEquals(1, prefixes.size());
    }

    @Test
    public void testFormatError() throws Exception {
        try {
            parser.parse("xmlns[bad=format]");
            fail("Expected IAE");
        } catch (ServiceException e) {
            assertProperServiceException(e);
        }

        try {
            parser.parse("xmlns(bad=http://format]");
            fail("Expected IAE");
        } catch (ServiceException e) {
            assertProperServiceException(e);
        }

        try {
            parser.parse("bad=http://format");
            fail("Expected IAE");
        } catch (ServiceException e) {
            assertProperServiceException(e);
        }
    }

    void assertProperServiceException(ServiceException e) {
        assertEquals(ServiceException.INVALID_PARAMETER_VALUE, e.getCode());
        assertEquals(parser.getKey(), e.getLocator());
    }

    @Test
    public void testSingle() throws Exception {
        NamespaceSupport ctx = parser.parse("xmlns(foo=http://bar)");
        assertEquals("http://bar", ctx.getURI("foo"));
    }

    @Test
    public void testMultiple() throws Exception {
        NamespaceSupport ctx = parser
                .parse("xmlns(foo=http://bar), xmlns(ex=http://example.com),xmlns(gs=http://geoserver.org)");
        assertEquals("http://bar", ctx.getURI("foo"));
        assertEquals("http://example.com", ctx.getURI("ex"));
        assertEquals("http://geoserver.org", ctx.getURI("gs"));
    }

    @Test
    public void testDefaultNamespace() throws Exception{
        NamespaceSupport ctx = parser.parse("xmlns(http://default.namespace.com)");
        assertEquals("http://default.namespace.com", ctx.getURI(XMLConstants.DEFAULT_NS_PREFIX));
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getPrefixes(NamespaceSupport ctx) {
        Enumeration<String> prefixes = ctx.getPrefixes();
        List<String> l = new ArrayList<String>();
        while (prefixes.hasMoreElements()) {
            l.add(prefixes.nextElement());
        }
        return l;
    }
}

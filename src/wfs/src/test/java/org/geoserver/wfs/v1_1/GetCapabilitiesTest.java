/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.v1_1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.data.test.CiteTestData;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.wfs.WFSGetFeatureOutputFormat;
import org.geoserver.wfs.WFSTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GetCapabilitiesTest extends WFSTestSupport {

    @Before
    public void revert() throws Exception {
        revertLayer(CiteTestData.UPDATES);
        revertLayer(CiteTestData.BUILDINGS);
    }

    @Override
    protected void setUpInternal(SystemTestData dataDirectory) throws Exception {
    	DataStoreInfo di = getCatalog().getDataStoreByName(CiteTestData.CITE_PREFIX);
    	di.setEnabled(false);
        getCatalog().save(di);
    }
    

    @Test
    public void testGet() throws Exception {
        Document doc = getAsDOM("wfs?service=WFS&request=getCapabilities&version=1.1.0");

        assertEquals("wfs:WFS_Capabilities", doc.getDocumentElement()
                .getNodeName());
        assertEquals("1.1.0", doc.getDocumentElement().getAttribute("version"));
        XpathEngine xpath =  XMLUnit.newXpathEngine();
        assertTrue(xpath.getMatchingNodes("//wfs:FeatureType", doc).getLength() > 0);
    }
    
    @Test
    public void testNamespaceFilter() throws Exception {
        // filter on an existing namespace
        Document doc = getAsDOM("wfs?service=WFS&version=1.1.0&request=getCapabilities&namespace=sf");
        Element e = doc.getDocumentElement();
        assertEquals("WFS_Capabilities", e.getLocalName());
        XpathEngine xpath =  XMLUnit.newXpathEngine();
        assertTrue(xpath.getMatchingNodes("//wfs:FeatureType/wfs:Name[starts-with(., sf)]", doc).getLength() > 0);
        assertEquals(0, xpath.getMatchingNodes("//wfs:FeatureType/wfs:Name[not(starts-with(., sf))]", doc).getLength());
        
        // try again with a missing one
        doc = getAsDOM("wfs?service=WFS&version=1.1.0&request=getCapabilities&namespace=NotThere");
        e = doc.getDocumentElement();
        assertEquals("WFS_Capabilities", e.getLocalName());
        assertEquals(0, xpath.getMatchingNodes("//wfs:FeatureType", doc).getLength());
    }

    @Test
    public void testPost() throws Exception {

        String xml = "<GetCapabilities service=\"WFS\" version='1.1.0'"
                + " xmlns=\"http://www.opengis.net/wfs\" "
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + " xmlns:ows=\"http://www.opengis.net/ows\" "
                + " xsi:schemaLocation=\"http://www.opengis.net/wfs "
                + " http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\">" 
                +   "<ows:AcceptVersions><ows:Version>1.1.0</ows:Version></ows:AcceptVersions>"
                + "</GetCapabilities>";

        Document doc = postAsDOM("wfs", xml);
        assertEquals("wfs:WFS_Capabilities", doc.getDocumentElement()
                .getNodeName());
        assertEquals("1.1.0", doc.getDocumentElement().getAttribute("version"));
    }

    @Test
    public void testPostNoSchemaLocation() throws Exception {
        String xml = "<GetCapabilities service=\"WFS\" version='1.1.0'"
                + " xmlns=\"http://www.opengis.net/wfs\" "
                + " xmlns:ows=\"http://www.opengis.net/ows\" "
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >"
                +   "<ows:AcceptVersions><ows:Version>1.1.0</ows:Version></ows:AcceptVersions>"
                + "</GetCapabilities>";

        Document doc = postAsDOM("wfs", xml);
        assertEquals("wfs:WFS_Capabilities", doc.getDocumentElement()
                .getNodeName());
        assertEquals("1.1.0", doc.getDocumentElement().getAttribute("version"));
    }
    
    @Test
    public void testOutputFormats() throws Exception {
        Document doc = getAsDOM("wfs?service=WFS&request=getCapabilities&version=1.1.0");
        
        // print(doc);

        // let's look for the outputFormat parameter values inside of the GetFeature operation metadata
        XpathEngine engine = XMLUnit.newXpathEngine();
        NodeList formats = engine.getMatchingNodes(
                "//ows:Operation[@name=\"GetFeature\"]/ows:Parameter[@name=\"outputFormat\"]/ows:Value", doc);
        
        Set<String> s1 = new TreeSet<String>();
        for ( int i = 0; i < formats.getLength(); i++ ) {
            String format = formats.item(i).getFirstChild().getNodeValue();
            s1.add( format );
        }
        
        List<WFSGetFeatureOutputFormat> extensions = GeoServerExtensions.extensions( WFSGetFeatureOutputFormat.class );
        
        Set<String> s2 = new TreeSet<String>();
        for ( Iterator e = extensions.iterator(); e.hasNext(); ) {
            WFSGetFeatureOutputFormat extension = (WFSGetFeatureOutputFormat) e.next();
            s2.addAll( extension.getOutputFormats() );
        }
        
        assertEquals( s1, s2 );
    }

    @Test
    public void testSupportedSpatialOperators() throws Exception {
        Document doc = getAsDOM("wfs?service=WFS&request=getCapabilities&version=1.1.0");

        // let's look for the spatial capabilities, extract all the spatial operators
        XpathEngine engine = XMLUnit.newXpathEngine();
        NodeList spatialOperators = engine
                .getMatchingNodes(
                        "//ogc:Spatial_Capabilities/ogc:SpatialOperators/ogc:SpatialOperator/@name",
                        doc);

        Set<String> ops = new TreeSet<String>();
        for (int i = 0; i < spatialOperators.getLength(); i++) {
            String format = spatialOperators.item(i).getFirstChild()
                    .getNodeValue();
            ops.add(format);
        }

        List<String> expectedSpatialOperators = getSupportedSpatialOperatorsList(false);
        assertEquals(expectedSpatialOperators.size(), ops.size());
        assertTrue(ops.containsAll(expectedSpatialOperators));
    }

    @Test
    public void testFunctionArgCount() throws Exception {
        Document doc = getAsDOM("wfs?service=WFS&request=getCapabilities&version=1.1.0");
        
        // print(doc);

        // let's check the argument count of "abs" function
        XMLAssert.assertXpathEvaluatesTo("1", "//ogc:FunctionName[text()=\"abs\"]/@nArgs", doc);
    }

    @Test
    public void testTypeNameCount() throws Exception {
        // filter on an existing namespace
        Document doc = getAsDOM("wfs?service=WFS&version=1.1.0&request=getCapabilities");
        Element e = doc.getDocumentElement();
        assertEquals("WFS_Capabilities", e.getLocalName());

        XpathEngine xpath = XMLUnit.newXpathEngine();

        final List<FeatureTypeInfo> enabledTypes = getCatalog().getFeatureTypes();
        for (Iterator<FeatureTypeInfo> it = enabledTypes.iterator(); it.hasNext();) {
            FeatureTypeInfo ft = it.next();
            if (!ft.enabled()) {
                it.remove();
            }
        }
        final int enabledCount = enabledTypes.size();

        assertEquals(enabledCount, xpath.getMatchingNodes(
                "/wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType", doc).getLength());
    }

    @Test
    public void testTypeNames() throws Exception {
        // filter on an existing namespace
        Document doc = getAsDOM("wfs?service=WFS&version=1.1.0&request=getCapabilities");
        Element e = doc.getDocumentElement();
        assertEquals("WFS_Capabilities", e.getLocalName());

        final List<FeatureTypeInfo> enabledTypes = getCatalog().getFeatureTypes();
        for (Iterator<FeatureTypeInfo> it = enabledTypes.iterator(); it.hasNext();) {
            FeatureTypeInfo ft = it.next();
            if (ft.enabled()) {
                String prefixedName = ft.getPrefixedName();

                String xpathExpr = "/wfs:WFS_Capabilities/wfs:FeatureTypeList/"
                        + "wfs:FeatureType/wfs:Name[text()=\"" + prefixedName + "\"]";

                XMLAssert.assertXpathExists(xpathExpr, doc);
            }
        }
    }
    
    @Test
    public void testLayerQualified() throws Exception {
     // filter on an existing namespace
        Document doc = getAsDOM("sf/PrimitiveGeoFeature/wfs?service=WFS&version=1.1.0&request=getCapabilities");
        
        Element e = doc.getDocumentElement();
        assertEquals("WFS_Capabilities", e.getLocalName());
        
        XpathEngine xpath =  XMLUnit.newXpathEngine();
        assertEquals(1, xpath.getMatchingNodes("//wfs:FeatureType/wfs:Name[starts-with(., sf)]", doc).getLength());
        assertEquals(0, xpath.getMatchingNodes("//wfs:FeatureType/wfs:Name[not(starts-with(., sf))]", doc).getLength());

        assertEquals(7, xpath.getMatchingNodes("//ows:Get[contains(@xlink:href,'sf/PrimitiveGeoFeature/wfs')]", doc).getLength());
        assertEquals(7, xpath.getMatchingNodes("//ows:Post[contains(@xlink:href,'sf/PrimitiveGeoFeature/wfs')]", doc).getLength());
        
        //TODO: test with a non existing workspace
    }
}

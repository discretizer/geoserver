/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts.dimensions;

import org.geoserver.catalog.DimensionDefaultValueSetting;
import org.geoserver.catalog.DimensionInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.wms.WMS;

/**
 * Represents an elevation dimension of a raster.
 */
public class RasterElevationDimension extends RasterDimension {

    public RasterElevationDimension(WMS wms, LayerInfo layerInfo, DimensionInfo dimensionInfo) {
        super(wms, ResourceInfo.ELEVATION, layerInfo, dimensionInfo, CoverageDimensionsReader.DataType.NUMERIC);
    }

    @Override
    protected String getDefaultValueFallbackAsString() {
        return "0";
    }

}

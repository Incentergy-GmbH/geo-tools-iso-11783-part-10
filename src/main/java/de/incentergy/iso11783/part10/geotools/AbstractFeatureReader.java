package de.incentergy.iso11783.part10.geotools;

import org.geotools.data.FeatureReader;
import org.geotools.data.store.ContentState;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public abstract class AbstractFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature>  {
    protected ContentState state;
}

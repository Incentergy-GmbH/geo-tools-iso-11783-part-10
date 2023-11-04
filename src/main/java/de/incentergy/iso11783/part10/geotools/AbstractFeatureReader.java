package de.incentergy.iso11783.part10.geotools;

import org.geotools.api.data.FeatureReader;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.store.ContentState;

public abstract class AbstractFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature>  {
    protected ContentState state;
}

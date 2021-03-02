package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

public class GridFeatureReader extends AbstractFeatureReader {

	@Override
	public SimpleFeatureType getFeatureType() {
		return null;
	}

	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		return null;
	}

	@Override
	public boolean hasNext() throws IOException {
		return false;
	}

	@Override
	public void close() throws IOException {
		
	}

}

package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

public class TimelogFeatureReader extends AbstractFeatureReader {

	@Override
	public FeatureType getFeatureType() {
		return null;
	}

	@Override
	public Feature next() throws IOException, IllegalArgumentException, NoSuchElementException {
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

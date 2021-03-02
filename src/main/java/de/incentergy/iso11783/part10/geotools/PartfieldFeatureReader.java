package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.feature.NameImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

public class PartfieldFeatureReader extends AbstractFeatureReader{
	
	public static final String TYPE_NAME_STRING = "Partfield";
	public static final Name TYPE_NAME = new NameImpl(PartfieldFeatureReader.class.getPackageName(), TYPE_NAME_STRING);

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

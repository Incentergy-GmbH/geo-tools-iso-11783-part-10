package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class ISO11783FeatureSource extends ContentFeatureSource {

	public ISO11783FeatureSource(ContentEntry entry, Query query) {
		super(entry, query);
	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
		return null;
	}

	@Override
	protected int getCountInternal(Query query) throws IOException {
		return 0;
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		switch(query.getTypeName()) {
			case PartfieldFeatureReader.TYPE_NAME_STRING:
				return new PartfieldFeatureReader(null, null);
		}
		return null;
	}

	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		return null;
	}

}

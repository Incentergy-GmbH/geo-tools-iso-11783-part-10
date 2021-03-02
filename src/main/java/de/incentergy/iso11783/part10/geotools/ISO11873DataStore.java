package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

public class ISO11873DataStore extends ContentDataStore {

	public static final List<Name> ISO11783_TYPES = Arrays.asList(
			PartfieldFeatureReader.TYPE_NAME,
			new NameImpl(ISO11873DataStore.class.getPackageName(), "GridCell"),
			new NameImpl(ISO11873DataStore.class.getPackageName(), "TimeLogPoint"));

	@Override
	protected List<Name> createTypeNames() throws IOException {
		return ISO11783_TYPES;
	}

	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		return null;
	}

}

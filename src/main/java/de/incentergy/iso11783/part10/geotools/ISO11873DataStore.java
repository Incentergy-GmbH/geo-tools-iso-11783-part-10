package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class ISO11873DataStore extends ContentDataStore {
    private Map<URL, ISO11783TaskDataFile> files = new ConcurrentHashMap<>();

	@Override
	protected List<Name> createTypeNames() throws IOException {
		return files.keySet().stream().map(url -> new NameImpl(url.toString())).collect(Collectors.toList());
	}

	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		return new ISO11783FeatureSource(files.get(new URL(entry.getName().getLocalPart())), entry, Query.ALL);
	}
}
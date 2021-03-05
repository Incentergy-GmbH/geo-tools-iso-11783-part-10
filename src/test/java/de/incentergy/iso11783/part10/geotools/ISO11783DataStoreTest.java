package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.junit.jupiter.api.Test;
import org.opengis.feature.type.Name;

class ISO11783DataStoreTest {

	@Test
	void testCreateTypeNames() throws Exception {

	}

	@Test
	void testCreateFeatureSource() throws Exception {
        ISO11783DataStore dataStore = new ISO11783DataStore();
        dataStore.updateFilesFromURL(getClass().getResource("/ISOXMLGenerator-100/"));
        List<Name> dataNames = dataStore.createTypeNames();
        ContentEntry entry = new ContentEntry(dataStore, dataNames.get(0));
        ContentFeatureSource featureSource = dataStore.createFeatureSource(entry);
        assertTrue(featureSource instanceof ISO11783FeatureSource);
	}

	@Test
	void testUpdateFilesFromURL() throws Exception {
        ISO11783DataStore dataStore = new ISO11783DataStore();
        dataStore.updateFilesFromURL(getClass().getResource("/ISOXMLGenerator-100/"));
        assertEquals(4, dataStore.getTypeNames().length);
        assertTrue(dataStore.getTypeNames()[0].startsWith("Partfield"));
    }
}

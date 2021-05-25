package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.List;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.junit.jupiter.api.Test;
import org.opengis.feature.type.Name;

class ISO11783DataStoreTest {


	@Test
	void testCreateFeatureSource() throws Exception {
        ISO11783DataStore dataStore = new ISO11783DataStore();
        dataStore.updateFilesFromURL(getClass().getResource("/ISOXMLGenerator-100/"));
        List<Name> dataNames = dataStore.createTypeNames();
        assertEquals(4, dataNames.size());
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

	@Test
	void testGetAvailableURLs() throws Exception {
        ISO11783DataStore dataStore = new ISO11783DataStore();
        dataStore.updateFilesFromURL(getClass().getResource("/ISOXMLGenerator-100/"));
        List<URL> urls = dataStore.getAvailableURLs();
        assertEquals(1, urls.size());
        assertTrue(urls.get(0).toString().endsWith("Taskdata-100.zip"));
    }

	@Test
	void testGetZipParserByURL() throws Exception {
        ISO11783DataStore dataStore = new ISO11783DataStore();
        dataStore.updateFilesFromURL(getClass().getResource("/ISOXMLGenerator-100/"));
        List<URL> urls = dataStore.getAvailableURLs();
        ISO11783TaskZipParser zipParser = dataStore.getZipParser(urls.get(0));
        assertNotNull(zipParser);
    }
}

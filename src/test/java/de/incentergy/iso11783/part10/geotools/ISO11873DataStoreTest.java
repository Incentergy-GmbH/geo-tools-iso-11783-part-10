package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class ISO11873DataStoreTest {

	@Test
	void testCreateTypeNames() throws Exception {

	}

	@Test
	void testCreateFeatureSource() throws Exception {

	}

	@Test
	void testUpdateFilesFromURL() throws Exception {
        ISO11873DataStore dataStore = new ISO11873DataStore();
        // dataStore.updateFilesFromURL(new URL('file:/'));
        dataStore.updateFilesFromURL(getClass().getResource("/"));
        assertEquals(Arrays.asList("TASKDATA-PartfieldFeatureReaderTest.XML"), dataStore.getTypeNames()); 
    }
}

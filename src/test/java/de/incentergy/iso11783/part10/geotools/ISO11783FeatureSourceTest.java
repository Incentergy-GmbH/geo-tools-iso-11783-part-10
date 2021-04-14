package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Optional;

import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

class ISO11783FeatureSourceTest {

    @Test
    void testISO11783FeatureSource() {
    }

    @Test
    void testGetBoundsInternalQuery() {
    }

    @Test
    void testGetCountInternalQuery() {
    }

    @Test
    void testGetReaderInternalQuery() {
    }

    @Test
    void testBuildFeatureType() {
        ISO11783DataStore dataStore = new ISO11783DataStore();
        dataStore.updateFilesFromURL(getClass().getResource("/TLGData/"));
        Optional<Name> dataName;
        try {
            dataName = dataStore.createTypeNames().stream().filter(dn -> dn.getLocalPart().startsWith("TimeLog"))
                    .findAny();
            ContentEntry entry = new ContentEntry(dataStore, dataName.get());

            ISO11783FeatureSource featureSource = new ISO11783FeatureSource(dataStore.getZipParser(dataName.get()), entry, Query.ALL);
            SimpleFeatureType featureType = featureSource.buildFeatureType();
            assertEquals(50, featureType.getAttributeCount());
            assertNotNull(featureType.getDescriptor("DDI271_DET-1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@Test
	void addAttributesForTimeLog() {
		
	}

}

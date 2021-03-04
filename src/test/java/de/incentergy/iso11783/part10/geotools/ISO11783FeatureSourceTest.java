package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;
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
        ISO11873DataStore dataStore = new ISO11873DataStore();
        dataStore.updateFilesFromURL(getClass().getResource("/TLGData/"));
        Optional<Name> dataName;
        try {
            dataName = dataStore.createTypeNames().stream().filter(dn -> dn.getLocalPart().startsWith("TimeLog"))
                    .findAny();
            ContentEntry entry = new ContentEntry(dataStore, dataName.get());

            ISO11783FeatureSource featureSource = new ISO11783FeatureSource(dataStore.getZipParser(dataName.get()), entry, Query.ALL);
            SimpleFeatureType featureType = featureSource.buildFeatureType();
            assertEquals(48, featureType.getAttributeCount());
            assertEquals("DDI271_DET-1", featureType.getAttributeDescriptors().get(10).getLocalName());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@Test
	void addAttributesForTimeLog() {
		
	}

}

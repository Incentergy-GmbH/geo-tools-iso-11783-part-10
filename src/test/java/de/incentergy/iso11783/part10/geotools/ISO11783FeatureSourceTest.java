package de.incentergy.iso11783.part10.geotools;

import java.net.URL;

import org.junit.jupiter.api.Test;

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
        URL url = getClass().getResource("TLGData/Taskdata.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        ISO11783FeatureSource featureSource = new ISO11783FeatureSource(taskZipParser);
	}
	
	@Test
	void addAttributesForTimeLog() {
		
	}

}

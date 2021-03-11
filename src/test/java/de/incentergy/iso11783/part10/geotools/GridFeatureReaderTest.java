package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeature;

class GridFeatureReaderTest {

	@Test
	void testNext() throws IllegalArgumentException, NoSuchElementException, IOException {
		ISO11783TaskZipParser parser = new ISO11783TaskZipParser(getClass().getResource("/fmis/2021-03-03T12-59_05.955Z_prescription_taskdata.zip"));

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
			builder.setName("Test");
			ISO11783FeatureSource.addAttributesForGrid(builder, parser.getGridList());
		GridFeatureReader gridFeatureReader = new GridFeatureReader(parser.getGridList(), builder.buildFeatureType());
		assertEquals(56, gridFeatureReader.gridEntries.size());
		SimpleFeature simpleFeature = gridFeatureReader.next();
		assertNotNull(simpleFeature);
	}

}

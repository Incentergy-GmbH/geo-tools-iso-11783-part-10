package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Test;
import org.geotools.api.feature.simple.SimpleFeature;

class GridFeatureReaderTest {

	@Test
	void testNext() throws IllegalArgumentException, NoSuchElementException, IOException {
		ISO11783TaskZipParser parser = new ISO11783TaskZipParser(
            getClass().getResource("/fmis/2021-03-03T12-59_05.955Z_prescription_taskdata.zip")
        );
		GridFeatureReader gridFeatureReader = new GridFeatureReader(parser.getGridList(), new NameImpl("Test"));
		assertEquals(56, gridFeatureReader.gridEntries.size());
		SimpleFeature simpleFeature = gridFeatureReader.next();
		assertNotNull(simpleFeature);

        ReferencedEnvelope envelope = gridFeatureReader.getBounds();

        assertTrue(
            envelope.equals(
                new ReferencedEnvelope(
                    13.300059609987226, 13.301137858011213, 52.49561250770104, 52.496414297664636,
                    DefaultGeographicCRS.WGS84
                )
            )
        );
	}

}

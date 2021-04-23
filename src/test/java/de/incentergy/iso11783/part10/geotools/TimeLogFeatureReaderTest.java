package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;

import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

class TimeLogFeatureReaderTest {

    @Test
    void test() {
    }

	@Test
	void testGetFeatureType() {
	}

	@Test
	void testNext() {
        URL url = getClass().getResource("/TLGData/machinedata_1.zip");
        ISO11783TaskZipParser parser = new ISO11783TaskZipParser(url);
        TimeLogFeatureReader timeLogReader = new TimeLogFeatureReader(parser.getTimeLogList(), new NameImpl("Test"));
        try {
            SimpleFeature feature = timeLogReader.next();
            SimpleFeatureType featureType = feature.getFeatureType();
            assertEquals(50, featureType.getAttributeCount());
            assertEquals(Point.class, featureType.getDescriptor("position").getType().getBinding());
            assertEquals(Long.class, featureType.getDescriptor("time").getType().getBinding());
            assertEquals("TLG00004", (String) feature.getAttribute("filename"));
            assertNotNull(featureType.getDescriptor("DDI60170_DET-1"));
            assertNotNull(featureType.getDescriptor("DDI117_DET-1"));
            timeLogReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Test
	void testHasNext() {
        URL url = getClass().getResource("/ISOXMLGenerator-100/Taskdata-100.zip");
        ISO11783TaskZipParser parser = new ISO11783TaskZipParser(url);
        TimeLogFeatureReader timeLogReader = new TimeLogFeatureReader(parser.getTimeLogList(), new NameImpl("Test"));
        try {
            assertTrue(timeLogReader.hasNext());
            timeLogReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Test
	void testGetBounds() {
        URL url = getClass().getResource("/ISOXMLGenerator-100/Taskdata-100.zip");
        ISO11783TaskZipParser parser = new ISO11783TaskZipParser(url);
        TimeLogFeatureReader timeLogReader = new TimeLogFeatureReader(parser.getTimeLogList(), new NameImpl("Test"));
        try {
            ReferencedEnvelope envelope = timeLogReader.getBounds();

            assertTrue(
                envelope.equals(
                    new ReferencedEnvelope(
                        11.4009179, 11.4015303, 48.2640165, 48.264558199999996,
                        DefaultGeographicCRS.WGS84
                    )
                )
            );
            assertTrue(timeLogReader.hasNext());
            timeLogReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Test
	void testClose() {
	}

}

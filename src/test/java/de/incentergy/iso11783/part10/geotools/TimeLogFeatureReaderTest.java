package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.xml.bind.JAXBContext;

class TimeLogFeatureReaderTest {

    @Test
    void test() {
    }

	@Test
	void testGetFeatureType() {
	}

	@Test
	void testNext() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("TEST");
        URL url = getClass().getResource("/ISOXMLGenerator-100/Taskdata-100.zip");
        ISO11783TaskZipParser parser = new ISO11783TaskZipParser(url);
        ISO11783FeatureSource.addAttributesForTimeLog(builder, parser);
        SimpleFeatureType featureType = builder.buildFeatureType();
        TimeLogFeatureReader timeLogReader = new TimeLogFeatureReader(parser.getTimeLogList(), featureType);
        try {
            SimpleFeature feature = timeLogReader.next();
            assertEquals(7, feature.getFeatureType().getAttributeCount());
            assertEquals(Point.class, feature.getFeatureType().getDescriptor("position").getType().getBinding());
            assertEquals(Long.class, feature.getFeatureType().getDescriptor("time").getType().getBinding());
            timeLogReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Test
	void testHasNext() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("TEST");
        URL url = getClass().getResource("/ISOXMLGenerator-100/Taskdata-100.zip");
        ISO11783TaskZipParser parser = new ISO11783TaskZipParser(url);
        ISO11783FeatureSource.addAttributesForTimeLog(builder, parser);
        SimpleFeatureType featureType = builder.buildFeatureType();
        TimeLogFeatureReader timeLogReader = new TimeLogFeatureReader(parser.getTimeLogList(), featureType);
        try {
            assertTrue(timeLogReader.hasNext());
            timeLogReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Test
	void testClose() {
	}

    @Test
    public void testForBoundsInternal() {
        try {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("TEST");
            URL url = getClass().getResource("/ISOXMLGenerator-100/Taskdata-100.zip");
            ISO11783TaskZipParser parser = new ISO11783TaskZipParser(url);
            ISO11783FeatureSource.addAttributesForTimeLog(builder, parser);
            SimpleFeatureType featureType = builder.buildFeatureType();
            TimeLogFeatureReader timeLogReader = new TimeLogFeatureReader(parser.getTimeLogList(), featureType);

            Envelope envelope = timeLogReader.getBoundsInternal();
            assertNotNull(envelope);
            System.out.println("== envelope " + envelope);
            timeLogReader.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

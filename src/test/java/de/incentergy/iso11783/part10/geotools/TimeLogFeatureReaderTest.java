package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.Test;
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
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("TEST");
        URL url = getClass().getResource("/ISOXMLGenerator-100/Taskdata-100.zip");
        ISO11783TaskZipParser parser = new ISO11783TaskZipParser(url);
        ISO11783FeatureSource.addAttributesForTimeLog(builder, parser);
        SimpleFeatureType featureType = builder.buildFeatureType();
        TimeLogFeatureReader timeLogReader = new TimeLogFeatureReader(parser.timeLogList, featureType);
        try {
            SimpleFeature feature = timeLogReader.next();
            assertEquals(5, feature.getFeatureType().getAttributeCount());
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
        TimeLogFeatureReader timeLogReader = new TimeLogFeatureReader(parser.timeLogList, featureType);
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

}

package de.incentergy.iso11783.part10.geotools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBContext;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class GuidancePatternFeatureReaderTest {
 
    
    @Test
    void testCanReadGuidancePatterns() throws Exception{
        ISO11783TaskDataFile iSO11783TaskDataFile = (ISO11783TaskDataFile) JAXBContext
        .newInstance(ISO11783TaskDataFile.class).createUnmarshaller()
        .unmarshal(getClass().getResourceAsStream("/GuidanceFeatureReaderTest/TASKDATA.xml"));
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Test");
        ISO11783FeatureSource.addAttributesForGuidancePattern(builder);
        GuidancePatternFeatureReader guidancePatternFeatureReader = new GuidancePatternFeatureReader(iSO11783TaskDataFile,
                builder.buildFeatureType());
        assertTrue(guidancePatternFeatureReader.hasNext());
        SimpleFeature simpleFeature = guidancePatternFeatureReader.next();
        assertEquals(guidancePatternFeatureReader.countFeatures(),2);
    }
}

package de.incentergy.iso11783.part10.geotools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBContext;

import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Test;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class GuidancePatternFeatureReaderTest {
 
    
    @Test
    void testCanReadGuidancePatterns() throws Exception{
        ISO11783TaskDataFile iSO11783TaskDataFile = (ISO11783TaskDataFile) JAXBContext
            .newInstance(ISO11783TaskDataFile.class).createUnmarshaller()
            .unmarshal(getClass().getResourceAsStream("/GuidanceFeatureReaderTest/TASKDATA.xml"));

        GuidancePatternFeatureReader guidancePatternFeatureReader = new GuidancePatternFeatureReader(
            iSO11783TaskDataFile,
            new NameImpl("Test")
        );

        assertTrue(guidancePatternFeatureReader.hasNext());
        // SimpleFeature simpleFeature = guidancePatternFeatureReader.next();
        assertEquals(2, guidancePatternFeatureReader.countGuidancePatterns());

        ReferencedEnvelope envelope = guidancePatternFeatureReader.getBounds();

        assertTrue(
            envelope.equals(
                new ReferencedEnvelope(
                    11.397423305669008, 11.400106140800974, 48.24147179065358, 48.24225406302071,
                    DefaultGeographicCRS.WGS84
                )
            )
        );

        guidancePatternFeatureReader.close();
    }
}

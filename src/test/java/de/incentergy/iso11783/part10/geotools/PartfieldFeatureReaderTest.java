package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeature;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

class PartfieldFeatureReaderTest {

	@Test
	void test() {
		try {
			ISO11783TaskDataFile iSO11783TaskDataFile = (ISO11783TaskDataFile) JAXBContext
					.newInstance(ISO11783TaskDataFile.class).createUnmarshaller()
					.unmarshal(getClass().getResourceAsStream("/PartfieldFeatureReaderTest/TASKDATA.XML"));
			PartfieldFeatureReader partfieldFeatureReader = new PartfieldFeatureReader(
                iSO11783TaskDataFile,
                new NameImpl("Test")
            );
			assertTrue(partfieldFeatureReader.hasNext());
			SimpleFeature simpleFeature = partfieldFeatureReader.next();
			assertFalse(partfieldFeatureReader.hasNext());

			assertEquals(
					"MULTIPOLYGON (((12.402130956 50.26397688, 12.401874961 50.263908764, 12.401789806 50.263887622, 12.400962091 50.263689209, 12.400517699 50.263575196, 12.400214418 50.26349941, 12.400111694 50.263480128, 12.399885878 50.263436836, 12.399746213 50.263421201, 12.399309188 50.263377912, 12.398913061 50.263338741, 12.398739579 50.263317916, 12.398572779 50.263276036, 12.398551832 50.263266554, 12.398499315 50.263255734, 12.398449415 50.263271096, 12.398404987 50.263418495, 12.398388626 50.26351683, 12.398375219 50.26371243, 12.398425483 50.264052443, 12.398445939 50.264387098, 12.398499601 50.264428974, 12.398583393 50.264466515, 12.398655469 50.264495033, 12.399078446 50.264618201, 12.39931735 50.264702365, 12.399960169 50.264985318, 12.400437665 50.26518098, 12.400608785 50.265241794, 12.40082038 50.265303932, 12.401246308 50.265398992, 12.401471501 50.265450545, 12.40163268 50.265484242, 12.401865639 50.26517501, 12.401987263 50.265025275, 12.402093599 50.264869705, 12.402198696 50.264704865, 12.402287892 50.264552084, 12.402352986 50.264363834, 12.402397101 50.264207491, 12.40245353 50.264072612, 12.402130956 50.26397688), (12.40144198 50.264104862, 12.401383904 50.264072184, 12.401422732 50.264034434, 12.401489344 50.26406275, 12.40144198 50.264104862)))",
					simpleFeature.getDefaultGeometry().toString());

            ReferencedEnvelope envelope = partfieldFeatureReader.getBounds();

            assertTrue(
                envelope.equals(
                    new ReferencedEnvelope(
                        12.398375219, 12.40245353, 50.263255734, 50.265484242,
                        DefaultGeographicCRS.WGS84
                    )
                )
            );
			partfieldFeatureReader.close();

		} catch (JAXBException | IllegalArgumentException | NoSuchElementException | IOException e) {
			e.printStackTrace();
		}
	}
	@Test
	void testIncorrectInnerRings() {
        try {
            ISO11783TaskZipParser parser = new ISO11783TaskZipParser(
                getClass().getResource("/PartfieldFeatureReaderTest/OnlyInternalRings.zip")
            );
            PartfieldFeatureReader partfieldFeatureReader = new PartfieldFeatureReader(parser.getTaskFile(), new NameImpl("Test"));

            assertTrue(partfieldFeatureReader.hasNext());
            SimpleFeature simpleFeature = partfieldFeatureReader.next();
            assertFalse(partfieldFeatureReader.hasNext());

            assertEquals(
                "MULTIPOLYGON (((9.578275681 45.527349447, 9.576762915 45.527834246, 9.576349854 45.527781632, 9.576768279 45.527026246, 9.578275681 45.527349447), (9.57767101636706 45.52745763739564, 9.57766985408884 45.52745107750382, 9.57767282507787 45.52744628737123, 9.57766940352461 45.52743459904838, 9.57768165100308 45.52742360171913, 9.57768649853766 45.52740826051854, 9.57768028904729 45.52739507595199, 9.57767473877815 45.52738931591291, 9.57766660281851 45.52738429994719, 9.5776558248489 45.52738088639017, 9.57764658421075 45.52738045631693, 9.57762188319379 45.52738476418116, 9.57759805899104 45.52739590904849, 9.57757270255029 45.52741371637118, 9.57756520530003 45.52742455624464, 9.5775552784803 45.52744938772859, 9.57754379884894 45.52751279040272, 9.57752112517448 45.52757277950087, 9.57751554151326 45.52759895877937, 9.57751714408931 45.52761366747788, 9.57752182136072 45.52762392614123, 9.57756207240412 45.52766965648522, 9.57757167659844 45.52768295082684, 9.57758182866021 45.52768990211123, 9.57760505261826 45.52769615072022, 9.57766187025429 45.52770137996842, 9.57771459941354 45.52770977710492, 9.57778124276754 45.52772754393043, 9.57767101636706 45.52745763739564)))",
                simpleFeature.getDefaultGeometry().toString()
            );

            ReferencedEnvelope envelope = partfieldFeatureReader.getBounds();
            assertTrue(
                envelope.equals(
                    new ReferencedEnvelope(
                        9.576349854, 9.578275681, 45.527026246, 45.527834246,
                        DefaultGeographicCRS.WGS84
                    )
                )
            );

			partfieldFeatureReader.close();
		} catch (IllegalArgumentException | NoSuchElementException | IOException e) {
			e.printStackTrace();
		}
    }

	@Test
	void testNoGeometry() {
        try {
            ISO11783TaskZipParser parser = new ISO11783TaskZipParser(
                getClass().getResource("/PartfieldFeatureReaderTest/PartfieldWithoutGeometry.zip")
            );
            PartfieldFeatureReader partfieldFeatureReader = new PartfieldFeatureReader(parser.getTaskFile(), new NameImpl("Test"));

            assertTrue(partfieldFeatureReader.hasNext());

            SimpleFeature feature = partfieldFeatureReader.next();
            assertNull(feature.getDefaultGeometry());

            assertNull(partfieldFeatureReader.getBounds());

			partfieldFeatureReader.close();
		} catch (IllegalArgumentException | NoSuchElementException | IOException e) {
			e.printStackTrace();
		}
    }
}

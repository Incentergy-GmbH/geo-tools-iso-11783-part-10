package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

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
			PartfieldFeatureReader partfieldFeatureReader = new PartfieldFeatureReader(iSO11783TaskDataFile, null);
			assertTrue(partfieldFeatureReader.hasNext());
			SimpleFeature simpleFeature = partfieldFeatureReader.next();
			assertFalse(partfieldFeatureReader.hasNext());

			assertEquals(
					"MULTIPOLYGON (((12.402130956 50.26397688, 12.401874961 50.263908764, 12.401789806 50.263887622, 12.400962091 50.263689209, 12.400517699 50.263575196, 12.400214418 50.26349941, 12.400111694 50.263480128, 12.399885878 50.263436836, 12.399746213 50.263421201, 12.399309188 50.263377912, 12.398913061 50.263338741, 12.398739579 50.263317916, 12.398572779 50.263276036, 12.398551832 50.263266554, 12.398499315 50.263255734, 12.398449415 50.263271096, 12.398404987 50.263418495, 12.398388626 50.26351683, 12.398375219 50.26371243, 12.398425483 50.264052443, 12.398445939 50.264387098, 12.398499601 50.264428974, 12.398583393 50.264466515, 12.398655469 50.264495033, 12.399078446 50.264618201, 12.39931735 50.264702365, 12.399960169 50.264985318, 12.400437665 50.26518098, 12.400608785 50.265241794, 12.40082038 50.265303932, 12.401246308 50.265398992, 12.401471501 50.265450545, 12.40163268 50.265484242, 12.401865639 50.26517501, 12.401987263 50.265025275, 12.402093599 50.264869705, 12.402198696 50.264704865, 12.402287892 50.264552084, 12.402352986 50.264363834, 12.402397101 50.264207491, 12.40245353 50.264072612, 12.402130956 50.26397688), (12.40144198 50.264104862, 12.401383904 50.264072184, 12.401422732 50.264034434, 12.401489344 50.26406275, 12.40144198 50.264104862)))",
					simpleFeature.getDefaultGeometry().toString());
			partfieldFeatureReader.close();

		} catch (JAXBException | IllegalArgumentException | NoSuchElementException | IOException e) {
			e.printStackTrace();
		}
	}

}

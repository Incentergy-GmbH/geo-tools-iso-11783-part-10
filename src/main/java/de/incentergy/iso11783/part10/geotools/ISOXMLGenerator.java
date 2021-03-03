package de.incentergy.iso11783.part10.geotools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import de.incentergy.iso11783.part10.v4.AllocationStamp;
import de.incentergy.iso11783.part10.v4.CropType;
import de.incentergy.iso11783.part10.v4.CropVariety;
import de.incentergy.iso11783.part10.v4.CulturalPractice;
import de.incentergy.iso11783.part10.v4.Customer;
import de.incentergy.iso11783.part10.v4.Device;
import de.incentergy.iso11783.part10.v4.DeviceAllocation;
import de.incentergy.iso11783.part10.v4.DeviceElement;
import de.incentergy.iso11783.part10.v4.DeviceElementType;
import de.incentergy.iso11783.part10.v4.Farm;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import de.incentergy.iso11783.part10.v4.LineString;
import de.incentergy.iso11783.part10.v4.LineStringType;
import de.incentergy.iso11783.part10.v4.OperTechPractice;
import de.incentergy.iso11783.part10.v4.OperationTechnique;
import de.incentergy.iso11783.part10.v4.OperationTechniqueReference;
import de.incentergy.iso11783.part10.v4.Partfield;
import de.incentergy.iso11783.part10.v4.Point;
import de.incentergy.iso11783.part10.v4.Polygon;
import de.incentergy.iso11783.part10.v4.PolygonType;
import de.incentergy.iso11783.part10.v4.Product;
import de.incentergy.iso11783.part10.v4.Task;
import de.incentergy.iso11783.part10.v4.TaskStatus;
import de.incentergy.iso11783.part10.v4.Time;
import de.incentergy.iso11783.part10.v4.TimeLog;
import de.incentergy.iso11783.part10.v4.TimeLogType;
import de.incentergy.iso11783.part10.v4.TimeType;

public class ISOXMLGenerator {

	private static Date date19800101;

	static {
		try {
			date19800101 = new SimpleDateFormat("yyyy-MM-dd").parse("1980-01-01");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws JAXBException, IOException, DatatypeConfigurationException {
		for (int sampleCount : Arrays.asList(100000)) {
			ISO11783TaskDataFile iso11783TaskData = new ISO11783TaskDataFile();
			iso11783TaskData.setManagementSoftwareManufacturer(ISOXMLGenerator.class.getSimpleName());

			Customer customer = new Customer();
			customer.setCustomerFirstName("Gen");
			customer.setCustomerLastName("Customer");
			customer.setCustomerId("CTR-1");

			iso11783TaskData.getCustomer().add(customer);

			Farm farm = new Farm();
			farm.setFarmDesignator("Gen Farm");
			farm.setFarmId("FRM-1");
			farm.setCustomerIdRef(customer);

			iso11783TaskData.getFarm().add(farm);

			CropType cropType = new CropType();
			cropType.setCropTypeId("CTP-1");
			cropType.setCropTypeDesignator("Mais");

			CropVariety cropVariety = new CropVariety();
			cropVariety.setCropVarietyId("CVT-1");
			cropVariety.setCropVarietyDesignator("ES Eurojet");
			cropType.getCropVariety().add(cropVariety);

			Product product = new Product();
			product.setProductDesignator("Saatgut Mais - ES Eurojet");
			product.setProductId("PDT-1");
			iso11783TaskData.getProduct().add(product);
			cropVariety.setProductIdRef(product);

			iso11783TaskData.getCropType().add(cropType);

			Partfield partfield = getPartfield(farm, customer, cropType,
					cropVariety);

			iso11783TaskData.getPartfield().add(partfield);

			CulturalPractice culturalPractice = new CulturalPractice();
			culturalPractice.setCulturalPracticeDesignator("Häckseln");
			culturalPractice.setCulturalPracticeId("CPC-1");

			iso11783TaskData.getCulturalPractice().add(culturalPractice);

			OperationTechnique operationTechnique = new OperationTechnique();
			operationTechnique.setOperationTechniqueDesignator("Häckseln");
			operationTechnique.setOperationTechniqueId("OTQ-1");

			iso11783TaskData.getOperationTechnique().add(operationTechnique);

			OperationTechniqueReference operationTechniqueReference = new OperationTechniqueReference();
			operationTechniqueReference.setOperationTechniqueIdRef(operationTechnique);
			culturalPractice.getOperationTechniqueReference().add(operationTechniqueReference);

			Task task = new Task();
			TimeZone tz = TimeZone.getTimeZone("UTC");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
			df.setTimeZone(tz);
			Date now = new Date();
			String nowAsISO = df.format(now);

			task.setTaskDesignator("Gen " + nowAsISO);
			task.setTaskStatus(TaskStatus.COMPLETED);
			task.setTaskId("TSK-1");
			OperTechPractice operTechPractice = new OperTechPractice();
			operTechPractice.setCulturalPracticeIdRef(culturalPractice);
			operTechPractice.setOperationTechniqueIdRef(operationTechnique);
			task.setOperTechPractice(operTechPractice);
			task.setCustomerIdRef(customer);
			task.setPartfieldIdRef(partfield);

			Device device = new Device();
			device.setDeviceId("DVC-1");
			device.setClientNAME("Gen Client".getBytes());
			device.setDeviceDesignator("Gen Tractor");

			DeviceElement deviceElement = new DeviceElement();
			deviceElement.setDeviceElementId("DET-1");
			deviceElement.setDeviceElementType(DeviceElementType.DEVICE);
			device.getDeviceElement().add(deviceElement);
			iso11783TaskData.getDevice().add(device);

			DeviceAllocation deviceAllocation = new DeviceAllocation();
			deviceAllocation.setDeviceIdRef(device);

			task.getDeviceAllocation().add(deviceAllocation);

			iso11783TaskData.getTask().add(task);

			Time time = new Time();
			time.setType(TimeType.EFFECTIVE);
			time.setStart(DatatypeFactory.newInstance().newXMLGregorianCalendar(nowAsISO));
			task.getTime().add(time);

			TimeLog timeLog = new TimeLog();
			String timelogFilename = "TLG00001";
			timeLog.setFilename(timelogFilename);
			timeLog.setTimeLogType(TimeLogType.BINARY_TIMELOG_FILE_TYPE_1);
			task.getTimeLog().add(timeLog);

			File dir = new File("src/test/resources/ISOXMLGenerator-" + sampleCount);
			if (!dir.exists()) {
				dir.mkdir();
			}

			File internalDir = new File("src/test/resources/ISOXMLGenerator-" + sampleCount + "/TASKDATA");
			if (!internalDir.exists()) {
				internalDir.mkdir();
			}

			generateTimeLogXmlFile(timelogFilename, sampleCount);
			Date stopDate = generateTimeLogBinFile(now, timelogFilename, sampleCount);

			AllocationStamp allocationStamp = new AllocationStamp();
			allocationStamp.setStart(DatatypeFactory.newInstance().newXMLGregorianCalendar(nowAsISO));
			allocationStamp.setStop(DatatypeFactory.newInstance().newXMLGregorianCalendar(df.format(stopDate)));
			deviceAllocation.setAllocationStamp(allocationStamp);

			JAXBContext jaxbContext = JAXBContext.newInstance(ISO11783TaskDataFile.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(iso11783TaskData,
					new FileOutputStream("src/test/resources/ISOXMLGenerator-" + sampleCount + "/TASKDATA/TASKDATA.XML"));

			FileOutputStream fos = new FileOutputStream(
					"src/test/resources/ISOXMLGenerator-" + sampleCount + "/Taskdata-" + sampleCount + ".zip");
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (String fileName : Arrays.asList("TASKDATA/TASKDATA.XML", "TASKDATA/" + timelogFilename + ".BIN", "TASKDATA/" + timelogFilename + ".XML")) {
				addToZipFile("src/test/resources/ISOXMLGenerator-" + sampleCount + "/", fileName, zos);
			}

			zos.close();
			fos.close();

		}
	}

	private static Partfield getPartfield(Farm farm, Customer customer, CropType cropType, CropVariety cropVariety) {
//		<PFD A="PFD1" B="ed5440f0-5304-408a-a527-33b68b28" C="7 Partfield 2017" D="45550" E="CTR1" F="FRM1">
		Partfield partfield = new Partfield();
		partfield.setPartfieldId("PFD-1");
		partfield.setPartfieldCode("ed5440f0-5304-408a-a527-33b68b28");
		partfield.setPartfieldDesignator("7 Partfield 2017");
		partfield.setPartfieldArea(45550);
		partfield.setCustomerIdRef(customer);
		partfield.setFarmIdRef(farm);
		partfield.setCropTypeIdRef(cropType);
		partfield.setCropVarietyIdRef(cropVariety);

//        <PLN A="1" C="45550">
		Polygon polygon = new Polygon();
		partfield.getPolygonNonTreatmentZoneOnly().add(polygon);
		polygon.setPolygonType(PolygonType.PARTFIELD_BOUNDARY);
		polygon.setPolygonArea(45550l);

//            <LSG A="1">
		LineString lineString = new LineString();
		polygon.getLineString().add(lineString);
		lineString.setLineStringType(LineStringType.POLYGONEXTERIOR);

		Point point;
//                <PNT A="2" C="48.263976880" D="11.402130956"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263976880");
		point.setPointEast("11.402130956");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263908764" D="11.401874961"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263908764");
		point.setPointEast("11.401874961");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263887622" D="11.401789806"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263887622");
		point.setPointEast("11.401789806");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263689209" D="11.400962091"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263689209");
		point.setPointEast("11.400962091");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263575196" D="11.400517699"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263575196");
		point.setPointEast("11.400517699");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263499410" D="11.400214418"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263499410");
		point.setPointEast("11.400214418");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263480128" D="11.400111694"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263480128");
		point.setPointEast("11.400111694");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263436836" D="11.399885878"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263436836");
		point.setPointEast("11.399885878");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263421201" D="11.399746213"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263421201");
		point.setPointEast("11.399746213");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263377912" D="11.399309188"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263377912");
		point.setPointEast("11.399309188");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263338741" D="11.398913061"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263338741");
		point.setPointEast("11.398913061");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263317916" D="11.398739579"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263317916");
		point.setPointEast("11.398739579");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263276036" D="11.398572779"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263276036");
		point.setPointEast("11.398572779");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263266554" D="11.398551832"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263266554");
		point.setPointEast("11.398551832");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263255734" D="11.398499315"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263255734");
		point.setPointEast("11.398499315");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263271096" D="11.398449415"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263271096");
		point.setPointEast("11.398449415");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263418495" D="11.398404987"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263418495");
		point.setPointEast("11.398404987");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263516830" D="11.398388626"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263516830");
		point.setPointEast("11.398388626");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263712430" D="11.398375219"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263712430");
		point.setPointEast("11.398375219");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264052443" D="11.398425483"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264052443");
		point.setPointEast("11.398425483");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264387098" D="11.398445939"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264387098");
		point.setPointEast("11.398445939");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264428974" D="11.398499601"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264428974");
		point.setPointEast("11.398499601");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264466515" D="11.398583393"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264466515");
		point.setPointEast("11.398583393");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264495033" D="11.398655469"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264495033");
		point.setPointEast("11.398655469");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264618201" D="11.399078446"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264618201");
		point.setPointEast("11.399078446");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264702365" D="11.399317350"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264702365");
		point.setPointEast("11.399317350");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264985318" D="11.399960169"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264985318");
		point.setPointEast("11.399960169");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.265180980" D="11.400437665"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.265180980");
		point.setPointEast("11.400437665");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.265241794" D="11.400608785"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.265241794");
		point.setPointEast("11.400608785");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.265303932" D="11.400820380"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.265303932");
		point.setPointEast("11.400820380");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.265398992" D="11.401246308"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.265398992");
		point.setPointEast("11.401246308");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.265450545" D="11.401471501"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.265450545");
		point.setPointEast("11.401471501");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.265484242" D="11.401632680"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.265484242");
		point.setPointEast("11.401632680");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.265175010" D="11.401865639"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.265175010");
		point.setPointEast("11.401865639");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.265025275" D="11.401987263"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.265025275");
		point.setPointEast("11.401987263");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264869705" D="11.402093599"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264869705");
		point.setPointEast("11.402093599");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264704865" D="11.402198696"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264704865");
		point.setPointEast("11.402198696");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264552084" D="11.402287892"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264552084");
		point.setPointEast("11.402287892");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264363834" D="11.402352986"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264363834");
		point.setPointEast("11.402352986");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264207491" D="11.402397101"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264207491");
		point.setPointEast("11.402397101");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264072612" D="11.402453530"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264072612");
		point.setPointEast("11.402453530");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.263976880" D="11.402130956"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.263976880");
		point.setPointEast("11.402130956");
		lineString.getPoint().add(point);
//            </LSG>
//            <LSG A="2">
		lineString = new LineString();
		polygon.getLineString().add(lineString);
		lineString.setLineStringType(LineStringType.POLYGONINTERIOR);
//                <PNT A="2" C="48.264104862" D="11.401441980"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264104862");
		point.setPointEast("11.401441980");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264072184" D="11.401383904"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264072184");
		point.setPointEast("11.401383904");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264034434" D="11.401422732"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264034434");
		point.setPointEast("11.401422732");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264062750" D="11.401489344"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264062750");
		point.setPointEast("11.401489344");
		lineString.getPoint().add(point);
//                <PNT A="2" C="48.264104862" D="11.401441980"/>
		point = new Point();
		point.setPointType("2");
		point.setPointNorth("48.264104862");
		point.setPointEast("11.401441980");
		lineString.getPoint().add(point);
//            </LSG>
//        </PLN>
//    </PFD>
		return partfield;
	}

	/**
	 * 
	 * @param now
	 * @param timelogFilename
	 * @param sampleCount
	 * @return the last date of the generated data.
	 */
	private static Date generateTimeLogBinFile(Date now, String timelogFilename, int sampleCount) {
		int millisecondsSinceMidnight = 0;
		short daysSince1980 = getDaysBetweenDateAnd19800101(now);
		try {

			ByteBuffer isoXmlTimeLogBin = ByteBuffer.allocate(sampleCount * 40);
			isoXmlTimeLogBin.order(ByteOrder.LITTLE_ENDIAN);

			// Partfield near Günding in Bavaria
			int positionNorth = 482641197;
			int positionEast = 114014195;

			for (int i = 0; i < sampleCount; i++) {
				isoXmlTimeLogBin.putInt(millisecondsSinceMidnight);
				isoXmlTimeLogBin.putShort(daysSince1980);
				// one second later
				millisecondsSinceMidnight += 1000;

				isoXmlTimeLogBin.putInt(positionNorth);
				// Move Position North randomly
				positionNorth += ThreadLocalRandom.current().nextInt(1, 1001) - 500;
				// Move East North randomly
				isoXmlTimeLogBin.putInt(positionEast);
				positionEast += ThreadLocalRandom.current().nextInt(1, 1001) - 500;

				// write amount of DLVs
				byte b = 5;
				isoXmlTimeLogBin.put(b);

				byte numOfDlvs = 0;
				isoXmlTimeLogBin.put(numOfDlvs);
				// 84 - Mass Per Area Yield mg/m² (1 - 100)
				isoXmlTimeLogBin.putInt(ThreadLocalRandom.current().nextInt(1, 101));

				numOfDlvs++;
				isoXmlTimeLogBin.put(numOfDlvs);
				// 149 - Fuel consumption per time mm³/s (1 - 100)
				isoXmlTimeLogBin.putInt(ThreadLocalRandom.current().nextInt(1, 101));

				numOfDlvs++;
				isoXmlTimeLogBin.put(numOfDlvs);
				// 192 - Ambient temperature mK (~ 10° - 20 ° C)
				isoXmlTimeLogBin.putInt(ThreadLocalRandom.current().nextInt(0, 11) * 1000 + 283000);

				numOfDlvs++;
				isoXmlTimeLogBin.put(numOfDlvs);
				// 67 - Actual Working Width mm (3m, 6m or 9m)
				isoXmlTimeLogBin.putInt(ThreadLocalRandom.current().nextInt(1, 4) * 300);

				numOfDlvs++;
				isoXmlTimeLogBin.put(numOfDlvs);
				// 177 - Actual length of cut mm (5mm - 20mm)
				isoXmlTimeLogBin.putInt(ThreadLocalRandom.current().nextInt(5, 21));
			}

			Files.write(Paths.get("src/test/resources/ISOXMLGenerator-" + sampleCount + "/TASKDATA/" + timelogFilename + ".BIN"),
					isoXmlTimeLogBin.array());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return getDateFromISO11783(millisecondsSinceMidnight, daysSince1980).getTime();

	}

	private static void generateTimeLogXmlFile(String timelogFilename, int sampleCount) {
		try {
			Files.write(Paths.get("src/test/resources/ISOXMLGenerator-" + sampleCount + "/TASKDATA/" + timelogFilename + ".XML"),
					("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + "<TIM A=\"\" D=\"4\">\n"
							+ " <PTN A=\"\" B=\"\" />\n"
							+ " <!-- 84 - Mass Per Area Yield mg/m² --><DLV A=\"0054\" B=\"\" C=\"DET-1\" />\n"
							+ " <!-- 149 - Fuel consumption per time mm³/s --><DLV A=\"0095\" B=\"\" C=\"DET-1\" />\n"
							+ " <!-- 192 - Ambient temperature mK --><DLV A=\"00C0\" B=\"\" C=\"DET-1\" />\n"
							+ " <!-- 67 - Actual Working Width mm --><DLV A=\"0043\" B=\"\" C=\"DET-1\" />\n"
							+ " <!-- 177 - Actual length of cut mm --><DLV A=\"00B1\" B=\"\" C=\"DET-1\" />\n"
							+ "</TIM>").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addToZipFile(String folderName, String fileName, ZipOutputStream zos)
			throws FileNotFoundException, IOException {

		File file = new File(folderName + fileName);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}

	public static Calendar getDateFromISO11783(long millisecondsSinceMidnight, int daysSince1980) {
		Calendar cal = Calendar.getInstance();
		cal.clear(Calendar.ZONE_OFFSET);
		cal.setTime(date19800101);
		cal.add(Calendar.DATE, daysSince1980); // minus number would decrement the days
		cal.add(Calendar.MILLISECOND, (int) millisecondsSinceMidnight);
		return cal;
	}

	public static short getDaysBetweenDateAnd19800101(Date date) {
		return (short) ((date.getTime() - date19800101.getTime()) / (24 * 3600 * 1000));
	}
}
package de.incentergy.iso11783.part10.geotools;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import de.incentergy.iso11783.part10.v4.DataLogValue;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import de.incentergy.iso11783.part10.v4.Position;
import de.incentergy.iso11783.part10.v4.PositionStatus;
import de.incentergy.iso11783.part10.v4.Time;
import de.incentergy.iso11783.part10.v4.TimeLog;

public class TimeLogFileData {

    private static Logger log = Logger.getLogger(TimeLogFileData.class.getName());
    private UUID id = java.util.UUID.randomUUID();

    private ISO11783TaskDataFile iso11783TaskData;

    private byte[] xmlFile;

    private byte[] binFile;

    private TimeLog timeLog;

    private List<Time> times = new ArrayList<>();

    private static JAXBContext jaxbContext;

    private static Date date19800101;

    private static BigDecimal pointOfOriginPositiv = new BigDecimal(1);
    private static BigDecimal pointOfOriginNegativ = new BigDecimal(-1);
    private static DatatypeFactory datatypeFactory;

    static {
        try {
            // Creating a jaxbContext is expensive. Make sure to do it
            // only once.
            jaxbContext = JAXBContext.newInstance(Time.class);
        } catch (JAXBException e) {
            log.log(Level.SEVERE, "Could not create jaxbContext for time", e);
        }

        // only create the dateformat once
        try {
            date19800101 = new SimpleDateFormat("yyyy-MM-dd").parse("1980-01-01");
        } catch (ParseException e) {
            log.log(Level.SEVERE, "Could not parse date 1980-01-01", e);
        }

        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }

    // 4326 -> WGS84
    static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    public TimeLogFileData() {

    }

    public TimeLogFileData(ISO11783TaskDataFile iso11783TaskData, TimeLog timeLog, byte[] xmlFile, byte[] binFile) {
        super();
        this.iso11783TaskData = iso11783TaskData;
        setBinFile(binFile);
        setXmlFile(xmlFile);
        setTimeLog(timeLog);
        convertTimeLogFileToTimeLogData();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TimeLog getTimeLog() {
        return timeLog;
    }

    public void setTimeLog(TimeLog timeLog) {
        this.timeLog = timeLog;
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        TimeLogFileData.log = log;
    }

    public ISO11783TaskDataFile getIso11783TaskData() {
        return iso11783TaskData;
    }

    public void setIso11783TaskData(ISO11783TaskDataFile iso11783TaskData) {
        this.iso11783TaskData = iso11783TaskData;
    }

    public byte[] getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(byte[] xmlFile) {
        this.xmlFile = xmlFile;
    }

    public byte[] getBinFile() {
        return binFile;
    }

    public void setBinFile(byte[] binFile) {
        this.binFile = binFile;
    }

    /**
     * Creates a new time instance directly from the byteArray.
     *
     * @param xmlByteArray
     */
    private Time createNewTimeInstance(Unmarshaller unmarshaller, byte[] xmlByteArray) {
        Time time = null;
        try {
            time = (Time) unmarshaller.unmarshal(new ByteArrayInputStream(xmlByteArray));
        } catch (JAXBException e) {
            log.log(Level.WARNING, "unable to read xml file", e);
        }
        return time;
    }

    private Time cloneTimeInstance(Time proto) {
        return proto;
    }

    public void convertTimeLogFileToTimeLogData() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(getBinFile());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        Map<String, Boolean> structureMap = createStructureMap(new ByteArrayInputStream(getXmlFile()));
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = ISO11873DataStore.jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            log.warning("could not create JAXBContext");
        }
        if (unmarshaller == null) {
            return;
        }
        Time time = createNewTimeInstance(unmarshaller, getXmlFile());
        while (byteBuffer.hasRemaining()) {
            try {
                // This is way faster than always calling createNewTimeInstance
                Time time2 = copyTime(time);
                readBinaryData(byteBuffer, structureMap, time2);
                Position position = time2.getPosition().get(0);
                if (positionIsValidEPSG4326Coordinate(position)) {
                    this.getTimes().add(time2);
                }
            } catch (NullPointerException e) {
                log.log(Level.FINEST, "could not create Time instance", e);
                return;
            } catch (Exception e) {
                log.log(Level.WARNING, "error reading binary data", e);
                return;
            }
        }
    }

    private Time copyTime(Time time) {
        Time time2 = new Time();
        time2.setStart(time.getStart());
        time2.setStop(time.getStop());
        time2.setType(time.getType());
        time2.setDuration(time.getDuration());
        for (DataLogValue dataLogValue : time.getDataLogValue()) {
            DataLogValue newDataLogValue = new DataLogValue();
            newDataLogValue.setProcessDataDDI(dataLogValue.getProcessDataDDI());
            newDataLogValue.setDeviceElementIdRef(dataLogValue.getDeviceElementIdRef());
            time2.getDataLogValue().add(newDataLogValue);
        }
        for (Position position : time.getPosition()) {
            Position newPosition = new Position();
            newPosition.setGpsUtcDate(position.getGpsUtcDate());
            newPosition.setGpsUtcTime(position.getGpsUtcTime());
            newPosition.setHDOP(position.getHDOP());
            newPosition.setNumberOfSatellites(position.getNumberOfSatellites());
            newPosition.setPDOP(position.getPDOP());
            newPosition.setPositionEast(position.getPositionEast());
            newPosition.setPositionNorth(position.getPositionNorth());
            newPosition.setPositionStatus(position.getPositionStatus());
            newPosition.setPositionUp(position.getPositionUp());
            time2.getPosition().add(newPosition);
        }
        return time2;
    }

    /**
     * Reads the information directly from the streams and the byteArray.
     *
     * @param timeToCSV
     * @param positionToCSV
     * @param dataLogValueToCSV
     */
    public void convertTimeLogFileToTimeLogData(List<String> timeToCSV, List<String> positionToCSV,
            List<String> dataLogValueToCSV) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(getBinFile());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        Map<String, Boolean> structureMap = createStructureMap(new ByteArrayInputStream(getXmlFile()));
        Unmarshaller unmarshaller = null;
        Time proto = null;
        try {
            unmarshaller = jaxbContext.createUnmarshaller();
            proto = createNewTimeInstance(unmarshaller, getXmlFile());
        } catch (JAXBException | NullPointerException e) {
            log.warning("could not create JAXBContext");
        }
        if (proto == null) {
            return;
        }
        while (byteBuffer.hasRemaining()) {
            try {
                Time time = cloneTimeInstance(proto);
                readBinaryData(byteBuffer, structureMap, time);
            } catch (Exception e) {
                log.log(Level.WARNING, "error reading binary data");
                return;
            }
        }
    }

    private static boolean positionIsValidEPSG4326Coordinate(Position pos) {
        boolean eastCoordinateIsValid = pos.getPositionEast().compareTo(pointOfOriginPositiv) > 0
                || pos.getPositionEast().compareTo(pointOfOriginNegativ) < 0;
        boolean northCoordinateIsValid = pos.getPositionNorth().compareTo(pointOfOriginPositiv) > 0
                || pos.getPositionNorth().compareTo(pointOfOriginNegativ) < 0;
        return eastCoordinateIsValid || northCoordinateIsValid;
    }

    private void readBinaryData(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Time time) {
        // Uint32 Local time zone milliseconds since midnight
        long millisecondsSinceMidnight = Unsigned.getUnsignedInt(byteBuffer);
        // Uint16 Local time zone days since 1980-01-01
        int daysSince1980 = Unsigned.getUnsignedShort(byteBuffer);
        setTimeStart(time, millisecondsSinceMidnight, daysSince1980);

        setTimePosition(byteBuffer, structureMap, time);
        List<DataLogValue> dlvs = time.getDataLogValue();
        short numberOfDLvs = Unsigned.getUnsignedByte(byteBuffer);
        // Search constants for DLVS
        for (int i = 0; i < numberOfDLvs; ++i) {
            final short dlvNumber = Unsigned.getUnsignedByte(byteBuffer);
            final int dlvValue = byteBuffer.getInt();
            try {
                dlvs.get(dlvNumber).setProcessDataValue(dlvValue);
            } catch (IndexOutOfBoundsException aie) {
                log.warning(() -> String.format("Can't read index: %d for DLV value: %d", dlvNumber, dlvValue));
            }
        }
    }

    private void readPositionNorth(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        // A PositionNorth 32-bit integer 107 degrees WGS-84
        if (structureMap.containsKey("PNT-A")) {
            position.setPositionNorth(BigDecimal.valueOf(byteBuffer.getInt() * 1e-7));
        }
    }

    private void readPositionEast(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        // B PositionEast 32-bit integer 107 degrees WGS-84
        if (structureMap.containsKey("PNT-B")) {
            position.setPositionEast(BigDecimal.valueOf(byteBuffer.getInt() * 1e-7));
        }
    }

    private void readPositionUp(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        // C Millimetres relative to WGS-84 ellipsoid
        if (structureMap.containsKey("PNT-C")) {
            position.setPositionUp(byteBuffer.getInt());
        }
    }

    /*
     * D Position status. Definition references NMEA2000 MethodGNSS parameter. 0 =
     * no GPS fix 1 = GNSS fix 2 = DGNSS fix 3 = Precise GNSS, no deliberate
     * degradation (such as SA), and higher resolution code (P-code) and 2
     * frequencies are used to correct atmospheric delays 4 = RTK Fixed Integer 5 =
     * RTK Float 6 = Est(DR)mode 7 = Manual Input 8 = Simulate mode 9-13 = Reserved
     * 14 = Error 15 = PositionStatus value not available
     */
    private void readPositionStatus(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        if (structureMap.containsKey("PNT-D")) {
            String positionStatusString = Byte.toString(byteBuffer.get());
            PositionStatus positionStatus = PositionStatus.POSITIONSTATUS_VALUE_IS_NOT_AVAILABLE;
            try {
                positionStatus = PositionStatus.fromValue(positionStatusString);
            } catch (IllegalArgumentException e) {
                log.log(Level.WARNING, "no valid position status", e);
            }
            position.setPositionStatus(positionStatus);
        }
    }

    private void readPDOP(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        if (structureMap.containsKey("PNT-E")) {
            position.setPDOP(BigDecimal.valueOf(Unsigned.getUnsignedShort(byteBuffer)));
        }
    }

    private void readHDOP(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        if (structureMap.containsKey("PNT-F")) {
            position.setHDOP(BigDecimal.valueOf(Unsigned.getUnsignedShort(byteBuffer)));
        }
    }

    private void readNumberOfSatellites(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        if (structureMap.containsKey("PNT-G")) {
            position.setNumberOfSatellites((short) byteBuffer.get());
        }
    }

    private void readGpsUtcTime(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        if (structureMap.containsKey("PNT-H")) {
            position.setGpsUtcTime(Unsigned.getUnsignedInt(byteBuffer));
        }
    }

    private void readGpsUtcDate(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Position position) {
        if (structureMap.containsKey("PNT-I")) {
            position.setGpsUtcDate(Unsigned.getUnsignedShort(byteBuffer));
        }
    }

    /*
     * Reads a Position from the byteBuffer! Attention! Do not alter the order of
     * function calls, as it determines the byte processing
     */
    private void setTimePosition(ByteBuffer byteBuffer, Map<String, Boolean> structureMap, Time time) {
        Position position = time.getPosition().get(0);
        readPositionNorth(byteBuffer, structureMap, position);
        readPositionEast(byteBuffer, structureMap, position);
        readPositionUp(byteBuffer, structureMap, position);
        readPositionStatus(byteBuffer, structureMap, position);
        readPDOP(byteBuffer, structureMap, position);
        readHDOP(byteBuffer, structureMap, position);
        readNumberOfSatellites(byteBuffer, structureMap, position);
        readGpsUtcTime(byteBuffer, structureMap, position);
        readGpsUtcDate(byteBuffer, structureMap, position);
    }

    private void setTimeStart(Time time, long millisecondsSinceMidnight, int daysSince1980) {
        Calendar cal = getDateFromISO11783(millisecondsSinceMidnight, daysSince1980);
        time.setStart(datatypeFactory.newXMLGregorianCalendar((GregorianCalendar) cal));
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
		return (short) ((date.getTime()-date19800101.getTime())/(24*3600*1000));
	}

	/**
	 * Creates a structure map from an InputStream that was created from a TimeLog
	 * description file.
	 *
	 * @param timeLogXmlInputStream
	 * @return
	 */
	private Map<String, Boolean> createStructureMap(InputStream timeLogXmlInputStream) {
		Map<String, Boolean> map = new HashMap<>();
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(timeLogXmlInputStream);
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					checkStartElementAndAddToMap(map, xmlEvent);
				}
			}

		} catch (XMLStreamException e) {
			log.log(Level.SEVERE, "Can't create structure map", e);
		}
		return map;
	}

	private void checkStartElementAndAddToMap(Map<String, Boolean> map, XMLEvent xmlEvent) {
		StartElement startElement = xmlEvent.asStartElement();
		if (startElement.getName().getLocalPart().equals("PTN")) {

			Iterator<Attribute> it = startElement.getAttributes();
			while (it.hasNext()) {
				Attribute attribute = it.next();
				if ("".equals(attribute.getValue())) {
					map.put("PNT-" + attribute.getName().getLocalPart(), true);
				}
			}
		}
	}

	public List<Time> getTimes() {
		return times;
	}

	public void setTimes(List<Time> times) {
		this.times = times;
	}
}

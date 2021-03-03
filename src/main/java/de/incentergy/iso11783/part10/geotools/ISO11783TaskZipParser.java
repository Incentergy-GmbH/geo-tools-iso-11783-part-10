package de.incentergy.iso11783.part10.geotools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.ObjectUtils.Null;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class ISO11783TaskZipParser {
    ISO11783TaskDataFile taskFile;
    private static Logger log = Logger.getLogger(ISO11783TaskZipParser.class.getName());
    private Map<String, InputStream> timeLogBinFiles = new HashMap<>();
    private Map<String, InputStream> timeLogXmlFiles = new HashMap<>();
    private Map<String, InputStream> gridBinFiles = new HashMap<>();

    Pattern TLG_BIN_PATTERN = Pattern.compile(".*TLG[0-9]+\\.BIN$");
    Pattern TLG_XML_PATTERN = Pattern.compile(".*TLG[0-9]+\\.XML$");

    public ISO11783TaskZipParser(URL url) {
        try {
            ZipInputStream zipStream = new ZipInputStream(url.openStream());
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory() == false) {
                    String upperName = entry.getName().toUpperCase();
                    if (upperName.endsWith("TASKDATA.XML")) {
                        this.taskFile = (ISO11783TaskDataFile) ISO11873DataStore.jaxbContext.createUnmarshaller()
                                .unmarshal(zipStream);
                    } else if(TLG_BIN_PATTERN.matcher(upperName).matches()) {
                        ByteArrayOutputStream boas = new ByteArrayOutputStream();
                        zipStream.transferTo(boas);
                        ByteArrayInputStream bais = new ByteArrayInputStream(boas.toByteArray());
                        timeLogBinFiles.put(upperName, bais);
                    } else if(TLG_XML_PATTERN.matcher(upperName).matches()) {
                        ByteArrayOutputStream boas = new ByteArrayOutputStream();
                        zipStream.transferTo(boas);
                        ByteArrayInputStream bais = new ByteArrayInputStream(boas.toByteArray());
                        timeLogXmlFiles.put(upperName, bais);
                        Map<String, Boolean> structure = createStructureMap(bais);

                    }
                }
            }
        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
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

                    @SuppressWarnings("unchecked")
                    Iterator<Attribute> it = startElement.getAttributes();
                    while (it.hasNext()) {
                            Attribute attribute = it.next();
                            if ("".equals(attribute.getValue())) {
                                    map.put("PNT-" + attribute.getName().getLocalPart(), true);
                            }
                    }
            }
    }
}

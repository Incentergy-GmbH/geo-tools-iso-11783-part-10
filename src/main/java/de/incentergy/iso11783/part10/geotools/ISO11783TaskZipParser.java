package de.incentergy.iso11783.part10.geotools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import de.incentergy.iso11783.part10.v4.TimeLog;

public class ISO11783TaskZipParser {
    ISO11783TaskDataFile taskFile;
    private static Logger log = Logger.getLogger(ISO11783TaskZipParser.class.getName());
    private Map<String, byte[]> timeLogBinFiles = new HashMap<>();
    private Map<String, byte[]> timeLogXmlFiles = new HashMap<>();
    private Map<String, byte[]> gridBinFiles = new HashMap<>();

    Pattern TLG_BIN_PATTERN = Pattern.compile(".*TLG[0-9]+\\.BIN$");
    Pattern TLG_XML_PATTERN = Pattern.compile(".*TLG[0-9]+\\.XML$");
    private List<TimeLogFileData>  timeLogList;

    public ISO11783TaskZipParser(URL url) {
        try (ZipInputStream zipStream = new ZipInputStream(url.openStream())) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory() == false) {
                    String upperName = entry.getName().toUpperCase();
                    String fileName = Path.of(upperName).getFileName().toString();
                    if (upperName.endsWith("TASKDATA.XML")) {
                        ByteArrayOutputStream boas = new ByteArrayOutputStream();
                        zipStream.transferTo(boas);
                        ByteArrayInputStream bais = new ByteArrayInputStream(boas.toByteArray());
                        this.taskFile = (ISO11783TaskDataFile) ISO11873DataStore.jaxbContext.createUnmarshaller()
                                .unmarshal(bais);
                    } else if(TLG_BIN_PATTERN.matcher(Path.of(upperName).getFileName().toString()).matches()) {
                        ByteArrayOutputStream boas = new ByteArrayOutputStream();
                        zipStream.transferTo(boas);
                        timeLogBinFiles.put(fileName, boas.toByteArray());
                    } else if(TLG_XML_PATTERN.matcher(upperName).matches()) {
                        ByteArrayOutputStream boas = new ByteArrayOutputStream();
                        zipStream.transferTo(boas);
                        timeLogXmlFiles.put(fileName, boas.toByteArray());
                    }
                }
                zipStream.closeEntry();
            }


            List<TimeLog> taskDataTimeLogList = this.taskFile.getTask().stream().flatMap((task)->task.getTimeLog().stream()).collect(Collectors.toList());
            this.timeLogList = new ArrayList<>();
            for( TimeLog timeLogEntry: taskDataTimeLogList){
                byte[] tlgXML = timeLogXmlFiles.get(timeLogEntry.getFilename() + ".XML");
                byte[] tlgBIN = timeLogBinFiles.get(timeLogEntry.getFilename() + ".BIN");
                if( (tlgXML!=null) && (tlgBIN!=null)){
                    this.timeLogList.add(new TimeLogFileData(this.taskFile,timeLogEntry,tlgXML, tlgBIN));

                }
            }




        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
    }
}

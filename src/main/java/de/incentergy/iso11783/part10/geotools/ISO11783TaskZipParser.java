package de.incentergy.iso11783.part10.geotools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;

import de.incentergy.iso11783.part10.v4.Grid;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import de.incentergy.iso11783.part10.v4.TimeLog;

public class ISO11783TaskZipParser {
    ISO11783TaskDataFile taskFile;
    TLGAdapter tlgAdapter;
    private static Logger log = Logger.getLogger(ISO11783TaskZipParser.class.getName());
    Map<String, byte[]> timeLogBinFiles = new HashMap<>();
    Map<String, byte[]> timeLogXmlFiles = new HashMap<>();
    Map<String, byte[]> gridBinFiles = new HashMap<>();

    Pattern TLG_BIN_PATTERN = Pattern.compile(".*TLG[0-9]+\\.BIN$");
    Pattern GRD_BIN_PATTERN = Pattern.compile(".*GRD[0-9]+\\.BIN$");
    Pattern TLG_XML_PATTERN = Pattern.compile(".*TLG[0-9]+\\.XML$");
    List<TimeLogFileData> timeLogList = new ArrayList<>();
    List<GridFileData> gridList = new ArrayList<>();

    public ISO11783TaskZipParser(URL url) {
        this.tlgAdapter = new TLGAdapter();
        try (ZipInputStream zipStream = new ZipInputStream(url.openStream())) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory() == false) {
                    String upperName = entry.getName().toUpperCase();
                    String fileName = Path.of(upperName).getFileName().toString();
                    ByteArrayOutputStream boas = new ByteArrayOutputStream();
                    zipStream.transferTo(boas);
                    if (upperName.endsWith("TASKDATA.XML")) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(boas.toByteArray());
                        this.taskFile = (ISO11783TaskDataFile) ISO11873DataStore.jaxbContext.createUnmarshaller()
                                .unmarshal(bais);
                    } else if(TLG_BIN_PATTERN.matcher(Path.of(upperName).getFileName().toString()).matches()) {
                        timeLogBinFiles.put(fileName, boas.toByteArray());
                    } else if(TLG_XML_PATTERN.matcher(upperName).matches()) {
                        timeLogXmlFiles.put(fileName, boas.toByteArray());
                    } else if(GRD_BIN_PATTERN.matcher(upperName).matches()) {
                        gridBinFiles.put(fileName, boas.toByteArray());
                    }
                }
                zipStream.closeEntry();
            }

            List<TimeLog> taskDataTimeLogList = this.taskFile.getTask().stream().flatMap((task)->task.getTimeLog().stream()).collect(Collectors.toList());
            this.tlgAdapter.setDeviceElementList(this.taskFile);
            for( TimeLog timeLogEntry: taskDataTimeLogList){
                byte[] tlgXML = timeLogXmlFiles.get(timeLogEntry.getFilename() + ".XML");
                byte[] tlgBIN = timeLogBinFiles.get(timeLogEntry.getFilename() + ".BIN");
                if( (tlgXML!=null) && (tlgBIN!=null)){
                    this.timeLogList.add(new TimeLogFileData(this.taskFile, this.tlgAdapter, timeLogEntry, tlgXML, tlgBIN));
                }
            }

            List<Grid> gridFileDataList = this.taskFile.getTask().stream().map((task)->task.getGrid()).filter(Objects::nonNull).collect(Collectors.toList());
            for( Grid gridEntry: gridFileDataList){
                byte[] gridBIN = gridBinFiles.get(gridEntry.getFilename() + ".BIN");
                if(gridBIN != null){
                    this.gridList.add(new GridFileData(gridEntry, gridBIN));
                }
            }

        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
    }
}

package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.Test;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class ISO11783TaskZipParserTest {

    @Test
    void testLoadZipFolderFromGeneratorWith100Elements(){
        URL url = getClass().getResource("/ISOXMLGenerator-100/Taskdata-100.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.getTaskFile());
        assertEquals(100, taskZipParser.getTimeLogList().get(0).getTimes().size());
    }

    @Test
    void testLoadZipFolderFromGeneratorWith100000Elements(){
        URL url = getClass().getResource("/ISOXMLGenerator-100000/Taskdata-100000.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.getTaskFile());
        assertEquals(100000, taskZipParser.getTimeLogList().get(0).getTimes().size());
    }

    @Test
    void testLoadZipFolderFromxFarmWith1Field(){
        URL url = getClass().getResource("/fmis/2021-03-01T14-43_58.030Z_prescription_taskdata.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.getTaskFile());
    }

    @Test
    void testLoadZipFolderFromxFarmWithMultipleFields(){
        URL url = getClass().getResource("/fmis/2021-03-01T14-45_10.160Z_prescription_taskdata.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.getTaskFile());
    }

    @Test
    void testLoadZipFolderWithMachineData(){
        URL url = getClass().getResource("/TLGData/machinedata_1.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        ISO11783TaskDataFile taskFile = taskZipParser.getTaskFile();
        assertNotNull(taskFile);
    }

    @Test
    void testLoadZipFolderWithExternalFiles(){
        URL url = getClass().getResource("/ExternalFileReferences/2021-04-09T15_33_26_taskdata.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        ISO11783TaskDataFile taskFile = taskZipParser.getTaskFile();
        assertNotNull(taskFile);
        assertEquals(15, taskFile.getPartfield().size());
    }
}

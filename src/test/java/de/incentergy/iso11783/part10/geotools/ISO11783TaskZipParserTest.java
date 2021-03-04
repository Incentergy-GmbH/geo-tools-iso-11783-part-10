package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import org.junit.jupiter.api.Test;

public class ISO11783TaskZipParserTest {

    @Test
    void testLoadZipFolderFromGeneratorWith100000Elements(){
        URL url = getClass().getResource("/ISOXMLGenerator-100/Taskdata-100.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.taskFile);
        assertEquals(100, taskZipParser.timeLogList.get(0).getTimes().size());
    }

    @Test
    void testLoadZipFolderFromxFarmWith1Field(){
        URL url = getClass().getResource("/fmis/2021-03-01T14-43_58.030Z_prescription_taskdata.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.taskFile);
    }

    @Test
    void testLoadZipFolderFromxFarmWithMultipleFields(){
        URL url = getClass().getResource("/fmis/2021-03-01T14-45_10.160Z_prescription_taskdata.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.taskFile);
    }

    @Test
    void testLoadZipFolderWithMachineData(){
        URL url = getClass().getResource("/TLGData/machinedata_1.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.taskFile);

    }

}

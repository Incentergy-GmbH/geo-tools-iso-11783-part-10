package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.junit.jupiter.api.Test;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class ISO11783TaskZipParserTest {
    private ISO11783TaskDataFile taskFile;
    private List<InputStream> logFiles;
    private List<InputStream> gridFiles;


    @Test
    void testLoadZipFolderFromGeneratorWith100000Elements(){
        URL url = getClass().getResource("/ISOXMLGenerator-100000/Taskdata-100000.zip");
        ISO11783TaskZipParser taskZipParser = new ISO11783TaskZipParser(url);
        assertNotNull(taskZipParser.taskFile);
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

}

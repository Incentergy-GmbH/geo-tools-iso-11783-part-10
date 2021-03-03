package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.ObjectUtils.Null;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class ISO11783TaskZipParser {
    private ISO11783TaskDataFile taskFile;
    private List<InputStream> logFiles;
    private List<InputStream> gridFiles;

    public ISO11783TaskZipParser(URL url) {
        try {
            ZipInputStream zipStream = new ZipInputStream(url.openStream());
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName()) {

                }
            }
            zipStream.getNextEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

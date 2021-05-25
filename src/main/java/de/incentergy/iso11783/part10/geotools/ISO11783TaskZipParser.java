package de.incentergy.iso11783.part10.geotools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.xml.bind.IDResolver;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.incentergy.iso11783.part10.v4.Device;
import de.incentergy.iso11783.part10.v4.ExternalFileContents;
import de.incentergy.iso11783.part10.v4.Grid;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import de.incentergy.iso11783.part10.v4.TimeLog;

public class ISO11783TaskZipParser {
    static interface InputStreamProvider {
        InputStream getInputStream(URL url) throws IOException;
    }
	private ISO11783TaskDataFile taskFile;

	private static Logger log = Logger.getLogger(ISO11783TaskZipParser.class.getName());

	private URL url;
	private InputStream inputStream;
	private boolean initialize = false;
    private InputStreamProvider streamProvider;
	Map<String, byte[]> timeLogBinFiles = new HashMap<>();
	Map<String, byte[]> timeLogXmlFiles = new HashMap<>();
	Map<String, byte[]> gridBinFiles = new HashMap<>();
	Map<String, byte[]> externalFiles = new HashMap<>();

    // order matters!
    final private List<String> EXTERNAL_FILES_PREFIXES =
        Arrays.asList("CCG", "CCT", "CLD", "CPC", "CTP", "CTR", "DVC", "FRM", "OTQ", "PDT", "PFD", "PGP", "WKR", "VPN", "TSK");
	final private Pattern TLG_BIN_PATTERN = Pattern.compile(".*TLG[0-9]+\\.BIN$");
	final private Pattern GRD_BIN_PATTERN = Pattern.compile(".*GRD[0-9]+\\.BIN$");
	final private Pattern TLG_XML_PATTERN = Pattern.compile(".*TLG[0-9]+\\.XML$");
	final private Pattern EXTERNAL_FILE_PATTERN = Pattern.compile(".*(" + String.join("|", EXTERNAL_FILES_PREFIXES) + ")[0-9]+\\.XML$");

	private List<TimeLogFileData> timeLogList = new ArrayList<>();

	private List<GridFileData> gridList = new ArrayList<>();

    private List<Device> deviceList = new ArrayList<>();

	public ISO11783TaskZipParser(URL url) {
		this.url = url;
        this.streamProvider = fileUrl -> fileUrl.openStream();
	}

	public ISO11783TaskZipParser(URL url, InputStreamProvider streamProvider) {
		this.url = url;
		this.streamProvider = streamProvider;
	}

    private void addExternalContent(ExternalFileContents content) {
        Arrays.stream(content.getClass().getMethods()).forEach(method -> {
            try {
                Method taskFileMethod = this.taskFile.getClass().getMethod(method.getName());

                List<Object> newObjects = (List<Object>) method.invoke(content);
                List<Object> taskFileObjects = (List<Object>) taskFileMethod.invoke(this.taskFile);

                taskFileObjects.addAll(newObjects);
            } catch (Exception e) {

            }
        });
    }

	private void parse() {
		try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
            MultipleFilesIDResolver resolver = new MultipleFilesIDResolver();
			ZipEntry entry;
			while ((entry = zipStream.getNextEntry()) != null) {
				if (entry.isDirectory() == false) {
					String upperName = entry.getName().toUpperCase();
					String fileName = Path.of(upperName).getFileName().toString();
					ByteArrayOutputStream boas = new ByteArrayOutputStream();
					zipStream.transferTo(boas);
					if (upperName.endsWith("TASKDATA.XML")) {
						ByteArrayInputStream bais = new ByteArrayInputStream(boas.toByteArray());
                        final Unmarshaller unmarshaller = ISO11783DataStore.jaxbContextMain.createUnmarshaller();
                        unmarshaller.setProperty(IDResolver.class.getName(), resolver);
						this.taskFile = (ISO11783TaskDataFile) unmarshaller.unmarshal(bais);
					} else if (TLG_BIN_PATTERN.matcher(Path.of(upperName).getFileName().toString()).matches()) {
						timeLogBinFiles.put(fileName, boas.toByteArray());
					} else if (TLG_XML_PATTERN.matcher(upperName).matches()) {
						timeLogXmlFiles.put(fileName, boas.toByteArray());
					} else if (GRD_BIN_PATTERN.matcher(upperName).matches()) {
						gridBinFiles.put(fileName, boas.toByteArray());
					} else if (EXTERNAL_FILE_PATTERN.matcher(upperName).matches()) {
						externalFiles.put(fileName, boas.toByteArray());
					}
				}
				zipStream.closeEntry();
			}

            EXTERNAL_FILES_PREFIXES.stream().forEach((String prefix) -> {
                this.taskFile.getExternalFileReference().stream()
                    .filter(xfr -> xfr.getFilename().startsWith(prefix))
                    .forEach(xfr -> {
                        try {
                            Unmarshaller unmarshaller = ISO11783DataStore.jaxbContextExternal.createUnmarshaller();
                            unmarshaller.setProperty(IDResolver.class.getName(), resolver);
                            ExternalFileContents contents = (ExternalFileContents) unmarshaller.unmarshal(
                                new ByteArrayInputStream(externalFiles.get(xfr.getFilename() + ".XML"))
                            );
                            addExternalContent(contents);
                        } catch(JAXBException e) {
                            e.printStackTrace();
                        }
                        // log.log(Level.WARNING, xfr.getFilename());
                    });
            });

			List<TimeLog> taskDataTimeLogList = this.taskFile.getTask().stream()
					.flatMap((task) -> task.getTimeLog().stream()).collect(Collectors.toList());
			for (TimeLog timeLogEntry : taskDataTimeLogList) {
				byte[] tlgXML = timeLogXmlFiles.get(timeLogEntry.getFilename() + ".XML");
				byte[] tlgBIN = timeLogBinFiles.get(timeLogEntry.getFilename() + ".BIN");
				if ((tlgXML != null) && (tlgBIN != null)) {
					this.timeLogList.add(new TimeLogFileData(this.taskFile, timeLogEntry, tlgXML, tlgBIN));
				}
			}

			List<Grid> gridFileDataList = this.taskFile.getTask().stream().map((task) -> task.getGrid())
					.filter(Objects::nonNull).collect(Collectors.toList());
			for (Grid gridEntry : gridFileDataList) {
				byte[] gridBIN = gridBinFiles.get(gridEntry.getFilename() + ".BIN");
				if (gridBIN != null) {
					this.gridList.add(new GridFileData(gridEntry, gridBIN));
				}
			}

            this.deviceList = this.taskFile.getDevice();

		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}
	}
	public Map<String, byte[]> getTimeLogXmlFiles() {
		initIfNecessary();
		return timeLogXmlFiles;
	}
	public List<TimeLogFileData> getTimeLogList() {
		initIfNecessary();
		return timeLogList;
	}
	public ISO11783TaskDataFile getTaskFile() {
		initIfNecessary();
		return taskFile;
	}
	public List<GridFileData> getGridList() {
		initIfNecessary();
		return gridList;
	}

    public List<Device> getDeviceList() {
		initIfNecessary();
        return deviceList;
    }

    public URL getURL() {
        return url;
    }

	private void initIfNecessary() {
		if (!initialize) {
			try {
				if (inputStream == null) {
					inputStream = this.streamProvider.getInputStream(url);
				}
				parse();
				initialize = true;
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read data from url or input stream: " + url, e);
			}
		}
		
	}

}

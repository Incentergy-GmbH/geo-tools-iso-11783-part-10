package de.incentergy.iso11783.part10.geotools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
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

import javax.xml.bind.JAXBException;

import de.incentergy.iso11783.part10.v4.Grid;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import de.incentergy.iso11783.part10.v4.TimeLog;

public class ISO11783TaskZipParser {
	private ISO11783TaskDataFile taskFile;

	private static Logger log = Logger.getLogger(ISO11783TaskZipParser.class.getName());

	private URL url;
	private InputStream inputStream;
	private boolean initialize = false;
	Map<String, byte[]> timeLogBinFiles = new HashMap<>();
	Map<String, byte[]> timeLogXmlFiles = new HashMap<>();
	Map<String, byte[]> gridBinFiles = new HashMap<>();

	Pattern TLG_BIN_PATTERN = Pattern.compile(".*TLG[0-9]+\\.BIN$");
	Pattern GRD_BIN_PATTERN = Pattern.compile(".*GRD[0-9]+\\.BIN$");
	Pattern TLG_XML_PATTERN = Pattern.compile(".*TLG[0-9]+\\.XML$");
	private List<TimeLogFileData> timeLogList = new ArrayList<>();

	private List<GridFileData> gridList = new ArrayList<>();

	public ISO11783TaskZipParser(URL url) {
		this.url = url;
	}

	public ISO11783TaskZipParser(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	private void parse(InputStream inputStream) {
		try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
			ZipEntry entry;
			while ((entry = zipStream.getNextEntry()) != null) {
				if (entry.isDirectory() == false) {
					String upperName = entry.getName().toUpperCase();
					String fileName = Path.of(upperName).getFileName().toString();
					ByteArrayOutputStream boas = new ByteArrayOutputStream();
					zipStream.transferTo(boas);
					if (upperName.endsWith("TASKDATA.XML")) {
						ByteArrayInputStream bais = new ByteArrayInputStream(boas.toByteArray());
						this.taskFile = (ISO11783TaskDataFile) ISO11783DataStore.jaxbContext.createUnmarshaller()
								.unmarshal(bais);
					} else if (TLG_BIN_PATTERN.matcher(Path.of(upperName).getFileName().toString()).matches()) {
						timeLogBinFiles.put(fileName, boas.toByteArray());
					} else if (TLG_XML_PATTERN.matcher(upperName).matches()) {
						timeLogXmlFiles.put(fileName, boas.toByteArray());
					} else if (GRD_BIN_PATTERN.matcher(upperName).matches()) {
						gridBinFiles.put(fileName, boas.toByteArray());
					}
				}
				zipStream.closeEntry();
			}

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
	private void initIfNecessary() {
		if (!initialize) {
			try {
				if (inputStream == null) {
					inputStream = url.openStream();
				}
				parse(inputStream);
				initialize = true;
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read data from url or input stream: " + url, e);
			}
		}
		
	}

}

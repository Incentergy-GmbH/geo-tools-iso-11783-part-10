package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;

public class ISO11783DataStore extends ContentDataStore {

	private Map<String, ISO11783TaskZipParser> files = new ConcurrentHashMap<>();

	Pattern EXTRACT_FILENAME = Pattern.compile("[^_]+_(.*)$");

	static JAXBContext jaxbContext;

	static {
		try {
			jaxbContext = JAXBContext.newInstance(ISO11783TaskDataFile.class);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected List<Name> createTypeNames() throws IOException {
		return files.keySet().stream()
				.flatMap(filename -> Stream.of(new NameImpl(getNamespaceURI(), "Partfield_" + filename.toString()),
						new NameImpl(getNamespaceURI(), "TimeLog_" + filename.toString()),
						new NameImpl(getNamespaceURI(), "Grid_" + filename.toString()),
						new NameImpl(getNamespaceURI(), "GuidancePattern_" + filename.toString())))
				.collect(Collectors.toList());
	}

	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		return new ISO11783FeatureSource(getZipParser(entry.getName()), entry, Query.ALL);
	}

	public ISO11783TaskZipParser getZipParser(Name name) {
		Matcher matcher = EXTRACT_FILENAME.matcher(name.getLocalPart());
		if (matcher.matches()) {
			return files.get(matcher.group(1));
		}
		return null;
	}

	public void updateFilesFromURL(URL url) {
		updateFilesFromURL(url, null, null, null);
	}

	public void updateFilesFromURL(URL url, String bearerToken, String username, String password) {
		if (url.getProtocol().equals("file")) {
			FileStorage.processFileUrl(url, files);
		} else if (url.getProtocol().equals("http") || url.getProtocol().equals("https")) {
			WebDAVStorage.processFileUrl(url, files, bearerToken, username, password);
		}
	}
}
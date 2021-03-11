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

import java.util.logging.Logger;

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
	
	private static Logger log = Logger.getLogger(ISO11783DataStore.class.getName());

	private Map<String, ISO11783TaskZipParser> files = new ConcurrentHashMap<>();

	private URL url;
	private String bearerToken;
	private String username;
	private String password;

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
		updateFiles();
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
	
	public void updateFiles() {
		updateFilesFromURL(null, null, null, null);
	}

	public void updateFilesFromURL(URL url) {
		updateFilesFromURL(url, null, null, null);
	}

	public void updateFilesFromURL(URL url, String bearerToken, String username, String password) {
		if(url == null && this.url == null) {
			log.warning("Url not set");
			return;
		}
		if(url != null) {			
			this.url = url;
		}
		if (bearerToken != null) {
			this.bearerToken = bearerToken;
		}
		if (username != null) {
			this.username = username;
		}
		if (password != null) {
			this.password = password;
		}
		if (this.url.getProtocol().equals("file")) {
			FileStorage.processFileUrl(this.url, files);
		} else if (url.getProtocol().equals("http") || url.getProtocol().equals("https")) {
			WebDAVStorage.processFileUrl(this.url, files, this.bearerToken, this.username, this.password);
		}
	}
}
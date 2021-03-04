package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class ISO11873DataStore extends ContentDataStore {
	
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
		return files.keySet().stream().flatMap(filename -> Stream.of(
            new NameImpl("Partfield_" + filename.toString()),
            new NameImpl("TimeLog_" + filename.toString()),
			new NameImpl("Grid_" + filename.toString()),
			new NameImpl("GuidancePattern_" + filename.toString())
        )).collect(Collectors.toList());
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

	public void updateFilesFromURL(URL url){
		if (url.getProtocol().equals("file")) {
			try {
				Path path = Paths.get(url.toURI());
				Files.list(path)
                    .filter(consumer -> {
						return  consumer.toString().toLowerCase().endsWith(".zip");
					})
                    .forEach((consumer) -> {
                        try {
                            files.put(consumer.toString(),
                                    new ISO11783TaskZipParser(consumer.toUri().toURL()));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    });
			} catch (URISyntaxException|IOException e) {
				e.printStackTrace();
			}			
		}
	}
}
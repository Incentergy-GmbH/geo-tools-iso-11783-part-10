package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
	
	private Map<URL, ISO11783TaskZipParser> files = new ConcurrentHashMap<>();

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
		return files.keySet().stream().flatMap(url -> Stream.of(
            new NameImpl(url.toString(), "Partfield"),
            new NameImpl(url.toString(), "TimeLog"),
			new NameImpl(url.toString(), "GuidancePattern")
        )).collect(Collectors.toList());
	}

	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		return new ISO11783FeatureSource(files.get(new URL(entry.getName().getLocalPart())), entry, Query.ALL);
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
                            files.put(consumer.toUri().toURL(),
                                    new ISO11783TaskZipParser(consumer.toFile().toURL()));
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
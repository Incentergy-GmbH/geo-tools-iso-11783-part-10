package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileStorage {
	
	private static Logger log = Logger.getLogger(FileStorage.class.getName());

	static void processFileUrl(URL url, Map<String, ISO11783TaskZipParser> files) {
		try {
			Path path = Paths.get(url.toURI());
			Files.list(path)
		        .filter(consumer -> {
					return  consumer.toString().toLowerCase().endsWith(".zip");
				})
		        .forEach((consumer) -> {
		            try {
		                files.put(consumer.getFileName().toString().replaceAll("-", ""),
		                        new ISO11783TaskZipParser(consumer.toUri().toURL()));
		            } catch (MalformedURLException e) {
		                e.printStackTrace();
		            }
		        });
		} catch (URISyntaxException|IOException e) {
			log.log(Level.SEVERE, "Could not process file", e);
		}
	}

}

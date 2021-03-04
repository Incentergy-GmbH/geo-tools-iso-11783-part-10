package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;

public class WebDAVStorage {

	private static Logger log = Logger.getLogger(WebDAVStorage.class.getName());

	public static void processFileUrl(URL url, Map<String, ISO11783TaskZipParser> files, String bearerToken,
			String username, String password) {

		Sardine sardine;
		if (bearerToken != null) {
			sardine = new SardineImpl(bearerToken);
		} else if (username != null || password != null) {
			sardine = SardineFactory.begin(username, password);
		} else {
			sardine = SardineFactory.begin();
		}
		processUrl(url, files, sardine);

	}

	static void processUrl(URL url, Map<String, ISO11783TaskZipParser> files, Sardine sardine) {
		List<DavResource> resources;
		try {
			resources = sardine.list(url.toString());
			for (DavResource res : resources) {
				// Skip the entry if it is the entry for the url itself
				if(url.getPath().equals(res.getPath())) {
					continue;
				}
				URL resourceUrl = new URL(url.toString() + res.getName() + "/");
				if (res.isDirectory()) {
					processUrl(resourceUrl, files, sardine);
				} else if (res.getName().toLowerCase().endsWith(".zip")) {
					files.put(res.getName(),
							new ISO11783TaskZipParser(sardine.get(url.toString() + res.getName())));
				}
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not download file "+url, e);
		}
	}

}

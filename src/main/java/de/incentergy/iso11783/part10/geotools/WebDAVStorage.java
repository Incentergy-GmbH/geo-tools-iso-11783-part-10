package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;

class WebDAVStreamProvider implements ISO11783TaskZipParser.InputStreamProvider {
    private Sardine sardine;
    WebDAVStreamProvider(Sardine sardine) {
        this.sardine = sardine;
    }

    @Override
    public InputStream getInputStream(URL url) throws IOException {
        return this.sardine.get(url.toString());
    }
}

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

        try {
            URL normalizedURL = url.toString().endsWith("/") ? url : new URL(url.toString() + "/");
            processUrl(normalizedURL, files, sardine);
        } catch(MalformedURLException e)  {

        }

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
					String mapKey = res.getName().replaceAll("-", "").replaceAll(".zip", "");
					if(!files.containsKey(mapKey)) {						
                        URL fileURL = new URL(url.toString() + res.getName());
						files.put(mapKey, new ISO11783TaskZipParser(fileURL, new WebDAVStreamProvider(sardine)));
					}
				}
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not download file "+url, e);
		}
	}

}

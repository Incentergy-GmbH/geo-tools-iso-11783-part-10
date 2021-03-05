package de.incentergy.iso11783.part10.geotools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class WebDAVStorageTest {

	// This is an integration test that should not be executed during normal processing
	@Test
	@Disabled
	void testProcessFileUrl() throws MalformedURLException {

		Map<String, ISO11783TaskZipParser> files = new HashMap<>();
		WebDAVStorage.processFileUrl(
				new URL("http://ec2-18-184-13-246.eu-central-1.compute.amazonaws.com:8080/webdav/undefined/"), files, null, null,
				null);
		assertEquals(4, files.entrySet().size());
	}

}

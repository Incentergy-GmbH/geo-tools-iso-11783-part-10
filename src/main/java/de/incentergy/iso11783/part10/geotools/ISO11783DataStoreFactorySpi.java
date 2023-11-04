package de.incentergy.iso11783.part10.geotools;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFactorySpi;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.util.KVP;
import org.locationtech.jts.geom.GeometryFactory;

public class ISO11783DataStoreFactorySpi implements DataStoreFactorySpi {

	private static Logger log = Logger.getLogger(ISO11783DataStoreFactorySpi.class.getName());

	/** Optional - uri of the FeatureType's namespace */
	public static final Param NAMESPACEP = new Param("namespace", URI.class, "uri to a the namespace", false, null, // not
			// required
			new KVP(Param.LEVEL, "advanced"));

	public static final Param URLP = new Param("isoxmlUrl", URL.class, "URL of files", true, null, // not
			// required
			new KVP(Param.LEVEL, "advanced"));

	public static final Param AUTHORIZATION_USERNAMEP = new Param("authorization_header_username", String.class,
			"Http Basic Auth Username", false, null, // not
			// required
			new KVP(Param.LEVEL, "advanced"));
	public static final Param AUTHORIZATION_PASSWORDP = new Param("authorization_header_password", String.class,
			"Http Basic Auth Password", false, null, // not
			// required
			new KVP(Param.LEVEL, "advanced"));
	
	public static final Param AUTHORIZATION_BEARER_TOKENP = new Param("authorization_header_bearer", String.class,
			"Authorization bearer token without 'Bearer' prefix e.g. 'eyv...', overwrite username and password", false, null, // not
			// required
			new KVP(Param.LEVEL, "advanced"));
	
	private static WeakHashMap<Map<String, ?>,ISO11783DataStore> dataStoreCache = new WeakHashMap<>();

	@Override
	public String getDisplayName() {
		return "ISOXML in Memory";
	}

	@Override
	public String getDescription() {
		return "This datastore loads an ISOXML into memory and converts ISOXML structures on the fly to JTS features.";
	}

	@Override
	public Param[] getParametersInfo() {
		return new Param[] { NAMESPACEP, URLP, AUTHORIZATION_BEARER_TOKENP, AUTHORIZATION_USERNAMEP, AUTHORIZATION_PASSWORDP};
	}

	@Override
	public boolean canProcess(Map<String, ?> params) {
		// params must not contain ISOXML
		return params.containsKey("isoxmlUrl");
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	/** No implementation hints required at this time */
	public Map<Key, ?> getImplementationHints() {
		return Collections.emptyMap();
	}

	@Override
	public DataStore createDataStore(Map<String, ?> params) throws IOException {
		if(dataStoreCache.containsKey(params)) {
			return dataStoreCache.get(params);
		} else {
			ISO11783DataStore store = new ISO11783DataStore();
			URI namespace = lookup(NAMESPACEP, params, URI.class);
			if (namespace != null) {
				store.setNamespaceURI(namespace.toString());
			}
	
			URL rootPath = lookup(URLP, params, URL.class);
			String bearerToken = lookup(AUTHORIZATION_BEARER_TOKENP, params, String.class);
			String username = lookup(AUTHORIZATION_USERNAMEP, params, String.class);
			String password = lookup(AUTHORIZATION_PASSWORDP, params, String.class);
	
			if (rootPath != null) {
				log.info("Creating ISOXML Datastore from "+rootPath.toString());
				store.updateFilesFromURL(rootPath, bearerToken, username, password);
			}
	
			store.setDataStoreFactory(this);
			store.setGeometryFactory(new GeometryFactory());
			store.setFeatureTypeFactory(new FeatureTypeFactoryImpl());
			store.setFeatureFactory(CommonFactoryFinder.getFeatureFactory(null));
			dataStoreCache.put(params, store);
			return store;
		}
	}

	/**
	 * Looks up a parameter, if not found it returns the default value, assuming
	 * there is one, or null otherwise
	 *
	 * @param <T>
	 * @param param
	 * @param params
	 * @param target
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	<T> T lookup(Param param, Map<String, ?> params, Class<T> target) throws IOException {
		T result = (T) param.lookUp(params);
		if (result == null) {
			return (T) param.getDefaultValue();
		} else {
			return result;
		}
	}

	@Override
	public DataStore createNewDataStore(Map<String, ?> params) throws IOException {
		throw new UnsupportedOperationException("ISOXML Datastore is read only");
	}
}

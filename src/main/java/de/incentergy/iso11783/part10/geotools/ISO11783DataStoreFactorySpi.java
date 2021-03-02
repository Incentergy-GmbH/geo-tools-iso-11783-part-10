package de.incentergy.iso11783.part10.geotools;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.util.KVP;
import org.locationtech.jts.geom.GeometryFactory;

public class ISO11783DataStoreFactorySpi implements DataStoreFactorySpi {

    private static Logger log = Logger.getLogger(ISO11783DataStoreFactorySpi.class.getName());

    /** Optional - uri of the FeatureType's namespace */
    public static final Param NAMESPACEP =
            new Param(
                    "namespace",
                    URI.class,
                    "uri to a the namespace",
                    false,
                    null, // not
                    // required
                    new KVP(Param.LEVEL, "advanced"));

    public static final Param URLP =
            new Param(
                    "isoxmlUrl",
                    URL.class,
                    "URL of files",
                    true,
                    null, // not
                    // required
                    new KVP(Param.LEVEL, "advanced"));

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
        return new Param[] {NAMESPACEP, URLP};
    }

    @Override
    public boolean canProcess(Map<String, Serializable> params) {
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
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        log.info("Creating ISOXML Datastore from {0}");
        ISO11873DataStore store = new ISO11873DataStore();
        URI namespace = lookup(NAMESPACEP, params, URI.class);
        if (namespace != null) {
             store.setNamespaceURI(namespace.toString());
        }

        URL rootPath = lookup(URLP, params, URL.class);

        if( rootPath != null){
            store.updateFilesFromURL(rootPath);
        }
        
        store.setDataStoreFactory(this);
        store.setGeometryFactory(new GeometryFactory());
        store.setFeatureTypeFactory(new FeatureTypeFactoryImpl());
        store.setFeatureFactory(CommonFactoryFinder.getFeatureFactory(null));
        return store;
    }

    /**
     * Looks up a parameter, if not found it returns the default value, assuming there is one, or
     * null otherwise
     *
     * @param        <T>
     * @param param
     * @param params
     * @param target
     * @return
     * @throws IOException
     */
    <T> T lookup(Param param, Map<String, Serializable> params, Class<T> target)
            throws IOException {
        T result = (T) param.lookUp(params);
        if (result == null) {
            return (T) param.getDefaultValue();
        } else {
            return result;
        }
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        throw new UnsupportedOperationException("ISOXML Datastore is read only");
    }
}


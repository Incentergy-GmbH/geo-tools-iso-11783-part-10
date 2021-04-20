package de.incentergy.iso11783.part10.geotools;

import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.xml.bind.ValidationEventHandler;

import com.sun.xml.bind.IDResolver;

import org.xml.sax.SAXException;

// The only difference with the DefaultIDResolver is that we don't clear the hash for a new document
// It allows us to keep Object references between multiple documents (such as ExternalFiles or TimeLog XMLs)
final public class MultipleFilesIDResolver extends IDResolver {
    /** Records ID->Object map. */
    private HashMap<String,Object> idmap = null;

    @Override
    public void startDocument(ValidationEventHandler eventHandler) throws SAXException {
        // if(idmap!=null)
        //     idmap.clear();
    }

    @Override
    public void bind(String id, Object obj) {
        if(idmap==null)     idmap = new HashMap<String,Object>();
        idmap.put(id,obj);
    }

    @Override
    public Callable resolve(final String id, Class targetType) {
        return new Callable() {
            public Object call() throws Exception {
                if(idmap==null)     return null;
                return idmap.get(id);
            }
        };
    }
}   
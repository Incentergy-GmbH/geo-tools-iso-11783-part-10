package de.incentergy.iso11783.part10.geotools;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class GeometryAdapter extends XmlAdapter<String, Geometry>  {
    private WKTReader wktReader = new WKTReader(new GeometryFactory());
    private WKTWriter wktWriter = new WKTWriter();

    @Override
    public Geometry unmarshal(String wellKnownText) throws Exception {
            return wktReader.read(wellKnownText);
    }

    @Override
    public String marshal(Geometry geometry) throws Exception {
            return wktWriter.write(geometry);
    }
}

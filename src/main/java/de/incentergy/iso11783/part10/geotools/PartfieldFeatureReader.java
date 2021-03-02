package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

public class PartfieldFeatureReader extends AbstractFeatureReader{
	
	public static final String TYPE_NAME_STRING = "Partfield";
	public static final Name TYPE_NAME = new NameImpl(PartfieldFeatureReader.class.getPackageName(), TYPE_NAME_STRING);


    protected SimpleFeatureType buildFeatureType() throws IOException {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(TYPE_NAME_STRING);

        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
        builder.add("polygonNonTreatmentZoneOnly", MultiPolygon.class);
        builder.add("partfieldId", String.class);
        builder.add("partfieldCode", String.class);
        builder.add("partfieldDesignator", String.class);
        builder.add("partfieldArea", Long.class);
        builder.add("customerIdRef", String.class);
        builder.add("partfieldId", String.class);
        builder.add("farmIdRef", String.class);
        builder.add("cropTypeIdRef", String.class);
        builder.add("cropVarietyIdRef", String.class);
        builder.add("fieldIdRef", String.class);

        final SimpleFeatureType SCHEMA = builder.buildFeatureType();
        return SCHEMA;
    }

	@Override
	public FeatureType getFeatureType() {
		return state.getFeatureType();
	}

	@Override
	public Feature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		return null;
	}

	@Override
	public boolean hasNext() throws IOException {
		return false;
	}

	@Override
	public void close() throws IOException {
		
	}

}

package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.geotools.data.store.ContentState;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import de.incentergy.iso11783.part10.v4.CropType;
import de.incentergy.iso11783.part10.v4.CropVariety;
import de.incentergy.iso11783.part10.v4.Customer;
import de.incentergy.iso11783.part10.v4.Farm;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import de.incentergy.iso11783.part10.v4.LineString;
import de.incentergy.iso11783.part10.v4.LineStringType;
import de.incentergy.iso11783.part10.v4.Partfield;
import de.incentergy.iso11783.part10.v4.Polygon;

public class PartfieldFeatureReader extends AbstractFeatureReader{
	
	public static final String TYPE_NAME_STRING = "Partfield";
	public static final Name TYPE_NAME = new NameImpl(PartfieldFeatureReader.class.getPackageName(), TYPE_NAME_STRING);
	private ISO11783TaskDataFile taskDataFile;
    /** Utility class used to build features */
    protected SimpleFeatureBuilder builder;

    /** Factory class for geometry creation */
    private GeometryFactory geometryFactory;

    public Coordinate[] coordinates(LineString ls) {
        return ls.getPoint().stream().map(
            point -> new Coordinate(
                Double.valueOf(point.getPointEast()),
                Double.valueOf(point.getPointNorth())
            )).toArray(Coordinate[]::new);
    }


	/**
	 * Copies the first point to the last if it is not the same.
	 *
	 * @param coordinates list of coordinates
	 */
	private static Coordinate[] closeRing(Coordinate[] coordinates) {
		if (!coordinates[0].equals2D(coordinates[coordinates.length-1])) {
				coordinates = Arrays.copyOf(coordinates, coordinates.length+1);
				coordinates[coordinates.length-1 ] = coordinates[0];
				return coordinates;
		}
		return coordinates;
	}

	public org.locationtech.jts.geom.Polygon mapPolygon(Polygon isoxmlPolygon){
		Optional<LineString> isoxmlOuterRing = isoxmlPolygon.getLineString().stream()
            .filter((ls)-> LineStringType.POLYGONEXTERIOR.equals((ls.getLineStringType()))).findAny();
		if( !isoxmlOuterRing.isPresent()){
			return  null;
		}

		List<LineString> isoxmlInnerRings = isoxmlPolygon.getLineString().stream()
            .filter((ls)-> LineStringType.POLYGONINTERIOR.equals(ls.getLineStringType())).collect(Collectors.toList());

        org.locationtech.jts.geom.LinearRing outerRing = geometryFactory.createLinearRing(closeRing(coordinates(isoxmlOuterRing.get())));

        org.locationtech.jts.geom.LinearRing[] innerRings = isoxmlInnerRings.stream()
            .map(ls -> geometryFactory.createLinearRing(closeRing(coordinates(ls)))).toArray(org.locationtech.jts.geom.LinearRing[]::new);

		org.locationtech.jts.geom.Polygon jtsPolygon = geometryFactory.createPolygon(outerRing,innerRings);
		

		return jtsPolygon;
	}

	public SimpleFeature convertPartField2SimpleFeature(Partfield partfield){

		builder.set("partfieldId", partfield.getPartfieldId());
        builder.set("partfieldCode", partfield.getPartfieldCode());
        builder.set("partfieldDesignator", partfield.getPartfieldDesignator());
        builder.set("partfieldArea", partfield.getPartfieldArea());
		if(partfield.getCustomerIdRef() instanceof Customer){
        	builder.set("customerIdRef", ((Customer)partfield.getCustomerIdRef()).getCustomerId());
		}
        if(partfield.getFarmIdRef() instanceof Farm) {
			builder.set("farmIdRef", ((Farm)partfield.getFarmIdRef()).getFarmId() );
		}
		if(partfield.getCropTypeIdRef() instanceof CropType){
        	builder.set("cropTypeIdRef", ((CropType)partfield.getCropTypeIdRef()).getCropTypeId());
		}
		if(partfield.getCropVarietyIdRef() instanceof CropVariety){
     	   builder.set("cropVarietyIdRef", ((CropVariety)partfield.getCropVarietyIdRef()).getCropVarietyId());
		}
		if(partfield.getFieldIdRef() instanceof Partfield){
			builder.set("fieldIdRef", ((Partfield)partfield.getFieldIdRef()).getPartfieldId());
		}

		org.locationtech.jts.geom.Polygon[] polygons = partfield.getPolygonNonTreatmentZoneOnly().stream().map((Polygon isoxmlPolygon)->{
			return mapPolygon(isoxmlPolygon);
		}).toArray(org.locationtech.jts.geom.Polygon[]::new);
		MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygons);

		builder.set("polygonNonTreatmentZoneOnly", multiPolygon);
	

		return builder.buildFeature(partfield.getPartfieldId());
	}


	public PartfieldFeatureReader(ISO11783TaskDataFile taskDataFile, ContentState contentState){
		this.taskDataFile = taskDataFile;
		this.state = contentState;


		builder = new SimpleFeatureBuilder(state.getFeatureType());
		geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

		for(Partfield partfield: this.taskDataFile.getPartfield()){

		};


	}


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
        builder.add("farmIdRef", String.class);
        builder.add("cropTypeIdRef", String.class);
        builder.add("cropVarietyIdRef", String.class);
        builder.add("fieldIdRef", String.class);

        final SimpleFeatureType SCHEMA = builder.buildFeatureType();
        return SCHEMA;
    }

	@Override
	public SimpleFeatureType getFeatureType() {
		return state.getFeatureType();
	}

	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
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

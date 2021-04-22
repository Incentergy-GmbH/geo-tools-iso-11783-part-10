package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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

public class PartfieldFeatureReader extends AbstractFeatureReader {

	public static final String TYPE_NAME_STRING = "Partfield";
	public static SimpleFeatureType FEATURE_TYPE;
	private ISO11783TaskDataFile taskDataFile;
	/** Utility class used to build features */
	protected SimpleFeatureBuilder builder;

	protected int index = 0;

	/** Factory class for geometry creation */
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	/**
	 * Copies the first point to the last if it is not the same.
	 *
	 * @param coordinates list of coordinates
	 */
	private static Coordinate[] closeRing(Coordinate[] coordinates) {
		if (!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
			coordinates = Arrays.copyOf(coordinates, coordinates.length + 1);
			coordinates[coordinates.length - 1] = coordinates[0];
			return coordinates;
		}
		return coordinates;
	}

	PartfieldFeatureReader(ISO11783TaskDataFile taskDataFile, Name entryName) {
		this.taskDataFile = taskDataFile;

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(entryName);
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
        SimpleFeatureType featureType = builder.buildFeatureType();

		this.builder = new SimpleFeatureBuilder(featureType);
	}

	@Override
	public void close() throws IOException {
        index = 0; //FIXME
	}

	public SimpleFeature convertPartField2SimpleFeature(Partfield partfield) {

		builder.set("partfieldId", partfield.getPartfieldId());
		builder.set("partfieldCode", partfield.getPartfieldCode());
		builder.set("partfieldDesignator", partfield.getPartfieldDesignator());
		builder.set("partfieldArea", partfield.getPartfieldArea());
		if (partfield.getCustomerIdRef() instanceof Customer) {
			builder.set("customerIdRef", ((Customer) partfield.getCustomerIdRef()).getCustomerId());
		}
		if (partfield.getFarmIdRef() instanceof Farm) {
			builder.set("farmIdRef", ((Farm) partfield.getFarmIdRef()).getFarmId());
		}
		if (partfield.getCropTypeIdRef() instanceof CropType) {
			builder.set("cropTypeIdRef", ((CropType) partfield.getCropTypeIdRef()).getCropTypeId());
		}
		if (partfield.getCropVarietyIdRef() instanceof CropVariety) {
			builder.set("cropVarietyIdRef", ((CropVariety) partfield.getCropVarietyIdRef()).getCropVarietyId());
		}
		if (partfield.getFieldIdRef() instanceof Partfield) {
			builder.set("fieldIdRef", ((Partfield) partfield.getFieldIdRef()).getPartfieldId());
		}

		MultiPolygon multiPolygon = mapPolygons(partfield.getPolygonNonTreatmentZoneOnly());

        if (multiPolygon != null) {
            builder.set("polygonNonTreatmentZoneOnly", multiPolygon);
        }

		return builder.buildFeature(partfield.getPartfieldId());
	}

	public Coordinate[] coordinates(LineString ls) {
		return ls.getPoint().stream().map(
				point -> new Coordinate(Double.valueOf(point.getPointEast()), Double.valueOf(point.getPointNorth())))
				.toArray(Coordinate[]::new);
	}

	@Override
	public SimpleFeatureType getFeatureType() {
		return builder.getFeatureType();
	}

	@Override
	public boolean hasNext() throws IOException {
		return index < taskDataFile.getPartfield().size();
	}

    // Take the outer rings from all the ISOXML Polygons and associate with each of them inner rings
    // (regardless the original ISOXML Polygon). Convert each outer ring and corresponding inner rings
    // to Polygons and return the Multipolygon constructed from all the Polygons
    // 
    // This algorithm works for both v3 and v4 of ISOXML standard. Moreover, it works for some cases of
    // invalid geometry, such as ISOXML Polygons without outer rings
	public MultiPolygon mapPolygons(List<Polygon> isoxmlPolygons) {

		List<LineString> isoxmlOuterRings = isoxmlPolygons.stream().flatMap(
            polygon -> polygon.getLineString().stream()
                .filter(ls -> LineStringType.POLYGONEXTERIOR.equals(ls.getLineStringType()))
        ).collect(Collectors.toList());
            
		if (isoxmlOuterRings.size() == 0) {
			return null;
		}

		List<LineString> isoxmlInnerRings = isoxmlPolygons.stream().flatMap(
            polygon -> polygon.getLineString().stream()
                .filter(ls -> LineStringType.POLYGONINTERIOR.equals(ls.getLineStringType()))
        ).collect(Collectors.toList());

		List<org.locationtech.jts.geom.LinearRing> outerRings = isoxmlOuterRings.stream()
            .map(ls -> geometryFactory.createLinearRing(closeRing(coordinates(ls))))
            .collect(Collectors.toList());

		List<org.locationtech.jts.geom.LinearRing> innerRings = isoxmlInnerRings.stream()
            .map(ls -> geometryFactory.createLinearRing(closeRing(coordinates(ls))))
            .collect(Collectors.toList());

        // performance optimization
        if (outerRings.size() == 1) {
            org.locationtech.jts.geom.Polygon jtsPolygon = geometryFactory.createPolygon(
                outerRings.get(0),
                innerRings.toArray(org.locationtech.jts.geom.LinearRing[]::new)
            );
            return geometryFactory.createMultiPolygon(new org.locationtech.jts.geom.Polygon[] { jtsPolygon });
        }

        List<Integer> outerRingIdx = innerRings.stream().map(innerRing -> {
            org.locationtech.jts.geom.Polygon innerPolygon = geometryFactory.createPolygon(innerRing);

            double biggestArea = 0;
            Integer bestIdx = -1;
            for (int i = 0; i < outerRings.size(); i++) {
                org.locationtech.jts.geom.Polygon outerPolygon = geometryFactory.createPolygon(outerRings.get(i));
                double area = outerPolygon.intersection(innerPolygon).getArea();

                if (area > biggestArea) {
                    biggestArea = area;
                    bestIdx = i;
                }
            }
            return bestIdx;
        }).collect(Collectors.toList());

        org.locationtech.jts.geom.Polygon[] jtsPolygons = new org.locationtech.jts.geom.Polygon[outerRings.size()];
        for (int outerIdx = 0; outerIdx < outerRings.size(); outerIdx++) {
            List<org.locationtech.jts.geom.LineString> currentInnerRings = new ArrayList<>();
            for (int innerIdx = 0; innerIdx < outerRings.size(); innerIdx++) {
                if (outerRingIdx.get(innerIdx) == outerIdx) {
                    currentInnerRings.add(innerRings.get(innerIdx));
                }
            }

            jtsPolygons[outerIdx] = geometryFactory.createPolygon(
                outerRings.get(outerIdx),
                innerRings.toArray(org.locationtech.jts.geom.LinearRing[]::new)
            );
        }

        return geometryFactory.createMultiPolygon(jtsPolygons);
	}

	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		SimpleFeature simpleFeature = convertPartField2SimpleFeature(taskDataFile.getPartfield().get(index));
		index++;
		return simpleFeature;
	}

    public ReferencedEnvelope getBounds() {
        ReferencedEnvelope envelope = new ReferencedEnvelope(DefaultGeographicCRS.WGS84);
        taskDataFile.getPartfield().stream().forEach(partfield -> {
            SimpleFeature feature = convertPartField2SimpleFeature(partfield);
            Geometry geometry = (Geometry)feature.getDefaultGeometry();

            if (geometry != null) {
                Envelope partfieldEnvelope = geometry.getEnvelopeInternal();
                envelope.expandToInclude(partfieldEnvelope);
            }
        });

        if (envelope.isEmpty()) {
            return null;
        }
        return envelope;
    }
}

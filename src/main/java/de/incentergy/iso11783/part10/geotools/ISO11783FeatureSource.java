package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class ISO11783FeatureSource extends ContentFeatureSource {

	private ISO11783TaskZipParser iSO11783TaskZipParser;

	public ISO11783FeatureSource(ISO11783TaskZipParser iSO11783TaskZipParser, ContentEntry entry, Query query) {
		super(entry, query);
		this.iSO11783TaskZipParser = iSO11783TaskZipParser;
	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
		return null;
	}

	@Override
	protected int getCountInternal(Query query) throws IOException {
		return -1;
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		return new PartfieldFeatureReader(iSO11783TaskZipParser.taskFile, getState());
	}

	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(entry.getName());

        switch(entry.getName().getLocalPart()) {
            case "Partfield":
                addAttributesForPartfield(builder);
				break;
            case "TimeLog":
                addAttributesForTimeLog(builder);
				break;
			case "GuidanceLine":
				addAttributesForGuidancePattern(builder);
				break;
        }

		final SimpleFeatureType SCHEMA = builder.buildFeatureType();
		return SCHEMA;
	}

	static void addAttributesForTimeLog(SimpleFeatureTypeBuilder builder) {
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system

        // byte[] iSO11783TaskZipParser.timeLogXmlFiles[entry.getName().getNamespaceURI()]
    }

	static void addAttributesForPartfield(SimpleFeatureTypeBuilder builder) {
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
	}


	static void addAttributesForGuidancePattern(SimpleFeatureTypeBuilder builder) {
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
		builder.add("guidancePatternId", String.class);
		builder.add("guidancePatternDesignator", String.class);
		builder.add("guidancePatternType", String.class);
		builder.add("guidanceLine", LineString.class);
	}
}

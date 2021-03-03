package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.incentergy.iso11783.part10.v4.Time;

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

		switch (entry.getName().getLocalPart()) {
			case "Partfield":
				addAttributesForPartfield(builder);
				break;
			case "TimeLog":
				addAttributesForTimeLog(builder, entry);
				break;
			case "Grid":
				addAttributesForGrid(builder, iSO11783TaskZipParser.gridList);
				break;
		}

		final SimpleFeatureType SCHEMA = builder.buildFeatureType();
		return SCHEMA;
	}

	static void addAttributesForTimeLog(SimpleFeatureTypeBuilder builder, ISO11783TaskZipParser iSO11783TaskZipParser) {
		builder.setCRS(DefaultGeographicCRS.WGS84);

        List<TimeLogFileData> timeLogs = iSO11783TaskZipParser.timeLogList;
        if (timeLogs.size() == 0) {
            return;
        }

        List<Time> times = timeLogs.get(0).getTimes();

        if (times.size() == 0) {
            return;
        }

        times.get(0).getDataLogValue().stream().forEach(logValue -> {
            ByteBuffer wrapped = ByteBuffer.wrap(logValue.getProcessDataDDI()); // big-endian by default
            short num = wrapped.getShort();
            builder.add("DDI" + num, Integer.class);
        });
    }

	static void addAttributesForGrid(SimpleFeatureTypeBuilder builder, List<GridFileData> gridList) {
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system

		builder.add("point", Point.class);

		int maxEntriesForGrids = gridList.stream().mapToInt(gridFileData -> gridFileData.getGridEntries().stream()
				.mapToInt(gridEntry -> gridEntry.getValues().size()).max().orElse(0)).max().orElse(0);
		for (int i = 0; i < maxEntriesForGrids; i++) {
			builder.add("value-" + (i + 1), Integer.class);
		}

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

}

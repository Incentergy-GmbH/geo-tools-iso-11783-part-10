package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import de.incentergy.iso11783.part10.v4.Position;
import de.incentergy.iso11783.part10.v4.Time;

public class TimeLogFeatureReader extends AbstractFeatureReader {

	private List<Time> timeLogs;
	protected SimpleFeatureBuilder builder;
	protected int index = 0;

	/** Factory class for geometry creation */
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	public TimeLogFeatureReader(List<TimeLogFileData> timeLogList, Name entryName) {
		timeLogs = timeLogList.stream().flatMap(timeLogFileData -> timeLogFileData.getTimes().stream())
				.collect(Collectors.toList());

		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName(entryName);
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);

        Set<String> attributeNames = timeLogList.stream()
            .filter(timeLog -> timeLog.getTimes().size() > 0)
            .flatMap(timeLog -> timeLog.getTimes().get(0).getDataLogValue().stream().map(logValue -> {
                ByteBuffer wrapped = ByteBuffer.wrap(logValue.getProcessDataDDI()); // big-endian by default
                short ddi = wrapped.getShort();
                return "DDI" + ddi + "_" + logValue.getDeviceElementIdRef();
            }))
            .collect(Collectors.toSet());
        
        typeBuilder.add("position", Point.class);
        typeBuilder.add("time", Long.class);
        attributeNames.stream().forEach(attrName -> {
            typeBuilder.add(attrName, Integer.class);
        });
        
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();

		builder = new SimpleFeatureBuilder(featureType);
	}

	private SimpleFeature convertTimeLog2SimpleFeature(Time time) {

		if (time.getPosition().size() > 0) {
			Position firstPosition = time.getPosition().get(0);
			if (firstPosition.getPositionEast() != null && firstPosition.getPositionNorth() != null) {
				builder.set("position",
						geometryFactory.createPoint(new Coordinate(firstPosition.getPositionEast().doubleValue(),
								firstPosition.getPositionNorth().doubleValue())));
			}
		}
        builder.set("time", time.getStart().toGregorianCalendar().getTimeInMillis());

		time.getDataLogValue().stream().forEach(logValue -> {
			ByteBuffer wrapped = ByteBuffer.wrap(logValue.getProcessDataDDI()); // big-endian by default
			short num = wrapped.getShort();
            builder.set("DDI" + num +"_"+logValue.getDeviceElementIdRef(), logValue.getProcessDataValue());
		});

		return builder.buildFeature(String.valueOf(index));
	}

	@Override
	public SimpleFeatureType getFeatureType() {
		return builder.getFeatureType();
	}

	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		return convertTimeLog2SimpleFeature(timeLogs.get(index++));
	}

	@Override
	public boolean hasNext() throws IOException {
		return index < timeLogs.size();
	}

	@Override
	public void close() throws IOException {

	}

    ReferencedEnvelope getBounds() {
        ReferencedEnvelope envelope = new ReferencedEnvelope(DefaultGeographicCRS.WGS84);
        timeLogs.stream().forEach(time -> {
            if (time.getPosition().size() == 0) {
                return;
            }

            Position pos = time.getPosition().get(0);
            envelope.expandToInclude(
                pos.getPositionEast().doubleValue(),
                pos.getPositionNorth().doubleValue()
            );
        });
        return envelope;
    }
}

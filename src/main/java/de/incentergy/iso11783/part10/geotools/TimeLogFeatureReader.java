package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.incentergy.iso11783.part10.v4.Position;
import de.incentergy.iso11783.part10.v4.Time;

public class TimeLogFeatureReader extends AbstractFeatureReader {

	private List<Time> timeLogs;
	protected SimpleFeatureBuilder builder;
	protected int index = 0;

	/** Factory class for geometry creation */
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	public TimeLogFeatureReader(List<TimeLogFileData> timeLogList, SimpleFeatureType featureType) {
		timeLogs = timeLogList.stream().flatMap(timeLogFileData -> timeLogFileData.getTimes().stream())
				.collect(Collectors.toList());

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
		time.getDataLogValue().stream().forEach(logValue -> {
			ByteBuffer wrapped = ByteBuffer.wrap(logValue.getProcessDataDDI()); // big-endian by default
			short num = wrapped.getShort();
			builder.set("DDI" + num, logValue.getProcessDataValue());
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

}

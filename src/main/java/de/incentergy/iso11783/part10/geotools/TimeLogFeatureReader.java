package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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

import de.incentergy.iso11783.part10.v4.DataLogValue;
import de.incentergy.iso11783.part10.v4.Position;
import de.incentergy.iso11783.part10.v4.Time;

class TimeWithFilename {
    public Time time;
    public String filename;
    TimeWithFilename(Time time, String filename) {
        this.time = time;
        this.filename = filename;
    }
}

public class TimeLogFeatureReader extends AbstractFeatureReader {

	private List<TimeWithFilename> timeLogs;
	protected SimpleFeatureBuilder builder;
	protected int index = 0;

	/** Factory class for geometry creation */
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

    private String constructAttributeName(DataLogValue logValue) {
        ByteBuffer wrapped = ByteBuffer.wrap(logValue.getProcessDataDDI()); // big-endian by default
        int num = Unsigned.getUnsignedShort(wrapped);

        List<String> components = new ArrayList<>();
        components.add("DDI" + num);
        components.add(logValue.getDeviceElementIdRef());

        if (logValue.getDataLogPGN() != null) {
            components.add(logValue.getDataLogPGN().toString());
            components.add(logValue.getDataLogPGNStartBit().toString());
            components.add(logValue.getDataLogPGNStopBit().toString());
        }

        return String.join("_", components);
    }

	public TimeLogFeatureReader(List<TimeLogFileData> timeLogList, Name entryName) {
		timeLogs = timeLogList.stream()
            .flatMap(timeLogFileData -> timeLogFileData.getTimes().stream()
                .map(time -> new TimeWithFilename(time, timeLogFileData.getTimeLog().getFilename()))
            )
            .collect(Collectors.toList());

		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName(entryName);
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);

        Set<String> attributeNames = timeLogList.stream()
            .filter(timeLog -> timeLog.getTimes().size() > 0)
            .flatMap(timeLog -> timeLog.getTimes().get(0).getDataLogValue().stream()
                .map(logValue -> constructAttributeName(logValue))
            )
            .collect(Collectors.toSet());
        
        typeBuilder.add("position", Point.class);
        typeBuilder.add("time", Long.class);
        typeBuilder.add("filename", String.class);
        attributeNames.stream().forEach(attrName -> {
            typeBuilder.add(attrName, Integer.class);
        });
        
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();

		builder = new SimpleFeatureBuilder(featureType);
	}

	private SimpleFeature convertTimeLog2SimpleFeature(TimeWithFilename timeWithFilename) {

		if (timeWithFilename.time.getPosition().size() > 0) {
			Position firstPosition = timeWithFilename.time.getPosition().get(0);
			if (firstPosition.getPositionEast() != null && firstPosition.getPositionNorth() != null) {
				builder.set("position",
						geometryFactory.createPoint(new Coordinate(firstPosition.getPositionEast().doubleValue(),
								firstPosition.getPositionNorth().doubleValue())));
			}
		}
        builder.set("time", timeWithFilename.time.getStart().toGregorianCalendar().getTimeInMillis());
        builder.set("filename", timeWithFilename.filename);

		timeWithFilename.time.getDataLogValue().stream().forEach(logValue -> {
            builder.set(constructAttributeName(logValue), logValue.getProcessDataValue());
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
        index = 0; //FIXME
	}

    ReferencedEnvelope getBounds() {
        ReferencedEnvelope envelope = new ReferencedEnvelope(DefaultGeographicCRS.WGS84);
        timeLogs.stream().forEach(timeWithFilename -> {
            if (timeWithFilename.time.getPosition().size() == 0) {
                return;
            }

            Position pos = timeWithFilename.time.getPosition().get(0);
            envelope.expandToInclude(
                pos.getPositionEast().doubleValue(),
                pos.getPositionNorth().doubleValue()
            );
        });
        return envelope;
    }
}

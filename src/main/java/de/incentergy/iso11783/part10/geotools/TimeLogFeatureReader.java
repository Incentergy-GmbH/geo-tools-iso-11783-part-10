package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.incentergy.iso11783.part10.v4.Time;

public class TimeLogFeatureReader extends AbstractFeatureReader {

    private List<Time> timeLogs;
    protected SimpleFeatureBuilder builder;
    protected int index = 0;

    public TimeLogFeatureReader(List<TimeLogFileData> timeLogList, SimpleFeatureType featureType) {
        timeLogs = timeLogList.stream()
            .flatMap(timeLogFileData -> timeLogFileData.getTimes().stream())
            .collect(Collectors.toList());

		builder = new SimpleFeatureBuilder(featureType);
    }

    private SimpleFeature convertTimeLog2SimpleFeature(Time time) {
        time.getDataLogValue().stream().forEach(logValue -> {
            ByteBuffer wrapped = ByteBuffer.wrap(logValue.getProcessDataDDI()); // big-endian by default
            short num = wrapped.getShort();
            builder.set("DDI" + num, logValue.getProcessDataValue());
        });

        return builder.buildFeature(String.valueOf(index));
    }

	@Override
	public SimpleFeatureType getFeatureType() {
		return state.getFeatureType();
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

package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import de.incentergy.iso11783.part10.v4.Time;

public class TimeLogFeatureReader extends AbstractFeatureReader {

    private List<TimeLogFileData> timeLogList;
    protected SimpleFeatureBuilder builder;

    public TimeLogFeatureReader(List<TimeLogFileData>  timeLogList) {
        this.timeLogList = timeLogList;
    }

    private convertTimeLog2SimpleFeature(Time time) {
        time.getDataLogValue().stream().forEach(logValue -> {
            ByteBuffer wrapped = ByteBuffer.wrap(logValue.getProcessDataDDI()); // big-endian by default
            long num = wrapped.getLong();
            builder.set("DDI" + String.valueOf(num), logValue.getProcessDataValue());
        })

        return builder.buildFeature()
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

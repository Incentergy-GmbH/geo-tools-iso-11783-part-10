package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class ISO11783FeatureSource extends ContentFeatureSource {


    private PartfieldFeatureReader partfieldFeatureReader;
    private TimeLogFeatureReader timeLogFeatureReader;
    private GridFeatureReader gridFeatureReader;
    private GuidancePatternFeatureReader guidancePatternFeatureReader;

	public ISO11783FeatureSource(ISO11783TaskZipParser iSO11783TaskZipParser, ContentEntry entry, Query query) {
		super(entry, query);

        this.partfieldFeatureReader = new PartfieldFeatureReader(iSO11783TaskZipParser.getTaskFile(), entry.getName());
	    this.timeLogFeatureReader = new TimeLogFeatureReader(iSO11783TaskZipParser.getTimeLogList(), entry.getName());
        this.gridFeatureReader = new GridFeatureReader(iSO11783TaskZipParser.getGridList(), entry.getName());
        this.guidancePatternFeatureReader = new GuidancePatternFeatureReader(iSO11783TaskZipParser.getTaskFile(), entry.getName());
	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
		if (entry.getName().getLocalPart().startsWith("Partfield")) {
            return partfieldFeatureReader.getBounds();
        }
		return null;
	}

	@Override
	protected int getCountInternal(Query query) throws IOException {
		return -1;
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		if (entry.getName().getLocalPart().startsWith("Partfield")) {
            return partfieldFeatureReader;
        } else if (entry.getName().getLocalPart().startsWith("TimeLog")) {
			return timeLogFeatureReader;
        } else if (entry.getName().getLocalPart().startsWith("Grid")) {
            return gridFeatureReader;
        } else if (entry.getName().getLocalPart().startsWith("GuidancePattern")) {
            return guidancePatternFeatureReader;
		}
		return null;
	}

	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {

        if (entry.getName().getLocalPart().startsWith("Partfield")) {
            return partfieldFeatureReader.getFeatureType();
        } else if (entry.getName().getLocalPart().startsWith("TimeLog")) {
            return timeLogFeatureReader.getFeatureType();
        } else if (entry.getName().getLocalPart().startsWith("Grid")) {
            return gridFeatureReader.getFeatureType();
        } else if (entry.getName().getLocalPart().startsWith("GuidancePattern")) {
            return guidancePatternFeatureReader.getFeatureType();
        } else {
            throw new IOException("Incorrect entry name");
        }
	}
}

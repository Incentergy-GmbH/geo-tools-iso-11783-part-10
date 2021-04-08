package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class GridFeatureReader extends AbstractFeatureReader {

	List<GridFileData> gridFileData;
	int index = 0;
	List<GridEntry> gridEntries;
	SimpleFeatureBuilder builder;

	// for unit test
	GridFeatureReader(List<GridFileData> gridFileData, SimpleFeatureType featureType){
		this.gridFileData = gridFileData;
		this.gridEntries = gridFileData.stream().flatMap(gfd -> gfd.getGridEntries().stream()).collect(Collectors.toList());
		builder = new SimpleFeatureBuilder(featureType);
	}

	public GridFeatureReader(List<GridFileData> gridFileData, ContentState contentState){
		this.gridFileData = gridFileData;
		this.gridEntries = gridFileData.stream().flatMap(gfd -> gfd.getGridEntries().stream()).collect(Collectors.toList());
		this.state = contentState;
		builder = new SimpleFeatureBuilder(state.getFeatureType());
	}

	@Override
	public SimpleFeatureType getFeatureType() {
		return state.getFeatureType();
	}

	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		SimpleFeature feature = convertToSimpleFeature(this.gridEntries.get(index));
		index++;
		return feature;
	}

	private SimpleFeature convertToSimpleFeature(GridEntry gridEntry) {
		builder.set("point", gridEntry.getPoint());
		for(int i=0; i<gridEntry.getValues().size();i++) {
			builder.set("value-"+(i+1), gridEntry.getValues().get(i));
		}
		return builder.buildFeature(gridEntry.getGridFile().getGrid().getFilename()+"-"+index);
	}

	@Override
	public boolean hasNext() throws IOException {
		return index < gridEntries.size();
	}

	@Override
	public void close() throws IOException {
		
	}

	public Envelope getBoundsInternal() {
		Envelope envelope = new Envelope();
		for(GridFileData gridFileData: gridFileData) {
			double rows = gridFileData.getGrid().getGridMaximumRow();
			double cols = gridFileData.getGrid().getGridMaximumColumn();

			double x = gridFileData.getGrid().getGridMinimumEastPosition().doubleValue();
			double y = gridFileData.getGrid().getGridMinimumNorthPosition().doubleValue();

			double w = gridFileData.getGrid().getGridCellEastSize();
			double h = gridFileData.getGrid().getGridCellNorthSize();

			envelope.expandToInclude(x, y);
			envelope.expandToInclude(x + w*cols, y + h*rows);
			}
			return envelope;
	}

}

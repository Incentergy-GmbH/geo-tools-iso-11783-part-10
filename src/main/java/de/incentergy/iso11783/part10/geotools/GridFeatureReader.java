package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

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

}

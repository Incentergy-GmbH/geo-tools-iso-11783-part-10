package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.Name;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Point;

import de.incentergy.iso11783.part10.v4.Grid;

public class GridFeatureReader extends AbstractFeatureReader {

	int index = 0;
	List<GridEntry> gridEntries;
	SimpleFeatureBuilder builder;

	GridFeatureReader(List<GridFileData> gridFileDataList, Name entryName){
		this.gridEntries = gridFileDataList.stream()
            .flatMap(gfd -> gfd.getGridEntries().stream())
            .collect(Collectors.toList());

		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName(entryName);
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
		typeBuilder.add("point", Point.class);

		int maxEntriesForGrids = gridFileDataList.stream()
            .mapToInt(gridFileData -> gridFileData.getGridEntries().stream()
                .mapToInt(gridEntry -> gridEntry.getValues().size())
                .max().orElse(0))
            .max().orElse(0);
		for (int i = 0; i < maxEntriesForGrids; i++) {
			typeBuilder.add("value-" + (i + 1), Integer.class);
		}
	    SimpleFeatureType featureType = typeBuilder.buildFeatureType();

		builder = new SimpleFeatureBuilder(featureType);
	}

	@Override
	public SimpleFeatureType getFeatureType() {
		return builder.getFeatureType();
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

    public ReferencedEnvelope getBounds() {
        ReferencedEnvelope envelope = new ReferencedEnvelope(DefaultGeographicCRS.WGS84);

        gridEntries.stream().forEach(gridEntry -> {
            Grid grid = gridEntry.getGridFile().getGrid();
            double x = grid.getGridMinimumEastPosition().doubleValue();
            double y = grid.getGridMinimumNorthPosition().doubleValue();
            double w = grid.getGridCellEastSize();
            double h = grid.getGridCellNorthSize();
            double cols = grid.getGridMaximumColumn();
            double rows = grid.getGridMaximumRow();
            envelope.expandToInclude(x, y);
            envelope.expandToInclude(x + w * cols, y + h * rows);
        });

        return envelope;
    }

}

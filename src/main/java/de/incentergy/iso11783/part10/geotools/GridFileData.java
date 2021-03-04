package de.incentergy.iso11783.part10.geotools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import de.incentergy.iso11783.part10.v4.Grid;
import de.incentergy.iso11783.part10.v4.GridType;

public class GridFileData {

	private static final Logger log = Logger.getLogger(GridFileData.class.getName());
	private byte[] binFile;

	private List<GridEntry> gridEntries = new ArrayList<>();

	private Grid grid;

	// 4326 -> WGS84
	static GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

	public GridFileData() {
		super();
	}

	public GridFileData(Grid grid, byte[] binFile) {
		setGrid(grid);
		setBinFile(binFile);
		parseGrid();
	}

	public byte[] getBinFile() {
		return binFile;
	}

	public void setBinFile(byte[] binFile) {
		this.binFile = binFile;
	}

	public List<GridEntry> getGridEntries() {
		return gridEntries;
	}

	public void setGridEntries(List<GridEntry> gridEntries) {
		this.gridEntries = gridEntries;
	}

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}


	private void parseGrid() {

		if (binFile == null) {
			log.warning(() -> String.format("binFile for %s is null", (grid != null ? grid.getFilename() : null)));
			return;
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(binFile);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		Consumer<GridEntry> readCellValues = gridTypeDepdendentValueReader(grid, byteBuffer, binFile.length);
		for (int row = 0; row < grid.getGridMaximumRow(); row++) {
			for (int column = 0; column < grid.getGridMaximumColumn(); column++) {
				GridEntry gridEntry = new GridEntry();
				gridEntry.setGridFile(this);
				gridEntry.setPoint(geometryFactory.createPoint(new Coordinate(
						grid.getGridMinimumEastPosition().doubleValue() + (column + 0.5) * grid.getGridCellEastSize(),
						grid.getGridMinimumNorthPosition().doubleValue() + (row + 0.5) * grid.getGridCellNorthSize())));
				readCellValues.accept(gridEntry);
				getGridEntries().add(gridEntry);
			}
		}
	}

	/* ByteBuffer read semantics conform to ISO11783-10, Section 8.6.2, p. 37
	*/
	static Consumer<GridEntry> gridTypeDepdendentValueReader(Grid grid, ByteBuffer byteBuffer, long bufferSize){
		//GRID_TYPE_1 is default
		Consumer<GridEntry> readCellValues = gridEntry ->
				gridEntry.getValues().add((int)byteBuffer.get());

		if (grid.getGridType() == GridType.GRID_TYPE_2) {

			long valuesPerCell = (bufferSize / 4) / (grid.getGridMaximumColumn() * grid.getGridMaximumRow());
			readCellValues = gridEntry -> {
				for (int j = 0; j < valuesPerCell; j++) {
					gridEntry.getValues().add(byteBuffer.getInt());
				}
			};
		}
		return readCellValues;
	}

}

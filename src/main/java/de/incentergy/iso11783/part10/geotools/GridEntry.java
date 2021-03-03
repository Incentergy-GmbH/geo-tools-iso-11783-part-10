package de.incentergy.iso11783.part10.geotools;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class GridEntry {

	private UUID id = java.util.UUID.randomUUID();

	private org.locationtech.jts.geom.Point point;

	private List<Integer> values = new ArrayList<>();
	private GridFileData gridFile;

	public GridEntry() {
		super();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@XmlJavaTypeAdapter(GeometryAdapter.class)
	public org.locationtech.jts.geom.Point getPoint() {
		return point;
	}

	public void setPoint(org.locationtech.jts.geom.Point point) {
		this.point = point;
	}

	public List<Integer> getValues() {
		return values;
	}

	public void setValues(List<Integer> values) {
		this.values = values;
	}

	public GridFileData getGridFile() {
		return gridFile;
	}

	public void setGridFile(GridFileData gridFile) {
		this.gridFile = gridFile;
	}
}

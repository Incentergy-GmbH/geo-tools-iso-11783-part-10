package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import de.incentergy.iso11783.part10.v4.CropType;
import de.incentergy.iso11783.part10.v4.CropVariety;
import de.incentergy.iso11783.part10.v4.Customer;
import de.incentergy.iso11783.part10.v4.Farm;
import de.incentergy.iso11783.part10.v4.GuidancePattern;
import de.incentergy.iso11783.part10.v4.GuidancePatternType;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import de.incentergy.iso11783.part10.v4.LineString;
import de.incentergy.iso11783.part10.v4.LineStringType;
import de.incentergy.iso11783.part10.v4.Partfield;
import de.incentergy.iso11783.part10.v4.Polygon;

public class GuidancePatternFeatureReader extends AbstractFeatureReader {
	private static final Logger log = Logger.getLogger(GridFileData.class.getName());
	public static final String TYPE_NAME_STRING = "GuidancePattern";
	public static SimpleFeatureType FEATURE_TYPE;
	private ISO11783TaskDataFile taskDataFile;
	public List<GuidancePattern> guidancePatterns;
	/** Utility class used to build features */
	protected SimpleFeatureBuilder builder;

	protected int index = 0;

	/** Factory class for geometry creation */
	private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);


	// only for unit test
	GuidancePatternFeatureReader(ISO11783TaskDataFile taskDataFile, SimpleFeatureType featureType) {
		this.taskDataFile = taskDataFile;
		this.builder = new SimpleFeatureBuilder(featureType);
		initializeGuidancePatternList();
	}

	public GuidancePatternFeatureReader(ISO11783TaskDataFile taskDataFile, ContentState contentState) {
		this.taskDataFile = taskDataFile;
		this.state = contentState;
		builder = new SimpleFeatureBuilder(state.getFeatureType());
		initializeGuidancePatternList();
	}


	private void initializeGuidancePatternList(){
		this.guidancePatterns = new ArrayList<>();
		this.taskDataFile.getPartfield().forEach((partfield)-> {
			partfield.getGuidanceGroup().forEach((guidanceGroup)->{
					guidanceGroup.getGuidancePattern().forEach((guidancePattern)->{
						if(guidancePattern.getGuidancePatternType() == GuidancePatternType.AB){
							this.guidancePatterns.add(guidancePattern); 
						} else {
							log.warning("There was a non-AB-line GuidancePattern, which is not yet supported!");	
						}
					});
			});
		});

	}

	@Override
	public void close() throws IOException {

	}

	public SimpleFeature convertGuidancePattern2SimpleFeature(GuidancePattern guidancePattern) {

		builder.set("guidancePatternId", guidancePattern.getGuidancePatternId());
		builder.set("guidancePatternDesignator", guidancePattern.getGuidancePatternDesignator());
		builder.set("guidancePatternType", guidancePattern.getGuidancePatternType().toString());
		if( guidancePattern.getGuidancePatternType() == GuidancePatternType.AB){
			LineString isoLineString =guidancePattern.getLineString();
			org.locationtech.jts.geom.LineString lineString =  geometryFactory.createLineString(coordinates(isoLineString));
			builder.set("guidanceLine", lineString);

		}


		return builder.buildFeature(guidancePattern.getGuidancePatternId());
	}

	public Coordinate[] coordinates(LineString ls) {
		return ls.getPoint().stream().map(
				point -> new Coordinate(Double.valueOf(point.getPointEast()), Double.valueOf(point.getPointNorth())))
				.toArray(Coordinate[]::new);
	}

	@Override
	public SimpleFeatureType getFeatureType() {
		return state.getFeatureType();
	}

	@Override
	public boolean hasNext() throws IOException {
		return index < guidancePatterns.size();
	}


	public int countGuidancePatterns(){
		return this.guidancePatterns.size();
	}

	@Override
	public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
		SimpleFeature simpleFeature = convertGuidancePattern2SimpleFeature(guidancePatterns.get(index));
		index++;
		return simpleFeature;
	}

}

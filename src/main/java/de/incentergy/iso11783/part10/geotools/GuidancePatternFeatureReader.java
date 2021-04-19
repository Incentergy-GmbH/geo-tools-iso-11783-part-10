package de.incentergy.iso11783.part10.geotools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import de.incentergy.iso11783.part10.v4.GuidancePattern;
import de.incentergy.iso11783.part10.v4.GuidancePatternType;
import de.incentergy.iso11783.part10.v4.ISO11783TaskDataFile;
import de.incentergy.iso11783.part10.v4.LineString;

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
	GuidancePatternFeatureReader(ISO11783TaskDataFile taskDataFile, Name entryName) {
		this.taskDataFile = taskDataFile;

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName(entryName);
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
		typeBuilder.add("guidanceLine", org.locationtech.jts.geom.LineString.class);
		typeBuilder.add("guidancePatternId", String.class);
		typeBuilder.add("guidancePatternDesignator", String.class);
		typeBuilder.add("guidancePatternType", String.class);
	    SimpleFeatureType featureType = typeBuilder.buildFeatureType();

		this.builder = new SimpleFeatureBuilder(featureType);
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
			LineString isoLineString = guidancePattern.getLineString();
			org.locationtech.jts.geom.LineString lineString = geometryFactory.createLineString(coordinates(isoLineString));
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
		return builder.getFeatureType();
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

    public ReferencedEnvelope getBounds() {
        ReferencedEnvelope envelope = new ReferencedEnvelope(DefaultGeographicCRS.WGS84);

        guidancePatterns.forEach(guidancePattern -> {
            SimpleFeature feature = convertGuidancePattern2SimpleFeature(guidancePattern);
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom != null) {
                envelope.expandToInclude(geom.getEnvelopeInternal());
            }
        });

        return envelope;
    }

}

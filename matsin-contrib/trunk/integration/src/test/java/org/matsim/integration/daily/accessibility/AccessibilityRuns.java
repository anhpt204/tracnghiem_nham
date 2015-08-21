package org.matsim.integration.daily.accessibility;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.accessibility.AccessibilityConfigGroup;
import org.matsim.contrib.accessibility.GridBasedAccessibilityControlerListenerV3;
import org.matsim.contrib.accessibility.Modes4Accessibility;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup.ActivityDurationInterpretation;
import org.matsim.core.controler.Controler;
import org.matsim.core.replanning.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.ExeRunner;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.testcases.MatsimTestUtils;

public class AccessibilityRuns {
	public static final Logger log = Logger.getLogger( AccessibilityRuns.class ) ;

	public static final Boolean doPopulationWeightedPlot = null;

	public static final Boolean doNonPopulationWeightedPlot = null;
	
	private static final double cellSize = 1000.;

	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test
	public void doAccessibilityTest() {

		Config config = ConfigUtils.createConfig( new AccessibilityConfigGroup() ) ;
		
		AccessibilityConfigGroup acg = (AccessibilityConfigGroup) config.getModule( AccessibilityConfigGroup.GROUP_NAME ) ;
		
		config.network().setInputFile("../../matsimExamples/countries/za/nmbm/network/NMBM_Network_CleanV7.xml.gz");
		config.facilities().setInputFile("../../matsimExamples/countries/za/nmbm/facilities/20121010/facilities.xml.gz" );

		config.controler().setLastIteration(0);

		config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.ABORT );

		// some (otherwise irrelevant) settings to make the vsp check happy:
		config.timeAllocationMutator().setMutationRange(7200.);
		config.timeAllocationMutator().setAffectingDuration(false);
		config.vspExperimental().setRemovingUnneccessaryPlanAttributes(true);
		config.plans().setActivityDurationInterpretation( ActivityDurationInterpretation.tryEndTimeThenDuration );

		{
			StrategySettings stratSets = new StrategySettings( ConfigUtils.createAvailableStrategyId(config) );
			stratSets.setStrategyName( DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta.toString() );
			stratSets.setWeight(1.);
			config.strategy().addStrategySettings(stratSets);
		}

		Scenario scenario = ScenarioUtils.loadScenario( config ) ;

		List<String> activityTypes = new ArrayList<String>() ;
		ActivityFacilities homes = FacilitiesUtils.createActivityFacilities("homes") ;
		for ( ActivityFacility fac : scenario.getActivityFacilities().getFacilities().values()  ) {
			for ( ActivityOption option : fac.getActivityOptions().values() ) {
				// figure out all activity types
				if ( !activityTypes.contains(option.getType()) ) {
					activityTypes.add( option.getType() ) ;
				}
				// figure out where the homes are
				if ( option.getType().equals("h") ) {
					homes.addActivityFacility(fac);
				}
			}
		}

		// new
		Map<String, ActivityFacilities> activityFacilitiesMap = new HashMap<String, ActivityFacilities>();
		Controler controler = new Controler(scenario) ;
		controler.setOverwriteFiles(true);
		// end new

		log.warn( "found activity types: " + activityTypes );
		// yyyy there is some problem with activity types: in some algorithms, only the first letter is interpreted, in some
		// other algorithms, the whole string.  BEWARE!  This is not good software design and should be changed.  kai, feb'14

		
		// two loops over activity types and modes to add one GridBasedAccessibilityControlerListenerV3 for each combination
		for ( String actType : activityTypes ) {
			for ( Modes4Accessibility mode : Modes4Accessibility.values()) {
				//				if ( !actType.equals("w") ) {
				//					log.error("skipping everything except work for debugging purposes; remove in production code. kai, feb'14") ;
				//					continue ;
				//				}
				if ( !mode.equals(Modes4Accessibility.freeSpeed) ) {
					log.error("skipping everything except freespeed for debugging purposes; remove in production code. dz, nov'14") ;
					continue ;
				}

				config.controler().setOutputDirectory( utils.getOutputDirectory());
				ActivityFacilities opportunities = FacilitiesUtils.createActivityFacilities() ;
				for ( ActivityFacility fac : scenario.getActivityFacilities().getFacilities().values()  ) {
					for ( ActivityOption option : fac.getActivityOptions().values() ) {
						if ( option.getType().equals(actType) ) {
							opportunities.addActivityFacility(fac);
						}
					}
				}

				activityFacilitiesMap.put(actType, opportunities);

				GridBasedAccessibilityControlerListenerV3 listener = 
						new GridBasedAccessibilityControlerListenerV3(activityFacilitiesMap.get(actType), config, scenario.getNetwork());
				// define the mode that will be considered
				// listener.setComputingAccessibilityForMode(Modes4Accessibility.freeSpeed, true);
				listener.setComputingAccessibilityForMode(mode, true);
				listener.addAdditionalFacilityData(homes) ;
				listener.generateGridsAndMeasuringPointsByNetwork(cellSize);

				listener.writeToSubdirectoryWithName(actType + "/" + mode);
				
				listener.setUrbansimMode(false); // avoid writing some (eventually: all) files that related to matsim4urbansim

				controler.addControlerListener(listener);
			}
		}

		controler.run();


		//		GnuplotScriptWriter.createGnuplotScript(config, activityTypes);


		// write mapnik file that is neded to have osm layer in QGis project
		final String mapnikFileName = "osm_mapnik.xml";
		String osmMapnikFile = config.controler().getOutputDirectory() + mapnikFileName;

		try {
			BufferedWriter writer = IOUtils.getBufferedWriter( osmMapnikFile ) ;

			writer.write("<GDAL_WMS>\n");
			writer.write("\t<Service name=\"TMS\">\n");
			writer.write("\t\t<ServerUrl>http://tile.openstreetmap.org/${z}/${x}/${y}.png</ServerUrl>\n");
			writer.write("\t</Service>\n");
			writer.write("\t<DataWindow>\n");
			//TODO make coordinates adjustable, ideally with an automatic coordinate system conversion so that they only have to be set once (dz, dez'14)
			writer.write("\t\t<UpperLeftX>-20037508.34</UpperLeftX>\n");
			writer.write("\t\t<UpperLeftY>20037508.34</UpperLeftY>\n");
			writer.write("\t\t<LowerRightX>20037508.34</LowerRightX>\n");
			writer.write("\t\t<LowerRightY>-20037508.34</LowerRightY>\n");
			writer.write("\t\t<TileLevel>18</TileLevel>\n");
			writer.write("\t\t<TileCountX>1</TileCountX>\n");
			writer.write("\t\t<TileCountY>1</TileCountY>\n");
			writer.write("\t\t<YOrigin>top</YOrigin>\n");
			writer.write("\t</DataWindow>\n");
			writer.write("\t<Projection>EPSG:3857</Projection>\n");
			writer.write("\t<BlockSizeX>256</BlockSizeX>\n");
			writer.write("\t<BlockSizeY>256</BlockSizeY>\n");
			writer.write("\t<BandsCount>3</BandsCount>\n");
			writer.write("\t<Cache>\n");
			writer.write("\t\t<Path>/tmp/cache_osm_mapnik</Path>\n");
			writer.write("\t</Cache>\t\n");
			writer.write("</GDAL_WMS>\n");

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException( "writing Mapnik file did not work") ;
		}
		
		
		
////		String workingDirectory =  "C:/Users/Daniel/Desktop/MATSimQGisIntegration/";
//		String workingDirectory =  config.controler().getOutputDirectory();
////		String qGisProjectFile = "testWithMergedImmissionsCSV.qgs";
//		
//		QGisWriter writer = new QGisWriter(TransformationFactory.DHDN_GK4, workingDirectory);
//		
//		
//		//use case 4: accessibility
//		double[] extent = {100000,-3720000,180000,-3675000};
//		writer.setExtent(extent);
//		
//		//example for adding a raster layer
//		//RasterLayer mapnikLayer = new RasterLayer("osm_mapnik_xml", workingDirectory + "testfiles/accessibility/osm_mapnik.xml");
//		RasterLayer mapnikLayer = new RasterLayer("osm_mapnik_xml", workingDirectory + "/osm_mapnik.xml");
//		mapnikLayer.setRenderer(new AccessibilityXmlRenderer());
//		mapnikLayer.setSrs("WGS84_Pseudo_Mercator");
//		writer.addLayer(0,mapnikLayer);


		// two loops over activity types and modes to produce one QGIs project file for each combination
		for (String actType : activityTypes) {
			for ( Modes4Accessibility mode : Modes4Accessibility.values()) {
				//				if ( !actType.equals("w") ) {
				//					log.error("skipping everything except work for debugging purposes; remove in production code. kai, feb'14") ;
				//					continue ;
				//				}
				if ( !mode.equals(Modes4Accessibility.freeSpeed) ) {
					log.error("skipping everything except freespeed for debugging purposes; remove in production code. dz, nov'14") ;
					continue ;
				}

				String version = "<qgis projectname=\"\" version=\"2.6.1-Brighton\">\n";

				QGisProjectFileWriter qgisWriter = new QGisProjectFileWriter();

				try {
					BufferedWriter writer = IOUtils.getBufferedWriter( config.controler().getOutputDirectory() + "/" + actType 
							+ "/" + mode + "/QGisProjectFile.qgs" ) ;
					//					openFile(this.outputFolder+filename);

					qgisWriter.writeQGisHead(writer);
					writer.write(version);
					//					handler.startLegend(writer);
					//					handler.writeLegendLayer(writer,key);
					//					handler.writeProjectLayer(writer, key, geometry, claz, type);

					//TODO Should be split up into (customizable) modules later, so far just a big collection of everything
					if (actType.equals("w")) {
						qgisWriter.writeEverythingWork(writer, "../../"+mapnikFileName);
					} else if (actType.equals("e")){
						qgisWriter.writeEverythingEdu(writer, "../../"+mapnikFileName);						
					} else {
						qgisWriter.writeEverythingOther(writer, "../../"+mapnikFileName);
					}

					//					handler.endProjectLayers(writer);
					//					handler.writeProperties(writer);
					qgisWriter.endDocument(writer);

					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException( "writing QGis project file did not work") ;
				}
				
				
				
				
				
		
//				// VectorLayer densityLayer = new VectorLayer("density", workingDirectory + "testFiles/accessibility/accessibilities.csv", QGisConstants.geometryType.Point);
//				VectorLayer densityLayer = new VectorLayer("density", workingDirectory + "/" + actType 
//						+ "/" + mode + "/accessibilities.csv", QGisConstants.geometryType.Point);
//				densityLayer.setXField("field_1");
//				densityLayer.setYField("field_2");
//				AccessibilityDensitiesRenderer dRenderer = new AccessibilityDensitiesRenderer();
//				dRenderer.setRenderingAttribute("field_8");
//				densityLayer.setRenderer(dRenderer);
//				writer.addLayer(1,densityLayer);
//				
////				VectorLayer accessibilityLayer = new VectorLayer("accessibility", workingDirectory + "testFiles/accessibility/accessibilities.csv", QGisConstants.geometryType.Point);
//				VectorLayer accessibilityLayer = new VectorLayer("accessibility", workingDirectory + "/" + actType 
//						+ "/" + mode + "t/accessibilities.csv", QGisConstants.geometryType.Point);
//				
//				//there are two ways to set x and y fields for csv geometry files
//				//1) if there is a header, you can set the members xField and yField to the name of the column headers
//				//2) if there is no header, you can write the column index into the member (e.g. field_1, field_2,...), but works also if there is a header
//				accessibilityLayer.setXField("field_1");
//				accessibilityLayer.setYField("field_2");
//				AccessibilityRenderer renderer = new AccessibilityRenderer();
//				renderer.setRenderingAttribute("field_3"); // choose column/header to visualize
//				accessibilityLayer.setRenderer(renderer);
//				writer.addLayer(2,accessibilityLayer);
				
				
				// now creating a snapshot based on above-created project file
				
				// if OS is Windows
				// example (daniel) // os.arch=amd64 // os.name=Windows 7 // os.version=6.1
				if ( System.getProperty("os.name").contains("Win") || System.getProperty("os.name").contains("win")) {
					// On Windows, the PATH variables need to be set correctly to be able to call "qgis.bat" on the command line
					// This needs to be done manually. It does not seem to be set automatically when installing QGis
					String cmd = "qgis.bat " + config.controler().getOutputDirectory() + actType + "/" + mode + "/QGisProjectFile.qgs" +
							" --snapshot " + config.controler().getOutputDirectory() + actType + "/" + mode + "/" + actType + "_" + mode + "_snapshot.png";

					String stdoutFileName = config.controler().getOutputDirectory() + actType + "/" + mode + "/" + actType + "_" + mode + "_snapshot.log";
					int timeout = 99999;

					ExeRunner.run(cmd, stdoutFileName, timeout);
				
				// if OS is Macintosh
				// example (dominik) // os.arch=x86_64 // os.name=Mac OS X // os.version=10.10.2
				//} else if ( System.getProperty("os.arch").contains("mac")) {
				} else if ( System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("mac") ) {
					String cmd = "/Applications/QGIS.app/Contents/MacOS/QGIS " + config.controler().getOutputDirectory() + actType + "/" + mode + "/QGisProjectFile.qgs" +
							" --snapshot " + config.controler().getOutputDirectory() + actType + "/" + mode + "/" + actType + "_" + mode + "_snapshot.png";

					String stdoutFileName = config.controler().getOutputDirectory() + actType + "/" + mode + "/" + actType + "_" + mode + "_snapshot.log";
					int timeout = 99999;

					ExeRunner.run(cmd, stdoutFileName, timeout);
				
				// if OS is Linux
				// example (benjamin) // os.arch=amd64 // os.name=Linux	// os.version=3.13.0-45-generic
				//} else if ( System.getProperty("os.name").contains("Lin") || System.getProperty("os.name").contains("lin") ) {
					// TODO for linux
					
				// if OS is other
				} else {
					log.warn("generating png files not implemented for os.arch=" + System.getProperty("os.arch") );
				}
			}	
		}		
	}
}

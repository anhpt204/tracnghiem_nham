package org.matsim.contrib.accessibility;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.accessibility.utils.Benchmark;
import org.matsim.contrib.accessibility.utils.LeastCostPathTreeExtended;
import org.matsim.contrib.matrixbasedptrouter.PtMatrix;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.facilities.ActivityFacilitiesImpl;
import org.matsim.roadpricing.RoadPricingScheme;
import org.matsim.roadpricing.RoadPricingSchemeImpl;
import org.matsim.utils.LeastCostPathTree;

import java.util.Map;

/**
 *  improvements feb'12
 *  - distance between zone centroid and nearest node on road network is considered in the accessibility computation
 *  as walk time of the euclidian distance between both (centroid and nearest node). This walk time is added as an offset 
 *  to each measured travel times
 *  - using walk travel times instead of travel distances. This is because of the betas that are utils/time unit. The walk time
 *  corresponds to distances since this is also linear.
 * 
 * This works for UrbanSim Zone and Parcel Applications !!! (march'12)
 * 
 *  improvements april'12
 *  - accessibility calculation uses configurable betas (coming from UrbanSim) for car/walk travel times, -distances and -costs
 *  
 * improvements / changes july'12 
 * - fixed error: used pre-factor (1/beta scale) in deterrence function instead of beta scale (fixed now!)
 * 
 * todo (sep'12):
 * - set external costs to opportunities within the same zone ...
 * 
 * improvements jan'13
 * - added pt for accessibility calculation
 * 
 * improvements april'13
 * - congested car modes uses TravelDisutility from MATSim
 * - taking disutilites directly from MATSim (controler.createTravelCostCalculator()), this 
 * also activates road pricing ...
 * 
 * improvements june'13
 * - removed zones as argument to ZoneBasedAccessibilityControlerListenerV3
 * - providing opportunity facilities (e.g. workplaces)
 * - reduced dependencies to MATSim4UrbanSim contrib: replaced ZoneLayer<Id> and Zone by standard MATSim ActivityFacilities
 * 
 * @author thomas
 *
 */
public final class ZoneBasedAccessibilityControlerListenerV3 extends AccessibilityControlerListenerImpl implements ShutdownListener{
	
	private static final Logger log = Logger.getLogger(ZoneBasedAccessibilityControlerListenerV3.class);
	private UrbanSimZoneCSVWriterV2 urbanSimZoneCSVWriterV2;
	

	// ////////////////////////////////////////////////////////////////////
	// constructors
	// ////////////////////////////////////////////////////////////////////
	
	public ZoneBasedAccessibilityControlerListenerV3(ActivityFacilitiesImpl measuringPoints,
			   ActivityFacilitiesImpl opportunities,
			   String matsim4opusTempDirectory,
			   Scenario scenario) {
		this(measuringPoints, opportunities, null, matsim4opusTempDirectory, scenario);
	}
	
	public ZoneBasedAccessibilityControlerListenerV3(ActivityFacilitiesImpl measuringPoints,
												   ActivityFacilitiesImpl opportunities,
												   PtMatrix ptMatrix,
												   String matsim4opusTempDirectory,
												   Scenario scenario) {
		
		log.info("Initializing ZoneBasedAccessibilityControlerListenerV3 ...");
		
		assert(measuringPoints != null);
		this.measuringPoints = measuringPoints;
		assert(matsim4opusTempDirectory != null);
		this.ptMatrix = ptMatrix; // this could be zero of no input files for pseudo pt are given ...
		assert(scenario != null);

		this.benchmark = new Benchmark();
		
		// writing accessibility measures continuously into "zone.csv"-file. Naming of this 
		// files is given by the UrbanSim convention importing a csv file into a identically named 
		// data set table. THIS PRODUCES URBANSIM INPUT
		urbanSimZoneCSVWriterV2 = new UrbanSimZoneCSVWriterV2(matsim4opusTempDirectory);
		initAccessibilityParameters(scenario.getConfig());

		// aggregating facilities to their nearest node on the road network
		this.aggregatedOpportunities = aggregatedOpportunities(opportunities, scenario.getNetwork());
		// yyyy ignores the "capacities" of the facilities. kai, mar'14
		
		
		log.info(".. done initializing ZoneBasedAccessibilityControlerListenerV3");
	}
	
	@Override
	public void notifyShutdown(ShutdownEvent event) {
		log.info("Entering notifyShutdown ..." );
		
		// make sure that that at least one tranport mode is selected
		boolean problem = true ;
		for ( Boolean bool : this.isComputingMode.values() ) {
			if ( bool == true ) {
				problem = false ;
				break ;
			}
		}
		
		if( problem ) {
			log.error("No transport mode for accessibility calculation is activated! For this reason no accessibilities can be calculated!");
			log.info("Please activate at least one transport mode by using the corresponding method when initializing the accessibility listener to fix this problem:");
			log.info("- useFreeSpeedGrid()");
			log.info("- useCarGrid()");
			log.info("- useBikeGrid()");
			log.info("- useWalkGrid()");
			log.info("- usePtGrid()");
			return;
		}
		
		
		// get the controller and scenario
		Controler controler = event.getControler();
        NetworkImpl network = (NetworkImpl) controler.getScenario().getNetwork();

		int benchmarkID = this.benchmark.addMeasure("zone-based accessibility computation");

		
		// get the free-speed car travel times (in seconds)
		TravelTime ttf = new FreeSpeedTravelTime() ;
		TravelDisutility tdFree = controler.getTravelDisutilityFactory().createTravelDisutility(ttf, controler.getConfig().planCalcScore() ) ;
		LeastCostPathTreeExtended lcptExtFreeSpeedCarTrvelTime = new LeastCostPathTreeExtended( ttf, tdFree, (RoadPricingSchemeImpl) controler.getScenario().getScenarioElement(RoadPricingScheme.ELEMENT_NAME) ) ;

		// get the congested car travel time (in seconds)
		TravelTime ttc = controler.getLinkTravelTimes(); // congested
		TravelDisutility tdCongested = controler.getTravelDisutilityFactory().createTravelDisutility(ttc, controler.getConfig().planCalcScore() ) ;
		LeastCostPathTreeExtended  lcptExtCongestedCarTravelTime = new LeastCostPathTreeExtended(ttc, tdCongested, (RoadPricingSchemeImpl) controler.getScenario().getScenarioElement(RoadPricingScheme.ELEMENT_NAME) ) ;

		// get travel distance (in meter)
		LeastCostPathTree lcptTravelDistance		 = new LeastCostPathTree( ttf, new LinkLengthTravelDisutility());
		
		this.scheme = (RoadPricingSchemeImpl) controler.getScenario().getScenarioElement(RoadPricingScheme.ELEMENT_NAME);

		try{
			log.info("Computing and writing zone based accessibility measures ..." );
			// printParameterSettings(); // use only for debugging (settings are printed as part of config dump)
			log.info(measuringPoints.getFacilities().values().size() + " measurement points are now processing ...");
			
			accessibilityComputation(ttc, 
					lcptExtFreeSpeedCarTrvelTime,
					lcptExtCongestedCarTravelTime, 
					lcptTravelDistance, 
					network, 
					measuringPoints,
					ZONE_BASED);
			
			System.out.println();
			// finalizing/closing csv file containing accessibility measures
			String matsimOutputDirectory = event.getControler().getScenario().getConfig().controler().getOutputDirectory();
			urbanSimZoneCSVWriterV2.close(matsimOutputDirectory);
			
			if (this.benchmark != null && benchmarkID > 0) {
				this.benchmark.stoppMeasurement(benchmarkID);
				log.info("Accessibility computation with " 
						+ measuringPoints.getFacilities().size()
						+ " zones (origins) and "
						+ this.aggregatedOpportunities.length
						+ " destinations (opportunities) took "
						+ this.benchmark.getDurationInSeconds(benchmarkID)
						+ " seconds ("
						+ this.benchmark.getDurationInSeconds(benchmarkID)
						/ 60. + " minutes).");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the measured accessibility for the current measurePoint instantly
	 * to disc in csv format.
	 * 
	 * @param measurePoint
	 * @param fromNode
	 * @param freeSpeedAccessibility
	 * @param carAccessibility
	 * @param bikeAccessibility
	 * @param walkAccessibility
	 * @param accCsvWriter
	 */
	@Override
	protected void writeCSVData4Urbansim( ActivityFacility measurePoint, Node fromNode, Map<Modes4Accessibility,Double> accessibilities ) {
		// (this is what, I think, writes the urbansim data, and should thus better not be touched. kai, feb'14)
		
		// writing accessibility measures of current measurePoint in csv format
		// The UrbanSimZoneCSVWriterV2 writer produces URBANSIM INPUT
		urbanSimZoneCSVWriterV2.write(measurePoint, accessibilities ) ;
	}

}

/* *********************************************************************** *
 * project: org.matsim.*
 * CalcLegTimes.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.analysis.kai;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.roadpricing.RoadPricingConfigGroup;
import org.matsim.roadpricing.RoadPricingReaderXMLv1;
import org.matsim.roadpricing.RoadPricingSchemeImpl;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author knagel, originally based on
 * @author mrieser
 */
public class KNAnalysisEventsHandler implements 
PersonDepartureEventHandler, PersonArrivalEventHandler, 
PersonMoneyEventHandler, 
LinkLeaveEventHandler, LinkEnterEventHandler, 
PersonLeavesVehicleEventHandler, PersonEntersVehicleEventHandler {

	private final static Logger log = Logger.getLogger(KNAnalysisEventsHandler.class);

	public static final String PAYMENTS = "payments";
	public static final String TRAV_TIME = "travTime" ;
	public static final String SUBPOPULATION = "subpopulation" ; // subpopulationAttributeName

	private Scenario scenario = null ;
	private Population population = null;
	private final TreeMap<Id, Double> agentDepartures = new TreeMap<Id, Double>();
	private final TreeMap<Id, Integer> agentLegs = new TreeMap<Id, Integer>();

	// statistics types:
	enum StatType { durations, durationsOtherBins, beelineDistances, legDistances, scores, payments } ;

	// container that contains the statistics containers:
	private final Map<StatType,Map<String,int[]>> statsContainer = new TreeMap<StatType,Map<String,int[]>>() ;
	// yy should probably be "double" instead of "int" (not all data are integer counts; think of emissions).  kai, jul'11

	// container that contains the data bin boundaries (arrays):
	private final Map<StatType,double[]> dataBoundaries = new TreeMap<StatType,double[]>() ;

	// container that contains the sum (to write averages):
	private final Map<StatType,Map<String,Double>> sumsContainer = new TreeMap<StatType,Map<String,Double>>() ;

	private double controlStatisticsSum;
	private double controlStatisticsCnt;

	private  Set<Id> tolledLinkIds = new HashSet<Id>() ;
	// (initializing with empty set, meaning output will say no vehicles at gantries).

	// general trip counter.  Would, in theory, not necessary to do this per StatType, but I find it too brittle 
	// to avoid under- or over-counting with respect to loops.
	//	private final Map<StatType,Integer> legCount = new TreeMap<StatType,Integer>() ;

	public KNAnalysisEventsHandler(final Scenario scenario) {
		this.scenario = scenario ;
		this.population = scenario.getPopulation() ;

		final String tollLinksFileName = ConfigUtils.addOrGetModule(this.scenario.getConfig(), RoadPricingConfigGroup.GROUP_NAME, RoadPricingConfigGroup.class).getTollLinksFile();
		if ( tollLinksFileName != null && !tollLinksFileName.equals("") ) {
			RoadPricingSchemeImpl scheme = new RoadPricingSchemeImpl();
			RoadPricingReaderXMLv1 rpReader = new RoadPricingReaderXMLv1(scheme);
			try {
				rpReader.parse( tollLinksFileName  );
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			this.tolledLinkIds = scheme.getTolledLinkIds() ;
		}


		for ( StatType type : StatType.values() ) {

			// instantiate the statistics containers:
			Map<String,int[]> legStats = new TreeMap<String,int[]>() ;
			this.statsContainer.put( type, legStats ) ;

			Map<String,Double> sums = new TreeMap<String,Double>() ;
			this.sumsContainer.put( type, sums ) ;

			// define the bin boundaries:
			switch ( type ) {
			case beelineDistances: {
				double[] dataBoundariesTmp = {0., 100., 200., 500., 1000., 2000., 5000., 10000., 20000., 50000., 100000.} ;
				dataBoundaries.put( type, dataBoundariesTmp ) ;
				break; }
			case durations: {
				double[] dataBoundariesTmp = {0., 300., 600., 900., 1200., 1500., 1800., 2100., 2400., 2700., 3000., 3300., 3600., 
						3900., 4200., 4500., 4800., 5100., 5400., 5700., 6000., 6300., 6600., 6900., 7200.} ;
				dataBoundaries.put( type, dataBoundariesTmp ) ;
				break; }
			case durationsOtherBins: {
				double[] dataBoundariesTmp = {0., 300., 900., 1800., 2700., 3600.} ;
				dataBoundaries.put( type, dataBoundariesTmp ) ;
				break; }
			case legDistances: {
				double[] dataBoundariesTmp = {0., 1000, 3000, 10000, 30000, 10000, 300000, 1000.*1000. } ;
				dataBoundaries.put( type, dataBoundariesTmp ) ;
				break; }
			case scores:{
				double[] dataBoundariesTmp = {Double.NEGATIVE_INFINITY} ; // yy ??
				dataBoundaries.put( type, dataBoundariesTmp ) ;
				break; }
			case payments:{
				double[] dataBoundariesTmp = {Double.NEGATIVE_INFINITY } ; // yy ??
				dataBoundaries.put( type, dataBoundariesTmp ) ;
				break; }
			default:
				throw new RuntimeException("statistics container for type "+type.toString()+" not initialized.") ;
			}
		}

		// initialize everything (in the same way it is done between iterations):
		reset(-1) ;
	}

	private int getIndex( StatType type, final double dblVal) {
		double[] dataBoundariesTmp = dataBoundaries.get(type) ;
		int ii = dataBoundariesTmp.length-1 ;
		for ( ; ii>=0 ; ii-- ) {
			if ( dataBoundariesTmp[ii] <= dblVal ) 
				return ii ;
		}
		log.warn("leg statistics contains value that smaller than the smallest category; adding it to smallest category" ) ;
		log.warn("statType: " + type + "; val: " + dblVal ) ;
		return 0 ;
	}

	@Override
	public void handleEvent(final PersonDepartureEvent event) {
		this.agentDepartures.put(event.getPersonId(), event.getTime());
		Integer cnt = this.agentLegs.get(event.getPersonId());
		if (cnt == null) {
			this.agentLegs.put(event.getPersonId(), 1);
		} else {
			this.agentLegs.put(event.getPersonId(), Integer.valueOf(1 + cnt.intValue()));
		}
	}

	private static int noCoordCnt = 0 ;
	private static int noDistanceCnt = 0 ;

	@Override
	public void handleEvent(final PersonArrivalEvent event) {
		Double depTime = this.agentDepartures.remove(event.getPersonId());
		Person person = this.population.getPersons().get(event.getPersonId());
		if (depTime != null && person != null) {
			double travTime = event.getTime() - depTime;

			controlStatisticsSum += travTime ;
			controlStatisticsCnt ++ ;

			add(person,travTime, TRAV_TIME) ;

			int legNr = this.agentLegs.get(event.getPersonId());
			Plan plan = person.getSelectedPlan();
			int index = (legNr - 1) * 2;
			final Activity fromAct = (Activity)plan.getPlanElements().get(index);
			final Leg leg = (Leg)plan.getPlanElements().get(index+1) ;
			final Activity toAct = (Activity)plan.getPlanElements().get(index + 2);

			// this defines to which legTypes this leg should belong for the statistical averaging:
			List<String> legTypes = new ArrayList<String>() ;

			// register the leg by activity type pair:
			legTypes.add(fromAct.getType() + "---" + toAct.getType()) ;

			// register the leg by mode:
			legTypes.add("zz_mode_" + leg.getMode()) ;

			// register the leg by subpop type:
			legTypes.add( this.getSubpopName(person) ) ;


			// register the leg for the overall average:
			legTypes.add("zzzzzzz_all") ;
			// (reason for so many "zzz": make entry long enough for the following tab)
			// (This works because now ALL legs will be of legType="zzzzzzz_all".)

			// go through all types of statistics that are generated ...
			for ( StatType statType : StatType.values() ) {

				// .. generate correct "item" for statType ...
				double item = 0. ;
				switch( statType) {
				case durations:
				case durationsOtherBins:
					item = travTime ;
					break;
				case beelineDistances:
					if ( fromAct.getCoord()!=null && toAct.getCoord()!=null ) {
						item = CoordUtils.calcDistance(fromAct.getCoord(), toAct.getCoord()) ;
					} else {
						if ( noCoordCnt < 1 ) {
							noCoordCnt ++ ;
							log.warn("either fromAct or to Act has no Coord; using link coordinates as substitutes.\n" + Gbl.ONLYONCE ) ;
						}
						Link fromLink = scenario.getNetwork().getLinks().get( fromAct.getLinkId() ) ;
						Link   toLink = scenario.getNetwork().getLinks().get(   toAct.getLinkId() ) ;
						item = CoordUtils.calcDistance( fromLink.getCoord(), toLink.getCoord() ) ; 
					}
					break;
				case legDistances:
					if ( leg.getRoute() instanceof NetworkRoute ) {
						item = RouteUtils.calcDistance( ((NetworkRoute)leg.getRoute()), this.scenario.getNetwork() ) ;
					} else if ( leg.getRoute()!=null && !Double.isNaN( leg.getRoute().getDistance() ) )  {
						item = leg.getRoute().getDistance() ;
					} else {
						if ( noDistanceCnt < 10 ) {
							noDistanceCnt++ ;
							log.warn("cannot get leg distance for arrival event") ;
							log.warn( "person: " + person.toString() ) ;
							log.warn( "leg: " + leg.toString() ) ;
							if ( noDistanceCnt==10 ) {
								log.warn( Gbl.FUTURE_SUPPRESSED ) ;
							}
						}
					}
					break;
				case payments:
				case scores:
					break ;
				default:
					throw new RuntimeException("`item' for statistics type not defined; statistics type: " + statType ) ;
				}

				addItemToAllRegisteredTypes(legTypes, statType, item);
			}

		}
	}

	private String getSubpopName(Person person) {
		return "yy_" + getSubpopName( person.getId(), this.population.getPersonAttributes(), this.scenario.getConfig().plans().getSubpopulationAttributeName() ) ;
	}
	public static final String getSubpopName( Id personId, ObjectAttributes personAttributes, String subpopAttrName ) {
		String subpop = (String) personAttributes.getAttribute( personId.toString(), subpopAttrName ) ;
		return "subpop_" + subpop;
	}

	private void addItemToAllRegisteredTypes(List<String> legTypes, StatType statType, double item) {
		// ... go through all legTypes to which the leg belongs ...
		for ( String legType : legTypes ) {

			// ... get correct statistics array by statType and legType ...
			int[] stats = this.statsContainer.get(statType).get(legType);

			// ... if that statistics array does not exist yet, initialize it ...
			if (stats == null) {
				Integer len = this.dataBoundaries.get(statType).length ;
				stats = new int[len];
				for (int i = 0; i < len; i++) {
					stats[i] = 0;
				}
				this.statsContainer.get(statType).put(legType, stats);

				// ... also initialize the sums container ...
				this.sumsContainer.get(statType).put(legType, 0.) ;
			}

			// ... finally add the "item" to the correct bin in the container:
			stats[getIndex(statType,item)]++;

			double newItem = this.sumsContainer.get(statType).get(legType) + item ;
			this.sumsContainer.get(statType).put( legType, newItem ) ;

		}
	}

	@Override
	public void reset(final int iteration) {
		this.agentDepartures.clear();
		this.agentLegs.clear();

		for ( StatType type : StatType.values() ) {
			this.statsContainer.get(type).clear() ;
			this.sumsContainer.get(type).clear() ;
		}

		for ( Person person : this.scenario.getPopulation().getPersons().values() ) {
			ObjectAttributes attribs = this.scenario.getPopulation().getPersonAttributes() ;
			attribs.putAttribute( person.getId().toString(), TRAV_TIME, 0. ) ;
			if ( attribs.getAttribute( person.getId().toString(), PAYMENTS) != null ) {
				attribs.putAttribute( person.getId().toString(), PAYMENTS, 0. ) ;
			}
		}
		// (yy not sure if I like the above; might be better to just use a local data structure. kai, may'14)

		controlStatisticsSum = 0. ;
		controlStatisticsCnt = 0. ;

	}

	@Override
	public void handleEvent(PersonMoneyEvent event) {
		List<String> legTypes = new ArrayList<String>() ;

		final Population pop = this.scenario.getPopulation();
		Person person = pop.getPersons().get( event.getPersonId() ) ;
		legTypes.add( this.getSubpopName(person)) ;

		double item = - event.getAmount() ;

		this.addItemToAllRegisteredTypes(legTypes, StatType.payments, item);
		// (this is not additive by person, but it is additive by legType.  So if a person has multiple money events, they
		// are added up in the legType category.  kai, feb'14)


		add(person, item, PAYMENTS);
	}

	private void add(Person person, double val, final String attributeName) {
		final ObjectAttributes pAttribs = this.scenario.getPopulation().getPersonAttributes();
		Double oldVal = (Double) pAttribs.getAttribute( person.getId().toString(), attributeName) ;
		double newVal = val ;
		if ( oldVal!=null ) {
			newVal += oldVal ;
		}
		pAttribs.putAttribute( person.getId().toString(), attributeName, newVal ) ; 
	}

	public void writeStats(final String filenameTmp) {
		final Population pop = this.scenario.getPopulation();

		// score statistics:
		for ( Person person : pop.getPersons().values() ) {
			// this defines to which categories this person should belong for the statistical averaging:
			List<String> categories = new ArrayList<String>() ;

			categories.add( this.getSubpopName(person) ) ;

			// register the leg for the overall average:
			categories.add("zzzzzzz_all") ;

			Double item = person.getSelectedPlan().getScore() ;
			this.addItemToAllRegisteredTypes(categories, StatType.scores, item);
		}

		// write population attributes:
		new ObjectAttributesXmlWriter(pop.getPersonAttributes()).writeFile("extendedPersonAttributes.xml.gz");

		//write statistics:
		for ( StatType type : StatType.values() ) {
			String filename = filenameTmp + type.toString() + ".txt" ;
			BufferedWriter legStatsFile = IOUtils.getBufferedWriter(filename);
			writeStatsHorizontal(type, legStatsFile );
			try {
				legStatsFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// toll analysis:
		double maxPayment = Double.NEGATIVE_INFINITY ;
		Set<String> subPopTypes = new HashSet<String>() ;
		for ( Person person : pop.getPersons().values() ) {
			Double payment = (Double) pop.getPersonAttributes().getAttribute( person.getId().toString(), PAYMENTS ) ;
			if ( payment==null ) continue ;
			if ( payment > maxPayment ) {
				maxPayment = payment ;
			}
			String subPopType = (String) pop.getPersonAttributes().getAttribute( person.getId().toString(), SUBPOPULATION ) ;
			subPopTypes.add(subPopType) ;
		}

		final int nBins = 100 ;
		final double binSize = maxPayment/nBins ; // not so great for commercial vs. private

		Map<String,double[]> sum = new HashMap<String,double[]>();
		Map<String,double[]> cnt = new HashMap<String,double[]>();
		for ( String subPopType : subPopTypes ) {
			sum.put( subPopType, new double[nBins+1] ) ;
			cnt.put( subPopType, new double[nBins+1] ) ;
		}

		for ( Person person : pop.getPersons().values() ) {
			String subPopType = (String) pop.getPersonAttributes().getAttribute( person.getId().toString(), SUBPOPULATION ) ;
			Double payment = (Double) pop.getPersonAttributes().getAttribute( person.getId().toString(), PAYMENTS ) ;
			if (payment==null) continue ;
			int bin = (int) (payment/binSize) ;
			sum.get(subPopType)[bin] += payment ;
			cnt.get(subPopType)[bin] ++ ;
		}

		try {
			for ( String subPopType : subPopTypes ) {
				double sum2 = 0. ;
				final String filename = filenameTmp + "payment_" + subPopType.toString() + ".txt" ;
				BufferedWriter out = IOUtils.getBufferedWriter(filename) ;
				out.write( 0 + "\t" + 0 + "\n" ) ;
				for ( int ii=0 ; ii<cnt.get(subPopType).length ; ii++ ) {
					if ( cnt.get(subPopType)[ii] > 0 ) {
						sum2 += sum.get(subPopType)[ii] ;
						out.write( sum.get(subPopType)[ii]/cnt.get(subPopType)[ii] + "\t" + sum2 + "\n") ;
					}
				}
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		// write link statistics:
		for ( Entry<Id, Double> entry : this.linkCnts.entrySet() ) {
			final Id linkId = entry.getKey();
			linkAttribs.putAttribute(linkId.toString(), CNT, entry.getValue().toString() ) ;
			linkAttribs.putAttribute(linkId.toString(), TTIME_SUM, this.linkTtimesSums.get(linkId).toString() ) ;
		}
		new ObjectAttributesXmlWriter( this.linkAttribs ).writeFile("networkAttributes.xml.gz");

		{
			BufferedWriter writer = IOUtils.getBufferedWriter("gantries.txt") ;
			for (  Entry<Id, Double> entry : this.vehicleGantryCounts.entrySet() ) {
				try {
					writer.write( entry.getKey() + "\t" + entry.getValue() + "\t 1 \n") ; // the "1" makes automatic processing a bit easier. kai, mar'14
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private ObjectAttributes linkAttribs = new ObjectAttributes() ;
	public static final String CNT = "cnt" ;
	public static final String TTIME_SUM = "ttimeSum" ;

	private void writeStatsHorizontal(StatType statType, final java.io.Writer out ) throws UncheckedIOException {
		try {
			boolean first = true;
			for (Map.Entry<String, int[]> entry : this.statsContainer.get(statType).entrySet()) {
				String legType = entry.getKey();
				int[] counts = entry.getValue();

				// header line etc:
				if (first) {
					first = false;
					out.write(statType.toString());
					for (int i = 0; i < counts.length; i++) {
						out.write("\t" + this.dataBoundaries.get(statType)[i] + "+" ) ;
					}
					out.write("\t|\t average \t|\t cnt \t | \t sum\n");
				}

				// data:
				int cnt = 0 ;
				out.write(legType);
				for (int i = 0; i < counts.length; i++) {
					out.write("\t" + counts[i]);
					cnt += counts[i] ;
				}
				out.write("\t|\t" + this.sumsContainer.get(statType).get(legType)/cnt ) ;
				out.write("\t|\t" + cnt  ) ;
				out.write("\t|\t" + this.sumsContainer.get(statType).get(legType) + "\n" ) ;

			}
			out.write("\n");

			if ( first ) { // means there was no data
				out.write("no legs, therefore no data") ;
				out.write("\n");
			}

			switch( statType ) {
			case durations:
			case durationsOtherBins:
				out.write("control statistics: average ttime = " + (controlStatisticsSum/controlStatisticsCnt) ) ;
				out.write("\n");
				out.write("\n");
				break;
			case beelineDistances:
				break;
			default:
				break;
			}

		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} finally {
			try {
				out.flush();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}

	private Map<Id,Double> vehicleEnterTimes = new HashMap<Id,Double>() ;

	private Map<Id,Double> vehicleGantryCounts = new HashMap<Id,Double>() ;

	@Override
	public void handleEvent(LinkEnterEvent event) {
		vehicleEnterTimes.put( event.getVehicleId(), event.getTime() ) ;

		if ( this.tolledLinkIds.contains( event.getLinkId() ) ) {
			final Double gantryCountSoFar = this.vehicleGantryCounts.get( event.getVehicleId() ) ;
			if ( gantryCountSoFar==null ) {
				this.vehicleGantryCounts.put( event.getVehicleId(), 1. ) ;
			} else {
				this.vehicleGantryCounts.put( event.getVehicleId(), 1. + gantryCountSoFar ) ;
			}
		}
	}

	private Map<Id,Double> linkTtimesSums = new HashMap<Id,Double>() ;
	private Map<Id,Double> linkCnts = new HashMap<Id,Double>() ;

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		Double enterTime = vehicleEnterTimes.get( event.getVehicleId() ) ;
		if ( enterTime != null && enterTime < 9.*3600. ) {
			final Id linkId = event.getLinkId();
			final Double sumSoFar = linkTtimesSums.get( linkId );
			if ( sumSoFar == null ) {
				linkTtimesSums.put( linkId, event.getTime() - enterTime ) ;
				linkCnts.put( linkId, 1. ) ; 
			} else {
				linkTtimesSums.put( linkId, event.getTime() - enterTime + sumSoFar ) ;
				linkCnts.put( linkId, 1. + linkCnts.get(linkId) ) ;
			}
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		// do we need to do anything here?
	}

	private static int wrnCnt = 0 ;
	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		Double result = vehicleEnterTimes.remove( event.getVehicleId() ) ;
		if ( result == null ) {
			if ( wrnCnt==0 ) {
				wrnCnt++ ;
				//				throw new RuntimeException("vehicle arrival for vehicle that never entered link.  teleportation?") ;
				Logger.getLogger(this.getClass()).warn("vehicle arrival for vehicle that never entered link.  I think this can happen with departures "
						+ "that have empty routes, i.e. go to a location on the same link. kai, may'14");
				Logger.getLogger(this.getClass()).warn( Gbl.ONLYONCE ) ;
			}
		}
	}

}

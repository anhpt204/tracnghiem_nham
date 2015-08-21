/* *********************************************************************** *
 * project: org.matsim.													   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,     *
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

/**
 * 
 */
package org.matsim.contrib.accessibility.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.matrixbasedptrouter.utils.TempDirectoryUtil;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.roadpricing.RoadPricingScheme;
import org.matsim.roadpricing.RoadPricingSchemeImpl;
import org.matsim.roadpricing.RoadPricingSchemeImpl.Cost;
import org.matsim.utils.LeastCostPathTree;

/**
 * This runs the normal LeastCostPathTree, with whatever TravelDisutility object it is using (e.g. including distance and/or toll).
 * However, since the normal LeastCostPathTree does not return its contributing cost dimensions, this class here is used to also
 * accumulate distance and toll separately along the route.
 * <p/>
 * To re-iterate: This class does <i>not</i> lead to a different tree than the base class.
 * 
 * @author thomas
 * 
 */
public final class LeastCostPathTreeExtended extends LeastCostPathTree{
	
	protected static final Logger log = Logger.getLogger(LeastCostPathTreeExtended.class);
	
	private Map<Id, NodeDataExtended> nodeDataExt = null;
	private RoadPricingScheme scheme = null;
	
	/**
	 * constructor
	 * @param controler Controler, to get the RoadPricingScheme if available
	 */
	public LeastCostPathTreeExtended(final TravelTime tt, final TravelDisutility td, final RoadPricingScheme scheme) {
		super(tt, td);
		this.scheme = scheme;
	}

	/**
	 * 
	 * @param network
	 * @param origin
	 * @param time
	 */
	public final void calculateExtended(final Network network, final Node origin, final double time) {
		
		this.nodeDataExt = new ConcurrentHashMap<Id, NodeDataExtended>((int) (network.getNodes().size() * 1.1), 0.95f);
		if(this.nodeDataExt.get(origin.getId()) == null){
			NodeDataExtended nde = new NodeDataExtended();
			nde.distance = 0.;
			nde.toll 	 = 0.;
			this.nodeDataExt.put(origin.getId(), nde);
		}
		
		calculate(network, origin, time);
	}
	
	/**
	 * @param link  
	 * @param currTime 
	 */
	@Override
	protected final void additionalComputationsHook( final Link link, final double currTime ) {
		
		Node fromNode = link.getFromNode();
		// get current distance and toll so far
		NodeDataExtended nde = nodeDataExt.get( fromNode.getId() );
		double currDistance = nde.getDistance();
		double currToll = nde.getToll();

		// query toll
		double toll = 0.;
		if(this.scheme != null){
			Cost cost = scheme.getLinkCostInfo(link.getId(), currTime, null, null);
			if(cost != null)
				toll = cost.amount;
		}
		
		double visitDistance = currDistance + link.getLength();
		double visitToll = currToll + toll;
		
		// put new nodes into nodeDataExtended
		Node toNode = link.getToNode();
		NodeDataExtended ndeNew = this.nodeDataExt.get( toNode.getId() );
		if(ndeNew == null){
			ndeNew = new NodeDataExtended();
			this.nodeDataExt.put( toNode.getId(), ndeNew);
		}
		ndeNew.visit(visitDistance, visitToll);
	}
	
	// ////////////////////////////////////////////////////////////////////
	// get methods
	// ////////////////////////////////////////////////////////////////////
	
	public final Map<Id, NodeDataExtended> getTreeExtended() {
		return this.nodeDataExt;
	}
	
	// ////////////////////////////////////////////////////////////////////
	// inner classes
	// ////////////////////////////////////////////////////////////////////
	
	public static class NodeDataExtended {
		private double distance = 0.;	// meter
		private double toll 	= 0.; 	// money

		/*package*/ final void reset() {
			this.distance 	= 0.;
			this.toll 		= 0.;
		}

		final void visit(final double distance, final double toll) {
			this.distance 	= distance;
			this.toll 		= toll;
		}

		public final double getDistance() {
			return this.distance;
		}

		public final double getToll() {
			return this.toll;
		}
	}
	
	// ////////////////////////////////////////////////////////////////////
	// testing 
	// ////////////////////////////////////////////////////////////////////
	
	/**
	 * for testing
	 * @param args
	 */
	public static void main(String args[]){
		TempDirectoryUtil tempDirectoryUtil = new TempDirectoryUtil() ;

		// create temp output dir
		String tmpOutputLocation = tempDirectoryUtil.createCustomTempDirectory("test");
		
		// create network
		NetworkImpl network = LeastCostPathTreeExtended.createTriangularNetwork();
		// create scenario
		Config config = ConfigUtils.createConfig();
		// set last iteration and output
		ControlerConfigGroup controlerCG = (ControlerConfigGroup) config.getModule(ControlerConfigGroup.GROUP_NAME);
		controlerCG.setLastIteration( 1 );
		controlerCG.setOutputDirectory( tmpOutputLocation );
		// set scenario
		ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario( config );
		scenario.setNetwork( LeastCostPathTreeExtended.createTriangularNetwork() );
		Controler controler = new Controler(scenario);
		controler.run();
		// init lcpte
		LeastCostPathTreeExtended lcpte = new LeastCostPathTreeExtended(controler.getLinkTravelTimes(), controler.createTravelDisutilityCalculator(), (RoadPricingSchemeImpl) controler.getScenario().getScenarioElement(RoadPricingScheme.ELEMENT_NAME));
		
		// contains all network nodes
		Map<Id, Node> networkNodesMap = network.getNodes();
		Id originNodeID = new IdImpl(1);
		Id destinationNodeId = new IdImpl(4);
		// run lcpte
		lcpte.calculateExtended(network, networkNodesMap.get( originNodeID ), 3600.);
		double time = lcpte.getTree().get( destinationNodeId ).getTime();
		double disutility = lcpte.getTree().get( destinationNodeId ).getCost();
		double distance = lcpte.getTreeExtended().get( destinationNodeId ).getDistance();
		double toll = lcpte.getTreeExtended().get( destinationNodeId ).getToll();
		
		log.info("Time = " + time);
		log.info("Disutility = " + disutility);		
		log.info("Distance = " + distance );
		log.info("Toll = " + toll);
		
		tempDirectoryUtil.cleanUpCustomTempDirectories();
	}
	
	/**
	 * creating a test network
	 * the path 1,2,4 has a total length of 1000m with a free speed travel time of 10m/s
	 * the second path 1,3,4 has a total length of 100m but only a free speed travel time of 0.1m/s
	 */
	private static NetworkImpl createTriangularNetwork() {
		/*
		 * 			(2)
		 *         /   \
		 *        /     \
		 *(10m/s)/       \(10m/s)
		 *(500m)/	      \(500m)
		 *     /           \
		 *    /             \
		 *	 /               \
		 *(1)-------(3)-------(4)
		 *(50m,0.1m/s)(50m,0.1m/s) 			
		 */
		ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());

		NetworkImpl network = (NetworkImpl) scenario.getNetwork();
		
		// add nodes
		Node node1 = network.createAndAddNode(new IdImpl(1), scenario.createCoord(0, 0));
		Node node2 = network.createAndAddNode(new IdImpl(2), scenario.createCoord(50, 100));
		Node node3 = network.createAndAddNode(new IdImpl(3), scenario.createCoord(50, 0));
		Node node4 = network.createAndAddNode(new IdImpl(4), scenario.createCoord(100, 0));

		// add links
		network.createAndAddLink(new IdImpl(1), node1, node2, 500.0, 10.0, 3600.0, 1);
		network.createAndAddLink(new IdImpl(2), node2, node4, 500.0, 10.0, 3600.0, 1);
		network.createAndAddLink(new IdImpl(3), node1, node3, 50.0, 0.1, 3600.0, 1);
		network.createAndAddLink(new IdImpl(4), node3, node4, 50.0, 0.1, 3600.0, 1);
		
		return network;
	}
	
}

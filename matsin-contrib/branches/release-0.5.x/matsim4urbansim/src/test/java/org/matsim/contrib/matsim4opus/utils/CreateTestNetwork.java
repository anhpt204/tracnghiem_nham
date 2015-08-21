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
package org.matsim.contrib.matsim4opus.utils;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.matsim4opus.constants.InternalConstants;
import org.matsim.contrib.matsim4opus.utils.io.TempDirectoryUtil;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.io.IOUtils;

/**
 * This class creates a simple test network to test for example the pt simulation in MATSim.
 * 
 * @author thomas
 * @author tthunig
 */
public class CreateTestNetwork {
	
	/**
	 * This method creates a test network. It is used for example in PtMatrixTest.java to test the pt simulation in MATSim.
	 * The network has 9 nodes and 8 links (see the sketch below).
	 * 
	 * @return the created test network
	 */
	public static NetworkImpl createTestNetwork() {

		/*
		 * (2)		(5)------(8)
		 * 	|		 |
		 * 	|		 |
		 * (1)------(4)------(7)
		 * 	|		 |
		 * 	|		 |
		 * (3)		(6)------(9)
		 */
		double freespeed = 2.7;	// this is m/s and corresponds to 50km/h
		double capacity = 500.;
		double numLanes = 1.;

		ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());

		NetworkImpl network = (NetworkImpl) scenario.getNetwork();

		// add nodes
		Node node1 = network.createAndAddNode(new IdImpl(1), scenario.createCoord(0, 100));
		Node node2 = network.createAndAddNode(new IdImpl(2), scenario.createCoord(0, 200));
		Node node3 = network.createAndAddNode(new IdImpl(3), scenario.createCoord(0, 0));
		Node node4 = network.createAndAddNode(new IdImpl(4), scenario.createCoord(100, 100));
		Node node5 = network.createAndAddNode(new IdImpl(5), scenario.createCoord(100, 200));
		Node node6 = network.createAndAddNode(new IdImpl(6), scenario.createCoord(100, 0));
		Node node7 = network.createAndAddNode(new IdImpl(7), scenario.createCoord(200, 100));
		Node node8 = network.createAndAddNode(new IdImpl(8), scenario.createCoord(200, 200));
		Node node9 = network.createAndAddNode(new IdImpl(9), scenario.createCoord(200, 0));

		// add links (bi-directional)
		network.createAndAddLink(new IdImpl(1), node1, node2, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(2), node2, node1, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(3), node1, node3, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(4), node3, node1, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(5), node1, node4, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(6), node4, node1, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(7), node4, node5, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(8), node5, node4, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(9), node4, node6, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(10), node6, node4, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(11), node4, node7, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(12), node7, node4, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(13), node5, node8, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(14), node8, node5, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(15), node6, node9, 100, freespeed, capacity, numLanes);
		network.createAndAddLink(new IdImpl(16), node9, node6, 100, freespeed, capacity, numLanes);

		return network;
	}
	
	/**
	 * This method creates 4 pt stops for the test network from createTestNetwork().
	 * The information about the coordinates will be written to a csv file.
	 * The 4 pt stops are located as a square in the coordinate plane with a side length of 180 meter (see the sketch below).
	 *  
	 * @return the location of the written csv file
	 */
	public static String createTestPtStationCSVFile(){
		
		/*
		 * (2)	    (5)------(8)
		 * 	|(pt2)   |   (pt3)
		 * 	|		 |
		 * (1)------(4)------(7)
		 * 	|		 |
		 * 	|(pt1)   |   (pt4)
		 * (3)      (6)------(9)
		 */
		
		String location = TempDirectoryUtil.createCustomTempDirectory("ptStopFileDir")  + "/ptStops.csv";
		BufferedWriter bw = IOUtils.getBufferedWriter(location);
		
		try{
			bw.write("id,x,y" + InternalConstants.NEW_LINE); 	// header
			bw.write("1,10,10" + InternalConstants.NEW_LINE);	// pt stop next to node (3)
			bw.write("2,10, 190" + InternalConstants.NEW_LINE); // pt stop next to node (2)
			bw.write("3,190,190" + InternalConstants.NEW_LINE); // pt stop next to node (8)
			bw.write("4,190,10" + InternalConstants.NEW_LINE);  // pt stop next to node (9)
			bw.flush();
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return location;
	}
	
	/**
	 * This methods creates a csv file with informations about pt travel times and pt distances for the test network from createTestNetwork().
	 * We set the pt travel time between all pairs of pt stops to 100 seconds, except pairs of same pt stops where the travel time is 0 seconds.
	 * We set the pt distance between all pairs of pt stops to 100 meter, except pairs of same pt stops where the distance is 0 meter.
	 * Because the data in the csv file does not need an entity, you can use the same csv file for both informations.
	 * 
	 * @return the location of the written file
	 */
	public static String createTestPtTravelTimesAndDistancesCSVFile(){
		
		// set dummy travel times or distances to all possible pairs of pt stops
		
		String location = TempDirectoryUtil.createCustomTempDirectory("ptStopFileDir")  + "/ptTravelInfo.csv";
		BufferedWriter bw = IOUtils.getBufferedWriter(location);
		
		try{
			for (int origin = 1; origin <= 4; origin++){
				for (int destination = 1; destination <= 4; destination++){
					if (origin == destination)
						// set a travel time/distance of 0s or 0m between same origin and destination pt stops
						bw.write(origin + " " + destination + " 0" + InternalConstants.NEW_LINE);
					else
						// set a dummy travel time/distance of 100s or 100m between different origin and destination pt stops
						bw.write(origin + " " + destination + " 100" + InternalConstants.NEW_LINE); 
				}
			}
			bw.flush();
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return location;
	}
	
	/**
	 * This method creates 4 facilities for the test network from createTestNetwork().
	 * The distance between each facility and the nearest pt stop is 50 meter (see the sketch below).
	 * 
	 * @return the facility list
	 */
	public static List<Coord> getTestFacilityLocations(){
		
		/*    B             C
		 * (2)		(5)------(8)
		 * 	|		 |
		 * 	|		 |    
		 * (1)------(4)------(7)
		 * 	|		 |           
		 * 	|		 |
		 * (3)		(6)------(9)
		 *    A             D
		 */   
		
		List<Coord> facilityList = new ArrayList<Coord>();
		facilityList.add(new CoordImpl(10, -40));  // 50m to pt station 1
		facilityList.add(new CoordImpl(10, 240));  // 50m to pt station 2
		facilityList.add(new CoordImpl(190, 240)); // 50m to pt station 3
		facilityList.add(new CoordImpl(190, -40)); // 50m to pt station 4
		return facilityList;
	}
	
}

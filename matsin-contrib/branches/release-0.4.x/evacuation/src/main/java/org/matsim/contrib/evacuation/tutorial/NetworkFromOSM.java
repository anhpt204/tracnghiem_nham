/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkFromOSMLoader.java
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
package org.matsim.contrib.evacuation.tutorial;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.core.config.ConfigUtils;
import org.xml.sax.SAXException;

/**
 * This class generates a <code>network.xml</code> from an openstreemap XML file
 * The path to osm file and <code>network.xml</code> has to be adapted to your
 * environment
 * 
 * @author laemmel
 * 
 */
public class NetworkFromOSM {

	public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException {
		if (args.length != 2) {
			printHelp();
			System.exit(-1);
		}
		String osm = args[0];
		String network = args[1];
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		Network net = sc.getNetwork();
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.WGS84_UTM33N);
		OsmNetworkReader onr = new OsmNetworkReader(net, ct);
		onr.parse(osm);
		new NetworkWriter(net).write(network);
	}

	/**
	 * 
	 */
	private static void printHelp() {
		System.out.println("NetworkFromOSM usage:");
		System.out.println("java -cp <MATSim release file> org.matsim.evacuation.tutorial.NetworkFromOSM <path to osm input file> <path to network.xml output file>");

	}

}

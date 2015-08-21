/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.experimental.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.utils.gis.matsim2esri.network.FeatureGeneratorBuilderImpl;
import org.matsim.utils.gis.matsim2esri.network.LanesBasedWidthCalculator;
import org.matsim.utils.gis.matsim2esri.network.LineStringBasedFeatureGenerator;
import org.matsim.utils.gis.matsim2esri.network.Links2ESRIShape;
import org.matsim.utils.gis.matsim2esri.network.PolygonFeatureGenerator;

@Deprecated
public class EvacuationNetworkGenerator {

	private static final Logger log = Logger.getLogger(EvacuationNetworkGenerator.class);

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Usage:");
			System.out.println("EvacuationNetworkGenerator osm-file output-path EPSG-Code");
			System.exit(0);
		}

		String osm = args[0];
		String outputPath = args[1];
		String epsg = args[2];

		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		Network net = sc.getNetwork();

		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, epsg); // the
																															// coordinate
																															// transformation
																															// is
																															// needed
																															// to
																															// get
																															// a
																															// projected
																															// coordinate
																															// system
		// for this basic example UTM zone 33 North is the right coordinate
		// system. This may differ depending on your scenario. See also
		// http://en.wikipedia.org/wiki/Universal_Transverse_Mercator

		log.info("Generating MATSim network");
		OsmNetworkReader onr = new OsmNetworkReader(net, ct, false); // constructs
																		// a new
																		// openstreetmap
																		// reader

		double laneCap = 1.33 * 0.71 * 3600;

		// hierarchy , type, lanes, free speed, speed factor, capacity per lane
		// and hour,
		onr.setHighwayDefaults(1, "motorway", 2 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap, true);
		onr.setHighwayDefaults(1, "motorway_link", 1 * 2 * 3.75 / 0.71, 1.66 / 3.6, 1.0, laneCap, true);
		onr.setHighwayDefaults(2, "trunk", 1 * 2 * 3.75 / 0.71, 1.66 / 3.6, 1.0, laneCap);
		onr.setHighwayDefaults(2, "trunk_link", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(3, "primary", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(3, "primary_link", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(4, "secondary", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(5, "tertiary", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(6, "minor", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(6, "unclassified", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(6, "residential", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(6, "living_street", 1 * 2 * 3.75 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(6, "pedestrian", 1 * 2 * 1.42 / 0.71, 1.66, 1.0, laneCap);
		onr.setHighwayDefaults(6, "track", 1 * 2 * 1.42 / 0.71, 1.66, 1.0, laneCap);

		onr.parse(osm); // starts the conversion from osm to matsim

		((NetworkImpl) net).setEffectiveCellSize(0.26);
		((NetworkImpl) net).setEffectiveLaneWidth(0.71);

		//
		List<Link> oneWays = new ArrayList<Link>();
		for (Link l : net.getLinks().values()) {
			Node to = l.getToNode();
			// Link oneWay = l;
			for (Link ll : to.getOutLinks().values()) {
				if (ll.getToNode() == l.getFromNode()) {
					l = null;
					break;
				}
			}
			if (l != null) {
				oneWays.add(l);
			}
		}
		log.info("detected " + oneWays.size() + " one way links");
		for (Link l : oneWays) {
			Id id = sc.createId("-" + l.getId().toString());
			((NetworkImpl) net).createAndAddLink(id, l.getToNode(), l.getFromNode(), l.getLength(), l.getFreespeed(), l.getCapacity(), l.getNumberOfLanes());
		}
		log.info("created corresponding reverse links");
		// at this point we already have a matsim network...
		new NetworkCleaner().run(net); // but may be there are isolated not
										// connected links. The network cleaner
										// removes those links

		new NetworkWriter(net).write(outputPath + "/network.xml");// here we
																	// write the
																	// network
																	// to a xml
																	// file
		log.info("done.");

		sc.getConfig().global().setCoordinateSystem(epsg);

		FeatureGeneratorBuilderImpl builder = new FeatureGeneratorBuilderImpl(net, epsg);
		builder.setWidthCoefficient(1);
		builder.setFeatureGeneratorPrototype(PolygonFeatureGenerator.class);
		builder.setWidthCalculatorPrototype(LanesBasedWidthCalculator.class);
		new Links2ESRIShape(net, outputPath + "/network.shp", builder).write();

		FeatureGeneratorBuilderImpl builder2 = new FeatureGeneratorBuilderImpl(net, epsg);
		builder2.setWidthCoefficient(1);
		builder2.setFeatureGeneratorPrototype(LineStringBasedFeatureGenerator.class);
		builder2.setWidthCalculatorPrototype(LanesBasedWidthCalculator.class);
		new Links2ESRIShape(net, outputPath + "/lines.shp", builder2).write();

	}
}

/* *********************************************************************** *
 * project: org.matsim.*
 * EvacuationAreaGenerator.java
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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.evacuation.base.EvacuationAreaFileWriter;
import org.matsim.contrib.evacuation.base.EvacuationAreaLink;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.config.ConfigUtils;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author laemmel
 * 
 */
public class EvacuationAreaGenerator {
	private static final Logger log = Logger.getLogger(EvacuationAreaGenerator.class);

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("EvacuationAreaGenerator usage:");
			System.out.println("java -cp <MATSim release file> org.matsim.evacuation.tutorial.EvacuationAreaGenerator <path to network.xml> <path to evacuationArea.shp> <path to output evacuationarea.xml>");
			System.exit(-1);
		}
		String network = args[0];
		String areaShape = args[1];
		String areaXml = args[2];

		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		Network net = sc.getNetwork();

		new MatsimNetworkReader(sc).readFile(network);
		log.info("Generating evacuation area links file.");
		FeatureSource fts = ShapeFileReader.readDataFile(areaShape);
		if (fts.getFeatures().size() > 1) {
			log.error("Evacuation zone shape file contains more than one evacuation zone! Exiting!!");
			System.exit(-1);
		}
		Feature ft = (Feature) fts.getFeatures().iterator().next();
		Geometry geo = ft.getDefaultGeometry();
		Map<Id, EvacuationAreaLink> els = new HashMap<Id, EvacuationAreaLink>();
		for (Link link : net.getLinks().values()) {
			if (geo.contains(MGC.coord2Point(link.getCoord()))) {
				EvacuationAreaLink el = new EvacuationAreaLink(link.getId().toString(), 0);
				els.put(link.getId(), el);
			}
		}
		new EvacuationAreaFileWriter(els).writeFile(areaXml);
		log.info("done.");

	}
}

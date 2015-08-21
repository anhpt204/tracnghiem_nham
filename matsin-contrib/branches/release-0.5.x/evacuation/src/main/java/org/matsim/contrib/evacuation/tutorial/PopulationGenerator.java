/* *********************************************************************** *
 * project: org.matsim.*
 * PopulationGenerator.java
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
import java.util.Random;

import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureSource;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * @author laemmel
 *
 */
public class PopulationGenerator {

	private static final Logger log = Logger.getLogger(PopulationGenerator.class);

	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.out.println("Usage:");
			System.out.println("java -cp <MATSim release file> org.matsim.evacuation.tutorial.PopulationGenerator <path to evacuationArea.shp> <path to network.xml> <path to output population.xml> <number of evacuees>");
			System.exit(-1);
		}

		String areaShp = args[0];
		String net = args[1];
		String pop = args[2];
		Integer evacs = Integer.parseInt(args[3]);

		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		NetworkImpl network = (NetworkImpl) sc.getNetwork();

		new MatsimNetworkReader(sc).readFile(net);

		SimpleFeatureSource fts = ShapeFileReader.readDataFile(areaShp);
		if (fts.getFeatures().size() > 1) {
			log.error("Evacuation zone shape file contains more than one evacuation zone! Exiting!!");
			System.exit(-1);
		}

		PopulationFactory factory = sc.getPopulation().getFactory();

		Random rnd = MatsimRandom.getRandom();

		SimpleFeature ft = fts.getFeatures().features().next();

		for (int i = 0; i < evacs; i++) {
			Person pers = factory.createPerson(sc.createId(Integer.toString(i)));
			Plan plan = factory.createPlan();
			Coord c = getRandomCoordInFeature(rnd, ft);
			Link l = network.getNearestLink(c);
			Activity act = factory.createActivityFromLinkId("h", l.getId());
			plan.addActivity(act);
			pers.addPlan(plan);
			sc.getPopulation().addPerson(pers);
		}
		new PopulationWriter(sc.getPopulation(), sc.getNetwork()).write(pop);
	}

	private static Coord getRandomCoordInFeature(Random rnd, SimpleFeature ft) {
		Point p = null;
		double x, y;
		do {
			x = ft.getBounds().getMinX() + rnd.nextDouble() * (ft.getBounds().getMaxX() - ft.getBounds().getMinX());
			y = ft.getBounds().getMinY() + rnd.nextDouble() * (ft.getBounds().getMaxY() - ft.getBounds().getMinY());
			p = MGC.xy2Point(x, y);
		} while (!((Geometry) ft.getDefaultGeometry()).contains(p));
		return new CoordImpl(p.getX(), p.getY());
	}

}

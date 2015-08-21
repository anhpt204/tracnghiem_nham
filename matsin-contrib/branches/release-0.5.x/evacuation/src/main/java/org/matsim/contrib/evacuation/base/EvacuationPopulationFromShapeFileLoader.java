/* *********************************************************************** *
 * project: org.matsim.*
 * EvacuationPopulationFromShapeFileLoader.java
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
package org.matsim.contrib.evacuation.base;

import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.evacuation.config.EvacuationConfigGroup;
import org.matsim.contrib.evacuation.config.EvacuationConfigGroup.EvacuationScenario;
import org.matsim.contrib.evacuation.flooding.FloodingInfo;
import org.matsim.contrib.evacuation.flooding.FloodingReader;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.geotools.MGC;

import com.vividsolutions.jts.geom.Envelope;

public class EvacuationPopulationFromShapeFileLoader {

	private final List<Building> buildings;

	private static final boolean EXCLUDE_SHELTER_EVACUEES = false;

	private final Population pop;
	private QuadTree<Link> quadTree;
	private final EvacuationScenario evacuationScenario;
	private final double sample;
	private final Scenario scenario;

	private QuadTree<FloodingInfo> fis = null;

	private List<FloodingReader> readers = null;

	private boolean initialized = false;

	public EvacuationPopulationFromShapeFileLoader(Population pop, List<Building> buildings, Scenario scenario) {
		this(pop, buildings, scenario, null);
	}

	public EvacuationPopulationFromShapeFileLoader(Population pop, List<Building> buildings, Scenario scenario, List<FloodingReader> netcdfReaders) {
		this.buildings = buildings;
		this.scenario = scenario;
		this.readers = netcdfReaders;
		this.pop = pop;
		this.evacuationScenario = ((EvacuationConfigGroup) this.scenario.getConfig().getModule("evacuation")).getEvacuationScanrio();
		this.sample = ((EvacuationConfigGroup) this.scenario.getConfig().getModule("evacuation")).getSampleSize();
	}

	public Population getPopulation() {
		if (this.initialized) {
			return this.pop;
		}

		if (this.readers != null) {
			buildFiQuadTree();
		}

		buildQuadTree();

		Link saveLink = this.scenario.getNetwork().getLinks().get(new IdImpl("el1"));
		EvacuationStartTimeCalculator time = getEndCalculatorTime();

		PopulationFactory popB = this.pop.getFactory();

		int count = 0;
		for (Building building : this.buildings) {

			// Coordinate c = building.getGeo().getCoordinate();
			// Link link = this.quadTree.get(c.x,c.y);

			Coord coord = MGC.point2Coord(building.getGeo().getCentroid());
			if (this.fis != null) {
				FloodingInfo fi = this.fis.get(coord.getX(), coord.getY());
				if (fi.getCoordinate().distance(building.getGeo().getCentroid().getCoordinate()) > ((EvacuationConfigGroup) this.scenario.getConfig().getModule("evacuation")).getBufferSize()) {
					continue;
				}

			}

			if (coord == null) {
				throw new RuntimeException();
			}

			int numOfPers = getNumOfPersons(building);

			for (int i = 0; i < numOfPers; i++) {

				Person pers = popB.createPerson(this.scenario.createId(Integer.toString(count++)));
				Plan plan = popB.createPlan();
				plan.setPerson(pers);
				Activity act = popB.createActivityFromCoord("h", coord);
				act.setEndTime(time.getEvacuationStartTime(act));
				plan.addActivity(act);
				Leg leg = popB.createLeg(TransportMode.car);
				plan.addLeg(leg);
				Activity act2 = popB.createActivityFromCoord("h", saveLink.getCoord());
				plan.addActivity(act2);
				pers.addPlan(plan);
				this.pop.addPerson(pers);

			}
		}

		this.initialized = true;
		return this.pop;

	}

	private void buildFiQuadTree() {

		Envelope envelope = new Envelope(0, 0, 0, 0);

		for (FloodingReader fr : this.readers) {
			envelope.expandToInclude(fr.getEnvelope());
		}
		this.fis = new QuadTree<FloodingInfo>(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
		for (FloodingReader fr : this.readers) {
			for (FloodingInfo fi : fr.getFloodingInfos()) {
				this.fis.put(fi.getCoordinate().x, fi.getCoordinate().y, fi);
			}
		}

	}

	protected int getNumOfPersons(Building building) {
		int pers = 0;
		if (this.evacuationScenario == EvacuationScenario.day) {
			pers = building.getPopDay();
		} else if (this.evacuationScenario == EvacuationScenario.night) {
			pers = building.getPopNight();
		} else if (this.evacuationScenario == EvacuationScenario.afternoon) {
			pers = building.getPopAf();
		} else {
			throw new RuntimeException("Unknown scenario type:" + this.evacuationScenario + "!");
		}

		int removed = 0;
		for (int i = 0; i < pers; i++) {
			if (MatsimRandom.getRandom().nextDouble() > this.sample) {
				removed++;
			}
		}
		pers -= removed;

		if (EXCLUDE_SHELTER_EVACUEES && building.isQuakeProof()) {
			building.setShelterSpace(Math.max(0, building.getShelterSpace() - pers));
			return 0;
		}
		return pers;
	}

	protected EvacuationStartTimeCalculator getEndCalculatorTime() {
		double endTime = Double.NaN;
		if (this.evacuationScenario == EvacuationScenario.day) {
			endTime = 12 * 3600;
		} else if (this.evacuationScenario == EvacuationScenario.night) {
			endTime = 3 * 3600;
		} else if (this.evacuationScenario == EvacuationScenario.afternoon) {
			endTime = 16 * 3600;
		} else {
			throw new RuntimeException("Unknown scenario type:" + this.evacuationScenario + "!");
		}
		return new StaticEvacuationStartTimeCalculator(endTime);
	}

	private void buildQuadTree() {
		this.quadTree = new QuadTree<Link>(0, 0, 700000, 9990000);
		for (Link link : this.scenario.getNetwork().getLinks().values()) {
			if (link.getId().toString().contains("el") || link.getId().toString().contains("s")) {
				continue;
			}
			this.quadTree.put(link.getCoord().getX(), link.getCoord().getY(), link);
		}

	}
}

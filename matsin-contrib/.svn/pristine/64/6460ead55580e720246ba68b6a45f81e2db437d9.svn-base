/* *********************************************************************** *
 * project: org.matsim.*
 * EvacuationPlansGeneratorAndNetworkTrimmer.java
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

package org.matsim.contrib.evacuation.base;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.utils.geometry.CoordImpl;

/**
 *@author glaemmel
 */
public class EvacuationPlansGenerator {

	private final static Logger log = Logger.getLogger(EvacuationPlansGenerator.class);

	protected Network network;

	protected Population pop;

	private final Link saveLink;

	public EvacuationPlansGenerator(Population pop, Network network, Link saveLink) {
		this.network = network;
		this.pop = pop;
		this.saveLink = saveLink;
	}

	/**
	 * Generates an evacuation plan for all agents inside the evacuation area.
	 * Agents outside the evacuation are will be removed from the plans.
	 *
	 * @param plans
	 * @param network
	 */
	public void run() {

		/* all persons that want to start on an already deleted link will be excluded from the
		 *simulation.     */
		log.info("  - removing all persons outside the evacuation area");
		Iterator<? extends Person> it = this.pop.getPersons().values().iterator();
		while (it.hasNext()) {
			Person pers = it.next();

			Id id = ((Activity)pers.getPlans().get(0).getPlanElements().get(0)).getLinkId();

			if (this.network.getLinks().get(id) == null) {
				it.remove();
			}
		}



		// the remaining persons take part in the evacuation simulation
		log.info("  - generating evacuation plans for the remaining persons");
		EvacuationStartTimeCalculator c = getEndCalculatorTime();

		final Coord saveCoord = new CoordImpl(12000.0, -12000.0);
		for (Person person : this.pop.getPersons().values()) {
			if (person.getPlans().size() != 1 ) {
				throw new RuntimeException("For each agent only one initial evacuation plan is allowed!");
			}

			Plan plan = person.getPlans().get(0);

			if (plan.getPlanElements().size() != 1 ) {
				throw new RuntimeException("For each initial evacuation plan only one Act is allowed - and no Leg at all");
			}
			((PlanImpl) plan).getFirstActivity().setEndTime(c.getEvacuationStartTime(((PlanImpl) plan).getFirstActivity()));

			LegImpl leg = new org.matsim.core.population.LegImpl(TransportMode.car);
			leg.setDepartureTime(0.0);
			leg.setTravelTime(0.0);
			leg.setArrivalTime(0.0);
			plan.addLeg(leg);

			plan.addActivity(new ActivityImpl("h", saveCoord, this.saveLink.getId()));

		}

	}

	protected EvacuationStartTimeCalculator getEndCalculatorTime() {
			return new StaticEvacuationStartTimeCalculator(3*3600);
	}

}

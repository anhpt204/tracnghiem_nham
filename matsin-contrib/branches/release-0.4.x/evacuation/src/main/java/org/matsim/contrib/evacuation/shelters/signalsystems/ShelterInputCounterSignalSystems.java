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

package org.matsim.contrib.evacuation.shelters.signalsystems;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.evacuation.base.Building;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.api.experimental.events.handler.LinkLeaveEventHandler;
import org.matsim.core.mobsim.framework.events.SimulationInitializedEvent;
import org.matsim.core.mobsim.framework.listeners.SimulationInitializedListener;


public class ShelterInputCounterSignalSystems implements LinkLeaveEventHandler, SimulationInitializedListener {

	private static final Logger log = Logger.getLogger(ShelterInputCounterSignalSystems.class);

	private final HashMap<Id,Counter> counts = new HashMap<Id, Counter>();
	private final HashMap<Id, Building> shelterLinkMapping;

	private int iteration;

	private final Scenario scenario;

	public ShelterInputCounterSignalSystems(Scenario scenario, HashMap<Id,Building> shelterLinkMapping) {

		this.shelterLinkMapping = shelterLinkMapping;
		this.scenario = scenario;

		for (Link link : this.scenario.getNetwork().getLinks().values()) {
			if (link.getId().toString().contains("sl") && link.getId().toString().contains("b")) {
				this.counts.put(link.getId(), new Counter());
			}
		}
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		Counter c = this.counts.get(event.getLinkId());
		if (c != null) {
			c.count++;
		}

	}

	public boolean getShelterOfLinkHasSpace(Id linkRefId) {
		Counter c = this.counts.get(linkRefId);
		Building b = this.shelterLinkMapping.get(linkRefId);
		if ((c.count <= b.getShelterSpace()) || (this.iteration == this.scenario.getConfig().controler().getLastIteration())) {
			return true;
		}
//
//		if (c.count <= 1.5 * b.getShelterSpace()) {
//			return MatsimRandom.getRandom().nextDouble() < 0.25;
//		}
//
//		if (c.count <= 2 * b.getShelterSpace()) {
//			return MatsimRandom.getRandom().nextDouble() < 0.1;
//		}
//		if (c.count <= 3 * b.getShelterSpace()) {
//			return MatsimRandom.getRandom().nextDouble() < 0.01;
//		}
		return false; // MatsimRandom.getRandom().nextDouble() < 0.01;
	}

	@Override
	public void reset(int iteration) {
		this.iteration = iteration;
		for (Entry<Id, Counter> e : this.counts.entrySet()) {
			int count = e.getValue().count;
			int max = this.shelterLinkMapping.get(e.getKey()).getShelterSpace();
			double overload = (double)count / max;
			log.info("Link:" + e.getKey() + "  count:" + count + " should be at most:" + max + " overload:" + overload);
			e.getValue().count = 0;
		}
	}

	@Override
	public void notifySimulationInitialized(SimulationInitializedEvent e) {
//		SortedMap<Id, SignalSystemController> scs = ((QSim) e.getQueueSimulation()).getQSimSignalEngine().getSignalSystemControlerBySystemId();
//		for (SignalSystemController ssc : scs.values()) {
//			if (!(ssc instanceof SheltersDoorBlockerController) ){
//				throw new RuntimeException("wrong SignalsystemsController type!");
//			}
//			((SheltersDoorBlockerController)ssc).setShelterInputCounter(this);
//			((SheltersDoorBlockerController)ssc).initialize();
//		}

	}

	private static class Counter {
		int count = 0;
	}

}

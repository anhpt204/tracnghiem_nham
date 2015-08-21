/* *********************************************************************** *
 * project: org.matsim.*
 * ShelterDoorBlockerSetup.java
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

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.lanes.data.LaneDefinitionsV11ToV20Conversion;
import org.matsim.lanes.data.v11.Lane;
import org.matsim.lanes.data.v11.LaneDefinitions;
import org.matsim.lanes.data.v11.LaneDefinitionsFactory;
import org.matsim.lanes.data.v11.LanesToLinkAssignment;
import org.matsim.lanes.data.v20.LaneDefinitions20;
import org.matsim.signalsystems.builder.SignalModelFactory;
import org.matsim.signalsystems.model.EmptyAmberLogicImpl;
import org.matsim.signalsystems.model.Signal;
import org.matsim.signalsystems.model.SignalGroup;
import org.matsim.signalsystems.model.SignalGroupImpl;
import org.matsim.signalsystems.model.SignalImpl;
import org.matsim.signalsystems.model.SignalSystem;
import org.matsim.signalsystems.model.SignalSystemsManager;


public class ShelterDoorBlockerSetup {

	private SignalModelFactory factory = new ShelterSignalModelFactory();
	private ShelterInputCounterSignalSystems shelterInputCounter;
	
	public ShelterDoorBlockerSetup(ShelterInputCounterSignalSystems sic) {
		this.shelterInputCounter = sic;
	}

	public SignalSystemsManager createSignalManager(ScenarioImpl scenario) {
		List<Link> doorBlockerLinks = getDoorBlockerLinks(scenario.getNetwork());
		
		SignalSystemsManager signalManager = this.factory.createSignalSystemsManager();
		signalManager.setAmberLogic(new EmptyAmberLogicImpl());
		LaneDefinitions laneDefinitions = scenario.getLaneDefinitions11();
		LaneDefinitionsFactory lanesFactory = laneDefinitions.getFactory();
		
		int laneId = 0;
		int lsdId = 0;
		int lsgId = 0;
		
		for (Link link : doorBlockerLinks) {
			LanesToLinkAssignment b = lanesFactory.createLanesToLinkAssignment(link.getId());
			Id toLink = link.getToNode().getOutLinks().values().iterator().next().getId();
			Lane lane = lanesFactory.createLane(new IdImpl(laneId++));
			lane.addToLinkId(toLink);
			lane.setStartsAtMeterFromLinkEnd(link.getLength()/2.);
			b.addLane(lane);

			laneDefinitions.addLanesToLinkAssignment(b);
			Id id = new IdImpl(lsdId++);
			
			SignalSystem signalSystem = this.factory.createSignalSystem(id);
			signalSystem.setSignalSystemsManager(signalManager);
			signalManager.addSignalSystem(signalSystem);
			
			Id id2 = new IdImpl(lsgId++);

			SignalGroup signalGroup = new SignalGroupImpl(id2);
			Signal signal = new SignalImpl(id2, link.getId());
			signalSystem.addSignal(signal);
			signalSystem.addSignalGroup(signalGroup);
			signalGroup.addSignal(signal);
			signal.getLaneIds().add(lane.getId());
			
			SheltersDoorBlockerController signalController = new SheltersDoorBlockerController();
			signalSystem.setSignalSystemController(signalController);
			signalController.setSignalSystem(signalSystem);
			signalController.setShelterLink(link);
			signalController.setShelterInputCounter(this.shelterInputCounter);
		}
		
		LaneDefinitionsV11ToV20Conversion conversion = new LaneDefinitionsV11ToV20Conversion();
		LaneDefinitions20 laneDefinitionsv2 = conversion.convertTo20(laneDefinitions, scenario.getNetwork());
		scenario.addScenarioElement(laneDefinitionsv2);
		return signalManager;
	}

	private List<Link> getDoorBlockerLinks(Network network) {
		List<Link> ret = new ArrayList<Link>();
		for (Link link : network.getLinks().values()) {
			if (link.getId().toString().contains("sl") && link.getId().toString().contains("b")) {
				ret.add(link);
			}
		}
		return ret;
	}

}

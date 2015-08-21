/* *********************************************************************** *
 * project: org.matsim.*
 * SheltersDoorBlockerController.java
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

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.signalsystems.model.SignalController;
import org.matsim.signalsystems.model.SignalGroup;
import org.matsim.signalsystems.model.SignalPlan;
import org.matsim.signalsystems.model.SignalSystem;

public class SheltersDoorBlockerController implements SignalController{
	
	private static final Logger log = Logger.getLogger(SheltersDoorBlockerController.class);
	
	private ShelterInputCounterSignalSystems shelterInputCounter;
	private SignalSystem signalSystem;
	private boolean isRed = false;
	
	private Link shelterLink;

	public SheltersDoorBlockerController() {
	}

	public void setShelterInputCounter(ShelterInputCounterSignalSystems shelterInputCounter) {
		this.shelterInputCounter = shelterInputCounter;
		
	}
	@Override
	public void updateState(double timeSeconds) {
		if (!this.isRed){
			for (SignalGroup sg : this.signalSystem.getSignalGroups().values()){
//			Id linkId = sg.getSignals().get(sg.getId()).getLinkId();
				if (!this.shelterInputCounter.getShelterOfLinkHasSpace(this.shelterLink.getId())){
					this.signalSystem.scheduleDropping(timeSeconds, sg.getId());
					this.isRed = true;
				}
			}
		}
	}
	
	@Override
	public void addPlan(SignalPlan plan) {}

	@Override
	public void setSignalSystem(SignalSystem system) {
		this.signalSystem = system;
	}

	public void setShelterLink(Link l) {
		log.debug("set shelter link: " + l.getId());
		this.shelterLink = l;
	}

	@Override
	public void reset(Integer iterationNumber) {
		this.isRed = false;
	}

	@Override
	public void simulationInitialized(double simStartTimeSeconds) {
		
	}
}

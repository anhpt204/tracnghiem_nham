/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.contrib.transEnergySim.analysis.charging;

import org.matsim.api.core.v01.Id;

public abstract class ChargingLogRow {
	Id agentId;
	Id linkId;
	double startChargingTime;
	double chargingDuration;
	double energyChargedInJoule;
	
	public double getStartChargingTime() {
		return startChargingTime;
	}

	public double getChargingDuration() {
		return chargingDuration;
	}

	public double getEnergyChargedInJoule() {
		return energyChargedInJoule;
	}
	
	public Id getAgentId() {
		return agentId;
	}

	public Id getLinkId() {
		return linkId;
	}
	
}
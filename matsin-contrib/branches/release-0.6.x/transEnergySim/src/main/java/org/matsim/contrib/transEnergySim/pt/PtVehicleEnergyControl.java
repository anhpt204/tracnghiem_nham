/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package org.matsim.contrib.transEnergySim.pt;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public interface PtVehicleEnergyControl {
	public void handleLinkTravelled(PtVehicleEnergyState ptVehicleEnergyState, Id linkId, double travelTime);
	public void handleChargingOpportunityAtStation(PtVehicleEnergyState ptVehicleEnergyState, double durationOfStayAtStationInSeconds, Id stationId);
}


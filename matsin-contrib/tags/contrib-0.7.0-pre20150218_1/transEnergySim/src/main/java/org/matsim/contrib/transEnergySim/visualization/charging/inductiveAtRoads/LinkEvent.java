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

package org.matsim.contrib.transEnergySim.visualization.charging.inductiveAtRoads;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.parking.lib.GeneralLib;

public class LinkEvent {

	private double time;
	private double value;
	private Id linkId;

	public double getTime() {
		return time;
	}

	public double getValue() {
		return value;
	}

	public Id getLinkId() {
		return linkId;
	}

	public LinkEvent(double time, double value, Id linkId) {
		super();
		this.time = GeneralLib.projectTimeWithin24Hours(time);
		this.value = value;
		this.linkId = linkId;
	}
}

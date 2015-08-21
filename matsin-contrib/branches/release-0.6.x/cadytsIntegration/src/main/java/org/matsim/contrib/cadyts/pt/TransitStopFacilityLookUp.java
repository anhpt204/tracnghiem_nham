/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.contrib.cadyts.pt;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.cadyts.general.LookUp;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

/**
 * @author nagel
 *
 */
public class TransitStopFacilityLookUp implements LookUp<TransitStopFacility> {
	
	private TransitSchedule schedule;

	public TransitStopFacilityLookUp( Scenario sc ) {
		this.schedule = sc.getTransitSchedule() ;
	}
	
	public TransitStopFacilityLookUp( TransitSchedule schedule ) {
		this.schedule = schedule ;
	}

	@Override
	public TransitStopFacility lookUp(Id id) {
		return this.schedule.getFacilities().get(id);
	}

}
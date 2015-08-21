/* *********************************************************************** *
 * project: org.matsim.*
 * MarginalTravelCostCalculatorII.java
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

package org.matsim.contrib.evacuation.socialcost;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class MarginalTravelCostCalculatorII implements TravelDisutility {

	private final TravelDisutility sc;
	private final TravelTime tc;

	public MarginalTravelCostCalculatorII(final TravelTime travelTime, final TravelDisutility sc) {
		this.tc = travelTime;
		this.sc = sc;
	}


	@Override
	public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
		double t = this.tc.getLinkTravelTime(link, time, person, vehicle);
		double s = this.sc.getLinkTravelDisutility(link, time, person, vehicle);
		return t + s;
	}
	
	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
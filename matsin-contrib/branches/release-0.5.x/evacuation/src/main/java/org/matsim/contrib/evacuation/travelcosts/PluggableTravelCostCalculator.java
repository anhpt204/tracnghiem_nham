/* *********************************************************************** *
 * project: org.matsim.*
 * PluggableTravelCostCalculator.java
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
package org.matsim.contrib.evacuation.travelcosts;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class PluggableTravelCostCalculator implements TravelDisutility {
	
	private final List<TravelDisutility> calcs = new ArrayList<TravelDisutility>();
	private final TravelTime  tt;
	
	public PluggableTravelCostCalculator(TravelTime tt) {
		this.tt = tt;
	}
	
	public void addTravelCost(TravelDisutility calc) {
		this.calcs.add(calc);
	}

	@Override
	public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
		double ret = this.tt.getLinkTravelTime(link, time, person, vehicle);
		for (TravelDisutility calc : this.calcs) {
			ret += calc.getLinkTravelDisutility(link, time, person, vehicle);
		}
		return ret;
	}

	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
	
}

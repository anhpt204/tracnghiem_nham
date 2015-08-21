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
import org.matsim.core.router.util.PersonalizableTravelCost;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.router.util.TravelTime;

public class PluggableTravelCostCalculator implements PersonalizableTravelCost {
	
	private final List<TravelCost> calcs = new ArrayList<TravelCost>();
	private final TravelTime  tt;
	
	public PluggableTravelCostCalculator(TravelTime tt) {
		this.tt = tt;
	}
	
	public void addTravelCost(TravelCost calc) {
		this.calcs.add(calc);
	}

	@Override
	public double getLinkGeneralizedTravelCost(Link link, double time) {
		double ret = this.tt.getLinkTravelTime(link, time);
		for (TravelCost calc : this.calcs) {
			ret += calc.getLinkGeneralizedTravelCost(link, time);
		}
		return ret;
	}

	@Override
	public void setPerson(Person person) {
		// This cost function doesn't change with persons.
	}

}

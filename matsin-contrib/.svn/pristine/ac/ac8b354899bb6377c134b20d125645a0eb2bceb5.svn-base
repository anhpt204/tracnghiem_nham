/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package org.matsim.contrib.freight.vrp.algorithms.rr;

import java.util.Collection;

import org.matsim.contrib.freight.vrp.algorithms.rr.costCalculators.RouteAgent;

/**
 * 
 * @author stefan schroeder
 * 
 */

public class RuinAndRecreateSolution {

	private Collection<RouteAgent> tourAgents;

	private double totalCosts;

	public RuinAndRecreateSolution(Collection<RouteAgent> tourAgents, double totalCosts) {
		super();
		this.tourAgents = tourAgents;
		this.totalCosts = totalCosts;
	}

	public double getResult() {
		return totalCosts;
	}

	public Collection<RouteAgent> getTourAgents() {
		return tourAgents;
	}

	@Override
	public String toString() {
		return "totalResult=" + getResult();
	}

}

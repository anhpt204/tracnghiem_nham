/**
 * Project: taxi
 * Package: org.matsim.contrib.sarp.vehreqpath
 * Author: pta
 * Date: Nov 24, 2014
 */
package org.matsim.contrib.sarp.route;

import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;

/**
 *
 * Define difference methods for calculating cost of a path
 */
public class PathCostCalculators
{
	public static final VehiclePathCost BEST_COST = new VehiclePathCost()
	{
		
		@Override
		public double getCost(VehicleRoute route)
		{
			double cost = 0.0;
			for(VehiclePath p : route.getPaths())
				cost += p.path.getTravelCost();
			route.setCost(cost);
			return cost;
		}
	};

}

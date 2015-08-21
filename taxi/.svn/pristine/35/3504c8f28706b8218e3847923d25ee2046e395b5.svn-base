/**
 * Project: taxi
 * Package: org.matsim.contrib.sarp.vehreqpath
 * Author: pta
 * Date: Nov 24, 2014
 */
package org.matsim.contrib.sarp.vehreqpath;

import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;

/**
 *
 * Define difference methods for calculating cost of a path
 */
public class PathCostCalculators
{
	public static final VehicleRequestPathCost BEST_COST = new VehicleRequestPathCost()
	{
		
		@Override
		public double getCost(VehicleRequestsRoute route)
		{
			double cost = 0.0;
			for(VehicleRequestPath p : route.getPaths())
				cost += p.path.getTravelCost();
			route.setCost(cost);
			return cost;
		}
	};

}

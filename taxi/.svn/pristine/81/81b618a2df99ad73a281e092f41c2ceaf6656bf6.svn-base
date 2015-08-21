/**
 * Project: taxi
 * Package: org.matsim.contrib.sarp.vehreqpath
 * Author: pta
 * Date: Nov 24, 2014
 */
package org.matsim.contrib.sarp.route;

import org.hsqldb.lib.HashMap;
import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;

import com.sun.xml.xsom.impl.scd.Iterators.Map;

/**
 *
 * Define difference methods for calculating cost of a path
 */
public class PathCostCalculators
{
	// initial fare charge for one passenger service (VND)
	private static final double ALPHA = 10000;
	// initial fare charge for one parcel service (VND)
	private static final double BETA = 10000;
	
	//9000/1000;// fare charge per meter (VND) for passenger services
	private static final double GAMMA1 = 9; 
	//5000/1000; // fare charge per meter (VND) for parcel services
	private static final double GAMMA2 = 5;
	//2000/1000; // fuel cost: 20 VND per meter = 2000 VND per km (10 litters for 100km -> 0.1l*20000 = 2000VND for 1km)
	private static final double GAMMA3 = 2;
	// discount factor for exceeding the direct delivery time of passengers
	private static final double GAMMA4 = 40000;

	
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
	
	
	private double getDiscount(double expectedDistance, double realDistance)
	{
		return (realDistance/expectedDistance - 1)*GAMMA4;
	}
	private double getCostFuel(double d)
	{
		return d * GAMMA3;
	}
	private double getParcelRevenue(double d)
	{
		return d*GAMMA2 + BETA;
	}
	private double getPeopleRevenue(double d)
	{
		return d*GAMMA1 + ALPHA;
	}
	
	public static final VehiclePathCost TRANSPORTATION_COST = new VehiclePathCost()
	{
		
		@Override
		public double getCost(VehicleRoute route)
		{
			HashMap peopleDistances = new HashMap();
			HashMap parcelDistances = new HashMap();
			
			double distance = 0.0;
			for(VehiclePath p : route.getPaths())
			{				
				int linkCounts = p.path.getLinkCount();
				for (int i = 0; i < linkCounts; i++)
				{
					distance += p.path.getLink(i).getLength();
				}
			}
			double cost = GAMMA3 * distance;			
			route.setCost(cost);
			return cost;
		}
	};

}

/**
 * 
 */
package org.matsim.contrib.sarp.optimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.route.PathCostCalculators;
import org.matsim.contrib.sarp.route.PathNode;
import org.matsim.contrib.sarp.route.VehiclePathCost;
import org.matsim.contrib.sarp.route.VehicleRoute;
import org.matsim.contrib.sarp.route.PathNode.PathNodeType;
import org.matsim.contrib.sarp.schedule.TaxiTask;
import org.matsim.contrib.sarp.scheduler.TaxiScheduler;
import org.matsim.contrib.sarp.scheduler.TaxiSchedules;

/**
 * @author pta
 *
 */
public class Baoxiang extends AbstractTaxiOptimizer
{

	/**
	 * @param optimConfig
	 * @param unplannedPeopleRequests
	 * @param unplannedParcelRequests
	 */
	public Baoxiang(TaxiOptimizerConfiguration optimConfig,
			Collection<AbstractRequest> unplannedPeopleRequests,
			Collection<AbstractRequest> unplannedParcelRequests)
	{
		super(optimConfig, unplannedPeopleRequests, unplannedParcelRequests);
		// TODO Auto-generated constructor stub
	}

	private Set<Vehicle> getIdleVehicles()
	{
		Set<Vehicle> idleVehicles = new HashSet<Vehicle>();
		
		List<Vehicle> allVehicles = optimConfig.context.getVrpData().getVehicles();
		
		for(Vehicle veh: allVehicles)
		{
			if(optimConfig.scheduler.isIdle(veh))
				idleVehicles.add(veh);
		}
		return idleVehicles;
	}
	
	
	/* (non-Javadoc)
	 * @see org.matsim.contrib.sarp.optimizer.AbstractTaxiOptimizer#scheduleUnplannedRequests()
	 */
	@Override
	protected void scheduleUnplannedRequests()
	{
		Set<Vehicle> idleVehicles = getIdleVehicles();
		
		Iterator<AbstractRequest> iterPeopleRequests = unplannedPeopleRequests.iterator();
		
		ArrayList<AbstractRequest> plannedPeopleRequest = new ArrayList<AbstractRequest>();
		
		//while there is a people request and have an idle vehicle
		while(iterPeopleRequests.hasNext() && !idleVehicles.isEmpty())
		{
			AbstractRequest peopleRequest = iterPeopleRequests.next();
			
			Vehicle feasibleVehicle = this.optimConfig.vrpFinder.findFeasibleTaxi(idleVehicles, peopleRequest);
			
			//if there is not any taxi satisfied, then reject
			if(feasibleVehicle == null)
			{
				//unplannedPeopleRequests.remove(peopleRequest);
				plannedPeopleRequest.add(peopleRequest);
			}
			else 
			{
				//select parcels for planning
//				Collection<AbstractRequest> selectedParcelRequests = new TreeSet<AbstractRequest>(Requests.ABSOLUTE_COMPARATOR);
				ArrayList<AbstractRequest> selectedParcelRequests = new ArrayList<>();
				//AbstractRequest[] parcels = (AbstractRequest[])parcelRequests.toArray();
				if(unplannedParcelRequests.size() < MAXNUMBERPARCELS)
				{
					for(AbstractRequest r : unplannedParcelRequests)
					{
						selectedParcelRequests.add(r);
					}
				}
				else
				{
					for(AbstractRequest r : unplannedParcelRequests)
					{
						selectedParcelRequests.add(r);
						
						if(selectedParcelRequests.size() == MAXNUMBERPARCELS)
							break;
					}
				}
				
				ArrayList<AbstractRequest> peopleRequests = new ArrayList<>();
				peopleRequests.add(peopleRequest);
				//find a route with some parcel requests
				VehicleRoute bestRoute = findBestRoute(feasibleVehicle, 
						peopleRequests, 
						selectedParcelRequests, 
						PathCostCalculators.BEST_COST);
				
				//if found the best route
				if(bestRoute != null)
				{
					//then build schedule for this vehicle
					optimConfig.scheduler.scheduleRequests(bestRoute);
					//and then remove all parcel request from unplannedParcelRequests
					for(AbstractRequest p: bestRoute.getParcelRequests())
						unplannedParcelRequests.remove(p);
					// and remove peopleRequest and feasibleVehicle
					//unplannedPeopleRequests.remove(peopleRequest);
					plannedPeopleRequest.add(peopleRequest);
					idleVehicles.remove(feasibleVehicle);
					
					
					//write route for each route
					//for(VehicleRequestPath path : bestRoute.getPaths())
					//{
					//	System.err.println(path.vehicle.toString() + path.vehicle.getSchedule().toString());
					//}
					
				}
				
			}
			
		}
		
		for(AbstractRequest peopleRequest: plannedPeopleRequest)
			unplannedPeopleRequests.remove(peopleRequest);
		
		
	}
	
	private VehicleRoute findBestRoute(Vehicle vehicle, ArrayList<AbstractRequest> peopleRequests,
			ArrayList<AbstractRequest> parcelRequests, VehiclePathCost costCalculator)
	{
		//2 for a person, 1 for current location
		PathNode[] nodes = new PathNode[2 + 2 * parcelRequests.size() + 1];
		//get earlist time, location when vehicle is idle
		LinkTimePair departure = this.optimConfig.scheduler.getEarliestIdleness(vehicle);

		AbstractRequest peopleRequest = peopleRequests.get(0);
		//make a simple route
		nodes[0] = new PathNode(departure.link, null, PathNodeType.START, departure.time);
		nodes[1] = new PathNode(peopleRequest.getFromLink(), peopleRequest, PathNodeType.PICKUP, 0);
		nodes[nodes.length-1] = new PathNode(peopleRequest.getToLink(), peopleRequest, PathNodeType.DROPOFF,0);
		
		int i = 2;
		for(AbstractRequest parcel: parcelRequests)
		{
			nodes[i] = new PathNode(parcel.getFromLink(), parcel, PathNodeType.PICKUP,0);
			nodes[i+1] = new PathNode(parcel.getToLink(), parcel, PathNodeType.DROPOFF,0);
			i += 2;
		}
		
		//generate route
		return optimConfig.vrpFinder.getRouteAndCalculateCost(vehicle, nodes, peopleRequests, parcelRequests, costCalculator);
		
	}
	
	/***
	 * Baoxiang greedy insertion algorithm
	 * 
	 * greedy insert all parcels to the route of a vehicle
	 * @param vehicle: parcels will be inserted in
	 * @param pacels: parcels to be inserted
	 * @param costCalculator
	 */
	private void GreedyInsertion(Vehicle vehicle, AbstractRequest pacels, 
			VehiclePathCost costCalculator)
			
	{
		Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(vehicle);
		
		
	}

}

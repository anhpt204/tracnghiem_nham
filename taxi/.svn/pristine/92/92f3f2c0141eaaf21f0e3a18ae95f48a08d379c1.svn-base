package org.matsim.contrib.sarp.optimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.matsim.contrib.dvrp.data.Requests;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.data.ParcelRequest;
import org.matsim.contrib.sarp.data.PeopleRequest;
import org.matsim.contrib.sarp.filter.*;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;
import org.matsim.contrib.sarp.vehreqpath.PathCostCalculators;
import org.matsim.contrib.sarp.vehreqpath.PathNode;
import org.matsim.contrib.sarp.vehreqpath.PathNode.PathNodeType;
import org.matsim.contrib.sarp.vehreqpath.VehicleRequestPath;
import org.matsim.contrib.sarp.vehreqpath.VehicleRequestPathCost;
import org.matsim.contrib.sarp.vehreqpath.VehicleRequestsRoute;

public class Optimizer1 extends AbstractTaxiOptimizer
{
	public final int MAXNUMBERPARCELS = 2;
	
	private Set<Vehicle> idleVehicles;
	
	//private final RequestFilter requestFilter;
	private final VehicleFilter vehicleFilter;
	
	public Optimizer1(TaxiOptimizerConfiguration optimConfig)
	{
		super(optimConfig, new TreeSet<AbstractRequest>(Requests.ABSOLUTE_COMPARATOR),
				new TreeSet<AbstractRequest>(Requests.ABSOLUTE_COMPARATOR));
		
		this.vehicleFilter = new KStraightLineNearestVehicleFilter(optimConfig.scheduler, optimConfig.params.nearestVehiclesLimit);
		
	}
	
	private void getIdleVehicles()
	{
		idleVehicles = new HashSet<Vehicle>();
		for(Vehicle veh: optimConfig.context.getVrpData().getVehicles())
		{
			if(optimConfig.scheduler.isIdle(veh))
				idleVehicles.add(veh);
		}
		
	}

	@Override
	protected void scheduleUnplannedRequests()
	{
		getIdleVehicles();
		
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
				Collection<AbstractRequest> selectedParcelRequests = new TreeSet<AbstractRequest>(Requests.ABSOLUTE_COMPARATOR);
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
				
				//find a route with some parcel requests
				VehicleRequestsRoute bestRoute = findBestRoute(feasibleVehicle, 
						peopleRequest, 
						selectedParcelRequests, 
						PathCostCalculators.BEST_COST);
				
				//if found the best route
				if(bestRoute != null)
				{
					//then build schedule for this vehicle
					optimConfig.scheduler.scheduleRequests(bestRoute);
					//and then remove all parcel request from unplannedParcelRequests
					for(AbstractRequest p: bestRoute.parcelRequests)
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
	
	private VehicleRequestsRoute findBestRoute(Vehicle vehicle, AbstractRequest peopleRequest,
			Collection<AbstractRequest> parcelRequests, VehicleRequestPathCost costCalculator)
	{
		//2 for a person, 1 for current location
		PathNode[] nodes = new PathNode[2 + 2 * parcelRequests.size() + 1];
		//get earlist time, location when vehicle is idle
		LinkTimePair departure = this.optimConfig.scheduler.getEarliestIdleness(vehicle);

		
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
		return optimConfig.vrpFinder.getRouteAndCalculateCost(vehicle, nodes, peopleRequest, parcelRequests, costCalculator);
		
	}

}

/**
 * 
 */
package org.matsim.contrib.sarp.optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task.TaskType;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.route.PathCostCalculators;
import org.matsim.contrib.sarp.route.PathNode;
import org.matsim.contrib.sarp.route.VehiclePathCost;
import org.matsim.contrib.sarp.route.VehicleRoute;
import org.matsim.contrib.sarp.route.PathNode.PathNodeType;
import org.matsim.contrib.sarp.schedule.TaxiTask;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;
import org.matsim.contrib.sarp.schedule.TaxiTaskWithRequest;
import org.matsim.contrib.sarp.scheduler.TaxiScheduler;
import org.matsim.contrib.sarp.scheduler.TaxiSchedules;

import com.google.common.base.CaseFormat;

/**
 * @author pta
 *
 */
public class Baoxiang extends AbstractTaxiOptimizer
{

	private static final int MAXSIZEROUTE = 6;


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
				// rejection
				//unplannedPeopleRequests.remove(peopleRequest);
				plannedPeopleRequest.add(peopleRequest);
			}
			else 
			{				
				// insert people to the route of the vehicle
				
				
				//find a route with some parcel requests
				VehicleRoute bestRoute = neighborhoodSearch(feasibleVehicle, unplannedParcelRequests);
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
	 * @param unplannedParcelRequests
	 * @return 
	 */
	
	private VehicleRoute greedyInsertion(Vehicle vehicle, 
			ArrayList<AbstractRequest> unplanedRequests)
	{
		Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(vehicle);
		
		List<TaxiTask> unservedTasks = TaxiSchedules.getUnservedTasks(schedule);
		
		ArrayList<PathNode> pathNodes = new ArrayList<PathNode>();
		for (TaxiTask task : unservedTasks)
		{
			if(task.getTaxiTaskType() == TaxiTaskType.CRUISE_DRIVE
				|| task.getTaxiTaskType() == TaxiTaskType.WAIT_STAY)
				continue;
			
			TaxiTaskWithRequest unservedTask = (TaxiTaskWithRequest)task;
			
			PathNodeType type = PathNodeType.PICKUP; 
			if (unservedTask.getTaxiTaskType() == TaxiTaskType.PARCEL_DROPOFF_DRIVE
					|| unservedTask.getTaxiTaskType() == TaxiTaskType.PARCEL_DROPOFF_STAY
					|| unservedTask.getTaxiTaskType() == TaxiTaskType.PEOPLE_DROPOFF_DRIVE
					|| unservedTask.getTaxiTaskType() == TaxiTaskType.PEOPLE_DROPOFF_STAY)
				type = PathNodeType.DROPOFF;
					
					
			pathNodes.add(new PathNode(unservedTask.getFromLink(), unservedTask.getRequest()
					, type, unservedTask.getBeginTime()));
			
		}
		while (pathNodes.size() < MAXSIZEROUTE)
		{
			int size = pathNodes.size();
			ArrayList<PathNode> newPathNode = new ArrayList<PathNode>();
			// deep clone
			newPathNode.addAll(pathNodes);
			
			ArrayList<PathNode> bestPathNode = new ArrayList<PathNode>();
			double bestBenefit = 0;
			
			for (AbstractRequest request: unplanedRequests)
			{
				for(int i = 0; i < size; i++)
				{
					// add a new pickup at i
					newPathNode.add(i, new PathNode(request.getFromLink(), request,
							PathNodeType.PICKUP, request.getT0()));
					
					for (int j = i+1; j < size+1; j++)
					{
						
						// add a new delivery
						newPathNode.add(j, new PathNode(request.getToLink(), request,
								PathNodeType.DROPOFF, request.getLateDeliveryTime()));

						// make a route
						VehicleRoute route = optimConfig.vrpFinder.getRouteAndCalculateCost(vehicle, 
								newPathNode, unplanedRequests, PathCostCalculators.TOTAL_BENEFIT);
						
						// calculate total benefits if route is feasible
						if(route.isFeasible())
						{
							// get shortest path for people
							double benefit = route.getTotalBenefit();
							
							if (bestBenefit < benefit)
							{
								bestBenefit = benefit;
								bestPathNode.clear();
								bestPathNode.addAll(newPathNode);
							}
							
						}
						
						// remove the delivery
						newPathNode.remove(j);
					}
					// remove the pickup
					newPathNode.remove(i);
				}
			}
			
			// update new pathNodes
			pathNodes.clear();
			pathNodes.addAll(bestPathNode);
		}
		
		return optimConfig.vrpFinder.getRouteAndCalculateCost(vehicle, 
				pathNodes, unplanedRequests, PathCostCalculators.TOTAL_BENEFIT);

	}
	
	
	private VehicleRoute neighborhoodSearch(Vehicle vehicle, AbstractRequest peopleRequest, 
			Collection<AbstractRequest> unplannedParcelRequests)
			
	{
		Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(vehicle);
		
		
	}

}

/**
 * 
 */
package org.matsim.contrib.sarp.optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.Requests;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task.TaskType;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.enums.RequestType;
import org.matsim.contrib.sarp.route.PathCostCalculators;
import org.matsim.contrib.sarp.route.PathNode;
import org.matsim.contrib.sarp.route.VehiclePath;
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
	private static final int MAXITERATIONSFORBIDDEN = 5;
	
	// parameters for simulated annealing
	private static final int MAXSEARCHES = 1000;
	//private static final double INIT_TEMPERATURE = 1000;
	private static final double COOLING_RATE = 0.99;


//	private Hashtable<Id<Vehicle>, VehicleRoute> vehicleRoutes;
	/**
	 * @param optimConfig
	 * @param unplannedPeopleRequests
	 * @param unplannedParcelRequests
	 */
	public Baoxiang(TaxiOptimizerConfiguration optimConfig)
	{
		super(optimConfig, new TreeSet<AbstractRequest>(Requests.ABSOLUTE_COMPARATOR),
				new TreeSet<AbstractRequest>(Requests.ABSOLUTE_COMPARATOR));
		
//		vehicleRoutes = new Hashtable<Id<Vehicle>, VehicleRoute>();
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
				// get schedule of this vehicle
				Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(feasibleVehicle);
				// append people to the route of the vehicle
				optimConfig.scheduler.appendPeopleRequest(schedule, peopleRequest);
				
				ArrayList<AbstractRequest> parcels = new ArrayList<AbstractRequest>();
				for(AbstractRequest parcel: unplannedParcelRequests)
					parcels.add(parcel);
				
				//find a route with some parcel requests
				VehicleRoute bestRoute = SimulatedAnnealing(feasibleVehicle,
						parcels, 
						MAXSEARCHES, COOLING_RATE, PathCostCalculators.TOTAL_BENEFIT);
				if(bestRoute != null)
				{
					// remove remaining tasks
					optimConfig.scheduler.removeUnservedTasks(feasibleVehicle);
					
					//then build schedule for this vehicle
					optimConfig.scheduler.scheduleRequests(bestRoute);
					
					//and then remove all parcel request from unplannedParcelRequests
					for(VehiclePath p: bestRoute.getPaths())
					{
						if(p.taskType == TaxiTaskType.PARCEL_PICKUP_DRIVE)
							unplannedParcelRequests.remove(p.request);
					}
					
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
			ArrayList<PathNode> pathNodes, 
			ArrayList<AbstractRequest> unplanedRequests,
			ArrayList<AbstractRequest> unservedPeopleRequests,
			VehiclePathCost costCalculator)
	{
		
		// insert new task
		while (!unplanedRequests.isEmpty() && pathNodes.size() < MAXSIZEROUTE)
		{
			int size = pathNodes.size();
			ArrayList<PathNode> newPathNode = new ArrayList<PathNode>();
			// deep clone
			newPathNode.addAll(pathNodes);
			
			ArrayList<PathNode> bestPathNode = new ArrayList<PathNode>();
			bestPathNode.addAll(pathNodes);
			
			double bestBenefit = 0;
			int start_i = 0;
			
			if (pathNodes.get(0).type == PathNodeType.START)
				start_i = start_i + 1;
			
			for (AbstractRequest request: unplanedRequests)
			{
				for(int i = start_i; i < size; i++)
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
								newPathNode, 
								unplanedRequests,
								unservedPeopleRequests,
								costCalculator);
						
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
				pathNodes, unplanedRequests, unservedPeopleRequests,
				costCalculator);

	}
	
	private ArrayList<PathNode> getUnservedPathNodes(Vehicle vehicle, 
			ArrayList<AbstractRequest> requests, ArrayList<AbstractRequest> unservedPeopleRequests)
	{
		Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(vehicle);
		// the first node is at the end of current task
		TaxiTask currentTask = schedule.getCurrentTask();

		
		// get unserved DRIVE tasks in the schedule (from next task to the end)
		List<TaxiTask> unservedDriveTasks = TaxiSchedules.getUnservedDriveTasks(schedule);

		ArrayList<PathNode> pathNodes = new ArrayList<PathNode>();
		
		switch (currentTask.getTaxiTaskType())
		{	
		case WAIT_STAY:
		case STRATEGIC_WAIT_STAY:
			pathNodes.add(new PathNode(currentTask.getFromLink(), null, PathNodeType.START, currentTask.getEndTime()));			
			break;

		case PEOPLE_PICKUP_DRIVE:
		case PEOPLE_PICKUP_STAY:
		case PARCEL_PICKUP_DRIVE:
		case PARCEL_PICKUP_STAY:
		case DUMMY_PICKUP_DRIVE:
			TaxiTaskWithRequest currentRequestTask = (TaxiTaskWithRequest)currentTask;
			AbstractRequest request = currentRequestTask.getRequest();
			pathNodes.add(new PathNode(request.getFromLink(), request, PathNodeType.PICKUP, currentTask.getEndTime()));
			break;
			
		default:
			TaxiTaskWithRequest currentDropoffTask = (TaxiTaskWithRequest)currentTask;
			AbstractRequest r = currentDropoffTask.getRequest();
			pathNodes.add(new PathNode(r.getToLink(), r, PathNodeType.DROPOFF, currentTask.getEndTime()));
			
			break;
		}

		// add node (end node) for each tasks
		for (TaxiTask task : unservedDriveTasks)
		{
									
			TaxiTaskWithRequest unservedTask = (TaxiTaskWithRequest)task;
			AbstractRequest request = unservedTask.getRequest(); 
			
			switch (unservedTask.getTaxiTaskType())
			{
			case PEOPLE_PICKUP_DRIVE:
			case PARCEL_PICKUP_DRIVE:
				pathNodes.add(new PathNode(request.getFromLink(), request, 
						PathNodeType.PICKUP, 0));

				// if have a pickup task than insert request of this task into set of requests
				
				if (request.getType() == RequestType.PARCEL)
					requests.add(request);
				else if (request.getType() == RequestType.PEOPLE) 
					unservedPeopleRequests.add(request);
				
				break;

			default: // dropoff
				pathNodes.add(new PathNode(request.getToLink(), request, 
						PathNodeType.DROPOFF, 0));
				break;
			}					
				
		}
		
		return pathNodes;

	}
	
	/**
	 * 
	 * @param schedule
	 * @param unplannedParcelRequests
	 * @param maxSearches
	 * @param T0: initial temperature
	 * @param c: cooling rate
	 * @return
	 */
	private VehicleRoute SimulatedAnnealing(Vehicle vehicle,
			ArrayList<AbstractRequest> unplannedParcelRequests,
			int maxSearches, double c,
			VehiclePathCost costCalculator)
			
	{
		Random rand = new Random();
		rand.setSeed(1000);
		
//		VehicleRoute bestRoute = null;

		ArrayList<AbstractRequest> unservedParcelRequests = new ArrayList<AbstractRequest>();
		ArrayList<AbstractRequest> unservedPeopleRequests = new ArrayList<AbstractRequest>();
		
		ArrayList<PathNode> pathNodes = getUnservedPathNodes(vehicle, unservedParcelRequests, unservedPeopleRequests);
		
		// get benefit of remaining requests
		VehicleRoute bestRoute = optimConfig.vrpFinder.getRouteAndCalculateCost(vehicle, pathNodes, unservedParcelRequests, unservedPeopleRequests, costCalculator);
		
		if (unplannedParcelRequests.isEmpty())
			return bestRoute;
		
		Hashtable<Id<Request>, Integer> removedRequests = new Hashtable<Id<Request>, Integer>();
		
		double T = 1;
		
		for (int i = 0; i < maxSearches; i++)
		{
			// increasing number of iterations that requests has been removed 
			Enumeration<Id<Request>> keys = removedRequests.keys();
			while(keys.hasMoreElements())
			{
				Id<Request> k = keys.nextElement();
				int v = removedRequests.get(k) + 1;
				removedRequests.put(k, v);
			}
			
			//randomly select a request that will be removed
			AbstractRequest removeRequest = null;
			while(true)
			{
				if (unservedParcelRequests.isEmpty())
					break;
				
				AbstractRequest tmpRequest = unservedParcelRequests.get(rand.nextInt(unservedParcelRequests.size()));
				Id<Request> k = tmpRequest.getId();
				
				//	check if it has been removed for 5 iterations
				
				if(removedRequests.containsKey(k))
				{
					int temp = removedRequests.get(k);
					if(temp >= MAXITERATIONSFORBIDDEN)
					{
						removedRequests.remove(k);
						removeRequest = tmpRequest;
						break;
					}
				}
				else 
				{
					removedRequests.put(k, 1);
				}
			}
			
			// if not any more request selected
			if(removeRequest == null && unplannedParcelRequests.isEmpty())
				continue;
			
			// remove it			
			ArrayList<PathNode> newPathNodes = new ArrayList<PathNode>();
			
			for (PathNode node : pathNodes)
			{
				// if equal, then ignore
				if(removeRequest != null
						&& node.request.getId().compareTo(removeRequest.getId()) == 0)
					continue;
				
				newPathNodes.add(node);
				
			}
			
			
			// reinsert new parcel request by using greedy insertion algorithm

			VehicleRoute route = greedyInsertion(vehicle, newPathNodes, 
					unplannedParcelRequests,
					unservedPeopleRequests,
					costCalculator);
			
			if (i == 0) // the first iteration
			{
				double x = Math.log(0.5);
				T = (route.getTotalBenefit()-bestRoute.getTotalBenefit()) / x;
			}
			
			if(acceptanceProbability(bestRoute.getTotalBenefit(), route.getTotalBenefit(), T))
				bestRoute = route;
		}
		return bestRoute;
		
		
	}
	
	private boolean acceptanceProbability(double oldBenefit, double newBenefit, double T)
	{
		double alpha = Math.exp(newBenefit - oldBenefit) / T;
		
		return alpha > Math.random();
	}

}

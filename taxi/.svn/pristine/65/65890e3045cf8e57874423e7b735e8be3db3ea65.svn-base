/**
 * Project: taxi
 * Package: org.matsim.contrib.sarp.vehreqpath
 * Author: pta
 * Date: Nov 24, 2014
 */
package org.matsim.contrib.sarp.route;

import java.awt.geom.IllegalPathStateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Requests;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.router.VrpPathCalculator;
import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.contrib.sarp.data.*;
import org.matsim.contrib.sarp.enums.RequestType;
import org.matsim.contrib.sarp.route.PathNode.PathNodeType;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;
import org.matsim.contrib.sarp.scheduler.TaxiScheduler;
import org.matsim.contrib.sarp.util.CombinationGenerator;
import org.matsim.contrib.sarp.util.PermutationGenerator;

/**
 * 
 *
 *TODO
 * Methods for optimizing routes
 */
public class VehiclePathFinder
{
	public final VrpPathCalculator pathCalculator;
	public final TaxiScheduler scheduler;
	
	
	public VehiclePathFinder(VrpPathCalculator pathCalculator,
			TaxiScheduler scheduler)
	{
		this.pathCalculator = pathCalculator;
		this.scheduler = scheduler;
	}
	
	/**
	 * find a taxi that can go to pickup people at time window with minimum cost
	 * @param idleVehicles: idle vehicles
	 * @param peopleRequest: People request
	 * @return a feasible taxi: can go to pickup this person
	 * in time window
	 */
	public Vehicle findFeasibleTaxi(Iterable<Vehicle> idleVehicles,
			AbstractRequest peopleRequest)
	{
		Vehicle feasibleVehicle = null;
		double bestCost = Double.MAX_VALUE;
		
		for(Vehicle veh : idleVehicles)
		{
			VrpPathWithTravelData path = getPathForPeopleRequest(veh, peopleRequest);
			double cost = path.getTravelCost();
			
			//
			if(cost < bestCost)
			{
				bestCost = cost;
				feasibleVehicle = veh;				
			}
		}
		
		return feasibleVehicle;
	}
	
	/**
	 * Implement the method of Dr. Dung
	 * @param vehicle
	 * @param peopleRequest
	 * @param maxNumberParcels: maximum number of parcels in this route
	 * @return null if not exist
	 */
	/*public VehicleRequestsRoute setupRouteWithParcelsInserted(Vehicle vehicle, 
			AbstractRequest peopleRequest,
			Collection<AbstractRequest> parcelRequests,
			int maxNumberParcels,
			VehicleRequestPathCost costCalculator)
	{
		VehicleRequestsRoute bestRoute = null;
		double bestCost = Double.MAX_VALUE;
		// copy parcels?
		// I do not understand why doing this ???
		ArrayList<AbstractRequest> selectedParcels = new ArrayList<AbstractRequest>();
		for(AbstractRequest parcelRequest: parcelRequests)
		{
			selectedParcels.add(parcelRequest);
		}
		
		//get optimal route without parcels
		ArrayList<AbstractRequest> parcels = new ArrayList<AbstractRequest>();
		VehicleRequestsRoute route = this.computeBestCostRoute(vehicle, peopleRequest, parcels, costCalculator);
		if(route != null)
		{
			bestCost = route.getCost();
			bestRoute = route;
		}
		
		ArrayList<AbstractRequest> removedParcelRequests = null;
		//check with each size of parcels
		for(int k = 1; k < maxNumberParcels; k++)
		{
			if(k > selectedParcels.size()) break;
			
			//generate all combination of k elements in n elements (k < n)
			CombinationGenerator comGenerator = new CombinationGenerator(k, selectedParcels.size());
			comGenerator.generate();
			
			//with each combination
			for(int idx = 0; idx < comGenerator.size(); idx++)
			{
				int[] combination = comGenerator.get(idx);
				
				//get parcel requests
				parcels.clear();
				for(int i = 0; i < combination.length; i++)
				{
					parcels.add(selectedParcels.get(combination[i]));
				}
				//clear old route
				route = null;
				//calculate route
				route = this.computeBestCostRoute(vehicle, peopleRequest, parcels, costCalculator);
				//update best route
				if(route != null && route.getCost() < bestCost)
				{
					bestCost = route.getCost();
					bestRoute = route;
					removedParcelRequests = parcels;
				}
			}
		}
		
		return bestRoute;
		
	}
	*/
	
	public VehicleRoute getRoute(Vehicle vehicle,
			PathNode[] nodes)
	{
		if(nodes == null)
			return null;
		
		VehiclePath[] paths = getPath(vehicle, nodes);
		
		return new VehicleRoute(vehicle, paths, nodes);
	}
	
	/**
	 * route = pickup people, [pickup parcel i, dropoff parcel i], dropoff people
	 * @param vehicle
	 * @param peopleRequest
	 * @param parcelRequests
	 * @param costCalculator
	 * @return
	 */
	public VehicleRoute getRouteAndCalculateCost(Vehicle vehicle,
			PathNode[] nodes, ArrayList<AbstractRequest> peopleRequests,
			ArrayList<AbstractRequest> parcelRequests,
			VehiclePathCost costCalculator)
	{		
		
		VehiclePath[] paths = getPath(vehicle, nodes);
		
		if(paths == null)
			return null;
		else
		{
			VehicleRoute aRoute = new VehicleRoute(vehicle, 
					peopleRequests, parcelRequests, paths );
			
			if(costCalculator != null)
				costCalculator.getCost(aRoute);
			
			return aRoute;
		}
	}

	/**
	 * Method from Dr.Dung's code
	 * @param vehicle
	 * @param peopleRequest
	 * @param parcels
	 * @return
	 */
	/*private VehicleRequestsRoute computeBestCostRoute(Vehicle vehicle,
			AbstractRequest peopleRequest, ArrayList<AbstractRequest> parcels,
			VehicleRequestPathCost costCalculator)
	{
		//generate permutation of pickup and dropoff points
		
		//create an array of Links
		Link[] links = new Link[2*parcels.size() + 2];
		links[0] = peopleRequest.getFromLink();
		links[1] = peopleRequest.getToLink();
		
		for(int i = 0; i < parcels.size(); i++)
		{
			links[2*i + 2] = parcels.get(i).getFromLink();
			links[2*i + 3] = parcels.get(i).getToLink();
		}
		//get all permutation??? should pickup people first?
		PermutationGenerator perm = new PermutationGenerator(links);
		perm.generate();
		
		double bestCost = Double.MAX_VALUE;
		VehicleRequestsRoute bestRoute = null;
		//with each permutation
		for(int k = 0; k < perm.size(); k++)
		{	
	//		VehicleRequestPath[] paths = getPath(vehicle, links, peopleRequest, parcels);
			
			if(paths == null)
				continue;
			else
			{
				VehicleRequestsRoute aRoute = new VehicleRequestsRoute(vehicle, 
						peopleRequest, parcels, paths );
				double cost = costCalculator.getCost(aRoute);
				if(cost < bestCost)
				{
					bestCost = cost;
					bestRoute = aRoute;
				}
			}
		}
		return bestRoute;
	}*/

	public VehiclePath findBestVehicleForRequests(
			PeopleRequest peopleRequest,
			Collection<ParcelRequest> parcelRequests,
			Iterable<Vehicle> vehicles,
			VehiclePathCost costCalculator)
	{
		VehiclePath bestPath = null;
		
		//the smaller cost, the better
		double bestCost = Double.MAX_VALUE;
		
		for(Vehicle veh : vehicles)
		{
			//check path for people request
			VrpPathWithTravelData peopleTempPath = getPathForPeopleRequest(veh, peopleRequest);
		}
		return null;
	}
	
	/**
	 * get path from current location of taxi to pickup location of request
	 * @param vehicle
	 * @param request
	 * @return path that satisfies time window constraint (vehicle can arrive
	 * before late pickup time window of request) or null
	 */
	private VrpPathWithTravelData getPathForPeopleRequest(Vehicle vehicle, AbstractRequest request)
	{
		// get (location, time) where vehicle drop off the person
		LinkTimePair departure = this.scheduler.getEarliestSARVehicle(vehicle);
		
		// calculate shortest path from current location of vehicle 
		//to pickup location of the request
		VrpPathWithTravelData path = this.pathCalculator.calcPath(departure.link, 
				request.getFromLink(), 
				departure.time);
		// if not exist a path, return null
		if (path == null)
			return null;
		
		// check pickup time window
		// check if vehicle can arrive before late pickup time window, 
		//then return path
		double arrivalTime = path.getArrivalTime();
		if (arrivalTime > request.getT1()) 
			return path;
		// else return null
		return null;
	}
	
	/**
	 * Get path for a vehicle to go through links
	 * @param vehicle
	 * @param links: the list of links that vehicle must go through
	 * @param peopleRequest
	 * @param parcelRequests
	 * @return
	 */
	
	private VehiclePath[] getPath(Vehicle vehicle,  
			PathNode[] nodes)
	{
		if (nodes.length == 0)
			return null;
		
		VehiclePath[] paths = new VehiclePath[nodes.length-1];
		
		VrpPathWithTravelData[] vrpPaths = new VrpPathWithTravelData[paths.length];
		
		for(int i = 0; i < paths.length; i++)
		{
			// get departure time
			double departureTime = nodes[0].departureTime;
			
			if (i > 0)
			{
				//departure time = max(arrival time of previous path, T0) + pickup or dropoff time
				
				if(nodes[i].type == PathNodeType.PICKUP)
				{
					departureTime =Math.max(vrpPaths[i-1].getArrivalTime(), nodes[i].request.getT0());
					departureTime += this.scheduler.getParams().pickupDuration;
					
				}
				else if(nodes[i].type == PathNodeType.DROPOFF)
				{
					departureTime = vrpPaths[i-1].getArrivalTime();
					departureTime += this.scheduler.getParams().dropoffDuration;
	
				}
			}
			// get task type
			
			TaxiTaskType taskType = null;

			if(nodes[i+1].type == PathNodeType.PICKUP)
			{
				if(nodes[i+1].request.getType() == RequestType.PEOPLE)
					taskType = TaxiTaskType.PEOPLE_PICKUP_DRIVE;
				else if(nodes[i+1].request.getType() == RequestType.PARCEL)
					taskType = TaxiTaskType.PARCEL_PICKUP_DRIVE;				
			}
			else if(nodes[i+1].type == PathNodeType.DROPOFF)
			{
				if(nodes[i+1].request.getType() == RequestType.PEOPLE)
					taskType = TaxiTaskType.PEOPLE_DROPOFF_DRIVE;
				else if(nodes[i+1].request.getType() == RequestType.PARCEL)
					taskType = TaxiTaskType.PARCEL_DROPOFF_DRIVE;				
			}
				
			
			vrpPaths[i] = this.pathCalculator.calcPath(nodes[i].link, 
					nodes[i+1].link,
					departureTime);
			
			if(vrpPaths[i] == null)
				return null;
			
			paths[i] = new VehiclePath(vehicle, nodes[i+1].request, vrpPaths[i], taskType);

			//System.err.println(paths[i].request.getType().toString() + ": " + paths[i].path.getDepartureTime() + ", " + paths[i].path.getArrivalTime());

		}
		
		
		//for(VehicleRequestPath p : paths)
		//{
		//	System.err.println(p.request.getType().toString() + ": " + p.path.getDepartureTime() + ", " + p.path.getArrivalTime());
		//}
		
		return paths;
	}

	/**
	 * @param vehicle
	 * @param newPathNode
	 * @param unplanedRequests 
	 * @param bestCost
	 * @return
	 */
	public VehicleRoute getRouteAndCalculateCost(Vehicle vehicle,
			ArrayList<PathNode> newPathNode, 
			ArrayList<AbstractRequest> unplanedRequests, 
			VehiclePathCost costCalculator)
	{
		PathNode[] nodes = (PathNode[])newPathNode.toArray();
		VehiclePath[] paths = getPath(vehicle, nodes);
		
		if(paths == null)
			return null;
		else
		{
			VehicleRoute aRoute = new VehicleRoute(vehicle, paths, nodes);

			// shortest distance for people requests
			double peopleDistance = 0;
			// total shortest distance for parcel requests
			double parcelDistance = 0;
			
			for (AbstractRequest request: unplanedRequests)
			{
				VrpPathWithTravelData tempPath = this.pathCalculator.calcPath(request.getFromLink(), 
						request.getToLink(), request.getT1());
				
				int linkCount = tempPath.getLinkCount();

				if (request.getType() == RequestType.PEOPLE)
				{

					for (int i = 0; i < linkCount; i++)
					{
						Link link = tempPath.getLink(i);
						peopleDistance += link.getLength();
						
					} 
				}
				else if (request.getType() == RequestType.PARCEL)
				{
					for (int i = 0; i < linkCount; i++)
					{
						Link link = tempPath.getLink(i);
						parcelDistance += link.getLength();
					}
				}
			}
					
			aRoute.setShortestPeopleDistance(peopleDistance);
			aRoute.setShortestParcelDistance(parcelDistance);
			// transportation cost
			double transCost = costCalculator.getCost(aRoute);
			aRoute.setCost(transCost);
			
			
			return aRoute;
		}
	}
}

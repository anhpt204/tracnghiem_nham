/**
 * taxi
 * org.matsim.contrib.sarp.vehreqpath
 * tuananh
 * Nov 26, 2014
 */
package org.matsim.contrib.sarp.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.data.ParcelRequest;
import org.matsim.contrib.sarp.data.PeopleRequest;
import org.matsim.contrib.sarp.enums.RequestType;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;

/**
 * @author tuananh
 * Route of a vehicle
 */
public class VehicleRoute
{
	private Vehicle vehicle;
	// current people on this vehicle
	private ArrayList<AbstractRequest> peopleRequests;
	// current parcels on this vehicle
	private ArrayList<AbstractRequest> parcelRequests;
	
	private VehiclePath[] paths;
	
	private double cost;

	private double shortestPeopleDistance;
	private double shortestParcelDistance;
	
	private double totalBenefit;
	
	public Vehicle getVehicle()
	{
		return vehicle;
	}


	public void setVehicle(Vehicle vehicle)
	{
		this.vehicle = vehicle;
	}


	public Collection<AbstractRequest> getPeopleRequests()
	{
		return peopleRequests;
	}


	public void setPeopleRequests(ArrayList<AbstractRequest> peopleRequests)
	{
		this.peopleRequests = peopleRequests;
	}


	public Collection<AbstractRequest> getParcelRequests()
	{
		return parcelRequests;
	}


	public void setParcelRequests(ArrayList<AbstractRequest> parcelRequests)
	{
		this.parcelRequests = parcelRequests;
	}
	
	public VehicleRoute(Vehicle vehicle, ArrayList<AbstractRequest> peopleRequests,
			ArrayList<AbstractRequest> parcelRequests,
			VehiclePath[] paths)
	{
		this.vehicle = vehicle;
		this.peopleRequests = peopleRequests;
		this.parcelRequests = parcelRequests;
		this.paths = paths;
	}
	
	
	/**
	 * @param vehicle
	 * @param paths
	 */
	public VehicleRoute(Vehicle vehicle, VehiclePath[] paths)
	{
		// TODO Auto-generated constructor stub
		this.vehicle = vehicle;
		this.paths = paths;
		this.peopleRequests = null;
		this.parcelRequests = null;
	}


	public VehiclePath[] getPaths()
	{
		return paths;
	}
	public void setPaths(VehiclePath[] paths)
	{
		this.paths = paths;
	}
	public double getCost()
	{
		return cost;
	}
	public void setCost(double cost)
	{
		this.cost = cost;
	}
	
	public boolean isFeasible()
	{
		HashMap<Id<Request>, Integer> numOfStops = new HashMap<Id<Request>, Integer>();
		
		for (VehiclePath p: paths)
		{
			Id<Request> requestId = p.request.getId();

			if (p.taskType == TaxiTaskType.PARCEL_PICKUP_DRIVE
					|| p.taskType == TaxiTaskType.PARCEL_PICKUP_DRIVE)
			{
				numOfStops.put(requestId, 0);
				
				// check time window constraint
				// arrival after time window
				if (p.path.getArrivalTime() > p.request.getT1())
				{
					return false;
				}
			}

			if (p.taskType == TaxiTaskType.PARCEL_DROPOFF_DRIVE
					|| p.taskType == TaxiTaskType.PEOPLE_DROPOFF_DRIVE)
			{
				if (numOfStops.containsKey(requestId))
					numOfStops.remove(requestId);
				
				// check time window: 
				// dropoff after delivery time window
				if(p.path.getArrivalTime() > p.request.getLateDeliveryTime())
					return false;
			}
			
			// constraint over maximum of stops
			if(numOfStops.containsKey(requestId))
			{
				Integer val = numOfStops.get(requestId) +1;
				
				if (val > p.request.getMaxNbStops())
					return false;
				
				numOfStops.put(requestId, val);
			}
						
		}
		return true;
	}
	/*
	 * get total distance of this route
	 */
	public double getRealPeopleDistance()
	{
		boolean hasPeople = false;
		double distance = 0.0;
		for (VehiclePath p: paths)
		{
			if (hasPeople)
			{
				int linkCount = p.path.getLinkCount();
				for (int i = 0; i < linkCount; i++)
				{
					distance += p.path.getLink(i).getLength();
				}
			}
			
			if (p.taskType == TaxiTaskType.PEOPLE_PICKUP_DRIVE)
				hasPeople = true;
			else if (p.taskType == TaxiTaskType.PEOPLE_DROPOFF_DRIVE)
				hasPeople = false;
				
		}
		
		return distance;
	}


	public double getShortestPeopleDistance()
	{
		return shortestPeopleDistance;
	}


	public void setShortestPeopleDistance(double shortestPeopleDistance)
	{
		this.shortestPeopleDistance = shortestPeopleDistance;
	}


	public double getShortestParcelDistance()
	{
		return shortestParcelDistance;
	}


	public void setShortestParcelDistance(double shortestParcelDistance)
	{
		this.shortestParcelDistance = shortestParcelDistance;
	}


	public double getTotalBenefit()
	{
		return totalBenefit;
	}


	public void setTotalBenefit(double totalBenefit)
	{
		this.totalBenefit = totalBenefit;
	}

}

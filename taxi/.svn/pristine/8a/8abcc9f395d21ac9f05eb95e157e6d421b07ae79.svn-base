/**
 * taxi
 * org.matsim.contrib.sarp.vehreqpath
 * tuananh
 * Nov 26, 2014
 */
package org.matsim.contrib.sarp.route;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.data.ParcelRequest;
import org.matsim.contrib.sarp.data.PeopleRequest;

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

}

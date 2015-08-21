/**
 * taxi
 * org.matsim.contrib.sarp.vehreqpath
 * tuananh
 * Nov 26, 2014
 */
package org.matsim.contrib.sarp.vehreqpath;

import java.util.Collection;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.data.ParcelRequest;
import org.matsim.contrib.sarp.data.PeopleRequest;

/**
 * @author tuananh
 *
 */
public class VehicleRequestsRoute
{
	public final Vehicle vehicle;
	public final AbstractRequest peopleRequest;
	public final Collection<AbstractRequest> parcelRequests;
	
	private VehicleRequestPath[] paths;
	private double cost;
	
	public VehicleRequestsRoute(Vehicle vehicle, AbstractRequest peopleRequest,
			Collection<AbstractRequest> parcelRequest,
			VehicleRequestPath[] paths)
	{
		this.vehicle = vehicle;
		this.peopleRequest = peopleRequest;
		this.parcelRequests = parcelRequest;
		this.paths = paths;
	}
	
	
	public VehicleRequestPath[] getPaths()
	{
		return paths;
	}
	public void setPaths(VehicleRequestPath[] paths)
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

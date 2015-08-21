package org.matsim.contrib.sarp.filter;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.sarp.data.AbstractRequest;

public interface VehicleFilter
{
	
	Iterable<Vehicle> filterVehiclesForRequest(Iterable<Vehicle> vehicles, AbstractRequest request);

	VehicleFilter NO_FILTER = new VehicleFilter()
	{
		
		@Override
		public Iterable<Vehicle> filterVehiclesForRequest(
				Iterable<Vehicle> vehicles, AbstractRequest request)
		{
			return vehicles;
		}
	};
}

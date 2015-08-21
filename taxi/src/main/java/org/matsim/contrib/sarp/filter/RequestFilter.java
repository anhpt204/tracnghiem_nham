package org.matsim.contrib.sarp.filter;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.sarp.data.AbstractRequest;

public interface RequestFilter
{
	Iterable<AbstractRequest> filterRequestsForVehicle(Iterable<AbstractRequest> requests, Vehicle vehicle);
	

	RequestFilter NO_FILTER = new RequestFilter()
	{
		
		@Override
		public Iterable<AbstractRequest> filterRequestsForVehicle(
				Iterable<AbstractRequest> requests, Vehicle vehicle)
		{
			return requests;
		}
	};
}

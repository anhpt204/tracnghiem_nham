package org.matsim.contrib.sarp.vehreqpath;

import java.util.Collection;

import org.matsim.contrib.dvrp.data.*;
import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Task.TaskStatus;
import org.matsim.contrib.dvrp.schedule.Task.TaskType;
import org.matsim.contrib.sarp.data.*;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;

/*
 * Data structure for a path
 * - vehicle: taxi
 * - peopleRequest: a person for this path
 * - parcelRequests: a set of parcel for this path
 * - path
 */
public class VehicleRequestPath
{
	public final Vehicle vehicle;
	public final AbstractRequest request;
	public final VrpPathWithTravelData path;
	public final TaxiTaskType taskType;
	
	
	public VehicleRequestPath(Vehicle vehicle, 
			AbstractRequest request,
			VrpPathWithTravelData path,
			TaxiTaskType taskType)
	{
		this.vehicle = vehicle;
		this.request = request;
		this.path = path;
		this.taskType = taskType;
		
	}
	

}

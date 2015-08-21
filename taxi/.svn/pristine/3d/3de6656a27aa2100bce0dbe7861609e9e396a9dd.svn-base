package org.matsim.contrib.sarp.route;

import java.util.Collection;

import org.matsim.contrib.dvrp.data.*;
import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Task.TaskStatus;
import org.matsim.contrib.dvrp.schedule.Task.TaskType;
import org.matsim.contrib.sarp.data.*;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;

/*
 * Data structure for a VehiclePath
 * - vehicle: taxi
 * - request: a request to be picked up or deliveried
 * - path: path between two events (ex: pickup A and delivery B)
 * - taskType: 
 */
public class VehiclePath
{
	public final Vehicle vehicle;
	public final AbstractRequest request;
	public final VrpPathWithTravelData path;
	public final TaxiTaskType taskType;
	
	
	public VehiclePath(Vehicle vehicle, 
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

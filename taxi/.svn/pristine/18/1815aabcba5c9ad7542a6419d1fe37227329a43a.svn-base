package org.matsim.contrib.sarp.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.router.VrpPathCalculator;
import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Schedule.ScheduleStatus;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.route.VehiclePath;
import org.matsim.contrib.sarp.route.VehicleRoute;
import org.matsim.contrib.sarp.schedule.TaxiDropoffDriveTask;
import org.matsim.contrib.sarp.schedule.TaxiDropoffStayTask;
import org.matsim.contrib.sarp.schedule.TaxiPickupDriveTask;
import org.matsim.contrib.sarp.schedule.TaxiPickupStayTask;
import org.matsim.contrib.sarp.schedule.TaxiTask;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;
import org.matsim.contrib.sarp.schedule.TaxiWaitStayTask;

public class TaxiScheduler 
{
	private final MatsimVrpContext context;
    private final VrpPathCalculator calculator;
    private final TaxiSchedulerParams params;
    
    public TaxiScheduler(MatsimVrpContext context, 
    		VrpPathCalculator calculator, TaxiSchedulerParams params)
    {
    	this.context = context;
    	this.calculator = calculator;
    	this.params = params;
    	
    	for(Vehicle veh: context.getVrpData().getVehicles())
    	{
			Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(veh);
					
    		schedule.addTask(new TaxiWaitStayTask(veh.getT0(), veh.getT1(), veh.getStartLink()));
    		
    	}
    }
    
    public TaxiSchedulerParams getParams()
    {
    	return this.params;
    }
    
    /*
     * A vehicle is idle iff vehicle is executing last task and
     * this last task is WAIT_STAY
     */
    public boolean isIdle(Vehicle vehicle)
    {
        double currentTime = context.getTime();
        
        //if time window T1 exceeded
        if (currentTime >= vehicle.getT1())
            return false;

        //else
        
        Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(vehicle);
        if (schedule.getStatus() != ScheduleStatus.STARTED) {
            return false;
        }

        TaxiTask currentTask = schedule.getCurrentTask();

        
        return Schedules.isLastTask(currentTask)
                && currentTask.getTaxiTaskType() == TaxiTaskType.WAIT_STAY;
    }
    
    
    /*
     * get the earlist time that this vehicle (veh) is idle
     */
    public LinkTimePair getEarliestIdleness(Vehicle veh)
    {
    	//get current time
    	double currentTime = context.getTime();
    	
    	//if current time is more than TW T1 (mean that veh is not working)
    	if(currentTime > veh.getT1())
    		return null;
    	
    	Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(veh);
    	
    	Link link;
    	double time;
    	
    	switch (schedule.getStatus())
		{
		case PLANNED:
		case STARTED:
			TaxiTask lastTask = Schedules.getLastTask(schedule);
			switch(lastTask.getTaxiTaskType())
			{
			case WAIT_STAY:
				link = ((StayTask)lastTask).getLink();
				time = Math.max(currentTime, lastTask.getBeginTime());
				
				return createValidLinkTimePair(link, time, veh);
			default:
				throw new IllegalStateException();
			}
			
		case COMPLETED:
			return null;
		case UNPLANNED: //there is always at least one WAIT TASK in a schedule
						
		default:
			throw new IllegalStateException();
		}
    }
    
    private LinkTimePair createValidLinkTimePair(Link link, double time,
			Vehicle veh)
	{
    	return time >= veh.getT1()? null: new LinkTimePair(link, time);
	}

	/*
     * Before a vehicle execute a next task, we should check and update
     * schedule (begin time and end time of all remaining tasks)
     */
    public void updateBeforeNextTask(Schedule<TaxiTask> schedule)
    {
    	//if schedule has not been started
    	if(schedule.getStatus() != ScheduleStatus.STARTED)
    		return;
    	
    	//else, update begin time and end time for each tasks of this schedule
    	//by first getting current time
    	double endTime = context.getTime();
    	TaxiTask currentTask = schedule.getCurrentTask();
    	//and then updating
    	updateCurrentAndPlannedTasks(schedule, endTime);
    	
    	//if we do not know destination of a schedule, it mean that 
    	//the last task in this schedule is PICKUP_STAY ???
    	if(!params.destinationKnown)
    	{
    		//currentTask is the last task ???
    		if(currentTask.getTaxiTaskType() == TaxiTaskType.PEOPLE_PICKUP_STAY
    				|| currentTask.getTaxiTaskType() == TaxiTaskType.PARCEL_PICKUP_STAY)
    		{
    			//add DropoffDriveTask and DropoffStayTask to this schedule
    			//appendDropoffAfterPickup(schedule);
    			
    			//add WaitStayTask after dropoffStayTask
    			//appendWaitAfterDropoff(schedule);
    			
    		}
    	}
    }
    
    /*
     * In this situation, the last task in schedule is PickupStayTask
     * so we need to complete this schedule by adding DropoffDriveTask and
     * DropoffStayTask to it.
     */
    public void appendDropoffAfterPickup(Schedule<TaxiTask> schedule)
    {
    	//in this situation, pickupStayTask is the last task in the schedule
    	TaxiPickupStayTask pickupStayTask = (TaxiPickupStayTask)Schedules.getLastTask(schedule);
    	//get request
    	AbstractRequest req = pickupStayTask.getRequest();
    	Link reqFromLink = req.getFromLink();
    	Link reqToLink = req.getToLink();
    	
    	double t3 = pickupStayTask.getEndTime();
    	
    	//get path of dropoffDriveTask
    	VrpPathWithTravelData path = calculator.calcPath(reqFromLink, reqToLink, t3);
    	//add dropoffDriveTask into schedule
    	schedule.addTask(new TaxiDropoffDriveTask(path, req));
    	
    	//and with dropoffStayTask
    	double t4 = path.getArrivalTime();
    	double t5 = t4 + params.dropoffDuration;
    	schedule.addTask(new TaxiDropoffStayTask(t4, t5, req));
    	
    }
    
    public void appendWaitAfterDropoff(Schedule<TaxiTask> schedule)
    {
    	TaxiDropoffStayTask lastTask = (TaxiDropoffStayTask)Schedules.getLastTask(schedule);
    	
    	//when not consider dropoffstay
    	//TaxiDropoffDriveTask lastTask = (TaxiDropoffDriveTask)Schedules.getLastTask(schedule);
    	// add wait time
    	double t5 = lastTask.getEndTime();
    	//each vehicle has a working time from t0 to t1 ?
    	double tEnd = Math.max(t5, schedule.getVehicle().getT1());
    	Link link = Schedules.getLastLinkInSchedule(schedule);
    	
    	schedule.addTask(new TaxiWaitStayTask(t5, tEnd, link));
    			
    }
    
    /*
     * when real execution of transportation is difference with planned schedule
     * (ex: real time) we need to update begin time and end time foreach tasks
     */
    public void updateCurrentAndPlannedTasks(Schedule<TaxiTask> schedule, double currentTaskNewEndTime)
    {
    	Task currentTask = schedule.getCurrentTask();
    	//if schedule is OK
    	if(currentTask.getEndTime() == currentTaskNewEndTime)
    		return;
    	// else, need to update end time for this task
    	currentTask.setEndTime(currentTaskNewEndTime);
    	// and begin and end time for next tasks of this schedule
    	
    	//get list of tasks
    	List<TaxiTask> tasks = schedule.getTasks();
    	//get index of next task of current task index (+1)
    	int nextTaskIdx = currentTask.getTaskIdx() + 1;
    	
    	double t = currentTaskNewEndTime;
    	
    	//for each task
    	for(int i = nextTaskIdx; i < tasks.size(); i++)
    	{
    		TaxiTask task = tasks.get(i);
    		
    		switch(task.getTaxiTaskType())
    		{
    		case WAIT_STAY:
    		{
    			if(i == tasks.size()-1)// is last task
    			{
    				task.setBeginTime(t);
    				if(task.getEndTime() < t)//happend if the pervious task is delayed
    					//do not remove this task, a Taxi schedule should end with WAIT Status
    					task.setEndTime(t);
    			}
    			else //is not last task, mean that there is some other tasks
    				//have been added at time submissionTime <= t
    			{
    				//get next task
    				TaxiTask nextTask = tasks.get(i + 1);
    				switch(nextTask.getTaxiTaskType())
    				{
    				case PEOPLE_PICKUP_DRIVE:
    				case PARCEL_PICKUP_DRIVE:
    				case CRUISE_DRIVE:
    					double endTime = task.getEndTime();
    					//if this WAIT_STAY task end before t then
    					//we should remove this WAIT_STAY task.
    					if(endTime <= t)
    					{
    						schedule.removeTask(task);
    						i--;
    					}
    					else
    					{
    						task.setBeginTime(t);
    						t = endTime;
    					}
    					break;
    					
    					default:
    						throw new RuntimeException();
    				
    				}
    			}
    			break;
    		}
    		
    		case PEOPLE_PICKUP_DRIVE:
    		case PARCEL_PICKUP_DRIVE:
    		case PEOPLE_DROPOFF_DRIVE:
    		case PARCEL_DROPOFF_DRIVE:
    		case CRUISE_DRIVE:
    		{
    			//can not be shortened/lengthen, therefore must be moved
    			// forward/backward
    			//so we need to set new begin time
    			task.setBeginTime(t);
    			//and re-calculate end time
    			VrpPathWithTravelData path = (VrpPathWithTravelData)((DriveTask)task).getPath();
    			t += path.getArrivalTime();
    			//and then set new end time
    			task.setEndTime(t);
    			
    			break;
    		}
    		
    		case PEOPLE_PICKUP_STAY:
    		case PARCEL_PICKUP_STAY:
    		{
    			//t = taxi's arrival time = begin time
    			task.setBeginTime(t);
    			// calculate end time
    			double t0 = ((TaxiPickupStayTask)task).getRequest().getT0();
    			t = Math.max(t, t0) + params.pickupDuration;
    			task.setEndTime(t);
    			
    			break;
    		}
    		
    		case PEOPLE_DROPOFF_STAY:
    		case PARCEL_DROPOFF_STAY:
    		{
    			//can not be shortened/lengthen, 
    			//therefore must be moved forward/backward
    			task.setBeginTime(t);
    			//dropoff customer immediately when arriving destination
    			t += params.dropoffDuration;
    			task.setEndTime(t);
    			
    			break;
    		}
    			    		
    		default:
    			throw new IllegalStateException();
    		}
    	}
    	
    	
    }
    
    /**
     * Append current schedule with new tasks
     * @param bestRoute: a new route (serve one people and more than one parcels)
     */

	public void scheduleRequests(VehicleRoute bestRoute)
	{
		Schedule<TaxiTask> bestSchedule = TaxiSchedules.getSchedule(bestRoute.getVehicle());
		//if PLANNED or STARTED
		if(bestSchedule.getStatus() != ScheduleStatus.UNPLANNED)
		{
			//get last task of this schedule, 
			//WaitStay is always the last task status 
			TaxiWaitStayTask lastTask = (TaxiWaitStayTask)Schedules.getLastTask(bestSchedule);
			
			switch(lastTask.getStatus())
			{
			//if last task have been planned, then we need to consider whether
			//this waiting task should exist or not?
			case PLANNED: //should not
				if(lastTask.getBeginTime() == bestRoute.getPaths()[0].path.getDepartureTime())
				{
					//this waiting task should be removed
					bestSchedule.removeLastTask();
				}
				else //should exist
				{
					//update end time of this waiting task
					lastTask.setEndTime(bestRoute.getPaths()[0].path.getDepartureTime());
				}
				break;
			case STARTED:
				lastTask.setEndTime(bestRoute.getPaths()[0].path.getDepartureTime());
				
				break;
			case PERFORMED:
			default:
				throw new IllegalStateException();
			}
			
		}
		
		//add task to the schedule (bestSchedule)
		for(int i = 0; i < bestRoute.getPaths().length; i++)
		{
			VehiclePath path = bestRoute.getPaths()[i];
			//drive to pickup
			if(path.taskType == TaxiTaskType.PARCEL_PICKUP_DRIVE 
					|| path.taskType == TaxiTaskType.PEOPLE_PICKUP_DRIVE)
			{
				bestSchedule.addTask(new TaxiPickupDriveTask(path.path, path.request));
				
				//add pickup stay
				double t1 = Math.max(path.path.getArrivalTime(), path.request.getT0());
				
				double t2 = t1 + this.params.pickupDuration;
				
				bestSchedule.addTask(new TaxiPickupStayTask(t1, t2, path.request));
			}
			else
				// drive to drop off
				if (path.taskType == TaxiTaskType.PARCEL_DROPOFF_DRIVE
					|| path.taskType == TaxiTaskType.PEOPLE_DROPOFF_DRIVE)
				{
					TaxiDropoffDriveTask dropoffDriveTask = new TaxiDropoffDriveTask(path.path, path.request); 
					bestSchedule.addTask(dropoffDriveTask);					
					
					//add dropoff stay task at arrival time
					double t = path.path.getArrivalTime();
					bestSchedule.addTask(new TaxiDropoffStayTask(t, t+this.params.dropoffDuration, path.request));
				}
			
			
		}
		
		appendWaitAfterDropoff(bestSchedule);
	}
	
	

}

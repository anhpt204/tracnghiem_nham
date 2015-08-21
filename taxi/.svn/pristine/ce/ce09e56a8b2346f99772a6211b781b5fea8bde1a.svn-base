package org.matsim.contrib.sarp.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.schedule.TaxiTask;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;
import org.matsim.contrib.sarp.schedule.TaxiTaskWithRequest;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class TaxiSchedules {
	
	public static final Predicate<TaxiTask> IS_PICKUP_STAY = new Predicate<TaxiTask>() {
        public boolean apply(TaxiTask t)
        {
            return (t.getTaxiTaskType() == TaxiTaskType.PEOPLE_PICKUP_STAY)
            		|| (t.getTaxiTaskType() == TaxiTaskType.PARCEL_PICKUP_STAY);
        };
    };
    
    public static final Function<TaxiTask, AbstractRequest> TAXI_TASK_TO_REQUEST = new Function<TaxiTask, AbstractRequest>() {
        public AbstractRequest apply(TaxiTask t)
        {
            if (t instanceof TaxiTaskWithRequest) {
                return ((TaxiTaskWithRequest)t).getRequest();
            }
            else {
                return null;
            }
        }
    };
    
    
	@SuppressWarnings("unchecked")
    public static Schedule<TaxiTask> getSchedule(Vehicle vehicle)
    {
        return (Schedule<TaxiTask>)vehicle.getSchedule();
    }


    public static Iterable<AbstractRequest> getTaxiRequests(Schedule<TaxiTask> schedule)
    {
        Iterable<TaxiTask> pickupTasks = Iterables.filter(schedule.getTasks(), IS_PICKUP_STAY);
        return Iterables.transform(pickupTasks, TAXI_TASK_TO_REQUEST);
    }
    
    public static List<TaxiTask> getUnservedTasks(Schedule<TaxiTask> schedule)
	{
    	List<TaxiTask> unservedTasks = new ArrayList();
		
		Task currentTask = schedule.getCurrentTask();
		int currentTaskIdx = currentTask.getTaskIdx();
		int scheduleSize = schedule.getTaskCount();
		List<TaxiTask> tasks = schedule.getTasks();
		
		for(int i = currentTaskIdx+1; i < scheduleSize; i++)
		{
			unservedTasks.add(tasks.get(i));
			
		}
		return unservedTasks;
	}
    
    public static TaxiTask getPersonDropoffStayTask(Schedule<TaxiTask> schedule)
	{
		int currentTaskIdx = schedule.getCurrentTask().getTaskIdx();
		List<TaxiTask> tasks = schedule.getTasks();
		
		for(int i = currentTaskIdx; i<schedule.getTaskCount(); i++)
		{
			TaxiTask currentTask = tasks.get(i);
			if(currentTask.getTaxiTaskType() == TaxiTaskType.PEOPLE_DROPOFF_STAY)
				return currentTask;
		}
		return null;
	}
    
    public static Iterable<AbstractRequest> getUnservedRequests(Schedule<TaxiTask> schedule)
    
	{
    	
		Iterable<TaxiTask> pickupTasks = Iterables.filter(getUnservedTasks(schedule), IS_PICKUP_STAY);
		return Iterables.transform(pickupTasks, TAXI_TASK_TO_REQUEST);
	}
}

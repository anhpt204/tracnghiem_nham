/**
 * taxi
 * org.matsim.contrib.sarp.util
 * tuananh
 * Dec 5, 2014
 */
package org.matsim.contrib.sarp.util;

import javax.management.RuntimeErrorException;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedule.ScheduleStatus;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.sarp.schedule.TaxiDropoffDriveTask;
import org.matsim.contrib.sarp.schedule.TaxiPickupDriveTask;
import org.matsim.contrib.sarp.schedule.TaxiTask;
import org.matsim.contrib.sarp.scheduler.TaxiSchedules;


public class TaxiStatisticsCalculator
{
	private TaxiStatistics taxiStat;
	
	public TaxiStatistics calculateTaxiStatistics(Iterable<? extends Vehicle> vehicles)
	{
		taxiStat = new TaxiStatistics();
		
		for(Vehicle vehicle: vehicles)
		{
			calculateStatistics(vehicle);
		}
		
		return taxiStat;
	}
	
	
	private void calculateStatistics(Vehicle vehicle)
	{
		Schedule<TaxiTask> schedule = TaxiSchedules.getSchedule(vehicle);
		
		//if this taxi has not ran
		if(schedule.getStatus() == ScheduleStatus.UNPLANNED)
			return;
		
		if(schedule.getTaskCount() < 1)
			throw new RuntimeException("Statistics: number of tasks must be more than 1");
		
		
		for(TaxiTask task : schedule.getTasks())
		{
			double timeDone = task.getEndTime() - task.getBeginTime();
			
			switch (task.getTaxiTaskType())
			{
			case PEOPLE_PICKUP_DRIVE:
				taxiStat.taxiPickupDriveTime += timeDone;
				taxiStat.numServicedPeople += 1;
				TaxiPickupDriveTask pickupDriveTask = (TaxiPickupDriveTask)task;
				if(task.getEndTime() > pickupDriveTask.getRequest().getT1())
					taxiStat.numLatePeoplePickup += 1;
				
				break;

			case PEOPLE_DROPOFF_DRIVE:
				TaxiDropoffDriveTask dropoffDriveTask = (TaxiDropoffDriveTask)task;
				if(task.getEndTime() > dropoffDriveTask.getRequest().getLateDeliveryTime())
					taxiStat.numLatePeopleDropoff += 1;
			default:
				break;
			}
		}
		
	}


	/**
	 * Taxi Statistics
	 *
	 */
	public static class TaxiStatistics
	{
		
		
	
		private double taxiPickupDriveTime;
		private double taxiDropoffDriveTime;
		private double taxiPickupStayTime;
		private double taxiDropoffStayTime;
		
		private double peopleWaitingTime;
		
		//pickup and dropoff people not in time windows
		private int numLatePeoplePickup;
		private int numLatePeopleDropoff;
		private int numServicedPeople;
		
		
		 @Override
	     public String toString()
	     {
	         return "Pickup Drive Time: " + this.taxiPickupDriveTime + "\n"
	        		 + "Number of PeopleSeviced: " + this.numServicedPeople + "\n"
	        		 + "Number of LatePickupPeople: " + this.numLatePeoplePickup + "\n"
	        		 + "Number of LateDropoffPeople: " + this.numLatePeopleDropoff;
	     }
	
	
	
	
		public double getTaxiPickupDriveTime()
		{
			return taxiPickupDriveTime;
		}
	
	
	
	
		public double getTaxiDropoffDriveTime()
		{
			return taxiDropoffDriveTime;
		}
	
	
	
	
		public double getTaxiPickupStayTime()
		{
			return taxiPickupStayTime;
		}
	
	
	
	
		public double getTaxiDropoffStayTime()
		{
			return taxiDropoffStayTime;
		}
	
	
	
	
		public double getPeopleWaitingTime()
		{
			return peopleWaitingTime;
		}




		/**
		 * @return the numErrorPeoplePickup
		 */
		public int getNumLatePeoplePickup()
		{
			return numLatePeoplePickup;
		}




		
		/**
		 * @return the numErrorPeopleDropoff
		 */
		public int getNumLatePeopleDropoff()
		{
			return numLatePeopleDropoff;
		}




		/**
		 * @return the numServicedPeople
		 */
		public int getNumServicedPeople()
		{
			return numServicedPeople;
		}




		
			 
		 
	}

}

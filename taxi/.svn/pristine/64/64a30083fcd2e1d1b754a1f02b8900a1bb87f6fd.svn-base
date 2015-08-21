package org.matsim.contrib.sarp.schedule;

import org.matsim.contrib.dvrp.schedule.Task;

public interface TaxiTask extends Task 
{
	public static enum TaxiTaskType
    {
        PEOPLE_PICKUP_DRIVE,	//drive to pickup a person
        PARCEL_PICKUP_DRIVE,	//drive to pickup a parcel
        
        PEOPLE_PICKUP_STAY,		//pickup a person
        PARCEL_PICKUP_STAY,		//pickup a parcel
        
        PEOPLE_DROPOFF_DRIVE,	//drive to drop off a person
        PARCEL_DROPOFF_DRIVE,	//drive to drop off a parcel

        PEOPLE_DROPOFF_STAY,	//drop off a person
        PARCEL_DROPOFF_STAY,	//drop off a parcel
        
        DUMMY_PICKUP_DRIVE,		//drive to strategic location
        STRATEGIC_WAIT_STAY,	//wait at strategic location
        
        CRUISE_DRIVE, 
        WAIT_STAY;

        //TODO consider shorter names:
        //TO_PICKUP, PICKUP, TO_DROPOFF, DROPOFF, CRUISE, WAIT;
    }


    TaxiTaskType getTaxiTaskType();

}

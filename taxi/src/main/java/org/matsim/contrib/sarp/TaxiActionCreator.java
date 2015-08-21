/**
 * taxi
 * org.matsim.contrib.sarp
 * tuananh
 * Nov 26, 2014
 */
package org.matsim.contrib.sarp;

import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.SinglePassengerDropoffActivity;
import org.matsim.contrib.dvrp.passenger.SinglePassengerPickupActivity;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.vrpagent.*;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.sarp.schedule.TaxiDropoffStayTask;
import org.matsim.contrib.sarp.schedule.TaxiPickupStayTask;
import org.matsim.contrib.sarp.schedule.TaxiTask;
import org.matsim.contrib.sarp.schedule.TaxiWaitStayTask;

/**
 * @author tuananh
 *
 */
public class TaxiActionCreator 
	implements VrpAgentLogic.DynActionCreator
{
	public final PassengerEngine passengerEngine;
	public final VrpLegs.LegCreator legCreator;
	private final double pickupDuration;
	
	public TaxiActionCreator(PassengerEngine passengerEngine,
			VrpLegs.LegCreator legCreator, double pickupDuration)
	{
		this.passengerEngine = passengerEngine;
		this.legCreator = legCreator;
		this.pickupDuration = pickupDuration;
	}

	@Override
	public DynAction createAction(Task task, double now)
	{
		TaxiTask tt = (TaxiTask)task;
		
		
		switch (tt.getTaxiTaskType()) {
        case PEOPLE_PICKUP_DRIVE:
        case PARCEL_PICKUP_DRIVE:
        case PEOPLE_DROPOFF_DRIVE:
        case PARCEL_DROPOFF_DRIVE:
        case CRUISE_DRIVE:
            return legCreator.createLeg((DriveTask)task);

        case PEOPLE_PICKUP_STAY:
        case PARCEL_PICKUP_STAY:
            final TaxiPickupStayTask pst = (TaxiPickupStayTask)task;
            return new SinglePassengerPickupActivity(passengerEngine, pst, pst.getRequest(),
                    pickupDuration);

        case PEOPLE_DROPOFF_STAY:
        case PARCEL_DROPOFF_STAY:
            final TaxiDropoffStayTask dst = (TaxiDropoffStayTask)task;
            return new SinglePassengerDropoffActivity(passengerEngine, dst, dst.getRequest());

        case WAIT_STAY:
            return new VrpActivity("Waiting", (TaxiWaitStayTask)task);

        default:
            throw new IllegalStateException();
    }
	}

}

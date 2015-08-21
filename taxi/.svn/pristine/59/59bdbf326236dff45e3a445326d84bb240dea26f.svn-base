package org.matsim.contrib.sarp.schedule;

import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTaskImpl;

public class TaxiCruiseDriveTask extends DriveTaskImpl
	implements TaxiTask
{

	public TaxiCruiseDriveTask(VrpPathWithTravelData path) {
		super(path);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TaxiTaskType getTaxiTaskType() {
		// TODO Auto-generated method stub
		return TaxiTaskType.CRUISE_DRIVE;
	}
	
	@Override
	protected String commonToString()
	{
        return "[" + getTaxiTaskType().name() + "]" + super.commonToString();
	}

}

package org.matsim.contrib.sarp.schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTaskImpl;
import org.matsim.contrib.sarp.route.PathNode;

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

	/* (non-Javadoc)
	 * @see org.matsim.contrib.sarp.schedule.TaxiTask#getFromLink()
	 */
	@Override
	public Link getFromLink()
	{
		return super.getPath().getFromLink();
	}

}

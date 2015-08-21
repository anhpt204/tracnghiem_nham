package org.matsim.contrib.sarp.schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;

public class TaxiWaitStayTask extends StayTaskImpl
	implements TaxiTask
{

	public TaxiWaitStayTask(double beginTime, double endTime, Link link) {
		super(beginTime, endTime, link);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TaxiTaskType getTaxiTaskType() {
		// TODO Auto-generated method stub
		return TaxiTaskType.WAIT_STAY;
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
		// TODO Auto-generated method stub
		return super.getLink();
	}
}

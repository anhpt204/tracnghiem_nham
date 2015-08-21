package org.matsim.contrib.sarp.schedule;

import org.matsim.contrib.dvrp.schedule.StayTaskImpl;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.data.PeopleRequest;
import org.matsim.contrib.sarp.enums.RequestType;

public class TaxiPickupStayTask extends StayTaskImpl
	implements TaxiTaskWithRequest
{
	private AbstractRequest request;

	public TaxiPickupStayTask(double beginTime, double endTime, AbstractRequest request)
	{
		super(beginTime, endTime, request.getFromLink());
		// TODO Auto-generated constructor stub
		this.request = request;
		request.setPickupStayTask(this);
		
	}

	@Override
	public TaxiTaskType getTaxiTaskType() 
	{
		if(request.getType() == RequestType.PEOPLE)
			return TaxiTaskType.PEOPLE_PICKUP_STAY;
		else
			return TaxiTaskType.PARCEL_PICKUP_STAY;
	}

	@Override
	public AbstractRequest getRequest() {
		// TODO Auto-generated method stub
		return this.request;
	}

	@Override
	public void removeFromRequest() {
		// TODO Auto-generated method stub
		request.setPickupStayTask(null);
		
	}
	
    @Override
    protected String commonToString()
    {
        return "[" + getTaxiTaskType().name() + "]" + super.commonToString();
    }

}

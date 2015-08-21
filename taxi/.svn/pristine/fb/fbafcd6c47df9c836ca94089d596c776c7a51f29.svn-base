package org.matsim.contrib.sarp.schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTaskImpl;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.enums.RequestType;

public class TaxiPickupDriveTask extends DriveTaskImpl
	implements TaxiTaskWithRequest
{
	private AbstractRequest request;

	public TaxiPickupDriveTask(VrpPathWithTravelData path, AbstractRequest request)
	{
		super(path);
		if(request.getFromLink() != path.getToLink())
			throw new IllegalArgumentException();
		this.request = request;
		
		request.setPickupDriveTask(this);
		
	}

	@Override
	public TaxiTaskType getTaxiTaskType() 
	{
		if(request.getType() == RequestType.PEOPLE)
			return TaxiTaskType.PEOPLE_PICKUP_DRIVE;
		else
			return TaxiTaskType.PARCEL_PICKUP_DRIVE;
		
	}

	@Override
	public AbstractRequest getRequest() {
		return this.request;
	}

	@Override
	public void removeFromRequest() {
		this.request.setPickupDriveTask(null);
		
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
		return super.getPath().getFromLink();
	}

}

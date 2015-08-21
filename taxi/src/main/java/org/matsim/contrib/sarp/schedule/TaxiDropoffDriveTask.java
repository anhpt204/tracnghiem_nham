package org.matsim.contrib.sarp.schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.router.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTaskImpl;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.enums.RequestType;

public class TaxiDropoffDriveTask extends DriveTaskImpl
	implements TaxiTaskWithRequest
{
	private AbstractRequest request;
	
	public TaxiDropoffDriveTask(VrpPathWithTravelData path, AbstractRequest request)
	{
		super(path);
		// TODO Auto-generated constructor stub
		if(//request.getFromLink() != path.getFromLink() && 
				request.getToLink() != path.getToLink())
		{
			throw new IllegalArgumentException();
		}
		this.request = request;
		request.setDropoffDriveTask(this);
	}

	@Override
	public TaxiTaskType getTaxiTaskType() 
	{
		if(request.getType() == RequestType.PEOPLE)
			return TaxiTaskType.PEOPLE_DROPOFF_DRIVE;
		else
			return TaxiTaskType.PARCEL_DROPOFF_DRIVE;
	}

	@Override
	public AbstractRequest getRequest() {
		// TODO Auto-generated method stub
		return this.request;
	}

	@Override
	public void removeFromRequest() {
		// TODO Auto-generated method stub
		this.request.setDropoffDriveTask(null);
		
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

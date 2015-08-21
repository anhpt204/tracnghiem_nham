package org.matsim.contrib.sarp.data;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.RequestImpl;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.sarp.enums.RequestType;
import org.matsim.contrib.sarp.schedule.TaxiDropoffDriveTask;
import org.matsim.contrib.sarp.schedule.TaxiDropoffStayTask;
import org.matsim.contrib.sarp.schedule.TaxiPickupDriveTask;
import org.matsim.contrib.sarp.schedule.TaxiPickupStayTask;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

public class AbstractRequest extends RequestImpl
	implements PassengerRequest
{
    public enum TaxiRequestStatus
    {
        //INACTIVE, // invisible to the dispatcher (ARTIFICIAL STATE!)
        UNPLANNED, // submitted by the CUSTOMER and received by the DISPATCHER
        PLANNED, // planned - included into one of the routes

        //we have started serving the request but we may still divert the cab
        PICKUP_DRIVE,

        //we have to carry out the request
        PICKUP_STAY, DROPOFF_DRIVE, DROPOFF_STAY,

        PERFORMED, //
        //REJECTED, // rejected by the DISPATCHER
        //CANCELLED, // canceled by the CUSTOMER
        ;
    };
    
    private RequestType type;
    
    private TaxiPickupDriveTask pickupDriveTask;
    private TaxiPickupStayTask pickupStayTask;
    private TaxiDropoffDriveTask dropoffDriveTask;
    private TaxiDropoffStayTask dropoffStayTask;

	private final MobsimPassengerAgent passenger;
    private final Link fromLink;
    private final Link toLink;
    private final double earlyDeliveryTime;// earliest end time
    private final double lateDeliveryTime;// latest end time
    private final double maxTravelDistance;
    private final int maxNbStops;
    
	public AbstractRequest(Id<Request> id, MobsimPassengerAgent passenger, double earlyPickupTime,
			double latePickupTime, double earlyDeliveryTime, double lateDeliveryTime, Link fromLink, Link toLink, 
			double submissionTime, double maxTravelDistance, int maxNbStops, RequestType type) 
	{
		super(id, 1, earlyPickupTime, latePickupTime, submissionTime);
		this.passenger = passenger;
		this.fromLink = fromLink;
		this.toLink = toLink;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.maxTravelDistance = maxTravelDistance;
		this.maxNbStops = maxNbStops;
		this.type = type;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Link getFromLink() {
		// TODO Auto-generated method stub
		return this.fromLink;
	}

	@Override
	public Link getToLink() {
		// TODO Auto-generated method stub
		return this.toLink;
	}

	@Override
	public MobsimPassengerAgent getPassenger() {
		// TODO Auto-generated method stub
		return this.passenger;
	}
		
	
	public TaxiPickupDriveTask getPickupDriveTask() {
		return pickupDriveTask;
	}

	public void setPickupDriveTask(TaxiPickupDriveTask pickupDriveTask) {
		this.pickupDriveTask = pickupDriveTask;
	}

	public TaxiPickupStayTask getPickupStayTask() {
		return pickupStayTask;
	}

	public void setPickupStayTask(TaxiPickupStayTask pickupStayTask) {
		this.pickupStayTask = pickupStayTask;
	}

	public TaxiDropoffDriveTask getDropoffDriveTask() {
		return dropoffDriveTask;
	}

	public void setDropoffDriveTask(TaxiDropoffDriveTask dropoffDriveTask) {
		this.dropoffDriveTask = dropoffDriveTask;
	}

	public TaxiDropoffStayTask getDropoffStayTask() {
		return dropoffStayTask;
	}

	public void setDropoffStayTask(TaxiDropoffStayTask dropoffStayTask) {
		this.dropoffStayTask = dropoffStayTask;
	}
	
    public TaxiRequestStatus getStatus()
    {
        if (pickupDriveTask == null) {
            return TaxiRequestStatus.UNPLANNED;
        }

        switch (pickupDriveTask.getStatus()) {
            case PLANNED:
                return TaxiRequestStatus.PLANNED;

            case STARTED:
                return TaxiRequestStatus.PICKUP_DRIVE;

            case CANCELLED:
                //may happen after diverting vehicles or cancellation by the customer
                throw new IllegalStateException(
                        "Request.pickupDriveTask should not point to a cancelled task");

            case PERFORMED:
                //at some later stage...
        }

        switch (pickupStayTask.getStatus()) {
            case PLANNED:
                throw new IllegalStateException("Unreachable code");

            case STARTED:
                return TaxiRequestStatus.PICKUP_STAY;

            case CANCELLED:
                //may happen only after cancellation by the customer
                throw new IllegalStateException(
                        "Request.pickupStayTask should not point to a cancelled task");

            case PERFORMED:
                break;//at some later stage...
        }

        switch (dropoffDriveTask.getStatus()) {
            case PLANNED:
                throw new IllegalStateException("Unreachable code");

            case STARTED:
                return TaxiRequestStatus.DROPOFF_DRIVE;

            case CANCELLED:
                throw new IllegalStateException("Cannot cancel at this stage");

            case PERFORMED:
                break;//at some later stage...
        }

        switch (dropoffStayTask.getStatus()) {
            case PLANNED:
                throw new IllegalStateException("Unreachable code");

            case STARTED:
                return TaxiRequestStatus.DROPOFF_STAY;

                
            case CANCELLED:
                throw new IllegalStateException("Cannot cancel at this stage");

            case PERFORMED:
                return TaxiRequestStatus.PERFORMED;
        }

        throw new IllegalStateException("Unreachable code");
    }

	public RequestType getType() {
		return type;
	}
	
	public void setType(RequestType type)
	{
		this.type = type;
	}

	public double getMaxTravelTime()
	{
		return maxTravelDistance;
	}

	public int getMaxNbStops()
	{
		return maxNbStops;
	}

	public double getLateDeliveryTime()
	{
		return lateDeliveryTime;
	}

	public double getEarlyDeliveryTime()
	{
		return earlyDeliveryTime;
	}

}

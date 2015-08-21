package org.matsim.contrib.sarp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator.RequestorType;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.RequestImpl;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.data.ParcelRequest;
import org.matsim.contrib.sarp.data.PeopleRequest;
import org.matsim.contrib.sarp.enums.RequestType;
import org.matsim.contrib.sarp.passenger.SARRequestCreator;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

public class RequestCreator implements SARRequestCreator
{
	public static String MODE = "taxi";
		

	@Override
	public AbstractRequest createRequest(Id<Request> id,
			MobsimPassengerAgent passenger, Link fromLink, Link toLink,
			double submissionTime, double earlyPickupTime,
			double latePickupTime, double earlyDeliveryTime,
			double lateDeliveryTime, double maxTravelDistance, int maxNbStops)
	{
		
		String name = passenger.getId().toString();
		RequestType type = RequestType.PEOPLE;

		if(name.contains("Parcel"))
			type = RequestType.PARCEL;
		
		return new AbstractRequest(id, passenger, earlyPickupTime, 
				latePickupTime, earlyDeliveryTime, lateDeliveryTime, 
				fromLink, toLink, submissionTime, maxTravelDistance, 
				maxNbStops, type);
	}



	/* (non-Javadoc)
	 * @see org.matsim.contrib.dvrp.passenger.PassengerRequestCreator#createRequest(org.matsim.api.core.v01.Id, org.matsim.core.mobsim.framework.MobsimPassengerAgent, org.matsim.api.core.v01.network.Link, org.matsim.api.core.v01.network.Link, double, double, double)
	 */
	@Override
	public PassengerRequest createRequest(Id<Request> id,
			MobsimPassengerAgent passenger, Link fromLink, Link toLink,
			double t0, double t1, double now)
	{
		// TODO Auto-generated method stub
		return null;
	}

}

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
	public PassengerRequest createRequest(Id<Request> id,
		MobsimPassengerAgent passenger, Link fromLink, Link toLink, double t0,
		double t1, double submissionTime)
	{
		//double l0 = t0 + 30*60;
		//double l0 = t0 + passenger.getExpectedTravelTime();
		double l0 = t0 + (new Random()).nextInt(60*60);
		double l1 = l0 + 10*60;
		
		String name = passenger.getId().toString();
		RequestType type = RequestType.PEOPLE;

		if(name.contains("Parcel"))
			type = RequestType.PARCEL;
		
		
		return new AbstractRequest(id, passenger, t0, t1, l0, l1, fromLink, toLink, submissionTime, type);
	}

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

}

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
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

public class RequestCreator implements PassengerRequestCreator
{
	public static String MODE = "taxi";
	
/*	@Override
	public AbstractRequest createRequest(Id<Request> id,
			MobsimPassengerAgent passenger, Link fromLink, Link toLink,
			double t0, double t1, double now)
	{
		String fileRequest = "./input/requests.txt";
		//read requests files
		double l0=t0;
		double l1=t1;
		double submissionTime = now;
		double maxTravelDistance = 0;
		int maxNbStops = 2;
		
		int idIndex = 0;
		int requestTypeIndex = 1;
		int timeCallIndex = 2;
		int pickupPointIndex = 3;
		int deliveryPointInddex = 4;
		int earlyPickupTimeIndex = 5;
		int latePickupTimeIndex = 6;
		int earlyDeliveryTimeIndex = 7;
		int lateDeliveryTimeIndex = 8;
		int maxTravelDistanceIndex = 9;
		int maxNbStopsIndex = 10;
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(fileRequest));
			String line = reader.readLine();
			
			while ((line=reader.readLine()) != null)
			{
				String[] parts = line.split(" ");
				if(Id.create((parts[idIndex].trim()), Request.class).compareTo(id) == 0)
				{
					l0 = Double.parseDouble(parts[earlyDeliveryTimeIndex]);
					l1 = Double.parseDouble(parts[lateDeliveryTimeIndex]);
					submissionTime = Double.parseDouble(parts[timeCallIndex]);

					int requestType = Integer.parseInt(parts[requestTypeIndex]);
					if(requestType == 1) //people
					{
						maxTravelDistance = Double.parseDouble(parts[maxTravelDistanceIndex]);
						maxNbStops = Integer.parseInt(parts[maxNbStopsIndex]);

						return (AbstractRequest)(new PeopleRequest(id, passenger, t0, t1, l0, l1, fromLink, toLink, maxNbStops, maxTravelDistance, submissionTime));

					}
					else
					{
						return (AbstractRequest)(new ParcelRequest(id, passenger, t0, t1, l0, l1, fromLink, toLink, submissionTime));
					}
				}
			}
			
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
*/
	
	

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

}

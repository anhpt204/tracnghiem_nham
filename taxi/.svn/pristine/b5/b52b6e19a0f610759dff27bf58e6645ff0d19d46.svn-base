package org.matsim.contrib.sarp.passenger;

import java.util.HashMap;
import java.util.Map;

import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;


public interface SARRequestCreator extends PassengerRequestCreator 
{
	AbstractRequest createRequest(Id<Request> id, MobsimPassengerAgent passenger,
			Link fromLink, Link toLink, double submissionTime, double earlyPickupTime,
			double latePickuptime, double earlyDeliveryTime, double lateDeliveryTime,
			double maxTravelDistance, int maxNbStops);
}

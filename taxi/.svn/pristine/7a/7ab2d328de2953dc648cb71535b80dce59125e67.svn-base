package org.matsim.contrib.sarp.data;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.sarp.enums.RequestType;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

public class PeopleRequest extends AbstractRequest 
{
	private int maxNbStops;
	private double maxTravelDistance;

	public PeopleRequest(Id<Request> id, MobsimPassengerAgent passenger,
			double t0, double t1, double l0, double l1, Link fromLink,
			Link toLink, double submissionTime) 
	{
		super(id, passenger, t0, t1, l0, l1, fromLink, toLink, submissionTime, RequestType.PEOPLE);
	}

	public PeopleRequest(Id<Request> id, MobsimPassengerAgent passenger,
			double t0, double t1, double l0, double l1, Link fromLink,
			Link toLink, int maxNbStops, double maxTravelDistance, double submissionTime) 
	{
		super(id, passenger, t0, t1, l0, l1, fromLink, toLink, submissionTime, RequestType.PEOPLE);

		this.maxNbStops = maxNbStops;
		this.maxTravelDistance = maxTravelDistance;
	}
	
	public int getMaxNbStops()
	{
		return this.maxNbStops;
	}

	public double getMaxTravelDistance()
	{
		return this.maxTravelDistance;
	}

	
	
}

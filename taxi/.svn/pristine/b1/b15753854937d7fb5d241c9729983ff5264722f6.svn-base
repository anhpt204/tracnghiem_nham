/**
 * taxi
 * org.matsim.contrib.sarp.util
 * tuananh
 * Nov 28, 2014
 */
package org.matsim.contrib.sarp.util;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.sarp.data.AbstractRequest;

/**
 * @author tuananh
 *
 */
public  class RequestEntry
{
	private Id<Person> personId;
	private double submissionTime;
	private int fromLinkId;
	private int toLinkId;
	private double earlyPickupTime;
	private double latePickupTime;
	private double earlyDeliveryTime;
	private double lateDeliveryTime;
	
	public RequestEntry(Id<Person> personId, double submissionTime,
			int fromLinkId, int toLinkId, double earlyPickupTime,
			double latePickupTime, double earlyDeliveryTime,
			double lateDeliveryTime, double maxTravelDistance, int maxNbStops)
	{
		super();
		this.personId = personId;
		this.submissionTime = submissionTime;
		this.fromLinkId = fromLinkId;
		this.toLinkId = toLinkId;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.maxTravelDistance = maxTravelDistance;
		this.maxNbStops = maxNbStops;
	}

	public int getFromLinkId()
	{
		return fromLinkId;
	}

	public void setFromLinkId(int fromLinkId)
	{
		this.fromLinkId = fromLinkId;
	}

	public int getToLinkId()
	{
		return toLinkId;
	}

	public void setToLinkId(int toLinkId)
	{
		this.toLinkId = toLinkId;
	}

	public double getEarlyPickupTime()
	{
		return earlyPickupTime;
	}

	public void setEarlyPickupTime(double earlyPickupTime)
	{
		this.earlyPickupTime = earlyPickupTime;
	}

	public double getLatePickupTime()
	{
		return latePickupTime;
	}

	public void setLatePickupTime(double latePickupTime)
	{
		this.latePickupTime = latePickupTime;
	}

	public double getEarlyDeliveryTime()
	{
		return earlyDeliveryTime;
	}

	public void setEarlyDeliveryTime(double earlyDeliveryTime)
	{
		this.earlyDeliveryTime = earlyDeliveryTime;
	}

	public double getLateDeliveryTime()
	{
		return lateDeliveryTime;
	}

	public void setLateDeliveryTime(double lateDeliveryTime)
	{
		this.lateDeliveryTime = lateDeliveryTime;
	}

	private double maxTravelDistance;
	private int maxNbStops;
	

	public Id<Person> getPersonId()
	{
		return personId;
	}

	public void setPersonId(Id<Person> personId)
	{
		this.personId = personId;
	}

	public double getSubmissionTime()
	{
		return submissionTime;
	}

	public void setSubmissionTime(double submissionTime)
	{
		this.submissionTime = submissionTime;
	}

	public double getMaxTravelDistance()
	{
		return maxTravelDistance;
	}

	public void setMaxTravelDistance(double maxTravelDistance)
	{
		this.maxTravelDistance = maxTravelDistance;
	}

	public int getMaxNbStops()
	{
		return maxNbStops;
	}

	public void setMaxNbStops(int maxNbStops)
	{
		this.maxNbStops = maxNbStops;
	}
	
	
	
}

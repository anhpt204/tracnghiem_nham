package org.matsim.contrib.freight.events;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.Offer;
import org.matsim.core.api.experimental.events.Event;

public class CarrierOfferRejectEvent implements Event{

	private Id id;
	
	private Offer offer;
	
	public CarrierOfferRejectEvent(Id id, Offer offer) {
		super();
		this.offer = offer;
	}

	public Id getId() {
		return id;
	}

	public Offer getOffer() {
		return offer;
	}

	@Override
	public double getTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, String> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}

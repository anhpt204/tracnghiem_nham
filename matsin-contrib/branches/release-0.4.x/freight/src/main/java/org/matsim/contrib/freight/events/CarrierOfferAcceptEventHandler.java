package org.matsim.contrib.freight.events;

import org.matsim.core.events.handler.EventHandler;

public interface CarrierOfferAcceptEventHandler extends EventHandler{
	
	public void handleEvent(CarrierOfferAcceptEvent event);

}

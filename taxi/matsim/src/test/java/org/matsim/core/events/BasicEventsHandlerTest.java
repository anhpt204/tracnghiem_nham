/* *********************************************************************** *
 * project: org.matsim.*
 * EventsHandlerTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.core.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.vehicles.Vehicle;

public class BasicEventsHandlerTest extends MatsimTestCase {

	public void testLinkEnterEventHandler() {
		EventsManagerImpl events = (EventsManagerImpl) EventsUtils.createEventsManager();
		MyLinkEnterEventHandler handler = new MyLinkEnterEventHandler();
		events.addHandler(handler);

		NetworkImpl network = NetworkImpl.createNetwork();
		Node node1 = network.getFactory().createNode(Id.create(1, Node.class), new CoordImpl(0, 0));
		Node node2 = network.getFactory().createNode(Id.create(2, Node.class), new CoordImpl(1000, 0));
		Link link1 = network.getFactory().createLink(Id.create(1, Link.class), node1, node2, network, 1000.0, 10.0, 3600.0, 0);

		events.processEvent(new LinkEnterEvent(8.0*3600, Id.create(1, Person.class), link1.getId(), Id.create("veh", Vehicle.class)));
		assertEquals("expected number of handled events wrong.", 1, handler.counter);
	}


	/*package*/ static class MyLinkEnterEventHandler implements LinkEnterEventHandler {

		/*package*/ int counter = 0;

		@Override
		public void handleEvent(LinkEnterEvent event) {
			this.counter++;
		}

		@Override
		public void reset(int iteration) {
			this.counter = 0;
		}

	}

}

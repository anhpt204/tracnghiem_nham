/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.core.scoring;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.TeleportationArrivalEvent;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.EventsToLegs.LegHandler;

public class EventsToLegsTest {

	@Test
	public void testCreatesLeg() {
		Scenario scenario = createTriangularNetwork();
		EventsToLegs eventsToLegs = new EventsToLegs(scenario);
		RememberingLegHandler lh = new RememberingLegHandler();
		eventsToLegs.setLegHandler(lh);
		eventsToLegs.handleEvent(new PersonDepartureEvent(10.0, Id.create("1", Person.class), Id.create("l1", Link.class), "walk"));
		eventsToLegs.handleEvent(new TeleportationArrivalEvent(30.0, Id.create("1", Person.class), 50.0));
		eventsToLegs.handleEvent(new PersonArrivalEvent(30.0, Id.create("1", Person.class), Id.create("l2", Link.class), "walk"));
		Assert.assertNotNull(lh.handledLeg);
		Assert.assertEquals(10.0, lh.handledLeg.getDepartureTime(), 1e-9);
		Assert.assertEquals(20.0, lh.handledLeg.getTravelTime(), 1e-9);
		Assert.assertEquals(50.0, lh.handledLeg.getRoute().getDistance(), 1e-9);
	}

	@Test
	public void testCreatesLegWithRoute() {
		Scenario scenario = createTriangularNetwork();
		EventsToLegs eventsToLegs = new EventsToLegs(scenario);
		RememberingLegHandler lh = new RememberingLegHandler();
		eventsToLegs.setLegHandler(lh);
		Id<Person> agentId = Id.create("1", Person.class);
		eventsToLegs.handleEvent(new PersonDepartureEvent(10.0, agentId, Id.create("l1", Link.class), "car"));
		eventsToLegs.handleEvent(new LinkLeaveEvent(10.0, agentId, Id.create("l1", Link.class), null));
		eventsToLegs.handleEvent(new LinkEnterEvent(11.0, agentId, Id.create("l2", Link.class), null));
		eventsToLegs.handleEvent(new LinkLeaveEvent(15.0, agentId, Id.create("l2", Link.class), null));
		eventsToLegs.handleEvent(new LinkEnterEvent(16.0, agentId, Id.create("l3", Link.class), null));
		eventsToLegs.handleEvent(new PersonArrivalEvent(30.0, agentId, Id.create("l3", Link.class), "car"));
		Assert.assertNotNull(lh.handledLeg);
		Assert.assertEquals(10.0,lh.handledLeg.getDepartureTime(), 1e-9);
		Assert.assertEquals(20.0,lh.handledLeg.getTravelTime(), 1e-9);
		Assert.assertEquals(20.0,lh.handledLeg.getRoute().getTravelTime(), 1e-9);
		
		// Don't know if it makes sense, but according to specification,
		// the length of a route still does not include first and last link.
		Assert.assertEquals(500.0,lh.handledLeg.getRoute().getDistance(), 1e-9);
	}
	
	private static Scenario createTriangularNetwork() {
		ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());

		NetworkImpl network = (NetworkImpl) scenario.getNetwork();
		
		// add nodes
		Node node1 = network.createAndAddNode(Id.create("n1", Node.class), scenario.createCoord(0, 0));
		Node node2 = network.createAndAddNode(Id.create("n2", Node.class), scenario.createCoord(50, 100));
		Node node3 = network.createAndAddNode(Id.create("n3", Node.class), scenario.createCoord(50, 0));
		Node node4 = network.createAndAddNode(Id.create("n4", Node.class), scenario.createCoord(100, 0));

		// add links
		network.createAndAddLink(Id.create("l1", Link.class), node1, node2, 500.0, 10.0, 3600.0, 1);
		network.createAndAddLink(Id.create("l2", Link.class), node2, node3, 500.0, 10.0, 3600.0, 1);
		network.createAndAddLink(Id.create("l3", Link.class), node3, node4, 50.0, 0.1, 3600.0, 1);
		network.createAndAddLink(Id.create("l4", Link.class), node4, node1, 50.0, 0.1, 3600.0, 1);
		
		return scenario;
	}
	
	private static class RememberingLegHandler implements LegHandler {

		/*package*/ Leg handledLeg = null;
		
		@Override
		public void handleLeg(Id<Person> agentId, Leg leg) {
			this.handledLeg = leg;
		}
	}

}

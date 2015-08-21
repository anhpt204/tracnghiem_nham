/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.contrib.transEnergySim.visualization.charging.inductiveAtRoads;

import org.matsim.contrib.transEnergySim.analysis.charging.ChargingLogRowLinkLevel;
import org.matsim.contrib.transEnergySim.analysis.charging.InductiveChargingAtRoadOutputLog;
import org.matsim.core.basic.v01.IdImpl;

import junit.framework.TestCase;

public class TestLinkVisualizationQueue extends TestCase {

	public void testBasic() {
		InductiveChargingAtRoadOutputLog log = new InductiveChargingAtRoadOutputLog();

		IdImpl linkId = new IdImpl("link-1");
		IdImpl agentId = new IdImpl("agent-1");
		log.add(new ChargingLogRowLinkLevel(agentId, linkId, (24 * 3600) - 10, 20, 3600 * 20));
		log.add(new ChargingLogRowLinkLevel(agentId, linkId, 80, 20, 3600 * 20));
		log.add(new ChargingLogRowLinkLevel(agentId, linkId, 90, 20, 3600 * 20));

		LinkVisualizationQueue linkEventsQueue = log.getLinkEventsQueue();

		assertEquals(3600.0, linkEventsQueue.getValue(linkId, 5));
		assertEquals(3600.0, linkEventsQueue.getValue(linkId, 10));
		assertEquals(0.0, linkEventsQueue.getValue(linkId, 15));
		assertEquals(3600.0, linkEventsQueue.getValue(linkId, 80));
		assertEquals(3600.0, linkEventsQueue.getValue(linkId, 85));
		assertEquals(2 * 3600.0, linkEventsQueue.getValue(linkId, 95));
		assertEquals(0.0, linkEventsQueue.getValue(linkId, 130));
		assertEquals(3600.0, linkEventsQueue.getValue(linkId, (24 * 3600) - 10 + 1));
	}

	public void testNoValueBeginning() {
		InductiveChargingAtRoadOutputLog log = new InductiveChargingAtRoadOutputLog();

		IdImpl linkId = new IdImpl("link-1");
		IdImpl agentId = new IdImpl("agent-1");
		log.add(new ChargingLogRowLinkLevel(agentId, linkId, 80, 20, 3600 * 20));

		LinkVisualizationQueue linkEventsQueue = log.getLinkEventsQueue();

		assertEquals(0.0, linkEventsQueue.getValue(linkId, 5));
	}

	public void testLinkIdDoesNotExist() {
		InductiveChargingAtRoadOutputLog log = new InductiveChargingAtRoadOutputLog();

		IdImpl linkId = new IdImpl("link-1");
		IdImpl agentId = new IdImpl("agent-1");
		log.add(new ChargingLogRowLinkLevel(agentId, linkId, 80, 20, 3600 * 20));

		LinkVisualizationQueue linkEventsQueue = log.getLinkEventsQueue();

		assertEquals(0.0, linkEventsQueue.getValue(new IdImpl("link-2"), 5));
	}

}

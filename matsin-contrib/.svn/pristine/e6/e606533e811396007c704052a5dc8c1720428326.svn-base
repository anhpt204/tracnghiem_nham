/* *********************************************************************** *
 * project: org.matsim.*
 * TravelTimeAndSocialCostCalculatorHeadMultiLink.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.contrib.evacuation.socialcost;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.AgentDepartureEvent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.api.experimental.events.handler.AgentDepartureEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkLeaveEventHandler;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.events.AgentMoneyEventImpl;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.router.util.TravelCost;

/**
 * 
 * @author laemmel
 * 
 */
// Social cost are approximated by: sc = t_e - (t_0 - \tau^{free}) where t_e
// denotes the time the queue disappears, t_0 the link enter time and
// \tau^{free} the freespeed travel time
public class SocialCostCalculatorSingleLink implements TravelCost, IterationStartsListener, AfterMobsimListener, AgentDepartureEventHandler, LinkEnterEventHandler, LinkLeaveEventHandler {

	private static Logger log = Logger.getLogger(SocialCostCalculatorSingleLink.class);
	private final int travelTimeBinSize;
	private final int numSlots;
	private final Network network;
	private final HashMap<Id, LinkInfo> linkInfos = new HashMap<Id, LinkInfo>();
	private final HashMap<Id, SocialCostRole> socCosts = new HashMap<Id, SocialCostRole>();
	private final EventsManager events;

	private final static int MSA_OFFSET = 20;

	private double oldCoef = 0;

	public SocialCostCalculatorSingleLink(final Network network, EventsManager events) {
		this(network, 15 * 60, 30 * 3600, events); // default timeslot-duration:
		// 15 minutes
	}

	private double newCoef = 1;

	public SocialCostCalculatorSingleLink(final Network network, final int timeslice, EventsManager events) {
		this(network, timeslice, 30 * 3600, events); // default: 30 hours at
		// most
	}

	public SocialCostCalculatorSingleLink(final Network network, final int timeslice, final int maxTime, EventsManager events) {
		this.travelTimeBinSize = timeslice;
		this.numSlots = (maxTime / this.travelTimeBinSize) + 1;
		this.network = network;
		this.events = events;
	}

	@Override
	public double getLinkGeneralizedTravelCost(final Link link, final double time) {
		SocialCostRole sc = this.socCosts.get(link.getId());
		if (sc == null) {
			return 0;
		}
		return sc.getSocCost(getTimeSlotIndex(time));
	}

	@Override
	public void notifyIterationStarts(final IterationStartsEvent event) {
		event.getControler();
		this.linkInfos.clear();
		updateSocCosts();
		if (event.getIteration() > MSA_OFFSET) {
			double n = event.getIteration() - MSA_OFFSET;
			this.oldCoef = n / (n + 1);
			this.newCoef = 1 / (n + 1);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.matsim.core.controler.listener.AfterMobsimListener#notifyAfterMobsim
	 * (org.matsim.core.controler.events.AfterMobsimEvent)
	 */
	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		for (LinkInfo li : this.linkInfos.values()) {
			if (li.agentsLeftLink.size() > 0) {
				li.lastLeft = 3600 * 24;
				computeSocCost(li);
			}
		}
	}

	private void updateSocCosts() {
		double maxCost = 0;
		double minCost = Double.POSITIVE_INFINITY;
		double costSum = 0;

		for (SocialCostRole scr : this.socCosts.values()) {
			scr.update(this.oldCoef);
			for (SocCostInfo sci : scr.socCosts.values()) {
				double cost = sci.cost;
				if (cost < minCost) {
					minCost = cost;
				} else if (cost > maxCost) {
					maxCost = cost;
				}
				costSum += cost;
			}
		}
		log.info("maxCost: " + maxCost + " minCost: " + minCost + " avg: " + costSum / this.socCosts.size());
	}

	@Override
	public void handleEvent(final LinkEnterEvent event) {
		LinkInfo info = getLinkInfo(event.getLinkId());
		AgentInfo aol = new AgentInfo();
		aol.enterTime = event.getTime();
		aol.id = event.getPersonId();
		info.agentsOnLink.add(aol);
	}

	@Override
	public void handleEvent(final AgentDepartureEvent event) {
		LinkInfo info = getLinkInfo(event.getLinkId());
		AgentInfo aol = new AgentInfo();
		aol.enterTime = event.getTime();
		aol.id = event.getPersonId();
		info.agentsOnLink.add(aol);
	}

	@Override
	public void handleEvent(final LinkLeaveEvent event) {
		LinkInfo info = getLinkInfo(event.getLinkId());
		AgentInfo aol = info.agentsOnLink.poll();
		info.lastLeft = event.getTime();

		if (info.agentsOnLink.size() == 0) {
			if (info.agentsLeftLink.size() > 0) {
				computeSocCost(info);
			}
			info.lastFSSlice = getTimeSlotIndex(event.getTime());
		} else if ((event.getTime() - aol.enterTime) <= info.t_free) {
			if (info.agentsLeftLink.size() > 0) {
				computeSocCost(info);
			}
			info.lastFSSlice = getTimeSlotIndex(event.getTime());
		} else {
			info.agentsLeftLink.add(aol);
		}

	}

	private void computeSocCost(final LinkInfo info) {

		SocialCostRole sc = this.socCosts.get(info.id);
		if (sc == null) {
			sc = new SocialCostRole();
			this.socCosts.put(info.id, sc);
		}

		int lB = info.lastFSSlice + 1;

		int uB = getTimeSlotIndex(info.lastLeft) - 1;

		double socCost = 0;
		for (int i = uB; i >= lB; i--) {
			socCost += this.travelTimeBinSize;
			sc.setSocCost(i, Math.max(0, socCost - info.t_free), this.oldCoef, this.newCoef);
		}

		while (info.agentsLeftLink.size() > 0) {
			AgentInfo ai = info.agentsLeftLink.poll();
			int slot = getTimeSlotIndex(ai.enterTime);
			if (slot < lB || slot > uB) {
				continue;
			}
			double tmp = sc.getSocCost(slot);
			double cost = tmp / -600;
			AgentMoneyEventImpl e = new AgentMoneyEventImpl(info.lastLeft, ai.id, cost);
			this.events.processEvent(e);
		}
	}

	private int getTimeSlotIndex(final double time) {
		int slice = ((int) time) / this.travelTimeBinSize;
		if (slice >= this.numSlots)
			slice = this.numSlots - 1;
		return slice;
	}

	private LinkInfo getLinkInfo(final Id id) {
		LinkInfo ret = this.linkInfos.get(id);
		if (ret == null) {
			ret = new LinkInfo(id);
			ret.t_free = Math.ceil(((LinkImpl) this.network.getLinks().get(id)).getFreespeedTravelTime()); // TODO
			// make
			// this
			// dynamic,
			// since
			// we
			// have
			// time
			// variant
			// networks
			this.linkInfos.put(id, ret);
		}
		return ret;
	}

	private static class LinkInfo {
		public double lastLeft;
		ConcurrentLinkedQueue<AgentInfo> agentsOnLink = new ConcurrentLinkedQueue<AgentInfo>();
		Queue<AgentInfo> agentsLeftLink = new ConcurrentLinkedQueue<AgentInfo>();
		double t_free;
		int lastFSSlice = 0;
		final Id id;

		public LinkInfo(Id id) {
			this.id = id;
		}
	}

	private static class AgentInfo {
		Id id;
		double enterTime;

		@Override
		public String toString() {
			return this.id + "  enterTime:" + this.enterTime;
		}
	}

	private static class SocCostInfo {

		double cost = 0;
		boolean updated = false;
	}

	private static class SocialCostRole {
		HashMap<Integer, SocCostInfo> socCosts = new HashMap<Integer, SocCostInfo>();

		public void setSocCost(final int timeSlice, final double socCost, final double oldCoef, final double newCoef) {
			if (Double.isInfinite(socCost)) {
				System.err.println("inf costs!!");
			}
			SocCostInfo sci = this.socCosts.get(timeSlice);
			if (sci == null) {
				sci = new SocCostInfo();
				this.socCosts.put(timeSlice, sci);
			}
			sci.cost = oldCoef * sci.cost + newCoef * socCost;
			sci.updated = true;
		}

		public double getSocCost(final int timeSlice) {
			SocCostInfo sci = this.socCosts.get(timeSlice);
			if (sci == null) {
				return 0;
			}
			return sci.cost;
		}

		public void update(double oldCoef) {
			for (SocCostInfo sci : this.socCosts.values()) {
				if (!sci.updated) {
					sci.cost = oldCoef * sci.cost;
				}
				sci.updated = false;
			}
		}
	}

	@Override
	public void reset(final int iteration) {
	}

}
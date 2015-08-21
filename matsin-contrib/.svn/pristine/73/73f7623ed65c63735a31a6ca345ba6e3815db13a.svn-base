/* *********************************************************************** *
 * project: org.matsim.*
 * SocialCostAggregator.java
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
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.misc.IntegerCache;

/**
 * @author laemmel
 * 
 */
public class SocialCostAggregator {

	private final Map<Id, LinkInfo> linkInfos = new HashMap<Id, LinkInfo>();

	public void addSocialCosts(Id linkId, int enterTimeslot, double amount) {
		LinkInfo li = this.linkInfos.get(linkId);
		if (li == null) {
			li = new LinkInfo();
			this.linkInfos.put(linkId, li);
		}
		li.addSocialCosts(enterTimeslot, amount);
	}

	public double getSocialCosts(Id linkId, int enterTimeSlot) {
		LinkInfo li = this.linkInfos.get(linkId);
		if (li == null) {
			return 0;
		}
		return li.getSocialCost(enterTimeSlot);
	}

	public void updateSocialCosts(int msaIteration) {
		for (LinkInfo li : this.linkInfos.values()) {
			li.updateSocialCosts(msaIteration);
		}
	}

	private static final class LinkInfo {
		private final Map<Integer, CostStruct> socialCosts = new HashMap<Integer, CostStruct>();

		public void addSocialCosts(final int enterTimeSlot, final double amount) {
			CostStruct curr = this.socialCosts.get(IntegerCache.getInteger(enterTimeSlot));
			if (curr != null) {
				curr.cnt += 1;
				curr.costSum += amount;
			} else {
				this.socialCosts.put(IntegerCache.getInteger(enterTimeSlot), new CostStruct(amount, 1));
			}

		}

		/**
		 * @param enterTimeSlot
		 * @return
		 */
		public double getSocialCost(int enterTimeSlot) {
			CostStruct cs = this.socialCosts.get(enterTimeSlot);
			if (cs == null) {
				return 0;
			}
			return cs.currentCost;
		}

		public void updateSocialCosts(int msaIteration) {
			double oldCoef = msaIteration / (msaIteration + 1.);
			double newCoef = 1. / (msaIteration + 1.);
			for (CostStruct c : this.socialCosts.values()) {
				double itScCost = c.cnt > 0 ? c.costSum / c.cnt : 0;
				if (msaIteration <= 0) {
					c.currentCost = itScCost;
				} else {
					c.currentCost = oldCoef * c.currentCost + newCoef * itScCost;
				}
				c.costSum = 0;
				c.cnt = 0;
			}
		}

		private static final class CostStruct {
			public double costSum;
			public int cnt;
			public double currentCost;

			public CostStruct(final double costSum, final int cnt) {
				this.cnt = cnt;
				this.costSum = costSum;
			}
		}
	}
}

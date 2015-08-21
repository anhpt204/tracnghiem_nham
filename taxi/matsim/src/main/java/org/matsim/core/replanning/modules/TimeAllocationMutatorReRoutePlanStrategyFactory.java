/* *********************************************************************** *
 * project: org.matsim.*
 * TimeAllocationMutatorReRoutePlanStategyFactory.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package org.matsim.core.replanning.modules;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyFactory;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.selectors.RandomPlanSelector;

/**
 * @author thibautd
 */
public class TimeAllocationMutatorReRoutePlanStrategyFactory implements PlanStrategyFactory {
	@Override
	public PlanStrategy createPlanStrategy(
			final Scenario scenario,
			final EventsManager eventsManager) {
		final PlanStrategyImpl strategy = new PlanStrategyImpl(new RandomPlanSelector());
		strategy.addStrategyModule( new TimeAllocationMutator(scenario.getConfig()) );
		strategy.addStrategyModule( new ReRoute( scenario ) );
		return strategy;
	}
}


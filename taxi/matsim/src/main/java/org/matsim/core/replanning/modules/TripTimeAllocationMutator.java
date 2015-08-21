/* *********************************************************************** *
 * project: org.matsim.*
 * TransitTimeAllocationMutator.java
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

package org.matsim.core.replanning.modules;

import org.matsim.core.config.Config;
import org.matsim.core.config.groups.VspExperimentalConfigGroup.ActivityDurationInterpretation;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.population.algorithms.TripPlanMutateTimeAllocation;

/**
 * Copy/Paste of TimeAllocationMutator, that calls TransitPlanMutateTimeAllocation instead
 * of PlanMutateTimeAllocation.
 *
 * @author mrieser
 */
public class TripTimeAllocationMutator extends AbstractMultithreadedModule {
	public final static String CONFIG_GROUP = "TimeAllocationMutator";
	public final static String CONFIG_MUTATION_RANGE = "mutationRange";

	private double mutationRange = 1800.0;
	private boolean useActivityDurations = true;
	private final boolean affectingDuration;

	/**
	 * Creates a new TimeAllocationMutator with a mutation range as defined in
	 * the configuration (module "TimeAllocationMutator", param "mutationRange").
	 */
	public TripTimeAllocationMutator(Config config) {
		super(config.global());
		this.mutationRange = config.timeAllocationMutator().getMutationRange() ;
		this.affectingDuration = config.timeAllocationMutator().isAffectingDuration() ;
		ActivityDurationInterpretation actDurInterpr = ( config.plans().getActivityDurationInterpretation() ) ;
		if ( actDurInterpr == ActivityDurationInterpretation.minOfDurationAndEndTime ) {
			useActivityDurations = true ;
		} else if ( actDurInterpr == ActivityDurationInterpretation.endTimeOnly ) {
			useActivityDurations = false ;
		} else if ( actDurInterpr == ActivityDurationInterpretation.tryEndTimeThenDuration ) {
			throw new UnsupportedOperationException( "need to clarify the correct setting here.  Probably not a big deal, but not done yet.  kai, aug'10") ;
		} else {
			throw new IllegalStateException( "beahvior not defined for this configuration setting") ;
		}
	}

	public TripTimeAllocationMutator(Config config, final double mutationRange, boolean affectingDuration) {
		super(config.global());
		this.mutationRange = mutationRange;
		this.affectingDuration = affectingDuration;
	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {
		TripPlanMutateTimeAllocation pmta =
			new TripPlanMutateTimeAllocation(
					getReplanningContext().getTripRouter().getStageActivityTypes(),
					this.mutationRange,
					affectingDuration, MatsimRandom.getLocalInstance());
		pmta.setUseActivityDurations(this.useActivityDurations);
		return pmta;
	}

}

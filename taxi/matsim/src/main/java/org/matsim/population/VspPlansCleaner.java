/* *********************************************************************** *
 * project: org.matsim.*
 * VspPlansCleaner.java
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
package org.matsim.population;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.VspExperimentalConfigGroup.ActivityDurationInterpretation;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.population.LegImpl;
import org.matsim.core.utils.misc.Time;

/**
 * @author nagel
 *
 */
class VspPlansCleaner implements BeforeMobsimListener {

    private final Scenario scenario;

    @Inject
    VspPlansCleaner(Scenario scenarioData) {
        this.scenario = scenarioData;
    }
	
	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		Population pop = scenario.getPopulation();
		Config config = scenario.getConfig() ;
		ActivityDurationInterpretation actDurInterp = ( config.plans().getActivityDurationInterpretation() ) ;
		for ( Person person : pop.getPersons().values() ) {

			Plan plan = person.getSelectedPlan() ; 
			// do this only for the selected plan in the assumption that the other ones are clean
			
			for ( PlanElement pe : plan.getPlanElements() ) {
				if ( pe instanceof Activity ) {
					Activity act = (Activity) pe ;
					
					if ( actDurInterp == ActivityDurationInterpretation.minOfDurationAndEndTime ) {
						
						// person stays at the activity either until its duration is over or until its end time, whatever comes first
						// do nothing
						
					} else if ( actDurInterp == ActivityDurationInterpretation.endTimeOnly ) {
						
						// always set duration to undefined:
						act.setMaximumDuration( Time.UNDEFINED_TIME ) ;
						
					} else if ( actDurInterp == ActivityDurationInterpretation.tryEndTimeThenDuration ) {
						
						// set duration to undefined if there is an activity end time:
						if ( act.getEndTime() != Time.UNDEFINED_TIME ) {
							act.setMaximumDuration(Time.UNDEFINED_TIME) ;
						}
						
					} else {
						throw new IllegalStateException("should not happen") ;
					}
					
					if ( config.vspExperimental().isRemovingUnneccessaryPlanAttributes() ) {
						act.setStartTime(Time.UNDEFINED_TIME) ;
					}
					
				} else if ( pe instanceof Leg ) {
					Leg leg = (Leg) pe ;
					if ( config.vspExperimental().isRemovingUnneccessaryPlanAttributes() ) {
						leg.setDepartureTime(Time.UNDEFINED_TIME) ; // given by activity end time; everything else confuses
						((LegImpl)leg).setArrivalTime(Time.UNDEFINED_TIME) ;
					}
				}
			}
									
		}
	}

}

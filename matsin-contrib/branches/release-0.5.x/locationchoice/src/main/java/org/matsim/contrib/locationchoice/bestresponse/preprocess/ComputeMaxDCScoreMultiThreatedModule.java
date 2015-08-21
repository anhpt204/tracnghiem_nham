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

package org.matsim.contrib.locationchoice.bestresponse.preprocess;

import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.locationchoice.bestresponse.DestinationSampler;
import org.matsim.contrib.locationchoice.bestresponse.DestinationChoiceBestResponseContext;
import org.matsim.contrib.locationchoice.bestresponse.scoring.DestinationScoring;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.population.algorithms.PlanAlgorithm;

public class ComputeMaxDCScoreMultiThreatedModule extends AbstractMultithreadedModule {	
	private String type;
	private TreeMap<Id, ActivityFacility> typedFacilities;
	private DestinationChoiceBestResponseContext lcContext;
	private static final Logger log = Logger.getLogger(ComputeMaxDCScoreMultiThreatedModule.class);
	private DestinationSampler sampler;
		
	public ComputeMaxDCScoreMultiThreatedModule(String type, DestinationChoiceBestResponseContext lcContext, DestinationSampler sampler) {
		super(lcContext.getScenario().getConfig().global().getNumberOfThreads());
		this.type = type;
		this.lcContext = lcContext;
		this.typedFacilities = ((ScenarioImpl) lcContext.getScenario()).
				getActivityFacilities().getFacilitiesForActivityType(lcContext.getConverter().convertType(type));
		if (this.typedFacilities.size() == 0) {
			log.warn("There are no facilities for type : " + type);
		} 
		this.sampler = sampler;
	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {
		DestinationScoring scorer = new DestinationScoring(this.lcContext);		
		return new ComputeMaxDCScorePlanAlgo(this.type, typedFacilities, scorer, this.sampler, lcContext);
	}
}

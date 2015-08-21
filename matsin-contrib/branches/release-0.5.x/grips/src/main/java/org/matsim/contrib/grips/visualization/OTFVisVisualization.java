/* *********************************************************************** *
 * project: org.matsim.*
 * OTFVisVisualization.java
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

package org.matsim.contrib.grips.visualization;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.otfvis.OTFVis;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.signalsystems.builder.FromDataBuilder;
import org.matsim.signalsystems.mobsim.QSimSignalEngine;
import org.matsim.signalsystems.mobsim.SignalEngine;
import org.matsim.vis.otfvis.OTFClientLive;
import org.matsim.vis.otfvis.OnTheFlyServer;


public class OTFVisVisualization {

	private static final Logger log = Logger.getLogger(OTFVisVisualization.class);
	private final String config;
	private final int it;
	private String baseURL = null;
	private String layer = null;
	
	
	public OTFVisVisualization(String string, int i) {
		this.config = string;
		this.it = i;
	}

	public OTFVisVisualization(String string, int i, String baseURL, String layer) {
		this.config = string;
		this.it = i;
		this.baseURL = baseURL;
		this.layer = layer;
	}

	public void run() {
		
		Config config = ConfigUtils.loadConfig(this.config);
		
		String plansfile = config.controler().getOutputDirectory() + "/ITERS/it." + this.it + "/" + this.it + ".plans.xml.gz";		
		log.info("changing plans file to: " + plansfile);
		config.plans().setInputFile(plansfile);
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		//MODIFY SCENARIO HERE
		new EvacuationScenarioCleaner(scenario).run();
		//
		
		System.gc();
		if (config.getQSimConfigGroup() == null){
			log.error("Cannot play live config without config module for QSim (in Java QSimConfigGroup). " +
					"Fixing this by adding default config module for QSim. " +
					"Please check if default values fit your needs, otherwise correct them in " +
					"the config given as parameter to get a valid visualization!");
			config.addQSimConfigGroup(new QSimConfigGroup());
		}
		
		EventsManager events = EventsUtils.createEventsManager();
		QSim qSim = (QSim) new QSimFactory().createMobsim(scenario, events);
		if (scenario.getConfig().scenario().isUseSignalSystems()){
			SignalEngine engine = new QSimSignalEngine(new FromDataBuilder(scenario, events).createAndInitializeSignalSystemsManager());
			qSim.addQueueSimulationListeners(engine);
		}

		OnTheFlyServer server = OTFVis.startServerAndRegisterWithQSim(config,scenario, events, qSim);
		if (this.baseURL != null && this.layer != null ) {
			OTFClientLiveWMS.run(config, server, this.baseURL, this.layer); 
		} else {
			OTFClientLive.run(config, server);
		}

		qSim.run();
		
		
		
	}
	
}

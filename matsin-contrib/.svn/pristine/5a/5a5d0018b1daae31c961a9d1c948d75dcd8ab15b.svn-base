/* *********************************************************************** *
 * project: org.matsim.*
 * WithinDayControllerListener.java
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

package org.matsim.contrib.evacuation.tutorial;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.events.ParallelEventsManagerImpl;
import org.matsim.core.mobsim.framework.events.MobsimInitializedEvent;
import org.matsim.core.mobsim.framework.listeners.FixedOrderSimulationListener;
import org.matsim.core.mobsim.framework.listeners.MobsimInitializedListener;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.population.PopulationFactoryImpl;
import org.matsim.core.population.routes.ModeRouteFactory;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelCostCalculatorFactory;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.DijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.functions.OnlyTravelDependentScoringFunctionFactory;
import org.matsim.withinday.mobsim.WithinDayEngine;
import org.matsim.withinday.mobsim.WithinDayQSimFactory;
import org.matsim.withinday.replanning.identifiers.ActivityEndIdentifierFactory;
import org.matsim.withinday.replanning.identifiers.InitialIdentifierImplFactory;
import org.matsim.withinday.replanning.identifiers.LeaveLinkIdentifierFactory;
import org.matsim.withinday.replanning.identifiers.interfaces.DuringActivityIdentifier;
import org.matsim.withinday.replanning.identifiers.interfaces.DuringLegIdentifier;
import org.matsim.withinday.replanning.identifiers.interfaces.InitialIdentifier;
import org.matsim.withinday.replanning.identifiers.tools.ActivityReplanningMap;
import org.matsim.withinday.replanning.identifiers.tools.LinkReplanningMap;
import org.matsim.withinday.replanning.identifiers.tools.SelectHandledAgentsByProbability;
import org.matsim.withinday.replanning.modules.ReplanningModule;
import org.matsim.withinday.replanning.replanners.CurrentLegReplannerFactory;
import org.matsim.withinday.replanning.replanners.InitialReplannerFactory;
import org.matsim.withinday.replanning.replanners.NextLegReplannerFactory;
import org.matsim.withinday.replanning.replanners.interfaces.WithinDayDuringActivityReplannerFactory;
import org.matsim.withinday.replanning.replanners.interfaces.WithinDayDuringLegReplannerFactory;
import org.matsim.withinday.replanning.replanners.interfaces.WithinDayInitialReplannerFactory;
import org.matsim.withinday.trafficmonitoring.TravelTimeCollector;
import org.matsim.withinday.trafficmonitoring.TravelTimeCollectorFactory;

public class WithinDayControllerListener implements StartupListener, BeforeMobsimListener, AfterMobsimListener, MobsimInitializedListener {

	/**
	 * Define the probability that an Agent uses the replanning 
	 * strategy. It is possible to assign multiple strategies 
	 * to the agents.
	 */
	private double pInitialReplanning = 0.0;
	private double pDuringActivityReplanning = 1.0;
	private double pDuringLegReplanning = 1.0;
	
	private int lastIteration = 0;
	
	/**
	 * Define the objects that are needed for the replanning.
	 */
	private InitialIdentifier initialIdentifier;
	private DuringActivityIdentifier duringActivityIdentifier;
	private DuringLegIdentifier duringLegIdentifier;
	private WithinDayInitialReplannerFactory initialReplannerFactory;
	private WithinDayDuringActivityReplannerFactory duringActivityReplannerFactory;
	private WithinDayDuringLegReplannerFactory duringLegReplannerFactory;

	private ActivityReplanningMap activityReplanningMap;
	private LinkReplanningMap linkReplanningMap;
	private Map<String, TravelTime> travelTimes;
	private TravelDisutilityFactory costFactory;
	private WithinDayEngine withinDayEngine;
	private SelectHandledAgentsByProbability selector;
	private FixedOrderSimulationListener fosl;
	
	private static final Logger log = Logger.getLogger(WithinDayControllerListener.class);
	
	public WithinDayControllerListener() {
		log.info("Please call setControllerParameters(Controler controller) so configure the Controller.");
	}

	public void setControllerParameters(Controler controller) {
		
		/*
		 * Create and register a FixedOrderQueueSimulationListener
		 */
		fosl = new FixedOrderSimulationListener();
		controller.getMobsimListeners().add(fosl);
		
		/*
		 * Use a Scoring Function, that only scores the travel times!
		 */
		controller.setScoringFunctionFactory(new OnlyTravelDependentScoringFunctionFactory());
		
		/*
		 * Crate and initialize a ReplanningManager and a ReplaningFlagInitializer.
		 * Use a FixedOrderQueueSimulationListener to bundle the Listeners and
		 * ensure that they are started in the correct order.
		 */
		int numReplanningThreads = controller.getConfig().global().getNumberOfThreads();
		withinDayEngine = new WithinDayEngine(controller.getEvents());
		withinDayEngine.initializeReplanningModules(numReplanningThreads);

		/*
		 * Use a WithinDayQSimFactory
		 */
		controller.setMobsimFactory(new WithinDayQSimFactory(withinDayEngine));
		
		/*
		 * Add this as a ControlerListener
		 */
		controller.addControlerListener(this);
		
		/*
		 * Register this as SimulationListener
		 */
		fosl.addSimulationListener(this);		
	}
	
	/*
	 * At this point, the Controller has already initialized its EventsHandler.
	 */
	@Override
	public void notifyStartup(StartupEvent event) {
		
		/*
		 * Set the number of iterations
		 */
		event.getControler().getConfig().controler().setLastIteration(lastIteration);
		
		/*
		 * Throw an Exception if an unsupported EventsManager is used
		 */
		if (event.getControler().getEvents() instanceof ParallelEventsManagerImpl) {
			throw new RuntimeException("Using a ParallelEventsManagerImpl is not supported by the WithinDay Replanning Code. Please use an EventsMangerImpl or a SimStepParallelEventsManagerImpl.");
		}
		
		/*
		 * Create and initialize an ActivityReplanningMap and a LinkReplanningMap.
		 * They identify, when an agent is going to end an activity and when an
		 * agent might be able to leave a link.
		 */
		activityReplanningMap = new ActivityReplanningMap();
		event.getControler().getEvents().addHandler(activityReplanningMap);
		fosl.addSimulationListener(activityReplanningMap);
		linkReplanningMap = new LinkReplanningMap(event.getControler().getNetwork());
		event.getControler().getEvents().addHandler(linkReplanningMap);
		fosl.addSimulationListener(linkReplanningMap);
		
		/*
		 * Create and initialize the travel time calculator which is used by
		 * the replanning modules.
		 * Here we use a TravelTimeCollector which collects the travel times for
		 * each link and returns the average travel time from the past few minutes.
		 * It has to be registered as SimulationListener and as an EventsHandler.
		 */
		travelTimes = new HashMap<String, TravelTime>();
		TravelTimeCollector carTravelTime = new TravelTimeCollectorFactory().createTravelTimeCollector(event.getControler().getScenario(), null);
		travelTimes.put(TransportMode.car, carTravelTime);
		fosl.addSimulationListener(carTravelTime);
		event.getControler().getEvents().addHandler(carTravelTime);

		/*
		 * Create and initialize a travel cost calculator which takes the travel
		 * times as travel costs.
		 */
		costFactory = new OnlyTimeDependentTravelCostCalculatorFactory();
	}
	
	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		
		selector = new SelectHandledAgentsByProbability();
		fosl.addSimulationListener(selector);
	}

	@Override
	public void notifyMobsimInitialized(MobsimInitializedEvent e) {
		
		log.info("Initialize Replanning Routers");
		QSim sim = (QSim) e.getQueueSimulation();
				
		/**
		 * Create a within day replanning module which is based on an
		 * AbstractMultithreadedModule.
		 */
		LeastCostPathCalculatorFactory factory = new DijkstraFactory();
		ModeRouteFactory routeFactory = ((PopulationFactoryImpl) sim.getScenario().getPopulation().getFactory()).getModeRouteFactory();
		
		AbstractMultithreadedModule router = new ReplanningModule(sim.getScenario().getConfig(), sim.getScenario().getNetwork(), costFactory, travelTimes, factory, routeFactory);
		
		/**
		 * Create and identify the within day replanners and identifiers which select
		 * the agents that should adapt their plans.
		 */
		this.initialIdentifier = new InitialIdentifierImplFactory(sim).createIdentifier();
		this.selector.addIdentifier(this.initialIdentifier, this.pInitialReplanning);
		this.initialReplannerFactory = new InitialReplannerFactory(sim.getScenario(), this.withinDayEngine, router, 1.0);
		this.initialReplannerFactory.addIdentifier(this.initialIdentifier);
		this.withinDayEngine.addIntialReplannerFactory(this.initialReplannerFactory);
		
		this.duringActivityIdentifier = new ActivityEndIdentifierFactory(activityReplanningMap).createIdentifier();
		this.selector.addIdentifier(this.duringActivityIdentifier, this.pDuringActivityReplanning);
		this.duringActivityReplannerFactory = new NextLegReplannerFactory(sim.getScenario(),this.withinDayEngine, router, 1.0);
		this.duringActivityReplannerFactory.addIdentifier(this.duringActivityIdentifier);
		this.withinDayEngine.addDuringActivityReplannerFactory(this.duringActivityReplannerFactory);
		
		this.duringLegIdentifier = new LeaveLinkIdentifierFactory(linkReplanningMap).createIdentifier();
		this.selector.addIdentifier(this.duringLegIdentifier, this.pDuringLegReplanning);
		this.duringLegReplannerFactory = new CurrentLegReplannerFactory(sim.getScenario(), this.withinDayEngine, router, 1.0);
		this.duringLegReplannerFactory.addIdentifier(this.duringLegIdentifier);
		this.withinDayEngine.addDuringLegReplannerFactory(this.duringLegReplannerFactory);
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		
		/*
		 * Remove some SimulationListeners
		 */
//		fosl.removeSimulationListener(withinDayEngine);
		fosl.removeSimulationListener(selector);
		withinDayEngine = null;
		selector = null;
	}

}

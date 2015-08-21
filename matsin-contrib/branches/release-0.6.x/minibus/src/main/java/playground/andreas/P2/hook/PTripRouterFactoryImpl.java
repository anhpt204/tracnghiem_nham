/* *********************************************************************** *
 * project: org.matsim.*
 * TripRouterFactoryImpl.java
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
package playground.andreas.P2.hook;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationFactoryImpl;
import org.matsim.core.population.routes.ModeRouteFactory;
import org.matsim.core.router.TripRouterFactory;
import org.matsim.core.router.IntermodalLeastCostPathCalculator;
import org.matsim.core.router.RoutingContext;
import org.matsim.core.router.LegRouterWrapper;
import org.matsim.core.router.TransitRouterWrapper;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeAndDisutility;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.old.NetworkLegRouter;
import org.matsim.core.router.old.PseudoTransitLegRouter;
import org.matsim.core.router.old.TeleportationLegRouter;
import org.matsim.core.router.util.AStarLandmarksFactory;
import org.matsim.core.router.util.DijkstraFactory;
import org.matsim.core.router.util.FastAStarLandmarksFactory;
import org.matsim.core.router.util.FastDijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.pt.router.TransitRouterFactory;

/**
 * This class exists only to allow the transit schedule to be updated in each iteration
 * 
 * Default factory, which sets the routing modules according to the
 * config file.
 * @author aneumann, thibautd
 */
public class PTripRouterFactoryImpl implements TripRouterFactory {
	private static final Logger log =
		Logger.getLogger(PTripRouterFactoryImpl.class);

	private final Controler controler;
	private Config config;
	private Network network;
	private TravelDisutilityFactory travelDisutilityFactory;
	private TravelTime travelTime;
	private LeastCostPathCalculatorFactory leastCostPathAlgorithmFactory;
	private ModeRouteFactory modeRouteFactory;
	private PopulationFactory populationFactory;
	private final TransitRouterFactory transitRouterFactory;
	private Scenario scenario;

	public PTripRouterFactoryImpl(final Controler controler, TransitRouterFactory transitRouterFactory) {
		this.controler = controler;
		this.transitRouterFactory = transitRouterFactory;
	}


	/**
	 * Hook provided to change the {@link TripRouter}
	 * implementation whithout changing the configuration.
	 *
	 * @return a new unconfigured instance
	 */
	protected TripRouter instanciateTripRouter() {
		return new TripRouter();
	}

	@Override
	public TripRouter instantiateAndConfigureTripRouter(RoutingContext iterationContext) {
		// initialize here - controller should be fully initialized by now
		// use fields to keep the rest of the code clean and comparable
		
		this.config = controler.getScenario().getConfig();
		this.network = controler.getScenario().getNetwork();
		this.travelDisutilityFactory = controler.getTravelDisutilityFactory();
		this.travelTime = controler.getLinkTravelTimes();
		this.leastCostPathAlgorithmFactory = createDefaultLeastCostPathCalculatorFactory(controler.getScenario());
		this.modeRouteFactory = ((PopulationFactoryImpl) controler.getScenario().getPopulation().getFactory()).getModeRouteFactory();
		this.populationFactory = controler.getScenario().getPopulation().getFactory();
		this.scenario = controler.getScenario();
		
		// end of own code
		
		TripRouter tripRouter = instanciateTripRouter();

		PlansCalcRouteConfigGroup routeConfigGroup = config.plansCalcRoute();
		TravelDisutility travelCost =
			travelDisutilityFactory.createTravelDisutility(
					travelTime,
					config.planCalcScore() );

		LeastCostPathCalculator routeAlgo =
			leastCostPathAlgorithmFactory.createPathCalculator(
					network,
					travelCost,
					travelTime);

		FreespeedTravelTimeAndDisutility ptTimeCostCalc =
			new FreespeedTravelTimeAndDisutility(-1.0, 0.0, 0.0);
		LeastCostPathCalculator routeAlgoPtFreeFlow =
			leastCostPathAlgorithmFactory.createPathCalculator(
					network,
					ptTimeCostCalc,
					ptTimeCostCalc);

		if ( NetworkUtils.isMultimodal( network ) ) {
			// note: LinkImpl has a default allowed mode of "car" so that all links
			// of a monomodal network are actually restricted to car, making the check
			// of multimodality unecessary from a behavioral point of view.
			// However, checking the mode restriction for each link is expensive,
			// so it is not worth doing it if it is not necessary. (td, oct. 2012)
			if (routeAlgo instanceof IntermodalLeastCostPathCalculator) {
				((IntermodalLeastCostPathCalculator) routeAlgo).setModeRestriction(
					Collections.singleton( TransportMode.car ));
				((IntermodalLeastCostPathCalculator) routeAlgoPtFreeFlow).setModeRestriction(
					Collections.singleton( TransportMode.car ));
			}
			else {
				// this is impossible to reach when using the algorithms of org.matsim.*
				// (all implement IntermodalLeastCostPathCalculator)
				log.warn( "network is multimodal but least cost path algorithm is not an instance of IntermodalLeastCostPathCalculator!" );
			}
		}

		for (String mainMode : routeConfigGroup.getTeleportedModeFreespeedFactors().keySet()) {
			tripRouter.setRoutingModule(
					mainMode,
					new LegRouterWrapper(
						mainMode,
						populationFactory,
						new PseudoTransitLegRouter(
							network,
							routeAlgoPtFreeFlow,
							routeConfigGroup.getTeleportedModeFreespeedFactors().get( mainMode ),
							routeConfigGroup.getBeelineDistanceFactor(),
							modeRouteFactory)));
		}

		for (String mainMode : routeConfigGroup.getTeleportedModeSpeeds().keySet()) {
			tripRouter.setRoutingModule(
					mainMode,
					new LegRouterWrapper(
						mainMode,
						populationFactory,
						new TeleportationLegRouter(
							modeRouteFactory,
							routeConfigGroup.getTeleportedModeSpeeds().get( mainMode ),
							routeConfigGroup.getBeelineDistanceFactor())));
		}

		for ( String mainMode : routeConfigGroup.getNetworkModes() ) {
			tripRouter.setRoutingModule(
					mainMode,
					new LegRouterWrapper(
						mainMode,
						populationFactory,
						new NetworkLegRouter(
							network,
							routeAlgo,
							modeRouteFactory)));
		}

		if ( config.scenario().isUseTransit() ) {
			tripRouter.setRoutingModule(
					TransportMode.pt,
					 new TransitRouterWrapper(
						transitRouterFactory.createTransitRouter(),
						
						// this line is the reason why this class exists in my playground 
						this.scenario.getTransitSchedule(),
						// end of modification
						
						scenario.getNetwork(), // use a walk router in case no PT path is found
						new LegRouterWrapper(
							TransportMode.transit_walk,
							populationFactory,
							new TeleportationLegRouter(
								modeRouteFactory,
								routeConfigGroup.getTeleportedModeSpeeds().get( TransportMode.walk ),
								routeConfigGroup.getBeelineDistanceFactor()))));
		}

		return tripRouter;
	}
	
	private LeastCostPathCalculatorFactory createDefaultLeastCostPathCalculatorFactory(Scenario scenario) {
		Config config = scenario.getConfig();
		if (config.controler().getRoutingAlgorithmType().equals(ControlerConfigGroup.RoutingAlgorithmType.Dijkstra)) {
            return new DijkstraFactory();
        } else if (config.controler().getRoutingAlgorithmType().equals(ControlerConfigGroup.RoutingAlgorithmType.AStarLandmarks)) {
            return new AStarLandmarksFactory(
                    scenario.getNetwork(), new FreespeedTravelTimeAndDisutility(config.planCalcScore()), config.global().getNumberOfThreads());
        } else if (config.controler().getRoutingAlgorithmType().equals(ControlerConfigGroup.RoutingAlgorithmType.FastDijkstra)) {
            return new FastDijkstraFactory();
        } else if (config.controler().getRoutingAlgorithmType().equals(ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks)) {
            return new FastAStarLandmarksFactory(
                    scenario.getNetwork(), new FreespeedTravelTimeAndDisutility(config.planCalcScore()));
        } else {
            throw new IllegalStateException("Enumeration Type RoutingAlgorithmType was extended without adaptation of Controler!");
        }
	}
}


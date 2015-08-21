/* *********************************************************************** *
 * project: org.matsim.*
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

package org.matsim.contrib.dvrp.run;

import java.io.File;

import org.matsim.analysis.*;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.dvrp.data.*;
import org.matsim.contrib.dvrp.data.file.VehicleReader;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.*;
import org.matsim.contrib.dvrp.router.*;
import org.matsim.contrib.dvrp.util.time.TimeDiscretizer;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dvrp.vrpagent.*;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.agents.*;
import org.matsim.core.network.*;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.router.util.*;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.*;


public class VrpLauncherUtils
{
    public static final int MAX_TIME = 36 * 60 * 60;

    //only the free-flow speed should decide on the movement of vehicles
    public static final double VARIANT_NETWORK_FLOW_CAP_FACTOR = 100;


    public enum TravelTimeSource
    {
        FREE_FLOW_SPEED, //time-variant (CYCLIC_24_HOURS) or invariant (CYCLIC_15_MIN); depends on the network type
        EVENTS; // based on eventsFileName, averaged over a whole day (CYCLIC_24_HOURS) or 15-minute time intervals (CYCLIC_15_MIN) 
    }


    public enum TravelDisutilitySource
    {
        DISTANCE, TIME;
    }


    public static Scenario initScenario(String netFileName, String plansFileName)
    {
        return initScenario(netFileName, plansFileName, null);
    }


    public static Scenario initScenario(String netFileName, String plansFileName,
            String changeEventsFileName)
    {
        Scenario scenario = ScenarioUtils.createScenario(VrpConfigUtils.createConfig());
        NetworkImpl network = (NetworkImpl)scenario.getNetwork();

        if (changeEventsFileName != null) {
            scenario.getConfig().network().setTimeVariantNetwork(true);
            scenario.getConfig().qsim().setFlowCapFactor(VARIANT_NETWORK_FLOW_CAP_FACTOR);
            network.getFactory().setLinkFactory(new TimeVariantLinkFactory());
        }

        new MatsimNetworkReader(scenario).readFile(netFileName);

        if (changeEventsFileName != null) {
            NetworkChangeEventsParser parser = new NetworkChangeEventsParser(network);
            parser.parse(changeEventsFileName);
            network.setNetworkChangeEvents(parser.getEvents());
        }

        new MatsimPopulationReader(scenario).readFile(plansFileName);
        return scenario;
    }


    public static TravelTime initTravelTime(Scenario scenario, TravelTimeSource ttimeSource,
            String eventsFileName)
    {
        switch (ttimeSource) {
            case FREE_FLOW_SPEED:
                return new FreeSpeedTravelTime();

            case EVENTS:
                scenario.getConfig().travelTimeCalculator()
                        .setTraveltimeBinSize(TimeDiscretizer.CYCLIC_15_MIN.getTimeInterval());
                TravelTimeCalculator ttCalculator = TravelTimeCalculators
                        .createTravelTimeCalculator(scenario);
                return TravelTimeCalculators.createTravelTimeFromEvents(eventsFileName,
                        ttCalculator);

            default:
                throw new IllegalArgumentException();
        }
    }


    public static TravelDisutility initTravelDisutility(TravelDisutilitySource tdisSource,
            TravelTime travelTime)
    {
        switch (tdisSource) {
            case DISTANCE:
                return new DistanceAsTravelDisutility();

            case TIME:
                return new TimeAsTravelDisutility(travelTime);

            default:
                throw new IllegalArgumentException();
        }
    }


    public static VrpData initVrpData(MatsimVrpContext context, String vehiclesFileName)
    {
        VrpData vrpData = new VrpDataImpl();
        new VehicleReader(context.getScenario(), vrpData).parse(vehiclesFileName);
        return vrpData;
    }


    public static PassengerEngine initPassengerEngine(String mode,
            PassengerRequestCreator requestCreator, VrpOptimizer optimizer,
            MatsimVrpContext context, QSim qSim)
    {
        PassengerEngine passengerEngine = new PassengerEngine(mode, requestCreator, optimizer,
                context);
        qSim.addMobsimEngine(passengerEngine);
        qSim.addDepartureHandler(passengerEngine);
        return passengerEngine;
    }


    public static void initAgentSources(QSim qSim, MatsimVrpContext context,
            VrpOptimizer optimizer, DynActionCreator actionCreator)
    {
        qSim.addAgentSource(new VrpAgentSource(actionCreator, context, optimizer, qSim));
        qSim.addAgentSource(new PopulationAgentSource(context.getScenario().getPopulation(),
                new DefaultAgentFactory(qSim), qSim));
    }


    public static void writeHistograms(LegHistogram legHistogram, String histogramOutDirName)
    {
        new File(histogramOutDirName).mkdir();
        legHistogram.write(histogramOutDirName + "legHistogram_all.txt");
        LegHistogramChart.writeGraphic(legHistogram, histogramOutDirName + "legHistogram_all.png");
        for (String legMode : legHistogram.getLegModes()) {
            LegHistogramChart.writeGraphic(legHistogram, histogramOutDirName + "legHistogram_"
                    + legMode + ".png", legMode);
        }
    }
}

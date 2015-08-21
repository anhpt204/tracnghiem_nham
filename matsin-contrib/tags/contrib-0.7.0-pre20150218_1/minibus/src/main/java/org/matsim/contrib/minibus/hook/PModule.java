/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * Builder.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2014 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package org.matsim.contrib.minibus.hook;

import org.matsim.contrib.minibus.PConfigGroup;
import org.matsim.contrib.minibus.stats.PStatsModule;
import org.matsim.contrib.minibus.stats.abtractPAnalysisModules.PtMode2LineSetter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.TripRouterFactory;

public class PModule {
    private AgentsStuckHandlerImpl agentsStuckHandler = null;
    private PersonReRouteStuckFactory stuckFactory = null;
    private PtMode2LineSetter lineSetter = null;
    private PTransitRouterFactory pTransitRouterFactory = null;
    private Class<? extends TripRouterFactory> tripRouterFactory = null;

    public void setLineSetter(PtMode2LineSetter lineSetter) {
        this.lineSetter = lineSetter;
    }
    public void setPTransitRouterFactory(PTransitRouterFactory pTransitRouterFactory) {
        this.pTransitRouterFactory = pTransitRouterFactory;
    }
    public void setStuckFactory(PersonReRouteStuckFactory stuckFactory) {
        this.stuckFactory = stuckFactory;
    }
    public void setTripRouterFactory(Class<? extends TripRouterFactory> tripRouterFactory) {
        this.tripRouterFactory = tripRouterFactory;
    }
    public void configureControler(Controler controler) {
        PConfigGroup pConfig = ConfigUtils.addOrGetModule(controler.getConfig(), PConfigGroup.GROUP_NAME, PConfigGroup.class);
        pConfig.validate();
        PBox pBox = new PBox(pConfig);
        controler.setMobsimFactory(new PQSimFactory());

        if (pTransitRouterFactory == null) {
            pTransitRouterFactory = new PTransitRouterFactory(pConfig.getPtEnabler(), pConfig.getPtRouter(), pConfig.getEarningsPerBoardingPassenger(), pConfig.getEarningsPerKilometerAndPassenger() / 1000.0);
        }
        controler.setTripRouterFactory(PTripRouterFactoryFactory.getTripRouterFactoryInstance(controler, tripRouterFactory, this.pTransitRouterFactory));

        if (pConfig.getReRouteAgentsStuck()) {
            this.agentsStuckHandler = new AgentsStuckHandlerImpl();
            if(stuckFactory == null) {
                this.stuckFactory = new PersonReRouteStuckFactoryImpl();
            }
        }

        PStatsModule.configureControler(controler, pConfig, pBox, lineSetter);

        PControlerListener pHook = new PControlerListener(controler, pBox, pTransitRouterFactory, stuckFactory, agentsStuckHandler);
        controler.addControlerListener(pHook);


    }
}

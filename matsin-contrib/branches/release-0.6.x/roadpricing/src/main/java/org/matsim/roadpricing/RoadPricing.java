/* *********************************************************************** *
 * project: org.matsim.*
 * RoadPricing.java
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

package org.matsim.roadpricing;

import org.apache.log4j.Logger;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.Time;

/**
 * Integrates the RoadPricing functionality into the MATSim Controler.  Does the following:
 * <p/>
 * Initialization:<ul>
 * <li> Reads the road pricing scheme and adds it as a scenario element.
 * <li> Adds the CalcPaidToll events listener (to calculate the toll per agent).
 * <li> Adds the toll to the TravelDisutility for the router (by wrapping the pre-existing Travel Disutility object).
 * </ul>
 * After mobsim:<ul>
 * <li> Send toll as money events to agents.
 * </ul>
 * Will also generate and output some statistics ...
 *
 * @author mrieser
 */
public class RoadPricing implements StartupListener, AfterMobsimListener, IterationEndsListener {

	private final RoadPricingSchemeImpl scheme = new RoadPricingSchemeImpl();
	private CalcPaidToll tollCalc = null;
	private CalcAverageTolledTripLength cattl = null;

	final static private Logger log = Logger.getLogger(RoadPricing.class);
    private RoadPricingConfigGroup rpConfig;

    public RoadPricing() {
		Gbl.printBuildInfo("RoadPricing", "/org.matsim.contrib/roadpricing/revision.txt");
	}
	
	@Override
	public void notifyStartup(final StartupEvent event) {
		final Controler controler = event.getControler();
        rpConfig = ConfigUtils.addOrGetModule(controler.getConfig(), RoadPricingConfigGroup.GROUP_NAME, RoadPricingConfigGroup.class);
        if (rpConfig.isUseRoadpricing()) {
            String tollLinksFile = rpConfig.getTollLinksFile();

            if (tollLinksFile != null) {
                RoadPricingReaderXMLv1 rpReader = new RoadPricingReaderXMLv1(this.scheme);
                rpReader.parse(tollLinksFile);
            }


            event.getControler().getScenario().addScenarioElement(
                    RoadPricingScheme.ELEMENT_NAME,
                    scheme);

            // add the events handler to calculate the tolls paid by agents
            this.tollCalc = new CalcPaidToll(controler.getNetwork(), this.scheme);
            controler.getEvents().addHandler(this.tollCalc);

            // replace the travelCostCalculator with a toll-dependent one if required
            if (RoadPricingScheme.TOLL_TYPE_DISTANCE.equals(this.scheme.getType()) || RoadPricingScheme.TOLL_TYPE_CORDON.equals(this.scheme.getType())) {
                final TravelDisutilityFactory previousTravelCostCalculatorFactory = controler.getTravelDisutilityFactory();
                // area-toll requires a regular TravelCost, no toll-specific one.

                TravelDisutilityFactory travelCostCalculatorFactory = new TravelDisutilityFactory() {
                    @Override
                    public TravelDisutility createTravelDisutility(TravelTime timeCalculator, PlanCalcScoreConfigGroup cnScoringGroup) {
                        return new TravelDisutilityIncludingToll(previousTravelCostCalculatorFactory.createTravelDisutility(timeCalculator, cnScoringGroup),
                                RoadPricing.this.scheme, controler.getConfig() );
                    }
                };
                controler.setTravelDisutilityFactory(travelCostCalculatorFactory);
            }

            this.cattl = new CalcAverageTolledTripLength(controler.getNetwork(), this.scheme);
            controler.getEvents().addHandler(this.cattl);
        }
	}

	@Override
	public void notifyAfterMobsim(final AfterMobsimEvent event) {
        if (rpConfig.isUseRoadpricing()) {
            // evaluate the final tolls paid by the agents and add them to their scores
            this.tollCalc.sendMoneyEvents(Time.MIDNIGHT, event.getControler().getEvents());
        }
	}

	@Override
	public void notifyIterationEnds(final IterationEndsEvent event) {
        if (rpConfig.isUseRoadpricing()) {
            log.info("The sum of all paid tolls : " + this.tollCalc.getAllAgentsToll() + " Euro.");
            log.info("The number of people who paid toll : " + this.tollCalc.getDraweesNr());
            log.info("The average paid trip length : " + this.cattl.getAverageTripLength() + " m.");
        }
	}

	public RoadPricingScheme getRoadPricingScheme() {
		return this.scheme;
	}

	public double getAllAgentsToll() {
		return this.tollCalc.getAllAgentsToll();
	}

	public int getDraweesNr() {
		return this.tollCalc.getDraweesNr();
	}

	public double getAvgPaidTripLength() {
		return this.cattl.getAverageTripLength();
	}

}
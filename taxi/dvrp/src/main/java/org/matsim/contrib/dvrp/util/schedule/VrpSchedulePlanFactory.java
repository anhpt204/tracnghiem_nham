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

package org.matsim.contrib.dvrp.util.schedule;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.contrib.dynagent.util.DynPlanFactory;


public class VrpSchedulePlanFactory
    implements DynPlanFactory
{
    private Scenario scenario;


    public VrpSchedulePlanFactory(Scenario scenario)
    {
        this.scenario = scenario;
    }


    @Override
    public Plan create(DynAgent agent)
    {
        VrpAgentLogic agentLogic = (VrpAgentLogic)agent.getAgentLogic();
        return new VrpSchedulePlan(agentLogic.getVehicle(), scenario);
    }
}

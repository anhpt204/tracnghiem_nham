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

package org.matsim.contrib.dvrp.vrpagent;

import java.util.List;

import org.matsim.api.core.v01.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.util.schedule.VrpSchedulePlanFactory;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.contrib.dynagent.util.DynAgentWithPlan;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.vehicles.*;


public class VrpAgentSource
    implements AgentSource
{
    private final DynActionCreator nextActionCreator;

    private final MatsimVrpContext context;
    private final VrpOptimizer optimizer;
    private final QSim qSim;

    private VrpSchedulePlanFactory planFactory = null;


    public VrpAgentSource(DynActionCreator nextActionCreator, MatsimVrpContext context,
            VrpOptimizer optimizer, QSim qSim)
    {
        this.nextActionCreator = nextActionCreator;
        this.context = context;
        this.optimizer = optimizer;
        this.qSim = qSim;
    }


    @Override
    public void insertAgentsIntoMobsim()
    {
        List<Vehicle> vehicles = context.getVrpData().getVehicles();
        VehiclesFactory qSimVehicleFactory = VehicleUtils.getFactory();

        for (Vehicle vrpVeh : vehicles) {
            Id<Vehicle> id = vrpVeh.getId();
            Id<Link> startLinkId = vrpVeh.getStartLink().getId();

            VrpAgentLogic vrpAgentLogic = new VrpAgentLogic(optimizer, nextActionCreator, vrpVeh);
            DynAgent vrpAgent = new DynAgent(Id.createPersonId(id), startLinkId, qSim,
                    vrpAgentLogic);
            QVehicle mobsimVehicle = new QVehicle(qSimVehicleFactory.createVehicle(
                    Id.create(id, org.matsim.vehicles.Vehicle.class),
                    VehicleUtils.getDefaultVehicleType()));
            vrpAgent.setVehicle(mobsimVehicle);
            mobsimVehicle.setDriver(vrpAgent);

            qSim.addParkedVehicle(mobsimVehicle, startLinkId);
            if (planFactory != null) {
                qSim.insertAgentIntoMobsim(new DynAgentWithPlan(vrpAgent, planFactory));
            }
            else {
                qSim.insertAgentIntoMobsim(vrpAgent);
            }
        }
    }


    public void useDynAgentWithPlan(Scenario scenario)
    {
        planFactory = new VrpSchedulePlanFactory(scenario);
    }
}

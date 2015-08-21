/* *********************************************************************** *
 * project: kai
 * Main.java
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

package tutorial.programming.ownMobsimAgent;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.ControlerConfigGroup.MobsimType;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.MobsimRegistrar;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleUtils;

/**
 * @author nagel
 *
 */
public class AgentSourceExample {

	public static void main(String[] args) {
		final Controler controler = new Controler("examples/tutorial/config/example5-config.xml");
        controler.setMobsimFactory(new MobsimFactory() {
            @Override
            public Mobsim createMobsim(Scenario sc, EventsManager eventsManager) {
                sc.getConfig().qsim().setEndTime(25*60*60);
                sc.getConfig().controler().setLastIteration(0);
                sc.getPopulation().getPersons().clear();
                MobsimFactory factory = new MobsimRegistrar().getFactoryRegister().getInstance(MobsimType.qsim.toString());
                final QSim qsim = (QSim) factory.createMobsim(sc, eventsManager);
                qsim.addAgentSource(new AgentSource() {
                    @Override
                    public void insertAgentsIntoMobsim() {
                        // insert traveler agent:
                        final MobsimAgent ag = new MyMobsimAgent(qsim.getScenario(), qsim.getSimTimer());
                        qsim.insertAgentIntoMobsim(ag);

                        // insert vehicle:
                        final Vehicle vehicle = VehicleUtils.getFactory().createVehicle(Id.create(ag.getId(), Vehicle.class), VehicleUtils.getDefaultVehicleType());
                        Id<Link> linkId4VehicleInsertion = Id.createLinkId(1);
                        qsim.createAndParkVehicleOnLink(vehicle, linkId4VehicleInsertion);
                    }
                });
                return qsim;
            }
        });
        controler.run();
	}

}

/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package org.matsim.core.mobsim.qsim.qnetsimengine;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.vehicles.Vehicle;

class VehicularDepartureHandler implements DepartureHandler {

	/*
	 * What to do when some agent wants to drive their vehicle but it isn't there.
	 * Either teleport it to the agent's location (no matter what!), or have the agent wait
	 * for the car to arrive, or throw an exception.
	 */
	public static enum VehicleBehavior { TELEPORT, WAIT_UNTIL_IT_COMES_ALONG, EXCEPTION }

    private static final Logger log = Logger.getLogger(VehicularDepartureHandler.class);

	private int cntTeleportVehicle = 0;

	private final VehicleBehavior vehicleBehavior;

	private final QNetsimEngine qNetsimEngine;

	private final Collection<String> transportModes;

	VehicularDepartureHandler(QNetsimEngine qNetsimEngine, VehicleBehavior vehicleBehavior) {
		this.qNetsimEngine = qNetsimEngine;
		this.vehicleBehavior = vehicleBehavior;
		this.transportModes = qNetsimEngine.getMobsim().getScenario().getConfig().qsim().getMainModes();
	}

	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id<Link> linkId) {
		if (this.transportModes.contains(agent.getMode())) {
			if ( agent instanceof MobsimDriverAgent ) {
				handleCarDeparture(now, (MobsimDriverAgent)agent, linkId);
				return true;
			} else {
				throw new UnsupportedOperationException("wrong agent type to depart on a network mode");
			}
		}
		return false;
	}

	private void handleCarDeparture(double now, MobsimDriverAgent agent, Id<Link> linkId) {
		// The situation where a leg starts and ends at the same link used to be
		// handled specially, for all agents except AbstractTransitDriverAgents.
		// This however caused some problems in some cases, as apparently for taxicabs.
		// Thus, such trips are now simulated normally.
		// See MATSIM-233 for details. td apr'14
		Id<Vehicle> vehicleId = agent.getPlannedVehicleId() ;
		QLinkInternalI qlink = (QLinkInternalI) qNetsimEngine.getNetsimNetwork().getNetsimLink(linkId);
		QVehicle vehicle = qlink.removeParkedVehicle(vehicleId);
		if (vehicle == null) {
			if (vehicleBehavior == VehicleBehavior.TELEPORT) {
				vehicle = qNetsimEngine.getVehicles().get(vehicleId);
				if ( vehicle==null ) {
					// log a maximum of information, to help the user identifying the cause of the problem
					final String msg = "could not find requested vehicle "+vehicleId+" in simulation for agent "+agent+" with id "+agent.getId()+" on link "+agent.getCurrentLinkId()+" at time "+now+".";
					log.error( msg );
					log.error( "Note that, with AgentSource and if the agent starts on a leg, the "
							+ "vehicle needs to be inserted BEFORE the agent!") ;
					throw new RuntimeException( msg+" aborting ...") ;
				}
				teleportVehicleTo(vehicle, linkId);

				vehicle.setDriver(agent);
				agent.setVehicle(vehicle) ;

				qlink.letVehicleDepart(vehicle, now);
				// (since the "teleportVehicle" does not physically move the vehicle, this is finally achieved in the departure
				// logic.  kai, nov'11)
			} else if (vehicleBehavior == VehicleBehavior.WAIT_UNTIL_IT_COMES_ALONG) {
				// While we are waiting for our car
				qlink.registerDriverAgentWaitingForCar(agent);
			} else {
				throw new RuntimeException("vehicle " + vehicleId + " not available for agent " + agent.getId() + " on link " + linkId + " at time "+ now);
			}
		} else {
			vehicle.setDriver(agent);
			agent.setVehicle(vehicle) ;
			qlink.letVehicleDepart(vehicle, now);
		}
	}

	/**
	 * Design thoughts:<ul>
	 * <li> yyyyyy It is not completely clear what happens when the vehicle is used by someone else. kai, nov'11
	 * <li> Seems to me that a parked vehicle is teleported. kai, nov'11
	 * <li> yyyyyy Seems to me that a non-parked vehicle will end up with two references to it, with race conditions???? kai, nov11
	 * <li> yyyyyy Note that the "linkId" parameter is not used for any physical action!!
	 * </ul> 
	 */
	private void teleportVehicleTo(QVehicle vehicle, Id<Link> linkId) {
		if (vehicle.getCurrentLink() != null) {
			if (cntTeleportVehicle < 9) {
				cntTeleportVehicle++;
				log.info("teleport vehicle " + vehicle.getId() + " from link " + vehicle.getCurrentLink().getId() + " to link " + linkId);
				if (cntTeleportVehicle == 9) {
					log.info("No more occurrences of teleported vehicles will be reported.");
				}
			}
			QLinkInternalI qlinkOld = (QLinkInternalI) qNetsimEngine.getNetsimNetwork().getNetsimLink(vehicle.getCurrentLink().getId());
			qlinkOld.removeParkedVehicle(vehicle.getId());
		}
	}

}

/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package org.matsim.contrib.dvrp.passenger;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.*;
import org.matsim.core.mobsim.framework.MobsimAgent.State;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.interfaces.*;


public class PassengerEngine
    implements MobsimEngine, DepartureHandler
{
    private final String mode;

    private InternalInterface internalInterface;
    private final MatsimVrpContext context;
    private final PassengerRequestCreator requestCreator;
    private final VrpOptimizer optimizer;

    private final AdvancedRequestStorage advancedRequestStorage;
    private final AwaitingPickupStorage awaitingPickupStorage;


    public PassengerEngine(String mode, PassengerRequestCreator requestCreator,
            VrpOptimizer optimizer, MatsimVrpContext context)
    {
        this.mode = mode;
        this.requestCreator = requestCreator;
        this.optimizer = optimizer;
        this.context = context;

        advancedRequestStorage = new AdvancedRequestStorage(context);
        awaitingPickupStorage = new AwaitingPickupStorage();
    }


    @Override
    public void setInternalInterface(InternalInterface internalInterface)
    {
        this.internalInterface = internalInterface;
    }


    public String getMode()
    {
        return mode;
    }


    @Override
    public void onPrepareSim()
    {}


    @Override
    public void doSimStep(double time)
    {}


    @Override
    public void afterSim()
    {}


    /**
     * This is to register an advance booking.  The method is called when, in reality, the request is made.
     * 
     * @param now -- time when trip is booked
     * @param passenger
     * @param leg -- contains information about the departure time. yyyy Michal, Joschka, note that in MATSim leg departure times may
     * be meaningless; the only thing that truly matters is the activity end time.  Is your code defensive against that? kai, jul'14
     * @return
     */
    public boolean prebookTrip(double now, MobsimPassengerAgent passenger, Leg leg)
    {
        if (!leg.getMode().equals(mode)) {
            return false;
        }

        if (leg.getDepartureTime() <= now) {
            throw new IllegalStateException("This is not a call ahead");
        }

        Id fromLinkId = leg.getRoute().getStartLinkId();
        Id toLinkId = leg.getRoute().getEndLinkId();
        double departureTime = leg.getDepartureTime();

        PassengerRequest request = createRequest(passenger, fromLinkId, toLinkId, departureTime,
                now);
        advancedRequestStorage.storeAdvancedRequest(request);

        optimizer.requestSubmitted(request);
        return !request.isRejected();
    }


    @Override
    public boolean handleDeparture(double now, MobsimAgent agent, Id fromLinkId)
    {
        if (!agent.getMode().equals(mode)) {
            return false;
        }

        MobsimPassengerAgent passenger = (MobsimPassengerAgent)agent;

        Id toLinkId = passenger.getDestinationLinkId();
        double departureTime = now;

        internalInterface.registerAdditionalAgentOnLink(passenger);

        PassengerRequest request = advancedRequestStorage.retrieveAdvancedRequest(passenger,
                fromLinkId, toLinkId);

        if (request == null) {//this is an immediate request
            request = createRequest(passenger, fromLinkId, toLinkId, departureTime, now);
            optimizer.requestSubmitted(request);
        }
        else {
            PassengerPickupActivity awaitingPickup = awaitingPickupStorage
                    .retrieveAwaitingPickup(request);

            if (awaitingPickup != null) {
                awaitingPickup.notifyPassengerIsReadyForDeparture(passenger, now);
            }
        }

        return !request.isRejected();
    }


    //================ REQUESTS CREATION

    private int nextId = 0;


    private PassengerRequest createRequest(MobsimPassengerAgent passenger, Id fromLinkId,
            Id toLinkId, double departureTime, double now)
    {
        Map<Id, ? extends Link> links = context.getScenario().getNetwork().getLinks();
        Link fromLink = links.get(fromLinkId);
        Link toLink = links.get(toLinkId);
        Id id = context.getScenario().createId(mode + "_" + nextId++);

        PassengerRequest request = requestCreator.createRequest(id, passenger, fromLink, toLink,
                departureTime, departureTime, now);
        context.getVrpData().addRequest(request);
        return request;
    }


    //================ PICKUP / DROPOFF

    public boolean pickUpPassenger(PassengerPickupActivity pickupActivity,
            MobsimDriverAgent driver, PassengerRequest request, double now)
    {
        MobsimPassengerAgent passenger = request.getPassenger();
        Id linkId = driver.getCurrentLinkId();

        if (passenger.getCurrentLinkId() != linkId || passenger.getState() != State.LEG
                || !passenger.getMode().equals(mode)) {
            awaitingPickupStorage.storeAwaitingPickup(request, pickupActivity);
            return false;//wait for the passenger
        }

        if (internalInterface.unregisterAdditionalAgentOnLink(passenger.getId(),
                driver.getCurrentLinkId()) == null) {
            //the passenger has already been picked up and is on another taxi trip
            //seems there have been at least 2 requests made by this passenger for this location
            awaitingPickupStorage.storeAwaitingPickup(request, pickupActivity);
            return false;//wait for the passenger (optimistically, he/she should appear soon)
        }

        MobsimVehicle mobVehicle = driver.getVehicle();
        mobVehicle.addPassenger(passenger);
        passenger.setVehicle(mobVehicle);

        EventsManager events = internalInterface.getMobsim().getEventsManager();
        events.processEvent(new PersonEntersVehicleEvent(now, passenger.getId(), mobVehicle.getId()));

        return true;
    }


    public void dropOffPassenger(MobsimDriverAgent driver, PassengerRequest request, double now)
    {
        MobsimPassengerAgent passenger = request.getPassenger();

        MobsimVehicle mobVehicle = driver.getVehicle();
        mobVehicle.removePassenger(passenger);
        passenger.setVehicle(null);

        EventsManager events = internalInterface.getMobsim().getEventsManager();
        events.processEvent(new PersonLeavesVehicleEvent(now, passenger.getId(), mobVehicle.getId()));

        passenger.notifyArrivalOnLinkByNonNetworkMode(passenger.getDestinationLinkId());
        passenger.endLegAndComputeNextState(now);
        internalInterface.arrangeNextAgentState(passenger);
    }
}

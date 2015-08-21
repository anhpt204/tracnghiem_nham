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

package org.matsim.contrib.dvrp.examples.onetaxi;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.RequestImpl;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;


public class OneTaxiRequest
    extends RequestImpl
    implements PassengerRequest
{
    private final MobsimPassengerAgent passenger;
    private final Link fromLink;
    private final Link toLink;

    private OneTaxiServeTask pickupTask;
    private OneTaxiServeTask dropoffTask;


    public OneTaxiRequest(Id id, MobsimPassengerAgent passenger, Link fromLink, Link toLink,
            double time)
    {
        //I want a taxi now: t0 == t1 == submissionTime
        super(id, 1, time, time, time);
        this.passenger = passenger;
        this.fromLink = fromLink;
        this.toLink = toLink;
    }


    @Override
    public Link getFromLink()
    {
        return fromLink;
    }


    @Override
    public Link getToLink()
    {
        return toLink;
    }


    @Override
    public MobsimPassengerAgent getPassenger()
    {
        return passenger;
    }


    public OneTaxiServeTask getPickupTask()
    {
        return pickupTask;
    }


    public OneTaxiServeTask getDropoffTask()
    {
        return dropoffTask;
    }


    public void setPickupTask(OneTaxiServeTask pickupTask)
    {
        this.pickupTask = pickupTask;
    }


    public void setDropoffTask(OneTaxiServeTask dropoffTask)
    {
        this.dropoffTask = dropoffTask;
    }
}

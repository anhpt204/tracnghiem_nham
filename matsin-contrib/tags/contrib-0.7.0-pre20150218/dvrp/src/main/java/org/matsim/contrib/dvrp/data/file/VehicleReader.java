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

package org.matsim.contrib.dvrp.data.file;

import java.util.*;

import org.matsim.api.core.v01.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.*;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;


public class VehicleReader
    extends MatsimXmlParser
{
    private final static String VEHICLE = "vehicle";

    private VrpData data;
    private Map<Id<Link>, ? extends Link> links;


    public VehicleReader(Scenario scenario, VrpData data)
    {
        this.data = data;

        links = scenario.getNetwork().getLinks();
    }


    @Override
    public void startTag(String name, Attributes atts, Stack<String> context)
    {
        if (VEHICLE.equals(name)) {
            startVehicle(atts);
        }
    }


    @Override
    public void endTag(String name, String content, Stack<String> context)
    {}


    private void startVehicle(Attributes atts)
    {
        Id<Vehicle> id = Id.create(atts.getValue("id"), Vehicle.class);

        Id<Link> startLinkId = Id.create(atts.getValue("start_link"), Link.class);
        Link startLink = links.get(startLinkId);

        double capacity = ReaderUtils.getDouble(atts, "capacity", 1);

        double t0 = ReaderUtils.getDouble(atts, "t_0", 0);
        double t1 = ReaderUtils.getDouble(atts, "t_1", 24 * 60 * 60);

        data.addVehicle(new VehicleImpl(id, startLink, capacity, t0, t1));
    }
}

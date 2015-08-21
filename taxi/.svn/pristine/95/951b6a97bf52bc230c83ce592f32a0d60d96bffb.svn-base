
package org.matsim.contrib.sarp.data;

import java.util.*;

import org.matsim.api.core.v01.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.*;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.contrib.dvrp.extensions.electric.*;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;


public class ElectricVehicleReader
    extends MatsimXmlParser
{
    private final static String VEHICLE = "vehicle";

    private VrpData data;
    private Map<Id<Link>, ? extends Link> links;


    public ElectricVehicleReader(Scenario scenario, VrpData data)
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

        ElectricVehicle ev = new ElectricVehicleImpl(id, startLink, capacity, t0, t1);

        double batteryCharge = ReaderUtils.getDouble(atts, "battery_charge", 20) * 1000 * 3600;
        double batteryCapacity = ReaderUtils.getDouble(atts, "battery_capacity", 20) * 1000 * 3600;

        Battery battery = new BatteryImpl(batteryCharge, batteryCapacity);
        ev.setBattery(battery);

        data.addVehicle(ev);
    }
}

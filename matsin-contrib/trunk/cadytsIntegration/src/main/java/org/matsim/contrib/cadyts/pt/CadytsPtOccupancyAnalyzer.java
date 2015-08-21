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

package org.matsim.contrib.cadyts.pt;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.utils.misc.Time;
import org.matsim.counts.Count;
import org.matsim.counts.Counts;
import org.matsim.counts.Volume;
import org.matsim.pt.counts.SimpleWriter;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import cadyts.measurements.SingleLinkMeasurement.TYPE;
import cadyts.supply.SimResults;

/**
 * Collects occupancy data of transit-line stations
 * <p/>
 * This is probably similar to code elsewhere.  However, it makes some sense to keep this here since the correct workings of cadyts 
 * (obviously) depends on the fact that the counts are actually what it thinks, and so it makes sense to decouple this from the upstream
 * counting method and leave it here. kai, sep'13 
 */
class CadytsPtOccupancyAnalyzer implements TransitDriverStartsEventHandler, PersonEntersVehicleEventHandler,
		PersonLeavesVehicleEventHandler, VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler 
		, SimResults<TransitStopFacility> {

	private static final long serialVersionUID = 1L;
	private final int timeBinSize, maxSlotIndex;
	private final double maxTime;
	private Map<Id<TransitStopFacility>, int[]> occupancies; // Map< stopFacilityId,value[]>
	private final Map<Id<Vehicle>, Id<TransitStopFacility>> vehStops = new HashMap<>(); // Map< vehId,stopFacilityId>
	private final Map<Id<Vehicle>, Integer> vehPassengers = new HashMap<>(); // Map<vehId,passengersNo.in Veh>
	private StringBuffer occupancyRecord = new StringBuffer("time\tvehId\tStopId\tno.ofPassengersInVeh\n");
	private final Set<Id> analyzedTransitDrivers = new HashSet<Id>();
	private final Set<Id> analyzedTransitVehicles = new HashSet<Id>();
	private final Set<Id<TransitLine>> calibratedLines;

	public CadytsPtOccupancyAnalyzer(final Set<Id<TransitLine>> calibratedLines, int timeBinSize_s ) {
		this.calibratedLines = calibratedLines;
		this.timeBinSize = timeBinSize_s ;

		this.maxTime = Time.MIDNIGHT-1; //24 * 3600 - 1;
		// (yy not completely clear if it might be better to use 24*this.timeBimSize, but it is overall not so great
		// to have this hardcoded.  kai/manuel, jul'12)

		this.maxSlotIndex = ((int) this.maxTime) / this.timeBinSize + 1;
		this.occupancies = new HashMap<>();
	}

	@Override
	public void reset(final int iteration) {
		this.occupancies.clear();
		this.vehStops.clear();
		this.vehPassengers.clear();
		this.occupancyRecord = new StringBuffer("time\tvehId\tStopId\tno.ofPassengersInVeh\n");
		this.analyzedTransitDrivers.clear();
		this.analyzedTransitVehicles.clear();
	}

	@Override
	public void handleEvent(final TransitDriverStartsEvent event) {
		if (this.calibratedLines.contains(event.getTransitLineId())) {
			this.analyzedTransitDrivers.add(event.getDriverId());
			this.analyzedTransitVehicles.add(event.getVehicleId());
		}
	}

	@Override
	public void handleEvent(final PersonEntersVehicleEvent event) {
		if (this.analyzedTransitDrivers.contains(event.getPersonId()) || !this.analyzedTransitVehicles.contains(event.getVehicleId())) {
			return; // ignore transit drivers or persons entering non-(analyzed-)transit vehicles
		}

		// ------------------veh_passenger- (for occupancy)-----------------
		Id<Vehicle> vehId = event.getVehicleId();
		Id<TransitStopFacility> stopId = this.vehStops.get(vehId);
		double time = event.getTime();
		Integer nPassengers = this.vehPassengers.get(vehId);
		this.vehPassengers.put(vehId, (nPassengers != null) ? (nPassengers + 1) : 1);
		this.occupancyRecord.append("time :\t" + time + " veh :\t" + vehId + " has Passenger\t" + this.vehPassengers.get(vehId)
				+ " \tat stop :\t" + stopId + " ENTERING PERSON :\t" + event.getPersonId() + "\n");
	}

	@Override
	public void handleEvent(final PersonLeavesVehicleEvent event) {
		if (this.analyzedTransitDrivers.contains(event.getPersonId()) || !this.analyzedTransitVehicles.contains(event.getVehicleId())) {
			return; // ignore transit drivers or persons entering non-(analyzed-)transit vehicles
		}

		// ----------------veh_passenger-(for occupancy)--------------------------
		Id<Vehicle> vehId = event.getVehicleId();
		double time = event.getTime();
		Integer nPassengers = this.vehPassengers.get(vehId);
		if (nPassengers == null) {
			throw new RuntimeException("null passenger-No. in vehicle ?");
		}
		this.vehPassengers.put(vehId, nPassengers - 1);
		if (this.vehPassengers.get(vehId).intValue() == 0) {
			this.vehPassengers.remove(vehId);
		}
		Integer passengers = this.vehPassengers.get(vehId);
		this.occupancyRecord.append("time :\t" + time + " veh :\t" + vehId + " has Passenger\t"
				+ ((passengers != null) ? passengers : 0) + "\n");
	}

	@Override
	public void handleEvent(final VehicleDepartsAtFacilityEvent event) {
		Id<Vehicle> vehId = event.getVehicleId();
		Id<TransitStopFacility> facId = event.getFacilityId();

		// -----------------------occupancy--------------------------------
		this.vehStops.remove(vehId);
		int[] occupancyAtStop = this.occupancies.get(facId);
		if (occupancyAtStop == null) { // no previous departure from this stop, therefore no occupancy
																		// record yet. Create this:
			occupancyAtStop = new int[this.maxSlotIndex + 1];
			this.occupancies.put(facId, occupancyAtStop);
		}

		Integer noPassengersInVeh = this.vehPassengers.get(vehId);

		if (noPassengersInVeh != null) {
			occupancyAtStop[this.getTimeSlotIndex(event.getTime())] += noPassengersInVeh;
			this.occupancyRecord.append(event.getTime());
			this.occupancyRecord.append("\t");
			this.occupancyRecord.append(vehId);
			this.occupancyRecord.append("\t");
			this.occupancyRecord.append(facId);
			this.occupancyRecord.append("\t");
			this.occupancyRecord.append(noPassengersInVeh);
			this.occupancyRecord.append("\n");
		}
	}

	@Override
	public void handleEvent(final VehicleArrivesAtFacilityEvent event) {
		Id<TransitStopFacility> stopId = event.getFacilityId();

		this.vehStops.put(event.getVehicleId(), stopId);
		// (constructing a table with vehId as key, and stopId as value; constructed when veh arrives at
		// stop; necessary
		// since personEnters/LeavesVehicle does not carry stop id)
	}

	private int getTimeSlotIndex(final double time) {
		if (time > this.maxTime) {
			return this.maxSlotIndex;
		}
		return ((int) time / this.timeBinSize);
	}

	/**
	 * @param stopId
	 * @return Array containing the number of passengers in bus after the transfer at the stop
	 *         {@code stopId} per time bin, starting with time bin 0 from 0 seconds to
	 *         (timeBinSize-1)seconds.
	 */
	@Deprecated // try to use request that also contains time instead
	int[] getOccupancyVolumesForStop(final Id<TransitStopFacility> stopId) {
		return this.occupancies.get(stopId);
	}
	 public int getOccupancyVolumeForStopAndTime(final Id<TransitStopFacility> stopId, final int time_s ) {
		 if ( this.occupancies.get(stopId) != null ) {
			 int timeBinIndex = getTimeSlotIndex( time_s ) ;
			 return this.occupancies.get(stopId)[timeBinIndex] ;
		 } else {
			 return 0 ;
		 }
	 }

	public Set<Id<TransitStopFacility>> getOccupancyStopIds() {
		return this.occupancies.keySet();
	}

	public void writeResultsForSelectedStopIds(final String filename, final Counts occupCounts, final Collection<Id<TransitStopFacility>> stopIds) {
		SimpleWriter writer = new SimpleWriter(filename);

		final String TAB = "\t";
		final String NL = "\n";

		// write header
		writer.write("stopId\t");
		for (int i = 0; i < 24; i++) {
			writer.write("oc" + i + "-" + (i + 1) + TAB);
		}
		for (int i = 0; i < 24; i++) {
			writer.write("scalSim" + i + "-" + (i + 1) + TAB);
		}
		writer.write("coordinate\tcsId\n");

		// write content
		for (Id<TransitStopFacility> stopId : stopIds) {
			// get count data
			Count count = occupCounts.getCounts().get(Id.create(stopId, Link.class));
			if (!occupCounts.getCounts().containsKey(Id.create(stopId, Link.class))) {
				continue;
			}

			// get sim-Values
			int[] ocuppancy = this.occupancies.get(stopId);
			writer.write(stopId.toString() + TAB);
			for (int i = 0; i < ocuppancy.length; i++) {
				Volume v = count.getVolume(i + 1);
				if (v != null) {
					writer.write(v.getValue() + TAB);
				} else {
					writer.write("n/a" + TAB);
				}
			}
			for (int i = 0; i < ocuppancy.length; i++) {
				writer.write((ocuppancy != null ? ocuppancy[i] : 0) + TAB);
			}
			writer.write(count.getCoord().toString() + TAB + count.getCsId() + NL);
		}
		writer.write(this.occupancyRecord.toString());
		writer.close();
	}

	@Override
	public double getSimValue(TransitStopFacility link, int startTimeS, int endTimeS, TYPE type) {
		final int tmp = (endTimeS - startTimeS) % 3600;
		if ( tmp != 0  || tmp != 1 ){ // the specification is that it should go from, say, 3600(inc.) to 7200(excl.), but I am finding 7199 as well. kai, sep'14
			throw new RuntimeException("this only works for time spans that are multiples of hours. kai, sep'14") ;
		}
		double sum = 0. ;
		int cnt = 0 ;
		for ( int sec = startTimeS ; sec < endTimeS ; sec += 3600 ) { // no second contribution both for endTimeS=7199 and 7200
			sum += this.getOccupancyVolumeForStopAndTime(link.getId(), startTimeS) ;
			cnt++ ;
		}
		switch (type){
		case COUNT_VEH:
			return sum ;
		case FLOW_VEH_H:
			return sum / cnt ; 
		default:
			throw new RuntimeException("not implemented") ;
		}
	}

	@Override
	public String toString() {
		final StringBuffer stringBuffer2 = new StringBuffer();
		final String STOPID = "stopId: ";
		final String VALUES = "; values:";
		final char TAB = '\t';
		final char RETURN = '\n';

		for (Id<TransitStopFacility> stopId : this.getOccupancyStopIds()) { // Only occupancy!
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(STOPID);
			stringBuffer.append(stopId);
			stringBuffer.append(VALUES);

			boolean hasValues = false; // only prints stops with volumes > 0
			int[] values = this.getOccupancyVolumesForStop(stopId);

			for (int ii = 0; ii < values.length; ii++) {
				hasValues = hasValues || (values[ii] > 0);

				stringBuffer.append(TAB);
				stringBuffer.append(values[ii]);
			}
			stringBuffer.append(RETURN);
			if (hasValues)
				stringBuffer2.append(stringBuffer.toString());

		}
		return stringBuffer2.toString();
	}


}

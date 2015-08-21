/* *********************************************************************** *
 * project: org.matsim.*
 * Facilities.java
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

package org.matsim.core.facilities;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.api.experimental.facilities.ActivityFacilitiesFactory;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.utils.objectattributes.ObjectAttributes;

/**
 * Maintainer: mrieser / Senozon AG
 * @author balmermi
 */
public class ActivityFacilitiesImpl implements ActivityFacilities {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private long counter = 0;
	private long nextMsg = 1;

	private static final Logger log = Logger.getLogger(ActivityFacilitiesImpl.class);
	private final ActivityFacilitiesFactory factory ;

	private final Map<Id<ActivityFacility>, ActivityFacility> facilities = new LinkedHashMap<Id<ActivityFacility>, ActivityFacility>();

	private String name;

	private final ObjectAttributes facilityAttributes = new ObjectAttributes();

	//////////////////////////////////////////////////////////////////////
	// constructor
	//////////////////////////////////////////////////////////////////////

	@Deprecated // use creational method in FacilitiesUtils instead.  kai, feb'14
	public ActivityFacilitiesImpl(final String name) {
		this.name = name;
		this.factory = new ActivityFacilitiesFactoryImpl();
	}

	@Deprecated // use creational method in FacilitiesUtils instead.  kai, feb'14
	public ActivityFacilitiesImpl() {
		this(null);
	}

	//////////////////////////////////////////////////////////////////////
	// create methods
	//////////////////////////////////////////////////////////////////////

	public final ActivityFacilityImpl createAndAddFacility(final Id<ActivityFacility> id, final Coord center) {
		if (this.facilities.containsKey(id)) {
			throw new IllegalArgumentException("Facility with id=" + id + " already exists.");
		}
		ActivityFacilityImpl f = new ActivityFacilityImpl(id, center);
		this.facilities.put(f.getId(),f);

		// show counter
		this.counter++;
		if (this.counter % this.nextMsg == 0) {
			this.nextMsg *= 2;
			printFacilitiesCount();
		}

		return f;
	}

	@Override
	public ActivityFacilitiesFactory getFactory() {
		return this.factory;
	}

	@Override
	public final Map<Id<ActivityFacility>, ? extends ActivityFacility> getFacilities() {
		return this.facilities;
	}

	@Override
	public final TreeMap<Id<ActivityFacility>, ActivityFacility> getFacilitiesForActivityType(final String act_type) {
		TreeMap<Id<ActivityFacility>, ActivityFacility> facs = new TreeMap<Id<ActivityFacility>, ActivityFacility>();
		Iterator<ActivityFacility> iter = this.facilities.values().iterator();
		while (iter.hasNext()){
			ActivityFacility f = iter.next();
			Map<String, ? extends ActivityOption> a = f.getActivityOptions();
			if(a.containsKey(act_type)){
				facs.put(f.getId(),f);
			}
		}
		return facs;
	}

	public final void printFacilitiesCount() {
		log.info("    facility # " + this.counter);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addActivityFacility(ActivityFacility facility) {
		// validation
		if (this.facilities.containsKey(facility.getId())) {
			throw new IllegalArgumentException("Facility with id=" + facility.getId() + " already exists.");
		}

		this.facilities.put(facility.getId(),facility);
	}

	@Override
	public ObjectAttributes getFacilityAttributes() {
		return this.facilityAttributes;
	}

}

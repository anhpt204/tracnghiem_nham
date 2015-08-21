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

package org.matsim.contrib.locationchoice.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.groups.LocationChoiceConfigGroup;
import org.matsim.core.facilities.ActivityFacilityImpl;
import org.matsim.core.facilities.ActivityOption;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.geometry.CoordUtils;

public class TreesBuilder {

	private Network network = null;
	private static final Logger log = Logger.getLogger(TreesBuilder.class);
	private HashSet<String> flexibleTypes = new HashSet<String>();
	private final LocationChoiceConfigGroup config;

	protected TreeMap<String, QuadTreeRing<ActivityFacility>> quadTreesOfType = new TreeMap<String, QuadTreeRing<ActivityFacility>>();
	protected TreeMap<String, ActivityFacilityImpl []> facilitiesOfType = new TreeMap<String, ActivityFacilityImpl []>();
	
	private ActTypeConverter converter = new ActTypeConverter(true);


	public TreesBuilder(HashSet<String> flexibleTypes, Network network, LocationChoiceConfigGroup config) {
		this.flexibleTypes = flexibleTypes;
		this.network = network;
		this.config = config;
	}

	public TreesBuilder(Network network, LocationChoiceConfigGroup config) {
		this.network = network;
		this.config = config;
		this.initFlexibleTypes();
	}

	private void initFlexibleTypes() {
		String types = config.getFlexibleTypes();

		if (!types.equals("null")) {
			log.info("Doing location choice for activity types: " + types);
			String[] entries = types.split(",", -1);
			for (int i = 0; i < entries.length; i++) {
				if (!entries[i].trim().equals("null")) {
					this.flexibleTypes.add(this.converter.convertType(entries[i].trim()));
				}
			}
		}
	}

	public void createTrees(ActivityFacilities facilities) {
		TreeMap<String, TreeMap<Id, ActivityFacility>> treesForTypes = this.createTreesForTypes(facilities);
		this.createQuadTreesAndArrays(treesForTypes);
	}

	private TreeMap<String, TreeMap<Id, ActivityFacility>> createTreesForTypes(ActivityFacilities facilities) {

		boolean regionalScenario = false;
		double radius = 0.0;
		Node centerNode = null;

		if (!config.getCenterNode().equals("null") &&
				!config.getRadius().equals("null")) {
			regionalScenario = true;
			centerNode = this.network.getNodes().get(new IdImpl(config.getCenterNode()));
			radius = Double.parseDouble(config.getRadius());
			log.info("Building trees regional scenario");
		}
		else {
			log.info("Building trees complete scenario");
		}

		TreeMap<String, TreeMap<Id, ActivityFacility>> trees = new TreeMap<String, TreeMap<Id, ActivityFacility>>();
		// get all types of activities
		for (ActivityFacility f : facilities.getFacilities().values()) {
			Map<String, ? extends ActivityOption> facilityActOpts = f.getActivityOptions();

			// do not add facility if it is not in region of interest ------------------------
			if (regionalScenario && (CoordUtils.calcDistance(f.getCoord(), centerNode.getCoord()) > radius)) {
				continue;
			}
			// -------------------------------------------------------------------------------

			Iterator<? extends ActivityOption> actOpt_it = facilityActOpts.values().iterator();
			while (actOpt_it.hasNext()) {
				ActivityOption actOpt = actOpt_it.next();

				// if flexibleTypes is empty we add all types to trees as potentially all types can be relocated
				// otherwise we add all types given by flexibleTypes
				if (this.flexibleTypes.size() == 0 ||  this.flexibleTypes.contains(this.converter.convertType(actOpt.getType()))) {
					if (!trees.containsKey(this.converter.convertType(actOpt.getType()))) {
						trees.put(this.converter.convertType(actOpt.getType()), new TreeMap<Id, ActivityFacility>());
					}
					trees.get(this.converter.convertType(actOpt.getType())).put(f.getId(), f);
				}
			}
		}
		return trees;
	}

	private void createQuadTreesAndArrays(TreeMap<String, TreeMap<Id, ActivityFacility>> trees) {
		Iterator<TreeMap<Id, ActivityFacility>> tree_it = trees.values().iterator();
		Iterator<String> type_it = trees.keySet().iterator();

		while (tree_it.hasNext()) {
			TreeMap<Id, ActivityFacility> tree_of_type = tree_it.next();
			String type = type_it.next();

			// do not construct tree for home and tta act
			if (type.startsWith("h") || type.startsWith("tta")) continue;

			this.quadTreesOfType.put(this.converter.convertType(type), this.builFacQuadTree(this.converter.convertType(type), tree_of_type));
			this.facilitiesOfType.put(this.converter.convertType(type), tree_of_type.values().toArray(new ActivityFacilityImpl[tree_of_type.size()]));
		}
	}

	private QuadTreeRing<ActivityFacility> builFacQuadTree(String type, TreeMap<Id,ActivityFacility> facilities_of_type) {
		Gbl.startMeasurement();
		log.info(" building " + type + " facility quad tree");
		double minx = Double.POSITIVE_INFINITY;
		double miny = Double.POSITIVE_INFINITY;
		double maxx = Double.NEGATIVE_INFINITY;
		double maxy = Double.NEGATIVE_INFINITY;

		for (final ActivityFacility f : facilities_of_type.values()) {
			if (f.getCoord().getX() < minx) { minx = f.getCoord().getX(); }
			if (f.getCoord().getY() < miny) { miny = f.getCoord().getY(); }
			if (f.getCoord().getX() > maxx) { maxx = f.getCoord().getX(); }
			if (f.getCoord().getY() > maxy) { maxy = f.getCoord().getY(); }
		}
		minx -= 1.0;
		miny -= 1.0;
		maxx += 1.0;
		maxy += 1.0;
		System.out.println("        xrange(" + minx + "," + maxx + "); yrange(" + miny + "," + maxy + ")");
		QuadTreeRing<ActivityFacility> quadtree = new QuadTreeRing<ActivityFacility>(minx, miny, maxx, maxy);
		for (final ActivityFacility f : facilities_of_type.values()) {
			quadtree.put(f.getCoord().getX(),f.getCoord().getY(),f);
		}
		log.info("    done");
		Gbl.printRoundTime();
		Gbl.printMemoryUsage();
		return quadtree;
	}

	public TreeMap<String, QuadTreeRing<ActivityFacility>> getQuadTreesOfType() {
		return quadTreesOfType;
	}
	public TreeMap<String, ActivityFacilityImpl[]> getFacilitiesOfType() {
		return facilitiesOfType;
	}

	public ActTypeConverter getActTypeConverter() {
		return converter;
	}

	public void setActTypeConverter(ActTypeConverter converter) {
		this.converter = converter;
	}
}

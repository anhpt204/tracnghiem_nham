/* *********************************************************************** *
 * project: org.matsim.*
 * EvacuationShelterNetLoader.java
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
package org.matsim.contrib.evacuation.shelters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.evacuation.base.Building;
import org.matsim.contrib.evacuation.base.EvacuationNetFromNetcdfGenerator;
import org.matsim.contrib.evacuation.base.EvacuationNetGenerator;
import org.matsim.contrib.evacuation.config.EvacuationConfigGroup;
import org.matsim.contrib.evacuation.flooding.FloodingReader;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.utils.geometry.geotools.MGC;

public class EvacuationShelterNetLoader {

	private final List<Building> buildings;

	private final Set<Link> shelterLinks = new HashSet<Link>();

	private final HashMap<Id, Building> buildingsLinkMapping = new HashMap<Id, Building>();

	private NetworkImpl network = null;

	private final Scenario scenario;

	private List<FloodingReader> netcdfReaders = null;

	public EvacuationShelterNetLoader(List<Building> buildings, Scenario scenario, List<FloodingReader> netcdfReaders) {
		this.scenario = scenario;
		this.buildings = buildings;
		this.netcdfReaders = netcdfReaders;
	}

	public EvacuationShelterNetLoader(List<Building> buildings, Scenario scenario) {
		this.scenario = scenario;
		this.buildings = buildings;
	}

	public NetworkImpl getNetwork() {
		if (this.network != null) {
			return this.network;
		}
		if (!(this.scenario.getNetwork() instanceof NetworkImpl)) {
			throw new RuntimeException("Implementation of Network interface not supported, a specific parser is needed for this implementation of Network interface!");
		}
		this.network = (NetworkImpl) this.scenario.getNetwork();

		createEvacuationNet();

		// generateShelterLinks();

		return this.network;
	}

	public void generateShelterLinks() {

		// BufferedWriter bw = null;
		// try {
		// bw = new BufferedWriter(new FileWriter(new
		// File("/home/laemmel/arbeit/svn/shared-svn/studies/countries/id/padang/network/shelter_info_v20100317")));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// try {
		// bw.append("# fromNode,flowCapacity, storageCapacity\n");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		Node saveNode = this.network.getNodes().get(new IdImpl("en1")); // TODO
		// GL
		// Apr.
		// 09 --
		// evacuation
		// node
		// should
		// not
		// retrieved
		// via
		// String
		// id
		for (Building building : this.buildings) {
			if (!building.isQuakeProof()) {
				continue;
			}

			//flow capacity on staircases according to Weidmann
			double flowCap = 0.85 * building.getMinWidth() * this.scenario.getConfig().getQSimConfigGroup().getTimeStepSize();
			// flowCap = 6;
			String shelterId = building.getId().toString();
			Coord c = MGC.point2Coord(building.getGeo().getCentroid());
			Node from = this.network.getNearestNode(c);
			while (from.getId().toString().contains("sn")) {
				from = from.getInLinks().values().iterator().next().getFromNode();
			}
			Node sn1 = this.network.createAndAddNode(new IdImpl("sn" + shelterId + "a"), c);
			Node sn2 = this.network.createAndAddNode(new IdImpl("sn" + shelterId + "b"), c);
			Link l1 = this.network.createAndAddLink(new IdImpl("sl" + shelterId + "a"), from, sn1, 1.66, 1.66, flowCap, 1); // FIXME
			// find
			// right
			// values
			// flow
			// cap,
			// lanes,
			// ...
			Link l2 = this.network.createAndAddLink(new IdImpl("sl" + shelterId + "b"), sn1, sn2, 10, 1.66, flowCap, 1); // FIXME
			// find
			// right
			// values
			// flow
			// cap,
			// lanes,
			// ...
			this.buildingsLinkMapping.put(l2.getId(), building);
			Link l3 = this.network.createAndAddLink(new IdImpl("sl" + shelterId + "c"), sn2, saveNode, 10, 10000, 10000, 1); // FIXME
			// find
			// right
			// values
			// flow
			// cap,
			// lanes,
			// ...
			this.shelterLinks.add(l1);
			this.shelterLinks.add(l2);
			this.shelterLinks.add(l3);
			// try {
			// bw.append(from.getId() + "," + flowCap + "," +
			// building.getShelterSpace() + "\n");
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

		}
		// try {
		// bw.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		this.network.connect();
	}

	public HashMap<Id, Building> getShelterLinkMapping() {

		getNetwork(); // make sure that mapping has been created;

		return this.buildingsLinkMapping;
	}

	public Set<Link> getShelterLinks() {
		if (this.network == null) {
			getNetwork();
		}

		return this.shelterLinks;
	}

	private double getDist(Node n1, Node n2) {
		return Math.sqrt(Math.pow(n1.getCoord().getX() - n2.getCoord().getX(), 2) + Math.pow(n1.getCoord().getY() - n2.getCoord().getY(), 2));
	}

	private void createEvacuationNet() {
		EvacuationConfigGroup ec = (EvacuationConfigGroup) this.scenario.getConfig().getModule("evacuation");
		if (ec.isGenerateEvacNetFromSWWFile()) {
			new EvacuationNetFromNetcdfGenerator(this.network, this.scenario.getConfig(), this.netcdfReaders).run();
		} else {
			new EvacuationNetGenerator(this.network, this.scenario.getConfig()).run();
		}
	}

}

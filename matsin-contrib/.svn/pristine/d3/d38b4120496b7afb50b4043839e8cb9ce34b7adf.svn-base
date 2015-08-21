/* *********************************************************************** *
 * project: org.matsim.*
 * EvacuationNetGenerator.java
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
package org.matsim.contrib.evacuation.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.evacuation.config.EvacuationConfigGroup;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.utils.geometry.CoordImpl;

public class EvacuationNetGenerator {
	private final static Logger log = Logger.getLogger(EvacuationNetGenerator.class);

	// evacuation Nodes an Link
	private final static String saveLinkId = "el1";
	private final static Id saveNodeAId = new IdImpl("en1");
	private final static Id saveNodeBId = new IdImpl("en2");

	// the positions of the evacuation nodes - for now hard coded
	// Since the real positions of this nodes not really matters
	// and for the moment we are going to evacuate Padang only,
	// the save nodes are located east of the city.
	// Doing so, the visualization of the resulting evacuation network is much
	// clearer in respect of coinciding links.
	private final static String saveAX = "662433";
	private final static String saveAY = "9898853";
	private final static String saveBX = "662433";
	private final static String saveBY = "9898853";

	protected final Map<Id, EvacuationAreaLink> evacuationAreaLinks = new HashMap<Id, EvacuationAreaLink>();
	protected final HashSet<Node> saveNodes = new HashSet<Node>();
	protected final HashSet<Node> redundantNodes = new HashSet<Node>();

	private final Config config;

	protected final Network network;

	public EvacuationNetGenerator(final Network network, Config config) {
		this.network = network;
		this.config = config;
	}

	/**
	 * Creates links from all save nodes to the evacuation node A
	 * 
	 * @param network
	 */
	protected void createEvacuationLinks() {
		Node saveNodeA = this.network.getFactory().createNode(saveNodeAId, new CoordImpl(saveAX, saveAY));
		this.network.addNode(saveNodeA);
		Node saveNodeB = this.network.getFactory().createNode(saveNodeBId, new CoordImpl(saveBX, saveBY));
		this.network.addNode(saveNodeB);

		double capacity = 100000.;
		Link l = this.network.getFactory().createLink(new IdImpl(saveLinkId), saveNodeA, saveNodeB);
		l.setLength(10);
		l.setFreespeed(100000);
		l.setCapacity(capacity);
		l.setNumberOfLanes(1);
		this.network.addLink(l);

		int linkId = 1;
		for (Node node : this.network.getNodes().values()) {
			Id nodeId = node.getId();
			if (isSaveNode(node) && !nodeId.equals(saveNodeAId) && !nodeId.equals(saveNodeBId)) {
				linkId++;
				String sLinkID = "el" + Integer.toString(linkId);
				Link l2 = this.network.getFactory().createLink(new IdImpl(sLinkID), node, saveNodeA);
				l2.setLength(10);
				l2.setFreespeed(100000);
				l2.setCapacity(capacity);
				l2.setNumberOfLanes(1);
				this.network.addLink(l2);
			}
		}
	}

	/**
	 * @param node
	 * @return true if <code>node</node> is outside the evacuation area
	 */
	private boolean isSaveNode(final Node node) {
		return this.saveNodes.contains(node);
	}

	/**
	 * Returns true if <code>node</code> is redundant. A node is redundant if it
	 * is not next to the evacuation area.
	 * 
	 * @param node
	 * @return true if <code>node</code> is redundant.
	 */
	protected boolean isRedundantNode(final Node node) {
		return this.redundantNodes.contains(node);
	}

	public void run() {
		log.info("generating evacuation net ...");
		log.info(" * reading evacuaton area file");
		readEvacuationAreaFile();
		log.info(" * classifing nodes");
		classifyNodes();
		log.info(" * cleaning up the network");
		cleanUpNetwork();
		log.info(" * creating evacuation links");
		createEvacuationLinks();
		log.info("done.");
	}

	protected void readEvacuationAreaFile() {
		String evacuationAreaLinksFile = ((EvacuationConfigGroup) this.config.getModule("evacuation")).getEvacuationAreaFile();
		new EvacuationAreaFileReader(this.evacuationAreaLinks).readFile(evacuationAreaLinksFile);
	}

	/**
	 * Classifies the nodes. Nodes that are next to the evacuation area and
	 * reachable from inside the evacuation area will be classified as save
	 * nodes. Other nodes outside the evacuation area will be classified as
	 * redundant nodes.
	 * 
	 * @param network
	 */
	protected void classifyNodes() {
		/*
		 * classes: 0: default, assume redundant 1: redundant node 2: save
		 * nodes, can be reached from evacuation area 3: "normal" nodes within
		 * the evacuation area
		 */
		for (Node node : this.network.getNodes().values()) {
			int inCat = 0;
			for (Link link : node.getInLinks().values()) {
				if (this.evacuationAreaLinks.containsKey(link.getId())) {
					if ((inCat == 0) || (inCat == 3)) {
						inCat = 3;
					} else {
						inCat = 2;
						break;
					}
				} else {
					if (inCat <= 1) {
						inCat = 1;
					} else {
						inCat = 2;
						break;
					}
				}
			}
			switch (inCat) {
			case 2:
				this.saveNodes.add(node);
				break;
			case 3:
				break;
			case 1:
			default:
				this.redundantNodes.add(node);
			}
		}

	}

	/**
	 * Removes all links and nodes outside the evacuation area except the nodes
	 * next to the evacuation area that are reachable from inside the evacuation
	 * area ("save nodes").
	 * 
	 * @param network
	 */
	protected void cleanUpNetwork() {

		ConcurrentLinkedQueue<Link> l = new ConcurrentLinkedQueue<Link>();
		for (Link link : this.network.getLinks().values()) {
			if (!this.evacuationAreaLinks.containsKey(link.getId())) {
				l.add(link);
			}
		}

		Link link = l.poll();
		while (link != null) {
			this.network.removeLink(link.getId());
			link = l.poll();
		}

		ConcurrentLinkedQueue<Node> n = new ConcurrentLinkedQueue<Node>();
		for (Node node : this.network.getNodes().values()) {
			if (isRedundantNode(node)) {
				n.add(node);
			}
		}

		Node node = n.poll();
		while (node != null) {
			this.network.removeNode(node.getId());
			node = n.poll();
		}
		new NetworkCleaner().run(this.network);
	}
}

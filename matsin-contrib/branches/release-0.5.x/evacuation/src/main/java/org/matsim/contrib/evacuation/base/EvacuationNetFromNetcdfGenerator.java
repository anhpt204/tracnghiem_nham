/* *********************************************************************** *
 * project: org.matsim.*
 * EvacuationNetFromNetcdfGenerator.java
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

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.evacuation.config.EvacuationConfigGroup;
import org.matsim.contrib.evacuation.flooding.FloodingInfo;
import org.matsim.contrib.evacuation.flooding.FloodingReader;
import org.matsim.core.config.Config;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.geotools.MGC;

import com.vividsolutions.jts.geom.Envelope;

public class EvacuationNetFromNetcdfGenerator extends EvacuationNetGenerator {

	private static final Logger log = Logger.getLogger(EvacuationNetFromNetcdfGenerator.class);

	private final List<FloodingReader> readers;
	private final Envelope envelope = new Envelope(0, 0, 0, 0);
	private QuadTree<FloodingInfo> quadTree;

	private final double bufferSize;

	public EvacuationNetFromNetcdfGenerator(Network network, Config config, List<FloodingReader> readers) {
		super(network, config);
		this.readers = readers;
		this.bufferSize = ((EvacuationConfigGroup) config.getModule("evacuation")).getBufferSize();
	}

	@Override
	public void run() {
		log.info("building quad tree");
		buildFiQuadTree();
		log.info("done");
		log.info(" * reading evacuaton area file");
		readEvacuationAreaFile();
		log.info("generating evacuation net ...");
		log.info(" * classifing nodes");
		classifyNodes();
		log.info(" * cleaning up the network");
		cleanUpNetwork();
		log.info(" * creating evacuation links");
		createEvacuationLinks();
		log.info("done.");

	}

	/**
	 * Classifies the nodes. Nodes that are next to the evacuation area and
	 * reachable from inside the evacuation area will be classified as save
	 * nodes. Other nodes outside the evacuation area will be classified as
	 * redundant nodes.
	 * 
	 * @param network
	 */
	@Override
	protected void classifyNodes() {
		/*
		 * classes: 0: default, assume redundant 1: redundant node 2: save
		 * nodes, can be reached from evacuation area 3: "normal" nodes within
		 * the evacuation area
		 */
		for (Node node : this.network.getNodes().values()) {
			int inCat = 0;
			for (Link link : node.getInLinks().values()) {
				if (isInsideEvacArea(link)) {
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
	@Override
	protected void cleanUpNetwork() {

		ConcurrentLinkedQueue<Link> l = new ConcurrentLinkedQueue<Link>();
		for (Link link : this.network.getLinks().values()) {
			if (!this.evacuationAreaLinks.containsKey(link.getId()) || isRedundantNode(link.getFromNode()) && isRedundantNode(link.getFromNode())) {
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

	private boolean isInsideEvacArea(Link link) {

		if (!this.evacuationAreaLinks.containsKey(link.getId())) {
			return false;
		}

		FloodingInfo fi = this.quadTree.get(link.getFromNode().getCoord().getX(), link.getFromNode().getCoord().getY());
		if (fi.getCoordinate().distance(MGC.coord2Coordinate(link.getFromNode().getCoord())) <= this.bufferSize) {
			return true;
		}
		fi = this.quadTree.get(link.getToNode().getCoord().getX(), link.getToNode().getCoord().getY());
		if (fi.getCoordinate().distance(MGC.coord2Coordinate(link.getToNode().getCoord())) <= this.bufferSize) {
			return true;
		}
		fi = this.quadTree.get(link.getCoord().getX(), link.getCoord().getY());
		if (fi.getCoordinate().distance(MGC.coord2Coordinate(link.getCoord())) <= this.bufferSize) {
			return true;
		}
		return false;
	}

	private void buildFiQuadTree() {
		for (FloodingReader fr : this.readers) {
			this.envelope.expandToInclude(fr.getEnvelope());
		}
		this.quadTree = new QuadTree<FloodingInfo>(this.envelope.getMinX(), this.envelope.getMinY(), this.envelope.getMaxX(), this.envelope.getMaxY());
		for (FloodingReader fr : this.readers) {
			for (FloodingInfo fi : fr.getFloodingInfos()) {
				this.quadTree.put(fi.getCoordinate().x, fi.getCoordinate().y, fi);
			}
		}

	}
}

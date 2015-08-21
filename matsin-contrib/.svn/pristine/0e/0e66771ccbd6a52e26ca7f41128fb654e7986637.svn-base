/* *********************************************************************** *
 * project: org.matsim.*
 * RiskCostFromFloodingData.java
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
package org.matsim.contrib.evacuation.riskaversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.evacuation.flooding.FloodingInfo;
import org.matsim.contrib.evacuation.flooding.FloodingLine;
import org.matsim.contrib.evacuation.flooding.FloodingReader;
import org.matsim.core.api.experimental.events.AgentMoneyEvent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.vehicles.Vehicle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class RiskCostFromFloodingData implements TravelDisutility, LinkEnterEventHandler {

	private final static Logger log = Logger.getLogger(RiskCostFromFloodingData.class);

	//TODO put this in config group
	public final static double FLOODED_DIST_THRESHOLD = 10.;
	public final double bufferSize;
	private final static double BASE_COST = 3 * 3600;

	private final Network network;

	private Map<Id, LinkInfo> lis;

	private HashMap<Node, Double> nft = null;


	private final EventsManager events;

	private Queue<FloodingReader> frsQueue;

	private List<FloodingLine> fls;
	private QuadTree<FloodingInfo> fiQuad;
	private Map<Integer, QuadTree<Coordinate>> floodLines = new HashMap<Integer, QuadTree<Coordinate>>();

	private Envelope envelope = new Envelope(0,0,0,0);

	public RiskCostFromFloodingData(Network net, List<FloodingReader> frs, EventsManager events, double bufferSize) {
		this.network = net;
		this.frsQueue = new ConcurrentLinkedQueue<FloodingReader>(frs);
		this.events = events;
		this.bufferSize = bufferSize;
		init();
	}

	private void init() {

		log.info("preparing data structures...");

		this.nft = new HashMap<Node, Double>();
		this.fls = new ArrayList<FloodingLine>();

		List<FloodingInfo> fis = new ArrayList<FloodingInfo>();

		while (this.frsQueue.size() > 0) {
			FloodingReader fr = this.frsQueue.poll();
			this.fls.add(new FloodingLine(fr));
			this.envelope.expandToInclude(fr.getEnvelope());
			fis.addAll(fr.getFloodingInfos());
		}
		this.frsQueue = null;

		this.fiQuad = new QuadTree<FloodingInfo>(this.envelope.getMinX(), this.envelope.getMinY(), this.envelope.getMaxX(), this.envelope.getMaxY());
		for (FloodingInfo fi : fis) {
			this.fiQuad.put(fi.getCoordinate().x, fi.getCoordinate().y, fi);
		}
		log.info("done.");

		log.info("classifying nodes");
		Map<Id, NodeInfo> nis = classifyNodesII();
		log.info("done.");

		log.info("classifying links");
		this.lis = classifyLinks(nis);
		log.info("done");

//		log.info("creating shape files.");
//		new NodeCostShapeCreator(this.lis, MGC.getCRS(TransformationFactory.WGS84_UTM47S), this.network);
//		new FloodLineShape(this.frs, MGC.getCRS(TransformationFactory.WGS84_UTM47S));
//		log.info("done");

		//free memory
		this.fls = null;
		this.fiQuad = null;
		this.floodLines = null;
		this.envelope = null;
		this.nft = null;
	}

	private Map<Id, NodeInfo> classifyNodesII() {
		Map<Id, NodeInfo> nis = new HashMap<Id, NodeInfo>();

		for (Node node : this.network.getNodes().values()) {
			FloodingInfo fi = getNearestFloodingInfo(node);
			double dist = fi.getCoordinate().distance(
					MGC.coord2Coordinate(node.getCoord()));
			if (dist > this.bufferSize) {
				continue;
			}

			NodeInfo ni = new NodeInfo();
//			ni.node = node;
//			ni.time = fi.getFloodingTime();
			ni.dist = dist;
			ni.cost = getNodeRiskCost(node);

			nis.put(node.getId(), ni);
		}

		return nis;

	}


	private FloodingInfo getNearestFloodingInfo(Node node) {
		return this.fiQuad.get(node.getCoord().getX(), node.getCoord().getY());
	}

	public double getNodeRiskCost(Node node) {
		Double ret = this.nft.get(node);
		if (ret == null) {
			ret = calculateNodeRiskCost(node);
		}

		return ret;

	}

	private Double calculateNodeRiskCost(Node node) {
		FloodingInfo fi = this.fiQuad.get(node.getCoord().getX(), node
				.getCoord().getY());
		double dist = fi.getCoordinate().distance(
				MGC.coord2Coordinate(node.getCoord()));
		if (dist > this.bufferSize) {
			this.nft.put(node, 0.);
			return 0.;
		}

		if (dist > FLOODED_DIST_THRESHOLD) {
			Double cost = (BASE_COST / 2)
					* (1 - (dist / this.bufferSize));
			this.nft.put(node, cost);
			return cost;
		}

		int time = (int) fi.getFloodingTime();
		if (time == 0) {
			this.nft.put(node, BASE_COST);
			return BASE_COST;
		}


		int time2 = time - 1;
		QuadTree<Coordinate> q1 = getFloodlineQuad(time2);
		while (q1.size() == 0) {
			q1 = getFloodlineQuad(--time2);
		}
		QuadTree<Coordinate> q2 = getFloodlineQuad(time);
		while (q2.size() == 0) {
			q2 = getFloodlineQuad(++time);
			if (time > 60) {
				q2 = q1;
				time = time2;
				break;
			}
		}

		Coordinate c1 = q1.get(node.getCoord().getX(), node.getCoord().getY());
		Coordinate c2 = q2.get(node.getCoord().getX(), node.getCoord().getY());
		double d1 = c1.distance(MGC.coord2Coordinate(node.getCoord()));
		double d2 = c2.distance(MGC.coord2Coordinate(node.getCoord()));
		double realTime = (d1 * time2 + d2 * time) / (d1 + d2);
		Double cost = BASE_COST - 60 * realTime;
		this.nft.put(node, cost);
		return cost;
	}



	private Map<Id, LinkInfo> classifyLinks(Map<Id, NodeInfo> nis) {
		Map<Id, LinkInfo> lis = new HashMap<Id, LinkInfo>();
		for (Link link : this.network.getLinks().values()) {
//			if (link.getId().toString().equals("11288")) {
//				int ii = 0;
//				ii++;
//			}
//
//			if (link.getId().toString().equals("111288")) {
//				int ii = 0;
//				ii++;
//			}
//
//			if (link.getId().toString().equals("9204")) {
//				int ii = 0;
//				ii++;
//			}
//
//			if (link.getId().toString().equals("109204")) {
//				int ii = 0;
//				ii++;
//			}
//
//			if (link.getId().toString().equals("6798")) {
//				int ii = 0;
//				ii++;
//			}
//
//			if (link.getId().toString().equals("106798")) {
//				int ii = 0;
//				ii++;
//			}

			NodeInfo toNode = nis.get(link.getToNode().getId());
			if (toNode == null) {
				continue;
			}
			NodeInfo fromNode = nis.get(link.getFromNode().getId());
			if ((fromNode == null) || (toNode.cost > fromNode.cost)) {
				LinkInfo li = new LinkInfo();
//				li.link = link;
//				li.time = toNode.time;
				li.cost = toNode.cost * link.getLength()/100.;
				li.setBaseCost(toNode.cost);
				li.setDist(toNode.dist);
				lis.put(link.getId(), li);
			}

		}

		return lis;

	}
	private QuadTree<Coordinate> getFloodlineQuad(int time) {
		QuadTree<Coordinate> q = this.floodLines.get(time);
		if (q == null) {
			List<ArrayList<Coordinate>> coords = new ArrayList<ArrayList<Coordinate>>();
			for (FloodingLine fl : this.fls) {
				coords.addAll(fl.getFloodLine(time));
			}
			q = new QuadTree<Coordinate>(this.envelope.getMinX(), this.envelope.getMinY(), this.envelope.getMaxX(),
					this.envelope.getMaxY());
			for (ArrayList<Coordinate> list : coords) {
				for (Coordinate c : list) {
					q.put(c.x, c.y, c);
				}

			}
//			if (q.size() == 0) {
//				System.out.println("No valid quad tree for time step:" + time);
//			} else {
//				System.out.println("Valid quad tree for time step:" + time);
//			}
			this.floodLines.put(time, q);
		}
		return q;
	}


	private static class NodeInfo {
//		Node node;
//		double time;
		double dist;
		double cost;

	}

	public static class LinkInfo {
//		Link link;
//		double time;
		double cost;
		private double baseCost;
		private double dist;
		public void setBaseCost(double baseCost) {
			this.baseCost = baseCost;
		}
		public double getBaseCost() {
			return this.baseCost;
		}
		public void setDist(double dist) {
			this.dist = dist;
		}
		public double getDist() {
			return this.dist;
		}
	}


	@Override
	public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
		LinkInfo li = this.lis.get(link.getId());
		if (li == null) {
			return 0;
		}
		return li.cost;
	}
	
	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		LinkInfo li = this.lis.get(event.getLinkId());
		if (li == null) {
			return;
		}

		AgentMoneyEvent e = new AgentMoneyEvent(event.getTime(), event
				.getPersonId(), li.cost / -600);
		this.events.processEvent(e);
	}

	@Override
	public void reset(int iteration) {

	}

}

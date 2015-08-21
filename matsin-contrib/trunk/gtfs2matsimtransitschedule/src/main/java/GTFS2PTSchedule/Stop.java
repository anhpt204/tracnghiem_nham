/* *********************************************************************** *
 * project: org.matsim.*
 * Stop.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package GTFS2PTSchedule;

import org.matsim.api.core.v01.Coord;

import GTFS2PTSchedule.GTFSDefinitions.RouteTypes;


public class Stop {
	
	//Attributes
	/**
	 * The location
	 */
	private Coord point;
	/**
	 * The link Id
	 */
	private String linkId;
	/**
	 * Is fixed the link Id
	 */
	private boolean fixedLinkId = false;
	/**
	 * The name
	 */
	private String name;
	/**
	 * If blocks other vehicles
	 */
	private boolean blocks;
	/**
	 * The route type
	 */
	private RouteTypes routeType;
	
	//Methods
	/**
	 * @param point
	 * @param name
	 * @param blocks
	 */
	public Stop(Coord point, String name, boolean blocks) {
		super();
		this.point = point;
		this.name = name;
		this.blocks = blocks;
	}
	/**
	 * @return the linkId
	 */
	public String getLinkId() {
		return linkId;
	}
	/**
	 * @param linkId the linkId to set
	 * @return 
	 */
	public boolean setLinkId(String linkId) {
		if(!fixedLinkId) {
			this.linkId = linkId;
			return true;
		}
		else
			return false;
	}
	/**
	 * @param linkId the linkId to set
	 * @return 
	 */
	public void forceSetLinkId(String linkId) {
		fixedLinkId = true;
		this.linkId = linkId;
	}
	/**
	 * @return the fixedLinkId
	 */
	public boolean isFixedLinkId() {
		return fixedLinkId;
	}
	/**
	 * Fixes the link id
	 */
	public void setFixedLinkId() {
		this.fixedLinkId = true;
	}
	/**
	 * @return the point
	 */
	public Coord getPoint() {
		return point;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the blocks
	 */
	public boolean isBlocks() {
		return blocks;
	}
	/**
	 * @return the routeType
	 */
	public RouteTypes getRouteType() {
		return routeType;
	}
	/**
	 * @param routeType the routeType to set
	 */
	public void setRouteType(RouteTypes routeType) {
		this.routeType = routeType;
	}
	
}

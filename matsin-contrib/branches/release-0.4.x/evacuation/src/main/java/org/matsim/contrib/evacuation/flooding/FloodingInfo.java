/* *********************************************************************** *
 * project: org.matsim.*
 * FloodingInfo.java
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
package org.matsim.contrib.evacuation.flooding;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class FloodingInfo {

	
	private final Coordinate c;
	private final List<Double> stages;
	private final double floodingTime;
	
	
	public FloodingInfo(Coordinate c, List<Double> floodingSeries, double time) {
		this.c = c;
		this.stages = floodingSeries;
		this.floodingTime = time;
	}

	public double getFloodingTime() {
		return this.floodingTime;			
		
	}
	
	public Coordinate getCoordinate() {
		return this.c;
	}
	
	public List<Double> getFloodingSeries() {
		return this.stages;
	}
	
}

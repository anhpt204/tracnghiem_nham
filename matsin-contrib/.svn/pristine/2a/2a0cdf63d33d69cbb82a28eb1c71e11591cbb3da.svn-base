/* *********************************************************************** *
 * project: org.matsim.*
 * Building.java
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

import org.matsim.api.core.v01.Id;

import com.vividsolutions.jts.geom.Geometry;

public class Building {

	private final Id id;
	private final int popNight;
	private final int popDay;
	private final int floor;
	private int quakeProof;
	private final Geometry geo;
	
	private int shelterSpace;
	private double minWidth;
	private final int popAf;
	
	
	public  Building(Id id, int popNight, int popDay, int popAf, int floor, int capacity,
			double minWidth, int quakeProof, Geometry geo) {
		this.id = id;
		this.popNight = popNight;
		this.popDay = popDay;
		this.popAf = popAf;
		this.floor = floor;
//		this.space = space;
		this.quakeProof = quakeProof;
		this.geo = geo;
		this.minWidth = minWidth;
		this.shelterSpace = capacity;
		
	}

	public Id getId() {
		return this.id;
	}



	public int getPopNight() {
		return this.popNight;
	}



	public int getPopDay() {
		return this.popDay;
	}

	public int getPopAf() {
		return this.popAf;
	}

	public int getFloor() {
		return this.floor;
	}





	public boolean isQuakeProof() {
		return this.quakeProof == 1;
	}
	
	public void setIsQuakeProof(int quakeProof) {
		this.quakeProof = quakeProof;
	}



	public Geometry getGeo() {
		return this.geo;
	}

	public int getShelterSpace() {
		return this.shelterSpace;
	}
	
	public void setShelterSpace(int space) {
		this.shelterSpace = space;
	}

	public void setMinWidth(double minWidth) {
		this.minWidth = minWidth;
	}
	
	public double getMinWidth() {
		return this.minWidth;
	}


}

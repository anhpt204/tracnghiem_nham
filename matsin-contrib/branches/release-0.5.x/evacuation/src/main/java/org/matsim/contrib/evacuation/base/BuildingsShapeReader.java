/* *********************************************************************** *
 * project: org.matsim.*
 * BuildingsShapeReader.java
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

public class BuildingsShapeReader {
	
	private static final Logger log = Logger.getLogger(BuildingsShapeReader.class);

	
	public static List<Building> readDataFile(String inFile,double sampleSize){
		List<Building> ret = new ArrayList<Building>();
		SimpleFeatureSource fts = ShapeFileReader.readDataFile(inFile);
		SimpleFeatureIterator it;
		try {
			it = fts.getFeatures().features();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		boolean errMsg = true;
		while (it.hasNext()) {
			SimpleFeature ft = it.next();
			Geometry geo = (Geometry) ft.getDefaultGeometry();
			Id id = new IdImpl((Integer)ft.getAttribute("ID"));
			int popNight = (Integer) ft.getAttribute("popNight");
			int popDay = (Integer) ft.getAttribute("popDay");
			int floor = (Integer) ft.getAttribute("floor");
			int capacity = (int)((double)(Integer) ft.getAttribute("capacity")*sampleSize);
			int quakeProof = (Integer) ft.getAttribute("quakeProof");
			double minWidth = (Double)ft.getAttribute("minWidth");
			int popAf = 0;
			try {
				popAf = (Integer) ft.getAttribute("popAf");
			} catch (Exception e) {
				if (errMsg) {
					log.warn("Shapefile has no information about afternoon population. Afternoon scenario will not work!!");
					errMsg = false;
				}
				
			}
			ret.add(new Building(id,popNight,popDay, popAf,floor,capacity,minWidth,quakeProof,geo));
		}
		it.close();
		
		return ret;
	}
	
}

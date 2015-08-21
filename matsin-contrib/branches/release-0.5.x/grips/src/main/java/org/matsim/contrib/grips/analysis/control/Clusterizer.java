/* *********************************************************************** *
 * project: org.matsim.*
 * RoadClosuresEditor.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.contrib.grips.analysis.control;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.collections.Tuple;

/**
 * this clusterizer is simply finding average values, while ignoring duplicate
 * values.
 * 
 * @author wdoering
 * 
 */
public class Clusterizer {

	public LinkedList<Tuple<Id, Double>> getClusters(LinkedList<Tuple<Id, Double>> data, int n) {
		LinkedList<Tuple<Id, Double>> clusters = new LinkedList<Tuple<Id, Double>>();

		Collections.sort(data, new Comparator<Tuple<Id, Double>>() {

			@Override
			public int compare(Tuple<Id, Double> o1, Tuple<Id, Double> o2) {
				if (o1.getSecond() > o2.getSecond())
					return 1;
				else if (o1.getSecond() < o2.getSecond())
					return -1;
				return 0;
			}
		});

		int m = (data.size() / n) > 0 ? (data.size() / n) : 1;
		int i = 0;

		for (Tuple<Id, Double> element : data) {
			if ((i++ % m == 0) && clusters.size() < n) {
				clusters.add(element);
			}

		}

		while (clusters.size() < n)
			clusters.add(clusters.get(clusters.size() - 1));

		return clusters;
	}

}

/* *********************************************************************** *
 * project: org.matsim.*
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

package playground.andreas.P2.replanning.modules.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import playground.andreas.P2.operator.Cooperative;
import playground.andreas.P2.replanning.AbstractPStrategyModule;
import playground.andreas.P2.replanning.PPlan;
import playground.andreas.P2.replanning.modules.SidewaysRouteExtension;
import playground.andreas.P2.routeProvider.PRouteProvider;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author droeder
 *
 * @deprecated Use {@linkplain SidewaysRouteExtension} instead
 *
 */
public class RectangleHullRouteExtension extends AbstractPStrategyModule {
	
	private static final Logger log = Logger
			.getLogger(RectangleHullRouteExtension.class);
	public static final String STRATEGY_NAME = "RectangleHullRouteExtension";
	private double height;

	/**
	 * 
	 * inserts a new stop 2 be served to the best plan of an cooperative.
	 * This is done by spreading out a rectangle between the first stop2beServed and the furthest away stop from the stops2beServed 
	 * list (assuming this is the turning-point of a route).
	 * The width of the rectangle is naturally given by the distance of the to stops. The height has to be defined as parameter relative to the length.
	 * 
	 * @param parameter
	 */
	public RectangleHullRouteExtension(ArrayList<String> parameter) {
		super(parameter);
		if(parameter.size() != 1){
			log.error("only exact one parameter allowed for this strategy...");
		}
		this.height = Double.parseDouble(parameter.get(0));
	}

	/* (non-Javadoc)
	 * @see playground.andreas.P2.replanning.PStrategy#run(playground.andreas.P2.pbox.Cooperative)
	 */
	@Override
	public PPlan run(Cooperative cooperative) {
		// get a List of served stop-facilities in the sequence they are served
		List<TransitStopFacility> currentlyUsedStops = this.getUsedFacilities(cooperative);
		// create the rectangle
		Geometry rectangle = this.createRectangle(cooperative.getBestPlan().getStopsToBeServed());
		// find currently unused stops inside the rectangle
		List<TransitStopFacility> newHullInteriorStops = 
			this.findNewHullInteriorStops(cooperative.getRouteProvider().getAllPStops(), currentlyUsedStops, rectangle);
		
		// draw a random stop from the candidates-list
		TransitStopFacility newStop = this.drawStop(cooperative.getRouteProvider(), newHullInteriorStops);
		if(newStop == null){
			log.error("can not create a new route for cooperative " + cooperative.getId() + " in iteration " + cooperative.getCurrentIteration() +
					", because there is no unused stop in the convex hull of the old route.");
			return null;
		}else{
			// create a new plan 
			PPlan oldPlan = cooperative.getBestPlan();
			PPlan newPlan = new PPlan(cooperative.getNewRouteId(), this.getName());
			newPlan.setNVehicles(1);
			newPlan.setStartTime(oldPlan.getStartTime());
			newPlan.setEndTime(oldPlan.getEndTime());
			//insert the new stop at the correct point (minimum average Distance from the subroute to the new Stop) in the sequence of stops 2 serve
			ArrayList<TransitStopFacility> stopsToServe = createNewStopsToServe(cooperative, newStop); 
			if(stopsToServe ==  null){
				return null;
			}
			newPlan.setStopsToBeServed(stopsToServe);
			
			newPlan.setLine(cooperative.getRouteProvider().createTransitLine(cooperative.getId(), newPlan));
			
			return newPlan;
		}
	}
	

	/**
	 * @param cooperative
	 * @param newStop
	 * @return
	 */
	private ArrayList<TransitStopFacility> createNewStopsToServe(Cooperative cooperative, TransitStopFacility newStop) {
		// find the subroutes, between the stops to be served
		List<List<TransitStopFacility>> subrouteFacilities = this.findSubroutes(cooperative, newStop);
		List<Double> avDist = calcAvDist(subrouteFacilities, newStop);
		if(avDist.size() > cooperative.getBestPlan().getStopsToBeServed().size()){
			log.info("can not create a new plan for cooperative " + cooperative.getId() + " in iteration " + 
					cooperative.getCurrentIteration() + ". more subroutes then expected were found.");
			return null;
		}
		
		//calculate the average distance from the new stop to the subroute and add the new stop between the 2 "stops2beServed" of the subroute
		int index = 0;
		double temp = Double.MAX_VALUE;
		for(int i = 0; i < avDist.size(); i++){
			if(avDist.get(i) < temp){
				temp = avDist.get(i);
				index = i;
			}
		}
		ArrayList<TransitStopFacility> stops2serve = new ArrayList<TransitStopFacility>();
		stops2serve.addAll(cooperative.getBestPlan().getStopsToBeServed());
		stops2serve.add(index + 1, newStop);
		
		return stops2serve;
	}

	/**
	 * @param subrouteFacilities
	 * @return
	 */
	private List<Double> calcAvDist(List<List<TransitStopFacility>> subrouteFacilities, TransitStopFacility newStop) {
		List<Double> dist = new ArrayList<Double>();
		double temp;
		
		for(List<TransitStopFacility> subroute: subrouteFacilities){
			temp = 0;
			for(TransitStopFacility t: subroute){
				temp += CoordUtils.calcDistance(t.getCoord(), newStop.getCoord());
			}
			temp = temp/subroute.size();
			dist.add(temp);
		}
		
		
		return dist;
	}

	/**
	 * finds the subroutes between stops2beServed+
	 * 
	 * @param cooperative
	 * @param newStop
	 * @return
	 */
	private List<List<TransitStopFacility>> findSubroutes(Cooperative cooperative, TransitStopFacility newStop) {
		List<List<TransitStopFacility>> subroutes = new ArrayList<List<TransitStopFacility>>();
		ArrayList<TransitStopFacility> temp = null;
		TransitStopFacility fac;
		
		for(TransitRouteStop s: cooperative.getBestPlan().getLine().getRoutes().values().iterator().next().getStops()){
			fac = s.getStopFacility();
			if(temp == null){
				temp = new ArrayList<TransitStopFacility>();
				temp.add(fac);
			}else{
				temp.add(fac);
				if(cooperative.getBestPlan().getStopsToBeServed().contains(s.getStopFacility())){
					subroutes.add(temp);
					temp = new ArrayList<TransitStopFacility>();
					temp.add(fac);
				}
			}
		}
		
		return subroutes;
	}

	/**
	 * @param cooperative
	 * @return
	 */
	private List<TransitStopFacility> getUsedFacilities(Cooperative cooperative) {
		List<TransitStopFacility> currentlyUsedStops = new ArrayList<TransitStopFacility>();
		
		for (TransitRouteStop stop : cooperative.getBestPlan().getLine().getRoutes().values().iterator().next().getStops()) {
			currentlyUsedStops.add(stop.getStopFacility());
		}
		return currentlyUsedStops;
	}

	/**
	 * @param currentlyUsedStops
	 * @return
	 */
	private Geometry createRectangle(List<TransitStopFacility> currentlyUsedStops) {
		Coord first, second, direction, normal;
		first = currentlyUsedStops.get(0).getCoord();
		second = findStopInGreatestDistance(currentlyUsedStops).getCoord();
		direction = CoordUtils.minus(second, first);
		normal = CoordUtils.scalarMult(1/CoordUtils.length(direction), CoordUtils.rotateToRight(direction));

		double height = this.height/2*CoordUtils.length(direction);
		
		Coordinate[] c = new Coordinate[4];
		c[0] = MGC.coord2Coordinate(CoordUtils.plus(first, CoordUtils.scalarMult(height, normal)));
		c[1] = MGC.coord2Coordinate(CoordUtils.plus(first, CoordUtils.scalarMult(-1*height, normal)));
		c[2] = MGC.coord2Coordinate(CoordUtils.plus(second, CoordUtils.scalarMult(height, normal)));
		c[3] = MGC.coord2Coordinate(CoordUtils.plus(second, CoordUtils.scalarMult(-1*height, normal)));
		
		GeometryFactory f = new GeometryFactory();
		return f.createMultiPoint(c).convexHull();
	}
	
	/**
	 * @param stops2serve
	 * @return
	 */
	private TransitStopFacility findStopInGreatestDistance(List<TransitStopFacility> stops2serve) {
		TransitStopFacility first = stops2serve.get(0);
		TransitStopFacility temp;
		Integer index = null;
		double maxDist = -1., currentDist;
		
		for(int i = 1; i < stops2serve.size(); i++ ){
			temp = stops2serve.get(i);
			currentDist = CoordUtils.calcDistance(temp.getCoord(), first.getCoord());
			if(currentDist > maxDist){
				maxDist =  currentDist;
				index = i;
			}
		}
		return stops2serve.get(index);
	}
	
	/**
	 * returns stops within the convex hull, which are not part of the current route, but of its convex hull
	 * 
	 * @param stopsToBeServed
	 * @param currentlyUsedStops
	 * @param hull
	 * @return
	 */
	private List<TransitStopFacility> findNewHullInteriorStops(Collection<TransitStopFacility> possibleStops, 
																List<TransitStopFacility> currentlyUsedStops, 
																Geometry hull) {
		List<TransitStopFacility> stopCandidates = new ArrayList<TransitStopFacility>();
		
		for(TransitStopFacility s: possibleStops){
//				if(hull.contains(MGC.coord2Point(s.getCoord()))){
			if(!currentlyUsedStops.contains(s)){
				// I replaced "contains" by "covers" since the exact interpretation seems to have changed 
				// with the geotools upgrade. kai, jan'13
				
					if(hull.covers(MGC.coord2Point(s.getCoord()))){
					stopCandidates.add(s);
				}
			}
		}
		return stopCandidates;
	}
	
	/**
	 * @param pRouteProvider 
	 * @param newHullInteriorStops
	 * @return
	 */
	private TransitStopFacility drawStop(PRouteProvider pRouteProvider, List<TransitStopFacility> newHullInteriorStops) {
		if(newHullInteriorStops.size() == 0){
			return null;
		}else{
//			TransitStopFacility newStop = null;
//			Double rnd = null;
//			if(newHullInteriorStops.size() > 1){
//				do{
//					//draw a random stop, if more than one stop is in the convex hull
//					for(TransitStopFacility f : newHullInteriorStops){
//						rnd = MatsimRandom.getRandom().nextDouble();
//						if(rnd < 0.1){
//							newStop = f;
//							break;
//						}
//					}
//					
//				}while(newStop == null);
//			}else{
//				newStop = newHullInteriorStops.get(0);
//			}
			
			// droeder code
//			int rnd = (int)(MatsimRandom.getRandom().nextDouble() * (newHullInteriorStops.size() - 1));
//			return newHullInteriorStops.get(rnd);
			return pRouteProvider.drawRandomStopFromList(newHullInteriorStops);
		}
	}

	/* (non-Javadoc)
	 * @see playground.andreas.P2.replanning.PStrategy#getName()
	 */
	@Override
	public String getName() {
		return STRATEGY_NAME;
	}

}

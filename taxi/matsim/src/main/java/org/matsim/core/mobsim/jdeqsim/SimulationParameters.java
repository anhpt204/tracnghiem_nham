/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.core.mobsim.jdeqsim;

import java.util.HashMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;

/**
 * The micro-simulation parameters.
 *
 * @author rashid_waraich
 */
public class SimulationParameters {

	// CONSTANTS
	public static final String START_LEG = "start leg";
	public static final String END_LEG = "end leg";
	public static final String ENTER_LINK = "enter link";
	public static final String LEAVE_LINK = "leave link";
	/**
	 *
	 * the priorities of the messages. a higher priority comes first in the
	 * message queue (when same time) usage: for example a person has a enter
	 * road message at the same time as leaving the previous road (need to keep
	 * the messages in right order) for events with same time stamp: <br>
	 * leave < arrival < departure < enter especially for testing this is
	 * important
	 *
	 */
	public static final int PRIORITY_LEAVE_ROAD_MESSAGE = 200;
	public static final int PRIORITY_ARRIVAL_MESSAGE = 150;
	public static final int PRIORITY_DEPARTUARE_MESSAGE = 125;
	public static final int PRIORITY_ENTER_ROAD_MESSAGE = 100;

	// INPUT
	private static double simulationEndTime; // in s
	private static double gapTravelSpeed; // in m/s
	private static double flowCapacityFactor; // 1.0 is default
	private static double storageCapacityFactor; // 1.0 is default
	private static double carSize; // in meter
	// in [vehicles/hour] per lane, can be scaled with flow capacity factor
	private static double minimumInFlowCapacity;
	/**
	 * stuckTime is used for deadlock prevention. when a car waits for more than
	 * 'stuckTime' for entering next road, it will enter the next. in seconds
	 */
	private static double squeezeTime;
	/**
	 * this must be initialized before starting the simulation! mapping:
	 * key=linkId used to find a road corresponding to a link
	 */
	private static HashMap<Id<Link>, Road> allRoads = null;

	public static void reset(){
		simulationEndTime = Double.MAX_VALUE;
		gapTravelSpeed = 15.0;
		flowCapacityFactor = 1.0;
		storageCapacityFactor = 1.0;
		carSize = 7.5;
		minimumInFlowCapacity = 1800;
		squeezeTime = 1800;
		allRoads = null;
		processEventThread = null;
	}


	// SETTINGS
	// should garbage collection of messages be activated
	private static boolean GC_MESSAGES = false;

	// OUTPUT
	// The thread for processing the events
	private static EventsManager processEventThread = null;

	// METHODS
	public static boolean isGC_MESSAGES() {
		return GC_MESSAGES;
	}

	public static void setGC_MESSAGES(boolean gc_messages) {
		GC_MESSAGES = gc_messages;
	}

	public static double getSimulationEndTime() {
		return simulationEndTime;
	}

	public static void setSimulationEndTime(double simulationEndTime) {
		SimulationParameters.simulationEndTime = simulationEndTime;
	}

	public static double getGapTravelSpeed() {
		return gapTravelSpeed;
	}

	public static void setGapTravelSpeed(double gapTravelSpeed) {
		SimulationParameters.gapTravelSpeed = gapTravelSpeed;
	}

	public static double getFlowCapacityFactor() {
		return flowCapacityFactor;
	}

	public static void setFlowCapacityFactor(double flowCapacityFactor) {
		SimulationParameters.flowCapacityFactor = flowCapacityFactor;
	}

	public static double getStorageCapacityFactor() {
		return storageCapacityFactor;
	}

	public static void setStorageCapacityFactor(double storageCapacityFactor) {
		SimulationParameters.storageCapacityFactor = storageCapacityFactor;
	}

	public static double getCarSize() {
		return carSize;
	}

	public static void setCarSize(double carSize) {
		SimulationParameters.carSize = carSize;
	}

	public static double getMinimumInFlowCapacity() {
		return minimumInFlowCapacity;
	}

	public static void setMinimumInFlowCapacity(double minimumInFlowCapacity) {
		SimulationParameters.minimumInFlowCapacity = minimumInFlowCapacity;
	}

	public static double getSqueezeTime() {
		return squeezeTime;
	}

	public static void setSqueezeTime(double squeezeTime) {
		SimulationParameters.squeezeTime = squeezeTime;
	}

	public static EventsManager getProcessEventThread() {
		return processEventThread;
	}

	public static void setProcessEventThread(EventsManager processEventThread) {
		SimulationParameters.processEventThread = processEventThread;
	}

	public static HashMap<Id<Link>, Road> getAllRoads() {
		return allRoads;
	}

	public static void setAllRoads(HashMap<Id<Link>, Road> allRoads) {
		SimulationParameters.allRoads = allRoads;
	}

}

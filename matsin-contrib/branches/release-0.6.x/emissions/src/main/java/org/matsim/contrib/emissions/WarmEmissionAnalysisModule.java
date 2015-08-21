/* *********************************************************************** *
 /* ********************************************************************** *
 * project: org.matsim.*												   *
 * WarmEmissionAnalysisModule.java									       *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
 *                                                                         *
 * *********************************************************************** */
package org.matsim.contrib.emissions;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.contrib.emissions.events.WarmEmissionEventImpl;
import org.matsim.contrib.emissions.types.HbefaTrafficSituation;
import org.matsim.contrib.emissions.types.HbefaVehicleAttributes;
import org.matsim.contrib.emissions.types.HbefaVehicleCategory;
import org.matsim.contrib.emissions.types.HbefaWarmEmissionFactor;
import org.matsim.contrib.emissions.types.HbefaWarmEmissionFactorKey;
import org.matsim.contrib.emissions.types.WarmPollutant;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.collections.Tuple;


/**
 * @author benjamin
 *
 */
public class WarmEmissionAnalysisModule {
	private static final Logger logger = Logger.getLogger(WarmEmissionAnalysisModule.class);

	final Map<Integer, String> roadTypeMapping;

	final Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> avgHbefaWarmTable;
	final Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> detailedHbefaWarmTable;

	final EventsManager eventsManager;
	final Double emissionEfficiencyFactor;

	int vehAttributesNotSpecifiedCnt = 0;
	int averageSpeedNegativeCnt = 0;
	int averageSpeedTooHighCnt = 0;
	final int maxWarnCnt = 3;
	
	// The following was tested to slow down significantly, therefore counters were commented out:
//	Set<Id> vehAttributesNotSpecified = Collections.synchronizedSet(new HashSet<Id>());
//	Set<Id> vehicleIdSet = Collections.synchronizedSet(new HashSet<Id>());

	int freeFlowCounter = 0;
	int stopGoCounter = 0;
	int fractionCounter = 0;
	int emissionEventCounter = 0;
	
	double kmCounter = 0.0;
	double freeFlowKmCounter = 0.0;
	double stopGoKmCounter = 0.0;


	public static class WarmEmissionAnalysisModuleParameter {

		public Map<Integer, String> roadTypeMapping;
		public Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> avgHbefaWarmTable;
		public Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> detailedHbefaWarmTable;

		public WarmEmissionAnalysisModuleParameter(
				Map<Integer, String> roadTypeMapping,
				Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> avgHbefaWarmTable,
				Map<HbefaWarmEmissionFactorKey, HbefaWarmEmissionFactor> detailedHbefaWarmTable) {
			this.roadTypeMapping = roadTypeMapping;
			this.avgHbefaWarmTable = avgHbefaWarmTable;
			this.detailedHbefaWarmTable = detailedHbefaWarmTable;
			// check if all needed tables are non-null
			if(roadTypeMapping == null){
				 logger.error("Road type mapping not set. Aborting...");
				 System.exit(0);
			}
			if(avgHbefaWarmTable == null && detailedHbefaWarmTable == null){
				 logger.error("Neither average nor detailed table vor Hbefa warm emissions set. Aborting...");
				 System.exit(0);
			}
		}
	}

	public WarmEmissionAnalysisModule(
			WarmEmissionAnalysisModuleParameter parameterObject,
			EventsManager emissionEventsManager, Double emissionEfficiencyFactor) {
		
		if(parameterObject == null){
			logger.error("No warm emission analysis module parameter set. Aborting...");
			System.exit(0);
		}
		if(emissionEventsManager == null){
			logger.error("Event manager not set. Please check the configuration of your scenario. Aborting..." );
			System.exit(0);
		}
		this.roadTypeMapping = parameterObject.roadTypeMapping;
		this.avgHbefaWarmTable = parameterObject.avgHbefaWarmTable;
		this.detailedHbefaWarmTable = parameterObject.detailedHbefaWarmTable;
		this.eventsManager = emissionEventsManager;
		this.emissionEfficiencyFactor = emissionEfficiencyFactor;
		

		
	}

	public void reset() {
		logger.info("resetting counters...");
		vehAttributesNotSpecifiedCnt = 0;
		averageSpeedNegativeCnt = 0;
		averageSpeedTooHighCnt = 0;
//		vehAttributesNotSpecified.clear();
//		vehicleIdSet.clear();
	
		freeFlowCounter = 0;
		stopGoCounter = 0;
		fractionCounter = 0;
		emissionEventCounter = 0;
		
		kmCounter = 0.0;
		freeFlowKmCounter = 0.0;
		stopGoKmCounter = 0.0;
	}

	public void throwWarmEmissionEvent(double leaveTime, Id linkId, Id vehicleId, Map<WarmPollutant, Double> warmEmissions){
		Event warmEmissionEvent = new WarmEmissionEventImpl(leaveTime, linkId, vehicleId, warmEmissions);
		this.eventsManager.processEvent(warmEmissionEvent);
	}

	public Map<WarmPollutant, Double> checkVehicleInfoAndCalculateWarmEmissions(
			Id personId,
			Integer roadType,
			Double freeVelocity,
			Double linkLength,
			Double travelTime,
			String vehicleInformation) {

		Map<WarmPollutant, Double> warmEmissions;
		if(vehicleInformation == null){
			throw new RuntimeException("Vehicle type description for person " + personId + "is missing. " +
					"Please make sure that requirements for emission vehicles in "
					+ VspExperimentalConfigGroup.GROUP_NAME + " config group are met. Aborting...");
		}
		
		Tuple<HbefaVehicleCategory, HbefaVehicleAttributes> vehicleInformationTuple = convertString2Tuple(vehicleInformation);
		if (vehicleInformationTuple.getFirst() == null){
			throw new RuntimeException("Vehicle category for person " + personId + " is not valid. " +
					"Please make sure that requirements for emission vehicles in " + 
					VspExperimentalConfigGroup.GROUP_NAME + " config group are met. Aborting...");
		}
		warmEmissions = calculateWarmEmissions(personId, travelTime, roadType, freeVelocity, linkLength, vehicleInformationTuple);
		
		// a basic apporach to introduce emission reduced cars:
		if(emissionEfficiencyFactor != null){
			warmEmissions = rescaleWarmEmissions(warmEmissions);
		}
		return warmEmissions;
	}
	
	private Map<WarmPollutant, Double> rescaleWarmEmissions(Map<WarmPollutant, Double> warmEmissions) {
		Map<WarmPollutant, Double> rescaledWarmEmissions = new HashMap<WarmPollutant, Double>();
		
		for(WarmPollutant wp : warmEmissions.keySet()){
			Double orgValue = warmEmissions.get(wp);
			Double rescaledValue = emissionEfficiencyFactor * orgValue;
			rescaledWarmEmissions.put(wp, rescaledValue);
		}
		return rescaledWarmEmissions;
	}

	private Map<WarmPollutant, Double> calculateWarmEmissions(
			Id personId,
			Double travelTime,
			Integer roadType,
			Double freeVelocity,
			Double linkLength,
			Tuple<HbefaVehicleCategory, HbefaVehicleAttributes> vehicleInformationTuple) {

		Map<WarmPollutant, Double> warmEmissionsOfEvent = new HashMap<WarmPollutant, Double>();

		String hbefaRoadTypeName = this.roadTypeMapping.get(roadType);

		HbefaWarmEmissionFactorKey keyFreeFlow = new HbefaWarmEmissionFactorKey();
		HbefaWarmEmissionFactorKey keyStopAndGo = new HbefaWarmEmissionFactorKey();

		if(vehicleInformationTuple.getFirst().equals(HbefaVehicleCategory.HEAVY_GOODS_VEHICLE)){
			keyFreeFlow.setHbefaVehicleCategory(HbefaVehicleCategory.HEAVY_GOODS_VEHICLE);
			keyStopAndGo.setHbefaVehicleCategory(HbefaVehicleCategory.HEAVY_GOODS_VEHICLE);
		} else{
			keyFreeFlow.setHbefaVehicleCategory(HbefaVehicleCategory.PASSENGER_CAR);
			keyStopAndGo.setHbefaVehicleCategory(HbefaVehicleCategory.PASSENGER_CAR);
		}
		keyFreeFlow.setHbefaRoadCategory(hbefaRoadTypeName);
		keyStopAndGo.setHbefaRoadCategory(hbefaRoadTypeName);
		keyFreeFlow.setHbefaTrafficSituation(HbefaTrafficSituation.FREEFLOW);
		keyStopAndGo.setHbefaTrafficSituation(HbefaTrafficSituation.STOPANDGO);

		if(this.detailedHbefaWarmTable != null){ // check if detailed emission factors file is set in config
			HbefaVehicleAttributes hbefaVehicleAttributes = new HbefaVehicleAttributes();
			hbefaVehicleAttributes.setHbefaTechnology(vehicleInformationTuple.getSecond().getHbefaTechnology());
			hbefaVehicleAttributes.setHbefaSizeClass(vehicleInformationTuple.getSecond().getHbefaSizeClass());
			hbefaVehicleAttributes.setHbefaEmConcept(vehicleInformationTuple.getSecond().getHbefaEmConcept());
			keyFreeFlow.setHbefaVehicleAttributes(hbefaVehicleAttributes);
			keyStopAndGo.setHbefaVehicleAttributes(hbefaVehicleAttributes);
		}
		
		double linkLength_km = linkLength / 1000;
		double travelTime_h = travelTime / 3600;
		double freeFlowSpeed_kmh = freeVelocity * 3.6;
		double averageSpeed_kmh = linkLength_km / travelTime_h;
		
		double freeFlowSpeedFromTable_kmh;
		double stopGoSpeedFromTable_kmh;
		double efFreeFlow_gpkm;
		double efStopGo_gpkm;

		for (WarmPollutant warmPollutant : WarmPollutant.values()) {
			double generatedEmissions;

			keyFreeFlow.setHbefaComponent(warmPollutant);
			keyStopAndGo.setHbefaComponent(warmPollutant);
			
			if(this.detailedHbefaWarmTable != null){
				if(this.detailedHbefaWarmTable.get(keyFreeFlow) != null && this.detailedHbefaWarmTable.get(keyStopAndGo) != null){
					stopGoSpeedFromTable_kmh = this.detailedHbefaWarmTable.get(keyStopAndGo).getSpeed();
					efFreeFlow_gpkm = this.detailedHbefaWarmTable.get(keyFreeFlow).getWarmEmissionFactor();
					efStopGo_gpkm = this.detailedHbefaWarmTable.get(keyStopAndGo).getWarmEmissionFactor();
					freeFlowSpeedFromTable_kmh = this.detailedHbefaWarmTable.get(keyFreeFlow).getSpeed();

				} else {
					vehAttributesNotSpecifiedCnt++;
					stopGoSpeedFromTable_kmh = this.avgHbefaWarmTable.get(keyStopAndGo).getSpeed();
					efFreeFlow_gpkm = this.avgHbefaWarmTable.get(keyFreeFlow).getWarmEmissionFactor();
					efStopGo_gpkm = this.avgHbefaWarmTable.get(keyStopAndGo).getWarmEmissionFactor();
					freeFlowSpeedFromTable_kmh = this.avgHbefaWarmTable.get(keyFreeFlow).getSpeed();

					if(vehAttributesNotSpecifiedCnt <= maxWarnCnt) {
						logger.warn("Detailed vehicle attributes are not specified correctly for person " + personId + ": " + 
								"`" + vehicleInformationTuple.getSecond() + "'. Using fleet average values instead.");
						if(vehAttributesNotSpecifiedCnt == maxWarnCnt) logger.warn(Gbl.FUTURE_SUPPRESSED);
					}
//					vehAttributesNotSpecified.add(personId);
				}
			} else {
				stopGoSpeedFromTable_kmh = this.avgHbefaWarmTable.get(keyStopAndGo).getSpeed();
				efFreeFlow_gpkm = this.avgHbefaWarmTable.get(keyFreeFlow).getWarmEmissionFactor();
				efStopGo_gpkm = this.avgHbefaWarmTable.get(keyStopAndGo).getWarmEmissionFactor();
				freeFlowSpeedFromTable_kmh = this.avgHbefaWarmTable.get(keyFreeFlow).getSpeed();
//				vehAttributesNotSpecified.add(personId);
			}
			
			if(averageSpeed_kmh <= 0.0){
				throw new RuntimeException("Average speed has been calculated to 0.0 or a negative value. Aborting...");
			}
			if ((averageSpeed_kmh - freeFlowSpeed_kmh) > 1.0){
				throw new RuntimeException("Average speed has been calculated to be greater than free flow speed; this might produce negative warm emissions. Aborting...");
			}
			/* NOTE: the following comparision does not make sense since HBEFA assumes free flow speeds to be different from speed limits.
			 * For instance, for RUR/MW/80/Freeflow HBEFA assumes a free flow speed of 82.80 kmh.
			 * benjamin, amit 01'2014
			 * */		
//			if(freeFlowSpeedFromTable_kmh - freeFlowSpeed_kmh > 1.0 || freeFlowSpeedFromTable_kmh - freeFlowSpeed_kmh <-1.0){
//				logger.warn("The given free flow speed does not match the table's value. Please check consistency of your scenario!");
//				logger.info("Using given speed value to avoid negative emission values...");	
//			}
			if((averageSpeed_kmh - freeFlowSpeed_kmh) >= -1.0) { // both speeds are assumed to be not very different > only freeFlow on link
				generatedEmissions = linkLength_km * efFreeFlow_gpkm;
				freeFlowCounter++;
				freeFlowKmCounter = freeFlowKmCounter + linkLength_km;
			} else if ((averageSpeed_kmh - stopGoSpeedFromTable_kmh) <= 0.0) { // averageSpeed is less than stopGoSpeed > only stop&go on link
				generatedEmissions = linkLength_km * efStopGo_gpkm;
				stopGoCounter++;
				stopGoKmCounter = stopGoKmCounter + linkLength_km;
			} else {
				double distanceStopGo_km = (linkLength_km * stopGoSpeedFromTable_kmh * (freeFlowSpeed_kmh - averageSpeed_kmh)) / (averageSpeed_kmh * (freeFlowSpeed_kmh - stopGoSpeedFromTable_kmh));
				double distanceFreeFlow_km = linkLength_km - distanceStopGo_km;

				generatedEmissions = (distanceFreeFlow_km * efFreeFlow_gpkm) + (distanceStopGo_km * efStopGo_gpkm);
				fractionCounter++;
				stopGoKmCounter = stopGoKmCounter + distanceStopGo_km;
				freeFlowKmCounter = freeFlowKmCounter + distanceFreeFlow_km;
			}
			kmCounter = kmCounter + linkLength_km;
			warmEmissionsOfEvent.put(warmPollutant, generatedEmissions);
		}
		emissionEventCounter++;
//		vehicleIdSet.add(personId);
		return warmEmissionsOfEvent;
	}

	private Tuple<HbefaVehicleCategory, HbefaVehicleAttributes> convertString2Tuple(String vehicleInformation) {
		Tuple<HbefaVehicleCategory, HbefaVehicleAttributes> vehicleInformationTuple;
		HbefaVehicleCategory hbefaVehicleCategory = null;
		HbefaVehicleAttributes hbefaVehicleAttributes = new HbefaVehicleAttributes();

		String[] vehicleInformationArray = vehicleInformation.split(";");

		for(HbefaVehicleCategory vehCat : HbefaVehicleCategory.values()){
			if(vehCat.toString().equals(vehicleInformationArray[0])){
				hbefaVehicleCategory = vehCat;
			} else continue;
		}

		if(vehicleInformationArray.length == 4){
			hbefaVehicleAttributes.setHbefaTechnology(vehicleInformationArray[1]);
			hbefaVehicleAttributes.setHbefaSizeClass(vehicleInformationArray[2]);
			hbefaVehicleAttributes.setHbefaEmConcept(vehicleInformationArray[3]);
		} else{
			// interpretation as "average vehicle"
		}

		vehicleInformationTuple = new Tuple<HbefaVehicleCategory, HbefaVehicleAttributes>(hbefaVehicleCategory, hbefaVehicleAttributes);
		return vehicleInformationTuple;
	}

//public int getAverageSpeedTooHighCnt() {
//		return averageSpeedTooHighCnt;
//	}
//
//public int getAverageSpeedNegativeCnt() {
//		return averageSpeedNegativeCnt;
//	}

//	public Set<Id> getVehAttributesNotSpecified() {
//		return vehAttributesNotSpecified;
//	}
//
//	public Set<Id> getVehicleIdSet() {
//		return vehicleIdSet;
//	}

	public int getFreeFlowOccurences() {
		return freeFlowCounter / WarmPollutant.values().length;
	}

	public int getFractionOccurences() {
		return fractionCounter / WarmPollutant.values().length;
	}
	
	public int getStopGoOccurences() {
		return stopGoCounter / WarmPollutant.values().length;
	}

	public double getKmCounter() {
		return kmCounter / WarmPollutant.values().length;
	}

	public double getFreeFlowKmCounter() {
		return freeFlowKmCounter / WarmPollutant.values().length;
	}

	public double getStopGoKmCounter() {
		return stopGoKmCounter / WarmPollutant.values().length;
	}

	public int getWarmEmissionEventCounter() {
		return emissionEventCounter;
	}
	
}
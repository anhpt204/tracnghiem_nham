/* *********************************************************************** *
 * project: org.matsim.													   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,     *
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

/**
 * 
 */
package org.matsim.contrib.matsim4urbansim.populationmerge;


import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.matrixbasedptrouter.utils.CreateTestNetwork;
import org.matsim.contrib.matsim4urbansim.config.M4UConfigUtils;
import org.matsim.contrib.matsim4urbansim.config.modules.UrbanSimParameterConfigModuleV3;
import org.matsim.contrib.matsim4urbansim.constants.InternalConstants;
import org.matsim.contrib.matsim4urbansim.utils.OPUSDirectoryUtil;
import org.matsim.contrib.matsim4urbansim.utils.io.CreateHomeWorkHomePlan;
import org.matsim.contrib.matsim4urbansim.utils.io.ReadFromUrbanSimModel;
import org.matsim.contrib.matsim4urbansim.utils.io.ReadFromUrbanSimModel.PopulationCounter;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.testcases.MatsimTestCase;

/**
 * @author thomas
 *
 */
public class PopulationMergeTest extends MatsimTestCase{

	private static final Logger log = Logger.getLogger(PopulationMergeTest.class);
	
	private static final double samplingRate = 1.0;
	private static final int year = 2000;
	private static final double radius = 100.;
	
	@Test
	public void testPopulationMergeZoneColdStart(){
		Config config = M4UConfigUtils.createEmptyConfigWithSomeDefaults();
		OPUSDirectoryUtil.setTmpDirectories(config);
		
		log.info("Testing merge process of new and old population for cold start (zones)");
				
		ActivityFacilitiesImpl zones = createZones();
		
		// create dummy persons
		Population oldPop = null;
		PopulationCounter counter = runTestZone(oldPop, zones, config);
		
		OPUSDirectoryUtil.cleaningUpOPUSDirectories(config);
		
		Assert.assertTrue( counter.numberOfUrbanSimPersons == 6);
		Assert.assertTrue( counter.fromBackupCnt == 0);				// 0 since no old population is there for comparison
		Assert.assertTrue( counter.identifiedCnt == 0);				// 0 since no old population is there for comparison
		Assert.assertTrue( counter.employmentChangedCnt == 0);		// 0 since no old population is there for comparison
		Assert.assertTrue( counter.homelocationChangedCnt == 0);	// 0 since no old population is there for comparison
		Assert.assertTrue( counter.unemployedCnt == 0); 			// 0 since no old population is there for comparison
		Assert.assertTrue( counter.worklocationChangedCnt == 0); 	// 0 since no old population is there for comparison
		Assert.assertTrue( counter.newPersonCnt == 0); 				// 0 since no old population is there for comparison
		
		log.info("Testing merge process of new and old population for cold start (zones) done!");
	}
	
	@Test
	public void testPopulationMergeZoneWarmAndHotStart(){
		
		log.info("Testing merge process of new and old population for warm and hot start (zones)!");
		
		ActivityFacilitiesImpl zones = createZones();
		
		Scenario scenario = ScenarioUtils.createScenario(M4UConfigUtils.createEmptyConfigWithSomeDefaults());
		Config config = scenario.getConfig();
		OPUSDirectoryUtil.setTmpDirectories(config);
		
		// create dummy persons
		Population oldPop = scenario.getPopulation();
		ActivityFacility dummyFacility = zones.getFacilities().get( new IdImpl(1) );
		Coord dummyCoord = dummyFacility.getCoord();
		// create persons
		
		PersonImpl person1 = new PersonImpl( new IdImpl(1));
		PlanImpl plan1 = person1.createAndAddPlan(true);
		CreateHomeWorkHomePlan.makeHomePlan(plan1, dummyCoord, dummyFacility);
		person1.setEmployed(true);
		CreateHomeWorkHomePlan.completePlanToHwh(plan1, dummyCoord, dummyFacility);
		
		PersonImpl person2 = new PersonImpl( new IdImpl(2));
		PlanImpl plan2 = person2.createAndAddPlan(true);
		CreateHomeWorkHomePlan.makeHomePlan(plan2, dummyCoord, dummyFacility);
		person2.setEmployed(true);
		CreateHomeWorkHomePlan.completePlanToHwh(plan2, dummyCoord, dummyFacility);
		
		PersonImpl person3 = new PersonImpl( new IdImpl(3));
		PlanImpl plan3 = person3.createAndAddPlan(true);
		CreateHomeWorkHomePlan.makeHomePlan(plan3, dummyCoord, dummyFacility);
		person3.setEmployed(true);
		CreateHomeWorkHomePlan.completePlanToHwh(plan3, dummyCoord, dummyFacility);
		
		PersonImpl person4 = new PersonImpl( new IdImpl(4));
		PlanImpl plan4 = person4.createAndAddPlan(true);
		CreateHomeWorkHomePlan.makeHomePlan(plan4, dummyCoord, dummyFacility);
		person4.setEmployed(true);
		CreateHomeWorkHomePlan.completePlanToHwh(plan4, dummyCoord, dummyFacility);
		
		PersonImpl person5 = new PersonImpl( new IdImpl(5));
		PlanImpl plan5 = person5.createAndAddPlan(true);
		CreateHomeWorkHomePlan.makeHomePlan(plan5, dummyCoord, dummyFacility);
		person5.setEmployed(true);
		CreateHomeWorkHomePlan.completePlanToHwh(plan5, dummyCoord, dummyFacility);
		
		PersonImpl person6 = new PersonImpl( new IdImpl(6));
		PlanImpl plan6 = person6.createAndAddPlan(true);
		CreateHomeWorkHomePlan.makeHomePlan(plan6, dummyCoord, dummyFacility);
		person6.setEmployed(false);
		// person 6 is unemployed (no completePlanToHwh)
		
		oldPop.addPerson(person1);
		oldPop.addPerson(person2);
		oldPop.addPerson(person3);
		oldPop.addPerson(person4);
		oldPop.addPerson(person5);
		oldPop.addPerson(person6);
		
		PopulationCounter counter = runTestZone(oldPop, zones, config);
		
		OPUSDirectoryUtil.cleaningUpOPUSDirectories(config);
		
		Assert.assertTrue( counter.numberOfUrbanSimPersons == 6);   // number of processed persons
		Assert.assertTrue( counter.fromBackupCnt == 1);				// one person not found in new population (person 1)
		Assert.assertTrue( counter.identifiedCnt == 1);				// one person re-identified from old population (person 2)
		Assert.assertTrue( counter.employmentChangedCnt == 1);		// one person got unemployed (person 3)
		Assert.assertTrue( counter.homelocationChangedCnt == 1);	// one person moved to new home location (person 4)
		Assert.assertTrue( counter.unemployedCnt == 1); 			// one person was already unemployed (person 6)
		Assert.assertTrue( counter.worklocationChangedCnt == 1); 	// one person got a new job (person 5)
		Assert.assertTrue( counter.newPersonCnt == 1); 				// one person is completely new (person 7)
		
		log.info("Testingmerge process of new and old population for warm and hot start (zones) done!");
	}

	/**
	 * @return
	 */
	private ActivityFacilitiesImpl createZones() {
		// create dummy zone facilities
		ActivityFacilitiesImpl zones   = new ActivityFacilitiesImpl("urbansim zones");
		zones.createAndAddFacility(new IdImpl(1), new CoordImpl(0., 0.));
		zones.createAndAddFacility(new IdImpl(2), new CoordImpl(200., 100.));
		return zones;
	}
	
	private PopulationCounter runTestZone(Population oldPop, ActivityFacilitiesImpl zones, Config config){
		
		// creates necessary folder structure (OPUS_HOME)
		OPUSDirectoryUtil.createOPUSDirectories(config);
		// create dummy network
		Network network = CreateTestNetwork.createTestNetwork();
		// dump new dummy population zone 
		dumpDummyPopulationZone(config);
		
		// init 
		ReadFromUrbanSimModel readFromUrbansim = new ReadFromUrbanSimModel(year, null, radius, config);
		// start Population merge
		readFromUrbansim.readPersonsZone(oldPop, zones, network, samplingRate);
		
		// get Population counter
		return readFromUrbansim.getPopulationCounter();
		
	}
	
	private void dumpDummyPopulationZone(Config c){
		UrbanSimParameterConfigModuleV3 module = (UrbanSimParameterConfigModuleV3) c.getModule(UrbanSimParameterConfigModuleV3.GROUP_NAME);
		String fileLocation = module.getMATSim4OpusTemp() + InternalConstants.URBANSIM_PERSON_DATASET_TABLE + year + InternalConstants.FILE_TYPE_TAB;
		
		BufferedWriter bw = IOUtils.getBufferedWriter(fileLocation);
		
		// write header
		try {
			bw.write("person_id\tzone_id_home\tzone_id_work\n");
			// no person with id 1 to check if an emigration is detected
			// person 2 is not changed
			bw.write("2\t1\t1\n");
			// person 3 gets unemployed
			bw.write("3\t1\t-1\n");
			// person 4 moved (new home location)
			bw.write("4\t2\t1\n");
			// person 5 has a new job
			bw.write("5\t1\t2\n");
			// person 6 nothing changed (was already unemployed)
			bw.write("6\t1\t-1\n");
			// person 7 new person (migrated)
			bw.write("7\t2\t2\n");
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
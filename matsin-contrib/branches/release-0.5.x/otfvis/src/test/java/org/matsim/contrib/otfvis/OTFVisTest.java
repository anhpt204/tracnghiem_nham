/* *********************************************************************** *
 * project: org.matsim.*
 * OTFVisTest.java
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

/**
 * 
 */
package org.matsim.contrib.otfvis;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.SnapshotWriterFactoryRegister;
import org.matsim.core.controler.SnapshotWriterRegistrar;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.vis.otfvis.OTFFileWriterFactory;
import org.matsim.vis.snapshotwriters.SnapshotWriter;
import org.matsim.vis.snapshotwriters.SnapshotWriterFactory;

/**
 * Simple test case to ensure the converting from eventsfile to .mvi-file
 * Needs somehow a bunch of memory - please use "-Xmx630m"!
 * 
 * @author yu
 * 
 */
public class OTFVisTest extends MatsimTestCase {

	@Test
	public void testConvert() {
		String networkFilename = "test/scenarios/equil/network.xml";
		String eventsFilename = "test/scenarios/equil/events.txt.gz";
		String mviFilename = "test/scenarios/equil/events.mvi";

		String[] args = {"-convert", eventsFilename, networkFilename, mviFilename, "300"};
		OTFVis.main(args);

		File f = new File(mviFilename);
		Assert.assertTrue("No mvi file written!", f.exists());
	}

	@Test
	public void testOTFVisSnapshotWriterOnQueueSimulation() {
		final Config config = ConfigUtils.loadConfig("test/scenarios/equil/config_plans1.xml");
		config.controler().setLastIteration(2);
		config.controler().setWriteEventsInterval(0);
		config.controler().setWritePlansInterval(0);
		config.controler().setSnapshotFormat(Arrays.asList("otfvis"));
		config.simulation().setSnapshotPeriod(600);
		config.simulation().setSnapshotStyle("equiDist");

		final Controler controler = new Controler(config);
		controler.addSnapshotWriterFactory("otfvis", new OTFFileWriterFactory());
		controler.setOverwriteFiles(true);
		controler.setCreateGraphs(false);
		controler.setDumpDataAtEnd(false);
		controler.run();

		assertTrue(new File(controler.getControlerIO().getIterationFilename(0, "otfvis.mvi")).exists());
		assertTrue(new File(controler.getControlerIO().getIterationFilename(1, "otfvis.mvi")).exists());
		assertTrue(new File(controler.getControlerIO().getIterationFilename(2, "otfvis.mvi")).exists());
	}

	@Test
	public void testOTFVisSnapshotWriterOnQSim() {
		final Config config = ConfigUtils.loadConfig("test/scenarios/equil/config_plans1.xml");
		config.controler().setLastIteration(2);
		config.controler().setWriteEventsInterval(0);
		config.controler().setWritePlansInterval(0);
		config.controler().setMobsim("qsim");
		config.controler().setSnapshotFormat(Arrays.asList("otfvis"));
		QSimConfigGroup qSimConfigGroup = new QSimConfigGroup();
		qSimConfigGroup.setSnapshotPeriod(600);
		qSimConfigGroup.setSnapshotStyle("equiDist");
		config.addQSimConfigGroup(qSimConfigGroup);

		final Controler controler = new Controler(config);
		controler.addSnapshotWriterFactory("otfvis", new OTFFileWriterFactory());
		controler.setOverwriteFiles(true);
		controler.setCreateGraphs(false);
		controler.setDumpDataAtEnd(false);
		controler.run();

		assertTrue(new File(controler.getControlerIO().getIterationFilename(0, "otfvis.mvi")).exists());
		assertTrue(new File(controler.getControlerIO().getIterationFilename(1, "otfvis.mvi")).exists());
		assertTrue(new File(controler.getControlerIO().getIterationFilename(2, "otfvis.mvi")).exists());
	}


	public void testGivesInstanceForOtfvisSnapshotWriter() {
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		SnapshotWriterRegistrar registrar = new SnapshotWriterRegistrar();
		SnapshotWriterFactoryRegister register = registrar.getFactoryRegister();
		register.register("otfvis", new OTFFileWriterFactory());
		SnapshotWriterFactory factory = register.getInstance("otfvis");
		SnapshotWriter snapshotWriter = factory.createSnapshotWriter(getOutputDirectory() + factory.getPreferredBaseFilename(), scenario);
		snapshotWriter.finish();
	}

}

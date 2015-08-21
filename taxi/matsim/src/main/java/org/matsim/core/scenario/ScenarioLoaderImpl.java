/* *********************************************************************** *
 * project: org.matsim.*
 * ScenarioLoader
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
 * *********************************************************************** */
package org.matsim.core.scenario;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.facilities.MatsimFacilitiesReader;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkChangeEventsParser;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.network.TimeVariantLinkFactory;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.utils.io.MatsimFileTypeGuesser;
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.households.HouseholdsReaderV10;
import org.matsim.lanes.data.MatsimLaneDefinitionsReader;
import org.matsim.lanes.data.v20.LaneDefinitions20;
import org.matsim.lanes.data.v20.LaneDefinitions20Impl;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.signalsystems.data.SignalsData;
import org.matsim.signalsystems.data.SignalsScenarioLoader;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;
import org.matsim.vehicles.VehicleReaderV1;

import java.io.File;

/**
 * Loads elements of Scenario from file. Non standardized elements
 * can also be loaded however they require a specific instance of
 * Scenario.
 * {@link #loadScenario()} reads the complete scenario from files while the
 * other load...() methods only load specific parts
 * of the scenario assuming that required parts are already
 * loaded or created by the user.
 * <p/>
 * Design thoughts:<ul>
 * <li> Given what we have now, does it make sense to leave this class public?  yy kai, mar'11
 * </ul>
 *
 * @see org.matsim.core.scenario.ScenarioImpl
 *
 * @author dgrether
 */
public class ScenarioLoaderImpl {

	private static final Logger log = Logger.getLogger(ScenarioLoaderImpl.class);


	static Scenario loadScenario(Config config) {
		// deliberately non-public.  Use method in ScenarioUtils.
		ScenarioLoaderImpl scenarioLoader = new ScenarioLoaderImpl(config);
		Scenario scenario = scenarioLoader.loadScenario();
		return scenario;
	}

	static void loadScenario(Scenario scenario) {
		// deliberately non-public.  Use method in ScenarioUtils.
		ScenarioLoaderImpl scenarioLoader = new ScenarioLoaderImpl(scenario);
		scenarioLoader.loadScenario();
	}

	/**
	 * @deprecated  This used to be a constructor with a global side effect, which is absolutely evil.
	 *				Please just load the Scenario with ScenarioUtils.loadScenario instead.
	 */
	@Deprecated
	public static ScenarioLoaderImpl createScenarioLoaderImplAndResetRandomSeed(String configFilename) {
		Config config = ConfigUtils.loadConfig(configFilename);
		MatsimRandom.reset(config.global().getRandomSeed());
		ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario(config);
		return new ScenarioLoaderImpl(scenario);
	}

	private final Config config;

	private final ScenarioImpl scenario;

	/**
	 * yy Does it make sense to leave this constructor public?  kai, mar'11
	 */
	@Deprecated // please use the static calls in ScenarioUtils instead
	public ScenarioLoaderImpl(Config config) {
		this.config = config;
		this.scenario = (ScenarioImpl) ScenarioUtils.createScenario(this.config);
	}

	/**
	 * yy Does it make sense to leave this constructor public?  kai, mar'11
	 */
	@Deprecated // please use the static calls in ScenarioUtils instead
	public ScenarioLoaderImpl(Scenario scenario) {
		this.scenario = (ScenarioImpl) scenario;
		this.config = this.scenario.getConfig();
	}


	/**
	 * @deprecated  Please use the static calls in ScenarioUtils instead.
	 *
	 */
	@Deprecated
	public Scenario getScenario() {
		return this.scenario;
	}

	/**
	 * Loads all mandatory Scenario elements and
	 * if activated in config's scenario module/group
	 * optional elements.
	 * @deprecated  Please use the static calls in ScenarioUtils instead.
	 * @return the Scenario
	 */
	@Deprecated
	public Scenario loadScenario() {
		String currentDir = new File("tmp").getAbsolutePath();
		currentDir = currentDir.substring(0, currentDir.length() - 3);
		log.info("loading scenario from base directory: " + currentDir);
		this.loadNetwork();
		this.loadActivityFacilities();
		this.loadPopulation();
		if (this.config.scenario().isUseHouseholds()) {
			this.loadHouseholds();
		}
		if (this.config.scenario().isUseTransit()) {
			this.loadTransit();
		}
		if (this.config.scenario().isUseVehicles()) {
			this.loadVehicles();
		}
		if (this.config.scenario().isUseLanes()) {
			this.loadLanes();
		}
		if (this.config.scenario().isUseSignalSystems()){
			this.loadSignalSystems();
		}
		return this.scenario;
	}

	/**
	 * Loads the network into the scenario of this class
	 *
	 * @deprecated  Please use the static calls in ScenarioUtils to load a scenario.
	 * 				If you want only a network, use the MatsimNetworkReader directly.
	 *
	 */
	@Deprecated
	public void loadNetwork() {
		String networkFileName = null;
		if ((this.config.network() != null) && (this.config.network().getInputFile() != null)) {
			networkFileName = this.config.network().getInputFile();
			log.info("loading network from " + networkFileName);
			NetworkImpl network = (NetworkImpl) this.scenario.getNetwork();
			if (this.config.network().isTimeVariantNetwork()) {
				log.info("use TimeVariantLinks in NetworkFactory.");
				network.getFactory().setLinkFactory(new TimeVariantLinkFactory());
			}
			new MatsimNetworkReader(this.scenario).parse(networkFileName);
			if ((this.config.network().getChangeEventsInputFile() != null) && this.config.network().isTimeVariantNetwork()) {
				log.info("loading network change events from " + this.config.network().getChangeEventsInputFile());
				NetworkChangeEventsParser parser = new NetworkChangeEventsParser(network);
				parser.parse(this.config.network().getChangeEventsInputFile());
				network.setNetworkChangeEvents(parser.getEvents());
			}
		}
	}

	/**
	 * @deprecated  Please use the static calls in ScenarioUtils to load a scenario.
	 * 				If you want only Facilities, use the MatsimFacilitiesReader directly.
	 *
	 */
	@Deprecated
	public void loadActivityFacilities() {
		if ((this.config.facilities() != null) && (this.config.facilities().getInputFile() != null)) {
			String facilitiesFileName = this.config.facilities().getInputFile();
			log.info("loading facilities from " + facilitiesFileName);
			new MatsimFacilitiesReader(this.scenario).parse(facilitiesFileName);
			log.info("loaded " + this.scenario.getActivityFacilities().getFacilities().size() + " facilities from " + facilitiesFileName);
		}
		else {
			log.info("no facilities file set in config, therefore not loading any facilities.  This is not a problem except if you are using facilities");
		}
		if ((this.config.facilities() != null) && (this.config.facilities().getInputFacilitiesAttributesFile() != null)) {
			String facilitiesAttributesFileName = this.config.facilities().getInputFacilitiesAttributesFile();
			log.info("loading facility attributes from " + facilitiesAttributesFileName);
			new ObjectAttributesXmlReader(this.scenario.getActivityFacilities().getFacilityAttributes()).parse(facilitiesAttributesFileName);
		}
		else {
			log.info("no facility-attributes file set in config, not loading any facility attributes");
		}
	}

	/**
	 * @deprecated  Please use the static calls in ScenarioUtils to load a scenario.
	 * 				If you want only a Population, use the MatsimPopulationReader directly.
	 *
	 */
	@Deprecated
	public void loadPopulation() {
		if ((this.config.plans() != null) && (this.config.plans().getInputFile() != null)) {
			String populationFileName = this.config.plans().getInputFile();
			log.info("loading population from " + populationFileName);
			new MatsimPopulationReader(this.scenario).parse(populationFileName);

			if (this.scenario.getPopulation() instanceof PopulationImpl) {
				((PopulationImpl)this.scenario.getPopulation()).printPlansCount();
			}
		}
		else {
			log.info("no population file set in config, not able to load population");
		}
		if ((this.config.plans() != null) && (this.config.plans().getInputPersonAttributeFile() != null)) {
			String personAttributesFileName = this.config.plans().getInputPersonAttributeFile();
			log.info("loading person attributes from " + personAttributesFileName);
			new ObjectAttributesXmlReader(this.scenario.getPopulation().getPersonAttributes()).parse(personAttributesFileName);
		}
		else {
			log.info("no person-attributes file set in config, not loading any person attributes");
		}
	}

	private void loadHouseholds() {
		if ((this.scenario.getHouseholds() != null) && (this.config.households() != null) && (this.config.households().getInputFile() != null) ) {
			String hhFileName = this.config.households().getInputFile();
			log.info("loading households from " + hhFileName);
			new HouseholdsReaderV10(this.scenario.getHouseholds()).parse(hhFileName);
			log.info("households loaded.");
		}
		else {
			log.info("no households file set in config or feature disabled, not able to load anything");
		}
		if ((this.config.households() != null) && (this.config.households().getInputHouseholdAttributesFile() != null)) {
			String householdAttributesFileName = this.config.households().getInputHouseholdAttributesFile();
			log.info("loading household attributes from " + householdAttributesFileName);
			new ObjectAttributesXmlReader(this.scenario.getHouseholds().getHouseholdAttributes()).parse(householdAttributesFileName);
		}
		else {
			log.info("no household-attributes file set in config, not loading any household attributes");
		}
	}

	private void loadTransit() throws UncheckedIOException {
		new TransitScheduleReader(this.scenario).readFile(this.config.transit().getTransitScheduleFile());
		if ((this.config.transit() != null) && (this.config.transit().getTransitLinesAttributesFile() != null)) {
			String transitLinesAttributesFileName = this.config.transit().getTransitLinesAttributesFile();
			log.info("loading transit lines attributes from " + transitLinesAttributesFileName);
			new ObjectAttributesXmlReader(this.scenario.getTransitSchedule().getTransitLinesAttributes()).parse(transitLinesAttributesFileName);
		}
		if ((this.config.transit() != null) && (this.config.transit().getTransitStopsAttributesFile() != null)) {
			String transitStopsAttributesFileName = this.config.transit().getTransitStopsAttributesFile();
			log.info("loading transit stop facilities attributes from " + transitStopsAttributesFileName);
			new ObjectAttributesXmlReader(this.scenario.getTransitSchedule().getTransitStopsAttributes()).parse(transitStopsAttributesFileName);
		}
	}

	private void loadVehicles() throws UncheckedIOException {
		final String vehiclesFile = this.config.transit().getVehiclesFile();
		log.info("loading vehicles from " + vehiclesFile );
		new VehicleReaderV1(this.scenario.getVehicles()).readFile(vehiclesFile);
	}

	private void loadLanes() {
		LaneDefinitions20 laneDefinitions = new LaneDefinitions20Impl();
		this.scenario.addScenarioElement(LaneDefinitions20.ELEMENT_NAME, laneDefinitions);
		String filename = this.config.network().getLaneDefinitionsFile();
		if (filename != null){
			MatsimFileTypeGuesser fileTypeGuesser = new MatsimFileTypeGuesser(filename);
			if (!MatsimLaneDefinitionsReader.SCHEMALOCATIONV20.equalsIgnoreCase(fileTypeGuesser
					.getSystemId())) {
				log.error("Lanes: Wrong file format. With the 0.5 version of matsim the scenario only accepts lane definitions in the "
						+ "file format version 2.0, i.e. "
						+ MatsimLaneDefinitionsReader.SCHEMALOCATIONV20
						+ ". An automatic conversion of the 1.1 file format is no longer provided, please call the "
						+ "LaneDefinitonsV11ToV20Converter manually in the preprocessing phase.");
				throw new UncheckedIOException("Wrong lane file format: " + fileTypeGuesser.getSystemId());
			}
		}
		if ((laneDefinitions != null) && (filename != null)) {
			MatsimLaneDefinitionsReader reader = new MatsimLaneDefinitionsReader(this.scenario);
			reader.readFile(filename);
		}
		else {
			log.info("no lane definition file set in config or feature disabled, not able to load anything");
		}
	}

	private void loadSignalSystems() {
		this.scenario.addScenarioElement(
				SignalsData.ELEMENT_NAME,
				new SignalsScenarioLoader(this.config.signalSystems()).loadSignalsData());
	}

}

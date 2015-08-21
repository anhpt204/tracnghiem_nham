/* *********************************************************************** *
 * project: org.matsim.*
 * MATSimConfigObject.java
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
package org.matsim.contrib.matsim4opus.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.matsim.contrib.matsim4opus.constants.Constants;
import org.matsim.contrib.matsim4opus.matsim4urbansim.jaxbconfig.ConfigType;
import org.matsim.contrib.matsim4opus.matsim4urbansim.jaxbconfig.Matsim4UrbansimType;
import org.matsim.contrib.matsim4opus.matsim4urbansim.jaxbconfig.MatsimConfigType;
import org.matsim.contrib.matsim4opus.utils.ids.IdFactory;
import org.matsim.contrib.matsim4opus.utils.io.Paths;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.config.groups.NetworkConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.config.groups.SimulationConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.scenario.ScenarioImpl;

/**
 * @author thomas
 * 
 * improvements dec'11:
 * - adjusting flow- and storage capacities to population sample rate. The
 * storage capacity includes a fetch factor to avoid backlogs and network breakdown
 * for small sample rates.
 * 
 * improvements jan'12:
 * - initGlobalSettings sets the number of available processors in the 
 * 	GlobalConfigGroup to speed up MATSim computations. Before that only
 * 	2 processors were used even if there are more.
 * 
 * improvements feb'12:
 * - setting mutationrange = 2h for TimeAllocationMutator (this seems to 
 * shift the depature times ???)
 *
 */
public class InitMATSimScenario {
	
	// logger
	private static final Logger log = Logger.getLogger(InitMATSimScenario.class);
	
	// MATSim scenario
	private ScenarioImpl scenario 	= null;
	// JAXB representation of matsim4urbansim config
	private MatsimConfigType matsimConfig = null;
	
	/**
	 * constructor
	 * 
	 * @param scenario stores MATSim parameters
	 * @param matsimConfig stores all parameters from matsim4urbansim config ( generated by UrbanSim )
	 */
	public InitMATSimScenario(ScenarioImpl scenario, MatsimConfigType matsimConfig){
		this.scenario = scenario;
		this.matsimConfig = matsimConfig;	
	}
	
	/**
	 * constructor
	 * 
	 * @param scenario stores MATSim parameters
	 * @param matsimConfiFile path to matsim config file
	 */
	public InitMATSimScenario(ScenarioImpl scenario, String matsimConfiFile){
		
		this.scenario = scenario;
		this.matsimConfig = unmarschal(matsimConfiFile); // loading and initializing MATSim config		
	}
	
	/**
	 * loading, validating and initializing MATSim config.
	 */
	MatsimConfigType unmarschal(String matsimConfigFile){
		
		// JAXBUnmaschal reads the UrbanSim generated MATSim config, validates it against
		// the current xsd (checks e.g. the presents and data type of parameter) and generates
		// an Java object representing the config file.
		JAXBUnmaschal unmarschal = new JAXBUnmaschal( matsimConfigFile );
		
		MatsimConfigType matsimConfig = null;
		
		// binding the parameter from the MATSim Config into the JAXB data structure
		if( (matsimConfig = unmarschal.unmaschalMATSimConfig()) == null){
			log.error("Unmarschalling failed. SHUTDOWN MATSim!");
			System.exit(Constants.UNMARSCHALLING_FAILED);
		}
		return matsimConfig;
	}
	
	/**
	 * Transferring all parameter from matsim4urbansim config to internal MATSim config/scenario
	 * @return boolean true if initialization successful
	 */
	public boolean init(){
		
		try{
			// get root elements from JAXB matsim4urbansim config object
			ConfigType matsimParameter = matsimConfig.getConfig();
			Matsim4UrbansimType matsim4UrbanSimParameter = matsimConfig.getMatsim4Urbansim();
			
			initGlobalSettings(); // tnicolai: experimental
			initMATSim4UrbanSimParameter(matsim4UrbanSimParameter);
			initNetwork(matsimParameter);
			initInputPlansFile(matsimParameter);
			initControler(matsimParameter);
			initPlanCalcScore(matsimParameter);
			initSimulation();
			initStrategy();
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Determines and sets available processors into MATSim config
	 */
	private void initGlobalSettings(){
		log.info("Setting GlobalConfigGroup to config...");
		GlobalConfigGroup globalCG = (GlobalConfigGroup) scenario.getConfig().getModule(GlobalConfigGroup.GROUP_NAME);
		globalCG.setNumberOfThreads(Runtime.getRuntime().availableProcessors());
		log.info("GlobalConfigGroup settings:");
		log.info("Number of Threads: " + Runtime.getRuntime().availableProcessors() + " ...");
		log.info("... done!");
	}
	
	/**
	 * store matsim4urbansim parameter in MATSim config.Param()
	 * 
	 * @param matsim4UrbanSimParameter
	 */
	private void initMATSim4UrbanSimParameter(Matsim4UrbansimType matsim4UrbanSimParameter){
		log.info("Setting MATSim4UrbanSim to config...");
		double samplingRate = matsim4UrbanSimParameter.getUrbansimParameter().getSamplingRate();
		int year = matsim4UrbanSimParameter.getUrbansimParameter().getYear().intValue();
		String opusHome = Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getOpusHome() );
		String opusDataPath = Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getOpusDataPath() );
		String matsim4Opus = Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4Opus() );
		String matsim4OpusConfig = Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4OpusConfig() );
		String matsim4OpusOutput = Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4OpusOutput() );
		String matsim4OpusTemp = Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4OpusTemp() );
		String matsim4OpusBackup = Paths.checkPathEnding( matsim4UrbanSimParameter.getUrbansimParameter().getMatsim4Opus() ) + Paths.checkPathEnding( "backup" );
		boolean isTestRun = matsim4UrbanSimParameter.getUrbansimParameter().isIsTestRun();
		boolean backupRunData = matsim4UrbanSimParameter.getUrbansimParameter().isBackupRunData();
		String testParameter = matsim4UrbanSimParameter.getUrbansimParameter().getTestParameter();
		
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.IS_TEST_RUN, isTestRun ? "TRUE" : "FALSE");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.SAMPLING_RATE, samplingRate + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.OPUS_HOME_PARAM, opusHome );
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.OPUS_DATA_PATH_PARAM, opusDataPath );
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_PARAM, matsim4Opus );
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_CONFIG_PARAM, matsim4OpusConfig );
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_OUTPUT_PARAM, matsim4OpusOutput );
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_TEMP_PARAM, matsim4OpusTemp );
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_BACKUP_PARAM, matsim4OpusBackup );
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.YEAR, year + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BACKUP_RUN_DATA_PARAM, backupRunData ? "TRUE" : "FALSE");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.TEST_PARAMETER_PARAM, testParameter);
		// tnicolai: implement/make configurable the following flags (matsim4urbansim config/JAXB)
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA, 1 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_TRAVEL_TIMES, 1 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_LN_TRAVEL_TIMES, 0 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_POWER_TRAVEL_TIMES, 0 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_TRAVEL_COSTS, 0 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_LN_TRAVEL_COSTS, 0 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_POWER_TRAVEL_COSTS, 0 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_TRAVEL_DISTANCE, 0 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_LN_TRAVEL_DISTANCE, 0 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_POWER_TRAVEL_DISTANCE, 0 + "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.CUSTOM_PARAMETER, "");
		scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MEASUREMENT_LOGFILE, Constants.MATSIM_4_OPUS_TEMP + Constants.MEASUREMENT_LOGFILE);
		
		// setting opus paths internally
		Constants.OPUS_HOME = scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.OPUS_HOME_PARAM);
		Constants.OPUS_DATA_PATH = scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.OPUS_DATA_PATH_PARAM);
		Constants.MATSIM_4_OPUS = scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_PARAM);
		Constants.MATSIM_4_OPUS_CONFIG = scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_CONFIG_PARAM);
		Constants.MATSIM_4_OPUS_OUTPUT = scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_OUTPUT_PARAM);
		Constants.MATSIM_4_OPUS_TEMP = scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_TEMP_PARAM);
		Constants.MATSIM_4_OPUS_BACKUP = scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_4_OPUS_BACKUP_PARAM);
		
		log.info("MATSim4UrbanSim settings:");
		log.info("SamplingRate: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.SAMPLING_RATE) );
		log.info("Year: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.YEAR) ); 
		log.info("OPUS_HOME: " + Constants.OPUS_HOME );
		log.info("OPUS_DATA_PATH: " + Constants.OPUS_DATA_PATH );
		log.info("MATSIM_4_OPUS: " + Constants.MATSIM_4_OPUS );
		log.info("MATSIM_4_OPUS_CONIG: " + Constants.MATSIM_4_OPUS_CONFIG );
		log.info("MATSIM_4_OPUS_OUTPUT: " + Constants.MATSIM_4_OPUS_OUTPUT );
		log.info("MATSIM_4_OPUS_TEMP: " + Constants.MATSIM_4_OPUS_TEMP ); 
		log.info("MATSIM_4_OPUS_BACKUP: " + Constants.MATSIM_4_OPUS_BACKUP );
		log.info("TestRun: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.IS_TEST_RUN) );
		log.info("BACKUP_RUN_DATA: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BACKUP_RUN_DATA_PARAM) );
		log.info("TEST_PARAMETER: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.TEST_PARAMETER_PARAM) );
		log.info("Beta: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA) );
		log.info("Beta Travel Times: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_TRAVEL_TIMES) );
		log.info("Beta ln(Travel Times): " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_LN_TRAVEL_TIMES) );
		log.info("Beta power(Travel Times): " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_POWER_TRAVEL_TIMES) );
		log.info("Beta Travel Costs: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_TRAVEL_COSTS) );
		log.info("Beta ln(Travel Costs): " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_LN_TRAVEL_COSTS) );
		log.info("Beta power(Travel Costs): " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_POWER_TRAVEL_COSTS) );
		log.info("Beta Travel Distance: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_TRAVEL_DISTANCE) );
		log.info("Beta ln(Travel Distance): " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_LN_TRAVEL_DISTANCE) );
		log.info("Beta power(Travel Distance): " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.BETA_POWER_TRAVEL_DISTANCE) );
		log.info("Custom Parameter: " + scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.CUSTOM_PARAMETER) );
		log.info("... done!");
	}
	
	/**
	 * setting MATSim network
	 * 
	 * @param matsimParameter
	 */
	private void initNetwork(ConfigType matsimParameter){
		log.info("Setting NetworkConfigGroup to config...");
		String networkFile = matsimParameter.getNetwork().getInputFile();
		NetworkConfigGroup networkCG = (NetworkConfigGroup) scenario.getConfig().getModule(NetworkConfigGroup.GROUP_NAME);
		// set network
		networkCG.setInputFile( networkFile );
		
		log.info("NetworkConfigGroup settings:");
		log.info("Network: " + networkCG.getInputFile());
		log.info("... done!");
	}
	
	/**
	 * setting input plans file (for warm/hot start)
	 * 
	 * @param matsimParameter
	 */
	private void initInputPlansFile(ConfigType matsimParameter){
		log.info("Looking for warm or hot start...");
		// get plans file for hot start
		String hotStart = matsimParameter.getHotStartPlansFile().getInputFile();
		// get plans file for warm start 
		String warmStart = matsimParameter.getInputPlansFile().getInputFile();
		
		// setting plans file as input
		if( !hotStart.equals("") &&
		  (new File(hotStart)).exists() ){
			log.info("Hot Start detcted!");
			setPlansFile( hotStart );
			scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_MODE, Constants.HOT_START);
		}
		else if( !warmStart.equals("") ){
			log.info("Warm Start detcted!");
			setPlansFile( warmStart );
			scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_MODE, Constants.WARM_START);
		}
		else{
			log.info("Cold Start (no plans file) detected!");
			scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_MODE, Constants.COLD_START);
		}
		
		// setting target location for hot start plans file
		if(!hotStart.equals("")){
			log.info("Storing plans file from current run. This enables hot start for next MATSim run.");
			scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.TARGET_LOCATION_HOT_START_PLANS_FILE, hotStart);
		}
		else
			scenario.getConfig().setParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.TARGET_LOCATION_HOT_START_PLANS_FILE, "");
	}

	/**
	 * sets (either a "warm" or "hot" start) a plans file, see above.
	 */
	private void setPlansFile(String plansFile) {
		log.info("Setting PlansConfigGroup to config...");
		PlansConfigGroup plansCG = (PlansConfigGroup) scenario.getConfig().getModule(PlansConfigGroup.GROUP_NAME);
		// set input plans file
		plansCG.setInputFile( plansFile );
		
		log.info("PlansConfigGroup setting:");
		log.info("Input plans file set to: " + plansCG.getInputFile());
		log.info("... done!");
	}
	
	/**
	 * setting controler parameter
	 * 
	 * @param matsimParameter
	 */
	private void initControler(ConfigType matsimParameter){
		log.info("Setting ControlerConfigGroup to config...");
		int firstIteration = matsimParameter.getControler().getFirstIteration().intValue();
		int lastIteration = matsimParameter.getControler().getLastIteration().intValue();
		ControlerConfigGroup controlerCG = (ControlerConfigGroup) scenario.getConfig().getModule(ControlerConfigGroup.GROUP_NAME);
		// set values
		controlerCG.setFirstIteration( firstIteration );
		controlerCG.setLastIteration( lastIteration);
		controlerCG.setOutputDirectory( Constants.MATSIM_4_OPUS_OUTPUT );
		
		HashSet<String> hs = new HashSet<String>();
		hs.add("otfvis");
		controlerCG.setSnapshotFormat(Collections.unmodifiableSet(hs));
		
		log.info("ControlerConfigGroup settings:");
		log.info("FirstIteration: " + controlerCG.getFirstIteration());
		log.info("LastIteration: " + controlerCG.getLastIteration());
		log.info("MATSim output directory: " +  controlerCG.getOutputDirectory());
		log.info("... done!");
	}
	
	/**
	 * setting planCalcScore parameter
	 * 
	 * @param matsimParameter
	 */
	private void initPlanCalcScore(ConfigType matsimParameter){
		log.info("Setting PlanCalcScore to config...");
		String activityType_0 = matsimParameter.getPlanCalcScore().getActivityType0();
		String activityType_1 = matsimParameter.getPlanCalcScore().getActivityType1();
		ActivityParams actType0 = new ActivityParams(activityType_0);
		actType0.setTypicalDuration(12*60*60);	// tnicolai: make configurable
		ActivityParams actType1 = new ActivityParams(activityType_1);
		actType1.setTypicalDuration(8*60*60);	// tnicolai: make configurable
		actType1.setOpeningTime(7*3600);		// tnicolai: make configurable
		actType1.setLatestStartTime(9*3600);	// tnicolai: make configurable
		scenario.getConfig().planCalcScore().addActivityParams( actType0 );
		scenario.getConfig().planCalcScore().addActivityParams( actType1 );
		
		log.info("PlanCalcScore settings:");
		log.info("Activity_Type_0: " + actType0.getType() + " Typical Duration Activity_Type_0: " + actType0.getTypicalDuration());
		log.info("Activity_Type_1: " + actType1.getType() + " Typical Duration Activity_Type_1: " + actType1.getTypicalDuration());
		log.info("Opening Time Activity_Type_1: " + actType1.getOpeningTime()); 
		log.info("Latest Start Time Activity_Type_1: " + actType1.getLatestStartTime());
		log.info("... done!");
	}
	
	/**
	 * setting simulation
	 */
	private void initSimulation(){
		log.info("Setting SimulationConfigGroup to config...");
		
		SimulationConfigGroup simulation = new SimulationConfigGroup();
		
		double popSampling = this.matsimConfig.getMatsim4Urbansim().getUrbansimParameter().getSamplingRate();
		
		log.warn("FlowCapFactor and StorageCapFactor are adapted to the population sampling rate (sampling rate = " + popSampling + ").");
		
		// setting FlowCapFactor == population sampling rate (no correction factor needed here)
		simulation.setFlowCapFactor( popSampling );	
		
		// Adapting the storageCapFactor has the following reason:
		// Too low SorageCapacities especially with small sampling 
		// rates can (eg 1%) lead to strong backlogs on the traffic network. 
		// This leads to an unstable behavior of the simulation (by breakdowns 
		// during the learning progress).
		// The correction fetch factor introduced here raises the 
		// storage capacity at low sampling rates and becomes flatten 
		// with increasing sampling rates (at a 100% sample, the 
		// storage capacity == 1).			tnicolai nov'11
		if(popSampling <= 0.){
			popSampling = 0.01;
			log.warn("Raised popSampling rate to " + popSampling + " to to avoid erros while calulating the correction fetch factor ...");
		}
		// tnicolai dec'11
		double fetchFactor = Math.pow(popSampling, -0.25);	// same as: / Math.sqrt(Math.sqrt(sample))
		double storageCap = popSampling * fetchFactor;
		
		// setting StorageCapFactor
		simulation.setStorageCapFactor( storageCap );	
		
		boolean removeStuckVehicles = false;
		simulation.setRemoveStuckVehicles( removeStuckVehicles );
		simulation.setStuckTime(10.);
		
		scenario.getConfig().addSimulationConfigGroup( simulation );
		
		log.info("SimulationConfigGroup settings:");
		log.info("FlowCapFactor (= population sampling rate): "+ scenario.getConfig().simulation().getFlowCapFactor());
		log.warn("StorageCapFactor: " + scenario.getConfig().simulation().getStorageCapFactor() + " (with fetch factor = " + fetchFactor + ")" );
		log.info("RemoveStuckVehicles: " + (removeStuckVehicles?"True":"False") );
		log.info("StuckTime: " + scenario.getConfig().simulation().getStuckTime());
		log.info("... done!");
	}
	
	/**
	 * setting strategy
	 */
	private void initStrategy(){
		log.info("Setting StrategyConfigGroup to config...");
		
		// some modules are disables after 80% of overall iterations, 
		// last iteration for them determined here tnicolai feb'12
		int disableStrategyAfterIteration = (int) Math.ceil(scenario.getConfig().controler().getLastIteration() * 0.8);
		
		// configure strategies for re-planning tnicolai: make configurable
		scenario.getConfig().strategy().setMaxAgentPlanMemorySize(5);
		
		StrategyConfigGroup.StrategySettings timeAlocationMutator = new StrategyConfigGroup.StrategySettings(IdFactory.get(1));
		timeAlocationMutator.setModuleName("TimeAllocationMutator");
		timeAlocationMutator.setProbability(0.1);
		timeAlocationMutator.setDisableAfter(disableStrategyAfterIteration);
		scenario.getConfig().strategy().addStrategySettings(timeAlocationMutator);
		// change mutation range to 2h. tnicolai feb'12
		scenario.getConfig().setParam("TimeAllocationMutator", "mutationRange", "7200"); 
		
		StrategyConfigGroup.StrategySettings changeExpBeta = new StrategyConfigGroup.StrategySettings(IdFactory.get(2));
		changeExpBeta.setModuleName("ChangeExpBeta");
		changeExpBeta.setProbability(0.9);
		scenario.getConfig().strategy().addStrategySettings(changeExpBeta);
		
		StrategyConfigGroup.StrategySettings reroute = new StrategyConfigGroup.StrategySettings(IdFactory.get(3));
		reroute.setModuleName("ReRoute_Dijkstra");
		reroute.setProbability(0.1);
		reroute.setDisableAfter(disableStrategyAfterIteration);
		scenario.getConfig().strategy().addStrategySettings(reroute);
		
		log.info("StrategyConfigGroup settings:");
		log.info("Strategy_1: " + timeAlocationMutator.getModuleName() + " Probability: " + timeAlocationMutator.getProbability() + " Disable After Itereation: " + timeAlocationMutator.getDisableAfter()); 
		log.info("Strategy_2: " + changeExpBeta.getModuleName() + " Probability: " + changeExpBeta.getProbability());
		log.info("Strategy_3_ " + reroute.getModuleName() + " Probability: " + reroute.getProbability() + " Disable After Itereation: " + reroute.getDisableAfter() );
		log.info("... done!");
	}
	
	// Testing fetch  factor calculation for storageCap 
	public static void main(String[] args) {
		// testing calculation of storage capacity fetch factor
		for(double sample = 0.01; sample <=1.; sample += 0.01){
			
			double factor = Math.pow(sample, -0.25); // same as: 1. / Math.sqrt(Math.sqrt(sample))
			double storageCap = sample * factor;
			
			System.out.println("Sample rate " + sample + " leads to a fetch fector of: " + factor + " and a StroraceCapacity of: " + storageCap );
		}
	}
}

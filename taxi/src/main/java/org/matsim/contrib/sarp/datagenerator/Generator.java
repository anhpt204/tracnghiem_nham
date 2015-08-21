package org.matsim.contrib.sarp.datagenerator;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.facilities.FacilitiesReaderMatsimV1;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;

public class Generator
{
	private Scenario scenario;
	
	private String networkFile = "./input/grid/network.xml";
	//private String plansFile = "./input/test/template_plans.xml";


	public Generator()
	{
		Config config = ConfigUtils.createConfig();
		this.scenario = ScenarioUtils.createScenario(config);
		/*
		 * Read the network and store it in the scenario
		 */
		(new MatsimNetworkReader(this.scenario)).readFile(networkFile);
		
		//new MatsimPopulationReader(scenario).readFile(plansFile);
	}
	
	private void write() {
		PopulationWriter populationWriter = new PopulationWriter(this.scenario.getPopulation(), this.scenario.getNetwork());
		populationWriter.write("./input/grid/plans.50.xml");
		//log.info("Number of persons: " + this.scenario.getPopulation().getPersons().size());
	}
	
	private void run() 
	{
		GeneratePopulation generatePopulation = new GeneratePopulation(this.scenario);
		generatePopulation.generatePopulation();
		
		this.write();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Generator generator = new Generator();
		generator.run();
	}

}

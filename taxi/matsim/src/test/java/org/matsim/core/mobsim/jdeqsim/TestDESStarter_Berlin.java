package org.matsim.core.mobsim.jdeqsim;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.scenario.ScenarioLoaderImpl;


public class TestDESStarter_Berlin extends AbstractJDEQSimTest {

	public void test_Berlin_TestHandlerDetailedEventChecker() {
		Scenario scenario = ScenarioLoaderImpl.createScenarioLoaderImplAndResetRandomSeed("test/scenarios/berlin/config.xml").loadScenario();
		this.runJDEQSim(scenario);
		
		assertEquals(scenario.getPopulation().getPersons().size(), super.eventsByPerson.size());
		super.checkAscendingTimeStamps();
		super.checkEventsCorrespondToPlans(scenario.getPopulation());
	}

}

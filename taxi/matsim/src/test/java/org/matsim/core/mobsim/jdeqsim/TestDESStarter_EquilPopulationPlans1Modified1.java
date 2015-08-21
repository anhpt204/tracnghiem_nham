package org.matsim.core.mobsim.jdeqsim;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.mobsim.jdeqsim.scenarios.EquilPopulationPlans1Modified1;
import org.matsim.core.scenario.ScenarioLoaderImpl;

public class TestDESStarter_EquilPopulationPlans1Modified1 extends AbstractJDEQSimTest {

	public void test_EquilPopulationPlans1Modified1_TestHandlerDetailedEventChecker() {
		ScenarioLoaderImpl sl = ScenarioLoaderImpl.createScenarioLoaderImplAndResetRandomSeed("test/scenarios/equil/config.xml");
		sl.getScenario().getConfig().plans().setInputFile("test/scenarios/equil/plans1.xml");
		Scenario scenario = sl.loadScenario();
		new EquilPopulationPlans1Modified1().modifyPopulation(scenario.getPopulation());
		this.runJDEQSim(scenario);
		
		assertEquals(scenario.getPopulation().getPersons().size(), super.eventsByPerson.size());
		super.checkAscendingTimeStamps();
		super.checkEventsCorrespondToPlans(scenario.getPopulation());
		super.compareToDEQSimTravelTimes(getPackageInputDirectory() + "deq_events.txt", 1.0);
	}

}

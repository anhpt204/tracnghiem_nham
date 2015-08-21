package tutorial.unsupported.mobsimPassingVehicleQ;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.qsim.ActivityEngine;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimFactory;
import org.matsim.core.mobsim.qsim.TeleportationEngine;
import org.matsim.core.mobsim.qsim.agents.AgentFactory;
import org.matsim.core.mobsim.qsim.agents.DefaultAgentFactory;
import org.matsim.core.mobsim.qsim.agents.PopulationAgentSource;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Example to show how the standard queue can be replaced by something else.  Search for PassingVehicleQ in the code below.
 * <p/>
 * I have a version of this that was running about a year ago in my playground.  This "tutorial" version has never been tested (but please
 * feel free to test, report back, and fix).
 * 
 * @author nagel
 *
 */
class MyControler {

	public static void main ( String[] args ) {

		// prepare the config:
		@SuppressWarnings("unchecked")
		Config config = ConfigUtils.loadConfig( args[0] ) ;
        config.qsim().setLinkDynamics(QSimConfigGroup.LinkDynamics.PassingQ.toString());
		config.vspExperimental().setRemovingUnneccessaryPlanAttributes(true) ;

		// prepare the scenario
		Scenario scenario = ScenarioUtils.loadScenario( config ) ;

		// prepare the control(l)er:
		Controler controler = new Controler( scenario ) ;
		controler.setMobsimFactory(new PatnaMobsimFactory()) ;

		// run everything:
		controler.run();

	}

	/**
	 * Look into {@link QSimFactory} for the default matsim qsim factory.  This is copy and paste (and somewhat reduced).
	 * 
	 * @author nagel
	 *
	 */
	static class PatnaMobsimFactory implements MobsimFactory {

		@Override
		public Mobsim createMobsim(Scenario sc, EventsManager eventsManager) {

			QSimConfigGroup conf = sc.getConfig().qsim();
			if (conf == null) {
				throw new NullPointerException("There is no configuration set for the QSim. Please add the module 'qsim' to your config file.");
			}

			// construct the QSim:
			QSim qSim = new QSim(sc, eventsManager);

			// add the activity engine:
			ActivityEngine activityEngine = new ActivityEngine();
			qSim.addMobsimEngine(activityEngine);
			qSim.addActivityHandler(activityEngine);

			// add the netsim engine:
			QNetsimEngine netsimEngine = new QNetsimEngine(qSim) ;
			qSim.addMobsimEngine(netsimEngine);
			qSim.addDepartureHandler(netsimEngine.getDepartureHandler());

			TeleportationEngine teleportationEngine = new TeleportationEngine();
			qSim.addMobsimEngine(teleportationEngine);

			AgentFactory agentFactory = new DefaultAgentFactory(qSim);

			PopulationAgentSource agentSource = new PopulationAgentSource(sc.getPopulation(), agentFactory, qSim);
			Map<String, VehicleType> modeVehicleTypes = new HashMap<>();

			VehicleType car = VehicleUtils.getFactory().createVehicleType(Id.create("car", VehicleType.class));
			car.setMaximumVelocity(60.0/3.6);
			car.setPcuEquivalents(1.0);
			modeVehicleTypes.put("car", car);

			VehicleType bike = VehicleUtils.getFactory().createVehicleType(Id.create("bike", VehicleType.class));
			bike.setMaximumVelocity(60.0/3.6);
			bike.setPcuEquivalents(0.25);
			modeVehicleTypes.put("bike", bike);

			VehicleType bicycles = VehicleUtils.getFactory().createVehicleType(Id.create("bicycle", VehicleType.class));
			bicycles.setMaximumVelocity(15.0/3.6);
			bicycles.setPcuEquivalents(0.05);
			modeVehicleTypes.put("bicycle", bicycles);

			VehicleType walks = VehicleUtils.getFactory().createVehicleType(Id.create("walk", VehicleType.class));
			walks.setMaximumVelocity(1.5);
			walks.setPcuEquivalents(0.10);  			// assumed pcu for walks is 0.1
			modeVehicleTypes.put("walk", walks);

			agentSource.setModeVehicleTypes(modeVehicleTypes);

			qSim.addAgentSource(agentSource);

			return qSim ;
		}
	}
}

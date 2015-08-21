package org.matsim.contrib.parking.PC2;

import java.util.Map;

import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.parking.PC2.scoring.ParkingBetas;
import org.matsim.contrib.parking.PC2.scoring.ParkingCostModel;
import org.matsim.contrib.parking.PC2.scoring.ParkingScoreManager;
import org.matsim.contrib.parking.PC2.scoring.ParkingScoringFunctionFactory;
import org.matsim.contrib.parking.PC2.simulation.ParkingChoiceSimulation;
import org.matsim.contrib.parking.PC2.simulation.ParkingInfrastructureManager;
import org.matsim.contrib.parking.lib.DebugLib;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.population.PersonImpl;

public class GeneralParkingModule implements StartupListener, IterationStartsListener,BeforeMobsimListener, IterationEndsListener {

	private Controler controler;
	private ParkingCostModel parkingCostModel; // TODO: don't overwrite parking cost model from config, if already set.
	private ParkingScoreManager parkingScoreManager;
	public ParkingScoreManager getParkingScoreManager() {
		return parkingScoreManager;
	}

	public void setParkingScoreManager(ParkingScoreManager parkingScoreManager) {
		this.parkingScoreManager = parkingScoreManager;
	}

	protected ParkingInfrastructureManager parkingInfrastructureManager;
	private ParkingChoiceSimulation parkingSimulation;

	public GeneralParkingModule(Controler controler){
		this.setControler(controler);
		
		controler.addControlerListener(this);
	}
	
	public void setParkingCostModel(ParkingCostModel parkingCostModel){
		this.parkingCostModel= parkingCostModel;
	}
	
	@Override
	public void notifyStartup(StartupEvent event) {
		parkingSimulation = new ParkingChoiceSimulation(controler, parkingInfrastructureManager);
		controler.getEvents().addHandler(parkingSimulation);
		controler.addControlerListener(parkingSimulation);
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		
	}
	
	public ParkingInfrastructureManager getParkingInfrastructure() {
		return parkingInfrastructureManager;
	}
	
	public void setParkingInfrastructurManager(ParkingInfrastructureManager parkingInfrastructureManager) {
		this.parkingInfrastructureManager = parkingInfrastructureManager;
	}

	public Controler getControler() {
		return controler;
	}

	public void setControler(Controler controler) {
		this.controler = controler;
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		parkingScoreManager.prepareForNewIteration();
		parkingInfrastructureManager.reset();
		parkingSimulation.prepareForNewIteration();
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		
	}
}

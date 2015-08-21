package org.matsim.contrib.freight.carrier;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Id;


public class CarrierImpl implements Carrier {
	
	private Id id;
	
	private CarrierPlan selectedPlan;
	
	private Collection<CarrierPlan> plans = new ArrayList<CarrierPlan>();
	
	private Id depotLinkId;
	
	private Collection<CarrierContract> contracts = new ArrayList<CarrierContract>();

	private CarrierCapabilities carrierCapabilities;

	private Collection<CarrierContract> expiredContracts = new ArrayList<CarrierContract>();

	private Collection<CarrierContract> newContracts = new ArrayList<CarrierContract>();

	public CarrierImpl(Id id, Id depotLinkId) {
		super();
		this.id = id;
		this.depotLinkId = depotLinkId;
	}

	/* (non-Javadoc)
	 * @see playground.mzilske.freight.Carrier#getId()
	 */
	@Override
	public Id getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see playground.mzilske.freight.Carrier#getDepotLinkId()
	 */
	@Override
	public Id getDepotLinkId() {
		return depotLinkId;
	}

	/* (non-Javadoc)
	 * @see playground.mzilske.freight.Carrier#getPlans()
	 */
	@Override
	public Collection<CarrierPlan> getPlans() {
		return plans;
	}

	/* (non-Javadoc)
	 * @see playground.mzilske.freight.Carrier#getContracts()
	 */
	@Override
	public Collection<CarrierContract> getContracts() {
		return contracts;
	}

	/* (non-Javadoc)
	 * @see playground.mzilske.freight.Carrier#getSelectedPlan()
	 */
	@Override
	public CarrierPlan getSelectedPlan() {
		return selectedPlan;
	}

	/* (non-Javadoc)
	 * @see playground.mzilske.freight.Carrier#setSelectedPlan(playground.mzilske.freight.CarrierPlan)
	 */
	@Override
	public void setSelectedPlan(CarrierPlan selectedPlan) {
		this.selectedPlan = selectedPlan;
	}

	/* (non-Javadoc)
	 * @see playground.mzilske.freight.Carrier#setCarrierCapabilities(playground.mzilske.freight.CarrierCapabilities)
	 */
	@Override
	public void setCarrierCapabilities(CarrierCapabilities carrierCapabilities) {
		this.carrierCapabilities = carrierCapabilities;
	}

	/* (non-Javadoc)
	 * @see playground.mzilske.freight.Carrier#getCarrierCapabilities()
	 */
	@Override
	public CarrierCapabilities getCarrierCapabilities() {
		return carrierCapabilities;
	}

	@Override
	public Collection<CarrierContract> getNewContracts() {
		return newContracts;
	}

	@Override
	public Collection<CarrierContract> getExpiredContracts() {
		return expiredContracts;
	}
	
}

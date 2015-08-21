package org.matsim.contrib.sarp.optimizer;

import java.util.ArrayList;

import org.matsim.contrib.dvrp.optimizer.VrpOptimizerWithOnlineTracking;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

public interface TaxiOptimizer
	extends VrpOptimizerWithOnlineTracking, MobsimBeforeSimStepListener
{
	public int getNumberOfRejectedPeopleRequests();
	public int getNumberOfUnServedParcelRequests();
	
}

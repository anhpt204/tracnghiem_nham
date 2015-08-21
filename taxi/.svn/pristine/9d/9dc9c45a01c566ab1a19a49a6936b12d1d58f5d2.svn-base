package org.matsim.contrib.sarp.optimizer;

import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.sarp.LauncherParams;
import org.matsim.contrib.sarp.enums.AlgorithmConfig;
import org.matsim.contrib.sarp.scheduler.TaxiScheduler;
import org.matsim.contrib.sarp.vehreqpath.VehicleRequestPathFinder;

public class TaxiOptimizerConfiguration 
{
	public static enum Goal
    {
        MIN_WAIT_TIME, MIN_PICKUP_TIME, DEMAND_SUPPLY_EQUIL, NULL
    };
    
    public final MatsimVrpContext context;
    public final TaxiScheduler scheduler;
    public final Goal goal;
    public final LauncherParams params;
    public final VehicleRequestPathFinder vrpFinder;
    
    public TaxiOptimizerConfiguration(MatsimVrpContext context, TaxiScheduler scheduler,
    		Goal goal, LauncherParams params,
    		VehicleRequestPathFinder vrpFinder)
    {
    	this.context = context;
    	this.scheduler = scheduler;
    	this.goal = goal;
    	this.params = params;
    	this.vrpFinder = vrpFinder;
    }
    
}

package org.matsim.contrib.sarp.enums;

import static org.matsim.contrib.dvrp.run.VrpLauncherUtils.TravelDisutilitySource.DISTANCE;
import static org.matsim.contrib.dvrp.run.VrpLauncherUtils.TravelDisutilitySource.TIME;
import static org.matsim.contrib.dvrp.run.VrpLauncherUtils.TravelTimeSource.EVENTS;
import static org.matsim.contrib.dvrp.run.VrpLauncherUtils.TravelTimeSource.FREE_FLOW_SPEED;

import org.matsim.contrib.dvrp.run.VrpLauncherUtils.TravelDisutilitySource;
import org.matsim.contrib.dvrp.run.VrpLauncherUtils.TravelTimeSource;
import org.matsim.contrib.sarp.optimizer.Optimizer1;
import org.matsim.contrib.sarp.optimizer.TaxiOptimizer;
import org.matsim.contrib.sarp.optimizer.TaxiOptimizerConfiguration;
import org.matsim.contrib.sarp.optimizer.TaxiOptimizerConfiguration.Goal;


public enum AlgorithmConfig
{	
    NO(AlgorithmType.SIMPLE, Goal.MIN_WAIT_TIME, FREE_FLOW_SPEED, DISTANCE),

	MIP_TW_FF(AlgorithmType.MIP, Goal.MIN_WAIT_TIME, FREE_FLOW_SPEED, TIME);
	 
	static enum AlgorithmType
    {
		SIMPLE,
		MIP
    }
    
	public final TravelTimeSource ttimeSource;
    public final Goal goal;
    public final TravelDisutilitySource tdisSource;
    public final AlgorithmType algorithmType;


    AlgorithmConfig(AlgorithmType algorithmType, Goal goal, TravelTimeSource ttimeSource,
            TravelDisutilitySource tdisSource)
    {
        this.ttimeSource = ttimeSource;
        this.goal = goal;
        this.tdisSource = tdisSource;
        this.algorithmType = algorithmType;
    }
    
    
    public TaxiOptimizer createTaxiOptimizer(TaxiOptimizerConfiguration optimizerConfig)
    {
    	switch(algorithmType)
    	{
    	case SIMPLE:
    		return new Optimizer1(optimizerConfig);
    	default:
    		return null;
    	}
    }


}

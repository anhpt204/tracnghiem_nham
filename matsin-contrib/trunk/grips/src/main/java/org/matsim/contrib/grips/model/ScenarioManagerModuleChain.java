package org.matsim.contrib.grips.model;

import java.util.ArrayList;

import org.matsim.contrib.grips.model.Constants.ModuleType;

/**
 * module chain implementation for the
 * basic scenario manager
 * 
 * @author wdoering
 *
 */
@SuppressWarnings("serial")
public class ScenarioManagerModuleChain extends ModuleChain {

	public ScenarioManagerModuleChain()
	{
		super();
		
		nextModules.put(ModuleType.SCENARIOXML, new ArrayList<ModuleType>(){{ add(ModuleType.EVACUATION); }});
		
		nextModules.put(ModuleType.EVACUATION, new ArrayList<ModuleType>(){{ add(ModuleType.POPULATION); }});
		nextModules.put(ModuleType.POPULATION, new ArrayList<ModuleType>(){{ add(ModuleType.GRIPSSCENARIO); }});
		nextModules.put(ModuleType.GRIPSSCENARIO, new ArrayList<ModuleType>(){{ add(ModuleType.ROADCLOSURE); add(ModuleType.BUSSTOPS); add(ModuleType.MATSIMSCENARIO); }});
		nextModules.put(ModuleType.MATSIMSCENARIO, new ArrayList<ModuleType>(){{ add(ModuleType.ANALYSIS); }});
		
		pastModules.put(ModuleType.EVACUATION, new ArrayList<ModuleType>(){{ add(ModuleType.SCENARIOXML);  }});
		pastModules.put(ModuleType.POPULATION, new ArrayList<ModuleType>(){{ add(ModuleType.SCENARIOXML); add(ModuleType.EVACUATION);}});
		pastModules.put(ModuleType.GRIPSSCENARIO, new ArrayList<ModuleType>(){{ add(ModuleType.EVACUATION); add(ModuleType.POPULATION); add(ModuleType.SCENARIOXML);}});
		pastModules.put(ModuleType.MATSIMSCENARIO, new ArrayList<ModuleType>(){{ add(ModuleType.ROADCLOSURE); add(ModuleType.BUSSTOPS); add(ModuleType.GRIPSSCENARIO);}});
		pastModules.put(ModuleType.ROADCLOSURE, new ArrayList<ModuleType>(){{ add(ModuleType.GRIPSSCENARIO);}});
		pastModules.put(ModuleType.BUSSTOPS, new ArrayList<ModuleType>(){{ add(ModuleType.GRIPSSCENARIO);}});
		
	}
	
}

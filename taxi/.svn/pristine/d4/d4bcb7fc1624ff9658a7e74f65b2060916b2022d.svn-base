package org.matsim.contrib.sarp.schedule;

import org.matsim.contrib.sarp.data.AbstractRequest;

public interface TaxiTaskWithRequest extends TaxiTask 
{
	AbstractRequest getRequest();
	
    //called (when removing a task) in order to update the request-2-task assignment 
	void removeFromRequest();

}

/**
 * taxi
 * org.matsim.contrib.sarp.vehreqpath
 * tuananh
 * Dec 2, 2014
 */
package org.matsim.contrib.sarp.route;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.contrib.sarp.schedule.TaxiTask.TaxiTaskType;

/**
 * A Node on the path
 *
 */
public class PathNode
{
	public Link link;
	public AbstractRequest request;
	public PathNodeType type;
	public double departureTime;
	
	public enum PathNodeType
	{
		START,
		PICKUP,
		DROPOFF,
	}
	
	public PathNode(Link link, AbstractRequest request, 
			PathNodeType type, double departureTime)
	{
		this.link = link;
		this.request = request;
		this.type = type;
		this.departureTime = departureTime;
	}

}

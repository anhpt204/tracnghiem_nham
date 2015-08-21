/**
 * taxi
 * org.matsim.contrib.sarp.util
 * tuananh
 * Nov 28, 2014
 */
package org.matsim.contrib.sarp.util;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.sarp.data.AbstractRequest;

/**
 * @author tuananh
 *
 */
public  class RequestEntry
{
	private Id<Person> personId;
	private AbstractRequest request;
	
	public RequestEntry(Id<Person> id, AbstractRequest request)
	{
		super();
		this.personId = id;
		this.request = request;
	}

	public void setPersonId(Id<Person> personId)
	{
		this.personId = personId;
	}

	public void setRequest(AbstractRequest request)
	{
		this.request = request;
	}

	public Id<Person> getPersonId()
	{
		return personId;
	}

	public AbstractRequest getRequest()
	{
		return request;
	}
	
	public double getSubmissionTime()
	{
		return request.getSubmissionTime();
	}
	
}

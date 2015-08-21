/**
 * taxi
 * org.matsim.contrib.sarp.util
 * tuananh
 * Nov 27, 2014
 */
package org.matsim.contrib.sarp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.sarp.RequestCreator;
import org.matsim.core.utils.misc.Time;

/**
 * @author tuananh
 *
 */
public class VrpUtilities
{
	
	public static Queue<RequestEntry> getRequestEntry(String taxiCustomersFile, MatsimVrpContext context) throws FileNotFoundException, IOException
	{
		if(taxiCustomersFile != null)
		{
			try (BufferedReader br = new BufferedReader(new FileReader(new File(taxiCustomersFile)))) 
			{
	            List<String> taxiCustomerIds = new ArrayList<>();
	
	            String line;
	            while ( (line = br.readLine()) != null) {
	                taxiCustomerIds.add(line);
	            }
	            
	    		Queue<RequestEntry> sortedSubmissionTimeQueue 
	    		= new PriorityBlockingQueue<>(taxiCustomerIds.size(), new Comparator<RequestEntry>()
	    		{
	
	    			@Override
	    			public int compare(RequestEntry o1, RequestEntry o2)
	    			{
	    				int cmp = Double.compare(o1.submissionTime, o2.submissionTime);
	    				if(cmp == 0)
	    				{
	    					return o1.person.getId().compareTo(o2.person.getId());
	    				}
	    				return cmp;
	    			}
	    		});
	            
	    		
	    		Map<Id<Person>, ? extends Person> persons = context.getScenario().getPopulation().getPersons();
	
	    		for(String id : taxiCustomerIds)
	    		{
	    			Person person = persons.get(Id.create(id, Person.class));
	    			for (PlanElement pe : person.getSelectedPlan().getPlanElements()) {
	                    if (pe instanceof Activity) 
	                    {
	                        double endtime = ((Activity)pe).getEndTime();
	                        if(endtime != Time.UNDEFINED_TIME)
	                        {
	                        	
	                        	//create randomly submission time
	                        	double start = endtime - 15 * 60;
	                        	double t = (new Random()).nextInt(10*60);
	                        	
	                        	double submissionTime = start + t;
	                        	
	                        	sortedSubmissionTimeQueue.add(new RequestEntry(person, submissionTime));
	                        	
	                        	break;
	                        }
	                    }
	                }
	    		}
	    		
	    		return sortedSubmissionTimeQueue;
	        }

        }
		else
		{
			
			//set all person to be taxi mode
			Map<Id<Person>, ? extends Person> persons = context.getScenario().getPopulation().getPersons();
			
			Queue<RequestEntry> sortedSubmissionTimeQueue 
    		= new PriorityBlockingQueue<>(persons.size(), new Comparator<RequestEntry>()
    		{

    			@Override
    			public int compare(RequestEntry o1, RequestEntry o2)
    			{
    				int cmp = Double.compare(o1.submissionTime, o2.submissionTime);
    				if(cmp == 0)
    				{
    					return o1.person.getId().compareTo(o2.person.getId());
    				}
    				return cmp;
    			}
    		});
            
			
			for(Person p : persons.values())
			{
				for(PlanElement pe : p.getSelectedPlan().getPlanElements())
				{
					if (pe instanceof Leg)
					{
						((Leg)pe).setMode(RequestCreator.MODE);
						
						double departimeTime = ((Leg)pe).getDepartureTime();
                        if(departimeTime != Time.UNDEFINED_TIME)
                        {
                        	
                        	//create randomly submission time
                        	double start = departimeTime - 15 * 60;
                        	double t = (new Random()).nextInt(10*60);
                        	
                        	double submissionTime = start + t;
                        	
                        	sortedSubmissionTimeQueue.add(new RequestEntry(p, submissionTime));
                        	
                        	
                        }
						
						break;
					}
				}
			}
			
			return sortedSubmissionTimeQueue;
		}
        
		
	}
	
	
    public static List<String> readTaxiCustomerIds(String taxiCustomersFile)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(taxiCustomersFile)))) {
            List<String> taxiCustomerIds = new ArrayList<>();

            String line;
            while ( (line = br.readLine()) != null) {
                taxiCustomerIds.add(line);
            }

            return taxiCustomerIds;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

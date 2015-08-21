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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.sarp.enums.RequestType;
import org.matsim.contrib.sarp.RequestCreator;
import org.matsim.contrib.sarp.data.AbstractRequest;
import org.matsim.core.utils.misc.Time;

import com.google.common.collect.Multiset.Entry;

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
	            HashMap<String, String> taxiCustomerIds = new HashMap<>();
	
	            String line;
	            while ( (line = br.readLine()) != null) 
	            {
	            	String id = line.split("")[0];
	                taxiCustomerIds.put(id, line);
	            }
	            
	    		Queue<RequestEntry> sortedSubmissionTimeQueue 
	    		= new PriorityBlockingQueue<>(taxiCustomerIds.size(), new Comparator<RequestEntry>()
	    		{
	
	    			@Override
	    			public int compare(RequestEntry o1, RequestEntry o2)
	    			{
	    				int cmp = Double.compare(o1.getSubmissionTime(), o2.getSubmissionTime());
	    				if(cmp == 0)
	    				{
	    					return o1.getPersonId().compareTo(o2.getPersonId());
	    				}
	    				return cmp;
	    			}
	    		});
	            
	    		
	    		Map<Id<Person>, ? extends Person> persons = context.getScenario().getPopulation().getPersons();
	    		
	    		Iterator<Id<Person>> iter = persons.keySet().iterator();
	    		while (iter.hasNext())
	    		{
	    			Id<Person> id = iter.next();
	    			String[] vs = id.toString().split("");

	    			RequestType requestType = RequestType.PEOPLE;
	    			if(vs[0] == "Parcel")
	    				requestType = RequestType.PARCEL;

	    			String request = taxiCustomerIds.get(vs[1]);

	    			String[] xs = request.split("");
	    			double submissionTime = Double.parseDouble(xs[1]);
	    			int pickupLinkId = Integer.parseInt(xs[2]);
	    			int deliveryLinkId = Integer.parseInt(xs[3]);
	    			double earlyPickupTime = Double.parseDouble(xs[4]);
	    			double latePickupTime = Double.parseDouble(xs[5]);
	    			double earlyDeliveryTime = Double.parseDouble(xs[6]);
	    			double lateDeliveryTime = Double.parseDouble(xs[7]);
	    			double maxTravelDistance = Double.parseDouble(xs[8]);
	    			int maxNbStops = Integer.parseInt(xs[9]);

	    			AbstractRequest request = new AbstractRequest(id, passenger, earlyPickupTime, latePickupTime, earlyDeliveryTime, lateDeliveryTime, fromLink, toLink, submissionTime, maxTravelTime, maxNbStops, type)
	    			
//	    			for (PlanElement pe : person.getSelectedPlan().getPlanElements()) {
//	                    if (pe instanceof Activity) 
//	                    {
//	                        double endtime = ((Activity)pe).getEndTime();
//	                        if(endtime != Time.UNDEFINED_TIME)
//	                        {
//	                        	
//	                        	//create randomly submission time
//	                        	double start = endtime - 15 * 60;
//	                        	double t = (new Random()).nextInt(10*60);
//	                        	
//	                        	double submissionTime = start + t;
//	                        	
//	                        	sortedSubmissionTimeQueue.add(new RequestEntry(person, submissionTime));
//	                        	
//	                        	break;
//	                        }
//	                    }
//	                }
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

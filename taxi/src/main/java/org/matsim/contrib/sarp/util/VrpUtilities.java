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

import javax.swing.JComboBox.KeySelectionManager;

import org.apache.poi.hssf.record.formula.functions.Links;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
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
		
		try (BufferedReader br = new BufferedReader(new FileReader(new File(taxiCustomersFile)))) 
		{
            HashMap<String, String> taxiCustomerIds = new HashMap<>();
            // read the first line
            br.readLine();
            
            String line;
            while ( (line = br.readLine()) != null) 
            {
            	if (line == "-1")
            		continue;
            	
            	String id = line.split(" ")[0];
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
    			String[] vs = id.toString().split("-");

//	    			RequestType requestType = RequestType.PEOPLE;
//	    			if(vs[0] == "Parcel")
//	    				requestType = RequestType.PARCEL;

    			String request = taxiCustomerIds.get(vs[1]);

    			String[] xs = request.split(" ");
    			double submissionTime = Double.parseDouble(xs[1]);
    			int fromLinkId = Integer.parseInt(xs[2]);
    			int toLinkId = Integer.parseInt(xs[3]);
    			double earlyPickupTime = Double.parseDouble(xs[4]);
    			double latePickupTime = Double.parseDouble(xs[5]);
    			double earlyDeliveryTime = Double.parseDouble(xs[6]);
    			double lateDeliveryTime = Double.parseDouble(xs[7]);
    			double maxTravelDistance = Double.parseDouble(xs[8]);
    			int maxNbStops = Integer.parseInt(xs[9]);

    			sortedSubmissionTimeQueue.add(new RequestEntry(id, submissionTime, 
    					fromLinkId, toLinkId, earlyPickupTime, latePickupTime, 
    					earlyDeliveryTime, lateDeliveryTime, maxTravelDistance, maxNbStops));
//	    			
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
    
    public void generateTaxi(MatsimVrpContext context, int numVehicles)
    {
    	Random rand = new Random();
    	rand.setSeed(1000);
    	Map<Id<Link>, ? extends Link> links = context.getScenario().getNetwork().getLinks();
    	
    	Id<Link>[] keys = (Id<Link>[])links.keySet().toArray();
    	
    	int numLinks = links.size();
    	
    	for (int i = 0; i < numVehicles; i++)
    	{
    		//randomly get start location of vehicle
    		int l = rand.nextInt(numLinks);
    		Id<Link> linkId = keys[l];
    		
    		// chua xong
    	}
    }
}

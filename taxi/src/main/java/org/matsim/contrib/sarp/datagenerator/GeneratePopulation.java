package org.matsim.contrib.sarp.datagenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.sarp.RequestCreator;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.api.experimental.facilities.ActivityFacilitiesFactory;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.routes.GenericRoute;
import org.matsim.core.population.routes.GenericRouteFactory;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.core.utils.misc.Time;
import org.matsim.utils.objectattributes.ObjectAttributes;


public class GeneratePopulation
{
	public final int REQUEST_NUMBER = 50;
	public final double P_PEOPLE = 0.4;
	

	private Scenario scenario;
	
	private ObjectAttributes personHomeAndWorkLocations = new ObjectAttributes();

	
	public GeneratePopulation(Scenario scenario)
	{
		this.scenario = scenario;
		
	}
	
	public void generatePopulation()
	{
		Population population = this.scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();
		
		//index
		int linkSize = this.scenario.getNetwork().getLinks().size();
		NetworkFactory networkFactory = this.scenario.getNetwork().getFactory();
		Object[] arrayLinks = this.scenario.getNetwork().getLinks().keySet().toArray();
		
		ActivityFacilities activityFacilities = this.scenario.getActivityFacilities();
		ActivityFacilitiesFactory activityFactory = activityFacilities.getFactory();
		
		
		Random r = new Random();
		
		int personIdx = 0;
		int parcelIdx = 0;
		
		for(int i = 0; i < REQUEST_NUMBER; i++)
		{
			String id = null;
			if(r.nextDouble() < P_PEOPLE)
			{
				id = "Person-"+ personIdx;
				personIdx += 1;
			}
			else
			{
				id = "Parcel-" + parcelIdx;
				parcelIdx += 1;
			}
			
			
			Person person = populationFactory.createPerson(Id.createPersonId(id));
			Plan plan = populationFactory.createPlan();
			
			person.addPlan(plan);
			population.addPerson(person);
			((PersonImpl)person).setSelectedPlan(plan);
			
			
			
			//create randomly FromLink and ToLink
			int fromLinkIdx = (new Random()).nextInt(linkSize);
			int toLinkIdx = (new Random()).nextInt(linkSize);
			
			@SuppressWarnings("unchecked")
			Id<Link> fromLinkId = (Id<Link>)arrayLinks[fromLinkIdx];
			Id<Link> toLinkId = (Id<Link>)arrayLinks[toLinkIdx];

			
			Activity fromActivity = populationFactory.createActivityFromLinkId("work", fromLinkId);
			
			//generate start time and end time
			int hh= 0;//(new Random()).nextInt(24);
			int mm = (new Random()).nextInt(60);
			double seconds = (hh * 60 + mm)*60;
			
			//fromActivity.setStartTime(seconds);
			
			
			//assume that time window length = 10 minutes
			fromActivity.setEndTime(seconds + 2*60);
			
			plan.addActivity(fromActivity);
			
			Leg leg = populationFactory.createLeg(RequestCreator.MODE);
			leg.setDepartureTime(fromActivity.getEndTime());
			leg.setTravelTime(30*60);
			
			Route route = populationFactory.createRoute("taxi", fromLinkId, toLinkId);
			leg.setRoute(route);
			
			plan.addLeg(leg);
			
			Activity toActivity = populationFactory.createActivityFromLinkId("home", toLinkId);			

			plan.addActivity(toActivity);
			
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

	}

}

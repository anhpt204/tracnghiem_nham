package pbts.simulation;

import java.util.HashMap;

import pbts.entities.ItineraryTravelTime;
import pbts.enums.VehicleStatus;

public class SolutionRoute {

	/**
	 * @param args
	 */
	public HashMap<Integer, VehicleStatus> mStatus;
	public ItineraryTravelTime	itinerary;
	public double benefits;
	public SolutionRoute(ItineraryTravelTime	itinerary, HashMap<Integer, VehicleStatus> mStatus, double benefits){
		this.mStatus = mStatus;
		this.itinerary = itinerary;
		this.benefits = benefits;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

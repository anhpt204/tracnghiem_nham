package pbts.simulation;

import java.util.ArrayList;
import java.util.HashMap;

import pbts.entities.Itinerary;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.entities.Vehicle;
import pbts.enums.VehicleStatus;
import pbts.onlinealgorithms.BaoxiangLiOnlinePlanner;

public class BaoxiangLiSimulator extends SimulatorBookedRequests {
	public ArrayList<PeopleRequest> insertedPeopleRequests;
	public ArrayList<ParcelRequest> insertedParcelRequests;
	
	BaoxiangLiOnlinePlanner LiPlanner;
	
	
	
	public BaoxiangLiSimulator() {
		super();
	}
	



	public String name(){ return "BaoxiangLiSimulator";}
	public void relaxTimeWindowParcelRequests(){
		for(int i = 0; i < allParcelRequests.size(); i++){
			ParcelRequest pr = allParcelRequests.get(i);
			pr.earlyPickupTime = T.start;
			pr.latePickupTime = T.end;
			pr.earlyDeliveryTime = T.start;
			pr.lateDeliveryTime = T.end;
			//System.out.println(name() + "::relaxTimeWindowParcelRequests, pr = " + pr.toString());
		}
		//System.exit(-1);
	}
	public void simulateFromFile(String fn, int maxNbParcelsInserted) {
		double t0 = System.currentTimeMillis();
		loadRequests(fn);
		
		
		int Th = terminateWorkingTime - startWorkingTime;
		T = new TimeHorizon(startWorkingTime, terminateRequestTime,
				terminateWorkingTime);

		relaxTimeWindowParcelRequests();
		
		pendingParcelRequests = new ArrayList<ParcelRequest>();

		initVehicles();

		insertedParcelRequests = new ArrayList<ParcelRequest>();
		insertedPeopleRequests = new ArrayList<PeopleRequest>();

		
		runningParcelRequests = new ArrayList<ParcelRequest>();
		runningPeopleRequests = new ArrayList<PeopleRequest>();
		for(int i = 0; i < allParcelRequests.size(); i++)
			runningParcelRequests.add(allParcelRequests.get(i));
		for(int i = 0; i < allPeopleRequests.size(); i++)
			runningPeopleRequests.add(allPeopleRequests.get(i));
		
		for(int i = 0; i < vehicles.size(); i++){
			Vehicle tx = vehicles.get(i);
			System.out.println(name() + "::simulateFromFile, begin greedyInsertion for taxi " + tx.ID);
			LiPlanner.greedyInsertion(tx, maxNbParcelsInserted, runningParcelRequests);
			System.out.println(name() + "::simulateFromFile, greedyInsertion for taxi " + tx.ID + ", remain requests = " + runningParcelRequests.size());
		}
		//System.exit(-1);
		
		PeopleRequest peopleR = null;
		if(runningPeopleRequests.size() > 0){
			peopleR = runningPeopleRequests.get(0);
			runningPeopleRequests.remove(0);
		}
		distanceRequests = new ArrayList<Double>();
		while (!T.finished()) {
			//if(true)break;
			// log.println("time point " + t);
			int t = T.currentTimePoint;
			for (int k = 0; k < nbTaxis; k++) {
				Vehicle vh = vehicles.get(k);
				if (vh.status != VehicleStatus.STOP_WORK)
					vh.move();

			}

			if (T.stopRequest()) {
				// System.out.println("Simulator::simulate stop request!!!!");
				for (int k = 0; k < nbTaxis; k++) {
					Vehicle vh = vehicles.get(k);
					if (vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD) {
						System.out
								.println(name() + "::simulateDataFromFile, taxi "
										+ vh.ID
										+ ", stopRequest, status = TRAVEL_WITHOUT_LOAD --> GO BACK TO DEPOT");
						setupRouteBack2Depot(vh);
					} else if (vh.status == VehicleStatus.REST_AT_PARKING) {
						if (vh.lastPoint != mTaxi2Depot.get(vh.ID)) {
							System.out
									.println(name() + "::simulateDataFromFile, taxi "
											+ vh.ID + ", lastPoint = " + vh.lastPoint + ", depot = " + mTaxi2Depot.get(vh.ID) 
											+ ", stopRequest, status = REST_AT_PARKING --> GO BACK TO DEPOT");
							setupRouteBack2Depot(vh);
						} else {
							vh.status = VehicleStatus.STOP_WORK;
						}
					}
				}
			}

			if(peopleR != null)if(peopleR.timePoint == t){
				
				totalPeopleRequests++;
				PeopleRequest pr = peopleR;
				//allPeopleRequests.add(pr);
				System.out.println("Simulator::simulateDataFromFile --> At " + T.currentTimePointHMS() + "[" + t + "], people request " + pr.id + " arrives");
				//if(pr.id == 242){
					//System.exit(-1);
				//}
				//Itinerary I = dijkstra.solve(pr.pickupLocationID, pr.deliveryLocationID);
				double D = dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
				//if(I.getDistance() >= dijkstra.infinity){
				//if(I == null){
				if(D > dijkstra.infinity-1){
					log.println("At " + T.currentTimePointHMS() + ", cannot serve people request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID + " due to disconnectivity");
					System.out.println("Simulator::simulateDataFromFile --> At " + T.currentTimePointHMS() + ", cannot serve people request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID + " due to disconnectivity");
					nbDisconnectedRequests++;
				}else{
					LiPlanner.processPeopleRequest(pr);
				}
				if(runningPeopleRequests.size() > 0){
					peopleR = runningPeopleRequests.get(0);
					runningPeopleRequests.remove(0);
				}
				
			}
		
			T.move();
			// log.println("-------------------");
		}
		//if(true) return;
		
		double totalDistance = 0;
		int nbUnusedTaxis = 0;
		for (int k = 0; k < vehicles.size(); k++) {
			Vehicle taxi = vehicles.get(k);
			if (taxi.totalTravelDistance <= 0) {
				nbUnusedTaxis++;
				continue;
			}
			// taxi.writeItinerriesToLog();
			totalDistance = totalDistance + taxi.totalTravelDistance;
			int costi = (int) (taxi.totalTravelDistance * gamma3 / 1000);
			log.println("distance of taxi[" + taxi.ID + "] = "
					+ taxi.totalTravelDistance / 1000 + "km, cost fuel = "
					+ costi + "K");
		}
		// logI.println(-2);

		for (int i = 0; i < distanceRequests.size(); i++) {
			double D = distanceRequests.get(i);
			int m = (int) (alpha + gamma1 * D);
			D = D / 1000;
			m = m / 1000;
			log.println("requests " + i + " has distance = " + D
					+ "km, money = " + m + "K");
		}

		cost = (int) totalDistance * gamma3;
		cost = cost / 1000;
		totalDistance = totalDistance / 1000;
		revenue = revenue / 1000;
		log.println("nbPeopleRequests = " + totalPeopleRequests);
		log.println("nbAcceptedPeople = " + acceptedPeopleRequests);
		log.println("nbParcelRequests = " + totalParcelRequests);
		log.println("nbAcceptedParcelRequests = " + acceptedParcelRequests);
		log.println("nbDisconnected Requests = " + nbDisconnectedRequests);
		log.println("total distance = " + totalDistance + "km");
		log.println("revenue = " + revenue + "K");
		log.println("cost fuel = " + cost + "K");
		double benefits = revenue - cost;
		log.println("benefits = " + benefits + "K");
		log.println("nbUnusedTaxis = " + nbUnusedTaxis);

		System.out
				.println("Simulator::simulateWithAParcelFollow --> FINISHED, allPeopleRequests.sz = "
						+ allPeopleRequests.size()
						+ ", allParcelRequests.sz = "
						+ allParcelRequests.size());

		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		System.out.println("simulation time = " + t);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BaoxiangLiSimulator sim = new BaoxiangLiSimulator();
		
		//String dir = "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco";
		String dir = "SanFrancisco";
		
		String mapFN = "SanFranciscoRoad-connected-contracted-5.txt";
		String configParametersFN = "config-parameters.txt";
		String depotParkingFN = "depot600-parkings54.txt";
		String requestFN = "request_people_parcel_day_1.txt";
		sim.loadMapFromTextFile(dir + "\\" + mapFN);
		//sim.loadRequests(dir + "\\request_day_1.txt");
		
		
		sim.loadParameters(dir + "\\" + configParametersFN);
		sim.loadDepotParkings(dir + "\\" + depotParkingFN);
		
		
		
		//String requestFN = "requests-people-10-parcel-10.ins2.txt";
		//String requestFN = "requests-long-people-" + 10 + "-parcel-" + 10 + ".ins" + 1 + ".txt";
		//String requestFN = "requests-0.txt";
		String requestPath = dir + "\\" + requestFN;
		String itinerariesFN = dir + "\\BaoxiangLi" + "\\" + requestFN + "." + depotParkingFN + ".itineraries.txt";//Itineraries-people-1-parcel-1.txt";
		//String itinerariesFN = "result_requests-people-10-parcel-10.txt";
		//String itinerariesFN = "result_requests-0-1-2.txt";
		
		BaoxiangLiOnlinePlanner planner = new BaoxiangLiOnlinePlanner(sim);
		//sim.setPlanner(planner);
		sim.LiPlanner = planner;
		sim.simulateFromFile(requestPath,6);
		sim.writeTaxiItineraries(itinerariesFN);
		
		sim.initVehicles();
		sim.loadRequests(requestPath);
		//sim.relaxTimeWindowParcelRequests();
		HashMap<Integer, ItineraryTravelTime> itineraries = sim.loadItineraries(itinerariesFN);
		sim.analyzeSolution(itineraries);
		
		
		
		sim.finalize();
	}

}

package pbts.onlinealgorithms;

import pbts.entities.Arc;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.entities.Vehicle;
import pbts.simulation.ServiceSequence;

import java.io.*;
import java.util.ArrayList;

import pbts.simulation.*;
public class GreedyDirectPeopleService implements OnlinePlanner {

	public PrintWriter log = null;
	public SimulatorBookedRequests sim = null;
	public GreedyDirectPeopleService(SimulatorBookedRequests sim){
		this.sim = sim;
		this.log = sim.log;
	}
	
	public ServiceSequence computeBestProfitsParcelInsertion(Vehicle taxi, ParcelRequest pr){
		int startIdx = 0;
		//if(taxi.remainRequestIDs.size() > 0)
			//startIdx = 1;// preserve next service point, do not insert new service point at position 0
		int idxDelivery = -1;
		int rid = -1;
		PeopleRequest peopleReq = null;
		if(taxi.peopleReqIDonBoard.size() > 0){
			rid = taxi.peopleReqIDonBoard.get(0);
			peopleReq = sim.mPeopleRequest.get(rid);
			
			for(int i = 0; i < taxi.remainRequestIDs.size(); i++){
				if(taxi.remainRequestIDs.get(i) == -rid){
					idxDelivery = i; break;
				}
			}
			if(sim.countStop.get(rid) + idxDelivery >= peopleReq.maxNbStops){
				startIdx = idxDelivery+1;
			}
		}
		int nextStartPoint = taxi.getNextStartPoint();
		int startTimePoint = taxi.getNextStartTimePoint();
		
		int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		double maxProfits = -sim.dijkstra.infinity;
		//int sel_pickup_index = -1;
		//int sel_delivery_index = -1;
		//int sel_pk = -1;
		ServiceSequence ss = null;
		double expectDistanceParcel = sim.dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
		for(int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++){
			for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
			
				// establish new sequence of request ids stored in nod
				if(rid > 0){
					if(i1 <= idxDelivery && i2 <= idxDelivery && sim.countStop.get(rid) + idxDelivery + 2 > peopleReq.maxNbStops)
						continue;
				}
				int idx = -1;
				double profits = sim.getParcelRevenue(expectDistanceParcel);
				for(int k1 = 0; k1 < i1; k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				idx++;
				nod[idx] = pr.id;// insert pickup
				for(int k1 = 0; k1 < i2-i1; k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(i1 + k1);
				}
				idx++;
				nod[idx] = -pr.id;// insert delivery
				for(int k1 = i2; k1 < taxi.remainRequestIDs.size(); k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				
				if(!sim.checkDirectPeopleServices(nod)) continue;
				
				// evaluate the insertion
				double D = sim.computeFeasibleDistance(nextStartPoint, startTimePoint, nod);
				if(D > sim.dijkstra.infinity - 1) continue;// constraints are violated
				
				for(int k = 0; k < parkings.size(); k++){
					int pk = parkings.get(k);
					//double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod, pk);
					//if(D > dijkstra.infinity - 1) continue;// constraints are violated
					int endPoint = sim.getLocationFromEncodedRequest(nod[nod.length-1]);
					D = D + sim.dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - sim.getCostFuel(extraDistance);
					if(profits > maxProfits){
						maxProfits = profits;
						//sel_pickup_index = i1;
						//sel_delivery_index = i2;
						//sel_pk = pk;
						ss = new ServiceSequence(nod,profits,pk,D);
					}
				}
			}
		}
		
		return ss;
	}

	public ServiceSequence computeBestProfitsPeopleInsertion(Vehicle taxi, PeopleRequest pr){
		int startIdx = 0;
		//if(taxi.remainRequestIDs.size() > 0)
			//startIdx = 1;// preserve next service point, do not insert new service point at position 0
		int taxiID = 1;
		if(taxi.peopleReqIDonBoard.size() > 0){
			// taxi is carrying a passenger
			int rid = taxi.peopleReqIDonBoard.get(0);
			for(int i = 0;  i < taxi.remainRequestIDs.size(); i++){
				if(taxi.remainRequestIDs.get(i) == -rid){
					startIdx = i+1; break;
				}
			}
			if(taxi.ID == taxiID){
				System.out.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, " +
						"request on board = " + rid + ", remainRequestIDs = " + taxi.getRemainRequestID() + ", startIdx = " + startIdx + ", new people quest = " + pr.id);
				log.println("SimulatorBookedRequest::computeBestProfitsPeopleInsertion, " +
						"request on board = " + rid + ", remainRequestIDs = " + taxi.getRemainRequestID() + ", startIdx = " + startIdx + ", new people quest = " + pr.id);
				
			}
		}
		
		int nextStartPoint = taxi.getNextStartPoint();
		int startTimePoint = taxi.getNextStartTimePoint();
		
		int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = sim.collectAvailableParkings(taxi);
		//double minExtraDistance = dijkstra.infinity;
		//int sel_pickup_index = -1;
		//int sel_delivery_index = -1;
		//int sel_pk = -1;
		double maxProfits = -sim.dijkstra.infinity;
		ServiceSequence ss = null;
		double expectDistancePeople = sim.dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
		for(int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++){
			int max = taxi.remainRequestIDs.size() < i1 + pr.maxNbStops ? taxi.remainRequestIDs.size() : i1 + pr.maxNbStops;
			//for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
			for(int i2 = i1; i2 <= i1; i2++){// direct people service, no intermediate stops between people service
				// establish new sequence of request ids stored in nod
				int idx = -1;
				int pickup_idx = -1;
				int delivery_idx = -1;
				double profits = sim.getPeopleRevenue(expectDistancePeople);
				for(int k1 = 0; k1 < i1; k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				idx++;
				nod[idx] = pr.id;// insert pickup
				pickup_idx = idx;
				for(int k1 = 0; k1 < i2-i1; k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(i1 + k1);
				}
				idx++;
				nod[idx] = -pr.id;// insert delivery
				delivery_idx = idx;
				for(int k1 = i2; k1 < taxi.remainRequestIDs.size(); k1++){
					idx++;
					nod[idx] = taxi.remainRequestIDs.get(k1);
				}
				
				// compute the distance of passenger service
				double distancePeople = 0;
				for(int k1 = pickup_idx; k1 < delivery_idx; k1++){
					int u = sim.getLocationFromEncodedRequest(nod[k1]);
					int v = sim.getLocationFromEncodedRequest(nod[k1+1]);
					distancePeople = distancePeople + sim.dijkstra.queryDistance(u,v);
				}
				if(distancePeople > pr.maxTravelDistance) continue;
				
				// check if travel distance of passenger on board exceeds maximum distance allowed
				boolean ok = true;
				for(int k1 = 0; k1 < taxi.peopleReqIDonBoard.size(); k1++){
					int pobReqID = taxi.peopleReqIDonBoard.get(k1);
					PeopleRequest pR = sim.mPeopleRequest.get(pobReqID);
					double d1 = sim.computeRemainTravelDistance(pobReqID,nod,nextStartPoint);
					Arc A = sim.map.getArc(taxi.lastPoint, nextStartPoint);
					double d2 = d1 + sim.accumulateDistance.get(pobReqID);
					if(A != null) d2 = d2 + A.w;
					if(d2 > pR.maxTravelDistance){
						ok = false; break;
					}
				}
				if(!ok) continue;
				
				if(pickup_idx < delivery_idx - 1)// not direct delivery
					profits = profits - sim.getDiscount(expectDistancePeople, distancePeople);
				
				// evaluate the insertion
				
				double D = sim.computeFeasibleDistance(nextStartPoint, startTimePoint, nod);
				if(D > sim.dijkstra.infinity - 1) continue;// constraints are violated
				
				for(int k = 0; k < parkings.size(); k++){
					int pk = parkings.get(k);
					//double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod, pk);
					//if(D > dijkstra.infinity - 1) continue;// constraints are violated
					int endPoint = sim.getLocationFromEncodedRequest(nod[nod.length-1]);
					D = D + sim.dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - sim.getCostFuel(extraDistance);
					
					if(profits > maxProfits){
						maxProfits = profits;
						//sel_pickup_index = i1;
						//sel_delivery_index = i2;
						//sel_pk = pk;
						ss = new ServiceSequence(nod,profits,pk,D);
						/*
						System.out.println("SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = " + ss.profitEvaluation +
								", sequence = " + ss.getSequence() + ", distance D = " + ss.distance);
						log.println("SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = " + ss.profitEvaluation +
								", sequence = " + ss.getSequence() + ", distance D = " + ss.distance);
								*/
					}
				}
			}
		}
		
		return ss;
	}

	public void processPeopleRequest(PeopleRequest pr) {
		// TODO Auto-generated method stub
		ServiceSequence sel_ss = null;
		Vehicle sel_taxi = null;
		int taxiID = 1;
		for(int k = 0; k < sim.vehicles.size(); k++){
			Vehicle taxi = sim.vehicles.get(k);
			if(taxi.remainRequestIDs.size() > sim.maxPendingStops) continue;
			ServiceSequence ss = computeBestProfitsPeopleInsertion(taxi, pr);
			if(ss != null){
				//System.out.println("SimulatorBookedRequest::processPeopleRequest, taxi " + taxi.ID + 
						//", profits = " + ss.profitEvaluation);
				if(sel_ss == null){
					sel_ss = ss;
					sel_taxi = taxi;
				}else{
					if(sel_ss.profitEvaluation < ss.profitEvaluation){
						sel_ss = ss;
						sel_taxi = taxi;
						System.out.println("SimulatorBookedRequest::processPeopleRequest, UPDATE sel_taxi " + sel_taxi.ID + 
								", sel_ss.profits = " + sel_ss.profitEvaluation);
						if(taxiID == sel_taxi.ID)
							log.println("SimulatorBookedRequest::processPeopleRequest, UPDATE sel_taxi " + sel_taxi.ID + 
								", sel_ss.profits = " + sel_ss.profitEvaluation);
					}
				}
			}
		}
		if(sel_taxi == null){
			System.out.println("SimulatorBookedRequest::processPeopleRequest --> request " + pr.id + " is REJECTED");
			log.println("SimulatorBookedRequest::processPeopleRequest --> request " + pr.id + " is REJECTED");
			return;
		}
		int nextStartTimePoint = sel_taxi.getNextStartTimePoint();
		int fromPoint = sel_taxi.getNextStartPoint();
		int fromIndex = sel_taxi.getNextStartPointIndex();
		System.out.println("SimulatorBookedRequest::processPeopleRequest, sequence = " + sel_ss.getSequence() + 
				", maxProfits = " + sel_ss.profitEvaluation + 
				", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		if(taxiID == sel_taxi.ID)log.println("SimulatorBookedRequest::processPeopleRequest, sequence = " + sel_ss.getSequence() + 
				", maxProfits = " + sel_ss.profitEvaluation + 
				", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
				", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
		sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);

	}

	public void processParcelRequest(ParcelRequest pr) {
		// TODO Auto-generated method stub
		
			
			ServiceSequence sel_ss = null;
			Vehicle sel_taxi = null;
			for(int k = 0; k < sim.vehicles.size(); k++){
				Vehicle taxi = sim.vehicles.get(k);
				if(taxi.remainRequestIDs.size() > sim.maxPendingStops) continue;
				ServiceSequence ss = computeBestProfitsParcelInsertion(taxi, pr);
				if(ss != null){
					//System.out.println("SimulatorBookedRequest::processPeopleRequest, taxi " + taxi.ID + 
							//", profits = " + ss.profitEvaluation);
					if(sel_ss == null){
						sel_ss = ss;
						sel_taxi = taxi;
					}else{
						if(sel_ss.profitEvaluation < ss.profitEvaluation){
							sel_ss = ss;
							sel_taxi = taxi;
						}
					}
				}
			}
			if(sel_taxi == null){
				System.out.println("SimulatorBookedRequest::processParcelRequest --> request " + pr.id + " is REJECTED");
				log.println("SimulatorBookedRequest::processParcelRequest --> request " + pr.id + " is REJECTED");
				return;
			}
			int nextStartTimePoint = sel_taxi.getNextStartTimePoint();
			int fromPoint = sel_taxi.getNextStartPoint();
			int fromIndex = sel_taxi.getNextStartPointIndex();
			
			System.out.println("SimulatorBookedRequest::processParcelRequest, sequence = " + sel_ss.getSequence() + 
					", maxProfits = " + sel_ss.profitEvaluation + 
					", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
					", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
			log.println("SimulatorBookedRequest::processParcelRequest, sequence = " + sel_ss.getSequence() + 
					", maxProfits = " + sel_ss.profitEvaluation + 
					", sel_taxi = " + sel_taxi.ID + ", nextStartTimePoint = " + nextStartTimePoint + 
					", fromIndex = " + fromIndex + ", fromPoint = " + fromPoint);
			sim.admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);
		

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

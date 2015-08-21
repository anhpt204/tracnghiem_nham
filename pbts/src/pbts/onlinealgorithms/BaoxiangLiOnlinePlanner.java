package pbts.onlinealgorithms;

import java.io.PrintWriter;
import java.util.ArrayList;

import pbts.entities.Arc;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.entities.Vehicle;
import pbts.simulation.ServiceSequence;
import pbts.simulation.SimulatorBookedRequests;

import java.util.*;
public class BaoxiangLiOnlinePlanner implements OnlinePlanner {
	public PrintWriter log = null;
	public SimulatorBookedRequests sim = null;
	Random R = new Random();
	public int maxParcelRequestsInserted = 6;
	public BaoxiangLiOnlinePlanner(SimulatorBookedRequests sim){
		this.sim = sim;
		this.log = sim.log;
	}
	public void greedyInsertion(Vehicle taxi, int maxSz, ArrayList<ParcelRequest> PR) {
		//System.out.println(name() + "::greedyInsertion, taxi " + taxi.ID + ", lastPoint = " + taxi.lastPoint);
		ArrayList<ParcelRequest> L = new ArrayList<ParcelRequest>();
		//for(int k = 0; k < maxSz; k++){
			for(int i = 0; i < PR.size(); i++){
				if(sim.getNumberParcelRequestEngaged(taxi) + L.size() >= maxSz) break;
				ParcelRequest pr = PR.get(i);
				System.out.println(name() + "::greedyInsertion(taxi " + taxi.ID + ", PR.sz = " + PR.size() + "), pr = " + pr.id);
				ServiceSequence ss = computeBestProfitsParcelInsertion(taxi, pr);
				if(ss == null)
					System.out.println(name() + "::greedyInsertion(taxi " + taxi.ID + ", PR.sz = " + PR.size() + "), pr = " + pr.id + ", obtained ss = null");
				else{
					sim.admitNewItinerary(taxi, taxi.getNextStartTimePoint(), taxi.getNextStartPointIndex(), taxi.getNextStartPoint(), ss);
					System.out.println(name() + "::greedyInsertion(taxi " + taxi.ID + ", PR.sz = " + PR.size() + "), pr = " + pr.id +
						", obtained ss = " + ss.rids.length + ", taxi.nextStartTimePoint = " + taxi.getNextStartTimePoint() + 
						", nextStartPointIndex = " + taxi.getNextStartPointIndex() + ", nextStartPoint = " + taxi.getNextStartPoint());
				
					L.add(pr);
				}
				//if(L.size() >= maxSz) break;
			}
		//}
		for(int i = 0; i < L.size(); i++){
			ParcelRequest pr = L.get(i);
			int idx = PR.indexOf(pr);
			PR.remove(idx);
		}
		//System.out.println(name() + "::greedyInsertion, taxi " + taxi.ID + ", finished lastPoint = " + taxi.lastPoint);
	}

	public ServiceSequence computeBestProfitsParcelInsertion(Vehicle taxi, ParcelRequest pr){
		int startIdx = 0;
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
		//System.out.println(name() + "::computeBestProfitsParcelInsertion, nextStartPoint = " +
		//nextStartPoint + ", startTimePoint = " + startTimePoint + ", startIdx = " + startIdx);
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
					if(i1 <= idxDelivery && i2 <= idxDelivery)// && sim.countStop.get(rid) + idxDelivery + 2 > peopleReq.maxNbStops)
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
				
				// evaluate the insertion
				double D = sim.computeFeasibleDistance(nextStartPoint, startTimePoint, nod);
				//System.out.println(name() + "::computeBestProfitsParcelInsertion, feasibleDistance D = "
				//+ D + ", nod.length = " + nod.length);
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

	public ServiceSequence selectANeighbor(Vehicle taxi, ServiceSequence ss, ArrayList<ParcelRequest> PR){
		System.out.println(name() + "::selectANeihgbor, taxi " + taxi.ID + ", ss.length = " + ss.rids.length + ", PR.sz = " + PR.size());
		if(PR == null || PR.size() == 0) return ss;
		ArrayList<Integer> L = new ArrayList<Integer>();
		for(int i = 0; i < ss.rids.length; i++){
			if(ss.rids[i] > 0){
				if(sim.mParcelRequest.get(ss.rids[i]) != null){// consider only parcel requests
					L.add(ss.rids[i]);
				}
			}
		}
		int sel_rid = -100000000;
		ParcelRequest removedParcelReq = null;//sim.getParcelRequest(sel_rid);
		if(L.size() > 0){
			sel_rid = L.get(R.nextInt(L.size()));
			removedParcelReq = sim.getParcelRequest(sel_rid);
			L.clear();
			taxi.remainRequestIDs.clear();
			for(int i = 0; i < ss.rids.length-1; i++){
				if(ss.rids[i] != sel_rid && ss.rids[i] != -sel_rid){
					//idx++;
					//L.add(ss.rids[i]);
					taxi.remainRequestIDs.add(ss.rids[i]);
				}
			}
		}
		//ServiceSequence nss = new ServiceSequence(nrids,ss.profitEvaluation,ss.parkingLocationPoint,ss.distance);
		ServiceSequence sel_ss = null;
		ParcelRequest addedParcelReq = null;
		
		double maxProfits = -100000;
		for(int i = 0; i < PR.size(); i++){
			ParcelRequest pr = PR.get(i);
			ServiceSequence ssi = computeBestProfitsParcelInsertion(taxi, pr);
			System.out.println(name() + "::selectANeihgbor, taxi " + taxi.ID + ", finish " + i + "/" + PR.size());
			if(ssi != null)if(maxProfits < ssi.profitEvaluation){
				maxProfits = ssi.profitEvaluation;
				sel_ss = ssi;
				addedParcelReq = pr;
				System.out.println(name() + "::selectANeihgbor, taxi " + taxi.ID + ", UPDATE maxProfits = " + maxProfits);
				break;
			}
			
		}
		if(addedParcelReq != null){
			int idx = PR.indexOf(addedParcelReq);
			PR.remove(idx);
		}
		
		if(removedParcelReq != null)
		PR.add(removedParcelReq);
		
		return sel_ss;
	}
	private boolean accept(double p){
		//int x = R.nextInt(10000);
		//if(10000*p < x) return true;
		//return false;
		return p > Math.random();
	}
	public ServiceSequence neighborhoodSearch(ServiceSequence ss, Vehicle taxi, ArrayList<ParcelRequest> PR, int maxIter, int maxTime){
		ServiceSequence best_ss = ss;
		ArrayList<ParcelRequest> best_remain_parcel_request = new ArrayList<ParcelRequest>();
		for(int i = 0; i < PR.size(); i++)
			best_remain_parcel_request.add(PR.get(i));
		maxTime = maxTime*1000;
		double t0 = System.currentTimeMillis();
		int it = 1;
		ServiceSequence ssi = ss;
		double T = 1;// temperature
		boolean firstIteration = true;
		while(it < maxIter && System.currentTimeMillis() - t0 < maxTime){
			ServiceSequence ssj = selectANeighbor(taxi,ssi,PR);
			if(ssj == null){it++; continue;}
			if(ssj.profitEvaluation >= ssi.profitEvaluation) {
				ssi = ssj;
				if(ssj.profitEvaluation > best_ss.profitEvaluation){
					best_ss = ssj;
					best_remain_parcel_request = new ArrayList<ParcelRequest>();
					for(int i = 0; i < PR.size(); i++)
						best_remain_parcel_request.add(PR.get(i));
				}
			}else{
				if(firstIteration){
					firstIteration = false;
					double x = Math.log(0.5);
					T = (ssj.profitEvaluation-ssi.profitEvaluation)*1.0/x;
				}
				
				double prob = Math.exp((ssj.profitEvaluation - ssi.profitEvaluation)*1.0/T);
				if(accept(prob)){
					ssi = ssj;
					T = 0.99*T;
				}
			}
			it++;
		}
		PR.clear();
		for(int i = 0; i < best_remain_parcel_request.size(); i++)
			PR.add(best_remain_parcel_request.get(i));
		return best_ss;
	}
	public ServiceSequence computeBestProfitsPeopleInsertion(Vehicle taxi, PeopleRequest pr){
		int startIdx = 0;
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
			//int max = taxi.remainRequestIDs.size() < i1 + pr.maxNbStops ? taxi.remainRequestIDs.size() : i1 + pr.maxNbStops;
			for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
			//for(int i2 = i1; i2 <= i1; i2++){
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

	public Vehicle findNearestAvailableTaxi(PeopleRequest pr){
		Vehicle taxi = null;
		double minD = 100000000;
		for(int i = 0; i < sim.vehicles.size(); i++){
			Vehicle tx = sim.vehicles.get(i);
			if(tx.peopleReqIDonBoard.size() > 0) continue;
			double d = sim.dijkstra.queryDistance(tx.getNextStartPoint(), pr.pickupLocationID);
			if(minD > d){
				minD = d;
				taxi = tx;
			}
		}
		return taxi;
	}
	public String name(){ return "BaoxiangLiOnlinePlanner";}
	public void processPeopleRequest(PeopleRequest pr) {
		// TODO Auto-generated method stub
		Vehicle taxi = findNearestAvailableTaxi(pr);
		if(taxi == null){
			System.out.println(name() + "::processPeopleRequest, taxi = null --> REJECT request due to no taxi available, info = " + sim.peopleParcelServiceToString());
			return;
		}
		ServiceSequence ss = computeBestProfitsPeopleInsertion(taxi, pr);
		if(ss == null){
			System.out.println(name() + "::processPeopleRequest, ss = null --> REJECT request due to no feasible insertion, info = " + sim.peopleParcelServiceToString());
			return;
		}
		greedyInsertion(taxi, maxParcelRequestsInserted , sim.runningParcelRequests);
		ss = neighborhoodSearch(ss, taxi, sim.runningParcelRequests, 1000, 2);
		System.out.println(name() + "::processPeopleRequest, ACCEPT people request, ss = " + ss.getSequence() + ", profits = " + ss.profitEvaluation);
		sim.admitNewItinerary(taxi, taxi.getNextStartTimePoint(), taxi.getNextStartPointIndex(), taxi.getNextStartPoint(), ss);
		
	}

	public void processParcelRequest(ParcelRequest pr) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(Math.log(0.5) + ", " + Math.random());
		
	}

}
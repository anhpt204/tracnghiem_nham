package pbts.simulation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import pbts.entities.*;
import pbts.enums.VehicleAction;
import pbts.enums.VehicleStatus;
import pbts.shortestpaths.DijkstraBinaryHeap;
import pbts.onlinealgorithms.*;

class TaxiIndex{
	public Vehicle taxi;
	public int index;
	public TaxiIndex(Vehicle taxi, int index){
		this.taxi = taxi; this.index = index;
	}
}
public class SimulatorBookedRequests extends Simulator {

	/**
	 * @param args
	 */
	
	public ArrayList<PeopleRequest> insertedPeopleRequests;
	public ArrayList<ParcelRequest> insertedParcelRequests;
	
	public OnlinePlanner planner;
	
	public SimulatorBookedRequests(){
		super();
		
	}
	public void setPlanner(OnlinePlanner planner){
		this.planner = planner;
	}
	
	public TaxiIndex findAppropriateTaxiForInsertion(ParcelRequest pr){
		double minD = DijkstraBinaryHeap.infinity;
		Vehicle sel_taxi = null;
		int index = -1;
		int sel_index = -1;
		int taxiID = -1;
		for(int k = 0; k < nbTaxis; k++){
			Vehicle tx = vehicles.get(k);
			int pos = -1;
			if(tx.status == VehicleStatus.REST_AT_PARKING){
				pos = tx.lastPoint; 
				index = -1;
			}else if(tx.status != VehicleStatus.STOP_WORK && tx.status != VehicleStatus.GO_BACK_DEPOT_FINISH_WORK){
				int idx = tx.currentItinerary.findLastDeliveryIndexPoint(tx.lastIndexPoint);
				if(tx.ID == taxiID){
					System.out.println("SimulatorBookedRequests::findAppropriateTaxiForInsertion, taxi = 173, tx.lastIndexPoint = " + tx.lastIndexPoint + ", idx = " + idx);
					log.println("SimulatorBookedRequests::findAppropriateTaxiForInsertion, taxi = 173, tx.lastIndexPoint = " + tx.lastIndexPoint + ", idx = " + idx);
					if(idx > 0 && tx.currentItinerary.getDepartureTime(idx) < T.currentTimePoint){
						log.println("SimulatorBookedRequests::findAppropriateTaxiForInsertion, taxi = 173, " +
								", BUG???????? tx.currentItinerary.getDepartureTime(" + idx + ") = " + tx.currentItinerary.getDepartureTime(idx) + " < T.current = " + T.currentTimePoint);
					}
				}
				if(idx < 0){
					if(tx.status == VehicleStatus.DELIVERY_PARCEL || tx.status == VehicleStatus.DELIVERY_PEOPLE)
						idx = tx.lastIndexPoint;
					else
						idx = tx.lastIndexPoint + 1;
				}
				pos = tx.currentItinerary.get(idx);
				index = idx;
			}
			if(pos < 0) return null;
			double D = dijkstra.queryDistance(pos, pr.pickupLocationID);
			if(D < minD){
				minD = D;
				sel_taxi = tx;
				sel_index = index;
			}
		}
		if(sel_taxi == null) return null;
		return new TaxiIndex(sel_taxi,sel_index);
	}
	/*
	public ArrayList<Integer> collectRemainingStops(Vehicle taxi){
		ArrayList<Integer> L = new ArrayList<Integer>();
		ItineraryTravelTime I = taxi.currentItinerary;
		if(I != null)
		for(int i = taxi.lastIndexPoint+1; i < I.size(); i++){
			int rid = I.getRequestID(i);
			VehicleAction act = I.getAction(i);
			if(act == VehicleAction.PICKUP_PARCEL){
				ParcelRequest pri = mParcelRequest.get(rid);
				L.add(pri.pickupLocationID);
			}else if(act == VehicleAction.DELIVERY_PARCEL){
				ParcelRequest pri = mParcelRequest.get(rid);
				L.add(pri.deliveryLocationID);
			}else if(act == VehicleAction.PICKUP_PEOPLE){
				PeopleRequest pri = mPeopleRequest.get(rid);
				L.add(pri.pickupLocationID);
			}else if(act == VehicleAction.DELIVERY_PEOPLE){
				PeopleRequest pri = mPeopleRequest.get(rid);
				L.add(pri.deliveryLocationID);
			}
		}
		return L;
	}
	*/
	
	
	
	public ItineraryTravelTime establishItinerary(int nextStartTimePoint, int fromIndex, 
			int reqIDAtFromPoint, int fromPoint, ServiceSequence ss){
		//int taxiID = 1;
		
		ItineraryTravelTime retI = new ItineraryTravelTime();
		
		int curPos = fromPoint;
		int td = nextStartTimePoint;
		//retI.addPoint(curPos);
		//retI.addAction(VehicleAction.PASS);
		//retI.addRequestID(-1);
		
		//taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
		//retI.setDepartureTime(retI.size(), td);
		
		//int lp = taxi.currentItinerary.get(taxi.currentItinerary.size()-1);
		//int lp = retI.get(retI.size()-1);
		//if(taxiID == taxi.ID)
			//log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + ", INIT setDepartureTime(" + lp + "," + td + ")" + ", T.currnet = " + T.currentTimePoint);
		
		int startI = 0;
		int firstLoc = getLocationFromEncodedRequest(ss.rids[0]);
		//if(taxi.currentItinerary.get(taxi.currentItinerary.size()-1) == firstLoc && 
				//taxi.currentItinerary.getRequestID(taxi.currentItinerary.size()-1) == Math.abs(ss.rids[0]))
		if(firstLoc == fromPoint && reqIDAtFromPoint == Math.abs(ss.rids[0]))
			startI = 1;// fromPoint = first point of ss.rids then startI = 1 in order to avoid repeating point
		for(int i = startI; i < ss.rids.length; i++){
		//for(int i = 0; i < ss.rids.length; i++){
			int rid = ss.rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			PeopleRequest peopleReq = mPeopleRequest.get(arid);
			if(peopleReq != null){
				if(rid > 0) nextPoint = peopleReq.pickupLocationID; else nextPoint = peopleReq.deliveryLocationID;
				Itinerary I = dijkstra.queryShortestPath(curPos, nextPoint);
				int t = getTravelTime(I.getDistance(), maxSpeedms);
				double d = I.getDistance();
				int v0 = I.get(0);
				for(int j = 1; j < I.size()-1; j++){
					int v = I.get(j);
					
					//taxi.currentItinerary.addPoint(v);
					retI.addPoint(v);
					//taxi.currentItinerary.addAction(VehicleAction.PASS);
					retI.addAction(VehicleAction.PASS);
					//taxi.currentItinerary.addRequestID(-1);
					retI.addRequestID(-1);
					
					Arc a = map.getArc(v0, v);
					int dt = (int)(t*a.w/d);
					td = td + dt;
					t = t - dt;
					d = d - a.w;
					v0 = v;
					//taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
					retI.setArrivalTime(retI.size()-1, td);
					//taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
					retI.setDepartureTime(retI.size()-1, td);
				}
				//taxi.currentItinerary.addPoint(I.get(I.size()-1));
				retI.addPoint(I.get(I.size()-1));
				//taxi.currentItinerary.addRequestID(arid);
				retI.addRequestID(arid);
				
				td = td + t;
				//taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
				retI.setArrivalTime(retI.size()-1, td);
				//lp = taxi.currentItinerary.get(taxi.currentItinerary.size()-1);
				//lp = retI.get(retI.size()-1);
				
				//if(taxiID == taxi.ID)
					//log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + ", setArrivalTime(" + lp + "," + td + ")" + ", T.currnet = " + T.currentTimePoint);
				if(rid > 0){
					//taxi.currentItinerary.addAction(VehicleAction.PICKUP_PEOPLE); 
					retI.addAction(VehicleAction.PICKUP_PEOPLE);
					//taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.PICKUP_PEOPLE);
					//taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.PICKUP_PEOPLE);
					
					td = td + peopleReq.pickupDuration;
					if(td > peopleReq.latePickupTime || td < peopleReq.earlyPickupTime) 
						return null;
					
					
					//taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
					retI.setDepartureTime(retI.size()-1, td);
					//if(taxiID == taxi.ID)
						//log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + ", setDepartureTime(" + lp + "," + td + ")" + ", T.currnet = " + T.currentTimePoint);
				}
				else{
					//taxi.currentItinerary.addAction(VehicleAction.DELIVERY_PEOPLE);
					retI.addAction(VehicleAction.DELIVERY_PEOPLE);
					//taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.DELIVERY_PEOPLE);
					
					if(td > peopleReq.lateDeliveryTime || td < peopleReq.earlyDeliveryTime) return null;
					
					td = td + peopleReq.deliveryDuration;
					//taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
					retI.setDepartureTime(retI.size()-1, td);
					//if(taxiID == taxi.ID)
						//log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + ", setDepartureTime(" + lp + "," + td + ")" + ", T.currnet = " + T.currentTimePoint);
				}
			}else{
				ParcelRequest parcelReq = mParcelRequest.get(arid);
				if(rid > 0) nextPoint = parcelReq.pickupLocationID; else nextPoint = parcelReq.deliveryLocationID;
				Itinerary I = dijkstra.queryShortestPath(curPos, nextPoint);
				int t = getTravelTime(I.getDistance(), maxSpeedms);
				double d = I.getDistance();
				int v0 = I.get(0);
				for(int j = 1; j < I.size()-1; j++){
					int v = I.get(j);
					//taxi.currentItinerary.addPoint(v);
					retI.addPoint(v);
					//taxi.currentItinerary.addAction(VehicleAction.PASS);
					retI.addAction(VehicleAction.PASS);
					//taxi.currentItinerary.addRequestID(-1);
					retI.addRequestID(-1);
					
					Arc a = map.getArc(v0, v);
					int dt = (int)(t*a.w/d);
					td = td + dt;
					t = t - dt;
					d = d - a.w;
					v0 = v;
					//taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
					retI.setArrivalTime(retI.size()-1, td);
					//taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
					retI.setDepartureTime(retI.size()-1, td);
				}
				//taxi.currentItinerary.addPoint(I.get(I.size()-1));
				retI.addPoint(I.get(I.size()-1));
				//taxi.currentItinerary.addRequestID(arid);
				retI.addRequestID(arid);
				
				td = td + t;
				//taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
				retI.setArrivalTime(retI.size()-1, td);
				
				if(rid > 0){
					//taxi.currentItinerary.addAction(VehicleAction.PICKUP_PARCEL);
					retI.addAction(VehicleAction.PICKUP_PARCEL);
					//taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.PICKUP_PARCEL);
					
					if((parcelReq.earlyPickupTime > td || parcelReq.latePickupTime < td) && 
							(parcelReq.earlyPickupTime > td + parcelReq.pickupDuration ||
									parcelReq.latePickupTime < td + parcelReq.pickupDuration))
						return null;
					
					td = td + parcelReq.pickupDuration;
					//taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
					retI.setDepartureTime(retI.size()-1, td);
				}
				else{
					//taxi.currentItinerary.addAction(VehicleAction.DELIVERY_PARCEL);
					retI.addAction(VehicleAction.DELIVERY_PARCEL);
					//taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.DELIVERY_PARCEL);
					
					if((parcelReq.earlyDeliveryTime > td || parcelReq.lateDeliveryTime < td) && 
							(parcelReq.earlyDeliveryTime > td + parcelReq.deliveryDuration ||
									parcelReq.lateDeliveryTime < td + parcelReq.deliveryDuration))
						return null;
					
					td = td + parcelReq.deliveryDuration;
					//taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
					retI.setDepartureTime(retI.size()-1, td);
				}
			}
			curPos = nextPoint;
		}
		Itinerary I = dijkstra.queryShortestPath(curPos, ss.parkingLocationPoint);
		int t = getTravelTime(I.getDistance(), maxSpeedms);
		double d = I.getDistance();
		int v0 = I.get(0);
		
		for(int j = 1; j < I.size()-1; j++){
			int v = I.get(j);
			//taxi.currentItinerary.addPoint(v);
			retI.addPoint(v);
			//taxi.currentItinerary.addAction(VehicleAction.PASS);
			retI.addAction(VehicleAction.PASS);
			//taxi.currentItinerary.addRequestID(-1);
			retI.addRequestID(-1);
			Arc a = map.getArc(v0, v);
			int dt = (int)(t*a.w/d);
			td = td + dt;
			t = t - dt;
			d = d - a.w;
			v0 = v;
			//taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
			retI.setArrivalTime(retI.size()-1, td);
			//taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
			retI.setDepartureTime(retI.size()-1, td);
		}
		//taxi.currentItinerary.addPoint(I.get(I.size()-1));
		retI.addPoint(I.get(I.size()-1));
		//taxi.currentItinerary.addRequestID(-1);
		retI.addRequestID(-1);
		//taxi.currentItinerary.addAction(VehicleAction.STOP);
		retI.addAction(VehicleAction.STOP);
		//taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.REST_AT_PARKING);
		td = td + t;
		//taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
		retI.setArrivalTime(retI.size()-1, td);
		/*
		boolean ok = assignTimePoint(taxi.currentItinerary, taxi, fromIndex, nextStartTimePoint, insertedPeopleRequests, insertedParcelRequests);
		if(!ok){
			System.out.println("SimulatorBookedRequest::admitNewItinerary --> FAILED when assignTimePoint????");
			log.close();
			System.exit(-1);
			
		}
		*/
		
		return retI;
	}
	public void admitNewItinerary(Vehicle taxi, int nextStartTimePoint, int fromIndex, 
			int fromPoint, ItineraryTravelTime I, ServiceSequence ss){
		
		int taxiID = 1;
		if(taxiID == taxi.ID)
			log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + " REST AT PARKING, " +
				"curPos = lastPoint = " + taxi.lastPoint + ", nextStartTimePoint = " + nextStartTimePoint + ", fromPoint = " +
					fromPoint + ", fromIndex = " + fromIndex + ", status = " + taxi.getStatusDescription(taxi.status) + ", T.current = " + T.currentTimePoint);
		if(taxi.status == VehicleStatus.REST_AT_PARKING){
			if(taxi.ID == taxi.ID){
				//System.out.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = " + curPos);
				//log.println("SimulatorBookedRequest::insertParcelRequest, taxi " + taxi.ID + " REST AT PARKING, " +
						//"curPos = lastPoint = " + taxi.lastPoint + ", nextStartTimePoint = " + nextStartTimePoint + ", fromPoint = " + fromPoint + ", fromIndex = " + fromIndex);
				//System.exit(-1);
			}
			taxi.mStatus.clear();
			taxi.mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
			taxi.status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			int[]  path = new int[1];
			ArrayList<Integer> requestID = new ArrayList<Integer>();
			ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
			path[0] = fromPoint;
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			taxi.currentItinerary = new ItineraryTravelTime(path, requestID, actions);
			
			taxi.lastIndexPoint = 0;
			
			taxi.addItinerary(taxi.currentItinerary);
		}
		
		pbts.entities.Parking P = taxi.getFinalParking();
		if(P != null){
			P.load--;
		}
		taxi.cancelSubItinerary(fromIndex+1);
		
		ItineraryTravelTime CI = taxi.currentItinerary;
		for(int i = 0; i < I.size(); i++){
			CI.addPoint(I.get(i));
			CI.addAction(I.getAction(i));
			CI.addRequestID(I.getRequestID(i));
			CI.setArrivalTime(CI.size()-1, I.getArrivalTime(i));
			CI.setDepartureTime(CI.size()-1, I.getDepartureTime(i));
			if(I.getAction(i) == VehicleAction.PICKUP_PARCEL)
				taxi.mStatus.put(CI.size()-1, VehicleStatus.PICKUP_PARCEL);
			else if(I.getAction(i) == VehicleAction.DELIVERY_PARCEL)
				taxi.mStatus.put(CI.size()-1, VehicleStatus.DELIVERY_PARCEL);
			else if(I.getAction(i) == VehicleAction.PICKUP_PEOPLE)
				taxi.mStatus.put(CI.size()-1, VehicleStatus.PICKUP_PEOPLE);
			else if(I.getAction(i) == VehicleAction.DELIVERY_PEOPLE)
				taxi.mStatus.put(CI.size()-1, VehicleStatus.DELIVERY_PEOPLE);
			else if(I.getAction(i) == VehicleAction.STOP)
				taxi.mStatus.put(CI.size()-1, VehicleStatus.REST_AT_PARKING);
			
		}
		
		taxi.remainRequestIDs = new ArrayList<Integer>();
		for(int i = 0; i < ss.rids.length; i++)
			taxi.remainRequestIDs.add(ss.rids[i]);
		taxi.remainDistance = ss.distance;
		
		P = taxi.getFinalParking();
		if(P != null){
			P.load++;
		}
	
	}
	public void admitNewItinerary(Vehicle taxi, int nextStartTimePoint, int fromIndex, int fromPoint, ServiceSequence ss){
		int taxiID = 1;
		if(taxiID == taxi.ID)
			log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + " REST AT PARKING, " +
				"curPos = lastPoint = " + taxi.lastPoint + ", nextStartTimePoint = " + nextStartTimePoint + ", fromPoint = " +
					fromPoint + ", fromIndex = " + fromIndex + ", status = " + taxi.getStatusDescription(taxi.status) + ", T.current = " + T.currentTimePoint);
		if(taxi.status == VehicleStatus.REST_AT_PARKING){
			if(taxi.ID == taxi.ID){
				//System.out.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = " + curPos);
				//log.println("SimulatorBookedRequest::insertParcelRequest, taxi " + taxi.ID + " REST AT PARKING, " +
						//"curPos = lastPoint = " + taxi.lastPoint + ", nextStartTimePoint = " + nextStartTimePoint + ", fromPoint = " + fromPoint + ", fromIndex = " + fromIndex);
				//System.exit(-1);
			}
			taxi.mStatus.clear();
			taxi.mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
			taxi.status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			int[]  path = new int[1];
			ArrayList<Integer> requestID = new ArrayList<Integer>();
			ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
			path[0] = fromPoint;
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			taxi.currentItinerary = new ItineraryTravelTime(path, requestID, actions);
			
			taxi.lastIndexPoint = 0;
			
			taxi.addItinerary(taxi.currentItinerary);
		}
		
		pbts.entities.Parking P = taxi.getFinalParking();
		if(P != null){
			P.load--;
		}
		taxi.cancelSubItinerary(fromIndex+1);
		
		int curPos = fromPoint;
		int td = nextStartTimePoint;
		taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
		int lp = taxi.currentItinerary.get(taxi.currentItinerary.size()-1);
		if(taxiID == taxi.ID)
			log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + ", INIT setDepartureTime(" + lp + "," + td + ")" + ", T.currnet = " + T.currentTimePoint);
		
		int startI = 0;
		int firstLoc = getLocationFromEncodedRequest(ss.rids[0]);
		if(taxi.currentItinerary.get(taxi.currentItinerary.size()-1) == firstLoc && 
				taxi.currentItinerary.getRequestID(taxi.currentItinerary.size()-1) == Math.abs(ss.rids[0]))
			startI = 1;// fromPoint = first point of ss.rids then startI = 1 in order to avoid repeating point
		for(int i = startI; i < ss.rids.length; i++){
		//for(int i = 0; i < ss.rids.length; i++){
			int rid = ss.rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			PeopleRequest peopleReq = mPeopleRequest.get(arid);
			if(peopleReq != null){
				if(rid > 0) nextPoint = peopleReq.pickupLocationID; else nextPoint = peopleReq.deliveryLocationID;
				Itinerary I = dijkstra.queryShortestPath(curPos, nextPoint);
				int t = getTravelTime(I.getDistance(), maxSpeedms);
				double d = I.getDistance();
				int v0 = I.get(0);
				for(int j = 1; j < I.size()-1; j++){
					int v = I.get(j);
					
					taxi.currentItinerary.addPoint(v);
					taxi.currentItinerary.addAction(VehicleAction.PASS);
					taxi.currentItinerary.addRequestID(-1);
					
					Arc a = map.getArc(v0, v);
					int dt = (int)(t*a.w/d);
					td = td + dt;
					t = t - dt;
					d = d - a.w;
					v0 = v;
					taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
					taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
				}
				taxi.currentItinerary.addPoint(I.get(I.size()-1));
				taxi.currentItinerary.addRequestID(arid);
				td = td + t;
				taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
				lp = taxi.currentItinerary.get(taxi.currentItinerary.size()-1);
				if(taxiID == taxi.ID)
					log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + ", setArrivalTime(" + lp + "," + td + ")" + ", T.currnet = " + T.currentTimePoint);
				if(rid > 0){
					taxi.currentItinerary.addAction(VehicleAction.PICKUP_PEOPLE); 
					taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.PICKUP_PEOPLE);
					
					td = td + peopleReq.pickupDuration;
					taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
					if(taxiID == taxi.ID)
						log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + ", setDepartureTime(" + lp + "," + td + ")" + ", T.currnet = " + T.currentTimePoint);
				}
				else{
					taxi.currentItinerary.addAction(VehicleAction.DELIVERY_PEOPLE);
					taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.DELIVERY_PEOPLE);
					
					td = td + peopleReq.deliveryDuration;
					taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
					if(taxiID == taxi.ID)
						log.println("SimulatorBookedRequest::admitNewItinerary, taxi " + taxi.ID + ", setDepartureTime(" + lp + "," + td + ")" + ", T.currnet = " + T.currentTimePoint);
				}
			}else{
				ParcelRequest parcelReq = mParcelRequest.get(arid);
				if(rid > 0) nextPoint = parcelReq.pickupLocationID; else nextPoint = parcelReq.deliveryLocationID;
				Itinerary I = dijkstra.queryShortestPath(curPos, nextPoint);
				int t = getTravelTime(I.getDistance(), maxSpeedms);
				double d = I.getDistance();
				int v0 = I.get(0);
				for(int j = 1; j < I.size()-1; j++){
					int v = I.get(j);
					taxi.currentItinerary.addPoint(v);
					taxi.currentItinerary.addAction(VehicleAction.PASS);
					taxi.currentItinerary.addRequestID(-1);
					
					Arc a = map.getArc(v0, v);
					int dt = (int)(t*a.w/d);
					td = td + dt;
					t = t - dt;
					d = d - a.w;
					v0 = v;
					taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
					taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
				}
				taxi.currentItinerary.addPoint(I.get(I.size()-1));
				taxi.currentItinerary.addRequestID(arid);
				
				td = td + t;
				taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
				
				if(rid > 0){
					taxi.currentItinerary.addAction(VehicleAction.PICKUP_PARCEL); 
					taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.PICKUP_PARCEL);
					
					td = td + parcelReq.pickupDuration;
					taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
				}
				else{
					taxi.currentItinerary.addAction(VehicleAction.DELIVERY_PARCEL);
					taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.DELIVERY_PARCEL);
					
					td = td + parcelReq.deliveryDuration;
					taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
				}
			}
			curPos = nextPoint;
		}
		Itinerary I = dijkstra.queryShortestPath(curPos, ss.parkingLocationPoint);
		int t = getTravelTime(I.getDistance(), maxSpeedms);
		double d = I.getDistance();
		int v0 = I.get(0);
		
		for(int j = 1; j < I.size()-1; j++){
			int v = I.get(j);
			taxi.currentItinerary.addPoint(v);
			taxi.currentItinerary.addAction(VehicleAction.PASS);
			taxi.currentItinerary.addRequestID(-1);
			
			Arc a = map.getArc(v0, v);
			int dt = (int)(t*a.w/d);
			td = td + dt;
			t = t - dt;
			d = d - a.w;
			v0 = v;
			taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
			taxi.currentItinerary.setDepartureTime(taxi.currentItinerary.size()-1, td);
		}
		taxi.currentItinerary.addPoint(I.get(I.size()-1));
		taxi.currentItinerary.addRequestID(-1);
		taxi.currentItinerary.addAction(VehicleAction.STOP);
		taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.REST_AT_PARKING);
		td = td + t;
		taxi.currentItinerary.setArrivalTime(taxi.currentItinerary.size()-1, td);
		/*
		boolean ok = assignTimePoint(taxi.currentItinerary, taxi, fromIndex, nextStartTimePoint, insertedPeopleRequests, insertedParcelRequests);
		if(!ok){
			System.out.println("SimulatorBookedRequest::admitNewItinerary --> FAILED when assignTimePoint????");
			log.close();
			System.exit(-1);
			
		}
		*/
		taxi.remainRequestIDs = new ArrayList<Integer>();
		for(int i = 0; i < ss.rids.length; i++)
			taxi.remainRequestIDs.add(ss.rids[i]);
		taxi.remainDistance = ss.distance;
		
		P = taxi.getFinalParking();
		if(P != null){
			P.load++;
		}
	}
	/*
	public ServiceSequence computeBestProfitsParcelInsertion(Vehicle taxi, ParcelRequest pr){
		int startIdx = 0;
		int idxDelivery = -1;
		int rid = -1;
		PeopleRequest peopleReq = null;
		if(taxi.peopleReqIDonBoard.size() > 0){
			rid = taxi.peopleReqIDonBoard.get(0);
			peopleReq = mPeopleRequest.get(rid);
			
			for(int i = 0; i < taxi.remainRequestIDs.size(); i++){
				if(taxi.remainRequestIDs.get(i) == -rid){
					idxDelivery = i; break;
				}
			}
			if(countStop.get(rid) + idxDelivery >= peopleReq.maxNbStops){
				startIdx = idxDelivery+1;
			}
		}
		int nextStartPoint = taxi.getNextStartPoint();
		int startTimePoint = taxi.getNextStartTimePoint();
		
		int[] nod = new int[taxi.remainRequestIDs.size() + 2];
		// explore all possible position i1 (pickup), i2 (delivery) among taxi.remainRequestIDs for inserting pr
		ArrayList<Integer> parkings = collectAvailableParkings(taxi);
		double maxProfits = -dijkstra.infinity;
		//int sel_pickup_index = -1;
		//int sel_delivery_index = -1;
		//int sel_pk = -1;
		ServiceSequence ss = null;
		double expectDistanceParcel = dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
		for(int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++){
			for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
				// establish new sequence of request ids stored in nod
				if(rid > 0){
					if(i1 <= idxDelivery && i2 <= idxDelivery && countStop.get(rid) + idxDelivery + 2 > peopleReq.maxNbStops)
						continue;
				}
				int idx = -1;
				double profits = getParcelRevenue(expectDistanceParcel);
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
				double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod);
				if(D > dijkstra.infinity - 1) continue;// constraints are violated
				
				for(int k = 0; k < parkings.size(); k++){
					int pk = parkings.get(k);
					//double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod, pk);
					//if(D > dijkstra.infinity - 1) continue;// constraints are violated
					int endPoint = getLocationFromEncodedRequest(nod[nod.length-1]);
					D = D + dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - getCostFuel(extraDistance);
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
	*/
	public double computeRemainTravelDistance(int rid, int[] nod, int startPoint){
		double D = 0;
		int curPoint = startPoint;
		int nextPoint = -1;
		for(int i = 0; i < nod.length; i++){
			int arid = Math.abs(nod[i]);
			PeopleRequest peopleReq = mPeopleRequest.get(arid);
			if(peopleReq != null){
				if(nod[i] > 0) nextPoint = peopleReq.pickupLocationID; else nextPoint = peopleReq.deliveryLocationID;
			}else{
				ParcelRequest parcelReq = mParcelRequest.get(arid);
				if(nod[i] > 0) nextPoint = parcelReq.pickupLocationID; else nextPoint = parcelReq.deliveryLocationID;
			}
			double di = dijkstra.queryDistance(curPoint, nextPoint);
			D = D + di;
			if(nod[i] == -rid) break;// reach delivery point corresponding with rid
			curPoint = nextPoint;
		}
		return D;
	}
	/*
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
		ArrayList<Integer> parkings = collectAvailableParkings(taxi);
		//double minExtraDistance = dijkstra.infinity;
		//int sel_pickup_index = -1;
		//int sel_delivery_index = -1;
		//int sel_pk = -1;
		double maxProfits = -dijkstra.infinity;
		ServiceSequence ss = null;
		double expectDistancePeople = dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
		for(int i1 = startIdx; i1 <= taxi.remainRequestIDs.size(); i1++){
			int max = taxi.remainRequestIDs.size() < i1 + pr.maxNbStops ? taxi.remainRequestIDs.size() : i1 + pr.maxNbStops;
			for(int i2 = i1; i2 <= taxi.remainRequestIDs.size(); i2++){
			//for(int i2 = i1; i2 <= i1; i2++){
				// establish new sequence of request ids stored in nod
				int idx = -1;
				int pickup_idx = -1;
				int delivery_idx = -1;
				double profits = getPeopleRevenue(expectDistancePeople);
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
					int u = getLocationFromEncodedRequest(nod[k1]);
					int v = getLocationFromEncodedRequest(nod[k1+1]);
					distancePeople = distancePeople + dijkstra.queryDistance(u,v);
				}
				if(distancePeople > pr.maxTravelDistance) continue;
				
				// check if travel distance of passenger on board exceeds maximum distance allowed
				boolean ok = true;
				for(int k1 = 0; k1 < taxi.peopleReqIDonBoard.size(); k1++){
					int pobReqID = taxi.peopleReqIDonBoard.get(k1);
					PeopleRequest pR = mPeopleRequest.get(pobReqID);
					double d1 = computeRemainTravelDistance(pobReqID,nod,nextStartPoint);
					Arc A = map.getArc(taxi.lastPoint, nextStartPoint);
					double d2 = d1 + accumulateDistance.get(pobReqID);
					if(A != null) d2 = d2 + A.w;
					if(d2 > pR.maxTravelDistance){
						ok = false; break;
					}
				}
				if(!ok) continue;
				
				if(pickup_idx < delivery_idx - 1)// not direct delivery
					profits = profits - getDiscount(expectDistancePeople, distancePeople);
				
				// evaluate the insertion
				
				double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod);
				if(D > dijkstra.infinity - 1) continue;// constraints are violated
				
				for(int k = 0; k < parkings.size(); k++){
					int pk = parkings.get(k);
					//double D = computeFeasibleDistance(nextStartPoint, startTimePoint, nod, pk);
					//if(D > dijkstra.infinity - 1) continue;// constraints are violated
					int endPoint = getLocationFromEncodedRequest(nod[nod.length-1]);
					D = D + dijkstra.queryDistance(endPoint, pk);
					double extraDistance = D - taxi.remainDistance;
					profits = profits - getCostFuel(extraDistance);
					
					if(profits > maxProfits){
						maxProfits = profits;
						//sel_pickup_index = i1;
						//sel_delivery_index = i2;
						//sel_pk = pk;
						ss = new ServiceSequence(nod,profits,pk,D);
						
						System.out.println("SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = " + ss.profitEvaluation +
								", sequence = " + ss.getSequence() + ", distance D = " + ss.distance);
						log.println("SimulatorBookedRequest::computeBestPeopleInsertion, UPDATE ss = " + ss.profitEvaluation +
								", sequence = " + ss.getSequence() + ", distance D = " + ss.distance);
								
					}
				}
			}
		}
		
		return ss;
	}
	*/
	/*
	public double computeFeasibleDistance(int startPoint, int startTimePoint, int[] rids, int parkingLoc){
		// feasible distance of itinerary starting from startPoint, traversing rids (+ means pickup and 0 means delivery) 
		// and back to a nearest available parking
		// return infinity if the itinerary violates time window constraints
		System.out.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = " + startPoint + ", startTimePoint = " + 
		startTimePoint + ", rids = ");
		for(int i = 0; i < rids.length; i++) System.out.print(rids[i] + ",");
		System.out.println();
		log.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = " + startPoint + ", startTimePoint = " + 
				startTimePoint + ", rids = ");
				for(int i = 0; i < rids.length; i++) log.print(rids[i] + ",");
				log.println();
				
		double distance = 0;
		int curPoint = startPoint;
		int td = startTimePoint;
		for(int i = 0; i < rids.length; i++){
			int rid = rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			int ta = -1;
			ParcelRequest parcelReq = mParcelRequest.get(arid);
			if(parcelReq != null){
				if(rid > 0) nextPoint = parcelReq.pickupLocationID; else nextPoint = parcelReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if(rid > 0){
					if(ta > parcelReq.latePickupTime) return dijkstra.infinity;
					td = ta + parcelReq.pickupDuration;
				}else{
					if(ta > parcelReq.lateDeliveryTime) return dijkstra.infinity;
					td = ta + parcelReq.deliveryDuration;
				}
				System.out.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + 
				td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
				log.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
			}else{
				PeopleRequest peopleReq = mPeopleRequest.get(arid);
				if(rid > 0) nextPoint = peopleReq.pickupLocationID; else nextPoint = peopleReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if(rid > 0){
					if(ta > peopleReq.latePickupTime) return dijkstra.infinity;
					td = ta + peopleReq.pickupDuration;
				}else{
					if(ta > peopleReq.lateDeliveryTime) return dijkstra.infinity;
					td = ta + peopleReq.deliveryDuration;
				}
				System.out.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
				log.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
			}
			curPoint = nextPoint;
		}
		distance = distance + dijkstra.queryDistance(curPoint, parkingLoc);
		return distance;
	}
	*/
	public double computeFeasibleDistance(int startPoint, int startTimePoint, int[] rids){
		// feasible distance of itinerary starting from startPoint, traversing rids ('+' means pickup and '-' means delivery) 
		// and back to a nearest available parking
		// return infinity if the itinerary violates time window constraints
		/*
		System.out.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = " + startPoint + ", startTimePoint = " + 
		startTimePoint + ", rids = ");
		for(int i = 0; i < rids.length; i++) System.out.print(rids[i] + ",");
		System.out.println();
		log.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = " + startPoint + ", startTimePoint = " + 
				startTimePoint + ", rids = ");
				for(int i = 0; i < rids.length; i++) log.print(rids[i] + ",");
				log.println();
		*/		
		ArrayList<Integer> stack = new ArrayList<Integer>();
		
		double distance = 0;
		int curPoint = startPoint;
		int td = startTimePoint;
		int countStop = -1;
		for(int i = 0; i < rids.length; i++){
			int rid = rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			int ta = -1;
			ParcelRequest parcelReq = mParcelRequest.get(arid);
			if(parcelReq != null){
				if(rid > 0) nextPoint = parcelReq.pickupLocationID; else nextPoint = parcelReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				//System.out.println(name() + "::computeFeasibleDistance, ParcelReq distance from " + curPoint + " to " + nextPoint + " Di = "+ Di);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if(rid > 0){
					td = ta + parcelReq.pickupDuration;
					if((ta > parcelReq.latePickupTime || ta < parcelReq.earlyPickupTime) && 
							(td > parcelReq.latePickupTime || td < parcelReq.earlyPickupTime)){
						//System.out.println(name() + "::computeFeasibleDistance, pickup rid = " + rid + ", ta = " + ta + 
								//", earlyPickupTime = " + parcelReq.earlyPickupTime + ", latePickupTime = " + parcelReq.latePickupTime + 
								//", earlyDeliveryTime = " + parcelReq.earlyDeliveryTime + ", lateDeliveryTime = " + parcelReq.lateDeliveryTime + " --> return infinity");
						return dijkstra.infinity;
					}
					//ta = td + getTravelTimeSegments(curPoint, nextPoint);
					//if(ta > parcelReq.latePickupTime) return dijkstra.infinity;
					
				}else{
					td = ta + parcelReq.deliveryDuration;
					if((ta > parcelReq.lateDeliveryTime || ta < parcelReq.earlyDeliveryTime) &&
							(td > parcelReq.lateDeliveryTime || td < parcelReq.earlyDeliveryTime)){
						//System.out.println(name() + "::computeFeasibleDistance, delivery rid = "+ rid + ", ta = " + ta + 
								//", earlyPickupTime = " + parcelReq.earlyPickupTime + ", latePickupTime = " + parcelReq.latePickupTime + 
								//", earlyDeliveryTime = " + parcelReq.earlyDeliveryTime + ", lateDeliveryTime = " + parcelReq.lateDeliveryTime + " --> return infinity");
						
						return dijkstra.infinity;
					}
					//ta = td + getTravelTimeSegments(curPoint, nextPoint);
					//if(ta > parcelReq.lateDeliveryTime) return dijkstra.infinity;
					
				}
				/*
				System.out.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + 
				td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
				log.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
						*/
				
				if(countStop >= 0){
					countStop++;
				
					int r = stack.get(0);
					PeopleRequest pr = mPeopleRequest.get(r);
					if(countStop > pr.maxNbStops){
						//System.out.println(name() + "::computeFeasibleDistance, countStop = " + countStop + " > pr.maxNbStops = " + pr.maxNbStops + " --> return infinity");
						return dijkstra.infinity;
					}
					//if(countStop > 0) return dijkstra.infinity;
				}
			}else{
				PeopleRequest peopleReq = mPeopleRequest.get(arid);
				if(rid > 0) nextPoint = peopleReq.pickupLocationID; else nextPoint = peopleReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if(rid > 0){
					td = ta + peopleReq.pickupDuration;
					if(td > peopleReq.latePickupTime || td < peopleReq.earlyPickupTime) return dijkstra.infinity;
					//ta = td + getTravelTimeSegments(curPoint, nextPoint);
					//if(ta > peopleReq.latePickupTime) return dijkstra.infinity;
					
					
					if(stack.size() > 0) return dijkstra.infinity;
					stack.add(rid);
					countStop = 0;
				}else{
					td = ta + peopleReq.deliveryDuration;
					if(ta > peopleReq.lateDeliveryTime || ta < peopleReq.earlyDeliveryTime) return dijkstra.infinity;
					//ta = td + getTravelTimeSegments(curPoint, nextPoint);
					//if(ta > peopleReq.latePickupTime) return dijkstra.infinity;
					
					
					if(stack.size() > 0){
						if(stack.get(stack.size()-1) == -rid){
							stack.remove(stack.size()-1);
							countStop = -1;
						}
					}
				}
				/*
				System.out.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
				log.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
						*/
			}
			curPoint = nextPoint;
		}
		
		return distance;
	}
	public double computeFeasibleDistance(int startPoint, int startTimePoint, ArrayList<Integer> rids){
		// feasible distance of itinerary starting from startPoint, traversing rids ('+' means pickup and '-' means delivery) 
		// and back to a nearest available parking
		// return infinity if the itinerary violates time window constraints
		/*
		System.out.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = " + startPoint + ", startTimePoint = " + 
		startTimePoint + ", rids = ");
		for(int i = 0; i < rids.length; i++) System.out.print(rids[i] + ",");
		System.out.println();
		log.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = " + startPoint + ", startTimePoint = " + 
				startTimePoint + ", rids = ");
				for(int i = 0; i < rids.length; i++) log.print(rids[i] + ",");
				log.println();
		*/		
		ArrayList<Integer> stack = new ArrayList<Integer>();
		
		double distance = 0;
		int curPoint = startPoint;
		int td = startTimePoint;
		int countStop = -1;
		for(int i = 0; i < rids.size(); i++){
			int rid = rids.get(i);
			int arid = Math.abs(rid);
			int nextPoint = -1;
			int ta = -1;
			ParcelRequest parcelReq = mParcelRequest.get(arid);
			if(parcelReq != null){
				if(rid > 0) nextPoint = parcelReq.pickupLocationID; else nextPoint = parcelReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if(rid > 0){
					td = ta + parcelReq.pickupDuration;
					if((ta > parcelReq.latePickupTime || ta < parcelReq.earlyPickupTime) && 
							(td > parcelReq.latePickupTime || td < parcelReq.earlyPickupTime)) return dijkstra.infinity;
					//ta = td + getTravelTimeSegments(curPoint, nextPoint);
					//if(ta > parcelReq.latePickupTime) return dijkstra.infinity;
					
				}else{
					td = ta + parcelReq.deliveryDuration;
					if((ta > parcelReq.lateDeliveryTime || ta < parcelReq.earlyDeliveryTime) &&
							(td > parcelReq.lateDeliveryTime || td < parcelReq.earlyDeliveryTime)) return dijkstra.infinity;
					//ta = td + getTravelTimeSegments(curPoint, nextPoint);
					//if(ta > parcelReq.lateDeliveryTime) return dijkstra.infinity;
					
				}
				/*
				System.out.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + 
				td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
				log.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
						*/
				
				if(countStop >= 0){
					countStop++;
				
					int r = stack.get(0);
					PeopleRequest pr = mPeopleRequest.get(r);
					if(countStop > pr.maxNbStops) return dijkstra.infinity;
					//if(countStop > 0) return dijkstra.infinity;
				}
			}else{
				PeopleRequest peopleReq = mPeopleRequest.get(arid);
				if(rid > 0) nextPoint = peopleReq.pickupLocationID; else nextPoint = peopleReq.deliveryLocationID;
				double Di = dijkstra.queryDistance(curPoint, nextPoint);
				distance = distance + Di;
				ta = td + getTravelTime(Di, maxSpeedms);
				if(rid > 0){
					td = ta + peopleReq.pickupDuration;
					if(td > peopleReq.latePickupTime || td < peopleReq.earlyPickupTime) return dijkstra.infinity;
					//ta = td + getTravelTimeSegments(curPoint, nextPoint);
					//if(ta > peopleReq.latePickupTime) return dijkstra.infinity;
					
					
					if(stack.size() > 0) return dijkstra.infinity;
					stack.add(rid);
					countStop = 0;
				}else{
					td = ta + peopleReq.deliveryDuration;
					if(ta > peopleReq.lateDeliveryTime || ta < peopleReq.earlyDeliveryTime) return dijkstra.infinity;
					//ta = td + getTravelTimeSegments(curPoint, nextPoint);
					//if(ta > peopleReq.latePickupTime) return dijkstra.infinity;
					
					
					if(stack.size() > 0){
						if(stack.get(stack.size()-1) == -rid){
							stack.remove(stack.size()-1);
							countStop = -1;
						}
					}
				}
				/*
				System.out.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
				log.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
						*/
			}
			curPoint = nextPoint;
		}
		
		return distance;
	}
	
	public boolean checkDirectPeopleServices(int[] rids){
		// return true if all people services are direct
		/*
		System.out.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = " + startPoint + ", startTimePoint = " + 
		startTimePoint + ", rids = ");
		for(int i = 0; i < rids.length; i++) System.out.print(rids[i] + ",");
		System.out.println();
		log.print("SimulatorBookedRequest::computeFeasibleDistance(startPoint = " + startPoint + ", startTimePoint = " + 
				startTimePoint + ", rids = ");
				for(int i = 0; i < rids.length; i++) log.print(rids[i] + ",");
				log.println();
		*/		
		ArrayList<Integer> stack = new ArrayList<Integer>();
		
		
		int countStop = -1;
		for(int i = 0; i < rids.length; i++){
			int rid = rids[i];
			int arid = Math.abs(rid);
			int nextPoint = -1;
			int ta = -1;
			ParcelRequest parcelReq = mParcelRequest.get(arid);
			if(parcelReq != null){
				/*
				System.out.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + 
				td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
				log.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", parcelReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
						*/
				
				if(countStop >= 0){
					countStop++;
				
					int r = stack.get(0);
					PeopleRequest pr = mPeopleRequest.get(r);
					//if(countStop > pr.maxNbStops) return dijkstra.infinity;
					if(countStop > 0) return false;
				}
			}else{
				PeopleRequest peopleReq = mPeopleRequest.get(arid);
				
				if(rid > 0){
					
					if(stack.size() > 0) return false;
					stack.add(rid);
					countStop = 0;
				}else{
					
					
					if(stack.size() > 0){
						if(stack.get(stack.size()-1) == -rid){
							stack.remove(stack.size()-1);
							countStop = -1;
						}
					}
				}
				
				/*
				System.out.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
				log.println("SimulatorBookedRequest::computeFeasibleDistance, i = " + i + ", peopleReq  = " + rid + ", ta = " + ta + ", new td = " + 
						td + ", curPoint = " + curPoint + ", nextPoint = " + nextPoint + ", Di = " + Di);
						*/
			}
			
		}
		
		return true;
	}

	public boolean checkDirectParcelServices(int[] rids){
		// return true if all parcel services are direct
		for(int i = 0; i < rids.length; i++){
			int arid = Math.abs(rids[i]);
			ParcelRequest pr = mParcelRequest.get(arid);
			if(pr != null){
				if(i+1 < rids.length){
					if(rids[i] > 0 && rids[i] + rids[i+1] != 0) return false;
				}
			}
		}
		/*
		int i = 0;
		if(rids.length % 2 == 1) i = 1;
		while(i < rids.length){
			boolean ok = rids[i] + rids[i+1] == 0 && rids[i] > 0 && rids[i+1] < 0;
			if(!ok) return false;
			i = i + 2;
		}
		*/
		return true;
	}

	public boolean insertParcelRequest(Vehicle taxi, ParcelRequest pr, int fromIdx){
		int curPos = taxi.lastPoint;
		int t = T.currentTimePoint;
		int taxiID = 11;
		if(taxi.ID == taxiID){
			if(taxi.currentItinerary != null){
				log.println("SimulatorBookedRequests::insertParcelRequest, taxi = " + taxi.ID + ", status = " + 
			taxi.getStatusDescription(taxi.status) + ", T.current = " + T.currentTimePoint + ", currentItinerary = ");
				taxi.currentItinerary.writeToFile(log);
				System.out.println("SimulatorBookedRequests::insertParcelRequest, taxi = " + taxi.ID + ", parcel request id = " + pr.id + ", T.current = " + t + ", fromIdx = " + fromIdx + ", " +
					"lastIndexPoint = " + taxi.lastIndexPoint + ", lastPoint = " + taxi.lastPoint + ", depTime at formIdx = " + taxi.currentItinerary.getDepartureTime(fromIdx));
				log.println("SimulatorBookedRequests::insertParcelRequest, taxi = " + taxi.ID + ", status = " + 
						taxi.getStatusDescription(taxi.status) + ", T.current = " + T.currentTimePoint + ", parcel request id = " + pr.id + ", T.current = " + t + ", fromIdx = " + fromIdx + ", " +
					"lastIndexPoint = " + taxi.lastIndexPoint + ", lastPoint = " + taxi.lastPoint + ", depTime at formIdx = " + taxi.currentItinerary.getDepartureTime(fromIdx));
	
			}
		}
		if(taxi.status == VehicleStatus.REST_AT_PARKING){
			//if(taxi.ID == 161){
				//System.out.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = " + curPos);
				//log.println("SimulatorBookedRequest::insertParcelRequest, taxi 161 REST AT PARKING, curPos = lastPoint = " + curPos);
				//System.exit(-1);
			//}
			taxi.mStatus.clear();
			taxi.mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
			taxi.status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			int[]  path = new int[1];
			ArrayList<Integer> requestID = new ArrayList<Integer>();
			ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
			path[0] = curPos;
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			taxi.currentItinerary = new ItineraryTravelTime(path, requestID, actions);
			
			//taxi.currentItinerary.addPoint(curPos);
			//taxi.currentItinerary.addAction(VehicleAction.PASS);
			//taxi.currentItinerary.addRequestID(-1);
			
			taxi.lastIndexPoint = 0;
			fromIdx = 0;
			taxi.addItinerary(taxi.currentItinerary);
			System.out.println("SimulatorBookedRequests::insertParcelRequest, setup  new itinerary for taxi " + taxi.ID);
			if(taxi.ID == taxiID)log.println("SimulatorBookedRequests::insertParcelRequest, status = REST_AT_PARKING --> setup  new itinerary for taxi " + taxi.ID);
			pbts.entities.Parking P = findParking(taxi.lastPoint);
			if(P != null){
				P.load--;
				P.lastUpdateTimePoint = T.currentTimePoint;
			}
		}else{
			if(fromIdx >= 0){
				int locID = taxi.currentItinerary.get(taxi.currentItinerary.size()-1);
				pbts.entities.Parking P = findParking(locID);
				if(P != null){
					P.load--;
				}
				System.out.println("SimulatorBookedRequests::insertParcelRequest, cancelSubItinerary(" + fromIdx + " + 1), of taxi " + taxi.ID + 
						", size of currentItinerary  = " + taxi.currentItinerary.size());
				if(taxi.ID == taxiID){
					log.println("SimulatorBookedRequests::insertParcelRequest, cancelSubItinerary(" + fromIdx + " + 1), of taxi " + taxi.ID + 
							", size of currentItinerary  = " + taxi.currentItinerary.size() + ", before cancel, I = ");
					taxi.currentItinerary.writeToFile(log);
				}
				t = taxi.currentItinerary.getDepartureTime(fromIdx);
				if(t < 0){// fromIdx will be a parking
					t = taxi.currentItinerary.getArrivalTime(fromIdx);
					if(taxi.currentItinerary.getAction(fromIdx) != VehicleAction.STOP){
						log.println("SimulateBookedRequest::insertParcelRequest, BUG AT STOP????, fromIdx = " + fromIdx + ", currentItinerary.sz = " + taxi.currentItinerary.size());
						
					}
				}
				taxi.cancelSubItinerary(fromIdx+1);
				
				curPos = taxi.currentItinerary.get(fromIdx);
				if(taxi.ID == taxiID){
					log.println("SimulatorBookedRequests::insertParcelRequest, cancelSubItinerary(" + fromIdx + " + 1), of taxi " + taxi.ID + 
							", size of currentItinerary  = " + taxi.currentItinerary.size() + ", after cancel, I = ");
					taxi.currentItinerary.writeToFile(log);
				}
				//if(taxi.ID == 161){
					//log.println("SimulatorBookedRequests::insertParcelRequest, taxi 161, fromIdx = " + fromIdx + ", curPos = " + curPos);
				//}
				
			}
		}
		
		Itinerary I1 = dijkstra.queryShortestPath(curPos, pr.pickupLocationID);
		Itinerary I2 = dijkstra.queryShortestPath(pr.pickupLocationID, pr.deliveryLocationID);
		double minD = dijkstra.infinity;
		pbts.entities.Parking sel_P = null;
		for(int k = 0; k < lstParkings.size(); k++){
			pbts.entities.Parking P = lstParkings.get(k);
			if(P.load < P.capacity){
				double D = dijkstra.queryDistance(pr.deliveryLocationID, P.locationID);
				if(D < minD){
					minD = D;
					sel_P = P;
				}
			}
		}
		int destinationLocationID = mTaxi2Depot.get(taxi.ID);
		if(sel_P != null){
			sel_P.load++;
			sel_P.lastUpdateTimePoint = T.currentTimePoint;
			destinationLocationID  = sel_P.locationID;
		}
		Itinerary I3 = dijkstra.queryShortestPath(pr.deliveryLocationID, destinationLocationID);
		
		for(int i = 1; i < I1.size()-1; i++){
			taxi.currentItinerary.addPoint(I1.get(i));
			taxi.currentItinerary.addAction(VehicleAction.PASS);
			taxi.currentItinerary.addRequestID(-1);
		}
		taxi.currentItinerary.addPoint(pr.pickupLocationID);
		taxi.currentItinerary.addAction(VehicleAction.PICKUP_PARCEL);
		taxi.currentItinerary.addRequestID(pr.id);
		taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.PICKUP_PARCEL);
		//if(taxi.ID == 161){
			//log.println("SimulatorBookedRequests::insertParcelRequest, taxi 161, SET PICKUP_PARCEL at point " + pr.pickupLocationID);
		//}
		for(int i = 1; i < I2.size()-1; i++){
			taxi.currentItinerary.addPoint(I2.get(i));
			taxi.currentItinerary.addAction(VehicleAction.PASS);
			taxi.currentItinerary.addRequestID(-1);
		}
		taxi.currentItinerary.addPoint(pr.deliveryLocationID);
		taxi.currentItinerary.addAction(VehicleAction.DELIVERY_PARCEL);
		taxi.currentItinerary.addRequestID(pr.id);
		taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.DELIVERY_PARCEL);
		//if(taxi.ID == 161){
			//log.println("SimulatorBookedRequests::insertParcelRequest, taxi 161, SET DELIVERY_PARCEL at point " + pr.deliveryLocationID);
		//}
		for(int i = 1; i < I3.size()-1; i++){
			taxi.currentItinerary.addPoint(I3.get(i));
			taxi.currentItinerary.addAction(VehicleAction.PASS);
			taxi.currentItinerary.addRequestID(-1);
		}
		taxi.currentItinerary.addPoint(I3.get(I3.size()-1));
		taxi.currentItinerary.addAction(VehicleAction.STOP);
		taxi.currentItinerary.addRequestID(-1);
		taxi.mStatus.put(taxi.currentItinerary.size()-1, VehicleStatus.REST_AT_PARKING);
		//if(taxi.ID == 161){
			//log.println("SimulatorBookedRequests::insertParcelRequest, taxi 161, SET REST_AT_PARKING at point " + I3.get(I3.size()-1) + " = " + sel_P.locationID);
		//}
		//ArrayList<PeopleRequest> peopleReq = new ArrayList<PeopleRequest>();
		//ArrayList<ParcelRequest> parcels = new ArrayList<ParcelRequest>();
		//parcels.add(pr);
		
		assignTimePoint(taxi.currentItinerary, taxi, fromIdx, t+1, insertedPeopleRequests, insertedParcelRequests);
		for(int i = fromIdx+1; i < taxi.currentItinerary.size(); i++){
			if(taxi.currentItinerary.getArrivalTime(i) < 0){
				System.out.println("SimulatorBookedRequests::insertParcelRequest, FAILED assignTimePoint taxi " + taxi.ID + ", at T = " + T.currentTimePoint + 
						", arrTime(" + i + ") < 0, fromIdx = " + fromIdx + ", Itinerary.sz = " + taxi.currentItinerary.size());
				taxi.currentItinerary.writeToFile(log);
				log.close();
				System.exit(-1);
			}
		}
		if(taxi.ID == taxiID){
			log.println("SimulatorBookedRequests::insertParcelRequest, taxi " + taxi.ID + ", At time T.current = " + T.currentTimePoint + ", setup Itinerary: ");
			taxi.currentItinerary.writeToFile(log);
		}
		return false;
	}
	/*
	public void processParcelRequest(ParcelRequest pr){
		
		ServiceSequence sel_ss = null;
		Vehicle sel_taxi = null;
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			if(taxi.remainRequestIDs.size() > maxPendingStops) continue;
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
		admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);
	}
	public void processPeopleRequest(PeopleRequest pr){
		ServiceSequence sel_ss = null;
		Vehicle sel_taxi = null;
		int taxiID = 1;
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			if(taxi.remainRequestIDs.size() > maxPendingStops) continue;
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
		admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);
		
		//admitNewItinerary(sel_taxi, nextStartTimePoint, fromIndex, fromPoint, sel_ss);
	}
	*/
	public void simulateDataFromFile(String requestFilename, int maxNbParcelsInserted, int maxNbStops){
		double t0 = System.currentTimeMillis();
		loadRequests(requestFilename);
		//System.exit(-1);
		
		
		//if(true) return;
		
		int Th = terminateWorkingTime - startWorkingTime;
		T = new TimeHorizon(startWorkingTime,terminateRequestTime,terminateWorkingTime);
		
		
		pendingParcelRequests = new ArrayList<ParcelRequest>();
		
		initVehicles();
		
		
		
		distanceRequests = new ArrayList<Double>();
		
		//EventGenerator eg = new EventGenerator(map,T);
		//eg.log = log;
		System.out.println("T = " + T + ", nbTaxis = " + nbTaxis);
		//if(true) return;
		
		//for(int t = startWorkingTime; t < terminateWorkingTime; t++){
		boolean ok = false;
		PeopleRequest peopleR = null;
		ParcelRequest parcelR = null;
		
		for(int i = 0; i < allPeopleRequests.size(); i++){
			PeopleRequest pr = allPeopleRequests.get(i);
			pr.maxNbStops = pr.maxNbStops < maxNbStops ? pr.maxNbStops : maxNbStops;
			runningPeopleRequests.add(pr);
			//System.out.println(pr.id + ", timePoint = " + pr.timePoint);
			//if(pr.timePoint < 0){
				
				//System.exit(-1);
			//}
		}
		for(int i = 0; i < allParcelRequests.size(); i++){
			ParcelRequest pr = allParcelRequests.get(i);
			
			runningParcelRequests.add(pr);
		}
		
		if(runningPeopleRequests.size() > 0){
			peopleR = runningPeopleRequests.get(0);
			runningPeopleRequests.remove(0);
		}
		if(runningParcelRequests.size() > 0){
			parcelR = runningParcelRequests.get(0);
			runningParcelRequests.remove(0);
		}
		insertedParcelRequests = new ArrayList<ParcelRequest>();
		insertedPeopleRequests = new ArrayList<PeopleRequest>();
		
		while(!T.finished()){
		//for(int t = startWorkingTime; t < startWorkingTime+3600*2; t++){
			//log.println("time point " + t);
			int t = T.currentTimePoint;
			//System.out.println("SimulatorBookedRequests::simulateFromFile, t = " + t + ", T.end = " + T.end);
			for(int k = 0; k < nbTaxis; k++){
				Vehicle vh = vehicles.get(k);
				if(vh.status != VehicleStatus.STOP_WORK)
					vh.move();
				
			}
			
			if(T.stopRequest()){
				//System.out.println("Simulator::simulate stop request!!!!");
				for(int k = 0; k < nbTaxis; k++){
					Vehicle vh = vehicles.get(k);
					//if(vh.ID == 4){
						//System.out.println("Simulator::simulateDataFromFile, taxi " + vh.ID + ", stopRequest, status = " + Vehicle.getStatusDescription(vh.status));
						//System.exit(-1);
					//}
					if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
						System.out.println("Simulator::simulateDataFromFile, taxi " + vh.ID + ", stopRequest, status = TRAVEL_WITHOUT_LOAD --> GO BACK TO DEPOT");
						setupRouteBack2Depot(vh);
					}else if(vh.status == VehicleStatus.REST_AT_PARKING){
						if(vh.lastPoint != mTaxi2Depot.get(vh.ID)){
							System.out.println("Simulator::simulateDataFromFile, taxi " + vh.ID + ", stopRequest, status = REST_AT_PARKING --> GO BACK TO DEPOT");
							setupRouteBack2Depot(vh);
						}else{
							vh.status = VehicleStatus.STOP_WORK;
						}
					}
				}
			}
			
			
			if(parcelR != null)if(parcelR.timePoint == t){
				totalParcelRequests++;
				ParcelRequest pr = parcelR;
				//allParcelRequests.add(pr);
				System.out.println("Simulator::simulateDataFromFile --> At " + T.currentTimePointHMS() + "[" + t + "], parcel request " + pr.id + " arrives");
				Itinerary I = dijkstra.solve(pr.pickupLocationID, pr.deliveryLocationID);
				//if(I.getDistance() >= dijkstra.infinity){
				if(I == null){
					log.println("At " + T.currentTimePointHMS() + ", cannot serve parcel request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID);
					System.out.println("Simulator::simulateDataFromFile --> At " + T.currentTimePointHMS() + ", cannot serve parcel request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID);
					nbDisconnectedRequests++;
				}else{
					//pendingParcelRequests.add(pr);
					
					/*
					// find appropriate taxi for assigning this parcel request
					
					TaxiIndex ti = findAppropriateTaxiForInsertion(pr);
					//int fromIdx = taxi.currentItinerary.findLastDeliveryIndexPoint(taxi.lastIndexPoint);
					if(ti == null){
						System.out.println("Simulator::simulateDataFromFile --> At " + T.currentTimePointHMS() + "[" + t + "], " +
								"cannot find any appropriate taxi for parcel request insertion");
					}else{
						boolean hasInsertion = insertParcelRequest(ti.taxi,pr,ti.index);
						insertedParcelRequests.add(pr);
					}
					*/
					planner.processParcelRequest(pr);
				}
				if(runningParcelRequests.size() > 0){
					parcelR = runningParcelRequests.get(0);
					runningParcelRequests.remove(0);
				}
			}
			
			//System.out.println("peopleReq.timePoint = " + peopleR.timePoint + ", t = " + t);
			
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
					/*
					Vehicle taxi = findPossibleTaxi(pr.pickupLocationID, pr.deliveryLocationID, maxWaitTime);
					if(taxi != null){
						//System.out.println("Simulator::simulate --> found taxi " + taxi + "!!!!!!!!!!!!!!!!!!!!!!!!!!");
						System.out.println("Simulator::simulateDataFromFile --> At " + T.currentTimePointHMS() + ", request " + pr.id + " from " + pr.pickupLocationID + " --> " + pr.deliveryLocationID + ", found taxi ID = " + taxi.ID + ", description = " + taxi);
						log.println("At " + T.currentTimePointHMS() + ", found taxi " + taxi + "!!!!!!!!!!!!!!!!!!!!!!!!!!");
						log.println("At " + T.currentTimePointHMS() + ", request " + pr.pickupLocationID + " --> " + pr.deliveryLocationID + ", found taxi " + taxi.ID);
						// try to insert parcel requests
						
						//setupRoute(taxi, t, pr.pickupLocationID, pr.deliveryLocationID);
						//setupRoute(taxi,t,pr);
						//boolean hasRoute = setupRouteWithAParcelFollow(taxi, t, pr);
						boolean hasRoute = setupRouteWithParcelsInserted(taxi, t, pr, maxNbParcelsInserted); 
						double cur_t = (System.currentTimeMillis() - t0)*0.001;
						System.out.println("Simulator::simulateFromFile, t = " + cur_t);		
						if(hasRoute){
							acceptedPeopleRequests++;
							revenue = revenue + alpha + gamma1*D;//I.getDistance();
							distanceRequests.add(D);
							insertedPeopleRequests.add(pr);
							ok = true;
						}
					}else{
						System.out.println("Simulator::simulateDataFromFile --> At " + T.currentTimePointHMS() + ", Request (" + pr.pickupLocationID + "," + pr.deliveryLocationID + ") is rejected ???????????????????????, no taxi available");
						log.println("At " + T.currentTimePointHMS() + ", Request (" + pr.pickupLocationID + "," + pr.deliveryLocationID + ") is rejected ???????????????????????, no taxi available");
					}
					*/
					planner.processPeopleRequest(pr);
				}
				if(runningPeopleRequests.size() > 0){
					peopleR = runningPeopleRequests.get(0);
					runningPeopleRequests.remove(0);
				}
				
			}
			
			
			
			
			T.move();
			//log.println("-------------------");
		}
		
		double totalDistance = 0;
		int nbUnusedTaxis = 0;
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			if(taxi.totalTravelDistance <= 0){
				nbUnusedTaxis++;
				continue;
			}
			//taxi.writeItinerriesToLog();
			totalDistance = totalDistance + taxi.totalTravelDistance;
			int costi = (int)(taxi.totalTravelDistance*gamma3/1000);
			log.println("distance of taxi[" + taxi.ID + "] = " + taxi.totalTravelDistance/1000 + "km, cost fuel = " + costi + "K");
		}
		//logI.println(-2);
		
		for(int i = 0; i < distanceRequests.size(); i++){
			double D = distanceRequests.get(i);
			int m = (int)(alpha + gamma1*D);
			D = D/1000;
			m = m/1000;
			log.println("requests " + i + " has distance = " + D + "km, money = " + m + "K");
		}
			
		cost = (int)totalDistance*gamma3;
		cost = cost/1000;
		totalDistance = totalDistance/1000;
		revenue = revenue/1000;
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
		
		System.out.println("Simulator::simulateWithAParcelFollow --> FINISHED, allPeopleRequests.sz = " + allPeopleRequests.size() + 
				", allParcelRequests.sz = " + allParcelRequests.size());

		double t = System.currentTimeMillis() - t0;
		t = t * 0.001;
		System.out.println("simulation time = " + t);
	}
	

	public static void runBatch(){
		int prob[] = {10,3,2,1};
		int start_idx[] = {1,1,1,1};
		int end_idx[]   = {1,1,5,5};
		int nbIns = 5;
		
		//for(int i = 0; i < prob.length; i++){
		for(int i = 0; i < 1; i++){	
			for(int k = start_idx[i]; k <= end_idx[i]; k++){
				String requestFN = "requests-long-people-" + prob[i] + "-parcel-" + prob[i] + ".ins" + k + ".txt";
				//requestFN = "quangnn\\requests-people-21-parcel-7.ins1.txt";
				System.out.println(requestFN);
				//System.exit(-1);
				//String itinerariesFN = "direct-people-direct-parcel-service-itinerary-" + requestFN;
				//String itinerariesFN = "direct-people-service-itinerary-" + requestFN;
				//String itinerariesFN = "sharing-best-insertion-itinerary-" + requestFN;
				//String itinerariesFN = "preserve-next-service-point-itinerary-" + requestFN;
				//String itinerariesFN = "/Users/dungpq/Downloads/20150208/result_requests-long-people-10-parcel-10.ins1.txt";
				//String itinerariesFN = "C:\\Users\\DHBK\\Downloads\\20150208\\Output\\result_requests-long-people-10-parcel-10.ins1.txt";
				String itinerariesFN = "result_requests-long-people-10-parcel-10.ins1.txt";
				//String itinerariesFN = "C:\\Users\\DHBK\\Downloads\\20150208\\Output\\result_requests-people-3-parcel-3.ins5.txt";
				//String itinerariesFN = "tuan_result_requests-long-people-10-parcel-10.ins1.txt";
				
				SimulatorBookedRequests sim = new SimulatorBookedRequests();
				sim.loadMapFromTextFile("map-hanoi-connected.txt");
				//sim.loadMapFromTextFile("data-tokyo\\DataMap.txt");
				//if(true) return;
				//sim.loadParameters("quangnn\\config-parameters.txt");
				sim.loadParameters("config-parameters.txt");
				//sim.loadDepotParkings("quangnn\\depots10-parkings10.txt");
				sim.loadDepotParkings("depots300-parkings20.txt");
				
				GreedyWithSharing planner = new GreedyWithSharing(sim);
				//GreedyWithSharingPreserveNextServicePoint planner = new GreedyWithSharingPreserveNextServicePoint(sim);
				//GreedyDirectPeopleService planner = new GreedyDirectPeopleService(sim);
				//GreedyDirectPeopleDirectParcelServices planner = new GreedyDirectPeopleDirectParcelServices(sim);
				sim.setPlanner(planner);
				
				//sim.simulateDataFromFile(requestFN,1,2);
				//sim.writeTaxiItineraries(itinerariesFN);
				
				
				sim.initVehicles();
				sim.loadRequests(requestFN);
				HashMap<Integer, ItineraryTravelTime> itineraries = sim.loadItineraries(itinerariesFN);
				sim.analyzeSolution(itineraries);
				
				
				
				sim.finalize();
			}
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//SimulatorBookedRequests sim = new SimulatorBookedRequests();
		//sim.loadMapFromTextFile("map-hanoi-connected.txt");
		//sim.checkDistance("shortestPath.txt");
		//sim.checkDistance(59,10);
		
		
		//SimulatorBookedRequests.runBatch();
		//if(true) return;
		
		//String dir = "C:\\DungPQ\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco";
		String dir = "SanFrancisco";
		SimulatorBookedRequests sim = new SimulatorBookedRequests();
		String mapFN = "SanFranciscoRoad-connected-contracted-5.txt";
		String configParametersFN = "config-parameters.txt";
		String depotParkingFN = "depot600-parkings54.txt";
		String requestFN = "request_people_parcel_day_1.txt";
		sim.loadMapFromTextFile(dir + "/" + mapFN);
		//sim.loadRequests(dir + "\\request_day_1.txt");
		
		
		sim.loadParameters(dir + "/" + configParametersFN);
		sim.loadDepotParkings(dir + "/" + depotParkingFN);
		
		
		//sim.loadMapFromTextFile("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\Tokyo\\20090101\\data01000000\\reduceGraph.txt");
		//sim.loadParameters("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\Tokyo\\20090101\\config-parameters.txt");
		//sim.loadDepotParkings("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\Tokyo\\20090101\\data01000000\\depotparking1.txt");
				
		
		//String requestFN = "requests-people-10-parcel-10.ins2.txt";
		//String requestFN = dir + "\\" + requestFN;
		String requestPath = dir + "/" + requestFN;
		String itinerariesFN = dir + "/GreedySharing" + "/" + requestFN + "." + depotParkingFN + ".itineraries.txt";//Itineraries-people-1-parcel-1.txt";
		//String itinerariesFN = "result_requests-people-10-parcel-10.txt";
		//String itinerariesFN = "result_requests-0-1-2.txt";
		
		GreedyWithSharing planner = new GreedyWithSharing(sim);
		//GreedyWithSharingPreserveNextServicePoint planner = new GreedyWithSharingPreserveNextServicePoint(sim);
		//GreedyDirectPeopleService planner = new GreedyDirectPeopleService(sim);
		//GreedyDirectPeopleDirectParcelServices planner = new GreedyDirectPeopleDirectParcelServices(sim);
		sim.setPlanner(planner);
		
		sim.simulateDataFromFile(requestPath,1,2);
		sim.writeTaxiItineraries(itinerariesFN);
		
		sim.initVehicles();
		sim.loadRequests(requestPath);
		HashMap<Integer, ItineraryTravelTime> itineraries = sim.loadItineraries(itinerariesFN);
		sim.analyzeSolution(itineraries);
		
		
		
		sim.finalize();
		
	}

}
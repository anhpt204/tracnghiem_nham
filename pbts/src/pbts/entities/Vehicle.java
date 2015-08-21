package pbts.entities;

import java.util.*;
import java.io.*;

import pbts.enums.VehicleAction;
import pbts.enums.VehicleStatus;
import pbts.simulation.*;
public class Vehicle {

	/**
	 * @param args
	 */
	
	public int ID;
	public RoadMap map;
	public Simulator sim;
	public int lastPoint;
	public int lastIndexPoint;// index of lastPoint in currentItinerary
	public int remainTimeToNextPoint;// remain time to reach the point nextItinerary[0]
	public int remainTimeToNextDeparture;// remain time to next departure status
	
	public VehicleStatus status;
	
	public ArrayList<ItineraryTravelTime> LI;// list of itineraries during the working session
	
	public ItineraryTravelTime currentItinerary;// the pre-computed itinerary of the vehicle, 
											/* the vehicle will follow this itinerary if no further events occurred
											 * this itinerary might be updated when further requests arrive
											 */
	public ArrayList<Integer> remainRequestIDs;// list of remaining requests in services (represented by id, '+' means pickup, '-' means delivery)
	public double remainDistance;// remain distance from current point to the end of the itinerary
	
	public ArrayList<PeopleRequest> bookedPeoReq;
	public ArrayList<ParcelRequest> bookedParReq;
	
	public ArrayList<Integer> peopleReqIDonBoard;
	public ArrayList<Integer> parcelReqIDonBoard;
	
	
	public int startTimePointNewItinerary;
	
	public HashMap<Integer, VehicleStatus> mStatus;// mStatus.get(i) is the new status of the vehicle from ith point of currentItinerary
													//in other words: at ith point, the status of the vehicle will be mStatus.get(i)
	
	public ItineraryTravelTime nextItinerary;
	public HashMap<Integer, VehicleStatus> mNextStatus;
	
	public HashMap<Integer, Integer> mService;// mService.get(i) is the id of people or parcel request that the vehicle serves at ith point of currentItinerary
												// mService.get(t) is the id of request of people or parcel that the vehicle serves at time point t
	
	public double totalTravelDistance;
	
	/*
	 * SPECIFICATION: at current position, whenever an event arrives, the vehicle must go to nextItinerary[0]
	*/
	
	public PrintWriter log = null;
	public PrintWriter logI = null;
	public TimeHorizon T;
	
	public Vehicle(int ID,RoadMap map, Simulator sim){
		this.map = map;
		this.sim = sim;
		this.ID = ID;
		mStatus = new HashMap<Integer, VehicleStatus>();
		mService = new HashMap<Integer, Integer>();
		LI = new ArrayList<ItineraryTravelTime>();
		remainRequestIDs = new ArrayList<Integer>();
		peopleReqIDonBoard = new ArrayList<Integer>();
		parcelReqIDonBoard = new ArrayList<Integer>();
		
	}
	public String nextItinerary2String(){
		String s = "";
		for(int i = 0; i < currentItinerary.size(); i++){
			s = s + "[idx: " + i + ", point: " + currentItinerary.get(i) + ", status: " + getStatusDescription(mStatus.get(i)) + 
					", action = " + getActionDescription(currentItinerary.getAction(i)) + 
					", ta = " + currentItinerary.getArrivalTime(i) + ", td = "+ currentItinerary.getDepartureTime(i) + 
					", requestID = " + currentItinerary.getRequestID(i) + "]\n";
			//if(i > 100) break;
		}
		return s;
	}
	public String getStatusDescription(){
		String s = "";
		if(status == VehicleStatus.REST_AT_PARKING) s = "REST_AT_PARKING";
		else if(status == VehicleStatus.TRAVEL_WITHOUT_LOAD) s = "TRAVEL_WITHOUT_LOAD";
		else if(status == VehicleStatus.GOING_TO_DELIVERY_PARCEL) s = "GOING_TO_DELIVERY_PARCEL";
		else if(status == VehicleStatus.GOING_TO_DELIVERY_PEOPEL) s = "GOING_TO_DELIVERY_PEOPEL";
		else if(status == VehicleStatus.GOING_TO_PICKUP_PARCEL) s = "GOING_TO_PICKUP_PARCEL";
		else if(status == VehicleStatus.GOING_TO_PICKUP_PEOPLE) s = "GOING_TO_PICKUP_PEOPLE";
		else if(status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK) s = "GO_BACK_DEPOT_FINISH_WORK";
		else if(status == VehicleStatus.STOP_WORK) s = "STOP_WORK";
		return s;
	}
	public static String getStatusDescription(VehicleStatus status){
		String s = "-";
		if(status == VehicleStatus.REST_AT_PARKING) s = "REST_AT_PARKING";
		else if(status == VehicleStatus.TRAVEL_WITHOUT_LOAD) s = "TRAVEL_WITHOUT_LOAD";
		else if(status == VehicleStatus.GOING_TO_DELIVERY_PARCEL) s = "GOING_TO_DELIVERY_PARCEL";
		else if(status == VehicleStatus.GOING_TO_DELIVERY_PEOPEL) s = "GOING_TO_DELIVERY_PEOPEL";
		else if(status == VehicleStatus.GOING_TO_PICKUP_PARCEL) s = "GOING_TO_PICKUP_PARCEL";
		else if(status == VehicleStatus.GOING_TO_PICKUP_PEOPLE) s = "GOING_TO_PICKUP_PEOPLE";
		else if(status == VehicleStatus.PICKUP_PEOPLE) s = "PICKUP_PEOPLE";
		else if(status == VehicleStatus.DELIVERY_PEOPLE) s = "DELIVERY_PEOPLE";
		else if(status == VehicleStatus.PICKUP_PARCEL) s = "PICKUP_PARCEL";
		else if(status == VehicleStatus.DELIVERY_PARCEL) s = "DELIVERY_PARCEL";
		else if(status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK) s = "GO_BACK_DEPOT_FINISH_WORK";
		else if(status == VehicleStatus.STOP_WORK) s = "STOP_WORK";
		else if(status == VehicleStatus.NOT_WORK) s = "NOT_WORK";
		else if(status == VehicleStatus.PREPARE_NEW_ITINERARY) s = "PREPARE_NEW_ITINERARY";
		else if(status == VehicleStatus.FINISHED_PICKUP_PEOPLE) s = "FINISHED_PICKUP_PEOPLE";
		else if(status == VehicleStatus.FINISHED_DELIVERY_PEOPLE) s = "FINISHED_DELIVERY_PEOPLE";
		else if(status == VehicleStatus.FINISHED_PICKUP_PARCEL) s = "FINISHED_PICKUP_PARCEL";
		else if(status == VehicleStatus.FINISHED_DELIVERY_PARCEL) s = "FINISHED_DELIVERY_PARCEL";
		return s;
	}
	public static String getActionDescription(VehicleAction a){
		String s = "-";
		if(a == VehicleAction.PASS) s = "PASS";
		else if(a == VehicleAction.PICKUP_PEOPLE) s = "PICKUP_PEOPLE";
		else if(a == VehicleAction.DELIVERY_PEOPLE) s = "DELIVERY_PEOPLE";
		else if(a == VehicleAction.STOP) s = "STOP";
		else if(a == VehicleAction.PICKUP_PARCEL) s = "PICKUP_PARCEL";
		else if(a == VehicleAction.DELIVERY_PARCEL) s = "DELIVERY_PARCEL";
		else if(a == VehicleAction.FINISH_WORK) s = "FINISH_WORK"; 
		return s;
	}
	public String getActionDescription(){
		VehicleAction a = VehicleAction.STOP;
		if(currentItinerary != null)
			a = this.currentItinerary.getAction(lastIndexPoint);
		return getActionDescription(a);
	}
	public void addItinerary(ItineraryTravelTime I){
		LI.add(I);
	}
	public void writeItinerriesToLog(){
		logI.println(ID);
		for(int i = 0; i < LI.size(); i++){
			ItineraryTravelTime I = LI.get(i);
			//logI.println(i + "th itinerary: ");
			int startJ = 0;
			if(i > 0) startJ = 1;
			for(int j = startJ; j < I.path.size(); j++){
				int loc = I.path.get(j);
				int arr_time = -1;
				//if(I.getArrivalTime(j) != null) 
				arr_time = I.getArrivalTime(j);
				int dep_time = -1;
				//if(I.getDepartureTime(j) != null) 
				dep_time = I.getDepartureTime(j);
				if(j == I.size()-1 && dep_time == -1 && i < LI.size()-1){
					ItineraryTravelTime I1 = LI.get(i+1);
					dep_time = I1.getDepartureTime(0);
				}
				String stat = "-";
				int request_id = -1;
				//if(mStatus.get(j) != null) stat = getStatusDescription(mStatus.get(j));
				//if(mService.get(j) != null) request_id = mService.get(j);
				stat = getActionDescription(I.getAction(j));
				request_id = I.getRequestID(j);
				//logI.println("taxi[" + ID + "]: " + loc + " " + arr_time + " " + dep_time + " " + stat + " " + request_id);
				logI.println(loc + " " + arr_time + " " + dep_time + " " + stat + " " + request_id);
				//System.out.println("taxi[" + ID + "]: " + loc + " " + arr_time + " " + dep_time + " " + stat + " " + request_id);
			}
		}
		logI.println(-1);
	}
	public String toString(){ return ID + "," + getStatusDescription() + "," + lastPoint;}
	
	public void cancelSubItinerary(int fromIdx){
		int idx = fromIdx;
		for(int i = fromIdx; i < currentItinerary.size(); i++){
			if(mStatus.get(i) != null) mStatus.remove(i);
			currentItinerary.arrTime.remove(i);
			currentItinerary.depTime.remove(i);
		}
		while(currentItinerary.size() > idx){
			currentItinerary.remove(idx);
			currentItinerary.removeAction(idx);
			currentItinerary.removeRequestID(idx);
			
		}
	}
	public void cancelRemainItinerary(){
		cancelSubItinerary(lastIndexPoint+1);
		/*
		int idx = lastIndexPoint+1;
		while(currentItinerary.size() > idx){
			currentItinerary.remove(idx);
			currentItinerary.removeAction(idx);
			currentItinerary.removeRequestID(idx);
		}
		*/
	}
	public void setNewItinerary(){
		currentItinerary = nextItinerary;
		lastIndexPoint = 0;
		lastPoint = currentItinerary.get(lastIndexPoint);
		mStatus = mNextStatus;
		status = mStatus.get(lastIndexPoint);
		
		addItinerary(currentItinerary);
		
		if(currentItinerary.size() >= 2){
			if(currentItinerary.get(0) != currentItinerary.get(1)){
				Arc a = map.getArc(currentItinerary.get(0), currentItinerary.get(1));
				remainTimeToNextPoint = Simulator.getTravelTime(a, Simulator.maxSpeedms); 
				totalTravelDistance += a.w;
			}
		}else{
			remainTimeToNextPoint = 0;
		}
		currentItinerary.setDepartureTime(0, T.currentTimePoint);
	}
	
	/*
	public void move(){
		//log.println("Vehicle::move At time point " + T.currentTimePoint + " --> taxi[" + ID + "] has status = " + getStatusDescription(status) + 
				//", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", remaiTimeToNextPoint = " + remainTimeToNextPoint);
		if(status == VehicleStatus.PICKUP_PEOPLE){
			
			remainTimeToNextDeparture--;
			if(remainTimeToNextDeparture == 0){
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setDepartureTime(" + lastIndexPoint + "," + T.currentTimePoint);
				status = VehicleStatus.FINISHED_PICKUP_PEOPLE;
			}
			
			
			return;
		}
		if(status == VehicleStatus.DELIVERY_PEOPLE){
			remainTimeToNextDeparture--;
			if(remainTimeToNextDeparture == 0){
				//status = VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setDepartureTime(" + lastIndexPoint + "," + T.currentTimePoint);
				status = VehicleStatus.FINISHED_DELIVERY_PEOPLE;
			}
			return;
		}
		if(status == VehicleStatus.PICKUP_PARCEL){
			remainTimeToNextDeparture--;
			if(remainTimeToNextDeparture == 0){
				//status = VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setDepartureTime(" + lastIndexPoint + "," + T.currentTimePoint);
				status = VehicleStatus.FINISHED_PICKUP_PARCEL;
			}
			return;
		}
		if(status == VehicleStatus.DELIVERY_PARCEL){
			remainTimeToNextDeparture--;
			if(remainTimeToNextDeparture == 0){
				//status = VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setDepartureTime(" + lastIndexPoint + "," + T.currentTimePoint);
				status = VehicleStatus.FINISHED_DELIVERY_PARCEL;
			}
			return;
		}
		
		if(status == VehicleStatus.REST_AT_PARKING){
			return;
			
		}
		
		
		if(status == VehicleStatus.PREPARE_NEW_ITINERARY){
			remainTimeToNextPoint--;
			if(remainTimeToNextPoint == 0){
				lastIndexPoint++;
				lastPoint = currentItinerary.get(lastIndexPoint);
				currentItinerary.setArrivalTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setArrivalTime(" + lastIndexPoint + "," + T.currentTimePoint);
				cancelRemainItinerary();
				setNewItinerary();
			}
			return;
		}
		if(status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK){
			//System.out.println("Vehicle::move, ID = " + ID + ", status = GO_BACK_DEPOT_FINISH_WORK");
		}
		remainTimeToNextPoint--;
		if(remainTimeToNextPoint == 0){
			lastIndexPoint++;
			
			lastPoint = currentItinerary.get(lastIndexPoint);
			currentItinerary.setArrivalTime(lastIndexPoint, T.currentTimePoint);
			//System.out.println("Taxi " + ID + ", setArrivalTime(" + lastIndexPoint + "," + T.currentTimePoint);
			
			String actionStr = "NULL";
			if(currentItinerary.getAction(lastIndexPoint) != null){
				actionStr = getActionDescription(currentItinerary.getAction(lastIndexPoint));
			}
			if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PASS){
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
			}
			//if(ID == 4){
				//System.out.println("Vehicle::move, lastIndexPoint = " + lastIndexPoint + ", action = " + actionStr);
			//}
			//log.println("At timePoint " + T.currentTimePoint + ": taxi[" + ID + "] changes lastPoint = " + lastPoint);
			//log.println("At " + T.currentTimePointHMS() + ", taxi[" + ID + "] lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", status " + getStatusDescription(status));
			if(currentItinerary.size() > lastIndexPoint + 1){
				int v = currentItinerary.get(lastIndexPoint+1);
				Arc a = map.getArc(lastPoint, v);
				double speed = computeSpeed();// Simulator.maxSpeedms;
				//System.out.println("Vehicle::move --> speed = " + speed + ", maxSpeed = " + sim.maxSpeedms + ", minSpeed = " + sim.minSpeedms);
				remainTimeToNextPoint = Simulator.getTravelTime(a, speed);//Simulator.maxSpeedms); //currentItinerary.getTravelTime(lastIndexPoint);//a.t;
				if(remainTimeToNextPoint == 0) remainTimeToNextPoint = 1;
				totalTravelDistance += a.w;
				//log.println("At " + T.currentTimePoint + ", remainTimeToNextPoint = " + remainTimeToNextPoint);
			}else{
				//status = VehicleStatus.REST_AT_PARKING;
			}
			if(mStatus.get(lastIndexPoint) != null){
				status = mStatus.get(lastIndexPoint);
				log.println("At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"taxi[" + ID + "] changes status = " + getStatusDescription(status) + 
						" at point " + lastPoint);
				
				if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.FINISH_WORK){
					System.out.println("Vehicle::move, ID = " + ID + ", action = FINISH_WORK, status = " + getStatusDescription(status));
					if(status != VehicleStatus.STOP_WORK){
						System.out.println("Vehicle::move, ID = " + ID + ", exception inconsistent, action = FINISH_WORK, but status != STOP_WORK");
						System.exit(-1);
					}
				}
				if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PEOPLE){
					if(status != VehicleStatus.PICKUP_PEOPLE){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;// 60;// default pickup time is 60s
					//if(currentItinerary.getRequestID(lastIndexPoint) == 242){
						//System.out.println("Vehicle[" + ID  + "]::move --> pickup people " + 242 + " at time point " + T.currentTimePoint);
						//System.exit(-1);
					//}
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PEOPLE){
					if(status != VehicleStatus.DELIVERY_PEOPLE){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery time is 60s
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PARCEL){
					if(status != VehicleStatus.PICKUP_PARCEL){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					ParcelRequest pr = sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;//60;// default pickup time is 60s
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PARCEL){
					if(status != VehicleStatus.DELIVERY_PARCEL){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					ParcelRequest pr = sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery time is 60s
				}  
			}
			
		}
	}
	*/
	public Parking getFinalParking(){
		if(currentItinerary == null) return null;
		int locID = currentItinerary.get(currentItinerary.size()-1);
		Parking P = sim.findParking(locID);
		return P;
	}
	public int getNextStartPointIndex(){
		Vehicle taxi = this;
		ItineraryTravelTime I = taxi.currentItinerary;
		if(I == null) return 0;
		
		int nextStartPointIndex = -1;
		if(taxi.status == VehicleStatus.REST_AT_PARKING || taxi.status == VehicleStatus.PICKUP_PARCEL ||
				 taxi.status == VehicleStatus.PICKUP_PEOPLE ||  taxi.status == VehicleStatus.DELIVERY_PARCEL ||
				 taxi.status == VehicleStatus.DELIVERY_PEOPLE || taxi.status == VehicleStatus.STOP_WORK){
			nextStartPointIndex = taxi.lastIndexPoint;
		}else{
			if(taxi.lastIndexPoint < I.size()-1){
				nextStartPointIndex = taxi.lastIndexPoint+1;
			}else{
				System.out.println("Vehicle::getNextStartPoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.println("Vehicle::getNextStartPoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.close();
				System.exit(-1);
			}
		}
		return nextStartPointIndex;
	}
	public int getNextStartPoint(){
		Vehicle taxi = this;
		ItineraryTravelTime I = taxi.currentItinerary;
		if(I == null) return taxi.lastPoint;
		
		int nextStartPoint = -1;
		if(taxi.status == VehicleStatus.REST_AT_PARKING || taxi.status == VehicleStatus.PICKUP_PARCEL ||
				 taxi.status == VehicleStatus.PICKUP_PEOPLE ||  taxi.status == VehicleStatus.DELIVERY_PARCEL ||
				 taxi.status == VehicleStatus.DELIVERY_PEOPLE || taxi.status == VehicleStatus.STOP_WORK){
			nextStartPoint = taxi.lastPoint;
		}else{
			if(taxi.lastIndexPoint < I.size()-1){
				nextStartPoint = I.get(taxi.lastIndexPoint+1);
			}else{
				System.out.println("Vehicle::getNextStartPoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.println("Vehicle::getNextStartPoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.close();
				System.exit(-1);
			}
		}
		return nextStartPoint;
	}
	public int getNextStartTimePoint(){
		Vehicle taxi = this;
		int startTimePoint = -1;//T.currentTimePoint + 1;
		ItineraryTravelTime I = taxi.currentItinerary;
		if(I == null) return T.currentTimePoint + 1;
		if(taxi.status == VehicleStatus.REST_AT_PARKING || taxi.status == VehicleStatus.STOP_WORK){
			startTimePoint = I.getDepartureTime(taxi.lastIndexPoint);
			if(startTimePoint < 0)// not predefined yet
				startTimePoint = T.currentTimePoint+1;
		}else if(taxi.status == VehicleStatus.PICKUP_PARCEL ||
				 taxi.status == VehicleStatus.PICKUP_PEOPLE ||  taxi.status == VehicleStatus.DELIVERY_PARCEL ||
				 taxi.status == VehicleStatus.DELIVERY_PEOPLE || taxi.status == VehicleStatus.STOP_WORK){
			startTimePoint = I.getDepartureTime(taxi.lastIndexPoint);
			if(startTimePoint < 0)// not predefined yet
				startTimePoint = T.currentTimePoint+1;
		}else{
			if(taxi.lastIndexPoint < I.size()-1){// traveling
				startTimePoint = I.getArrivalTime(taxi.lastIndexPoint+1);
			}else{
				System.out.println("Vehicle::getNextStartTimePoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.println("Vehicle::getNextStartTimePoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.close();
				System.exit(-1);
			}
		}
		return startTimePoint;
	}
	public String getRemainRequestID(){
		String s = "";
		for(int i = 0; i < remainRequestIDs.size(); i++)
			s = s + remainRequestIDs.get(i) + ",";
		return s;
	}
	public VehicleStatus changeStatus(int fromIdx){
		for(int i = fromIdx; i < currentItinerary.size(); i++){
			VehicleAction a = currentItinerary.getAction(i);
			if(a == VehicleAction.PICKUP_PARCEL) status = VehicleStatus.GOING_TO_PICKUP_PARCEL;
			else if(a == VehicleAction.DELIVERY_PARCEL) status = VehicleStatus.GOING_TO_DELIVERY_PARCEL;
			else if(a == VehicleAction.PICKUP_PEOPLE) status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
			else if(a == VehicleAction.DELIVERY_PEOPLE) status = VehicleStatus.GOING_TO_DELIVERY_PEOPEL;
			else if(a == VehicleAction.FINISH_WORK) status = VehicleStatus.GO_BACK_DEPOT_FINISH_WORK;
		}
		return status;
	}
	/*
	public void move(){
		//log.println("Vehicle::move At time point " + T.currentTimePoint + " --> taxi[" + ID + "] has status = " + getStatusDescription(status) + 
				//", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", remaiTimeToNextPoint = " + remainTimeToNextPoint);
		int taxiID = -1;
		if(status == VehicleStatus.PICKUP_PEOPLE){
			if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = VehicleStatus.FINISHED_PICKUP_PEOPLE;
				if(lastIndexPoint < currentItinerary.size()-1)
					if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if(mStatus.get(lastIndexPoint) != null){
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			
			
			return;
		}
		if(status == VehicleStatus.DELIVERY_PEOPLE){
			if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = VehicleStatus.FINISHED_DELIVERY_PEOPLE;
				
				if(lastIndexPoint < currentItinerary.size()-1)
					if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if(mStatus.get(lastIndexPoint) != null){
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			
			return;
		}
		if(status == VehicleStatus.PICKUP_PARCEL){
			if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = VehicleStatus.FINISHED_PICKUP_PARCEL;
				
				if(lastIndexPoint < currentItinerary.size()-1)
					if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if(mStatus.get(lastIndexPoint) != null){
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			
			return;
		}
		if(status == VehicleStatus.DELIVERY_PARCEL){
			if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = VehicleStatus.FINISHED_DELIVERY_PARCEL;
				
				if(lastIndexPoint < currentItinerary.size()-1)
					if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if(mStatus.get(lastIndexPoint) != null){
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			
			return;
		}
		
		// update data structure when we go to the next time point
		
		
		if(status == VehicleStatus.REST_AT_PARKING){
			
			if(currentItinerary != null) if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = changeStatus(lastIndexPoint);
			}
			return;
			
		}
		
		
		if(status == VehicleStatus.PREPARE_NEW_ITINERARY){
			if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
				cancelRemainItinerary();
				setNewItinerary();
			}
			
			return;
		}
		if(status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK){
			//System.out.println("Vehicle::move, ID = " + ID + ", status = GO_BACK_DEPOT_FINISH_WORK");
		}
		remainTimeToNextPoint--;
		//if(remainTimeToNextPoint == 0){
		//if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){	
		if(ID == taxiID)log.println("Vehicle[" + ID + "]::move, T.current = " + T.currentTimePoint + 
				", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", getArrivalTime(lastIndexPoint+1) = " + 
				currentItinerary.getArrivalTime(lastIndexPoint+1) + ", status = " + getStatusDescription(status));
		
		while(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){	
			int nextPoint = currentItinerary.get(lastIndexPoint+1);
			Arc a0 = map.getArc(lastPoint, nextPoint);
			if(a0 != null){
				remainDistance = remainDistance - a0.w;
			}
			
			int rid = currentItinerary.getRequestID(lastIndexPoint+1);
			VehicleAction act = currentItinerary.getAction(lastIndexPoint+1);
			if(rid > 0){
				remainRequestIDs.remove(0);
				if(act == VehicleAction.PICKUP_PARCEL){
					parcelReqIDonBoard.add(rid);
				}else if(act == VehicleAction.DELIVERY_PARCEL){
					int idx = parcelReqIDonBoard.indexOf(rid);
					parcelReqIDonBoard.remove(idx);
				}else if(act == VehicleAction.PICKUP_PEOPLE){
					peopleReqIDonBoard.add(rid);
					sim.countStop.put(rid, 0);
					sim.accumulateDistance.put(rid, 0.0);
				}else if(act == VehicleAction.DELIVERY_PEOPLE){
					int idx = peopleReqIDonBoard.indexOf(rid);
					peopleReqIDonBoard.remove(idx);
				}else{
					System.out.println("Vehicle::move EXCEPTION unknown action?????");
					System.exit(-1);
				}
				
				// update stops of people requests
				for(int i = 0; i < peopleReqIDonBoard.size(); i++){
					int r = peopleReqIDonBoard.get(i);
					if(r != rid){
						sim.countStop.put(r, sim.countStop.get(r) + 1);
						
						if(a0 != null)
							sim.accumulateDistance.put(r, sim.accumulateDistance.get(r) + a0.w);
					}
				}
			}
			
			
			lastIndexPoint++;
			
			lastPoint = currentItinerary.get(lastIndexPoint);
			if(ID == taxiID)log.println("Vehicle[" + ID + "]::move, REACH T.current = " + T.currentTimePoint + 
					", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", getArrivalTime(lastIndexPoint+1) = " + 
					currentItinerary.getArrivalTime(lastIndexPoint+1) + ", status = " + getStatusDescription(status));
			
			//currentItinerary.setArrivalTime(lastIndexPoint, T.currentTimePoint);
			//System.out.println("Taxi " + ID + ", setArrivalTime(" + lastIndexPoint + "," + T.currentTimePoint);
			
			String actionStr = "NULL";
			if(currentItinerary.getAction(lastIndexPoint) != null){
				actionStr = getActionDescription(currentItinerary.getAction(lastIndexPoint));
			}
			if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PASS){
				//currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
			}
			//if(ID == 4){
				//System.out.println("Vehicle::move, lastIndexPoint = " + lastIndexPoint + ", action = " + actionStr);
			//}
			//log.println("At timePoint " + T.currentTimePoint + ": taxi[" + ID + "] changes lastPoint = " + lastPoint);
			//log.println("At " + T.currentTimePointHMS() + ", taxi[" + ID + "] lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", status " + getStatusDescription(status));
			if(currentItinerary.size() > lastIndexPoint + 1){
				int v = currentItinerary.get(lastIndexPoint+1);
				Arc a = map.getArc(lastPoint, v);
				
				if(a != null)
					totalTravelDistance += a.w;
				else{
					System.out.println("Vehicle::move, Arc (" + lastPoint + "," + v + ") does not exists ????????????????");
					log.println("Vehicle[" + ID + "]::move, Arc (" + lastPoint + "," + v + ") does not exists ????????????????");
				}
				//log.println("At " + T.currentTimePoint + ", remainTimeToNextPoint = " + remainTimeToNextPoint);
			}else{
				//status = VehicleStatus.REST_AT_PARKING;
			}
			if(mStatus.get(lastIndexPoint) != null){
				status = mStatus.get(lastIndexPoint);
				if(status == VehicleStatus.REST_AT_PARKING){
					System.out.println("Vehicle::move, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
							"taxi[" + ID + "] REST AT PARKING");
				}
				log.println("Vehicle::move, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"taxi[" + ID + "] changes status = " + getStatusDescription(status) + 
						" at point " + lastPoint);
				
				if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.FINISH_WORK){
					System.out.println("Vehicle::move, ID = " + ID + ", action = FINISH_WORK, status = " + getStatusDescription(status));
					if(status != VehicleStatus.STOP_WORK){
						System.out.println("Vehicle::move, ID = " + ID + ", exception inconsistent, action = FINISH_WORK, but status != STOP_WORK");
						System.exit(-1);
					}
				}
				if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PEOPLE){
					if(status != VehicleStatus.PICKUP_PEOPLE){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;// 60;// default pickup time is 60s
					//if(currentItinerary.getRequestID(lastIndexPoint) == 242){
						//System.out.println("Vehicle[" + ID  + "]::move --> pickup people " + 242 + " at time point " + T.currentTimePoint);
						//System.exit(-1);
					//}
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PEOPLE){
					if(status != VehicleStatus.DELIVERY_PEOPLE){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery time is 60s
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PARCEL){
					if(status != VehicleStatus.PICKUP_PARCEL){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					System.out.println("Vehicle::move, lastIndexPoint = " + currentItinerary.getRequestID(lastIndexPoint) + 
							", nextItinerary = " + nextItinerary2String());
					ParcelRequest pr = sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;//60;// default pickup time is 60s
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PARCEL){
					if(status != VehicleStatus.DELIVERY_PARCEL){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					ParcelRequest pr = sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery time is 60s
				}  
			}
			
		}
	}
	*/
	public double computeSpeed(){
		
		double dis = 0;
		int reqID = -1;
		int time = -1;
		int taxiID = -1;
		if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, lastIndexPoint = " + lastIndexPoint);
		for(int ii = lastIndexPoint; ii < currentItinerary.size(); ii++){
			if(currentItinerary.getAction(ii) == VehicleAction.PICKUP_PARCEL && ii > lastIndexPoint){
				
				reqID = currentItinerary.getRequestID(ii);
				ParcelRequest pr = sim.getParcelRequest(reqID);
				int t = T.currentTimePoint + (int)(dis/sim.maxSpeedms);
				if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, T.current = " + T.currentTimePoint + 
						", pickup parcel " + pr.id + ", pr.earlyPickup = " + pr.earlyPickupTime + 
						", pr.latePickup = " + pr.latePickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", t = " + t);
				
				if(t + pr.pickupDuration >= pr.earlyPickupTime && t + pr.pickupDuration <= pr.latePickupTime){
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, t = " + t + ", parcelRequest " + pr.id + ", pr.pickupDuration = " + pr.pickupDuration + 
							", pr.earlyPickupTime = " + pr.earlyPickupTime + ", pr.latePickupTime = " + pr.latePickupTime + ", --> return maxSpeedms = " + sim.maxSpeedms);
					return sim.maxSpeedms;
				}
				else if(t + pr.pickupDuration < pr.earlyPickupTime){
					double speed = dis/(pr.earlyPickupTime - pr.pickupDuration - T.currentTimePoint);
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, t = " + t + ", parcelRequest " + pr.id + 
							", pr.pickupDuration = " + pr.pickupDuration + ", < pr.earlyPickupTime, dis = " + dis + ", --> return speed = " + speed);
					return speed;
				}
				else{
					if(pr.latePickupTime - pr.pickupDuration/2 - T.currentTimePoint <= 0){
						System.out.println("Vehicle[" + ID + "]::computeSpeed, BUG???? " + "T.current = " + T.currentTimePoint + 
								", pr.latePickupTime = " + pr.latePickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", distance = " + dis);
						if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, ii = " + ii + ", BUG???? " + "T.current = " + T.currentTimePoint + 
								", pr.latePickupTime = " + pr.latePickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", distance = " + dis);
						log.close();
						System.exit(-1);
					}
					double speed = dis/(pr.latePickupTime - pr.pickupDuration/2 - T.currentTimePoint);
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, parcelRequest " + pr.id + ", pr.latePickupTime = " + pr.latePickupTime + 
							" - pr.pickupDuration = " + pr.pickupDuration + " > T.current =  " + T.currentTimePoint + ", dis = " + dis + " --> return speed =  " + speed);
					return speed;
				}
				/*
				time = (pr.latePickupTime + pr.earlyPickupTime)/2 - pr.pickupDuration - T.currentTimePoint;
				if(time <= 0)
				time = pr.latePickupTime - pr.pickupDuration - T.currentTimePoint;
				//System.out.println("Vehicle::computeSpeed, peopleRequest = " + reqID + ", pr.latePickupTime = "
					//	+ pr.latePickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", currentTime = " + T.currentTimePoint + ", time = " + time + ", dis = " + dis);
				if(time <= 0 && dis > 0){
					System.out.println("Vehicle::computeSpeed, time = " + time + " < 0 --> BUG???????");
					System.exit(-1);
				}
				*/
				//break;
			}else if(currentItinerary.getAction(ii) == VehicleAction.PICKUP_PEOPLE && ii > lastIndexPoint){
				reqID = currentItinerary.getRequestID(ii);
				PeopleRequest pr = sim.getPeopleRequest(reqID);
				int t = T.currentTimePoint + (int)(dis/sim.maxSpeedms);
				if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, T.current = " + T.currentTimePoint + 
						", pickup people " + pr.id + ", pr.earlyPickup = " + pr.earlyPickupTime + 
						", pr.latePickup = " + pr.latePickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", t = " + t);
				
				if(t + pr.pickupDuration >= pr.earlyPickupTime && t + pr.pickupDuration <= pr.latePickupTime){
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, t = " + t + ", people Request " + pr.id + ", pr.pickupDuration = " + pr.pickupDuration + 
							", pr.earlyPickupTime = " + pr.earlyPickupTime + ", pr.latePickupTime = " + pr.latePickupTime + ", --> return maxSpeedms = " + sim.maxSpeedms);
					
					return sim.maxSpeedms;
				}
				else if(t + pr.pickupDuration < pr.earlyPickupTime){
					double speed = dis/(pr.earlyPickupTime - pr.pickupDuration - T.currentTimePoint);
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, t = " + t + ", people Request " + pr.id + 
							", pr.pickupDuration = " + pr.pickupDuration + ", < pr.earlyPickupTime, dis = " + dis + " --> return speed = " + speed);
					
					return speed;
				}
				else{
					if(pr.latePickupTime  - pr.pickupDuration/2 - T.currentTimePoint <= 0){
						System.out.println("Vehicle[" + ID + "]::computeSpeed, BUG???? " + "T.current = " + T.currentTimePoint + 
								", pr.latePickupTime = " + pr.latePickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", distance = " + dis);
						if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, ii = " + ii + ", BUG???? " + "T.current = " + T.currentTimePoint + 
								", pr.latePickupTime = " + pr.latePickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", distance = " + dis);
						log.close();
						System.exit(-1);
					}
					double speed = dis/(pr.latePickupTime - pr.pickupDuration/2 - T.currentTimePoint);
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, people Request " + pr.id + ", pr.latePickupTime = " + pr.latePickupTime + 
							" - pr.pickupDuration = " + pr.pickupDuration + " > T.current =  " + T.currentTimePoint + ", dis = " + dis + " --> return speed =  " + speed);
					return speed;
				}
				//System.out.println("Vehicle::computeSpeed, peopleRequest = " + reqID + ", pr.pickupTime = "
				//+ pr.earlyPickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", currentTime = " + T.currentTimePoint);
				/*
				time = (pr.latePickupTime + pr.earlyPickupTime)/2 - pr.pickupDuration - T.currentTimePoint;
				if(time <= 0)
				time = pr.latePickupTime - pr.pickupDuration - T.currentTimePoint;
				//System.out.println("Vehicle::computeSpeed, peopleRequest = " + reqID + ", pr.latePickupTime = "
					//	+ pr.latePickupTime + ", pr.pickupDuration = " + pr.pickupDuration + ", currentTime = " + T.currentTimePoint + ", time = " + time + ", dis = " + dis);
				if(time <= 0 && dis > 0){
					System.out.println("Vehicle::computeSpeed, time = " + time + " <= 0 --> BUG???????");
					System.exit(-1);
				}
				*/
				
				//break;
			}else if(currentItinerary.getAction(ii) == VehicleAction.DELIVERY_PEOPLE && ii > lastIndexPoint ){
				reqID = currentItinerary.getRequestID(ii);
				PeopleRequest pr = sim.getPeopleRequest(reqID);
				int t = T.currentTimePoint + (int)(dis/sim.maxSpeedms);
				
				if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, T.current = " + T.currentTimePoint + 
						", delivery people " + pr.id + ", pr.earlyDelivery = " + pr.earlyDeliveryTime + 
						", pr.lateDelivery = " + pr.lateDeliveryTime + ", pr.deliveryDuration = " + pr.deliveryDuration + ", t = " + t);
				
				if(t >= pr.earlyDeliveryTime && t <= pr.lateDeliveryTime){
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, peopleRequest " + pr.id + ", t = " + t + 
							", pr.earlyDeliveryTime = " + pr.earlyDeliveryTime + ", pr.lateDeliveryTime = " + pr.lateDeliveryTime + " --> return maxSpeed = " + sim.maxSpeedms);
					return sim.maxSpeedms;
				}
				else if(t < pr.earlyDeliveryTime){
					double speed = dis/(pr.earlyPickupTime - T.currentTimePoint);
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, people request " + pr.id + ", t = " + t + 
							" < pr.earlyDeliveryTime = " + pr.earlyDeliveryTime + ", dis = " + dis + " --> return  speed = " + speed);
					return speed;
				}
				else{
					if(pr.lateDeliveryTime  - T.currentTimePoint <= 0){
						System.out.println("Vehicle[" + ID + "]::computeSpeed, BUG???? " + "T.current = " + T.currentTimePoint + 
								", pr.lateDeliveryTime = " + pr.lateDeliveryTime + ", distance = " + dis);
						if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, ii = " + ii + ", BUG???? " + "T.current = " + T.currentTimePoint + 
								", pr.lateDeliveryTime = " + pr.lateDeliveryTime + ", distance = " + dis);
						log.close();
						System.exit(-1);
					}
					double speed = dis/(pr.lateDeliveryTime - T.currentTimePoint);
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, people request " + pr.id + 
							", pr.lateDeliveryTime = " + pr.lateDeliveryTime + " > T.current, dis = " + dis + " --> return speed = " + speed);
					return speed;
				}
				/*
				time =(pr.lateDeliveryTime + pr.earlyDeliveryTime)/2 - pr.deliveryDuration - T.currentTimePoint;
				if(time <= 0)
				time = pr.lateDeliveryTime - pr.deliveryDuration - T.currentTimePoint;
				//System.out.println("Vehicle::computeSpeed, peopleRequest = " + reqID + ", pr.lateDeliveryTime = "
						//+ pr.lateDeliveryTime + ", pr.pickupDuration = " + pr.pickupDuration + ", currentTime = " + T.currentTimePoint + ", time = " + time + ", dis = " + dis);
				if(time <= 0 && dis > 0){
					System.out.println("Vehicle::computeSpeed, time = " + time + " < 0 --> BUG???????");
					System.exit(-1);
				}
				break;
				*/
			}else if(currentItinerary.getAction(ii) == VehicleAction.DELIVERY_PARCEL && ii > lastIndexPoint){
				reqID = currentItinerary.getRequestID(ii);
				ParcelRequest pr = sim.getParcelRequest(reqID);
				int t = T.currentTimePoint + (int)(dis/sim.maxSpeedms);
				if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, T.current = " + T.currentTimePoint + 
						", delivery people " + pr.id + ", pr.earlyDelivery = " + pr.earlyDeliveryTime + 
						", pr.lateDelivery = " + pr.lateDeliveryTime + ", pr.deliveryDuration = " + pr.deliveryDuration + ", t = " + t);
				
				if(t >= pr.earlyDeliveryTime && t <= pr.lateDeliveryTime){
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, parcelRequest " + pr.id + ", t = " + t + 
							", pr.earlyDeliveryTime = " + pr.earlyDeliveryTime + ", pr.lateDeliveryTime = " + pr.lateDeliveryTime + " --> return maxSpeed = " + sim.maxSpeedms);
					
					return sim.maxSpeedms;
				}
				else if(t < pr.earlyDeliveryTime){
					double speed = dis/(pr.earlyPickupTime - T.currentTimePoint);
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, parcel request " + pr.id + ", t = " + t + 
							" < pr.earlyDeliveryTime = " + pr.earlyDeliveryTime + ", dis = " + dis + ", --> return  speed = " + speed);
					
					return speed;
				}
				else{
					if(pr.lateDeliveryTime  - T.currentTimePoint <= 0){
						System.out.println("Vehicle[" + ID + "]::computeSpeed, ii = " + ii + ", BUG???? " + "T.current = " + T.currentTimePoint + 
								", pr.lateDeliveryTime = " + pr.lateDeliveryTime + ", distance = " + dis);
						if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, ii = " + ii + ", BUG???? " + "T.current = " + T.currentTimePoint + 
								", pr.lateDeliveryTime = " + pr.lateDeliveryTime + ", distance = " + dis);
						log.close();
						System.exit(-1);
					}
					double speed = dis/(pr.lateDeliveryTime - T.currentTimePoint);
					if(ID == taxiID)log.println("Vehicle[" + ID + "]::computeSpeed, parcel request + " + pr.id + 
							", pr.lateDeliveryTime = " + pr.lateDeliveryTime + " > T.current, dis = " + dis + ", --> return speed = " + speed);
					return speed;
					//return dis/(pr.lateDeliveryTime - T.currentTimePoint);
				}
				/*
				time = (pr.lateDeliveryTime + pr.earlyDeliveryTime)/2 - pr.deliveryDuration - T.currentTimePoint;
				if(time <= 0)
				time = pr.lateDeliveryTime - pr.deliveryDuration - T.currentTimePoint;
				//System.out.println("Vehicle::computeSpeed, peopleRequest = " + reqID + ", pr.lateDeliveryTime = "
						//+ pr.lateDeliveryTime + ", pr.pickupDuration = " + pr.pickupDuration + ", currentTime = " + T.currentTimePoint + ", time = " + time + ", dis = " + dis);
				if(time <= 0 && dis > 0){
					System.out.println("Vehicle::computeSpeed, time = " + time + " < 0 --> BUG???????");
					System.exit(-1);
				}
				break;
				*/
			}else{
				if(ii + 1 < currentItinerary.size()){
					int u1 = currentItinerary.get(ii);
					int u2 = currentItinerary.get(ii+1);
					Arc a12 = map.getArc(u1, u2);
					dis = dis + a12.w;
					//log.println("Vehicle[" + ID + "]::computeSpeed, ii = " + ii + ", arc(" + u1 + "," + u2 + ") w = " + a12.w + ", dis = " + dis);
				}
			}
		}
		if(time < 0) return sim.maxSpeedms;
		
		return dis/time;
	}
	public boolean finishedItinerary(){
		return lastIndexPoint == currentItinerary.size()-1;
	}
	public String name(){
		return "Vehicle";
	}
	public void move(){
		//if(ID == 3) 
			//System.out.println(name() + "["+ ID + "]::move() --> T = " + T.currentTimePoint + ", lastIndexPoint = " + 
		//lastIndexPoint + ", lastPoint = " + lastPoint + ", status = " + getStatusDescription() + ", action = " + getActionDescription());
		//log.println("Vehicle::move At time point " + T.currentTimePoint + " --> taxi[" + ID + "] has status = " + getStatusDescription(status) + 
				//", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", remaiTimeToNextPoint = " + remainTimeToNextPoint);
		int taxiID = -1;
		if(status == VehicleStatus.PICKUP_PEOPLE){
			if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = VehicleStatus.FINISHED_PICKUP_PEOPLE;
				if(lastIndexPoint < currentItinerary.size()-1)
					if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if(mStatus.get(lastIndexPoint) != null){
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			/*
			remainTimeToNextDeparture--;
			if(remainTimeToNextDeparture == 0){
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setDepartureTime(" + lastIndexPoint + "," + T.currentTimePoint);
				status = VehicleStatus.FINISHED_PICKUP_PEOPLE;
			}
			*/
			
			return;
		}
		if(status == VehicleStatus.DELIVERY_PEOPLE){
			if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = VehicleStatus.FINISHED_DELIVERY_PEOPLE;
				
				if(lastIndexPoint < currentItinerary.size()-1)
					if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if(mStatus.get(lastIndexPoint) != null){
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			/*
			remainTimeToNextDeparture--;
			if(remainTimeToNextDeparture == 0){
				//status = VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setDepartureTime(" + lastIndexPoint + "," + T.currentTimePoint);
				status = VehicleStatus.FINISHED_DELIVERY_PEOPLE;
			}
			*/
			return;
		}
		if(status == VehicleStatus.PICKUP_PARCEL){
			if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = VehicleStatus.FINISHED_PICKUP_PARCEL;
				
				if(lastIndexPoint < currentItinerary.size()-1)
					if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if(mStatus.get(lastIndexPoint) != null){
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			/*
			remainTimeToNextDeparture--;
			if(remainTimeToNextDeparture == 0){
				//status = VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setDepartureTime(" + lastIndexPoint + "," + T.currentTimePoint);
				status = VehicleStatus.FINISHED_PICKUP_PARCEL;
			}
			*/
			return;
		}
		if(status == VehicleStatus.DELIVERY_PARCEL){
			if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = VehicleStatus.FINISHED_DELIVERY_PARCEL;
				
				if(lastIndexPoint < currentItinerary.size()-1)
					if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
						lastIndexPoint++;
						lastPoint = currentItinerary.get(lastIndexPoint);
						if(mStatus.get(lastIndexPoint) != null){
							status = mStatus.get(lastIndexPoint);
						}
					}
			}
			/*
			remainTimeToNextDeparture--;
			if(remainTimeToNextDeparture == 0){
				//status = VehicleStatus.GOING_TO_DELIVERY_PEOPEL;//VehicleStatus.DELIVERY_PEOPLE;
				currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setDepartureTime(" + lastIndexPoint + "," + T.currentTimePoint);
				status = VehicleStatus.FINISHED_DELIVERY_PARCEL;
			}
			*/
			return;
		}
		/*
		if(status == VehicleStatus.REST_AT_PARKING){
			Parking p = sim.findParking(lastPoint);
			if(p != null){
				p.load--;
				p.lastUpdateTimePoint = T.currentTimePoint;
				System.out.println("Vehicle::move At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"parking " + p + " load decrease = " + p.load + ", capacity = " + p.capacity);
			}
		}
		*/
		// update data structure when we go to the next time point
		
		
		if(status == VehicleStatus.REST_AT_PARKING){
			/*
			if(T.currentTimePoint > T.endrequest){
				//status = VehicleStatus.NOT_WORK;
				status = VehicleStatus.STOP_WORK;
				log.println("At time point " + T.currentTimePoint + ", taxi[" + ID + "] --> STOP WOTKING");
			}
			*/
			if(currentItinerary != null) if(T.currentTimePoint == currentItinerary.getDepartureTime(lastIndexPoint)){
				status = changeStatus(lastIndexPoint);
			}
			return;
			
		}
		
		
		if(status == VehicleStatus.PREPARE_NEW_ITINERARY){
			if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){
				cancelRemainItinerary();
				setNewItinerary();
			}
			/*
			remainTimeToNextPoint--;
			if(remainTimeToNextPoint == 0){
				lastIndexPoint++;
				lastPoint = currentItinerary.get(lastIndexPoint);
				currentItinerary.setArrivalTime(lastIndexPoint, T.currentTimePoint);
				//System.out.println("Taxi " + ID + ", setArrivalTime(" + lastIndexPoint + "," + T.currentTimePoint);
				cancelRemainItinerary();
				setNewItinerary();
			}
			*/
			return;
		}
		if(status == VehicleStatus.GO_BACK_DEPOT_FINISH_WORK){
			//System.out.println("Vehicle::move, ID = " + ID + ", status = GO_BACK_DEPOT_FINISH_WORK");
		}
		remainTimeToNextPoint--;
		//if(remainTimeToNextPoint == 0){
		//if(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){	
		if(ID == taxiID)log.println("Vehicle[" + ID + "]::move, T.current = " + T.currentTimePoint + 
				", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", getArrivalTime(lastIndexPoint+1) = " + 
				currentItinerary.getArrivalTime(lastIndexPoint+1) + ", status = " + getStatusDescription(status));
		
		while(T.currentTimePoint == currentItinerary.getArrivalTime(lastIndexPoint+1)){	
			int nextPoint = currentItinerary.get(lastIndexPoint+1);
			Arc a0 = map.getArc(lastPoint, nextPoint);
			if(a0 != null){
				remainDistance = remainDistance - a0.w;
			}
			
			int rid = currentItinerary.getRequestID(lastIndexPoint+1);
			VehicleAction act = currentItinerary.getAction(lastIndexPoint+1);
			if(rid > 0){
				remainRequestIDs.remove(0);
				if(act == VehicleAction.PICKUP_PARCEL){
					parcelReqIDonBoard.add(rid);
					sim.nbParcelOnBoard++;
					sim.nbParcelServed++;
					System.out.println("Vehicle[" + ID + "] At " + T.currentTimePointHMS() + ", pickupParcel, nbParcelServed = "+ sim.nbParcelServed + ", nbParlceOnBoard = " + sim.nbParcelOnBoard);
				}else if(act == VehicleAction.DELIVERY_PARCEL){
					int idx = parcelReqIDonBoard.indexOf(rid);
					parcelReqIDonBoard.remove(idx);
					sim.nbParcelComplete++;
					sim.nbParcelOnBoard--;
					System.out.println("Vehicle[" + ID + "] At " + T.currentTimePointHMS() + ", deliveryParcel, nbParcelComplete = "+ sim.nbParcelComplete + ", nbParlceOnBoard = " + sim.nbParcelOnBoard);
				}else if(act == VehicleAction.PICKUP_PEOPLE){
					peopleReqIDonBoard.add(rid);
					sim.countStop.put(rid, 0);
					sim.accumulateDistance.put(rid, 0.0);
					sim.nbPeopleOnBoard++;
					sim.nbPeopleServed++;
					System.out.println("Vehicle[" + ID + "] At " + T.currentTimePointHMS() + ", pickupPeople, nbPeopleServed = "+ sim.nbPeopleServed + ", nbPeopleOnBoard = " + sim.nbPeopleOnBoard);
				}else if(act == VehicleAction.DELIVERY_PEOPLE){
					int idx = peopleReqIDonBoard.indexOf(rid);
					peopleReqIDonBoard.remove(idx);
					sim.nbPeopleComplete++;
					sim.nbPeopleOnBoard--;
					System.out.println("Vehicle[" + ID + "] At " + T.currentTimePointHMS() + ", deliveryPeople, nbPeopleComplete = "+ sim.nbPeopleComplete + ", nbPeopleOnBoard = " + sim.nbPeopleOnBoard);
				}else{
					System.out.println("Vehicle::move EXCEPTION unknown action?????");
					System.exit(-1);
				}
				
				// update stops of people requests
				for(int i = 0; i < peopleReqIDonBoard.size(); i++){
					int r = peopleReqIDonBoard.get(i);
					if(r != rid){
						sim.countStop.put(r, sim.countStop.get(r) + 1);
						
						if(a0 != null)
							sim.accumulateDistance.put(r, sim.accumulateDistance.get(r) + a0.w);
					}
				}
			}
			
			
			lastIndexPoint++;
			
			lastPoint = currentItinerary.get(lastIndexPoint);
			if(ID == taxiID)log.println("Vehicle[" + ID + "]::move, REACH T.current = " + T.currentTimePoint + 
					", lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", getArrivalTime(lastIndexPoint+1) = " + 
					currentItinerary.getArrivalTime(lastIndexPoint+1) + ", status = " + getStatusDescription(status));
			
			//currentItinerary.setArrivalTime(lastIndexPoint, T.currentTimePoint);
			//System.out.println("Taxi " + ID + ", setArrivalTime(" + lastIndexPoint + "," + T.currentTimePoint);
			
			String actionStr = "NULL";
			if(currentItinerary.getAction(lastIndexPoint) != null){
				actionStr = getActionDescription(currentItinerary.getAction(lastIndexPoint));
			}
			if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PASS){
				//currentItinerary.setDepartureTime(lastIndexPoint, T.currentTimePoint);
			}
			//if(ID == 4){
				//System.out.println("Vehicle::move, lastIndexPoint = " + lastIndexPoint + ", action = " + actionStr);
			//}
			//log.println("At timePoint " + T.currentTimePoint + ": taxi[" + ID + "] changes lastPoint = " + lastPoint);
			//log.println("At " + T.currentTimePointHMS() + ", taxi[" + ID + "] lastIndexPoint = " + lastIndexPoint + ", lastPoint = " + lastPoint + ", status " + getStatusDescription(status));
			if(currentItinerary.size() > lastIndexPoint + 1){
				int v = currentItinerary.get(lastIndexPoint+1);
				Arc a = map.getArc(lastPoint, v);
				/*
				double speed = computeSpeed();// Simulator.maxSpeedms;
				//System.out.println("Vehicle::move --> speed = " + speed + ", maxSpeed = " + sim.maxSpeedms + ", minSpeed = " + sim.minSpeedms);
				remainTimeToNextPoint = Simulator.getTravelTime(a, speed);//Simulator.maxSpeedms); //currentItinerary.getTravelTime(lastIndexPoint);//a.t;
				if(remainTimeToNextPoint == 0) remainTimeToNextPoint = 1;
				*/
				if(a != null)
					totalTravelDistance += a.w;
				else{
					System.out.println("Vehicle::move, Arc (" + lastPoint + "," + v + ") does not exists ????????????????");
					log.println("Vehicle[" + ID + "]::move, Arc (" + lastPoint + "," + v + ") does not exists ????????????????");
				}
				//log.println("At " + T.currentTimePoint + ", remainTimeToNextPoint = " + remainTimeToNextPoint);
			}else{
				//status = VehicleStatus.REST_AT_PARKING;
			}
			if(mStatus.get(lastIndexPoint) != null){
				status = mStatus.get(lastIndexPoint);
				if(status == VehicleStatus.REST_AT_PARKING){
					System.out.println("Vehicle["+ ID + "]::move, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
					
							"taxi[" + ID + "] REST AT PARKING");
				}
				log.println("Vehicle[" + ID + "]::move, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"taxi[" + ID + "] changes status = " + getStatusDescription(status) + 
						" at point " + lastPoint);
				
				if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.FINISH_WORK){
					System.out.println("Vehicle::move, ID = " + ID + ", action = FINISH_WORK, status = " + getStatusDescription(status));
					if(status != VehicleStatus.STOP_WORK){
						System.out.println("Vehicle["+ ID + "]::move, ID = " + ID + ", exception inconsistent, action = FINISH_WORK, but status != STOP_WORK");
						System.exit(-1);
					}
				}
				if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PEOPLE){
					if(status != VehicleStatus.PICKUP_PEOPLE){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;// 60;// default pickup time is 60s
					//if(currentItinerary.getRequestID(lastIndexPoint) == 242){
						//System.out.println("Vehicle[" + ID  + "]::move --> pickup people " + 242 + " at time point " + T.currentTimePoint);
						//System.exit(-1);
					//}
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PEOPLE){
					if(status != VehicleStatus.DELIVERY_PEOPLE){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					PeopleRequest pr = sim.getPeopleRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery time is 60s
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.PICKUP_PARCEL){
					if(status != VehicleStatus.PICKUP_PARCEL){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					//System.out.println("Vehicle["+ ID + "]::move, lastIndexPoint = " + lastIndexPoint + 
							//", lastIndex RequestID = " + currentItinerary.getRequestID(lastIndexPoint) +
							//", nextItinerary = " + nextItinerary2String());
					ParcelRequest pr = sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.pickupDuration;//60;// default pickup time is 60s
				}else if(currentItinerary.getAction(lastIndexPoint) == VehicleAction.DELIVERY_PARCEL){
					if(status != VehicleStatus.DELIVERY_PARCEL){
						System.out.println("Vehicle::move, time = " + T.currentTimePoint + " --> INCONSISTENT");
						System.exit(1);
					}
					ParcelRequest pr = sim.getParcelRequest(currentItinerary.getRequestID(lastIndexPoint));
					remainTimeToNextDeparture = pr.deliveryDuration;//60;// default delivery time is 60s
				}  
			}
			
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

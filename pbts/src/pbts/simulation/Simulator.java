package pbts.simulation;

import pbts.entities.Arc;
import pbts.entities.LatLng;
import pbts.entities.ItineraryTravelTime;
import pbts.entities.Parking;
import pbts.entities.Vehicle;
import pbts.gismap.*;
import java.io.*;
import java.util.*;

import pbts.shortestpaths.*;

import pbts.entities.*;
import pbts.enums.*;
class Pair{
	int u,v;
	public Pair(int u, int v){
		this.u = u;
		this.v = v;
	}
}

class TaxiLocation{
	int taxi;
	int point;
	public TaxiLocation(int taxi, int point){
		this.taxi = taxi; this.point = point;
	}
}


public class Simulator {

	/**
	 * @param args
	 */
	public static double maxSpeedkmh = 40;// km per hour
	public static double maxSpeedmm = maxSpeedkmh*1000/60; // meters per minute
	public static double maxSpeedms = maxSpeedkmh*1000/3600; // meters per minute
	public int startWorkingTime = 6*3600;// start work at 6am, convert into seconds
	public int terminateRequestTime = 18*3600;// after 6pm, no request is generated
	public int terminateWorkingTime = 20*3600;// finish work at 8pm, convert into seconds
	public static double minSpeedkmh = 5;// minimum speed that a taxi can ride is 5km/h
	public static double minSpeedmm = minSpeedkmh*1000/60;
	public static double minSpeedms = minSpeedkmh*1000/3600;
	public static int maxWaitTime = 900;// 15 minutes maximum wait time of immediate request (e.g., pickup_time - call_time)
	public static int minArcWeight = 20;
	
	public static int Qk = 1;
	public static double alpha = 10000;// initial fare charge for one passenger service (VND)
	public static double beta = 10000;// initial fare charge for one parcel service (VND)
	public static double gamma1 = 9;//9000/1000;// fare charge per meter (VND) for passenger services 
	public static double gamma2 = 5;//5000/1000; // fare charge per meter (VND) for parcel services
	public static double gamma3 = 2;//2000/1000; // fuel cost: 20 VND per meter = 2000 VND per km (10 litters for 100km -> 0.1l*20000 = 2000VND for 1km)
	public static double gamma4 = 40000;// discount factor for exceeding the direct delivery time of passengers
	public static double maxDistanceFactor = 2;// maximum delivery distance factor
	
	// algorithms parameters
	public static int maxPendingStops = 10;// maximum number of remaining stops (pickup/delivery) to be traversed for each taxi
	
	
	//running parameters
	public int nbPeopleRejects = 0;
	public int nbParcelRejects = 0;
	public int nbPeopleComplete = 0;
	public int nbParcelComplete = 0;
	public int nbPeopleOnBoard = 0;
	public int nbParcelOnBoard = 0;
	public int nbPeopleServed = 0;
	public int nbParcelServed = 0;
	public int nbPeopleWaitBoarding = 0;
	public int nbParcelWaitBoarding = 0;
	
	// statistic information
	public double transportationCost;
	public double revenuePeople;
	public double revenueParcel;
	public double discount;
	public int totalPeopleRequests;
	public int acceptedPeopleRequests;
	public int totalParcelRequests;
	public int acceptedParcelRequests;
	public int nbDisconnectedRequests;
	public ArrayList<Double> distanceRequests;
	
	//public int n,m;// number of vertices and arcs
	//public HashMap<Integer, ArrayList<Arc>> A;// A[i] is the set of adjacent arcs of vertex i
	//public ArrayList<Integer> V;
	//public ArrayList<Arc> Arcs;
	//public HashMap<Integer, LatLng> mLatLng;
	
	public RoadMap map;
	public TimeHorizon T;
	
	public int nbTaxis;// number of taxis
	public int nbPeopleRequests;
	public int nbParcelRequests;
	public int nbParkings;
	
	public ArrayList<ParcelRequest> pendingParcelRequests;
	
	public ArrayList<PeopleRequest> allPeopleRequests;
	public ArrayList<ParcelRequest> allParcelRequests;
	public ArrayList<PeopleRequest> runningPeopleRequests = new ArrayList<PeopleRequest>();
	public ArrayList<ParcelRequest> runningParcelRequests = new ArrayList<ParcelRequest>();
	
	public HashMap<Integer, PeopleRequest> mPeopleRequest;
	public HashMap<Integer, ParcelRequest> mParcelRequest;
	public HashMap<Integer, Integer> mTaxi2Depot;
	
	public ArrayList<Integer> lstDepots;
	public ArrayList<Parking> lstParkings;
	public ArrayList<Integer> lstOrgPeople;
	public ArrayList<Integer> lstDesPeople;
	public ArrayList<Integer> lstOrgParcel;
	public ArrayList<Integer> lstDesParcel;
	public ArrayList<Integer> lstArrTimeOrgPeople;// arrival time at origin of people
	public ArrayList<Integer> lstDepTimeOrgPeople;// departure time at origin of people
	public ArrayList<Integer> lstArrTimeDesPeople;// arrival time at origin of people
	public ArrayList<Integer> lstDepTimeDesPeople;// departure time at origin of people

	public ArrayList<Integer> lstArrTimeOrgParcel;// arrival time at origin of parcel
	public ArrayList<Integer> lstDepTimeOrgParcel;// departure time at origin of parcel
	public ArrayList<Integer> lstArrTimeDesParcel;// arrival time at origin of parcel
	public ArrayList<Integer> lstDepTimeDesParcel;// departure time at origin of parcel
	
	public HashMap<Integer, ArrayList<TaxiLocation>> taxiLocationStatus;// taxiLocationStatus.get(t) is the list of taxi and its corresponding location
	public byte[][] status;// status[k][t] is the status of taxi k at time point t
							// 0: free, 1: riding,
	public int[]	curLocation;// curLocation[k] is the current location of taxi k
	
	public ArrayList<Vehicle> vehicles;
	
	public DijkstraBinaryHeap dijkstra = null;
	
	public HashMap<Integer, Itinerary> mPath = null;
	public HashMap<Integer, Integer> countStop;
	public HashMap<Integer, Double> accumulateDistance;// accumulateDistance.get(rid) is the distance traversed of request rid
	
	Random R = new Random();
	
	// objectives
	public double revenue;
	public double cost;
	
	public PrintWriter log;
	public PrintWriter logI;
	public Simulator(){
		try{
			log = new PrintWriter("Simulator-log.txt");
			logI = new PrintWriter("Simulator-Itinerary-log.txt");
			countStop = new HashMap<Integer, Integer>();
			accumulateDistance = new HashMap<Integer, Double>();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void finalize(){
		log.close();
		logI.close();
	}
	
	public int getNumberParcelRequestEngaged(Vehicle taxi){
		if(taxi.currentItinerary == null) return 0;
		HashSet<Integer> S = taxi.currentItinerary.collectRequestID();
		if(S == null) return 0;
		int count = 0;
		Iterator it = S.iterator();
		while(it.hasNext()){
			int rid = (Integer)it.next();
			if(mParcelRequest.get(rid) != null) count++;
		}
		return count;
	}
	public void loadMapFromTextFile(String filename){
		map = new RoadMap();
		map.loadData(filename);
		//System.exit(-1);
		T = new TimeHorizon(startWorkingTime,terminateRequestTime,terminateWorkingTime);
		
		// set default travel time for all roads
		for(int i = 0; i < map.Arcs.size(); i++){
			Arc a = map.Arcs.get(i);
			map.setTravelTime(a, getTravelTime(a,maxSpeedms));
		}
		dijkstra = new DijkstraBinaryHeap(map.V,map.A);
		/*
		double t0 = System.currentTimeMillis();
		double D = dijkstra.queryDistance(1, 130942);
		double ti = System.currentTimeMillis() - t0;
		ti = ti*0.001;
		System.out.println("Distance = " + D + ", Time = " + ti);
		System.exit(-1);
		*/
		
		/*
		try{
			
			Scanner in = new Scanner(new File(filename));
			n = 0;
			V = new ArrayList<Integer>();
			A = new HashMap<Integer, ArrayList<Arc>>();
			Arcs = new ArrayList<Arc>();
			mLatLng = new HashMap<Integer, LatLng>();
			double minDis = 999999999;
			double maxDis = -minDis;
			while(true){
				int u = in.nextInt();
				if(u == -1) break;
				double lat = in.nextDouble();
				double lng = in.nextDouble();
				LatLng ll = new LatLng(lat,lng);
				mLatLng.put(u, ll);
				n++;
				V.add(u);
				A.put(u,  new ArrayList<Arc>());
			}
			
			
			m = 0;
			while(true){
				int u = in.nextInt();
				if(u == -1) break;
				int v = in.nextInt();
				double w = in.nextDouble();
				if(minDis > w) minDis = w;
				if(maxDis < w) maxDis = w;
				Arc a = new Arc(u,v,w);
				Arcs.add(a);
				m++;
				A.get(u).add(a);
			}
			in.close();
			
			for(int i = 0; i < V.size(); i++){
				int v = V.get(i);
				System.out.print("A[" + v + "] = ");
				for(int j = 0; j < A.get(v).size(); j++){
					Arc a = A.get(v).get(j);
						System.out.print(a.end + ", ");
				}
				System.out.println();
			}
			
			System.out.println("n = " + V.size() + ", m = " + m + ", minDis = " + minDis + ", maxDis = " + maxDis);
			dijkstra = new DijkstraBinaryHeap(V,A);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		*/
	}
	public ArrayList<Pair> gen(int depot, double maxLength){
		// generate and return list of source-destination such that itinerary starting from depot and terminating at depot does not exceed maxLength
		ArrayList<Pair> L = new ArrayList<Pair>();
		DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(map.V, map.A);
		double totalLength = 0;
		int curPos = depot;
		while(true){
			int o = -1;
			int d = -1;
			while(true){
				o = map.V.get(R.nextInt(map.n));
				d = map.V.get(R.nextInt(map.n));
				if(o != d) break;
			}
			//double dis1 = dijkstra.solve(curPos,o);
			//double dis2 = dijkstra.solve(o, d);
			//double dis3 = dijkstra.solve(d, depot);
			Itinerary I1 = dijkstra.solve(curPos,o);
			Itinerary I2 = dijkstra.solve(o, d);
			Itinerary I3 = dijkstra.solve(d, depot);
			
			if(totalLength + I1.getDistance() + 
					I2.getDistance() + I3.getDistance() > maxLength) break;
			L.add(new Pair(o,d));
			curPos = d;
			totalLength += I1.getDistance() + I2.getDistance();
			
		}
		return L;
	}
	public static int getTravelTime(double dis, double speedms){
		int t = (int)Math.ceil(dis/speedms);
		if(t == 0) t = 1;
		return t;
	}
	public static int getTravelTime(Arc a, double speedms){
		int t = (int)Math.ceil(a.w/speedms);
		if(t == 0) t = 1;
		return t;
	}
	public int genPoint(int cur, double minL, double maxL){
		ArrayList<Integer> L = new ArrayList<Integer>();
		Queue<Integer> Q = new LinkedList<Integer>();
		HashMap<Integer, Double> dis = new HashMap<Integer, Double>();
		Q.add(cur);
		dis.put(cur, 0.0);
		while(Q.size() > 0){
			int v = Q.poll();
			Iterator it = map.A.get(v).iterator();
			while(it.hasNext()){
				Arc a = (Arc)it.next();
				if(dis.get(a.end) == null){
					dis.put(a.end, dis.get(v) + a.w);
					if(dis.get(a.end) < maxL)
						Q.add(a.end);
					if(dis.get(a.end) > minL && dis.get(a.end) < maxL) L.add(a.end);
				}
			}
		}
		if(L.size() <= 0) return -1;
		return L.get(R.nextInt(L.size()));
	}
	public ArrayList<Integer> genItinerary(int depot, double maxLen, double maxTime, double minL, double maxL){
		ArrayList<Integer> L = new ArrayList<Integer>();
		DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(map.V, map.A);
		double totalLength = 0;
		double totalTime = 0;
		int curPos = depot;
		while(true){
			int v = genPoint(curPos,minL,maxL);// V.get(R.nextInt(n));
			if(v == -1) return null;
			Itinerary I1 = dijkstra.solve(curPos, v);
			double dis1 = I1.getDistance();
			//if(dis1 > maxDis) continue;
			Itinerary I2 = dijkstra.solve(v, depot);
			double dis2 = I2.getDistance();
			if(dis2 > dijkstra.infinity-1) continue;
			
			double t1 = getTravelTime(dis1, maxSpeedms);//dis1/speedms;
			double t2 = getTravelTime(dis2, maxSpeedms);//dis2/speedms;
			if(totalLength + dis1 + dis2 > maxLen) break;
			if(totalTime + t1 + t1 > maxTime) break;
			L.add(v);
			curPos = v;
			totalLength += dis1;
			totalTime += t1;
			//System.out.println("Admit point " + v + " totalLength = " + totalLength + ", totalTime = " + totalTime);
		}
		return L;
	}
	public int findAvailableTaxi(int t, int src, int des){
		//TODO
		//System.out.println("MapBasedDataGenerator::findAvailableTaxi not implemented");
		//System.exit(1);
		int sel_taxi = -1;
		double minDis = dijkstra.infinity;
		for(int k = 0; k < nbTaxis; k++){
			if(getStatus(k,t) == 0){// taxi k is free, available for serving requests
				int pos = curLocation[k];//getLocation(t, k);
				Itinerary I = dijkstra.solve(pos, src); 
				double L = I.getDistance();
				if(L < minDis){
					minDis = L; sel_taxi = k;
				}
			}
		}
		return sel_taxi;
	}
	public Vehicle findPossibleTaxi(int src, int des, int maxTime){
		// find a taxi that can go to pickup people at src within time maxTime
		Vehicle sel_taxi = null;
		double minDis = dijkstra.infinity;
		int count1 = 0;
		int count2 = 0;
		for(int k = 0; k < nbTaxis; k++){
			Vehicle vh = vehicles.get(k);
			if(vh.status == VehicleStatus.NOT_WORK) continue;
			if(vh.status == VehicleStatus.REST_AT_PARKING){
				count1++;
				//Itinerary I = dijkstra.solve(vh.lastPoint, src);
				double L = dijkstra.queryDistance(vh.lastPoint, src);
				//if(I != null) L = I.getDistance();
				if(L < minDis){
					minDis = L; sel_taxi = vh;
				}
			}else if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
				count2++;
				//Itinerary I = dijkstra.solve(vh.currentItinerary.get(vh.lastIndexPoint+1), src); 
				double L = dijkstra.queryDistance(vh.currentItinerary.get(vh.lastIndexPoint+1), src); 
				//if(I != null) L = I.getDistance();
				if(L < minDis){
					minDis = L; sel_taxi = vh;
				}
			}
		}
		log.println("findPossibleTaxi(" + src + "," + des + "," + maxTime + "), minDis = " + minDis + 
				", minTime = " + getTravelTime(minDis,maxSpeedms) + ", number of taxis resting = " + count1 + 
				", number of taxis travling without load = " + count2 + ", total taxis = " + nbTaxis);
		if(getTravelTime(minDis,maxSpeedms) > maxTime) sel_taxi = null; 
		return sel_taxi;
	}
	public int getLocation(int t, int k){
		// return the location of taxi k at time point t
		ArrayList<TaxiLocation> L = taxiLocationStatus.get(t);
		if(L != null)
		for(int i = 0; i < L.size(); i++){
			TaxiLocation tl = L.get(i);
			if(tl.taxi == k) return tl.point;
		}
		return -1;
	}
	public void printPath(int[] path){
		for(int i = 0; i < path.length; i++)
			System.out.print(path[i] + ",");
		System.out.println();
	}
	
	public void setupRoute(int taxi, int t, int src, int des){
		// compute a route for taxi at current time t
		// update TaxiLocation data structure
		//System.out.println("MapBasedDataGenerator::setUpRoute not implemented");
		//System.exit(1);
		System.out.println("setupRoute(" + taxi + "," + t + "," + src + "," + des + ")");
		int curPos = curLocation[taxi];//getLocation(taxi,t);
		Itinerary I1 = dijkstra.solve(curPos, src);
		//double cost1 = I1.getDistance();
		//if(cost1 >= dijkstra.infinity){
		if(I1 == null){
			System.out.println("setupRoute(" + taxi + "," + t + "," + src + "," + des + " --> cannot find path from curPos = " + curPos + " to src = " + src);
			
			return;
		}
		int[] path1 = dijkstra.getPath(curPos, src);
		System.out.print("path1 = "); printPath(path1);
		Itinerary I2 = dijkstra.solve(src, des);
		//double cost2 = I2.getDistance();
		//if(cost2 >= dijkstra.infinity){
		if(I2 == null){
			System.out.println("setupRoute(" + taxi + "," + t + "," + src + "," + des + " --> cannot find path from src = " + src + " to des = " + des);
			return;
		}
		int[] path2 = dijkstra.getPath(src, des);
		System.out.print("path2 = "); printPath(path2);
		double cost3 = dijkstra.infinity;
		
		int[] path3 = null;
		
		// find nearest parkings
		for(int i = 0; i < lstParkings.size(); i++){
			int p = lstParkings.get(i).locationID;
			Itinerary Ii = dijkstra.solve(des, p); 
			double Li = dijkstra.infinity;
			if(Ii != null) Li = Ii.getDistance();
			if(Li < cost3){
				cost3 = Li;
				path3 = dijkstra.getPath(des, p);
			}
		}
		if(cost3 >= dijkstra.infinity){
			System.out.println("setupRoute(" + taxi + "," + t + "," + src + "," + des + " --> cannot find path from des = " + des + " to a parking");
			return;
		}
		System.out.print("path3 = "); printPath(path3);
		int[] path = new int[path1.length + path2.length + path3.length - 2];
		int idx = -1;
		for(int i = 0; i < path1.length-1; i++){
			idx++;
			path[idx] = path1[i];
		}
		for(int i = 0; i < path2.length-1; i++){
			idx++;
			path[idx] = path2[i];
		}
		for(int i = 0; i < path3.length; i++){
			idx++;
			path[idx] = path3[i];
		}
		int ti = t;
		
		for(int i = 0;i < path.length-1; i++){
			int u = path[i];
			int v = path[i+1];
			Arc a = map.getArc(u,v);
			if(a == null){
				System.out.println("getArc(" + u + "," + v + ") --> exception BUG arc not exists");
				System.exit(1);
			}
			int tt = getTravelTime(a, maxSpeedms);
			ti = ti + tt;
			
			if(taxiLocationStatus.get(ti) == null) taxiLocationStatus.put(ti, new ArrayList<TaxiLocation>());
			ArrayList tl = taxiLocationStatus.get(ti);
			tl.add(new TaxiLocation(taxi,v));
		}
		for(int i = t; i <= ti; i++){
			//status[taxi][i] = 1;// taxi is busy at time i
			byte stat = 1;
			setStatus(taxi,i,stat);// taxi is busy at time i
		}
		System.out.print("setupRoute(" + taxi + "," + t + "," + src + "," + des + " paths = ");
		for(int i = 0;i < path.length; i++){
			System.out.print(path[i] + ",");
		}
		System.out.println();
	}
	public void setupRoute(Vehicle vh, int t, PeopleRequest pr){
		// compute a route for taxi at current time t
		// update TaxiLocation data structure
		//System.out.println("MapBasedDataGenerator::setUpRoute not implemented");
		//System.exit(1);
		int src = pr.pickupLocationID;
		int des = pr.deliveryLocationID;
		System.out.println("setupRoute(" + vh.toString() + "," + t + "," + src + "," + des + ")");
		int curPos = vh.lastPoint;
		if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
			curPos = vh.currentItinerary.get(vh.lastIndexPoint+1);
		}
		Itinerary I1 = dijkstra.solve(curPos, src);
		//double cost1 = I1.getDistance();
		//if(cost1 >= dijkstra.infinity){
		if(I1 == null){
			System.out.println("setupRoute(" + vh.toString() + "," + t + "," + src + "," + des + " --> cannot find path from curPos = " + curPos + " to src = " + src);
			log.println("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ") path(" + curPos + " -> " + src + ") --> failed");
			return;
		}
		int[] path1 = dijkstra.getPath(curPos, src);
		System.out.print("path1 = "); printPath(path1);
		Itinerary I2 = dijkstra.solve(src, des);
		//double cost2 = I2.getDistance();
		//if(cost2 >= dijkstra.infinity){
		if(I2 == null){
			System.out.println("setupRoute(" + vh.toString() + "," + t + "," + src + "," + des + " --> cannot find path from src = " + src + " to des = " + des);
			log.println("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ") path(" + src + " -> " + des + ") --> failed");
			return;
		}
		int[] path2 = dijkstra.getPath(src, des);
		System.out.print("path2 = "); printPath(path2);
		double cost3 = dijkstra.infinity;
		
		int[] path3 = null;
		
		// find nearest parkings
		for(int i = 0; i < lstParkings.size(); i++){
			int p = lstParkings.get(i).locationID;
			Itinerary Ii = dijkstra.solve(des, p); 
			double Li = dijkstra.infinity;
			if(Ii != null) Li = Ii.getDistance();
			if(Li < cost3){
				cost3 = Li;
				path3 = dijkstra.getPath(des, p);
			}
		}
		if(cost3 >= dijkstra.infinity){
			System.out.println("setupRoute(" + vh.toString() + "," + t + "," + src + "," + des + " --> cannot find path from des = " + des + " to a parking");
			log.println("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ") path(" + des + " -> parkings) --> failed");
			return;
		}
		System.out.print("path3 = "); printPath(path3);
		//ArrayList<Integer> arrTime = new ArrayList<Integer>();
		//ArrayList<Integer> depTime = new ArrayList<Integer>();
		ArrayList<Integer> requestID = new ArrayList<Integer>();
		ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
		
		HashMap<Integer, VehicleStatus> mStatus = new HashMap<Integer, VehicleStatus>();
		mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PEOPLE);
		//log.println("Simulator::setupRoute, mStatus.put(" + 0 + "," + vh.getStatusDescription(VehicleStatus.GOING_TO_PICKUP_PEOPLE) + ")");
		int[] path = new int[path1.length + path2.length + path3.length - 2];
		int idx = -1;
		//arrTime.add(T.currentTimePoint);
		for(int i = 0; i < path1.length-1; i++){
			idx++;
			path[idx] = path1[i];
			Arc a = map.getArc(path1[i], path1[i+1]);
			int tt = getTravelTime(a, maxSpeedms);
			//depTime.add(arrTime.get(arrTime.size()-1));
			//arrTime.add(depTime.get(depTime.size()-1) + tt);
			requestID.add(-1);// no request served at this point, vehicle just passes
			actions.add(VehicleAction.PASS);
		}
		actions.add(VehicleAction.PICKUP_PEOPLE);
		requestID.add(pr.id);
		
		//vh.mStatus.put(idx+1,VehicleStatus.GOING_TO_DELIVERY_PEOPEL);
		mStatus.put(idx+1,VehicleStatus.PICKUP_PEOPLE);
		int d = idx+1;
		//log.println("Simulator::setupRoute, mStatus.put(" + d + "," + vh.getStatusDescription(VehicleStatus.PICKUP_PEOPLE) + ")");
		//mService.put(idx+1, pr.id);
		for(int i = 0; i < path2.length-1; i++){
			idx++;
			path[idx] = path2[i];
			Arc a = map.getArc(path2[i], path2[i+1]);
			int tt = getTravelTime(a, maxSpeedms);
			if(i == 0){
				//depTime.add(arrTime.get(arrTime.size()-1) + 60);// wait time at the point for pickup is 60 seconds
			}else{
				//depTime.add(arrTime.get(arrTime.size()-1));// wait time at the point for pickup is 60 seconds
			}	
			//arrTime.add(depTime.get(depTime.size()-1) + tt);
			
			if(i > 0){
				actions.add(VehicleAction.PASS);
				requestID.add(-1);// just passes, no vehicle served
			}
		}
		actions.add(VehicleAction.DELIVERY_PEOPLE);
		requestID.add(pr.id);
		//vh.mStatus.put(idx+1, VehicleStatus.TRAVEL_WITHOUT_LOAD);
		mStatus.put(idx+1, VehicleStatus.DELIVERY_PEOPLE);
		d = idx+1;
		//log.println("Simulator::setupRoute, mStatus.put(" + d + "," + vh.getStatusDescription(VehicleStatus.DELIVERY_PEOPLE) + ")");
		//vh.mService.put(idx+1, pr.id);
		for(int i = 0; i < path3.length; i++){
			idx++;
			path[idx] = path3[i];
			if(i+1 < path3.length){
				Arc a = map.getArc(path3[i], path3[i+1]);
				int tt = getTravelTime(a, maxSpeedms);
				if(i == 0){
					//depTime.add(arrTime.get(arrTime.size()-1) + 60);// wait time at the point for pickup is 60 seconds
				}else{
					//depTime.add(arrTime.get(arrTime.size()-1));// wait time at the point for pickup is 60 seconds
				}
				//arrTime.add(depTime.get(depTime.size()-1) + tt);
				if( i > 0)actions.add(VehicleAction.PASS);
			}else{
				if(i > 0)actions.add(VehicleAction.STOP);
			}
			if(i > 0)requestID.add(-1);
		}
		mStatus.put(idx, VehicleStatus.REST_AT_PARKING);
		d = idx+1;
		//log.println("Simulator::setupRoute, mStatus.put(" + d + "," + vh.getStatusDescription(VehicleStatus.REST_AT_PARKING) + ")");
		
		//vh.currentItinerary = new ItineraryTravelTime(path,requestID, actions);//new Itinerary(path);
		vh.nextItinerary = new ItineraryTravelTime(path,requestID, actions);//new Itinerary(path);
		vh.mNextStatus = mStatus;
		if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
			vh.status = VehicleStatus.PREPARE_NEW_ITINERARY;
		}else if(vh.status == VehicleStatus.REST_AT_PARKING){
			vh.status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
			vh.setNewItinerary();
			//log.println("Simulator::setupRoute(" + vh.ID + ") --> Next Itinerary is: " + vh.nextItinerary2String());
		}
		/*
		vh.addItinerary(vh.currentItinerary);
		Arc a = map.getArc(path[0], path[1]);
		vh.lastPoint = path[0];
		vh.lastIndexPoint  = 0;
		vh.remainTimeToNextPoint = getTravelTime(a, maxSpeedms); 
		vh.totalTravelDistance += a.w;
		//vh.status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
		vh.status = VehicleStatus.PREPARE_NEW_ITINERARY;
		vh.startTimePointNewItinerary = T.currentTimePoint + vh.remainTimeToNextPoint; 
		*/
		if(actions.size() != path.length){
			System.out.println("BUG actions.sz = " + actions.size() + " != path.length = " + path.length);
			System.exit(1);
		}
		System.out.print("setupRoute(" + vh.toString() + "," + t + "," + src + "," + des + " paths = ");
		for(int i = 0;i < path.length; i++){
			System.out.print(path[i] + ",");
		}
		System.out.println();
		
		//log.print("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ", path(" + path.length + ") = ");
		//for(int i = 0; i < path.length; i++) log.print(path[i] + ","); log.println();
	}
	
	public Parking findParking(int locID){
		for(int i = 0; i < lstParkings.size(); i++){
			Parking p = lstParkings.get(i);
			if(p.locationID == locID) return p;
		}
		return null;
	}
	/*
	public int getNextStartPoint(Vehicle taxi){
		
		ItineraryTravelTime I = taxi.currentItinerary;
		if(I == null) return taxi.lastPoint;
		
		int nextStartPoint = -1;
		if(taxi.status == VehicleStatus.REST_AT_PARKING || taxi.status == VehicleStatus.PICKUP_PARCEL ||
				 taxi.status == VehicleStatus.PICKUP_PEOPLE ||  taxi.status == VehicleStatus.DELIVERY_PARCEL ||
				 taxi.status == VehicleStatus.DELIVERY_PEOPLE){
			nextStartPoint = taxi.lastPoint;
		}else{
			if(taxi.lastIndexPoint < I.size()-1){
				nextStartPoint = I.get(taxi.lastIndexPoint+1);
			}else{
				System.out.println("SimulatorBookedRequest::getNextStartPoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.println("SimulatorBookedRequest::getNextStartPoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.close();
				System.exit(-1);
			}
		}
		return nextStartPoint;
	}
	public int getNextStartTimePoint(Vehicle taxi){
		int startTimePoint = -1;//T.currentTimePoint + 1;
		ItineraryTravelTime I = taxi.currentItinerary;
		if(I == null) return T.currentTimePoint + 1;
		if(taxi.status == VehicleStatus.REST_AT_PARKING){
			startTimePoint = I.getDepartureTime(taxi.lastIndexPoint);
			if(startTimePoint < 0)// not predefined yet
				startTimePoint = T.currentTimePoint+1;
		}else if(taxi.status == VehicleStatus.PICKUP_PARCEL ||
				 taxi.status == VehicleStatus.PICKUP_PEOPLE ||  taxi.status == VehicleStatus.DELIVERY_PARCEL ||
				 taxi.status == VehicleStatus.DELIVERY_PEOPLE){
			startTimePoint = I.getDepartureTime(taxi.lastIndexPoint);
			if(startTimePoint < 0)// not predefined yet
				startTimePoint = T.currentTimePoint+1;
		}else{
			if(taxi.lastIndexPoint < I.size()-1){// traveling
				startTimePoint = I.getArrivalTime(taxi.lastIndexPoint+1);
			}else{
				System.out.println("SimulatorBookedRequest::getNextStartTimePoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.println("SimulatorBookedRequest::getNextStartTimePoint " +
						"BUG???, taxi.status = " + taxi.getStatusDescription(taxi.status) + " BUT itinerary ended lastIndexPoint = Itinerary.sz = " + I.size());
				log.close();
				System.exit(-1);
			}
		}
		return startTimePoint;
	}
	*/
	
	public int getLocationFromEncodedRequest(int rid){
		int arid = Math.abs(rid);
		ParcelRequest parcelreq = mParcelRequest.get(arid);
		if(parcelreq != null){
			if(rid > 0) return parcelreq.pickupLocationID; else return parcelreq.deliveryLocationID;
		}else{
			PeopleRequest peopleReq = mPeopleRequest.get(arid);
			if(rid > 0) return peopleReq.pickupLocationID; else return peopleReq.deliveryLocationID;
		}
		
	}
	public ArrayList<Integer> collectAvailableParkings(Vehicle taxi){
		//HashSet<Integer> AP = new HashSet<Integer>();
		ArrayList<Integer> L = new ArrayList<Integer>();
		for(int k1 = 0; k1 < lstParkings.size(); k1++){
			Parking pk = lstParkings.get(k1);
			if(pk.load < pk.capacity)
				L.add(pk.locationID);
		}
		L.add(mTaxi2Depot.get(taxi.ID));// depot is also an available parking
		
		if(taxi.currentItinerary != null){
			int p = taxi.currentItinerary.get(taxi.currentItinerary.size()-1);
			if(L.indexOf(p) < 0 || L.indexOf(p) >= L.size())
				L.add(p);// last point of currentItinerary is also an available parking
		}
		
		//for(int i = 0; i < L.size(); i++){
			//System.out.println(L.get(i) + ",");
		//}
		/*
		System.out.println("Simulator::collectAvailable parkings, AP.sz = " + AP.size());
		Iterator it = AP.iterator();
		while(it.hasNext()){
			int u = (Integer)it.next();
			L.add(u);
		}
		*/
		return L;
	}
	public SolutionRoute computeBenefitOptimizeRoute(Vehicle vh, int t, PeopleRequest pr, ArrayList<ParcelRequest> parcels){
		int[] v = new int[2*(parcels.size()+1)];
		v[0] = pr.pickupLocationID;
		v[1] = pr.deliveryLocationID;
		for(int i = 0; i < parcels.size(); i++){
			v[2+2*i] = parcels.get(i).pickupLocationID;
			v[2+2*i+1] = parcels.get(i).deliveryLocationID;
		}
		
		PermutationGenerator perm = new PermutationGenerator(v.length);
		perm.generate();
		
		double maxBenefits = -99999999;
		double distance = 0;
		//ArrayList<Itinerary> bestItineraries = null;
		int[] bestSequenceNodes = null;
		ItineraryTravelTime bestItinerary = null;
		
		int endNod = -1;
	
		for(int k = 0; k < perm.size(); k++){
			int[] p = perm.get(k);
			//System.out.print("perm " + k + "th = ");
			//for(int i = 0; i < p.length; i++) System.out.print(p[i] + ","); System.out.println();
			
			int[] idx = new int[p.length];
			for(int i = 0; i < p.length; i++) idx[p[i]] = i;
			boolean ok = true;
			for(int i = 0; i < p.length/2; i++)
				if(idx[2*i] > idx[2*i+1]){
					ok = false;
					break;
				}
			if(!ok){
				//System.out.println("KO");
				continue;
			}
			
			int[] nod = new int[v.length+1];
			int curPos = vh.lastPoint;
			double delta_t = 0;
			if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
				curPos = vh.currentItinerary.get(vh.lastIndexPoint+1);
				Arc a = map.getArc(vh.lastPoint, curPos);
				delta_t = getTravelTime(a, maxSpeedms);
			}
			nod[0] = curPos;
			for(int i = 0; i < p.length; i++){
				nod[i+1] = v[p[i]];
			}
			//System.out.print("Simulator::computeOptimizeBenefits, nod = ");
			//for(int i = 0; i < nod.length; i++) System.out.print(nod[i] + ","); System.out.println();
			
			// compute travel distance
			double dis = 0;
			double peopleDis = 0;
			double distancePickupPeople = 0;
			double distanceDeliveryPeople = 0;
			boolean computeDistancePickupPeople = true;
			boolean computeDistanceDeliveryPeople = true;
			boolean accumulatePeopleDistance = false;
			int extraPickupDuration = 0;
			int extraDeliveryDuration = 0;
			int nbStops = 0;
			int td = t+1;
			//ArrayList<Itinerary> II = new ArrayList<Itinerary>();
			for(int i = 0; i < p.length; i++){
				//Itinerary I = dijkstra.solve(nod[i], nod[i+1]);
				//if(I == null){ ok = false; break;}
				//II.add(I);
				//System.out.println("Simulator::computeOptimizeBenefits, queryDistance(" + nod[i] + " -> " + nod[i+1]);
				double Di = dijkstra.queryDistance(nod[i], nod[i+1]);
				//System.out.println("Simulator::computeOptimizeBenefits, queryDistance(" + nod[i] + " -> " + nod[i+1] + ", Di = " + Di);
				
				if(vh.ID == 11 && pr.id == 1094){
					log.println("Simulator::computeBenefitsOptmalRoute, taxi = " + vh.ID + ", status = " + vh.getStatusDescription(vh.status) + ", people request = " + pr.id + 
							", i = " + i + ", Itinerary(" + nod[i] + " --> " + nod[i+1] + ") IS (vh.lastIndexPoint = " + vh.lastIndexPoint + 
							", vh.lastPoint = " + vh.lastPoint + ", vh.itinerary.sz = " + vh.currentItinerary.size() + 
							", vh.lastIndexPoint+1 = " + vh.currentItinerary.get(vh.lastIndexPoint+1) + 
							", vh.itinerary.last = " + vh.currentItinerary.get(vh.currentItinerary.size()-1) + ", curPos = " + curPos);
					Itinerary II = dijkstra.queryShortestPath(nod[i], nod[i+1]);
					for(int ii = 0; ii < II.size(); ii++) log.println(II.get(ii));
				}
				if(Di > dijkstra.infinity-1){ ok = false; break;}
				dis = dis + Di;//I.getDistance();
				int t_dur = getTravelTime(Di, maxSpeedms);
				int ta = td + t_dur;
				
				if(nod[i+1] == pr.pickupLocationID){
					accumulatePeopleDistance = true;
					computeDistancePickupPeople = false;
					if(ta + pr.pickupDuration < pr.earlyPickupTime || ta + pr.pickupDuration > pr.latePickupTime){
						ok = false; break;
					}
					td = ta + pr.pickupDuration;
				}
				/*
				if(accumulatePeopleDistance){
					peopleDis = peopleDis + Di;//I.getDistance();
				}
				if(nod[i+1] == pr.deliveryLocationID){
					accumulatePeopleDistance = false;
				}
				*/
				if(nod[i+1] == pr.deliveryLocationID){
					peopleDis = peopleDis + Di;
					accumulatePeopleDistance = false;
					
					distanceDeliveryPeople += Di;
					computeDistanceDeliveryPeople = false;
					if(ta < pr.earlyDeliveryTime || ta > pr.lateDeliveryTime){
						ok = false; break;
					}
					td = ta + pr.deliveryDuration;
				}
				
				for(int j = 0; j < parcels.size(); j++){
					ParcelRequest prj = parcels.get(j);
					if(nod[i+1] == prj.pickupLocationID){
						if((ta < prj.earlyPickupTime || ta > prj.latePickupTime) && 
								(ta + prj.pickupDuration < prj.earlyPickupTime || ta + prj.pickupDuration > prj.latePickupTime)){
							ok = false; break;
						}
					}else if(nod[i+1] == prj.deliveryLocationID){
						if((ta < prj.earlyDeliveryTime || ta > prj.lateDeliveryTime) && 
								(ta + prj.deliveryDuration < prj.earlyDeliveryTime || ta + prj.deliveryDuration > prj.lateDeliveryTime)){
							ok = false; break;
						}
					}else{
						//System.out.println("Simulator::computeBenefitsOptimalRoute -> Unknown nod --> BUG??????");
						//System.exit(-1);
					}
				}
				if(accumulatePeopleDistance){
					peopleDis = peopleDis + Di;//I.getDistance();
					for(int j = 0; j < parcels.size(); j++){
						ParcelRequest prj = parcels.get(j);
						if(prj.pickupLocationID == nod[i+1] || prj.deliveryLocationID == nod[i+1])
							nbStops++;
					}
				}
				
				if(computeDistancePickupPeople){
					distancePickupPeople += Di;
					for(int j = 0; j < parcels.size(); j++){
						ParcelRequest prj = parcels.get(j);
						if(prj.pickupLocationID == nod[i+1])
							extraPickupDuration += pr.pickupDuration;
						if(prj.deliveryLocationID == nod[i+1])
							extraPickupDuration += pr.deliveryDuration;
							
					}
				}
				if(computeDistanceDeliveryPeople){
					distanceDeliveryPeople += Di;
					for(int j = 0; j < parcels.size(); j++){
						ParcelRequest prj = parcels.get(j);
						if(prj.pickupLocationID == nod[i+1])
							extraDeliveryDuration += pr.pickupDuration;
						if(prj.deliveryLocationID == nod[i+1])
							extraDeliveryDuration += pr.deliveryDuration;
							
					}
				}
			}
			if(!ok) continue;
			
			/*
			double maxSpeedPickup = (distancePickupPeople)/(pr.earlyPickupTime - pr.pickupDuration - extraPickupDuration);
			double minSpeedPickup = (distancePickupPeople)/(pr.latePickupTime - pr.pickupDuration - extraPickupDuration);
			if(minSpeedPickup > maxSpeedms || maxSpeedPickup < minSpeedms) continue;
			
			
			delta_t += 0;//100;
			double speed = maxSpeedms;// - (maxSpeedms - minSpeedms)*1.0/4;
			*/
			
			/*
			double t_early_pickuppeople = t + getTravelTime(distancePickupPeople, maxSpeedms) + extraPickupDuration;
			double t_late_pickuppeople = t + getTravelTime(distancePickupPeople, minSpeedms) + extraPickupDuration;
			double t_early_deliverypeople = t + getTravelTime(distanceDeliveryPeople, maxSpeedms) + extraDeliveryDuration;
			double t_late_deliverypeople = t + getTravelTime(distanceDeliveryPeople, minSpeedms) + extraDeliveryDuration;
			
			
			if(t_late_pickuppeople + pr.pickupDuration <= pr.earlyPickupTime || 
					t_early_pickuppeople + pr.pickupDuration  >= pr.latePickupTime) continue;
			
			if(t_late_deliverypeople <= pr.earlyDeliveryTime || t_early_deliverypeople >= pr.lateDeliveryTime) continue;
			*/
			
			if(nbStops > pr.maxNbStops) continue;
			
			double t_costFuel = getCostFuel(dis);
			double t_revenue = 0;//getPeopleRevenue(Di);
			
			for(int i = 0; i < parcels.size(); i++){
				ParcelRequest parcR = parcels.get(i);
				//Itinerary Li = dijkstra.solve(parcR.pickupLocationID, parcR.deliveryLocationID);
				double Di = dijkstra.queryDistance(parcR.pickupLocationID, parcR.deliveryLocationID);
				t_revenue = t_revenue + getParcelRevenue(Di);//Li.getDistance());
			}
			double t_discount = 0;
			//Itinerary I = dijkstra.solve(pr.pickupLocationID, pr.deliveryLocationID);
			//if(peopleDis > I.getDistance()*maxDistanceFactor) continue;
			double expectedDis = dijkstra.queryDistance(pr.pickupLocationID, pr.deliveryLocationID);
			//if(peopleDis > expectedDis*maxDistanceFactor) continue;
			if(peopleDis > pr.maxTravelDistance) continue;
			
			t_revenue = t_revenue + getPeopleRevenue(expectedDis);
			
			t_discount = getDiscount(expectedDis,peopleDis);
			double benefits = t_revenue - t_costFuel - t_discount;
			
			
			
			if(benefits > maxBenefits){
				
				
				ArrayList<Itinerary> itineraries = new ArrayList<Itinerary>();
				for(int i = 0; i < nod.length-1; i++){
					Itinerary I = dijkstra.queryShortestPath(nod[i], nod[i+1]);
					itineraries.add(I);
				}
				ItineraryTravelTime I = establishItinerary(itineraries, nod[1], pr, parcels);
				
				if(!assignTimePoint(I, vh, t+1, pr, parcels)){
					//System.out.println("Simulator::computeBenefitsOptimalRoute --> assignTimePoint FALSE, BUG?????");
					//System.exit(-1);
					continue;
				}
				maxBenefits = benefits;
				//bestItineraries = II;
				//bestSequenceNodes = nod;
				endNod = nod[nod.length-1];
				distance = dis;
				bestItinerary = I;
				log.println("Simulator::computeBenefitsOptimalRoute, admit bestItinerary I end point = " + I.get(I.size()-1));
			}
			//System.out.println("Simulator::computeOptimizeBenefits, expectedDis = " + expectedDis + ", peopleDis = " + 
				//	peopleDis + ", distance = " + dis + ", t_revenue = " + t_revenue + ", t_cost_fuel = " + t_costFuel + 
					//", t_discount = " + t_discount + ", benefits = " + benefits + ", maxBenefits = " + maxBenefits);
		}
		
		if(endNod == -1){
			return null;// no feasible path
		}
		/*
		if(bestSequenceNodes == null) return null;
		bestItineraries = new ArrayList<Itinerary>();
		
		for(int i = 0; i < bestSequenceNodes.length-1; i++){
			Itinerary I = dijkstra.solve(bestSequenceNodes[i], bestSequenceNodes[i+1]);
			bestItineraries.add(I);
		}
		*/
		
		int t12 = getTravelTime(distance, maxSpeedms);
		boolean stopWorking = false;
		if(t + t12 > T.endrequest) stopWorking = true;
		
		Itinerary sel_Ip = null;
		
		if(stopWorking){
			sel_Ip = dijkstra.solve(endNod, mTaxi2Depot.get(vh.ID));
			if(sel_Ip== null){
				System.out.println("Simulator::setupRouteWithParcelsInsertedFollow(" + vh.toString() + "," + t + 
						"," +  endNod + "," + mTaxi2Depot.get(vh.ID) + " --> cannot find path from src = " + endNod + " to des = " + mTaxi2Depot.get(vh.ID));
				log.println("Simulator::setupRouteWithParcelsInsertedFollow(" + vh.toString() + "," + t + 
						"," +  endNod + "," + mTaxi2Depot.get(vh.ID) + " --> cannot find path from src = " + endNod + " to des = " + mTaxi2Depot.get(vh.ID));
				//log.println("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ") path(" + src + " -> " + endNod + ") --> failed");
				return null;
			}
			//path3 = dijkstra.getPath(des, mTaxi2Depot.get(vh.ID));
			//System.out.println("Simulator::setupRouteWithParcelFollow(" + vh.toString() + ") stopWorking, path3 back to depot = " + path3.length);
		}else{
			
			double minDis = 100000000;
			int sel_p = -1;
			int count = 0;
			ArrayList<Parking> L = new ArrayList<Parking>();
			for(int i = 0; i < lstParkings.size(); i++){
				Parking p = lstParkings.get(i);
				if(p.load < p.capacity) L.add(p);
			}
			int[] P = new int[L.size()+1];
			for(int i = 0; i < L.size(); i++){
				P[i] = L.get(i).locationID;
			}
			/*
			log.println("Simulator::setupRouteWithParcelInserted, available parkings = ");
			for(int i = 0; i < L.size(); i++){
				log.println(L.get(i).locationID + ", cap = " + L.get(i).capacity + ", " +
						"load = " + L.get(i).load + ", lastUpdateTime = " + L.get(i).lastUpdateTimePoint);
			}
			*/
			P[L.size()] = mTaxi2Depot.get(vh.ID);
			for(int i = 0; i < P.length; i++){
				int p = P[i];
				double Di = dijkstra.queryDistance(endNod, p);
				if(Di > dijkstra.infinity-1) continue;
				if(minDis > Di){
					minDis = Di;
					sel_p = p;
				}
			}
			
			sel_Ip = dijkstra.solve(endNod, sel_p);
		}
		
		for(int j = 1; j < sel_Ip.size()-1; j++){
			int pp = sel_Ip.get(j);
			bestItinerary.addPoint(pp);
			bestItinerary.addRequestID(-1);
			bestItinerary.addAction(VehicleAction.PASS);
			/*
			LP.add(pp);
			requestID.add(-1);
			actions.add(VehicleAction.PASS);
			*/
		}
		//LP.add(sel_Ip.get(sel_Ip.size()-1));
		bestItinerary.addPoint(sel_Ip.get(sel_Ip.size()-1));
		if(stopWorking){
			//requestID.add(-1);
			//actions.add(VehicleAction.FINISH_WORK);
			//mStatus.put(actions.size()-1,VehicleStatus.STOP_WORK);
			bestItinerary.addRequestID(-1);
			bestItinerary.addAction(VehicleAction.FINISH_WORK);
			bestItinerary.mStatus.put(bestItinerary.size()-1,VehicleStatus.STOP_WORK);
		}else{
			//requestID.add(-1);
			//actions.add(VehicleAction.STOP);
			//mStatus.put(actions.size()-1,VehicleStatus.REST_AT_PARKING);
			bestItinerary.addRequestID(-1);
			bestItinerary.addAction(VehicleAction.STOP);
			bestItinerary.mStatus.put(bestItinerary.size()-1,VehicleStatus.REST_AT_PARKING);
		}
		
		if(!assignTimePoint(bestItinerary, vh, t+1, pr, parcels)){
			System.out.println("Simulator::computeBenefitsOptimalRoute, final assign time point failed -> BUG??????");
			log.println("Simulator::computeBenefitsOptimalRoute, final assign time point failed -> BUG??????");
			log.close();
			System.exit(-1);
		}
		return new SolutionRoute(bestItinerary,bestItinerary.mStatus,maxBenefits);
		//return new SolutionRoute(I,mStatus,maxBenefits);
	}
	public double getDiscount(double expectedDistance, double realDistance){
		return (realDistance/expectedDistance - 1)*gamma4;
	}
	public double getCostFuel(double d){
		return d * gamma3;
	}
	public double getParcelRevenue(double d){
		return d*gamma2 + beta;
	}
	public double getPeopleRevenue(double d){
		return d*gamma1 + alpha;
	}
	
	public void removeRequest(ArrayList<ParcelRequest> PR, ArrayList<ParcelRequest> S){
		if(S == null) return;
		for(int i = 0; i < S.size(); i++){
			ParcelRequest pr = S.get(i);
			int idx = PR.indexOf(pr);
			PR.remove(idx);
		}
	}
	
	public ItineraryTravelTime establishItinerary(ArrayList<Itinerary> itineraries, int nextPoint, PeopleRequest pr, ArrayList<ParcelRequest> parcels){
		ArrayList<Integer> requestID = new ArrayList<Integer>();
		ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
		HashMap<Integer, VehicleStatus> mStatus = new HashMap<Integer, VehicleStatus>();
		ArrayList<Integer> LP = new ArrayList<Integer>();
		if(nextPoint == pr.pickupLocationID){
			mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PEOPLE);
		}else{
			mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PARCEL);
		}
		int pp = itineraries.get(0).get(0);
		LP.add(pp);
		requestID.add(-1);
		actions.add(VehicleAction.PASS);
		
		for(int i = 0; i < itineraries.size(); i++){
			Itinerary I = itineraries.get(i);
			for(int j = 1; j < I.size()-1; j++){
				pp = I.get(j);
				LP.add(pp);
				requestID.add(-1);
				actions.add(VehicleAction.PASS);
			}
			
			pp = I.get(I.size()-1);
			LP.add(pp);
			if(pp == pr.pickupLocationID){
				requestID.add(pr.id);
				actions.add(VehicleAction.PICKUP_PEOPLE);
				mStatus.put(actions.size()-1, VehicleStatus.PICKUP_PEOPLE);
			}else if(pp == pr.deliveryLocationID){
				requestID.add(pr.id);
				actions.add(VehicleAction.DELIVERY_PEOPLE);
				mStatus.put(actions.size()-1, VehicleStatus.DELIVERY_PEOPLE);
			}else{
				for(int j = 0; j < parcels.size(); j++){
					ParcelRequest parcR = parcels.get(j);
					if(pp == parcR.pickupLocationID){
						requestID.add(parcR.id);
						actions.add(VehicleAction.PICKUP_PARCEL);
						mStatus.put(actions.size()-1, VehicleStatus.PICKUP_PARCEL);
					}else if(pp == parcR.deliveryLocationID){
						requestID.add(parcR.id);
						actions.add(VehicleAction.DELIVERY_PARCEL);
						mStatus.put(actions.size()-1, VehicleStatus.DELIVERY_PARCEL);
					}else{
						//System.out.println("Simulator::computeBenefitOptimizeRoute --> BUG???????, unknown point");
						//System.exit(-1);
					}
				}
			}
		}
		
		
		
		
		int[] path = new int[LP.size()];
		for(int i = 0; i < LP.size(); i++)
			path[i] = LP.get(i);
		
		if(actions.size() != path.length){
			System.out.println("Simulator::computeOptimizeBenefits, BUG actions.sz = " + actions.size() + " != path.length = " + path.length);
			System.exit(1);
		}
		
		ItineraryTravelTime I = new ItineraryTravelTime(path, requestID, actions);
		I.mStatus = mStatus;
		return I;
	}
	public boolean assignTimePoint(ItineraryTravelTime I, Vehicle vh, int t){
		ArrayList<PeopleRequest> peopleReq = new ArrayList<PeopleRequest>();
		ArrayList<ParcelRequest> parcels = new ArrayList<ParcelRequest>();
		return assignTimePoint(I,vh,0,t,peopleReq,parcels);
	}
	public boolean assignTimePoint(ItineraryTravelTime I, Vehicle vh, int t, PeopleRequest pr, ArrayList<ParcelRequest> parcels){
		ArrayList<PeopleRequest> peopleReq = new ArrayList<PeopleRequest>();
		peopleReq.add(pr);
		return assignTimePoint(I,vh,0,t,peopleReq,parcels);
	}
	/*
	public boolean assignTimePoint(ItineraryTravelTime I, Vehicle vh, int t, ArrayList<PeopleRequest> peopleReq, ArrayList<ParcelRequest> parcels){
		// return false if cannot assign feasibly time to points
		int td = t;//T.currentTimePoint;
		int taxiID = -1;
		//if(vh.ID == taxiID)log.println("Simulator::assignTimePoint taxi " + vh.ID + ", pr = " + pr.id + ", init td = " + td + 
				//", start point I = " + I.get(0) + ", end point I = " + I.get(I.size()-1));
		I.setDepartureTime(0, td);
		for(int i = 1; i < I.size(); i++){
			int v1 = I.get(i-1);
			int v2 = I.get(i);
			int travelTime = 0;
			if(v1 != v2){
				Arc a = map.getArc(v1, v2);
				if(a == null){
					System.out.println("Simulator::assignTimePoint taxi " + vh.ID  + " arc(" + v1 + "," + v2 + ") does not exist BUGBUGBUG");
					log.println("Simulator::assignTimePoint taxi " + vh.ID +  " arc(" + v1 + "," + v2 + ") does not exist BUGBUGBUG");
					log.close();
					System.exit(-1);
				}
				travelTime = getTravelTime(a, maxSpeedms);
				//if(vh.ID == taxiID)log.println("Simulator::assignTimePoint taxi " + vh.ID + ", pr = " + pr.id + 
						//", arc(" + v1 + "," + v2 + ") = " + a.w + ", td = " + td);
			}
			int ta = td + travelTime;
			
			int rid = I.getRequestID(i);
			if(rid > 0){
				//if(rid == pr.id){
				for(int j = 0; j < peopleReq.size(); j++){
					PeopleRequest pr = peopleReq.get(j);
					//if(I.getAction(i) == VehicleAction.PICKUP_PEOPLE){
					if(pr.pickupLocationID == v2){
						if(ta + pr.pickupDuration < pr.earlyPickupTime || ta + pr.pickupDuration > pr.latePickupTime){
							System.out.println("Simulator::assignTimePoint, rid = pr.id = " + pr.id + 
									", v2 = pr.pickupLocationID = " + v2 + ", ta = " + ta + 
									" + pr.pickupDuration < pr.earlyPickupTime = " + pr.earlyPickupTime + 
									" || > pr.latePickupTime " + pr.latePickupTime);
							log.close();
							System.exit(-1);
							return false;
						}
						I.setArrivalTime(i, ta);
						td = ta + pr.pickupDuration;
						I.setDepartureTime(i, td);
					//}else if(I.getAction(i) == VehicleAction.DELIVERY_PEOPLE){
					}else if(pr.deliveryLocationID == v2){
						if(ta < pr.earlyDeliveryTime || ta > pr.lateDeliveryTime){
							System.out.println("Simulator::assignTimePoint, rid = pr.id = " + pr.id + 
									", v2 = pr.deliveryLocationID = " + v2 + ", ta = " + ta + " < pr.earlyDeliveryTime = " + pr.earlyDeliveryTime + 
									" || > pr.lateDeliveryTime " + pr.lateDeliveryTime);
							log.close();
							System.exit(-1);
							return false;
						}
						td = ta + pr.deliveryDuration;
						I.setArrivalTime(i, ta);
						I.setDepartureTime(i, td);
					}else{
						//System.out.println("Simulator::assignTimePoint --> rid = " + rid + " = pr.id BUT current point is not pickup, delivery locations --> BUG????????");
						//System.exit(-1);
					}
				}//else{
				
				for(int j = 0; j < parcels.size(); j++){
						ParcelRequest prj = parcels.get(j);
						if(prj.pickupLocationID == v2){
							if((ta + prj.pickupDuration < prj.earlyPickupTime || ta + prj.pickupDuration > prj.latePickupTime) &&
									(ta < prj.earlyPickupTime || ta > prj.latePickupTime)){
								System.out.println("Simulator::assignTimePoint, prj.pickupLocationID = " + v2 + ", ta = " + 
								ta + " < pr.earlyPickupTime = " + prj.earlyPickupTime + 
										" || > pr.latePickupTime " + prj.latePickupTime);
								log.close();
								System.exit(-1);
								return false;
							}
							I.setArrivalTime(i, ta);
							td = ta + prj.pickupDuration;
							I.setDepartureTime(i, td);
						}else if(prj.deliveryLocationID == v2){
							if((ta + prj.deliveryDuration < prj.earlyDeliveryTime || ta + prj.deliveryDuration > prj.lateDeliveryTime) &&
									(ta < prj.earlyDeliveryTime || ta > prj.lateDeliveryTime)) {
								System.out.println("Simulator::assignTimePoint, prj.deliveryLocationID = " + v2 + ", ta = " + 
										ta + " < pr.earlyDeliveryTime = " + prj.earlyDeliveryTime + 
											" || > pr.lateDeliveryTime " + prj.lateDeliveryTime);
								log.close();
								System.exit(-1);
								return false;
							}
							I.setArrivalTime(i, ta);
							td = ta + prj.deliveryDuration;
							I.setDepartureTime(i, td);
						}else{
							//System.out.println("Simulator::assignTimePoint --> BUG????????");
							//System.exit(-1);
						}
						
					}
				//}
			}else{
				// PASS
				td = ta;
				I.setArrivalTime(i, ta);
				I.setDepartureTime(i, td);
			}
		}
		//if(vh.ID == taxiID)log.println("Simulator::assignTimePoint taxi " + vh.ID + ", pr = " + pr.id + " END");
		return true;
	}
	*/
	
	public boolean assignTimePoint(ItineraryTravelTime I, Vehicle vh, int fromIdx, int t, 
			ArrayList<PeopleRequest> peopleReq, ArrayList<ParcelRequest> parcels){
		//System.out.println("Simulator::assignTimePoint(taxi = " + vh.ID + ", fromIdx = " + fromIdx + ", fromPoint = " + I.get(fromIdx)  +
				//", I.sz  " + I.size() + ", t = " + t + ", AT + " + T.currentTimePoint);
		// return false if cannot assign feasibly time to points
		// taxi departs from point fromIdx at time point t
		int td = t;//T.currentTimePoint;
		int taxiID = -1;//205;
		//if(vh.ID == taxiID)log.println("Simulator::assignTimePoint taxi " + vh.ID + ", pr = " + pr.id + ", init td = " + td + 
				//", start point I = " + I.get(0) + ", end point I = " + I.get(I.size()-1));
		I.setDepartureTime(fromIdx, td);
		for(int i = fromIdx+1; i < I.size(); i++){
			int v1 = I.get(i-1);
			int v2 = I.get(i);
			int travelTime = 0;
			if(v1 != v2){
				Arc a = map.getArc(v1, v2);
				if(a == null){
					System.out.println("Simulator::assignTimePoint taxi " + vh.ID + " arc(" + v1 + "," + v2 + ") does not exist BUGBUGBUG");
					log.println("Simulator::assignTimePoint taxi " + vh.ID + " arc(" + v1 + "," + v2 + ") does not exist BUGBUGBUG");
					log.close();
					System.exit(-1);
				}
				travelTime = getTravelTime(a, maxSpeedms);
				//if(vh.ID == taxiID)log.println("Simulator::assignTimePoint taxi " + vh.ID + ", pr = " + pr.id + 
						//", arc(" + v1 + "," + v2 + ") = " + a.w + ", td = " + td);
			}
			int ta = td + travelTime;
			
			int rid = I.getRequestID(i);
			if(rid > 0){
				PeopleRequest peopleRequest = mPeopleRequest.get(rid);
				if(peopleRequest != null){
					if(peopleRequest.pickupLocationID == v2){
						if(ta + peopleRequest.pickupDuration < peopleRequest.earlyPickupTime || ta + peopleRequest.pickupDuration > peopleRequest.latePickupTime){
							System.out.println("Simulator::assignTimePoint, rid = pr.id = " + peopleRequest.id + 
									", v2 = pr.pickupLocationID = " + v2 + ", ta = " + ta + 
									" + pr.pickupDuration < pr.earlyPickupTime = " + peopleRequest.earlyPickupTime + 
									" || > pr.latePickupTime " + peopleRequest.latePickupTime);
							log.println("Simulator::assignTimePoint, rid = pr.id = " + peopleRequest.id + 
									", v2 = pr.pickupLocationID = " + v2 + ", ta = " + ta + 
									" + pr.pickupDuration < pr.earlyPickupTime = " + peopleRequest.earlyPickupTime + 
									" || > pr.latePickupTime " + peopleRequest.latePickupTime);
							return false;
						}
						I.setArrivalTime(i, ta);
						td = ta + peopleRequest.pickupDuration;
						I.setDepartureTime(i, td);
						
					//}else if(I.getAction(i) == VehicleAction.DELIVERY_PEOPLE){
					}else if(peopleRequest.deliveryLocationID == v2){
						if(ta < peopleRequest.earlyDeliveryTime || ta > peopleRequest.lateDeliveryTime){
							System.out.println("Simulator::assignTimePoint, rid = pr.id = " + peopleRequest.id + 
									", v2 = pr.deliveryLocationID = " + v2 + ", ta = " + ta + " < pr.earlyDeliveryTime = " + peopleRequest.earlyDeliveryTime + 
									" || > pr.lateDeliveryTime " + peopleRequest.lateDeliveryTime);
							return false;
						}
						td = ta + peopleRequest.deliveryDuration;
						I.setArrivalTime(i, ta);
						I.setDepartureTime(i, td);
						
					}else{
						System.out.println("Simulator::assignTimePoint --> rid = " + rid + " = pr.id BUT current point is not pickup, delivery locations --> BUG????????");
						System.exit(-1);
					}
			
				}
			
				ParcelRequest parcelRequest = mParcelRequest.get(rid);
				if(parcelRequest != null){
					if(parcelRequest.pickupLocationID == v2){
						if(ta + parcelRequest.pickupDuration < parcelRequest.earlyPickupTime || ta + parcelRequest.pickupDuration > parcelRequest.latePickupTime){
							System.out.println("Simulator::assignTimePoint, rid = pr.id = " + parcelRequest.id + 
									", v2 = pr.pickupLocationID = " + v2 + ", ta = " + ta + 
									" + pr.pickupDuration < pr.earlyPickupTime = " + parcelRequest.earlyPickupTime + 
									" || > pr.latePickupTime " + parcelRequest.latePickupTime);
							log.println("Simulator::assignTimePoint, rid = pr.id = " + parcelRequest.id + 
									", v2 = pr.pickupLocationID = " + v2 + ", ta = " + ta + 
									" + pr.pickupDuration < pr.earlyPickupTime = " + parcelRequest.earlyPickupTime + 
									" || > pr.latePickupTime " + parcelRequest.latePickupTime);
							return false;
						}
						I.setArrivalTime(i, ta);
						td = ta + parcelRequest.pickupDuration;
						I.setDepartureTime(i, td);
						
					//}else if(I.getAction(i) == VehicleAction.DELIVERY_PEOPLE){
					}else if(parcelRequest.deliveryLocationID == v2){
						if(ta < parcelRequest.earlyDeliveryTime || ta > parcelRequest.lateDeliveryTime){
							System.out.println("Simulator::assignTimePoint, rid = pr.id = " + parcelRequest.id + 
									", v2 = pr.deliveryLocationID = " + v2 + ", ta = " + ta + " < pr.earlyDeliveryTime = " + parcelRequest.earlyDeliveryTime + 
									" || > pr.lateDeliveryTime " + parcelRequest.lateDeliveryTime);
							return false;
						}
						td = ta + parcelRequest.deliveryDuration;
						I.setArrivalTime(i, ta);
						I.setDepartureTime(i, td);
						
					}else{
						System.out.println("Simulator::assignTimePoint --> rid = " + rid + " = pr.id BUT current point is not pickup, delivery locations --> BUG????????");
						System.exit(-1);
					}
			
				}
			
				/*
				boolean ok = false;
				for(int j = 0; j < peopleReq.size(); j++){
				
					PeopleRequest pr = peopleReq.get(j);
				
					if(rid == pr.id){
						//if(I.getAction(i) == VehicleAction.PICKUP_PEOPLE){
						if(pr.pickupLocationID == v2){
							if(ta + pr.pickupDuration < pr.earlyPickupTime || ta + pr.pickupDuration > pr.latePickupTime){
								System.out.println("Simulator::assignTimePoint, rid = pr.id = " + pr.id + 
										", v2 = pr.pickupLocationID = " + v2 + ", ta = " + ta + 
										" + pr.pickupDuration < pr.earlyPickupTime = " + pr.earlyPickupTime + 
										" || > pr.latePickupTime " + pr.latePickupTime);
								log.println("Simulator::assignTimePoint, rid = pr.id = " + pr.id + 
										", v2 = pr.pickupLocationID = " + v2 + ", ta = " + ta + 
										" + pr.pickupDuration < pr.earlyPickupTime = " + pr.earlyPickupTime + 
										" || > pr.latePickupTime " + pr.latePickupTime);
								return false;
							}
							I.setArrivalTime(i, ta);
							td = ta + pr.pickupDuration;
							I.setDepartureTime(i, td);
							ok = true;
						//}else if(I.getAction(i) == VehicleAction.DELIVERY_PEOPLE){
						}else if(pr.deliveryLocationID == v2){
							if(ta < pr.earlyDeliveryTime || ta > pr.lateDeliveryTime){
								System.out.println("Simulator::assignTimePoint, rid = pr.id = " + pr.id + 
										", v2 = pr.deliveryLocationID = " + v2 + ", ta = " + ta + " < pr.earlyDeliveryTime = " + pr.earlyDeliveryTime + 
										" || > pr.lateDeliveryTime " + pr.lateDeliveryTime);
								return false;
							}
							td = ta + pr.deliveryDuration;
							I.setArrivalTime(i, ta);
							I.setDepartureTime(i, td);
							ok = true;
						}else{
							System.out.println("Simulator::assignTimePoint --> rid = " + rid + " = pr.id BUT current point is not pickup, delivery locations --> BUG????????");
							System.exit(-1);
						}
					}
				}
				//}else{
					for(int j = 0; j < parcels.size(); j++){
						ParcelRequest prj = parcels.get(j);
						if(prj.pickupLocationID == v2){
							if((ta + prj.pickupDuration < prj.earlyPickupTime || ta + prj.pickupDuration > prj.latePickupTime) &&
									(ta < prj.earlyPickupTime || ta > prj.latePickupTime)){
								System.out.println("Simulator::assignTimePoint, prj.pickupLocationID = " + v2 + ", ta = " + 
									
									ta + " < pr.earlyPickupTime = " + prj.earlyPickupTime + 
										" || > pr.latePickupTime " + prj.latePickupTime);
								log.println("Simulator::assignTimePoint, prj.pickupLocationID = " + v2 + ", ta = " + 
										
									ta + " < pr.earlyPickupTime = " + prj.earlyPickupTime + 
										" || > pr.latePickupTime " + prj.latePickupTime);
								return false;
							}
							I.setArrivalTime(i, ta);
							td = ta + prj.pickupDuration;
							I.setDepartureTime(i, td);
							ok = true;
						}else if(prj.deliveryLocationID == v2){
							if((ta + prj.deliveryDuration < prj.earlyDeliveryTime || ta + prj.deliveryDuration > prj.lateDeliveryTime) &&
									(ta < prj.earlyDeliveryTime || ta > prj.lateDeliveryTime)) {
								System.out.println("Simulator::assignTimePoint, prj.deliveryLocationID = " + v2 + ", ta = " + 
										ta + " < pr.earlyDeliveryTime = " + prj.earlyDeliveryTime + 
											" || > pr.lateDeliveryTime " + prj.lateDeliveryTime);
								log.println("Simulator::assignTimePoint, prj.deliveryLocationID = " + v2 + ", ta = " + 
										ta + " < pr.earlyDeliveryTime = " + prj.earlyDeliveryTime + 
											" || > pr.lateDeliveryTime " + prj.lateDeliveryTime);
								return false;
							}
							I.setArrivalTime(i, ta);
							td = ta + prj.deliveryDuration;
							I.setDepartureTime(i, td);
							ok = true;
						}else{
							//System.out.println("Simulator::assignTimePoint --> BUG????????");
							//System.exit(-1);
						}
						
					}
				if(!ok){
					System.out.println("Simulator::assignTimePoint, fromIdx = " + fromIdx + ", i = " + i + ", rid = " + rid + " > 0, BUT no time point is assigned");
					log.println("Simulator::assignTimePoint, fromIdx = " + fromIdx + ", i = " + i + ", rid = " + rid + " > 0, BUT no time point is assigned");
				}
				//}
				 
				 */
			}else{
				if(I.getAction(i) == VehicleAction.PASS){
					// PASS
					td = ta;
					I.setArrivalTime(i, ta);
					I.setDepartureTime(i, td);
				}else if(I.getAction(i) == VehicleAction.STOP || I.getAction(i) == VehicleAction.FINISH_WORK){
					I.setArrivalTime(i, ta);
				}else{
					System.out.println("Simulator::assignTimePoint, BUG??? unknown action " + I.getAction(i));
					System.exit(-1);
				}
			}
		}
		//if(vh.ID == taxiID)log.println("Simulator::assignTimePoint taxi " + vh.ID + ", pr = " + pr.id + " END");
		return true;
	}

	public int getTravelTimeSegments(int fromLoc, int toLoc){
		Itinerary I = dijkstra.queryShortestPath(fromLoc, toLoc);
		if(I == null) return (int)dijkstra.infinity;
		int t = 0;
		for(int i = 0; i < I.size()-1; i++){
			int u = I.get(i);
			int v = I.get(i+1);
			Arc a = map.getArc(u, v);
			t  = t + getTravelTime(a, maxSpeedms);
		}
		return t;
	}
	public boolean setupRouteWithParcelsInserted(Vehicle vh, int t, PeopleRequest pr, int maxSz){
		int taxiID = 11;
		ArrayList<ParcelRequest> sel_parcel_requests = new ArrayList<ParcelRequest>();
		for(int i = 0; i < pendingParcelRequests.size(); i++){
			ParcelRequest parcelReq = pendingParcelRequests.get(i);
			sel_parcel_requests.add(parcelReq);
		}
		
		double maxBenefits = 0;
		SolutionRoute sel_solution_route = null;
		
		ArrayList<ParcelRequest> PR = new ArrayList<ParcelRequest>();
		SolutionRoute SR = computeBenefitOptimizeRoute(vh, t, pr, PR);
		if(SR != null){
			if(SR.benefits > maxBenefits){
		
				maxBenefits = SR.benefits;
				sel_solution_route = SR;
			}
			System.out.println("Simulator::setupRouteWithParcelsInserted, init without parcel benefits = " + SR.benefits);
		}
		
		ArrayList<ParcelRequest> removed_parcel_request = null;
		
		for(int k = 1; k <= maxSz; k++){
			if(k > sel_parcel_requests.size()) continue;
			
			CombinationGenerator C = new CombinationGenerator(k,sel_parcel_requests.size());
			C.generate();
			System.out.println("Simulator::setupRouteWithParcelsInserted, sel_parcel_requests = " + sel_parcel_requests.size() + 
					", k = " + k + ", maxSz = " + maxSz + ", C.size = " + C.size());
			
					
			for(int idx = 0; idx < C.size(); idx++){
				int[] c = C.get(idx);
				//System.out.print("Simulator::setupRouteWithParcelsInserted, combination = ");
				//for(int i = 0; i < c.length; i++) System.out.print(c[i] + ","); System.out.println();
				
				//PR = new ArrayList<ParcelRequest>();
				PR.clear();
				for(int i = 0; i < c.length; i++){
					PR.add(sel_parcel_requests.get(c[i]));
				}
				//System.out.print("Simulator::setupRouteWithParcelsInserted, PR = ");
				//for(int i = 0; i < PR.size(); i++) 
					//System.out.print(PR.get(i).pickupLocationID + "->" + PR.get(i).deliveryLocationID + "\t"); System.out.println();
					
				SR = computeBenefitOptimizeRoute(vh, t, pr, PR);
				if(SR != null)if(SR.benefits > maxBenefits){
					maxBenefits = SR.benefits;
					sel_solution_route = SR;
					removed_parcel_request = PR;
				}
			}
		}
		
		if(sel_solution_route == null) return false;
		System.out.println("Simulator::setupRouteParcelInserted, benefits = " + sel_solution_route.benefits);
		
		if(vh.ID == taxiID){
			log.println("Simulator::setupRouteWithParcelsInserted, taxi " + vh.ID + ", peopleReq " + pr.id);
			for(int jj = 0; jj < sel_solution_route.itinerary.size(); jj++){
				int v = sel_solution_route.itinerary.get(jj);
				String act = Vehicle.getActionDescription(sel_solution_route.itinerary.getAction(jj));
				int rid = sel_solution_route.itinerary.getRequestID(jj);
				String msg = "Simulator::setupRouteWithParcelsInserted, taxi " + vh.ID + ", peopleReq " + pr.id + ", point " + v + 
						", Act " + act + ", rid " + rid;
				if(rid > 0){
				if(v == pr.pickupLocationID) msg += ", pickup people windows = " + pr.earlyPickupTime + " -- " + pr.latePickupTime;
				else if(v == pr.deliveryLocationID) msg += ", delivery people + windows = " + pr.earlyDeliveryTime + " -- " + pr.lateDeliveryTime;
				else{
					for(int jjj = 0; jjj < allParcelRequests.size(); jjj++){
						ParcelRequest prj = allParcelRequests.get(jjj);
						if(v == prj.pickupLocationID) msg += ", pickup parcel " + prj.id + ", windows = " + prj.earlyPickupTime + " -- " + prj.latePickupTime;
						else if(v == prj.deliveryLocationID) msg += ", delivery parcel " + prj.id + ", windows = " + prj.earlyDeliveryTime + " -- " + prj.lateDeliveryTime;
					}
				}
				}
				log.println(msg);
			}
			
		}
		
		removeRequest(pendingParcelRequests, removed_parcel_request);
		
		int endNod = sel_solution_route.itinerary.get(sel_solution_route.itinerary.size()-1);
		Parking des_parking = findParking(endNod);
		if(des_parking != null){
			des_parking.load++;
			des_parking.lastUpdateTimePoint = T.currentTimePoint;//des_parking.lastUpdateTimePoint < T.currentTimePoint ? des_parking.lastUpdateTimePoint : T.currentTimePoint;
			System.out.println("Simulator::setupRouteWithParcelFollow, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + 
			", parking " + des_parking.locationID + ", load increases = " + des_parking.load + ", capacity = " + des_parking.capacity);
			log.println("Simulator::setupRouteWithParcelFollow, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + 
					", parking " + des_parking.locationID + ", load increases = " + des_parking.load + ", capacity = " + des_parking.capacity);
			
		}
		
		if(vh.ID == taxiID){
			log.println("Simulator::setupRouteWithParcelInserted, people request = " + pr.id + ", T.current = " + T.currentTimePoint + 
				", taxi = " + vh.ID + ", status = " + vh.getStatusDescription(vh.status) + ", nextItinerary = ");
			sel_solution_route.itinerary.writeToFile(log);
		}
		vh.nextItinerary = sel_solution_route.itinerary;
		vh.mNextStatus = sel_solution_route.mStatus;
		
		if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
			vh.status = VehicleStatus.PREPARE_NEW_ITINERARY;
			
			int locID = vh.currentItinerary.get(vh.currentItinerary.size()-1);
			log.println("Simulator::setupRouteWithParcelFollow, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + 
			" currentStatus = TRAVEL_WITHOUT_LOAD, taxi = " + vh.ID + ", endPoint of current itinerary = " + locID);
			Parking p = findParking(locID);
			if(p != null){
				p.load--;
				p.lastUpdateTimePoint = T.currentTimePoint;//p.lastUpdateTimePoint < T.currentTimePoint ? p.lastUpdateTimePoint : T.currentTimePoint;
				System.out.println("Simulator::setupRouteWithParcelFollow At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"parking " + p.locationID + " load decrease = " + p.load + ", capacity = " + p.capacity);
				log.println("Simulator::setupRouteWithParcelFollow At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"taxi " + vh.ID + ", parking " + p.locationID + " load decrease = " + p.load + ", capacity = " + p.capacity);
			}
		}else if(vh.status == VehicleStatus.REST_AT_PARKING){
			vh.status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
			
			Parking p = findParking(vh.lastPoint);
			if(p != null){
				p.load--;
				p.lastUpdateTimePoint = T.currentTimePoint;
				System.out.println("Simulator::setupRouteWithParcelFollow At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"taxi = " + vh.ID + " REST_AT_PARKING, parking " + p.locationID + " load decrease = " + p.load + ", capacity = " + p.capacity);
				log.println("Simulator::setupRouteWithParcelFollow At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"parking " + p.locationID + " load decrease = " + p.load + ", capacity = " + p.capacity);
			}
			vh.setNewItinerary();
			//log.println("Simulator::setupRoute(" + vh.ID + ") --> Next Itinerary is: " + vh.nextItinerary2String());
		}
		
	
		return true;
	}
	public boolean setupRouteWithAParcelFollow(Vehicle vh, int t, PeopleRequest pr){
		// compute a route for taxi at current time t
		// update TaxiLocation data structure
		//System.out.println("MapBasedDataGenerator::setUpRoute not implemented");
		//System.exit(1);
		System.out.println("Simulator::setupRouteWithAParcelFollow, pendingParcelRequests.sz = " + pendingParcelRequests.size());
		int src = pr.pickupLocationID;
		int des = pr.deliveryLocationID;
		//System.out.println("setupRoute(" + vh.toString() + "," + t + "," + src + "," + des + ")");
		int curPos = vh.lastPoint;
		if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
			curPos = vh.currentItinerary.get(vh.lastIndexPoint+1);
		}
		Itinerary I1 = dijkstra.solve(curPos, src);
		double cost1 = I1.getDistance();
		//if(cost1 >= dijkstra.infinity){
		if(I1 == null){
			System.out.println("Simulator::setupRouteWithParcelFollow(" + vh.toString() + "," + t + "," + src + "," + des + " --> cannot find path from curPos = " + curPos + " to src = " + src);
			log.println("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ") path(" + curPos + " -> " + src + ") --> failed");
			return false;
		}
		int[] path1 = dijkstra.getPath(curPos, src);
		//System.out.print("path1 = "); printPath(path1);
		Itinerary I2 = dijkstra.solve(src, des);
		double cost2 = I2.getDistance();
		//if(cost2 >= dijkstra.infinity){
		if(I2 == null){
			System.out.println("Simulator::setupRouteWithParcelFollow(" + vh.toString() + "," + t + "," + src + "," + des + " --> cannot find path from src = " + src + " to des = " + des);
			log.println("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ") path(" + src + " -> " + des + ") --> failed");
			return false;
		}
		int[] path2 = dijkstra.getPath(src, des);
		//System.out.print("path2 = "); printPath(path2);
		//double cost3 = dijkstra.infinity;
		
		int t12 = getTravelTime(cost1 + cost2, maxSpeedms);
		boolean stopWorking = false;
		if(t + t12 > T.endrequest) stopWorking = true;
		
		int[] path3 = null;
		double maxExtraRevenue = -9999999;
		
		int idx_pickup_parcel = -1;
		int idx_delivery_parcel = -1;
		int sel_parcel_request_index = -1;
		ParcelRequest sel_parcel_request = null;
		
		if(stopWorking){
			Itinerary I3 = dijkstra.solve(des, mTaxi2Depot.get(vh.ID));
			if(I3 == null){
				System.out.println("Simulator::setupRouteWithParcelFollow(" + vh.toString() + "," + t + "," +  des + "," + mTaxi2Depot.get(vh.ID) + " --> cannot find path from src = " + src + " to des = " + des);
				log.println("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ") path(" + src + " -> " + des + ") --> failed");
				return false;
			}
			path3 = dijkstra.getPath(des, mTaxi2Depot.get(vh.ID));
			System.out.println("Simulator::setupRouteWithParcelFollow(" + vh.toString() + ") stopWorking, path3 back to depot = " + path3.length);
		}else{
			int count = 0;
			ArrayList<Parking> L = new ArrayList<Parking>();
			for(int i = 0; i < lstParkings.size(); i++){
				Parking p = lstParkings.get(i);
				if(p.load < p.capacity) L.add(p);
			}
			int[] P = new int[L.size()+1];
			for(int i = 0; i < L.size(); i++){
				P[i] = L.get(i).locationID;
			}
			log.println("Simulator::setupRouteWithParcelFollows, available parkings = ");
			for(int i = 0; i < L.size(); i++){
				log.println(L.get(i).locationID + ", cap = " + L.get(i).capacity + ", " +
						"load = " + L.get(i).load + ", lastUpdateTime = " + L.get(i).lastUpdateTimePoint);
			}
			
			P[L.size()] = mTaxi2Depot.get(vh.ID);
			
			// find nearest parkings and a parcel request
			int sel_parking_loc = -1;
			//for(int i = 0; i < lstParkings.size(); i++){
			for(int i = 0; i < P.length; i++){
				//int p = lstParkings.get(i).locationID;
				int p = P[i];
				for(int j = 0; j < pendingParcelRequests.size(); j++){
					ParcelRequest parcelrequest = pendingParcelRequests.get(j);
					//Itinerary Ii = dijkstra.solve(des, p); 
					Itinerary IJ1 = dijkstra.solve(des, parcelrequest.pickupLocationID);
					//if(IJ1.getDistance() >= dijkstra.infinity-1) continue;
					if(IJ1 == null) continue;
					Itinerary IJ2 = dijkstra.solve(parcelrequest.pickupLocationID, parcelrequest.deliveryLocationID);
					//if(IJ2.getDistance() >= dijkstra.infinity-1) continue;
					if(IJ2 == null) continue;
					Itinerary IJ3 = dijkstra.solve(parcelrequest.deliveryLocationID, p);
					//if(IJ3.getDistance() >= dijkstra.infinity-1) continue;
					if(IJ3 == null) continue;
					double Li = IJ1.getDistance() + IJ2.getDistance() + IJ3.getDistance();//Ii.getDistance();
					double costFuel = Li*gamma3;
					double extraRevenue = IJ2.getDistance()*gamma2 + beta;
					if(extraRevenue <= costFuel) continue;
					if(maxExtraRevenue < extraRevenue - costFuel){
						maxExtraRevenue = extraRevenue - costFuel;
						idx_pickup_parcel = IJ1.size()-1;
						idx_delivery_parcel = IJ1.size() + IJ2.size()-2;
						path3 = new int[IJ1.size() + IJ2.size() + IJ3.size() - 2];
						int id = -1;
						for(int jj = 0; jj < IJ1.size()-1; jj++){
							id++;
							path3[id] = IJ1.get(jj);
							//System.out.println("Simulator::setupRouteWithAParcelFollow --> Update from IJ1 path3[" + id + "] = " + path3[id]);
						}
						for(int jj = 0; jj < IJ2.size()-1;jj++){
							id++;
							path3[id] = IJ2.get(jj);
							//System.out.println("Simulator::setupRouteWithAParcelFollow --> Update from IJ2 path3[" + id + "] = " + path3[id]);
						}
						for(int jj = 0; jj < IJ3.size(); jj++){
							id++;
							path3[id] = IJ3.get(jj);
							//System.out.println("Simulator::setupRouteWithAParcelFollow --> Update from IJ3 path3[" + id + "] = " + path3[id]);
						}
						sel_parking_loc = p;
					}
					//System.out.println("end of update path3");
					sel_parcel_request_index = j;
					sel_parcel_request = parcelrequest;
				}
				if(maxExtraRevenue < 0){
					Itinerary IJ = dijkstra.solve(des, p);
					if(IJ == null) continue;
					//if(IJ.getDistance() > dijkstra.infinity-1) continue;
					double re = -IJ.getDistance()*gamma3;// cost of fuel
					if(maxExtraRevenue < re){
						maxExtraRevenue = re;
						path3 = dijkstra.getPath(des, p);
						sel_parking_loc = p;
					}
				}
			}
			if(sel_parcel_request_index > -1){
				pendingParcelRequests.remove(sel_parcel_request_index);
				acceptedParcelRequests++;
				System.out.println("Simulator::setupRouteWithAParcelFollow, Engage a parcel request id = " + sel_parcel_request.id + 
						" pendingParcelRequests.sz = " + pendingParcelRequests.size());
			}
			//if(cost3 >= dijkstra.infinity){
			if(path3 == null){
				System.out.println("setupRouteWithAParcelFollow(" + vh.toString() + "," + t + "," + src + "," + des + " --> cannot find path from des = " + des + " to a parking");
				log.println("At " + T.currentTimePointHMS() + ", setupRouteWithAParcelFollow(" + vh.ID + ") path(" + des + " -> parkings) --> failed");
				return false;
			}
			
			log.println("Simulator::setupRouteWithParcelFollow, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + 
			" end point of Route = " + sel_parking_loc);
			Parking des_parking = findParking(sel_parking_loc);
			if(des_parking != null){
				des_parking.load++;
				des_parking.lastUpdateTimePoint = T.currentTimePoint;//des_parking.lastUpdateTimePoint < T.currentTimePoint ? des_parking.lastUpdateTimePoint : T.currentTimePoint;
				System.out.println("Simulator::setupRouteWithParcelFollow, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + 
				", parking " + des_parking.locationID + ", load increases = " + des_parking.load + ", capacity = " + des_parking.capacity);
				log.println("Simulator::setupRouteWithParcelFollow, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + 
						", parking " + des_parking.locationID + ", load increases = " + des_parking.load + ", capacity = " + des_parking.capacity);
				
			}
		}
		
		
		//System.out.print("path3 = "); printPath(path3);
		//ArrayList<Integer> arrTime = new ArrayList<Integer>();
		//ArrayList<Integer> depTime = new ArrayList<Integer>();
		ArrayList<Integer> requestID = new ArrayList<Integer>();
		ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
		
		HashMap<Integer, VehicleStatus> mStatus = new HashMap<Integer, VehicleStatus>();
		mStatus.put(0, VehicleStatus.GOING_TO_PICKUP_PEOPLE);
		//log.println("Simulator::setupRoute, mStatus.put(" + 0 + "," + vh.getStatusDescription(VehicleStatus.GOING_TO_PICKUP_PEOPLE) + ")");
		int[] path = new int[path1.length + path2.length + path3.length - 2];
		int idx = -1;
		//arrTime.add(T.currentTimePoint);
		for(int i = 0; i < path1.length-1; i++){
			idx++;
			path[idx] = path1[i];
			Arc a = map.getArc(path1[i], path1[i+1]);
			int tt = getTravelTime(a, maxSpeedms);
			//depTime.add(arrTime.get(arrTime.size()-1));
			//arrTime.add(depTime.get(depTime.size()-1) + tt);
			requestID.add(-1);// no request served at this point, vehicle just passes
			actions.add(VehicleAction.PASS);
		}
		actions.add(VehicleAction.PICKUP_PEOPLE);
		requestID.add(pr.id);
		
		//vh.mStatus.put(idx+1,VehicleStatus.GOING_TO_DELIVERY_PEOPEL);
		mStatus.put(idx+1,VehicleStatus.PICKUP_PEOPLE);
		int d = idx+1;
		//log.println("Simulator::setupRoute, mStatus.put(" + d + "," + vh.getStatusDescription(VehicleStatus.PICKUP_PEOPLE) + ")");
		//mService.put(idx+1, pr.id);
		for(int i = 0; i < path2.length-1; i++){
			idx++;
			path[idx] = path2[i];
			Arc a = map.getArc(path2[i], path2[i+1]);
			int tt = getTravelTime(a, maxSpeedms);
			if(i == 0){
				//depTime.add(arrTime.get(arrTime.size()-1) + 60);// wait time at the point for pickup is 60 seconds
			}else{
				//depTime.add(arrTime.get(arrTime.size()-1));// wait time at the point for pickup is 60 seconds
			}	
			//arrTime.add(depTime.get(depTime.size()-1) + tt);
			
			if(i > 0){
				actions.add(VehicleAction.PASS);
				requestID.add(-1);// just passes, no vehicle served
			}
		}
		actions.add(VehicleAction.DELIVERY_PEOPLE);
		requestID.add(pr.id);
		//vh.mStatus.put(idx+1, VehicleStatus.TRAVEL_WITHOUT_LOAD);
		mStatus.put(idx+1, VehicleStatus.DELIVERY_PEOPLE);
		d = idx+1;
		//log.println("Simulator::setupRoute, mStatus.put(" + d + "," + vh.getStatusDescription(VehicleStatus.DELIVERY_PEOPLE) + ")");
		//vh.mService.put(idx+1, pr.id);
		
		
		if(stopWorking){
			for(int i = 0; i < path3.length; i++){
				idx++;
				path[idx] = path3[i];
				if(i > 0 && i < path3.length-1){
					actions.add(VehicleAction.PASS);
					requestID.add(-1);
				}else if(i == path3.length-1){
					actions.add(VehicleAction.FINISH_WORK);
					requestID.add(-1);
				}
			}
			mStatus.put(idx, VehicleStatus.STOP_WORK);
			//if(vh.ID == 4){
				//System.out.println("Vehicle::setupRouteWithParcelFollow, StopWorking --> mStatus.put(" + idx + "," + 
			//"STOP_WORK), action " + Vehicle.getActionDescription(actions.get(actions.size()-1)) + " at  position " + actions.size() + " minus 1");
			//}
		}else{
			for(int i = 0; i < path3.length; i++){
				idx++;
				path[idx] = path3[i];
				if(i == idx_pickup_parcel){
					actions.add(VehicleAction.PICKUP_PARCEL);
					requestID.add(sel_parcel_request.id);
					mStatus.put(idx, VehicleStatus.PICKUP_PARCEL);
				}else if(i == idx_delivery_parcel){
					actions.add(VehicleAction.DELIVERY_PARCEL);
					requestID.add(sel_parcel_request.id);
					mStatus.put(idx, VehicleStatus.DELIVERY_PARCEL);
				}else if(i+1 < path3.length && i > 0){
					actions.add(VehicleAction.PASS);
					requestID.add(-1);
				}else{
					if(i > 0){
						actions.add(VehicleAction.STOP);
						requestID.add(-1);
					}
				}
			}
			mStatus.put(idx, VehicleStatus.REST_AT_PARKING);
			d = idx+1;
		}
		
		//log.println("Simulator::setupRoute, mStatus.put(" + d + "," + vh.getStatusDescription(VehicleStatus.REST_AT_PARKING) + ")");
		
		//vh.currentItinerary = new ItineraryTravelTime(path,requestID, actions);//new Itinerary(path);
		vh.nextItinerary = new ItineraryTravelTime(path,requestID, actions);//new Itinerary(path);
		//if(vh.ID == 4){
			//System.out.println("Simulator::setupRouteWithParcelFollow, LAST ACTION (" +
					//"index = " + vh.nextItinerary.size() + " minus 1) = " + Vehicle.getActionDescription(vh.nextItinerary.getAction(vh.nextItinerary.size()-1)));
		//}
		vh.mNextStatus = mStatus;
		if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
			vh.status = VehicleStatus.PREPARE_NEW_ITINERARY;
			
			int locID = vh.currentItinerary.get(vh.currentItinerary.size()-1);
			log.println("Simulator::setupRouteWithParcelFollow, At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + 
			" currentStatus = TRAVEL_WITHOUT_LOAD, taxi = " + vh.ID + ", endPoint of current itinerary = " + locID);
			Parking p = findParking(locID);
			if(p != null){
				p.load--;
				p.lastUpdateTimePoint = T.currentTimePoint;//p.lastUpdateTimePoint < T.currentTimePoint ? p.lastUpdateTimePoint : T.currentTimePoint;
				System.out.println("Simulator::setupRouteWithParcelFollow At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"parking " + p.locationID + " load decrease = " + p.load + ", capacity = " + p.capacity);
				log.println("Simulator::setupRouteWithParcelFollow At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"taxi " + vh.ID + ", parking " + p.locationID + " load decrease = " + p.load + ", capacity = " + p.capacity);
			}
		}else if(vh.status == VehicleStatus.REST_AT_PARKING){
			vh.status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
			
			Parking p = findParking(vh.lastPoint);
			if(p != null){
				p.load--;
				p.lastUpdateTimePoint = T.currentTimePoint;
				System.out.println("Simulator::setupRouteWithParcelFollow At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"taxi = " + vh.ID + " REST_AT_PARKING, parking " + p.locationID + " load decrease = " + p.load + ", capacity = " + p.capacity);
				log.println("Simulator::setupRouteWithParcelFollow At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "], " +
						"parking " + p.locationID + " load decrease = " + p.load + ", capacity = " + p.capacity);
			}
			vh.setNewItinerary();
			//log.println("Simulator::setupRoute(" + vh.ID + ") --> Next Itinerary is: " + vh.nextItinerary2String());
		}
		/*
		vh.addItinerary(vh.currentItinerary);
		Arc a = map.getArc(path[0], path[1]);
		vh.lastPoint = path[0];
		vh.lastIndexPoint  = 0;
		vh.remainTimeToNextPoint = getTravelTime(a, maxSpeedms); 
		vh.totalTravelDistance += a.w;
		//vh.status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
		vh.status = VehicleStatus.PREPARE_NEW_ITINERARY;
		vh.startTimePointNewItinerary = T.currentTimePoint + vh.remainTimeToNextPoint; 
		*/
		if(actions.size() != path.length){
			System.out.println("BUG actions.sz = " + actions.size() + " != path.length = " + path.length);
			System.exit(1);
		}
		/*
		System.out.print("setupRoute(" + vh.toString() + "," + t + "," + src + "," + des + " paths = ");
		for(int i = 0;i < path.length; i++){
			System.out.print(path[i] + ",");
		}
		System.out.println();
		*/
		//log.print("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ", path(" + path.length + ") = ");
		//for(int i = 0; i < path.length; i++) log.print(path[i] + ","); log.println();
		
		return true;
	}
	public String name(){ return "Simulator";}
	public boolean setupRouteBack2Depot(Vehicle vh){
		
		int curPos = vh.lastPoint;
		int depot = mTaxi2Depot.get(vh.ID);
		System.out.println(name() + "::setupRouteBack2Depot(" + vh.ID +"), curPos = " + curPos + ", depot = "  + depot);
		if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
			curPos = vh.currentItinerary.get(vh.lastIndexPoint+1);
		}
		Itinerary I1 = dijkstra.solve(curPos, depot);
		if(I1 == null){
			System.out.println(name() + "::setupRouteBack2Depot(" + vh.toString() + ") no path is found");
			log.println("At " + T.currentTimePointHMS() + ", setupRouteBack2Depot(" + vh.ID + ") path(" + curPos + " -> " + depot + ") --> failed");
			return false;
		}
		int[] path1 = dijkstra.getPath(curPos, depot);
		ArrayList<Integer> requestID = new ArrayList<Integer>();
		ArrayList<VehicleAction> actions = new ArrayList<VehicleAction>();
		
		HashMap<Integer, VehicleStatus> mStatus = new HashMap<Integer, VehicleStatus>();
		//mStatus.put(0, VehicleStatus.TRAVEL_WITHOUT_LOAD);
		mStatus.put(0, VehicleStatus.GO_BACK_DEPOT_FINISH_WORK);
		//log.println("Simulator::setupRoute, mStatus.put(" + 0 + "," + vh.getStatusDescription(VehicleStatus.GOING_TO_PICKUP_PEOPLE) + ")");
		System.out.println("Simulator::setupRouteBack2Depot(" + vh.toString() + ") path.length = " + path1.length);
		int[] path = new int[path1.length];
		int idx = -1;
		//arrTime.add(T.currentTimePoint);
		for(int i = 0; i < path1.length-1; i++){
			idx++;
			path[idx] = path1[i];
			//Arc a = map.getArc(path1[i], path1[i+1]);
			//int tt = getTravelTime(a, maxSpeedms);
			//depTime.add(arrTime.get(arrTime.size()-1));
			//arrTime.add(depTime.get(depTime.size()-1) + tt);
			requestID.add(-1);// no request served at this point, vehicle just passes
			actions.add(VehicleAction.PASS);
		}
		idx++;
		path[idx] = path1[path1.length-1];
		actions.add(VehicleAction.FINISH_WORK);
		requestID.add(-1);
		mStatus.put(idx, VehicleStatus.STOP_WORK);
		
		//log.println("Simulator::setupRoute, mStatus.put(" + d + "," + vh.getStatusDescription(VehicleStatus.REST_AT_PARKING) + ")");
		
		//vh.currentItinerary = new ItineraryTravelTime(path,requestID, actions);//new Itinerary(path);
		vh.nextItinerary = new ItineraryTravelTime(path,requestID, actions);//new Itinerary(path);
		assignTimePoint(vh.nextItinerary,vh,T.currentTimePoint); 
		vh.mNextStatus = mStatus;
		if(vh.ID == 4){
			System.out.println("Simulator::setupRouteBack2Depot, LAST ACTION (" +
					"index = " + vh.nextItinerary.size() + " minus 1) = " + Vehicle.getActionDescription(vh.nextItinerary.getAction(vh.nextItinerary.size()-1)));
		}
		if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
			vh.status = VehicleStatus.PREPARE_NEW_ITINERARY;
		}else if(vh.status == VehicleStatus.REST_AT_PARKING){
			vh.status = VehicleStatus.GO_BACK_DEPOT_FINISH_WORK;
			vh.setNewItinerary();
			//log.println("Simulator::setupRoute(" + vh.ID + ") --> Next Itinerary is: " + vh.nextItinerary2String());
		}else{
			System.out.println("Simulator::setupRouteBack2Depot, unknown status = " + Vehicle.getStatusDescription(vh.status));
		}
		/*
		vh.addItinerary(vh.currentItinerary);
		Arc a = map.getArc(path[0], path[1]);
		vh.lastPoint = path[0];
		vh.lastIndexPoint  = 0;
		vh.remainTimeToNextPoint = getTravelTime(a, maxSpeedms); 
		vh.totalTravelDistance += a.w;
		//vh.status = VehicleStatus.GOING_TO_PICKUP_PEOPLE;
		vh.status = VehicleStatus.PREPARE_NEW_ITINERARY;
		vh.startTimePointNewItinerary = T.currentTimePoint + vh.remainTimeToNextPoint; 
		*/
		if(actions.size() != path.length){
			System.out.println("BUG actions.sz = " + actions.size() + " != path.length = " + path.length);
			System.exit(1);
		}
		/*
		System.out.print("setupRoute(" + vh.toString() + "," + t + "," + src + "," + des + " paths = ");
		for(int i = 0;i < path.length; i++){
			System.out.print(path[i] + ",");
		}
		System.out.println();
		*/
		//log.print("At " + T.currentTimePointHMS() + ", setupRoute(" + vh.ID + ", path(" + path.length + ") = ");
		//for(int i = 0; i < path.length; i++) log.print(path[i] + ","); log.println();
		
		return true;
	}

	public void setStatus(int k, int t, byte stat){
		status[k][t-startWorkingTime] = stat;
	}
	public int getStatus(int k, int t){
		return status[k][t - startWorkingTime];
	}
	public void loadDepotParkings(String fn){
		try{
			Scanner in = new Scanner(new File(fn));
			String s = in.next();
			lstDepots = new ArrayList<Integer>();
			lstParkings = new ArrayList<Parking>();
			while(true){
				int dp = in.nextInt();
				if(dp == -1) break;
				lstDepots.add(dp);
			}
			s = in.next();
			while(true){
				int locationID = in.nextInt();
				if(locationID == -1) break;
				int cap = in.nextInt();
				lstParkings.add(new Parking(locationID,cap));
			}
			
			/*
			// consider a depot as a parking with unlimited capacity
			for(int i = 0; i < lstDepots.size(); i++){
				int dp = lstDepots.get(i);
				lstParkings.add(new Parking(dp,1000000));
			}
			*/
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public HashMap<Integer,ItineraryTravelTime> loadItineraries(String filename){
		try{
			Scanner in = new Scanner(new File(filename));
			HashMap<Integer,ItineraryTravelTime> itineraries = new HashMap<Integer,ItineraryTravelTime>();
			System.out.println("Simulator::loadItineraries, filename = " + filename);
			
			while(true){
				int taxi_id = in.nextInt();
				//System.out.println("Simulator::loadItineraries, got taxi " + taxi_id);
				if(taxi_id == -2) break;
				ArrayList<Integer> lstPoints = new ArrayList<Integer>();
				HashMap<Integer,Integer> lstArrTime = new HashMap<Integer,Integer>();
				HashMap<Integer,Integer> lstDepTime = new HashMap<Integer,Integer>();
				ArrayList<VehicleAction> lstActions = new ArrayList<VehicleAction>();
				ArrayList<Integer> ids = new ArrayList<Integer>();
				while(true){
					int p = in.nextInt();
					if(p == -1) break;
					lstPoints.add(p);
					int ta = in.nextInt();
					lstArrTime.put(lstPoints.size()-1,ta);
					int td = in.nextInt();
					lstDepTime.put(lstPoints.size()-1,td);
					String action = in.next();
					if(action.equals("PICKUP_PEOPLE")) lstActions.add(VehicleAction.PICKUP_PEOPLE);
					else if(action.equals("DELIVERY_PEOPLE")) lstActions.add(VehicleAction.DELIVERY_PEOPLE);
					else if(action.equals("PICKUP_PARCEL")) lstActions.add(VehicleAction.PICKUP_PARCEL);
					else if(action.equals("DELIVERY_PARCEL")) lstActions.add(VehicleAction.DELIVERY_PARCEL);
					else if(action.equals("PASS")) lstActions.add(VehicleAction.PASS);
					else if(action.equals("STOP")) lstActions.add(VehicleAction.STOP);
					else if(action.equals("FINISH_WORK")) lstActions.add(VehicleAction.FINISH_WORK);
					else{
						System.out.println("Simulator::loadItineraries --> Unknown action " + action);
						System.exit(-1);
					}
					int id = in.nextInt();
					ids.add(id);
					//if(id == 242){
						//System.out.println("Simulator::loadItineraries, ID request = 242, point = " + p + " ta = " + ta + ", td = " + td + ", taxi = " + taxi_id);
						//System.exit(-1);
					//}
				}
				ItineraryTravelTime I = new ItineraryTravelTime(lstPoints, lstArrTime, lstDepTime, ids, lstActions);
				itineraries.put(taxi_id, I);
			}
			in.close();
			return itineraries;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public void initVehicles(){
		mTaxi2Depot = new HashMap<Integer, Integer>();
		this.nbTaxis = lstDepots.size();
		vehicles = new ArrayList<Vehicle>();
		for(int k = 0; k < nbTaxis; k++){
			Vehicle vh = new Vehicle(k+1,map,this);
			vh.log = log;
			vh.logI = logI;
			vh.T = T;
			vehicles.add(vh);
			vh.lastIndexPoint = 0;
			vh.lastPoint = lstDepots.get(k);//curLocation[k];
			//System.out.println(name() + "::initVehicles, taxi " + vh.ID + ", lastPoint = " + vh.lastPoint);
			vh.status = VehicleStatus.REST_AT_PARKING;
			vh.currentItinerary = null;
			
			mTaxi2Depot.put(vh.ID, lstDepots.get(k));
		}
		//System.exit(-1);
	}
	
	public void simulateDataFromFile(String requestFilename, int maxNbParcelsInserted, int maxNbStops){
		double t0 = System.currentTimeMillis();
		loadRequests(requestFilename);
		
		
		
		//if(true) return;
		
		int Th = terminateWorkingTime - startWorkingTime;
		T = new TimeHorizon(startWorkingTime,terminateRequestTime,terminateWorkingTime);
		
		
		pendingParcelRequests = new ArrayList<ParcelRequest>();
		
		initVehicles();
		
		/*
		lstDepots = new ArrayList<Integer>();
		curLocation = new int[nbTaxis];
		for(int k = 0; k < nbTaxis; k++){
			int idx = R.nextInt(map.n);
			int v = map.V.get(idx);
			curLocation[k] = v;
		}
		*/
		
		/*
		lstParkings = new ArrayList<Integer>();
		for(int i = 0; i < 20; i++){
			int idx = R.nextInt(map.n);
			int v = map.V.get(idx);
			lstParkings.add(v);
		}
		*/
		/*
		status = new byte[nbTaxis][T];
		for(int k = 0; k < nbTaxis; k++)
			for(int t = startWorkingTime; t < terminateWorkingTime; t++)
				status[k][t-startWorkingTime] = 0;
		taxiLocationStatus = new HashMap<Integer, ArrayList<TaxiLocation>>();
		*/
		
		distanceRequests = new ArrayList<Double>();
		
		//EventGenerator eg = new EventGenerator(map,T);
		//eg.log = log;
		System.out.println("Simulator::simulateDataFromFile, T = " + T + ", nbTaxis = " + nbTaxis);
		//if(true) return;
		
		//for(int t = startWorkingTime; t < terminateWorkingTime; t++){
		boolean ok = false;
		PeopleRequest peopleR = null;
		ParcelRequest parcelR = null;
		
		for(int i = 0; i < allPeopleRequests.size(); i++){
			PeopleRequest pr = allPeopleRequests.get(i);
			pr.maxNbStops = pr.maxNbStops < maxNbStops ? pr.maxNbStops : maxNbStops;
			runningPeopleRequests.add(pr);
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
		while(!T.finished()){
		//for(int t = startWorkingTime; t < startWorkingTime+3600*2; t++){
			//log.println("time point " + t);
			int t = T.currentTimePoint;
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
			
			//if(ok){ T.move(); continue; }
			
			/*
			
			*/
			//if(t == 21770){
				//System.out.println("At 21780, peopleR id = " + peopleR.id + ", parcelR id = " + parcelR.id);
			//}
			//System.out.println("T = " + t + ", peopleR = " + peopleR.toString() + ", parcelR = " + parcelR.toString());
			
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
					pendingParcelRequests.add(pr);
				}
				if(runningParcelRequests.size() > 0){
					parcelR = runningParcelRequests.get(0);
					runningParcelRequests.remove(0);
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
							ok = true;
						}
					}else{
						System.out.println("Simulator::simulateDataFromFile --> At " + T.currentTimePointHMS() + ", Request (" + pr.pickupLocationID + "," + pr.deliveryLocationID + ") is rejected ???????????????????????, no taxi available");
						log.println("At " + T.currentTimePointHMS() + ", Request (" + pr.pickupLocationID + "," + pr.deliveryLocationID + ") is rejected ???????????????????????, no taxi available");
					}
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
			
		cost = totalDistance*gamma3;
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
	public void initLogI(String fn){
		try{
			logI = new PrintWriter(fn);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void finalizeLogI(){
		logI.close();
	}
	public void writeTaxiItineraries(String fn){
		initLogI(fn);
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			taxi.logI = logI;
		}
		for(int k = 0; k < vehicles.size(); k++){
			Vehicle taxi = vehicles.get(k);
			if(taxi.totalTravelDistance <= 0){
				//nbUnusedTaxis++;
				continue;
			}
			taxi.writeItinerriesToLog();
			//totalDistance = totalDistance + taxi.totalTravelDistance;
			//int costi = (int)(taxi.totalTravelDistance*gamma3/1000);
			//log.println("distance of taxi[" + taxi.ID + "] = " + taxi.totalTravelDistance/1000 + "km, cost fuel = " + costi + "K");
		}
		logI.println(-2);
		finalizeLogI();
	}
	public void simulate(int maxNbParcelsInserted, int probPeopleRequest, int probParcelRequest){
		
		/*
		dijkstra.initLog();
		double cost = dijkstra.solve(19543, 20172);
		int[] P = dijkstra.getPath(19543, 20172);
		printPath(P);
		dijkstra.finalize();
		if(true) return;
		*/
		
		
		
		
		int Th = terminateWorkingTime - startWorkingTime;
		T = new TimeHorizon(startWorkingTime,terminateRequestTime,terminateWorkingTime);
		pendingParcelRequests = new ArrayList<ParcelRequest>();
		
		initVehicles();
		
		
		distanceRequests = new ArrayList<Double>();
		
		EventGenerator eg = new EventGenerator(this,map,T,probPeopleRequest,probParcelRequest);
		eg.log = log;
		System.out.println("T = " + T + ", nbTaxis = " + nbTaxis);
		//if(true) return;
		
		allPeopleRequests = new ArrayList<PeopleRequest>();
		allParcelRequests = new ArrayList<ParcelRequest>();
		
		//for(int t = startWorkingTime; t < terminateWorkingTime; t++){
		boolean ok = false;
		while(!T.finished()){
		//for(int t = startWorkingTime; t < startWorkingTime+3600*2; t++){
			//log.println("time point " + t);
			int t = T.currentTimePoint;
			for(int k = 0; k < nbTaxis; k++){
				Vehicle vh = vehicles.get(k);
				vh.move();
				//System.out.println(vh.toString());
				//log.println(vh.toString());
			}
			//if(ok){ T.move(); continue; }
			
			if(T.stopRequest()){
				//System.out.println("Simulator::simulate stop request!!!!");
				for(int k = 0; k < nbTaxis; k++){
					Vehicle vh = vehicles.get(k);
					if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
						System.out.println("Simulator::simulate, At " + T.currentTimePointHMS() + ", taxi " + vh.ID + ", stopRequest, status = TRAVEL_WITHOUT_LOAD --> GO BACK TO DEPOT");
						setupRouteBack2Depot(vh);
					}else if(vh.status == VehicleStatus.REST_AT_PARKING){
						int curPos = vh.lastPoint;
						int depot = mTaxi2Depot.get(vh.ID);
						if(curPos != depot){
							System.out.println("Simulator::simulate, At " + T.currentTimePointHMS() + ", taxi " + vh.ID + ", stopRequest, status = REST_AT_PARKING --> GO BACK TO DEPOT");
							setupRouteBack2Depot(vh);
						}
					}
				}
			}
			
			ParcelRequest pr = eg.getParcelRequest();
			if(pr != null){
				totalParcelRequests++;
				
				pr.timePoint = t;
				pr.earlyPickupTime = t;
				pr.latePickupTime = T.end;
				pr.earlyDeliveryTime = t;
				pr.lateDeliveryTime = T.end;
				allParcelRequests.add(pr);
				System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", parcel request " + pr.id + " arrives");
				Itinerary I = dijkstra.solve(pr.pickupLocationID, pr.deliveryLocationID);
				//if(I.getDistance() >= dijkstra.infinity){
				if(I == null){
					log.println("At " + T.currentTimePointHMS() + ", cannot serve parcel request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID);
					System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", cannot serve parcel request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID);
					nbDisconnectedRequests++;
				}else{
					pendingParcelRequests.add(pr);
				}
			}
			
			PeopleRequest peopleReq = eg.getPeopleRequest();
			if(peopleReq != null){
				totalPeopleRequests++;
				
				peopleReq.timePoint = t;
				peopleReq.maxNbStops = 5;
				allPeopleRequests.add(peopleReq);
				System.out.println("Simulator::simulate --> At " + T.currentTimePointHMS() + ", people request " + peopleReq.id + " arrives");
				Itinerary I = dijkstra.solve(peopleReq.pickupLocationID, peopleReq.deliveryLocationID);
				//if(I.getDistance() >= dijkstra.infinity){
				if(I == null){
					log.println("At " + T.currentTimePointHMS() + ", cannot serve people request from " + peopleReq.pickupLocationID + " to " + peopleReq.deliveryLocationID + " due to disconnectivity");
					System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", cannot serve people request from " + peopleReq.pickupLocationID + " to " + peopleReq.deliveryLocationID + " due to disconnectivity");
					nbDisconnectedRequests++;
				}else{
					peopleReq.maxTravelDistance = 2*I.getDistance();
					peopleReq.earlyPickupTime = t;
					peopleReq.latePickupTime = t + maxWaitTime;
					peopleReq.earlyDeliveryTime = t;
					peopleReq.lateDeliveryTime = T.end;
					
					Vehicle taxi = findPossibleTaxi(peopleReq.pickupLocationID, peopleReq.deliveryLocationID, maxWaitTime);
					if(taxi != null){
						//System.out.println("Simulator::simulate --> found taxi " + taxi + "!!!!!!!!!!!!!!!!!!!!!!!!!!");
						System.out.println("Simulator::simulate --> At " + T.currentTimePointHMS() + ", request " + peopleReq.pickupLocationID + " --> " + peopleReq.deliveryLocationID + ", found taxi ID = " + taxi.ID + ", description = " + taxi);
						log.println("At " + T.currentTimePointHMS() + ", found taxi " + taxi + "!!!!!!!!!!!!!!!!!!!!!!!!!!");
						log.println("At " + T.currentTimePointHMS() + ", request " + peopleReq.pickupLocationID + " --> " + peopleReq.deliveryLocationID + ", found taxi " + taxi.ID);
						// try to insert parcel requests
						
						//setupRoute(taxi, t, pr.pickupLocationID, pr.deliveryLocationID);
						//setupRoute(taxi,t,pr);
						//boolean hasRoute = setupRouteWithAParcelFollow(taxi, t, pr);
						boolean hasRoute = setupRouteWithParcelsInserted(taxi,t,peopleReq,maxNbParcelsInserted);
						
						if(hasRoute){
							acceptedPeopleRequests++;
							revenue = revenue + alpha + gamma1*I.getDistance();
							distanceRequests.add(I.getDistance());
							ok = true;
						}
					}else{
						System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", Request (" + peopleReq.pickupLocationID + "," + peopleReq.deliveryLocationID + ") is rejected ???????????????????????, no taxi available");
						log.println("At " + T.currentTimePointHMS() + ", Request (" + peopleReq.pickupLocationID + "," + peopleReq.deliveryLocationID + ") is rejected ???????????????????????, no taxi available");
					}
				}
			}
			/*
			AbstractEvent e = eg.nextEvent();
			if(e instanceof PeopleRequest){
				
				totalPeopleRequests++;
				PeopleRequest pr = (PeopleRequest)e;
				pr.timePoint = t;
				pr.maxNbStops = 5;
				allPeopleRequests.add(pr);
				System.out.println("Simulator::simulate --> At " + T.currentTimePointHMS() + ", people request " + pr.id + " arrives");
				Itinerary I = dijkstra.solve(pr.pickupLocationID, pr.deliveryLocationID);
				//if(I.getDistance() >= dijkstra.infinity){
				if(I == null){
					log.println("At " + T.currentTimePointHMS() + ", cannot serve people request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID + " due to disconnectivity");
					System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", cannot serve people request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID + " due to disconnectivity");
					nbDisconnectedRequests++;
				}else{
					pr.maxTravelDistance = 2*I.getDistance();
					pr.earlyPickupTime = t;
					pr.latePickupTime = t + maxWaitTime;
					pr.earlyDeliveryTime = t;
					pr.lateDeliveryTime = T.end;
					
					Vehicle taxi = findPossibleTaxi(pr.pickupLocationID, pr.deliveryLocationID, maxWaitTime);
					if(taxi != null){
						//System.out.println("Simulator::simulate --> found taxi " + taxi + "!!!!!!!!!!!!!!!!!!!!!!!!!!");
						System.out.println("Simulator::simulate --> At " + T.currentTimePointHMS() + ", request " + pr.pickupLocationID + " --> " + pr.deliveryLocationID + ", found taxi ID = " + taxi.ID + ", description = " + taxi);
						log.println("At " + T.currentTimePointHMS() + ", found taxi " + taxi + "!!!!!!!!!!!!!!!!!!!!!!!!!!");
						log.println("At " + T.currentTimePointHMS() + ", request " + pr.pickupLocationID + " --> " + pr.deliveryLocationID + ", found taxi " + taxi.ID);
						// try to insert parcel requests
						
						//setupRoute(taxi, t, pr.pickupLocationID, pr.deliveryLocationID);
						//setupRoute(taxi,t,pr);
						//boolean hasRoute = setupRouteWithAParcelFollow(taxi, t, pr);
						boolean hasRoute = setupRouteWithParcelsInserted(taxi,t,pr,2);
						
						if(hasRoute){
							acceptedPeopleRequests++;
							revenue = revenue + alpha + gamma1*I.getDistance();
							distanceRequests.add(I.getDistance());
							ok = true;
						}
					}else{
						System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", Request (" + pr.pickupLocationID + "," + pr.deliveryLocationID + ") is rejected ???????????????????????, no taxi available");
						log.println("At " + T.currentTimePointHMS() + ", Request (" + pr.pickupLocationID + "," + pr.deliveryLocationID + ") is rejected ???????????????????????, no taxi available");
					}
				}
			}else if(e instanceof ParcelRequest){
				totalParcelRequests++;
				ParcelRequest pr = (ParcelRequest)e;
				pr.timePoint = t;
				pr.earlyPickupTime = t;
				pr.latePickupTime = T.end;
				pr.earlyDeliveryTime = t;
				pr.lateDeliveryTime = T.end;
				allParcelRequests.add(pr);
				System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", parcel request " + pr.id + " arrives");
				Itinerary I = dijkstra.solve(pr.pickupLocationID, pr.deliveryLocationID);
				//if(I.getDistance() >= dijkstra.infinity){
				if(I == null){
					log.println("At " + T.currentTimePointHMS() + ", cannot serve parcel request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID);
					System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", cannot serve parcel request from " + pr.pickupLocationID + " to " + pr.deliveryLocationID);
					nbDisconnectedRequests++;
				}else{
					pendingParcelRequests.add(pr);
				}
			}else{
				
			}
			*/
			
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
			double costi = (taxi.totalTravelDistance*gamma3/1000);
			log.println("distance of taxi[" + taxi.ID + "] = " + taxi.totalTravelDistance/1000 + "km, cost fuel = " + costi + "K");
		}
		//logI.println(-2);
		
		for(int i = 0; i < distanceRequests.size(); i++){
			double D = distanceRequests.get(i);
			double m = (alpha + gamma1*D);
			D = D/1000;
			m = m/1000;
			log.println("requests " + i + " has distance = " + D + "km, money = " + m + "K");
		}
		
		cost = totalDistance*gamma3;
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
		
	}
	public void simulateGenerateRequest(int maxNbParcelsInserted, int probPeopleRequest, int probParcelRequest){
		
		
		int Th = terminateWorkingTime - startWorkingTime;
		//System.out.println("startWorkingTime = " + startWorkingTime + ", terminateRequest = " + terminateRequestTime);
		T = new TimeHorizon(startWorkingTime,terminateRequestTime,terminateWorkingTime);
		pendingParcelRequests = new ArrayList<ParcelRequest>();
		
		initVehicles();
		
		
		distanceRequests = new ArrayList<Double>();
		
		EventGenerator eg = new EventGenerator(this,map,T,probPeopleRequest,probParcelRequest);
		//if(true)return;
		
		eg.log = log;
		System.out.println("T = " + T + ", nbTaxis = " + nbTaxis);
		//if(true) return;
		
		allPeopleRequests = new ArrayList<PeopleRequest>();
		allParcelRequests = new ArrayList<ParcelRequest>();
		
		
		
		//for(int t = startWorkingTime; t < terminateWorkingTime; t++){
		boolean ok = false;
		while(!T.finished()){
		//for(int t = startWorkingTime; t < startWorkingTime+3600*2; t++){
			//log.println("time point " + t);
			int t = T.currentTimePoint;
			//System.out.println("t = " + t);
			//System.exit(-1);
			for(int k = 0; k < nbTaxis; k++){
				Vehicle vh = vehicles.get(k);
				vh.move();
				//System.out.println(vh.toString());
				//log.println(vh.toString());
			}
			//if(ok){ T.move(); continue; }
			
			if(T.stopRequest()){
				//System.out.println("Simulator::simulate stop request!!!!");
				for(int k = 0; k < nbTaxis; k++){
					Vehicle vh = vehicles.get(k);
					if(vh.status == VehicleStatus.TRAVEL_WITHOUT_LOAD){
						System.out.println("Simulator::simulate, At " + T.currentTimePointHMS() + ", taxi " + vh.ID + ", stopRequest, status = TRAVEL_WITHOUT_LOAD --> GO BACK TO DEPOT");
						setupRouteBack2Depot(vh);
					}else if(vh.status == VehicleStatus.REST_AT_PARKING){
						int curPos = vh.lastPoint;
						int depot = mTaxi2Depot.get(vh.ID);
						if(curPos != depot){
							System.out.println("Simulator::simulate, At " + T.currentTimePointHMS() + ", taxi " + vh.ID + ", stopRequest, status = REST_AT_PARKING --> GO BACK TO DEPOT");
							setupRouteBack2Depot(vh);
						}
					}
				}
			}
			
			//ParcelRequest pr = eg.getParcelRequest();
			ParcelRequest pr = eg.getParcelRequestLongDistance();
			if(pr != null){
				totalParcelRequests++;
				
				pr.timePoint = t;
				pr.earlyPickupTime = t;
				pr.latePickupTime = T.end;
				pr.earlyDeliveryTime = t;
				pr.lateDeliveryTime = T.end;
				allParcelRequests.add(pr);
			}
			
			//PeopleRequest peopleReq = eg.getPeopleRequest();
			PeopleRequest peopleReq = eg.getPeopleRequestLongDistance();
			
			if(peopleReq != null){
				totalPeopleRequests++;
				
				peopleReq.timePoint = t;
				peopleReq.maxNbStops = 5;
				allPeopleRequests.add(peopleReq);
				System.out.println("Simulator::simulate --> At " + T.currentTimePointHMS() + ", people request " + peopleReq.id + " arrives");
				Itinerary I = dijkstra.solve(peopleReq.pickupLocationID, peopleReq.deliveryLocationID);
				//if(I.getDistance() >= dijkstra.infinity){
				if(I == null){
					log.println("At " + T.currentTimePointHMS() + ", cannot serve people request from " + peopleReq.pickupLocationID + " to " + peopleReq.deliveryLocationID + " due to disconnectivity");
					System.out.println("Simulator::simulate At " + T.currentTimePointHMS() + ", cannot serve people request from " + peopleReq.pickupLocationID + " to " + peopleReq.deliveryLocationID + " due to disconnectivity");
					nbDisconnectedRequests++;
				}else{
					peopleReq.maxTravelDistance = 2*I.getDistance();
					peopleReq.earlyPickupTime = t;
					peopleReq.latePickupTime = t + maxWaitTime;
					peopleReq.earlyDeliveryTime = t;
					peopleReq.lateDeliveryTime = T.end;
					
				}
			}
			
			T.move();
			//log.println("-------------------");
		}
		
	}

	public void loadParameters(String filename){
		try{
			Scanner in = new Scanner(new File(filename));
			String s = in.nextLine();
			s = in.nextLine();
			maxSpeedkmh = Double.valueOf(s);
			maxSpeedms = maxSpeedkmh*1000/3600;
			//System.out.println("s = " + s + ", maxSpeedkmh = " + maxSpeedkmh);
			s = in.nextLine();
			
			s = in.nextLine();
			minSpeedkmh = Double.valueOf(s.trim());
			minSpeedms = minSpeedkmh*1000/3600;
			s = in.nextLine();
			s = in.nextLine();
			startWorkingTime = Integer.valueOf(s.trim());
			s = in.nextLine();
			s = in.nextLine();
			terminateRequestTime = Integer.valueOf(s);
			s = in.nextLine();
			s = in.nextLine();
			terminateWorkingTime = Integer.valueOf(s);
			//System.out.println("Simulator::loadParameters(" + filename + "), startWorkingTime = " + 
			//startWorkingTime + ", terminateWorkingTime = " + terminateWorkingTime);
			//System.exit(-1);
			s = in.nextLine();
			//System.out.println("s = " + s);
			s = in.nextLine();
			//System.out.println("s = " + s);
			maxWaitTime = Integer.valueOf(s.trim());
			s = in.nextLine();
			//System.out.println("s = " + s);
			s = in.nextLine();
			//System.out.println("s = " + s);
			Qk = Integer.valueOf(s.trim());
			s = in.nextLine();
			s = in.nextLine();
			alpha = Double.valueOf(s.trim());
			s = in.nextLine();
			s = in.nextLine();
			beta = Double.valueOf(s.trim());
			s = in.nextLine();
			s = in.nextLine();
			gamma1 = Double.valueOf(s.trim()) * 0.001;// people fare charge per m
			s = in.nextLine();
			s = in.nextLine();
			gamma2 = Double.valueOf(s.trim()) * 0.001; // parcel fare charge per m
			s = in.nextLine();
			s = in.nextLine();
			gamma3 = Double.valueOf(s.trim()) * 0.001;// fuel cost per m
			s = in.nextLine();
			s = in.nextLine();
			gamma4 = Double.valueOf(s.trim());
			s = in.nextLine();
			s = in.nextLine();
			maxDistanceFactor = Double.valueOf(s.trim());
			/*
			System.out.println("maxSpeedms = " +  maxSpeedms);
			System.out.println("minSpeedms = " + minSpeedms);
			System.out.println("startWorkingTime = " + startWorkingTime);
			System.out.println("terminateWorkingTime = " + terminateWorkingTime);
			System.out.println("maxWaitTime = " + maxWaitTime);
			System.out.println("Qk = " + Qk);
			System.out.println("alpha = " + alpha);
			System.out.println("beta = " + beta);
			System.out.println("gamma1 = " + gamma1);
			System.out.println("gamma2 = " + gamma2);
			System.out.println("gamma3 = " + gamma3);
			System.out.println("gamma4 = " + gamma4);
			*/
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void writeRequestsToTextFile(String fn){
		try{
			PrintWriter out = new PrintWriter(fn);
			out.println("People requests (id, time_call, pickup_point, delivery_point, early_pickup_time, late_pickup_time, " +
					"early_delivery_time, late_delivery_time, max_travel_distance (m), maxNbStops)");
			for(int i = 0; i < allPeopleRequests.size(); i++){
				PeopleRequest pr = allPeopleRequests.get(i);
				out.println(pr.id + "\t" + pr.timePoint + "\t" + pr.pickupLocationID + "\t" + pr.deliveryLocationID + "\t" +
						pr.earlyPickupTime + "\t" + pr.latePickupTime + "\t" + pr.earlyDeliveryTime + "\t" + pr.lateDeliveryTime + "\t" +
						pr.maxTravelDistance + "\t" + pr.maxNbStops);
			}
			out.println("-1");
			out.println("Parcel requests (id, time_call, pickup_point, delivery_point, early_pickup_time, late_pickup_time, " +
					"early_delivery_time, late_delivery_time)");
			for(int i = 0; i < allParcelRequests.size(); i++){
				ParcelRequest pr = allParcelRequests.get(i);
				out.println(pr.id + "\t" + pr.timePoint + "\t" + pr.pickupLocationID + "\t" + pr.deliveryLocationID + "\t" +
						pr.earlyPickupTime + "\t" + pr.latePickupTime + "\t" + pr.earlyDeliveryTime + "\t" + pr.lateDeliveryTime);
			}
			out.println("-1");
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public PeopleRequest getPeopleRequest(int rid){
		for(int i = 0; i < allPeopleRequests.size(); i++){
			if(allPeopleRequests.get(i).id == rid) return allPeopleRequests.get(i);
		}
		return null;
	}
	public ParcelRequest getParcelRequest(int rid){
		for(int i = 0; i < allParcelRequests.size(); i++){
			if(allParcelRequests.get(i).id == rid) return allParcelRequests.get(i);
		}
		return null;
	}
	private void preprocessTimeCallPeopleRequest(ArrayList<PeopleRequest> peopleReq){
		if(peopleReq.size() == 0) return;
		PeopleRequest[] t = new PeopleRequest[peopleReq.size()];
		for(int i = 0; i < peopleReq.size(); i++)
			t[i] = peopleReq.get(i);
		
		// sort t in an increasing order
		for(int i = 0; i < t.length-1; i++){
			for(int j = i+1; j < t.length; j++){
				if(t[i].timePoint > t[j].timePoint){
					PeopleRequest tmp = t[i]; t[i] = t[j]; t[j] = tmp;
				}
			}
		}
		
		int d = 0;
		int nic = 0;
		int v = t[0].timePoint;
		for(int i = 1; i < t.length; i++){
			if(t[i].timePoint == v){
				d++;
				t[i].timePoint += (nic+d);
			}else{
				if(t[i-1].timePoint >= t[i].timePoint){
					nic = t[i-1].timePoint - t[i].timePoint + 1;
				}else nic = 0;
				d = 0;
				v = t[i].timePoint;
				t[i].timePoint += nic;
			}
		}
		peopleReq.clear();
		for(int i = 0; i < t.length; i++)
			peopleReq.add(t[i]);
	}
	private void preprocessTimeCallParcelRequest(ArrayList<ParcelRequest> parcelReq){
		if(parcelReq.size() == 0) return;
		ParcelRequest[] t = new ParcelRequest[parcelReq.size()];
		for(int i = 0; i < parcelReq.size(); i++)
			t[i] = parcelReq.get(i);
		
		// sort t in an increasing order
		for(int i = 0; i < t.length-1; i++){
			for(int j = i+1; j < t.length; j++){
				if(t[i].timePoint > t[j].timePoint){
					ParcelRequest tmp = t[i]; t[i] = t[j]; t[j] = tmp;
				}
			}
		}
		
		int d = 0;
		int nic = 0;
		int v = t[0].timePoint;
		for(int i = 1; i < t.length; i++){
			if(t[i].timePoint == v){
				d++;
				t[i].timePoint += (nic+d);
			}else{
				if(t[i-1].timePoint >= t[i].timePoint){
					nic = t[i-1].timePoint - t[i].timePoint + 1;
				}else nic = 0;
				d = 0;
				v = t[i].timePoint;
				t[i].timePoint += nic;
			}
		}
		parcelReq.clear();
		for(int i = 0; i < t.length; i++)
			parcelReq.add(t[i]);
	}
	
	public void loadRequests(String fn){
		System.out.println(name() + "::loadRequest, filename = " + fn);
		try{
			Scanner in = new Scanner(new File(fn));
			allPeopleRequests = new ArrayList<PeopleRequest>();
			allParcelRequests = new ArrayList<ParcelRequest>();
			mPeopleRequest = new HashMap<Integer, PeopleRequest>();
			mParcelRequest = new HashMap<Integer, ParcelRequest>();
			
			String str = in.nextLine();
			//System.out.println("people request str = " + str);
			while(true){
				int id = in.nextInt();
				//System.out.println("people id = " + id);
				if(id == -1) break;
				int timePoint = in.nextInt();
				int pickupLocationID = in.nextInt();
				int deliveryLocationID = in.nextInt();
				int earlyPickupTime = in.nextInt();
				int latePickupTime = in.nextInt();
				int earlyDeliveryTime = in.nextInt();
				int lateDeliveryTime = in.nextInt();
				double maxDistance = in.nextDouble();
				int maxNbStops = in.nextInt();
				
				if(timePoint < 0) continue;
				//timePoint = earlyPickupTime;
				
				PeopleRequest pr = new PeopleRequest(pickupLocationID,deliveryLocationID);
				pr.id = id;
				pr.timePoint = timePoint;
				pr.earlyPickupTime = earlyPickupTime;
				pr.latePickupTime = latePickupTime;
				pr.earlyDeliveryTime = earlyDeliveryTime;
				pr.lateDeliveryTime = lateDeliveryTime;
				pr.maxTravelDistance = maxDistance;
				pr.maxNbStops = maxNbStops;
				allPeopleRequests.add(pr);
				mPeopleRequest.put(pr.id, pr);
			}
			str = in.nextLine(); str = in.nextLine();
			//System.out.println("parcel request str = " + str);
			while(true){
				int id = in.nextInt();
				//System.out.println("parcel request id = " + id);
				if(id == -1) break;
				int timePoint = in.nextInt();
				int pickupLocationID = in.nextInt();
				int deliveryLocationID = in.nextInt();
				int earlyPickupTime = in.nextInt();
				int latePickupTime = in.nextInt();
				int earlyDeliveryTime = in.nextInt();
				int lateDeliveryTime = in.nextInt();
				
				if(timePoint < 0) continue;
				//timePoint = earlyPickupTime;
				
				ParcelRequest pr = new ParcelRequest(pickupLocationID,deliveryLocationID);
				pr.id = id;
				pr.timePoint = timePoint;
				pr.earlyPickupTime = earlyPickupTime;
				pr.latePickupTime = latePickupTime;
				pr.earlyDeliveryTime = earlyDeliveryTime;
				pr.lateDeliveryTime = lateDeliveryTime;
				
				allParcelRequests.add(pr);
				mParcelRequest.put(pr.id, pr);
			}
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		preprocessTimeCallPeopleRequest(allPeopleRequests);
		preprocessTimeCallParcelRequest(allParcelRequests);
		
		double min_lat = 1000000;
		double max_lat = -min_lat;
		double min_lng = 1000000;
		double max_lng = -min_lng;
		
		for(int i = 0; i < allPeopleRequests.size(); i++){
			PeopleRequest r = allPeopleRequests.get(i);
			LatLng ll = map.mLatLng.get(r.pickupLocationID);
			min_lat = min_lat < ll.lat ? min_lat : ll.lat;
			min_lng = min_lng < ll.lng ? min_lng : ll.lng;
			max_lat = max_lat > ll.lat ? max_lat : ll.lat;
			max_lng = max_lng > ll.lng ? max_lng : ll.lng;
			
			ll = map.mLatLng.get(r.deliveryLocationID);
			min_lat = min_lat < ll.lat ? min_lat : ll.lat;
			min_lng = min_lng < ll.lng ? min_lng : ll.lng;
			max_lat = max_lat > ll.lat ? max_lat : ll.lat;
			max_lng = max_lng > ll.lng ? max_lng : ll.lng;
		}
		System.out.println("Simulator::loadRequests -> windows request = " + min_lat + "," + 
		min_lng + "\t" + max_lat + "," + max_lng);
		//System.exit(-1);
	}
	public ArrayList<Integer> genPointType(ArrayList<Integer> I, int maxStop){
		ArrayList<Integer> T = new ArrayList<Integer>();
		// generate sequence of types of points of the itinerary I
		// maxStop is the maximum number of stops between a people service
		// 1: pickup people
		// -1: delivery people
		// 2: pickup parcel
		// -2: delivery parcel
		T.add(1);
		int last = T.get(0);
		for(int i = 1; i < I.size(); i++){
			if(last == 1) last = 2;
			else if(last == 2) last = -2;
			else if(last == -2) last = -1;
			else if(last == -1) last = 1;
			T.add(last);
		}
		return T;
	}
	public void gen(int nbTaxis, int nbPeopleRequests, int nbParcelRequests, int nbParkings, int maxEta){
		// eta is the maximum number of stops between a people service
		DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(map.V, map.A);
		this.nbTaxis = nbTaxis;
		//this.nbPeopleRequests = nbPeopleRequests;
		//this.nbParcelRequests = nbParcelRequests;
		this.nbParkings = nbParkings;
		
		lstDepots = new ArrayList<Integer>();
		lstParkings = new ArrayList<Parking>();
		lstOrgPeople = new ArrayList<Integer>();
		lstDesPeople = new ArrayList<Integer>();
		lstOrgParcel = new ArrayList<Integer>();
		lstDesParcel = new ArrayList<Integer>();
		lstArrTimeOrgPeople = new ArrayList<Integer>();
		lstDepTimeOrgPeople = new ArrayList<Integer>();
		lstArrTimeDesPeople = new ArrayList<Integer>();
		lstDepTimeDesPeople = new ArrayList<Integer>();
		lstArrTimeOrgParcel = new ArrayList<Integer>();
		lstDepTimeOrgParcel = new ArrayList<Integer>();
		lstArrTimeDesParcel = new ArrayList<Integer>();
		lstDepTimeDesParcel = new ArrayList<Integer>();
		/*
		for(int k = 0; k < nbTaxis; k++){
			int idx = R.nextInt(n);
			int depot = V.get(idx);
			lstDepots.add(depot);
		}
		*/
		for(int k = 0; k < nbParkings; k++){
			int idx = R.nextInt(map.n);
			int locID = map.V.get(idx);
			lstParkings.add(new Parking(locID,20));
		}
		
		for(int k = 0; k < nbTaxis; k++){
			ArrayList<Integer> I;
			int depot = -1;
			while(true){
				depot = map.V.get(R.nextInt(map.n));
				I = genItinerary(depot,100000,5*3600,500,10000);
				if(I != null) break;
			}
			lstDepots.add(depot);
			
			System.out.println("I[" + k + "] = " + I.size());
			int startTimek = R.nextInt(7200) + startWorkingTime;
			while(I.size()%4!=0) I.remove(I.size()-1);// remove the last item if I.sz is odd
			int lastPickup = -1;
			int c = 0;// number of stops between people service where pickup point is lastPickup 
			int mC = -1;//maximum number of stops between people service where pickup point is lastPickup
			int eta = R.nextInt(4);
			ArrayList<Integer> pointTypes = genPointType(I, eta);
			//System.out.print("Seq of I.sz = " + I.size() + ", eta = " + eta + " is: ");
			int curPos = lstDepots.get(k);
			int curT = startWorkingTime;
			for(int i = 0; i < pointTypes.size(); i++){
				//System.out.print(pointTypes.get(i) + " ");
				int v = I.get(i);
				int type = pointTypes.get(i);
				double speedms = R.nextInt((int)Math.ceil(maxSpeedms-minSpeedms)) + 2*minSpeedms;
				Itinerary Ii = dijkstra.solve(curPos, v); 
				double dis = Ii.getDistance();
				int arrTime = curT + getTravelTime(dis, speedms);
				int depTime = arrTime + 60;
				if(type == 1){
					lstOrgPeople.add(v);
					lstArrTimeOrgPeople.add(arrTime);
					lstDepTimeOrgPeople.add(depTime);
				}else if(type == -1){
					lstDesPeople.add(v);
					lstArrTimeDesPeople.add(arrTime);
					lstDepTimeDesPeople.add(depTime);
				}else if(type == 2){
					lstOrgParcel.add(v);
					lstArrTimeOrgParcel.add(arrTime);
					lstDepTimeOrgParcel.add(depTime);
				}else if(type == -2){
					lstDesParcel.add(v);
					lstArrTimeDesParcel.add(arrTime);
					lstDepTimeDesParcel.add(depTime);
				}
			}
			//System.out.println();
		}
		nbPeopleRequests = lstOrgPeople.size();
		nbParcelRequests = lstOrgParcel.size();
		
		/*
		for(int k = 0; k < nbPeopleRequests; k++){
			int idx = R.nextInt(n);
			int v = V.get(idx);
			lstOrgPeople.add(v);
			idx = R.nextInt(n);
			v = V.get(idx);
			lstDesPeople.add(v);
		}
		
		for(int k = 0; k < nbParcelRequests; k++){
			int idx = R.nextInt(n);
			int v = V.get(idx);
			lstOrgParcel.add(v);
			idx = R.nextInt(n);
			v = V.get(idx);
			lstDesParcel.add(v);
		}
		*/
	}
	public void writeToFile(String filename){
		try{
			System.out.println("Start write to file " + filename);
			DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(map.V, map.A);
			PrintWriter out = new PrintWriter(filename);
			out.println("#nbPassengers  #nbParcels  #nbTaxis  #nbParkings");
			nbPeopleRequests = lstOrgPeople.size();
			nbParcelRequests = lstOrgParcel.size();
			out.println(nbPeopleRequests + " " + nbParcelRequests + " " + nbTaxis + " " + nbParkings);
			out.println(-1);
			out.println("#id	#lat	#lng (source passengers)");
			for(int i = 0; i < lstOrgPeople.size(); i++){
				int v = lstOrgPeople.get(i);
				LatLng ll = map.mLatLng.get(v);
				out.println(v + " " + ll.lat + " " + ll.lng);
			}
			out.println(-1);
			System.out.println("finished write people and parcel requests");
			
			out.println("#id	#lat	#lng (source parcels)");
			for(int i = 0; i < lstOrgParcel.size(); i++){
				int v = lstOrgParcel.get(i);
				LatLng ll = map.mLatLng.get(v);
				out.println(v + " " + ll.lat + " " + ll.lng);
			}
			out.println(-1);
			
			out.println("#id	#lat	#lng (destination passengers)");
			for(int i = 0; i < lstDesPeople.size(); i++){
				int v = lstDesPeople.get(i);
				LatLng ll = map.mLatLng.get(v);
				out.println(v + " " + ll.lat + " " + ll.lng);
			}
			out.println(-1);
			
			out.println("#id	#lat	#lng (destination parcels)");
			for(int i = 0; i < lstDesParcel.size(); i++){
				int v = lstDesParcel.get(i);
				LatLng ll = map.mLatLng.get(v);
				out.println(v + " " + ll.lat + " " + ll.lng);
			}
			out.println(-1);
			
			out.println("#id	#lat	#lng (parkings)");
			for(int i = 0; i < lstParkings.size(); i++){
				int v = lstParkings.get(i).locationID;
				LatLng ll = map.mLatLng.get(v);
				out.println(v + " " + ll.lat + " " + ll.lng);
			}
			out.println(-1);
			
			out.println("#id	#lat	#lng (depots of taxis)");
			for(int i = 0;i < lstDepots.size(); i++){
				int v = lstDepots.get(i);
				LatLng ll = map.mLatLng.get(v);
				out.println(v + " " + ll.lat + " " + ll.lng);
			}
			out.println(-1);
			
			out.println("#id  #early  #late  #max_wait_time  #request");
			for(int i = 0; i < lstArrTimeOrgPeople.size(); i++){
				int id = lstOrgPeople.get(i);
				int ta = lstArrTimeOrgPeople.get(i);
				int td = lstDepTimeOrgPeople.get(i);
				int early = td - 30;
				int late = td + 30;
				int max_wait_time = 60;
				int qi = 0;
				out.println(id + " " + early + " " + late + " " + max_wait_time + " " + qi);
			}
			for(int i = 0; i < lstArrTimeDesPeople.size(); i++){
				int id = lstDesPeople.get(i);
				int ta = lstArrTimeDesPeople.get(i);
				int td = lstDepTimeDesPeople.get(i);
				int early = td - 30;
				int late = td + 30;
				int max_wait_time = 60;
				int qi = 0;
				out.println(id + " " + early + " " + late + " " + max_wait_time + " " + qi);
			}
			for(int i = 0; i < lstArrTimeOrgParcel.size(); i++){
				int id = lstOrgParcel.get(i);
				int ta = lstArrTimeOrgParcel.get(i);
				int td = lstDepTimeOrgParcel.get(i);
				int early = td - 30;
				int late = td + 30;
				int max_wait_time = 60;
				int qi = 1;
				out.println(id + " " + early + " " + late + " " + max_wait_time + " " + qi);
			}
			for(int i = 0; i < lstArrTimeOrgParcel.size(); i++){
				int id = lstDesParcel.get(i);
				int ta = lstArrTimeDesParcel.get(i);
				int td = lstDepTimeDesParcel.get(i);
				int early = td - 30;
				int late = td + 30;
				int max_wait_time = 60;
				int qi = -1;
				out.println(id + " " + early + " " + late + " " + max_wait_time + " " + qi);
			}
			out.println(-1);
			
			out.println("#passenger_src_id  #Di  #Ti   #MaxDi  #MaxTi	#eta");
			for(int i = 0; i < lstOrgPeople.size(); i++){
				int src = lstOrgPeople.get(i);
				int des = lstDesPeople.get(i);
				Itinerary Ii = dijkstra.solve(src, des); 
				double Di = Ii.getDistance();
				int Ti = getTravelTime(Di,maxSpeedms);
				double maxDi = 2*Di;
				int maxTi = 2*Ti;
				int eta = 2;
				out.println(src + " " + Di + " " + Ti + " " + maxDi + " " + maxTi + " " + eta);
				System.out.println(src + " " + Di + " " + Ti + " " + maxDi + " " + maxTi + " " + eta);
			}
			out.println(-1);
			
			out.println("T #Qk #eta  #alpha  #beta  #gamma1  #gamma2  #gama3  #gama4");
			int T = terminateWorkingTime - startWorkingTime;
			out.println(T + " 1	2	10000	10000	9000	5000	20	40000");
			out.println(-1);
			
			out.println("#parking_id  #capacity");
			for(int i = 0; i < lstParkings.size(); i++){
				int v = lstParkings.get(i).locationID;
				int capacity = 10;
				out.println(v + " " + capacity);
			}
			out.println(-1);
			
			out.println("#src  #des  #distance (m)  #min_travel_time  #max_travel_time");
			for(int i = 0; i < map.Arcs.size(); i++){
				Arc a = map.Arcs.get(i);
				int mint = getTravelTime(a.w, maxSpeedms);
				int maxt = getTravelTime(a.w, minSpeedms);
				out.println(a.begin + " " + a.end + " " + a.w + " " + mint + " " + maxt);
			}
			out.println(-1);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void generateDepotParking(int nbTaxis, int nbParkings, String fn){
		try{
			System.out.println("Simulator::generateDepotParkings(nbTaxis = " + nbTaxis + ", nbParkings = " + nbParkings + ") filename = " + fn);
			PrintWriter out = new PrintWriter(fn);
			out.println("Depots");
			for(int i = 0; i < nbTaxis; i++){
				int k = map.V.get(R.nextInt(map.V.size()));
				out.print(k + " ");
			}
			out.println(-1);
			out.println("Parkings");
			for(int i = 0; i < nbParkings; i++){
				int k = map.V.get(R.nextInt(map.V.size()));
				int capacity = R.nextInt(10)+10;
				out.println(k + " " + capacity);
			}
			out.println("-1");
			
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public int computeBenefits(ArrayList<Itinerary> I, ArrayList<PeopleRequest> peoplerequests, ArrayList<ParcelRequest> parcelrequests){
		
		return 0;
	}
	
	public AnalysisTemplate analyzeSolution(HashMap<Integer, ItineraryTravelTime> itineraries){
		SolutionAnalyzer analyzer = new SolutionAnalyzer(this);
		AnalysisTemplate AT = analyzer.analyzeSolution(itineraries);
		analyzer.finalize();
		return AT;
	}
	public void checkDistance(int s, int t){
		dijkstra.initLog();
		Itinerary I = dijkstra.queryShortestPath(s, t);
		Itinerary I1 = dijkstra.solveWithoutHeap(s, t);
		System.out.println("I = " + I.getDistance() + ", I1 = " + I1.getDistance());
		dijkstra.finalize();
	}
	public void checkDistance(String fn){
		try{
			Scanner in = new Scanner(new File(fn));
			while(true){
				int s, t;
				double d;
				s = in.nextInt();
				if(s == -2) break;
				t = in.nextInt();
				d = in.nextDouble();
				ArrayList<Integer> P = new ArrayList<Integer>();
				while(true){
					int v = in.nextInt();
					if(v == -1) break;
					P.add(v);
				}
				double di = 0;
				for(int i = 0; i < P.size()-1; i++){
					int u = P.get(i);
					int v = P.get(i+1);
					Arc a = map.getArc(u, v);
					if(a == null){
						System.out.println("Arc(" + u + "," + v + ") does not exsist!!!!!!!!");
						System.exit(-1);
					}
					di = di + a.w;
				}
				if(Math.abs(d - di) < 0.00001){
					System.out.println("d = " + d + " while di = " + di);
					System.exit(-1);
				}
				System.out.println("d = " + d + " == di = " + di);
				Itinerary I = dijkstra.queryShortestPath(s, t);
				System.out.println("Distance from " + s + " to " + t + " = " + I.getDistance());
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public String peopleParcelServiceToString(){
		return "nbPeopleRejects = " + nbPeopleRejects +
		", nbParcelRejects = " + nbParcelRejects + 
		", nbPeopleComplete = " + nbPeopleComplete + 
		", nbParcelComplete = " + nbParcelComplete +
		", nbPeopleOnBoard = " + nbPeopleOnBoard +
		", nbParcelOnBoard = " + nbParcelOnBoard +
		", nbPeopleServed = " + nbPeopleServed +
		", nbParcelServed = " + nbParcelServed +
		", nbPeopleWaitBoarding = " + nbPeopleWaitBoarding +
		", nbParcelWaitBoarding = " + nbParcelWaitBoarding;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		ArrayList<Integer> L = new ArrayList<Integer>();
		for(int i = 1; i <= 100; i++) L.add(i*10);
		int idx = 5;
		while(L.size() > idx) L.remove(idx);
		for(int i = 0; i < L.size(); i++)
			System.out.println(L.get(i));
		
		if(true)return;
		*/
		
		Simulator sim = new Simulator();
		
		/*
		sim.loadParameters("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/config-parameters.txt");
		if(true) return;
		*/
		
		//sim.loadMapFromTextFile("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/map-hanoi.txt");
		/*
		sim.loadMapFromTextFile("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/map-hanoi-connected.txt");
		sim.loadParameters("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/config-parameters.txt");
		*/
		
		
		//sim.loadMapFromTextFile("map-hanoi-connected.txt");
		//sim.loadParameters("config-parameters.txt");
		sim.loadMapFromTextFile("tokyo_map2.txt");
		sim.loadParameters("tokyo_config-parameters.txt");
		
		
		/*
		sim.generateDepotParking(300, 20, "/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/depots300-parkings20.txt");
		if(true) return;
		*/
		
		
		//int nbTaxis = 10;
		//int nbPeopleRequests = 200;
		//int nbParcelRequests = 200;
		//int nbParkings = 20;
		//MBDG.gen(nbTaxis, nbPeopleRequests, nbParcelRequests, nbParkings, 4);
		//sim.loadDepotParkings("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/depots-parkings.txt");
		
		
		//sim.loadDepotParkings("depots300-parkings20.txt");
		sim.loadDepotParkings("tokyo_depot2.txt");
		
		int probPeople = 10;
		int probParcel = 10;
		String requestFN = "tokyo_requests2.txt";
		//String requestFN = "requests-long-people-10-parcel-10.ins1.txt";
		//String itinerariesFN = "Itineraries-people-10-parcel-10-maxParcelInserted-0-maxStops-0.txt";
		String itinerariesFN = "out.txt";
		//String itinerariesFN = "result_requests-0-1-2.txt";
		
		/*
		sim.simulateDataFromFile(requestFN,1,2);
		sim.writeTaxiItineraries(itinerariesFN);
		*/
		
		
		
		//sim.simulate();
		//sim.simulateGenerateRequest(0, probPeople, probParcel);
		//sim.writeRequestsToTextFile(requestFN);
		//sim.writeTaxiItineraries(itinerariesFN);
		
		
		
		
		sim.initVehicles();
		sim.loadRequests(requestFN);
		HashMap<Integer, ItineraryTravelTime> itineraries = sim.loadItineraries(itinerariesFN);
		sim.analyzeSolution(itineraries);
		
		
		//MBDG.writeToFile("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/sarp-hanoi-data.txt");
		
		sim.finalize();
	}

}
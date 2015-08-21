package pbts.datamanipulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.util.*;

import pbts.entities.LatLng;
import pbts.entities.ParcelRequest;
import pbts.entities.PeopleRequest;
import pbts.shortestpaths.DijkstraBinaryHeap;
import pbts.simulation.*;
class PeopleRequestSanFrancisco{
	int id;
	int timeCall;
	int pickupLocationID;
	int deliveryLocationID;
	int earlyPickupTime;
	int latePickupTime;
	int earlyDeliveryTime;
	int lateDeliveryTime;
	double maxTravelDistance;
	int maxNbStops;
	
	public PeopleRequestSanFrancisco(int id, int timeCall, int pickupLocationID, int deliveryLocationID,
			int earlyPickupTime, int latePickupTime, int earlyDeliveryTime, int lateDeliveryTime,
			double maxTravelDistance, int maxNbStops){
		this.id = id;
		this.timeCall = timeCall;
		this.pickupLocationID = pickupLocationID;
		this.deliveryLocationID = deliveryLocationID;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.maxTravelDistance = maxTravelDistance;
		this.maxNbStops = maxNbStops;
	}
}
public class RequestSanFrancisco {

	RoadMap M;
	
	public void loadMap(String fn){
		M = new RoadMap();
		M.loadData(fn);
	}
	public void analyze(String fn){
		try{
			double t0 = System.currentTimeMillis();
			double min_lat = 100000;
			double max_lat = -min_lat;
			double min_lng = 1000000;
			double max_lng = -min_lng;
			Scanner in = new Scanner(new File(fn));
			int lines = 0;
			HashSet<Integer> taxis = new HashSet<Integer>();
			HashMap<Integer, Integer> mR = new HashMap<Integer, Integer>();
			ArrayList<Integer> days = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Integer>> mTaxiDay = new HashMap<Integer, HashSet<Integer>>();
			while(in.hasNext()){
				int taxiID = in.nextInt();
				lines++;
				//System.out.println("Line "+ lines + ", taxiID = "+ taxiID);
				if(taxiID == 0) break;
				taxis.add(taxiID);
				double s_t = in.nextDouble();
				Date s_d = new Date((long)s_t*1000);
				double s_lat = in.nextDouble();
				double s_lng = in.nextDouble();
				double e_t = in.nextDouble();
				Date e_d = new Date((long)e_t*1000);
				double e_lat = in.nextDouble();
				double e_lng = in.nextDouble();
				
				min_lat = min_lat < s_lat ? min_lat : s_lat;
				min_lat = min_lat < e_lat ? min_lat : e_lat;
				max_lat = max_lat > s_lat ? max_lat : s_lat;
				max_lat = max_lat > e_lat ? max_lat : e_lat;
				
				min_lng = min_lng < s_lng ? min_lng : s_lng;
				min_lng = min_lng < e_lng ? min_lng : e_lng;
				max_lng = max_lng > s_lng ? max_lng : s_lng;
				max_lng = max_lng > e_lng ? max_lng : e_lng;
				
				System.out.println("Line " + lines + ", taxi " + taxiID + ", Start " + s_lat + "," + s_lng + " AT " + s_d.getDate() + "/" + s_d.getMonth() + "/" + 
				s_d.getYear() + " - " + s_d.getHours() + ":" + s_d.getMinutes() + ":" + s_d.getSeconds() + ", End  " + e_lat + "," + e_lng + " AT " +  
				e_d.getDate() + "/" + e_d.getMonth() + "/" + 
				e_d.getYear() + " - " + e_d.getHours() + ":" + e_d.getMinutes() + ":" + e_d.getSeconds() + ", day " + e_d.getDay());
				
				int d = s_d.getDate();
				if(mR.get(d) == null){
					days.add(d);
					mR.put(d, 1);
				}else{
					mR.put(d, mR.get(d) + 1);
				}
				if(mTaxiDay.get(d) == null){
					mTaxiDay.put(d, new HashSet<Integer>());
				}else{
					mTaxiDay.get(d).add(taxiID);
				}
			}
			for(int i = 0; i < days.size(); i++){
				int d = days.get(i);
				System.out.println("day " + d + ", has " + mR.get(d) + " requests, nbTaxis = " + mTaxiDay.get(d).size());
			}
			double t = (System.currentTimeMillis() - t0)*0.001;
			System.out.println("taxis = " + taxis.size() + ", time = " + t);
			in.close();
			
			System.out.println("Request --> min_lat = " + min_lat + ", max_lat = " + max_lat + ", min_lng = " + min_lng + ", max_lng = " + max_lng);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public ArrayList<PeopleRequest> loadPeopleRequests(String fn){
		ArrayList<PeopleRequest> allPeopleRequests = new ArrayList<PeopleRequest>();
		try{
			Scanner in = new Scanner(new File(fn));
			
			
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
				//mPeopleRequest.put(pr.id, pr);
			}
				in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		preprocessTimeCallPeopleRequest(allPeopleRequests);
		return allPeopleRequests;
	}

	public void extractRequest(String fn, String dir, double threshold, int delta_time_call, 
			int delta_pickup_late, int delta_delivery_late, double maxTravelDistanceFactor, 
			double minDistanceRequest, int maxNbStops){
		try{
			DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap(M.V, M.A);
			
			double t0 = System.currentTimeMillis();
			double min_lat = 100000;
			double max_lat = -min_lat;
			double min_lng = 1000000;
			double max_lng = -min_lng;
			double minDis = 100000000;
			double maxDis = -minDis;
			Scanner in = new Scanner(new File(fn));
			int lines = 0;
			HashSet<Integer> taxis = new HashSet<Integer>();
			HashMap<Integer, ArrayList<PeopleRequestSanFrancisco>> mR = new HashMap<Integer, ArrayList<PeopleRequestSanFrancisco>>();
			ArrayList<Integer> days = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Integer>> mTaxiDay = new HashMap<Integer, HashSet<Integer>>();
			int id = 0;
			while(in.hasNext()){
				int taxiID = in.nextInt();
				lines++;
				//if(lines > 5000) break;
				//System.out.println("Line "+ lines + ", taxiID = "+ taxiID);
				if(taxiID == 0) break;
				taxis.add(taxiID);
				double s_t = in.nextDouble();
				Date s_d = new Date((long)s_t*1000);
				double s_lat = in.nextDouble();
				double s_lng = in.nextDouble();
				double e_t = in.nextDouble();
				Date e_d = new Date((long)e_t*1000);
				double e_lat = in.nextDouble();
				double e_lng = in.nextDouble();
				
				/*
				min_lat = min_lat < s_lat ? min_lat : s_lat;
				min_lat = min_lat < e_lat ? min_lat : e_lat;
				max_lat = max_lat > s_lat ? max_lat : s_lat;
				max_lat = max_lat > e_lat ? max_lat : e_lat;
				
				min_lng = min_lng < s_lng ? min_lng : s_lng;
				min_lng = min_lng < e_lng ? min_lng : e_lng;
				max_lng = max_lng > s_lng ? max_lng : s_lng;
				max_lng = max_lng > e_lng ? max_lng : e_lng;
				*/
				
				//System.out.println("Line " + lines + ", taxi " + taxiID + ", Start " + s_lat + "," + s_lng + " AT " + s_d.getDate() + "/" + s_d.getMonth() + "/" + 
				//s_d.getYear() + " - " + s_d.getHours() + ":" + s_d.getMinutes() + ":" + s_d.getSeconds() + ", End  " + e_lat + "," + e_lng + " AT " +  
				//e_d.getDate() + "/" + e_d.getMonth() + "/" + 
				//e_d.getYear() + " - " + e_d.getHours() + ":" + e_d.getMinutes() + ":" + e_d.getSeconds() + ", day " + e_d.getDay());
				
				int d = s_d.getDate();
				if(mR.get(d) == null){
					days.add(d);
					mR.put(d, new ArrayList<PeopleRequestSanFrancisco>());
				}else{
					//mR.put(d, mR.get(d) + 1);
					int pickupLocationID = M.findNearestPoint(s_lat, s_lng, threshold);
					if(pickupLocationID < 0) continue;
					int deliveryLocationID = M.findNearestPoint(e_lat, e_lng, threshold);
					if(deliveryLocationID < 0) continue;
					int p_t = TimeHorizon.hms2Int(s_d.getHours(), s_d.getMinutes(), s_d.getSeconds());
					int d_t = TimeHorizon.hms2Int(e_d.getHours(), e_d.getMinutes(), e_d.getSeconds());
					int timeCall = p_t - delta_time_call;// 10 minutes
					int earlyPickupTime = timeCall;
					int latePickupTime = p_t + delta_pickup_late;
					int earlyDeliveryTime = d_t;
					int lateDeliveryTime = d_t + delta_delivery_late;
					double dis = dijkstra.queryDistance(pickupLocationID, deliveryLocationID);
					if(dis < minDistanceRequest) continue;
					minDis = minDis < dis ? minDis : dis;
					maxDis = maxDis > dis ? maxDis : dis;
					
					min_lat = min_lat < s_lat ? min_lat : s_lat;
					min_lat = min_lat < e_lat ? min_lat : e_lat;
					max_lat = max_lat > s_lat ? max_lat : s_lat;
					max_lat = max_lat > e_lat ? max_lat : e_lat;
					
					min_lng = min_lng < s_lng ? min_lng : s_lng;
					min_lng = min_lng < e_lng ? min_lng : e_lng;
					max_lng = max_lng > s_lng ? max_lng : s_lng;
					max_lng = max_lng > e_lng ? max_lng : e_lng;
					
					double ti = System.currentTimeMillis() - t0;
					ti = ti*0.001;
					System.out.println("Line " + lines + ", days.sz = "+ days.size() + 
							", distance = " + dis + ", minDis = " + minDis + 
							", maxDis = " + maxDis + ", time = " + ti);
					id++;
					
					mR.get(d).add(new PeopleRequestSanFrancisco(id,timeCall,pickupLocationID,
							deliveryLocationID,earlyPickupTime, latePickupTime,earlyDeliveryTime,
							lateDeliveryTime,dis*maxTravelDistanceFactor,maxNbStops));
					
				}
				if(mTaxiDay.get(d) == null){
					mTaxiDay.put(d, new HashSet<Integer>());
				}else{
					mTaxiDay.get(d).add(taxiID);
				}
			}
			for(int i = 0; i < days.size(); i++){
				int d = days.get(i);
				try{
					String fo = dir + "\\" + "request_day_" + d + ".txt";
					PrintWriter out = new PrintWriter(fo);
					out.println("r.id  r.timeCall  r.pickupLocationID " +  
							"r.deliveryLocationID  r.earlyPickupTime  r.latePickupTime " +
							" r.earlyDeliveryTime  r.lateDeliveryTime " + 
							" r.maxTravelDistance  r.maxNbStops");
					for(int j = 0; j < mR.get(d).size(); j++){
						PeopleRequestSanFrancisco r = mR.get(d).get(j);
						out.println(r.id + " " + r.timeCall + " " + r.pickupLocationID + " " + 
						r.deliveryLocationID + " " + r.earlyPickupTime + " " + r.latePickupTime +
						" " + r.earlyDeliveryTime + " " + r.lateDeliveryTime + " " + 
						r.maxTravelDistance + " " + r.maxNbStops);
					}
					out.println(-1);
					out.close();
					System.out.println("day " + d + ", has " + mR.get(d).size() + " requests, nbTaxis = " + mTaxiDay.get(d).size());
			
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			double t = (System.currentTimeMillis() - t0)*0.001;
			System.out.println("taxis = " + taxis.size() + ", time = " + t);
			in.close();
			
			System.out.println("Request --> min_lat = " + min_lat + ", max_lat = " + max_lat + ", min_lng = " + min_lng + ", max_lng = " + max_lng);
		}catch(Exception ex){
			ex.printStackTrace();
		}
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
	
	public void genParcelRequestFromPeopleRequest(String fReq, String newFReq, int endParlceRequestTime){
		Simulator sim = new Simulator();
		System.out.println("RequestSanFrancisco::genParcelRequestFromPeopleRequest, fReq = "+ fReq);
		ArrayList<PeopleRequest> allpeopleReq = loadPeopleRequests(fReq);
		System.out.println("RequestSanFrancisco::genParcelRequestFromPeopleRequest --> AllPeopleRequest.sz = " + allpeopleReq.size());
		
		ArrayList<ParcelRequest> parcelReq = new ArrayList<ParcelRequest>();
		ArrayList<PeopleRequest> peopleReq = new ArrayList<PeopleRequest>();
		
		for(int i = 0; i < allpeopleReq.size(); i++){
			PeopleRequest pr = allpeopleReq.get(i);
			if(i%2==0){
				
				ParcelRequest par = new ParcelRequest();
				par.id = pr.id;
				par.timePoint = pr.timePoint;
				par.earlyPickupTime = pr.timePoint;
				par.latePickupTime = endParlceRequestTime;
				par.earlyDeliveryTime = pr.timePoint;
				par.lateDeliveryTime = endParlceRequestTime;
				par.pickupLocationID = pr.pickupLocationID;
				par.deliveryLocationID = pr.deliveryLocationID;		
				
				parcelReq.add(par);
			}else{
				peopleReq.add(pr);
			}
		}
		try{
			PrintWriter out = new PrintWriter(newFReq);
			out.println("r.id  r.timeCall  r.pickupLocationID r.deliveryLocationID  r.earlyPickupTime  r.latePickupTime  r.earlyDeliveryTime  r.lateDeliveryTime  r.maxTravelDistance  r.maxNbStops");
			for(int i = 0; i < peopleReq.size(); i++){
				PeopleRequest pr = peopleReq.get(i);
				out.println(pr.id + " " + pr.timePoint + " " + pr.pickupLocationID + " " + 
				pr.deliveryLocationID + " " + pr.earlyPickupTime + " " + pr.latePickupTime + " " +
				pr.earlyDeliveryTime + " " + pr.lateDeliveryTime + " " + pr.maxTravelDistance + " " + pr.maxNbStops);
			}
			out.println(-1);
			
			out.println("r.id  r.timeCall  r.pickupLocationID r.deliveryLocationID  r.earlyPickupTime  r.latePickupTime  r.earlyDeliveryTime  r.lateDeliveryTime");
			for(int i = 0; i < parcelReq.size(); i++){
				ParcelRequest pr = parcelReq.get(i);
				out.println(pr.id + " " + pr.timePoint + " " + pr.pickupLocationID + " " + 
				pr.deliveryLocationID + " " + pr.earlyPickupTime + " " + pr.latePickupTime + " " +
				pr.earlyDeliveryTime + " " + pr.lateDeliveryTime);
			}
			out.println(-1);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void extractRequest(){
		int threshold = 100;// 100m
		int delta_time_call = 600;// 10 minutes
		int delta_pickup_late = 300;// 5 m inutes
		int delta_delivery_late = 1800;//30 minutes
		double maxTravelDistanceFactor = 2;
		int maxNbStops = 5;
		double minDistanceRequestAccepted = 1000;// extract only request having distance >= 1000
		extractRequest("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\2010_03.trips",
				"C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data",
				threshold,delta_time_call,delta_pickup_late,delta_delivery_late,maxTravelDistanceFactor,
				minDistanceRequestAccepted,maxNbStops);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//pbts.simulation.RoadMap map = new pbts.simulation.RoadMap();
		//map.loadData("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco-contracted.txt");
	
		RequestSanFrancisco RS = new RequestSanFrancisco();
		String dir = "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco\\";
		RS.loadMap(dir + "SanFranciscoRoad-connected-contracted-5.txt");
		//RS.analyze("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\2010_03.trips");
		
		//RS.extractRequest();
		
		RS.genParcelRequestFromPeopleRequest(dir + "request_day_1.txt", dir + "request_people_parcel_day_1.txt",3600*18);
	}

}

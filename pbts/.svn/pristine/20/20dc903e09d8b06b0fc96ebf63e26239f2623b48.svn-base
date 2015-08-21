package pbts.simulation;

import pbts.entities.*;

import java.util.HashMap;
import java.util.Random;
import java.io.*;
import java.util.*;
public class EventGenerator {

	/**
	 * @param args
	 */
	private RoadMap map;
	private TimeHorizon T;
	private Random R = null;
	public PrintWriter log = null;
	public int r_id;
	public int probPeople;
	public int probParcel;
	HashMap<Integer, Boolean> used;
	Simulator sim;
	WindowManager WM;
	
	public EventGenerator(Simulator sim, RoadMap map, TimeHorizon T, int probPeople, int probParcel){
		this.map  = map;
		this.T = T;
		R = new Random();
		r_id = 0;
		this.probPeople = probPeople;
		this.probParcel = probParcel;
		this.sim = sim;
		
		used = new HashMap<Integer, Boolean>();
		for(int i = 0; i < sim.lstDepots.size(); i++){
			int v = sim.lstDepots.get(i);
			used.put(v, true);
		}
		for(int i = 0; i < sim.lstParkings.size(); i++){
			Parking p = sim.lstParkings.get(i);
			used.put(p.locationID, true);
		}
		WM = new WindowManager(map);
		
		WM.generateWindows(5, 5);
	}
	public AbstractEvent nextEvent(){
		AbstractEvent pr = null;
		if(T.stopRequest()) return null;
		if(T.currentTimePoint%60 == 0){
			int x = R.nextInt()%probPeople;
			//x = 0;
			if(x == 0){
				r_id++;
				int pickupLocation = map.V.get(R.nextInt(map.V.size()));
				int deliveryLocation = map.V.get(R.nextInt(map.V.size()));
				pr = new PeopleRequest(pickupLocation, deliveryLocation);
				pr.id = r_id;
				log.println("EventGenerator::nextEvent --> At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + " --> people request from " + pickupLocation + " to " + deliveryLocation + " arrive");
				return pr;
			}
		//}
		//if(T.currentTimePoint%60 == 0){
			x = R.nextInt()%probParcel;
			if(x == 0){
				r_id++;
				int pickupLocation = map.V.get(R.nextInt(map.V.size()));
				int deliveryLocation = map.V.get(R.nextInt(map.V.size()));
				pr = new ParcelRequest(pickupLocation, deliveryLocation);
				pr.id = r_id;
				log.println("EventGenerator::nextEvent --> At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + " --> parcel request from " + pickupLocation + " to " + deliveryLocation + " arrive");
				return pr;
			}
		}
		return pr;
	}
	public PeopleRequest getPeopleRequest(){
		
		if(T.stopRequest()) return null;
		if(T.currentTimePoint%60 == 0){
			int x = R.nextInt()%probPeople;
			//x = 0;
			if(x == 0){
				r_id++;
				int pickupLocation = -1;//map.V.get(R.nextInt(map.V.size()));
				while(true){
					pickupLocation = map.V.get(R.nextInt(map.V.size()));
					if(used.get(pickupLocation) == null) break;
					System.out.println("EventGenerator::getPeopleRequest repeat.....");
				}
				used.put(pickupLocation, true);
				
				int deliveryLocation = -1;//map.V.get(R.nextInt(map.V.size()));
				while(true){
					deliveryLocation = map.V.get(R.nextInt(map.V.size()));
					if(used.get(deliveryLocation) == null) break;
					System.out.println("EventGenerator::getPeopleRequest repeat.....");
				}
				used.put(deliveryLocation, true);
				
				PeopleRequest pr = new PeopleRequest(pickupLocation, deliveryLocation);
				pr.id = r_id;
				log.println("EventGenerator::nextEvent --> At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + " --> people request from " + pickupLocation + " to " + deliveryLocation + " arrive");
				return pr;
			}
		}
		return null;
	}
	public PeopleRequest getPeopleRequestLongDistance(){
		
		if(T.stopRequest()) return null;
		if(T.currentTimePoint%60 == 0){
			int x = R.nextInt()%probPeople;
			//x = 0;
			if(x == 0){
				r_id++;
				int pickupLocation = -1;//map.V.get(R.nextInt(map.V.size()));
				Window w = WM.notEmptyWindows.get(WM.notEmptyWindows.size()-1);
				Window wd = WM.farest.get(w);
				ArrayList<Integer> P = WM.lstPoints.get(w);
				ArrayList<Integer> PD = WM.lstPoints.get(wd);
				while(true){
					pickupLocation = P.get(R.nextInt(P.size()));////map.V.get(R.nextInt(map.V.size()));
					//if(used.get(pickupLocation) == null) 
						break;
					//System.out.println("EventGenerator::getPeopleRequest repeat.....");
				}
				used.put(pickupLocation, true);
				
				int deliveryLocation = -1;//map.V.get(R.nextInt(map.V.size()));
				while(true){
					deliveryLocation = PD.get(R.nextInt(PD.size()));//map.V.get(R.nextInt(map.V.size()));
					//if(used.get(deliveryLocation) == null) 
						break;
					//System.out.println("EventGenerator::getPeopleRequest repeat.....");
				}
				used.put(deliveryLocation, true);
				
				PeopleRequest pr = new PeopleRequest(pickupLocation, deliveryLocation);
				pr.id = r_id;
				log.println("EventGenerator::nextEvent --> At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + " --> people request from " + pickupLocation + " to " + deliveryLocation + " arrive");
				return pr;
			}
		}
		return null;
	}
	public ParcelRequest getParcelRequestLongDistance(){
		if(T.stopRequest()) return null;
		if(T.currentTimePoint%60 == 0){
			int x = R.nextInt()%probParcel;
			if(x == 0){
				r_id++;
				Window w = WM.notEmptyWindows.get(WM.notEmptyWindows.size()-1);
				Window wd = WM.farest.get(w);
				ArrayList<Integer> P = WM.lstPoints.get(w);
				ArrayList<Integer> PD = WM.lstPoints.get(wd);
				
				int pickupLocation = -1;//map.V.get(R.nextInt(map.V.size()));
				while(true){
					pickupLocation = P.get(R.nextInt(P.size()));//map.V.get(R.nextInt(map.V.size()));
					//if(used.get(pickupLocation) == null) 
						break;
					//System.out.println("EventGenerator::getParcelRequest repeat.....");
				}
				used.put(pickupLocation, true);
				
				int deliveryLocation = PD.get(R.nextInt(PD.size()));//map.V.get(R.nextInt(map.V.size()));
				while(true){
					deliveryLocation = PD.get(R.nextInt(PD.size()));//map.V.get(R.nextInt(map.V.size()));
					//if(used.get(deliveryLocation) == null) 
						break;
					//System.out.println("EventGenerator::getParcelRequest repeat.....");
				}
				used.put(deliveryLocation, true);
				
				ParcelRequest pr = new ParcelRequest(pickupLocation, deliveryLocation);
				pr.id = r_id;
				log.println("EventGenerator::nextEvent --> At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + " --> parcel request from " + pickupLocation + " to " + deliveryLocation + " arrive");
				return pr;
			}
		}
		return null;
	}

	public ParcelRequest getParcelRequest(){
		if(T.stopRequest()) return null;
		if(T.currentTimePoint%60 == 0){
			int x = R.nextInt()%probParcel;
			if(x == 0){
				r_id++;
				int pickupLocation = -1;//map.V.get(R.nextInt(map.V.size()));
				while(true){
					pickupLocation = map.V.get(R.nextInt(map.V.size()));
					if(used.get(pickupLocation) == null) break;
					System.out.println("EventGenerator::getParcelRequest repeat.....");
				}
				used.put(pickupLocation, true);
				
				int deliveryLocation = map.V.get(R.nextInt(map.V.size()));
				while(true){
					deliveryLocation = map.V.get(R.nextInt(map.V.size()));
					if(used.get(deliveryLocation) == null) break;
					System.out.println("EventGenerator::getParcelRequest repeat.....");
				}
				used.put(deliveryLocation, true);
				
				ParcelRequest pr = new ParcelRequest(pickupLocation, deliveryLocation);
				pr.id = r_id;
				log.println("EventGenerator::nextEvent --> At " + T.currentTimePointHMS() + "[" + T.currentTimePoint + "]" + " --> parcel request from " + pickupLocation + " to " + deliveryLocation + " arrive");
				return pr;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

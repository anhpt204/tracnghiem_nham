package pbts.entities;
import java.util.*;
public class Parking {

	/**
	 * @param args
	 */
	public int locationID;
	public int capacity;
	public int load;
	public int lastUpdateTimePoint;
	
	public ArrayList<TaxiArrivalDepartureTime> taxisParking;
	
	public Parking(int loc, int cap){
		this.locationID = loc;
		this.capacity = cap;
		this.load = 0;
	}
	
	public void admitTaxiParking(int taxiID, int arrivalTimePoint, int departureTimePoint){
		taxisParking.add(new TaxiArrivalDepartureTime(taxiID, arrivalTimePoint, departureTimePoint));
	}
	public void removeTaxiParking(int taxiID, int arrivalTimePoint, int departureTimePoint){
		int idx = -1;
		for(int i = 0; i < taxisParking.size(); i++){
			TaxiArrivalDepartureTime tad = taxisParking.get(i);
			if(tad.taxiID == taxiID && tad.arrivalTimePoint == arrivalTimePoint && tad.departureTimePoint == departureTimePoint){
				idx = i;
				break;
			}
		}
		if(idx != -1)
			taxisParking.remove(idx);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

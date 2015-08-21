package pbts.entities;

public class TaxiArrivalDepartureTime {
	public int taxiID;
	public int arrivalTimePoint;
	public int departureTimePoint;
	
	public TaxiArrivalDepartureTime(int taxiID, int arrivalTimePoint, int departureTimePoint){
		this.taxiID = taxiID;
		this.arrivalTimePoint = arrivalTimePoint;
		this.departureTimePoint = departureTimePoint;
	}
}

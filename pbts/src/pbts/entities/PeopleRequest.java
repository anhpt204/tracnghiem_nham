package pbts.entities;

public class PeopleRequest extends AbstractEvent {

	/**
	 * @param args
	 */
	public int pickupLocationID;
	public int deliveryLocationID;
	public int earlyPickupTime;
	public int latePickupTime;
	public int earlyDeliveryTime;
	public int lateDeliveryTime;
	public int maxNbStops;
	public double maxTravelDistance;
	public int pickupDuration = 30;// seconds
	public int deliveryDuration = 30;// seconds
	
	public PeopleRequest(int pickupLocationID, int deliveryLocationID){
		this.pickupLocationID = pickupLocationID;
		this.deliveryLocationID = deliveryLocationID;
	}
	public String toString(){
		String  s = "";
		s = "parcel id = " + id + ", time = " + timePoint;
		return s;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

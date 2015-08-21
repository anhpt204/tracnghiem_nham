package pbts.entities;

public class ParcelRequest extends AbstractEvent{

	/**
	 * @param args
	 */
	public int pickupLocationID;
	public int deliveryLocationID;
	public int earlyPickupTime;
	public int latePickupTime;
	public int earlyDeliveryTime;
	public int lateDeliveryTime;
	public int pickupDuration = 30;// seconds
	public int deliveryDuration = 30;// seconds
	
	public ParcelRequest(int pickupLocationID, int deliveryLocationID){
		this.pickupLocationID = pickupLocationID;
		this.deliveryLocationID = deliveryLocationID;
	}
	public ParcelRequest(){
		
	}
	public String toString(){
		String  s = "";
		s = "(id = " + id + ", timeCall = " + timePoint + ", earlyPickupTime = " + earlyPickupTime + 
				", latePickupTime = "+ latePickupTime + ", earlyDeliveryTime = " + earlyDeliveryTime + 
				", lateDeliveryTime = " + lateDeliveryTime + ")";
		return s;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

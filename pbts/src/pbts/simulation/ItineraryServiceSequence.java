package pbts.simulation;
import pbts.entities.*;

public class ItineraryServiceSequence {
	public Vehicle taxi;
	public ItineraryTravelTime I;
	public ServiceSequence ss;
	public ItineraryServiceSequence(Vehicle taxi, ItineraryTravelTime I, ServiceSequence ss){
		this.taxi = taxi;
		this.I = I;
		this.ss = ss;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

package pbts.simulation;

public class AnalysisTemplate {

	/**
	 * @param args
	 */
	public double travelDistance;
	public double fuelCost;
	public double discount;
	public double revenuePassengers;
	public double revenueParcels;
	public double benefits;
	public int nbServedPassengers;
	public int nbServedParcels;
	public int nbSharedPeopleService;// number of people request having sharing ride
	
	public AnalysisTemplate(){
		this.travelDistance = 0;
		this.fuelCost = 0;
		this.discount = 0;
		this.revenueParcels = 0;
		this.revenuePassengers = 0;
		this.benefits = 0;
		this.nbServedParcels = 0;
		this.nbServedPassengers = 0;
		this.nbSharedPeopleService = 0;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

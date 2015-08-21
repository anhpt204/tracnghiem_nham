package pbts.simulation;

public class ServiceSequence {

	/**
	 * @param args
	 */
	
	public int[] rids;// id of request: '+' means pickup, '-' means delivery
	public double profitEvaluation;
	public int parkingLocationPoint;
	public double distance;
	public ServiceSequence(int[] rids, double profitEvaluation, int parkingLocationPoint, double distance){
		this.rids = new int[rids.length];
		for(int i = 0; i < rids.length; i++)
			this.rids[i] = rids[i];
		this.profitEvaluation = profitEvaluation;
		this.parkingLocationPoint = parkingLocationPoint;
		this.distance = distance;
	}
	public String getSequence(){
		String s = "";
		for(int i = 0; i < rids.length; i++)
			s = s + rids[i] + ",";
		return s;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

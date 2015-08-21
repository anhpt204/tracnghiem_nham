package pbts.entities;

public class Arc{
	
	public Arc(int begin, int end, double w){
		this.begin = begin;
		this.end = end;
		this.w = w;
	}
	
	public int begin, end;
	public double w;
	public int t;// travel time from u to v
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

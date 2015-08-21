package pbts.gismap;

public class Point {

	/**
	 * @param args
	 */
	private double dLat; //lat
    private double dLong;//long    
    
    public Point(double dlat, double dlong){
        this.dLat = dlat;
        this.dLong = dlong;
    }
    public boolean equals(Point p){
    	return Utility.equals(dLat, p.getdLat()) && Utility.equals(dLong, p.getdLong());
    }
    public String toString(){
    	return "(" + this.dLat + "," + this.dLong + ")";
    }
    /**
     * @return the dLat
     */
    public double getdLat() {
        return dLat;
    }

    /**
     * @param dLat the dLat to set
     */
    public void setdLat(double dLat) {
        this.dLat = dLat;
    }

    /**
     * @return the dLong
     */
    public double getdLong() {
        return dLong;
    }

    /**
     * @param dLong the dLong to set
     */
    public void setdLong(double dLong) {
        this.dLong = dLong;
    }

    public void setCoordinate(double dLong, double dLat){
    	this.dLong = dLong;
    	this.dLat = dLat;
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

package pbts.gismap;

public class Line {

	/**
	 * @param args
	 */
	
	// a*x + b*y + c = 0
	private double a;
	private double b;
	private double c;
	
	private double _x1;
	private double _y1;
	private double _x2;
	private double _y2;
	
	public Line(double x1, double y1, double x2, double y2){
		_x1 = x1;
		_y1 = y1;
		_x2 = x2;
		_y2 = y2;
		compute(x1,y1,x2,y2);
	}
	public double getA(){ return this.a;}
	public double getB(){ return this.b;}
	public double getC(){ return this.c;}
	public double getX1(){ return this._x1;}
	public double getX2(){ return this._x2;}
	public double getY1(){ return this._y1;}
	public double getY2(){ return this._y2;}
	public void compute(double x1, double y1, double x2, double y2){
		if(Utility.equals(x1, x2) && Utility.equals(y1, y2)){
			System.out.println("Line::compute(" + x1 + "," + y1 + "," + x2 + "," + y2 + ") --> exception" + 
					"one point cannot specify a line ?????????");
			return;
		}
		
		if(Utility.equals(x1, x2)){
			a = 1; b = 0; c = -x1;
		}else if(Utility.equals(y1, y2)){
			a = 0; b = 1; c = -y1;
		}else{
			a = y1 - y2;
			b = x2 - x1;
			c = y1*(x1-x2) - x1*(y1-y2);
		}
		_x1 = x1; _y1 = y1; _x2 = x2; _y2 = y2;
	}
	
	public void compute(double x0, double y0, double angle){
		//TODO
		if (Utility.equals(angle, 90)) {
            a = 1;
            b = 0;
            c = -x0;
        } else if (Utility.equals(angle, 0)) {
            a = 0;
            b = 1;
            c = -y0;
        } else {
            double k = Math.tan(angle * Math.PI / 180);
            c = y0 - k * x0;
            a = k;
            b = -1;
        }
	}
	
	public boolean lineContains(double x1, double y1){
		//TODO
		double v = a * x1 + b * y1 + c;
        return Utility.equals(v, 0);
	}
	public boolean segmentContains(double x1, double y1){
		//TODO
		double v = a * x1 + b * y1 + c;
        boolean ok = Utility.equals(v, 0);
        if (!ok) {
            return false;
        }
        double MINX1X2 = Math.min(_x1, _x2);
        double MAXX1X2 = Math.max(_x1, _x2);
        double MINY1Y2 = Math.min(_y1, _y2);
        double MAXY1Y2 = Math.max(_y1, _y2);

        ok = MINX1X2 <= x1 && x1 <= MAXX1X2 && MINY1Y2 <= y1 && y1 <= MAXY1Y2;

        return ok;
	}
	public boolean segmentStronglyContains(double x1, double y1){
		//TODO
		double v = a * x1 + b * y1 + c;
        boolean ok = Utility.equals(v, 0);
        if (!ok) {
            return false;
        }
        double MINX1X2 = Math.min(_x1, _x2);
        double MAXX1X2 = Math.max(_x1, _x2);
        double MINY1Y2 = Math.min(_y1, _y2);
        double MAXY1Y2 = Math.max(_y1, _y2);

        ok = MINX1X2 < x1 && x1 < MAXX1X2 || MINY1Y2 < y1 && y1 < MAXY1Y2;

        return ok;
	}
	
	public double getAngle(){
		if(Utility.equals(a, 0))
			return 0.0;
		else if(Utility.equals(b, 0))
			return 90.0;
		else
			return Math.atan(-a/b)*180/Utility.PI;
	}
	
	public TWO_LINES_RELATION intersectLine(Line l, Point p){
		double a1 = getA();
		double b1 = getB();
		double c1 = getC();
		double a2 = l.getA();
		double b2 = l.getB();
		double c2 = l.getC();
		
		if(Utility.equals(getAngle(), l.getAngle())){
			if(Utility.equals(a1, 0)){
				if(Utility.equals(c1/b1,c2/b2)){
					return TWO_LINES_RELATION.IDENTICAL;
				}else{
					return TWO_LINES_RELATION.PARALLEL;
				}
			}else if(Utility.equals(b1,0)){
				if(Utility.equals(c1/a1, c2/a2)){
					return TWO_LINES_RELATION.IDENTICAL;
				}else{
					return TWO_LINES_RELATION.PARALLEL;
				}
			}else{
				if(Utility.equals(c1/b1, c2/b2)){
					return TWO_LINES_RELATION.IDENTICAL;
				}else{
					return TWO_LINES_RELATION.PARALLEL;
				}
			}
		}else{
			double y = (a2*c1-a1*c2)/(a1*b2-a2*b1);
			double x = (b1*c2-c1*b2)/(a1*b2-a2*b1);
			p.setCoordinate(x, y);
			
			return TWO_LINES_RELATION.INTERSECTIONAL;
		}
	}
	
	TWO_SEGMENTS_RELATION intersectSegment(Line l, Point p){
		TWO_LINES_RELATION R = intersectLine(l, p);
		if(R == TWO_LINES_RELATION.PARALLEL){
			return TWO_SEGMENTS_RELATION.SEGMENT_NOT_INTERSECTIONAL;
		}else if(R == TWO_LINES_RELATION.IDENTICAL){
			double x1 = l.getX1();
			double y1 = l.getY1();
			double x2 = l.getX2();
			double y2 = l.getY2();
			if(segmentContains(x1, y1) || segmentContains(x2, y2)){
				return TWO_SEGMENTS_RELATION.SEGMENT_IDENTICAL;
			}else{
				return TWO_SEGMENTS_RELATION.SEGMENT_NOT_INTERSECTIONAL;
			}
		}else{
			double x = p.getdLong();
			double y = p.getdLat();
			boolean ok = segmentContains(x, y) && l.segmentContains(x, y);
			if(ok) return TWO_SEGMENTS_RELATION.SEGMENT_INTERSECTIONAL;
			else return TWO_SEGMENTS_RELATION.SEGMENT_NOT_INTERSECTIONAL;
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

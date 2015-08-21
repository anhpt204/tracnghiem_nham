package ktuan;

public class Parking {
	public Position _pos;
	private int[] _time;
	private int _capacity;
	private int _T;
	public int _id;
	public Parking(Position pos, int maxTime) {
		this._pos=pos;
		this._time= new int[maxTime+3];
		this._capacity= pos._Capacity;
		this._T= maxTime;
		this._id= pos._id;
	}
	public boolean checkCapacityForTime(int t1,int t2){
		for (int t=t1;t<=t2;t++) if (_time[t]>=this._capacity) return false;
		return true;
	}
	
	public int getTimeGotoFrom(int time) {
		if (time<=0) return -1;
		if (time>=this._T) return -1;
		while (time<=this._T&&_time[time]>=this._capacity) time++;
		if (time>=this._T) return -1;
		return time;
	}
	public int findArrivePark(int timeGotoPark, int segmentStartPark,
			int segmentFinishPark) {
		if (timeGotoPark==-1) return -1;
		int time=timeGotoPark;
		while (_time[time+1]<this._capacity && time<segmentFinishPark && (time+1)<=this._T) time++;
		if (time<segmentStartPark) return -1;
		return time;
	}
	
	public void addCapacity(int time1, int time2, int value) {
		for (int t=time1;t<=time2;t++){
			_time[t]+=value;
		}	
	}
	public void printfInfo() {
		System.out.println("ID = "+this._pos._id);
		for(int cs=1;cs<this._T;cs++) System.out.printf("[%2d::%1d]",cs,this._time[cs]);
		System.out.println();
		
	}
}

package ktuan;

public class EdgeMap {
	public int _id1;
	public int _id2;
	public double _distance;
	public int _minTime;
	public int _maxTime;
	public int _avgTime;
	public EdgeMap(int id1, int id2, double distance) {
		this._id1=id1;
		this._id2=id2;
		this._distance= distance;
	}
	public EdgeMap(EdgeMap edgeMap, boolean checkSwapId) {
		this._id1= edgeMap._id1;
		this._id2= edgeMap._id2;
		this._distance= edgeMap._distance;
		this._minTime= edgeMap._minTime;
		this._maxTime= edgeMap._maxTime;
		this._avgTime= edgeMap._avgTime;
		if (checkSwapId){
			int tmpId= _id1;
			_id1=_id2;
			_id2=tmpId;
		}
	}
	public String toString(){
		return String.format("Edge Map[%6d|%6d|%10.3f|minTime= %4d|maxTime= %4d|avgTime= %4d]", _id1, _id2, _distance,_minTime,_maxTime,_avgTime);
	}
	public void updateMinMaxTravelTime(SystemSARP _sys) {
		_minTime= (int)(_distance/_sys._maxSpeedms);
		if (_minTime*_sys._maxSpeedms+ParameterSARP.EPSILON< _distance) _minTime++;
		_maxTime= (int)(_distance/_sys._minSpeedms);
		_avgTime= (_minTime+_maxTime)/2;
	}
}

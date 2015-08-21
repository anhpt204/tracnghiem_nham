package ktuan;

public class Position {
	public static final int REQUEST_DELIVERY = 1;
	public static final int REQUEST_PICKUP = 0;
	public Position(int _id, double _lat, double _lng) {
		this._id=_id;
		this._lat= _lat;
		this._lng=_lng;
	}
	
	public Position() {
	}
	@Override
	public String toString() {
		return "Position [_id=" + _id + ", _early=" + _early + ", _late="
				+ _late + "]";
	}
	public int _id;
	double _lat;
	double _lng;
	public boolean _isInRequest;
	public int _early;
	public int _late;
	public int _PICKUP_OR_DELIVERY;
	public int _type_request;
	public boolean _isPassengerSrcId;
	public double _Di;
	public int _Ti;
	public double _MaxDi;
	public int _MaxTi;
	public boolean _isParking;
	public int _Capacity;
	public int _eta;
	public boolean _isDepotOfTaxis;
	public int _type_position;
	public int _idRequest;
	/*Co 3 truong hop xay ra:
	 * Doi voi request: La id cua request
	 * Doi voi parking: La id cua diem parking
	 * Doi voi taxi   : La so thu tu cua taxi
	 * */
}

package ktuan;

public class RequestTx {
	private static final int ooEta = 100000;
	public Position _posStart;
	public Position _posEnd;
	private int _eta;
	public double _valueGreedy20141201;

	public Position get_posStart() {
		return _posStart;
	}

	public void set_posStart(Position _posStart) {
		this._posStart = _posStart;
	}

	public RequestTx(Position posStart, Position posEnd) {
		this._posStart = posStart;
		this._posEnd = posEnd;
		if (this._posStart._isPassengerSrcId) {
			this._posEnd._type_request = -this._posEnd._type_request;
			this._eta = posStart._eta;
		} else
			this._eta = ooEta;
	}

	@Override
	public String toString() {
		return "RequestTx [_posStart=" + _posStart + ", _posEnd=" + _posEnd
				+ "]";
	}

	public void printfInfo() {
		System.out
				.printf("[Request: Start = %3d| End = %3d| Request = %3d| Eta = %3d]\n",
						_posStart._id, _posEnd._id, _posStart._type_request,
						_eta);
	}
}

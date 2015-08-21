package ktuan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class Data {

	public SystemSARP _sys;
	public VirtureGraph _grV;
	public ArrayList<SequenTaxi> _listTaxis;
	public ArrayList<RequestTx> _listRequest;
	private ArrayList<RequestTx> _listRequestFail;
	private double _dentaSystem;
	public double _sumProfit;
	private int[] listPost;
	private int[] timeGoto;
	private int[] timeArrive;
	private boolean _checkInsertRequest;
	private int[] saveListPost;
	private ArrayList<SequenTaxi> _listPositions;
	private int[] saveTimeGoto;
	private int[] saveTimeArrive;
	private double saveProfit;
	private HashMap<Integer, SequenTaxi> _hashMapTaxi;
	private Object[] _listObjectTaxiAndPark;

	public void updateSystemSARP(SystemSARP sys) {
		this._sys = sys;
	}

	public void initSequenTaxisAndParking() {
		System.out
				.println("Start: Khoi tao lo trinh khi chua co khach cho cac taxi.");
		_listObjectTaxiAndPark=new  Object[_grV._virtureId+2];
		_listTaxis = new ArrayList<SequenTaxi>();
		for (int cs = 0; cs < _grV._listDepotsTaxi.size(); cs++){
			_listTaxis.add(new SequenTaxi(_grV._listPositions
					.get(_grV._listDepotsTaxi.get(cs) - 1), _sys));
		}
		
		for(int cs=0;cs<_listTaxis.size();cs++){
			_listObjectTaxiAndPark[_listTaxis.get(cs)._listPost[0]]= _listTaxis.get(cs);
		}
		System.out.println("Finish: Printf list hashMapTaxi");
		/*
		for (int cs = 0; cs < _listTaxis.size(); cs++)
			_listTaxis.get(cs).printfInfo();
		System.out
				.println("Finish: Khoi tao lo trinh khi chua co khach cho cac taxi.");
		System.out
				.println("So luong taxi trong he thong: " + _listTaxis.size());
		*/
		for (int cs=0;cs<_grV._listParking.size();cs++){
			_listObjectTaxiAndPark[_grV._listParking.get(cs)._id]= _grV._listParking.get(cs);
		}
	}

	public void initCreateRequests() {
		System.out.println("Start: Tao danh sach cac request");
		_listRequest = new ArrayList<RequestTx>();
		for (int cs = 0; cs < _grV._nbPassenger + _grV._nbParcel; cs++) {
			_listRequest.add(new RequestTx(_grV._listPositions.get(cs),
					_grV._listPositions.get(cs + _grV._nbPassenger
							+ _grV._nbParcel)));
		}

		for (int cs = 0; cs < _grV._nbPassenger + _grV._nbParcel; cs++)
			_listRequest.get(cs).printfInfo();
		System.out.println("Sort");
		sortRequestFollowEarly();
		System.out.println("So luong request ma he thong nhan duoc: "
				+ _listRequest.size());
		System.out.println("Finish: Tao danh sach cac request");
	}

	private void sortRequestFollowEarly() {
		System.out.println("Process: Sort Request");
		Collections.sort(_listRequest, new ComparatorEarly());
	}

	public void updateGraphAndRequest(VirtureGraph grV) {
		this._grV = grV;
	}

	public void solve() {
		System.out.println("Start: Solve");
		_listRequestFail = new ArrayList<RequestTx>();
		while (_listRequest.size() > 0) {
			RequestTx rq = _listRequest.get(0);
			_listRequest.remove(0);
			_dentaSystem = -ParameterSARP.OO_Double;
			//int saveCsTaxi=-1;
			for (int csTaxi = 0; csTaxi <_listTaxis.size(); csTaxi++) {
				estimatesCreateProfit(_listTaxis.get(csTaxi), rq);
				/*
				if (_dentaSystem>0) {
					saveCsTaxi= csTaxi;
					break;
				}
				*/
			}
			if (_dentaSystem > 0) {
				System.out.println("Message: Chap nhan Request "
						+ rq.toString());
				updateSequenTaxi(rq);
				/*
				if (saveCsTaxi==_listTaxis.size()-1) continue;
				SequenTaxi tmp=_listTaxis.get(_listTaxis.size()-1);
				_listTaxis.set(_listTaxis.size()-1, _listTaxis.get(saveCsTaxi));
				_listTaxis.set(saveCsTaxi, tmp);
				*/
			} else {
				System.out.println("Message: Tu choi Request " + rq.toString());
				_listRequestFail.add(rq);
			}
		}
		printfListPostTaxis(true);
		System.out.printf("Sum Profit = %15.5f\n", _sumProfit);
		System.out.println("Finish: Solve");
	}

	public void printfListPostTaxis(boolean b) {
		_sumProfit = 0.0;
		for (int csTaxi = 0; csTaxi < _listTaxis.size(); csTaxi++) {
			_sumProfit += _listTaxis.get(csTaxi)._profit;
			if (_listTaxis.get(csTaxi)._profit > 0.0) {
				System.out.printf("Taxi %3d : %15.5f\n", csTaxi,
						_listTaxis.get(csTaxi)._profit);
				if (b)reacallProfitAndPrintfDetail(_listTaxis.get(csTaxi));
				_listTaxis.get(csTaxi).printfInfo();
			}
		}
	}

	private void reacallProfitAndPrintfDetail(SequenTaxi sequenTaxi) {
		listPost = new int[sequenTaxi._listPost.length];
		timeGoto = new int[sequenTaxi._listPost.length];
		timeArrive = new int[sequenTaxi._listPost.length];
		for (int cs = 0; cs < listPost.length; cs++) {
			listPost[cs] = sequenTaxi._listPost[cs];
			timeGoto[cs] = sequenTaxi._timeGoto[cs];
			timeArrive[cs] = sequenTaxi._timeArrive[cs];
		}
		double Profit= getProfitAndPrintfDetail(true);
		System.out.println(listPost[0]+"________"+listPost[1]);
		System.out.println(_grV._min_travel_time[listPost[0]][listPost[1]]);
		System.out.println(_grV._distance[listPost[0]][listPost[1]]);
		System.out.println("Profit detail: "+Profit);
	}

	private double getProfitAndPrintfDetail(boolean b) {
		return getProfit(b);
	}

	private void updateSequenTaxi(RequestTx rq) {
		for (int cs = 0; cs < 1; cs++) {
			//_listTaxis.size();
			/*
			int id = _listTaxis.get(cs)._listPost[0];
			if (id != saveListPost[0])
				continue;
			SequenTaxi taxi= _listTaxis.get(cs);
			*/
			//System.out.println(String.format("Taxi hashcode = %s", taxi.hashCode()));
			//System.out.println(String.format("Check hashcode= %s", ((SequenTaxi)_hashMapTaxi.get(saveListPost[0])).hashCode()));
			SequenTaxi taxi=(SequenTaxi)_listObjectTaxiAndPark[saveListPost[0]];
			//taxi=_hashMapTaxi.get(listPost[0]);
			taxi._listRequestServer.add(rq);
			//Loai bo hanh trinh cu cua taxi
			for (int csPos=0;csPos<taxi._listPost.length;csPos++){
				Position pos=_grV._listPositions.get(taxi._listPost[csPos]-1);
				if (pos._isParking){
					Parking par= (Parking)_listObjectTaxiAndPark[pos._id];
					par.addCapacity(taxi._timeGoto[csPos], taxi._timeArrive[csPos], -1);
					/*
					for (int csPark=0;csPark<_grV._listParking.size();csPark++){
						if (_grV._listParking.get(csPark)._id== pos._id){
							//System.out.println("pos._id ="+ pos._id);
							_grV._listParking.get(csPark).addCapacity(taxi._timeGoto[csPos], taxi._timeArrive[csPos], -1);
							break;
						}
					}
					*/
				}
			}
			taxi.update(saveListPost, saveTimeGoto,
					saveTimeArrive, saveProfit);
			for (int csPos=0;csPos<saveListPost.length;csPos++){
				Position pos= _grV._listPositions.get(saveListPost[csPos]-1);
				if (pos._isParking){
					for (int csPark=0;csPark<_grV._listParking.size();csPark++){
						if (_grV._listParking.get(csPark)._id==pos._id){
							_grV._listParking.get(csPark).addCapacity(saveTimeGoto[csPos], saveTimeArrive[csPos], 1);
							break;
						}
					}
				}
			}
		}
	}

	private void estimatesCreateProfit(SequenTaxi sequenTaxi,
			RequestTx requestTx) {
		//System.out.println("Taxi: "+sequenTaxi.hashCode()+"|| Request"+requestTx.hashCode());
		//System.out.println(requestTx.toString());
		int posId1 = requestTx._posStart._id;
		int posId2 = requestTx._posEnd._id;
		addCapacity(sequenTaxi,-1);
		listPost = new int[sequenTaxi._listPost.length + 2];
		timeGoto = new int[listPost.length];
		timeArrive = new int[listPost.length];
		for (int cs = 0; cs < listPost.length - 2; cs++) {
			listPost[cs] = sequenTaxi._listPost[cs];
		}
		listPost[listPost.length - 1] = sequenTaxi._listPost[listPost.length - 3];
		listPost[listPost.length - 3] = posId1;
		listPost[listPost.length - 2] = posId2;
		trimListPost();
		_checkInsertRequest = false;
		// ...........................................................
		for (int posStart = listPost.length - 3; posStart > 0; posStart--) {
			for (int posFinish = listPost.length - 2; posFinish > posStart; posFinish--) {
				if (checkCondition() == true) {
					double Profit = getProfit(false);
					double tmpDenta = Profit - sequenTaxi._profit;
					if (tmpDenta > _dentaSystem) {
						_dentaSystem = tmpDenta;
						saveState_listPost_timeGoto_timeArrive(Profit);
						//if (_dentaSystem>0) return;
					}
				}
				trimListPost();
				swapArrayInt(listPost, posFinish, posFinish - 1);
			}
			swapArrayInt(listPost, posStart, posStart + 1);
			swapArrayInt(listPost, posStart, posStart - 1);
		}
		addCapacity(sequenTaxi, 1);
	}

	private void swapArrayInt(int[] list, int posOne, int posTwo) {
		int value= list[posOne];
		list[posOne]= list[posTwo];
		list[posTwo]= value;
	}

	private void saveState_listPost_timeGoto_timeArrive(double profit) {
		System.out.println("Process::Saving state for best Solution finded!!!"+ profit);
		saveListPost = new int[listPost.length];
		saveTimeGoto = new int[listPost.length];
		saveTimeArrive = new int[listPost.length];
		saveProfit = profit;
		for (int cs = 0; cs < listPost.length; cs++) {
			saveListPost[cs] = listPost[cs];
			saveTimeGoto[cs] = timeGoto[cs];
			saveTimeArrive[cs] = timeArrive[cs];
		}
	}

	private double getProfit(boolean _checkPrint) {
		double profit = 0.0;
		for (int cs = 1; cs < listPost.length - 1; cs++) {
			Position pos = _grV._listPositions.get(listPost[cs] - 1);
			int startId = pos._id;
			int finishId = startId + _grV._nbPassenger + _grV._nbParcel;
			if (!pos._isPassengerSrcId) continue;
			//System.out.println("StartId= :"+startId);
			//System.out.println("FinishId= :"+finishId);
			if (startId>_grV._nbPassenger+_grV._nbParcel) continue;
			if (pos._isInRequest) {
				if (pos._type_request == ParameterSARP.TYPE_REQUEST_PEOPLE) {
					profit =profit +_sys._alpha + (double)_sys._gamma1 
							* _grV._distance[startId][finishId];
					if (_checkPrint){
						System.out.println(String.format("+ %10.4f | D= %10.4f| Alpha= %8d | gamma1= %8d", 
								_sys._alpha + (double)_sys._gamma1* _grV._distance[startId][finishId],
								_grV._distance[startId][finishId],
								_sys._alpha,
								_sys._gamma1));
					}
				} else if (pos._type_request == ParameterSARP.TYPE_REQUEST_PARCEL) {
					profit =profit+ _sys._beta + (double)_sys._gamma2
							* _grV._distance[startId][finishId];
					if (_checkPrint){
						System.out.println(String.format("+ %10.4f | D= %10.4f| Alpha= %8d | gamma2= %8d", 
								_sys._beta + (double)_sys._gamma2* _grV._distance[startId][finishId],
								_grV._distance[startId][finishId],
								_sys._beta,
								_sys._gamma2));
					}
				}
			}
			//Tinh tien bang khoang cach voi duong di ngan nhat
		}
		
		// Tinh cach chi phi xang xe dung de di lai
		for (int cs = 0; cs < listPost.length - 1; cs++){
			profit =profit- (double)_sys._gamma3 * _grV._distance[listPost[cs]][listPost[cs + 1]];
			if (_checkPrint){
				System.out.println(listPost[cs]+"__"+listPost[cs+1]);
				System.out.println(String.format("- %10.4f | D= %10.4f| TMin= %10.4f| gamma3= %8d", 
						_sys._gamma3 * _grV._distance[listPost[cs]][listPost[cs + 1]],
						_grV._distance[listPost[cs]][listPost[cs + 1]],
						(double)_grV._min_travel_time[listPost[cs]][listPost[cs + 1]],
						_sys._gamma3));
			}
		}
			
		
		for (int cs = 1; cs < listPost.length - 1; cs++) {
			Position pos = _grV._listPositions.get(listPost[cs] - 1);
			if (pos._isPassengerSrcId) {
				if (pos._type_request==ParameterSARP.TYPE_REQUEST_PARCEL) continue;
				int IdEndPos = pos._id +_grV._nbParcel + _grV._nbPassenger;
				int eta = pos._eta;
				int time1 = timeArrive[cs];
				int time2 = time1;
				double distanceTravelReal=0;
				double distance=0;
				for (int i = 1; i <= eta && cs + i < listPost.length; i++){
					distanceTravelReal+= _grV._distance[listPost[cs+i-1]][listPost[cs+i]];
					if (listPost[cs + i] == IdEndPos) {
						time2 = timeGoto[cs + i];
						distance= _grV._distance[listPost[cs]][listPost[cs+i]];
						break;
					}
				}
				/*	
				int time = (time2 - time1)*ParameterSARP.DIV_MINUTE;
				profit =profit- _sys._gamma4 * Math.max(0,((double) time / (double) pos._Ti - 1));
				if (_checkPrint){
					System.out.println(String.format("- %10.4f | gamma4= %8d", 
							_sys._gamma4 * Math.max(0,((double) time / (double) pos._Ti - 1)),
									_sys._gamma4));
				}
				*/
				profit =profit- _sys._gamma4 * Math.max(0,((double) distanceTravelReal / (double) distance - 1));
				if (_checkPrint){
					System.out.println(String.format("- %10.4f | gamma4= %.6f", 
							_sys._gamma4 * Math.max(0,((double) distanceTravelReal / (double) distance - 1)),
									_sys._gamma4));
				}
			}
		}
		return profit;
	}

	private boolean checkCondition(){
		if (!checkTimeCondition()){
			//System.out.println("Loi check time condition");
			return false;
		} 
		if (!checkPosCondition()) {
			//System.out.println("Loi check pos condition");
			return false;
		}
		if (!checkDistanceCondition()){
			//System.out.println("Loi check Distance condition");
			return false;
		}
		/*
		if (!checkTimeArrive()) {
			//System.out.println("Loi check Time Arrive condition");
			return false;
		}
		*/
		if (!checkCapacityTaxi()){
			//System.out.println("Loi check capacity condition");
			return false;
		}
		return true;
	}

	private boolean checkCapacityTaxi() {
		int capacity = 0;
		for (int cs = 1; cs < listPost.length - 1; cs++) {
			Position pos = _grV._listPositions.get(listPost[cs] - 1);
			if (pos._isInRequest) {
				capacity += pos._type_request;
				if (!(capacity == 0 || capacity == 1 || capacity == 5 || capacity == 6||
						capacity==2|| capacity==7))
					return false;
			}
		}
		return true;
	}

	private boolean checkTimeArrive() {
		for (int cs = 1; cs < listPost.length - 1; cs++) {
			Position pos = _grV._listPositions.get(listPost[cs] - 1);
			if (pos._isPassengerSrcId) {
				if (pos._type_request == ParameterSARP.TYPE_REQUEST_PARCEL)
					return true;
				int IdEndPos = pos._id + _grV._nbParcel + _grV._nbPassenger;
				int eta = pos._eta;
				int minTime = pos._Ti;
				int maxTime = pos._MaxTi;
				int time1 = timeArrive[cs];
				int time2 = time1;
				for (int i = 1; i <= eta && cs + i < listPost.length; i++)
					if (listPost[cs + i] == IdEndPos) {
						time2 = timeGoto[cs + i];
						break;
					}
				int time = (time2 - time1)*ParameterSARP.DIV_MINUTE;
				/*
				 * System.out
				 * .printf("[Start = %2d|End = %2d|MinT = %3d|MaxT = %3d|T = %3d]\n"
				 * , pos._id, IdEndPos, minTime, maxTime, time);
				 */
				if (time < minTime || time > maxTime)
					return false;
			}
		}
		return true;
	}
	/*Ham check rang buoc khoang canh dang co thu tu uu tien sau Ham check contrain vi tri
	 * */
	private boolean checkDistanceCondition() {
		for (int cs = 1; cs < listPost.length - 1; cs++) {
			Position pos = _grV._listPositions.get(listPost[cs] - 1);
			if (pos._isPassengerSrcId) {
				//Neu la request cho vat thi co the bo qua
				if (pos._type_request==ParameterSARP.TYPE_REQUEST_PARCEL) return true;
				//Bay gio ta xet voi request nguoi
				int IdEndPos = pos._id + _grV._nbParcel + _grV._nbPassenger;
				int eta = pos._eta;
				double minDistance = pos._Di;
				double maxDistance = pos._MaxDi;
				double distance = 0;
				for (int i = 1; i <= eta && cs + i < listPost.length; i++) {
					distance += _grV._distance[listPost[cs + i - 1]][listPost[cs + i]];
					if (listPost[cs + i] == IdEndPos)
						break;
				}
				if (distance < minDistance - 0.00000001
						|| distance > maxDistance + 0.000000001)
					return false;
			}
		}
		return true;
	}

	private boolean checkPosCondition() {
		// System.out.println("Checking:::Chech Pos Condition");
		for (int cs = 0; cs < listPost.length - 1; cs++) {
			Position pos = _grV._listPositions.get(listPost[cs] - 1);
			/*Chi co cac diem la nguon cua request cho hang hoac cho nguoi moi duoc xet de dem so diem nam giua
			 * 
			 * */
			if (pos._isPassengerSrcId) {
				int IdEndPos = pos._id + _grV._nbParcel + _grV._nbPassenger;
				int eta = pos._eta;
				boolean findFollowEta = false;
				/*Duyet va dem theo gioi han so diem
				 * */
				for (int i = 1; (i <= (eta + 1)) && (cs + i < listPost.length); i++) {
					if (listPost[cs + i] == IdEndPos) {
						findFollowEta = true;
						break;
					}
				}
				if (!findFollowEta)
					return false;
			}
		}
		return true;
	}

	private boolean checkTimeCondition() {
		/*
		System.out.println(listPost.length);
		for(int cs=0;cs<listPost.length;cs++){
			System.out.printf(String.format("%5d %5d %5d\n", listPost[cs], timeGoto[cs],timeArrive[cs]));
		}
		System.out.println();
		*/
		for(int cs=1;cs<listPost.length-1;cs++){
			timeGoto[cs]=_grV._listPositions.get(listPost[cs]-1)._early;
			timeArrive[cs]=_grV._listPositions.get(listPost[cs]-1)._late;
		}
		// Khoi tao ban dau cac diem don va diem tra chinh la cac khung thoi gian
		// tham so maxWaitTime trong file config trong ban cap nhap khong duoc su dung
		// tham so chi tham chieu khoang cach thoi gian tinh tu thoi diem goi toi diem pick up som nhat
		for (int cs = 1; cs < listPost.length - 2; cs++) {
			//System.out.println("CS= "+ cs);
			if (cs >= listPost.length - 2){
				timeGoto[listPost.length-1]= timeArrive[listPost.length-2]+ 
						_grV._min_travel_time[listPost[listPost.length-2]][listPost[listPost.length-1]];
				break;
			}
			// Khong con gi de ma xet 
			int early = _grV._listPositions.get(listPost[cs + 1] - 1)._early;
			int late = _grV._listPositions.get(listPost[cs + 1] - 1)._late;
			int minTime = _grV._min_travel_time[listPost[cs]][listPost[cs + 1]];
			int maxTime = _grV._max_travel_time[listPost[cs]][listPost[cs + 1]];
			if (minTime>maxTime) return false;
			// Lay cac gia tri early, late, minTime, maxTime dung de tinh toan
			int segmentStart = early - maxTime;
			int segmentFinish = late - minTime;
			// segmentStart va segmentFinish la khoang thoi gian dung de xac dinh khung thoi 
			// gian de roi khoi diem truoc do ma co du thoi gian den diem hien tai trong
			// khoang thoi gian tu early voi late
			// Diem phia truoc la diem i, diem dang xet voi khung thoi gian den la diem i+1
			// Vi ly do nay nen chi so chay chi chay tu 1 deim listPost.length -3
			
			/*Truong hop 1: timeArrive[cs] nam trong khoang thoi gian co the den duoc diem thu
			 * i+1 dung thoi gian trong doan thu early toi late
			 * */
			//System.out.println("Seg: start + finish");
			//System.out.println(segmentStart+"___"+segmentFinish);
			if (timeArrive[cs] >= segmentStart
					&& timeArrive[cs] <= segmentFinish) {
				//Tham lam: Lay thoi gian den diem (cs+1) som nhat co the
				//Gia tri nay duoc lay bang cac lay gia tri lon nhat cua 1 trong 2 gia tri sau
				timeGoto[cs + 1] = Math.max(timeGoto[cs + 1], timeArrive[cs]
						+ minTime);
				timeArrive[cs+1]= Math.min(timeGoto[cs+1]+ ParameterSARP.TIME_WAIT_PER_REQUEST,timeArrive[cs+1]);// Luc don cung mac dinh la luc di
				//Neu da xac dinh duoc thoi timeGoto[cs+1] va timeArrive[cs+1] thi co ta thuc hien chuyen den chi so tiep theo
				continue;
			}
			//Day la duoc hop timeArrive[cs] khong nam trong doan dang xet
			/* Truong hop thoi diem den cua timeGoto[cs] lon hon gia tri cuoi
			 * Khong thuc hien buoc nay nua
			 * Mac dinh khong tinh toan gia tri thoi gian cho cac buoc tiep theo
			 * */
			if (timeGoto[cs] > segmentFinish)
				return false;// Truong hop khong the toi diem dung ke hoach
			// Be hon thi xet lai
			
			if (timeGoto[cs] >= segmentStart) {
				// Mang tinh chat tham lam, chi xet phan co timeGoto >= segmentStart
				timeArrive[cs] = timeGoto[cs];
				//Neu thoi gian den diem cs nam trong khoang thoi gian co the di chuyen den
				//Khi do ta can thoi diem roi khoi bang chinh thoi diem di chuyen den
				timeGoto[cs + 1] = Math.max(timeGoto[cs + 1], timeArrive[cs]
						+ minTime);// cap nhat lai thoi gian den cua diem cs+1
				timeArrive[cs+1]= Math.min(timeGoto[cs+1]+ ParameterSARP.TIME_WAIT_PER_REQUEST,timeArrive[cs+1]);// Luc don cung mac dinh la luc di
				/*
				 * Befor 2015 02 07 
				 * Because: Parameter ._max_wait_time trong cac request khong con
				 * timeArrive[cs + 1] = timeGoto[cs + 1]+ _listPositions.get(listPost[cs + 1] - 1)._max_wait_time;
				 * 
				 * */
				continue;
			}
			// Xet trong truong hop timeGoto[cs]> segmentStart
			if (timeArrive[cs]>=segmentStart){
				timeArrive[cs]=segmentStart;
				continue;
			}
			insertParkingBeetween(cs);
			//System.out.println("After add parking");
			
			double maxDistance = Integer.MAX_VALUE;
			for (int csParking = 0; csParking < _grV._listParking.size(); csParking++) {
				//Duyet qua cac tram dung xe de tim tram hop ly nhat 
				Parking par = _grV._listParking.get(csParking);
				timeArrive[cs]= timeGoto[cs];
				/*Trong truong hop xet diem tram thi xet timeGoto[cs]= timeArrive[cs] cho moi truong hop
				 * */
				int waitTime=0;
				
				int timeGotoPark = par.getTimeGotoFrom(timeGoto[cs]
						+ _grV._min_travel_time[listPost[cs]][par._pos._id]);
				// timeGotoPark: co nghia la thoi gian gan nhat de toi 1 tram, ma khi do tram xe con cho trong de chua xe
				if (timeGotoPark == -1)
					continue;
				// Khong tim duoc thoi gian cho yeu cau tren thi thoat de xet den tram tiep theo
				int posPark = par._pos._id;
				if (timeGotoPark> timeGoto[cs]+_grV._max_travel_time[listPost[cs]][posPark]) continue;
				// Neu thoi gian den tram vuot qua diem thoi gian co the di chuyen toi lau nhat co the thi bo qua tram xe nay de xet toi tram xe tiep theo
				//....................
				int segmentStartPark = early
						- _grV._max_travel_time[posPark][listPost[cs + 2]];
				int segmentFinishPark = early
						- _grV._min_travel_time[posPark][listPost[cs + 2]];
				// Xac dinh khung thoi gian de roi khoi tram, tien toi diem tiep theo dung thoi gian cua timewindow
				int timeArrivePark = par.findArrivePark(timeGotoPark,
						segmentStartPark, segmentFinishPark);
				//Do thoi gian dung o tram la khong xac dinh, khi do ta chi can tim diem thoi gian de roi khoi tram mot
				//cach hop ly nhat
				/*Uu tien gia tri o gan diem thoi gian segmentFinishPark
				 * */
				// Tim thoi gian de doi khoi ben do thich hop nhat
				if (timeArrivePark == -1)
					continue;
				if(timeGotoPark>=timeArrivePark) continue;
				//Trong cac tram dung tim duoc uu tien tram co loi the ve khoang cach nho nhat
				if (_grV._distance[listPost[cs]][posPark]
						+ _grV._distance[posPark][listPost[cs + 2]] < maxDistance) {
					maxDistance = _grV._distance[listPost[cs]][posPark]
							+ _grV._distance[posPark][listPost[cs + 2]];
					listPost[cs + 1] = posPark;
					timeGoto[cs + 1] = timeGotoPark;
					timeArrive[cs + 1] = timeArrivePark;
				}
			}
			if (maxDistance < Integer.MAX_VALUE - 1)
				cs++;
			else
				return false;
		}
		/*
		for(int cs=0;cs<listPost.length;cs++){
			System.out.printf(String.format("%5d %5d %5d\n", listPost[cs], timeGoto[cs],timeArrive[cs]));
		}
		System.out.println();
		*/
		return true;
	}

	private void insertParkingBeetween(int pos) {
		//Chem them 1 diem tram, vi tri cua no la phia sau vi tri pos
		int[] tmpListPos = new int[listPost.length + 1];
		int[] tmpTimeGoto = new int[listPost.length + 1];
		int[] tmpTimeArrive = new int[listPost.length + 1];
		for (int cs = 0; cs <= pos; cs++) {
			tmpListPos[cs] = listPost[cs];
			tmpTimeGoto[cs] = timeGoto[cs];
			tmpTimeArrive[cs] = timeArrive[cs];
		}
		//Tu vi tri 0 den vi tri cs
		for (int cs = tmpListPos.length - 1; cs > pos + 1; cs--) {
			tmpListPos[cs] = listPost[cs - 1];
			tmpTimeGoto[cs] = timeGoto[cs - 1];
			tmpTimeArrive[cs] = timeArrive[cs - 1];
		}
		tmpListPos[pos + 1] = 0;
		tmpTimeGoto[pos + 1] = 0;
		tmpTimeArrive[pos + 1] = 0;
		listPost = new int[tmpListPos.length];
		timeGoto = new int[tmpListPos.length];
		timeArrive = new int[tmpListPos.length];
		for (int cs = 0; cs < listPost.length; cs++) {
			listPost[cs] = tmpListPos[cs];
			timeGoto[cs] = tmpTimeGoto[cs];
			timeArrive[cs] = tmpTimeArrive[cs];
		}
	}

	private void trimListPost() {
		int csTmp = 0;
		for (int cs = 0; cs < listPost.length; cs++) {
			if (listPost[cs] != 0) {
				Position pos = _grV._listPositions.get(listPost[cs] - 1);
				if (pos._isParking) {
				} else {
					listPost[csTmp] = listPost[cs];
					timeGoto[csTmp] = timeGoto[cs];
					timeArrive[csTmp] = timeArrive[cs];
					csTmp++;
				}
			}
		}
		int[] tmpListPost = new int[csTmp];
		int[] tmpTimeGoto = new int[csTmp];
		int[] tmpTimeArrive = new int[csTmp];
		for (int cs = 0; cs < csTmp; cs++) {
			tmpListPost[cs] = listPost[cs];
			tmpTimeGoto[cs] = timeGoto[cs];
			tmpTimeArrive[cs] = timeArrive[cs];
		}
		listPost = tmpListPost;
		timeGoto = tmpTimeGoto;
		timeArrive = tmpTimeArrive;
	}

	private void addCapacity(SequenTaxi sequenTaxi, int value) {
		// Co the cai tien duoc o buoc nay
		for (int cs = 0; cs < sequenTaxi._listPost.length; cs++) {
			int id = sequenTaxi._listPost[cs];
			Position pos = _grV._listPositions.get(id - 1);
			if (pos._isParking) {
				int time1 = sequenTaxi._timeGoto[cs];
				int time2 = sequenTaxi._timeArrive[cs];
				for (int csPark = 0; csPark < _grV._listParking.size(); csPark++) {
					if (_grV._listParking.get(csPark)._id == id) {
						_grV._listParking.get(csPark).addCapacity(time1, time2,
								value);
						break;
					}
				}
			}
		}
	}

}

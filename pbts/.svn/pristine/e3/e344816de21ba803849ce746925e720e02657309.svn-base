package ktuan;

import java.util.ArrayList;

public class SequenTaxi {
	public double _profit;
	public int[] _listPost;
	public int[] _timeGoto;
	public int[] _timeArrive;
	public ArrayList<RequestTx> _listRequestServer;
	public Position _posTaxi;
	private ArrayList<Position> _lPos;

	public SequenTaxi(Position position, SystemSARP _sys) {
		this._profit = 0;
		this._listPost = new int[2];
		this._timeGoto = new int[2];
		this._timeArrive = new int[2];
		_listPost[0] = position._id;
		_listPost[1] = position._id;
		_timeGoto[0] = _timeArrive[0] = _sys._startWorkingTime/ParameterSARP.DIV_MINUTE;
		_timeGoto[1] = _timeArrive[1] = _sys._terminateWorkingTime/ParameterSARP.DIV_MINUTE;
		_listRequestServer = new ArrayList<RequestTx>();
		_posTaxi = position;
	}

	public void printfInfo() {
		System.out.println("_____________________________________________");
		System.out.println("Profit       : " + _profit);
		System.out.println("Number Point : " + _listPost.length);
		for (int cs = 0; cs < _listPost.length; cs++) {
			System.out.printf("[Id = %2d|Goto = %2d | Arr = %2d]\n",
					_listPost[cs], _timeGoto[cs], _timeArrive[cs]);
		}
		System.out.println("_____________________________________________");
	}

	public void update(int[] saveListPost, int[] saveTimeGoto,
			int[] saveTimeArrive, double saveProfit) {
		this._profit = saveProfit;
		_listPost = new int[saveListPost.length];
		_timeGoto = new int[saveListPost.length];
		_timeArrive = new int[saveListPost.length];
		for (int cs = 0; cs < _listPost.length; cs++) {
			_listPost[cs] = saveListPost[cs];
			_timeGoto[cs] = saveTimeGoto[cs];
			_timeArrive[cs] = saveTimeArrive[cs];
		}
	}

	public void createTextResult(VirtureGraph grV) {
		int n= _listPost.length;
		_timeGoto[0]=-1;
		_timeArrive[0]= _timeGoto[1]-(int)(grV._min_travel_time[_listPost[0]][_listPost[1]]);
		_timeGoto[n-1]= _timeArrive[n-2]+(int)(grV._min_travel_time[_listPost[n-2]][_listPost[n-1]]);
		_timeArrive[n-1]=-1;
	}

	public void tranFromRealId(VirtureGraph grV, Data data) {
		int n= _listPost.length;
		_lPos= new ArrayList<Position>();
		for(int i=0;i<n;i++){
			_lPos.add(grV._listPositions.get(_listPost[i]-1));
			_listPost[i]= grV._convert.get(_listPost[i]); 
			if (_timeGoto[i]>=0) _timeGoto[i]*=ParameterSARP.DIV_MINUTE;
			if (_timeArrive[i]>=0) _timeArrive[i]*=ParameterSARP.DIV_MINUTE;
		}
	}

	public String printfListPostReal(GraphSARP graph, SystemSARP sys, boolean b) {
		if (_listPost.length<=2) return new  String("");
		String ans= new String();
		System.out.println(_lPos.get(0)._idRequest);
		ans=ans.concat(new String(_lPos.get(0)._idRequest+"\n"));
		//................................................
		int n= _listPost.length;
		for (int i=0;i<n;i++){
			if (i>0 && _listPost[i]!=_listPost[i-1]){
				String concatStr= new String();
				graph.dijkstraHeap(_listPost[i-1]);
				if (b)ans=ans.concat(String.format("Distance:    %10.2f<m>\n-----%10.2f<VND>\n", graph._d[_listPost[i]],graph._d[_listPost[i]]*sys._gamma3));
				int u=_listPost[i];
				ArrayList<Integer> posIJK= new  ArrayList<Integer>();
				ArrayList<Double>  disIJK= new  ArrayList<Double>();
				ArrayList<Integer> timeMinIJK= new  ArrayList<Integer>();
				ArrayList<Integer> timeMaxIJK= new  ArrayList<Integer>();
				while(u!=_listPost[i-1]){
					int v=graph._preNode[u];
					posIJK.add(v);
					disIJK.add(graph._d[u]-graph._d[v]);
					timeMinIJK.add(graph._timeMin[u]-graph._timeMin[v]);
					timeMaxIJK.add(graph._timeMax[u]-graph._timeMax[v]);
					u=v;
				}
				ArrayList<Integer> timeIJK=new ArrayList<Integer>();
				int M= posIJK.size();
				for(int cs=0;cs<M;cs++)timeIJK.add(0);
				int totalTime= _timeGoto[i]-_timeArrive[i-1];
				if (totalTime<M) System.out.println("Qua nhieu diem++++++++++++++++++++++++++++++++++++++++++++++");
				if (totalTime<M) System.out.println("Qua nhieu diem++++++++++++++++++++++++++++++++++++++++++++++");
				if (totalTime<M) System.out.println("Qua nhieu diem++++++++++++++++++++++++++++++++++++++++++++++");
				if (totalTime<M) System.out.println("Qua nhieu diem++++++++++++++++++++++++++++++++++++++++++++++");
				if (totalTime<M) System.out.println("Qua nhieu diem++++++++++++++++++++++++++++++++++++++++++++++");
				for(int cs=0;cs<M;cs++){
					if (totalTime<=0) break;
					timeIJK.set(cs,timeIJK.get(cs)+timeMinIJK.get(cs));
					totalTime-=timeMinIJK.get(cs);
				}
				
				for(int cs=0;cs<M;cs++){
					if (totalTime<=0) break;
					int t=timeMaxIJK.get(cs)-timeIJK.get(cs);
					t=Math.min(t, totalTime);
					timeIJK.set(cs, timeIJK.get(cs)+t);
					totalTime-=t;
				}
				int findTime= _timeArrive[i-1];
				for(int cs=M-1;cs>=1;cs--){
					int v=posIJK.get(cs-1);
					int t=timeIJK.get(cs);
					findTime+=t;
					if (!b)concatStr=concatStr.concat(new  String(v+" "+findTime+" "+findTime+" PASS -1\n"));
					//concatStr=concatStr.concat(String.format("[D = %10.2f||T_MIN = %10.2f||T_MAX = %10.2f]\n", disIJK.get(cs),disIJK.get(cs)/vmax,disIJK.get(cs)/vmin));
				}
				
				//System.out.println(new String(v+" "+findTime+" "+findTime+" PASS -1\n"));				}
				if (!b)ans= ans.concat(concatStr);
			}
		
			System.out.print(_listPost[i]+" "+_timeGoto[i]+" "+_timeArrive[i]);
			ans=ans.concat(_listPost[i]+" "+_timeGoto[i]+" "+_timeArrive[i]);
			if (i==0){
				System.out.println(" "+"PASS -1");
				ans=ans.concat(" "+"PASS -1\n");
			}else
			if (i==n-1){
				System.out.println(" FINISH_WORK -1");
				System.out.println("-1");
				ans=ans.concat(" FINISH_WORK -1\n");
				ans=ans.concat("-1\n");
			}else {
				Position pos= _lPos.get(i);
				if (pos._type_position==ParameterSARP.PICKUP_PEOPLE){
					System.out.println(" PICKUP_PEOPLE "+pos._idRequest);
					ans=ans.concat(" PICKUP_PEOPLE "+pos._idRequest+"\n");
					
					int cs=i+1;
					while (cs!=0){
						if (_lPos.get(cs)._idRequest!=pos._idRequest) cs++; else break;
					}
					graph.dijkstraHeap(_listPost[i]);
					if (b)ans= ans.concat(String.format("KC= %10.2f<m>\n+++++%10.2f<VND>\n",graph._d[_listPost[cs]], graph._d[_listPost[cs]]*sys._gamma1+sys._alpha));
					int time= _timeGoto[cs]-_timeArrive[i];
					int time_max= _lPos.get(i)._Ti;
					double subBecauseTime=sys._gamma4 * Math.max(0,(((double) time / (double) time_max) - 1));
					if (b)ans=ans.concat(String.format( "-----%10.2f<VND>\n", subBecauseTime));
				}else 
				if (pos._type_position==ParameterSARP.DELIVERY_PEOPLE){
					System.out.println(" DELIVERY_PEOPLE "+pos._idRequest);
					ans=ans.concat(" DELIVERY_PEOPLE "+pos._idRequest+"\n");
				}else
				if (pos._type_position==ParameterSARP.PICKUP_PARCEL){
					System.out.println(" PICKUP_PARCEL "+pos._idRequest);
					ans=ans.concat(" PICKUP_PARCEL "+pos._idRequest+"\n");
					
					int cs=i+1;
					while (cs!=0){
						if(cs>=_lPos.size()) break;
						if (_lPos.get(cs)._idRequest!=pos._idRequest) cs++; else break;
					}
					graph.dijkstraHeap(_listPost[i]);
					if (b)ans= ans.concat(String.format("KC= %10.2f<m>\n+++++%10.2f<VND>\n",graph._d[_listPost[cs]],graph._d[_listPost[cs]]*sys._gamma2+sys._beta));
				}else 
				if (pos._type_position==ParameterSARP.DELIVERY_PARCEL){
					System.out.println(" DELIVERY_PARCEL "+pos._idRequest);
					ans=ans.concat(" DELIVERY_PARCEL "+pos._idRequest+"\n");
				}else 
				if (pos._type_position==ParameterSARP.STOP){
					System.out.println(" STOP -1");
					ans=ans.concat(" STOP -1"+"\n");
				}else {
					System.out.println(" PASS -1");
					ans=ans.concat("PASS -1\n");
				}					
			}
		}
		System.out.println(ans.length()+"____________");
		return ans;
	}

}

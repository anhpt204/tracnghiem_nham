package pbts.shortestpaths;

import java.util.*;
import java.io.*;

import pbts.entities.Arc;
import pbts.entities.*;
class Element{
	public int id;
	public double key;
	public boolean fixed;
	public Element(int id, double key){
		this.id = id;
		this.key = key;
		fixed = false;
	}
}

class BinaryHeap{
	public int maxSz;
	public Element[] L;
	public int sz;// current size
	public HashMap<Element, Integer> map;
	
	
	public BinaryHeap(int maxSz){
		this.maxSz = maxSz;
		L = new Element[maxSz];
		map = new HashMap<Element, Integer>();
	}
	public void print(){
		System.out.print("DijkstraBinaryHeap::print(sz = " + sz + "): ");
		for(int i = 0; i < sz; i++){
			System.out.print("nod " + i + " (" + L[i].id + "," + L[i].key + "), ");
		}
		System.out.println();
		/*
		for(int i = 0; i < sz; i++){
			int u = 2*i+1;
			int v = 2*i+2;
			if(u < sz)if(L[i].key > L[u].key){
				System.out.println("DijkstraBinaryHeap::print check failed L[" + i + "] = " + L[i].key + " > L[" + u + "] = " + L[u].key);
				//print();
				return;// false;
			}
			if(v < sz)if(L[i].key > L[v].key){
				System.out.println("DijkstraBinaryHeap::printcheck failed L[" + i + "] = " + L[i].key + " > L[" + v + "] = " + L[v].key);
				//print();
				return;// false;
			}
		}
		*/
		//if(!check()){
			//System.out.println("EXIT"); System.exit(-1);
		//}
	}
	public void adjustUp(int idx){
		int i = idx;
		while(true){
			if(i == 0) break;
			int pi = (i + 1)/2-1;
			if(L[i].key < L[pi].key){
				Element tmp = L[i];
				L[i] = L[pi];
				L[pi] = tmp;
				map.put(L[i],i);
				map.put(L[pi],pi);
				i = pi;
			}else
				break;
		}
	}

	public void adjustDown(int idx){
		int i = idx;
		double K = L[idx].key;
		while(true){
			if(i >= sz) break;
			int ci1 = 2*i+1;
			int ci2 = 2*i+2;
			int ci = i;
			
			if(ci1 >= sz) break;
			if(K > L[ci1].key) {
				ci = ci1;
			}
			
			if(ci2 < sz) if(L[ci1].key > L[ci2].key && L[ci2].key < K){
				ci = ci2;
			}
			
			if(ci == i) break;
			
			Element tmp = L[i];
			L[i] = L[ci];
			L[ci] = tmp;
				map.put(L[i],i);
				map.put(L[ci],ci);
			
			i = ci;
		}
	}
	public void insert(Element x){
		//System.out.println("BinaryHeap::insert(" + x.id + ", " + x.key + ") before insert");
		//print();
		double K = x.key;
		sz++;
		L[sz-1] = x;
		map.put(x,sz-1);
		// adjust heap
		adjustUp(sz-1);
		
		//if(!check()){
			//System.out.println("BinaryHeap::insert(" + x.id + ", " + x.key + ") --> check failed");
			//System.exit(-1);
		//}
		
	}
	public int size(){ return sz;}
	public boolean check(){
		for(int i = 0; i < sz; i++){
			int u = 2*i+1;
			int v = 2*i+2;
			//System.out.println("BinaryHeap::check sz = " + sz + ", u = " + u + ", v = " + v + ", L[" + i + "] = " + L[i].key);
			if(u < sz)if(L[i].key > L[u].key){
				System.out.println("BinaryHeap::check failed L[" + i + "] = " + L[i].key + " > L[" + u + "] = " + L[u].key);
				print();
				return false;
			}
			if(v < sz)if(L[i].key > L[v].key){
				System.out.println("BinaryHeap::check failed L[" + i + "] = " + L[i].key + " > L[" + v + "] = " + L[v].key);
				print();
				return false;
			}
		}
		return true;
	}
	public Element extractMin(){
		//System.out.println("BinaryHeap::extractMin, before extractMin");
		//print();
		if(size() <= 0) {
			System.out.println("HeapMin::extractMin() exception, Heap is empty");
			assert(false);
			return null;
		}
		Element ret = L[0];
		
		Element tmp = L[0];
		L[0] = L[sz-1];
		L[sz-1] = L[0];
		map.put(L[0],0);
		map.put(L[sz-1],sz-1);
		
		sz--;
		adjustDown(0);
		map.put(ret, null);
		
		//if(!check()){
			//System.out.println("BinaryHeap::extractMin --> check failed");
			//System.exit(-1);
		//}
		
		return ret;
	}
	public void decreaseKey(Element x, double key){
		int idx = map.get(x);
		adjustUp(idx);
	}
	public boolean contains(Element e){ return map.get(e) != null;}
	
	public boolean empty(){
		return sz == 0;
	}
	public void clear(){ sz = 0; map.clear();}
	public Element get(int idx){
		return L[idx];
	}

	

	public void updateKey(Element e, double newKey){
		//System.out.println("BinaryHeap::updateKey(" + e.id + ", oldKey = " + e.key + ", newKey = " + newKey + ") before update");
		//print();
		
		//if(!check()){
			//System.out.println("BinaryHeap::updateKey(" + e.id + ", oldKey = " + e.key + ", newKey = " + newKey + ") before --> check failed");
			//System.exit(-1);
		//}
		double k = e.key;
		int idx = map.get(e);
		e.key = newKey;
		if(k > newKey)
			adjustUp(idx);
		else if(k < newKey)
			adjustDown(idx);
		
		//if(!check()){
			//System.out.println("BinaryHeap::updateKey(" + e.id + ", " + newKey + ") --> check failed");
			//System.exit(-1);
		//}
		
	}

}



public class DijkstraBinaryHeap {

	private int n;// number of vertices: 1, 2, ..., n
	private ArrayList<Integer> V;
	private HashMap<Integer, ArrayList<Arc>>	 A;// A[v] is the set of adjacent (outgoing) edges of v
	private int s, t;
	
	public HashMap<Integer, Integer> pre;
	public HashMap<Integer, Element> mD;
	//private double[] d;// d[v] will be the shortest distance from s to v
	private BinaryHeap H;
	
	//private int[] 	 pre;// pre[v] will be the precedent vertex of v on the shortest path from s to v
	
	public static final double infinity = 100000000;
	private PrintWriter log = null;
	
	HashMap<Integer,Double> mCost;
	HashMap<Integer, Itinerary> mPath;
	int cooe;
	
	//public DijkstraBinaryHeap(){
		
	//}
	public DijkstraBinaryHeap(ArrayList<Integer> V, HashMap<Integer, ArrayList<Arc>> A){
		this.V = V;
		this.A = A;
		this.n = V.size();
		cooe = 0;
		for(int i = 0; i < V.size(); i++){
			int v = V.get(i);
			if(cooe < v) cooe = v;
		}
		cooe++;
		mCost = new HashMap<Integer, Double>();
		mPath = new HashMap<Integer, Itinerary>();
		
		H = new BinaryHeap(n);
		mD = new HashMap<Integer, Element>();
		//Element[] d = new Element[n+1];
		pre = new HashMap<Integer, Integer>();//int[n+1];
	}
	public void initLog(){
		try{
			log = new PrintWriter("DijkstraBinaryHeap-log.txt");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void finalize(){
		log.close();
	}
	private int composeKey(int u, int v){
		return u * cooe + v;
	}
	
	/*
	public void readData(String filename){
		try{
			Scanner in = new Scanner(new File(filename));
			n = in.nextInt();
			int m = in.nextInt();
			A = new HashSet[n+1]; //A[1], A[2], ..., A[n]
			for(int v = 1; v <= n; v++)
				A[v] = new HashSet<Arc>();
			
			System.out.println("n = " + n + ", m = " + m);
			for(int i = 0; i < m; i++){
				int u = in.nextInt();
				int v = in.nextInt();
				double w = in.nextDouble();
				
				Arc e = new Arc(u,v,w);
				A[u].add(e);
			}
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	*/
	public Element findMin(){
		Element e = H.extractMin();
		return e;
	}
	public Itinerary queryShortestPath(int s, int t){
		Itinerary I = solve(s,t);
		if(I != null){
			int key = composeKey(s,t);
			if(mCost.get(key) == null)
				mCost.put(key, I.getDistance());
		}
		return I;
		/*
		int key = composeKey(s,t);
		//if(mCost.get(key) != null) return mCost.get(key);
		if(mPath.get(key) != null) return mPath.get(key);
		Itinerary I = solve(s,t);// store distances computed in mCost
		if(I != null){
			//double dis = I.getDistance();//mCost.get(key);//I.getDistance();
			//return dis;
			mPath.put(key, I);
			return I;
		}
		return null;
		*/
	}
	public double queryDistance(int s, int t){
		int key = composeKey(s,t);
		if(mCost.get(key) != null) return mCost.get(key);
		//if(mPath.get(key) != null) return mPath.get(key).getDistance();
		Itinerary I = solve(s,t);// store distances computed in mCost
		if(I != null){
			double dis = I.getDistance();//mCost.get(key);//I.getDistance();
			mCost.put(key,dis);
			return dis;
			//mPath.put(key, I);
			//return I.getDistance();
		}
		return infinity;
	}
	public Itinerary solveWithoutHeap(int s, int t){
		this.s = s; this.t = t;
		double cost = -1;
		HashMap<Integer, Double> D = new HashMap<Integer, Double>();
		for(int i = 0; i < V.size(); i++)
			D.put(V.get(i), infinity);
		D.put(s,0.0);
		Iterator it = A.get(s).iterator();
		while(it.hasNext()){
			Arc a = (Arc)it.next();
			D.put(a.end, a.w);
			pre.put(a.end, s);
			//if(a.end == 17347)
				//log.println("DijkstraBinaryHeap::solveWithoutHeap, init D[" + a.end + "] = " + D.get(a.end));
		}
		//log.println("DijkstraBinaryHeap::solveWithoutHeap, init D[" + 17347 + "] = " + D.get(17347));
		HashSet<Integer> S = new HashSet<Integer>();
		for(int i = 0; i < V.size(); i++){
			int v = V.get(i);
			S.add(v);
		}
		while(S.size() > 0){
			int u = -1;
			double minD = infinity;
			Iterator is = S.iterator();
			while(is.hasNext()){
				int v = (Integer)is.next();
				if(D.get(v) < minD){
					u = v; minD = D.get(v);
				}
			}
			//System.out.println("DijkstraBinaryHeap::solveWithoutHeap, POP u = " + u + ", d = " + D.get(u));
			//log.println("DijkstraBinaryHeap::solveWithoutHeap, POP u = " + u + ", d = " + D.get(u));
			if(u == t){
				cost = D.get(u);
				break;
			}
			S.remove(u);
			is = A.get(u).iterator();
			while(is.hasNext()){
				Arc a = (Arc)is.next();
				if(D.get(a.end) > D.get(u) + a.w){
					D.put(a.end, D.get(u) + a.w);
					pre.put(a.end, u);
					//if(a.end == 17347)
						//log.println("DijkstraBinaryHeap::solveWithoutHeap --> from u = " + u + ", Update D(" + a.end + ") = " + D.get(a.end));
				}
			}
			
		}
		if(cost > infinity-1){
			return null;
			/*
			Itinerary I = new Itinerary(new ArrayList<Integer>());
			I.setDistance(infinity);
			return I;// not found path
			*/
		}
		
		ArrayList<Integer> L = new ArrayList<Integer>();
		// print out the shortest path
		Stack St = new Stack<Integer>();
		int x = t;
		//while(pre.get(x) != s){
		while(true){
			St.push(x);
			x = pre.get(x);
			if(x == s) break;
			//System.out.println("push " + x);
		}
		St.push(s);
		
		while(St.size() > 0){
			int v = (Integer)St.pop();
			//System.out.print(v + " -> ");
			L.add(v);
		}
		Itinerary I = new Itinerary(L);
		I.setDistance(cost);
		return I;
	
	}
	public Itinerary solve(int s, int t){
		
		//System.out.println("Dijkstra.solve(" + s + "," + t + ")");
		this.s = s; this.t = t;
		
		if(s == t){
			ArrayList<Integer> L = new ArrayList<Integer>();
			L.add(s);
			Itinerary I = new Itinerary(L);
			I.setDistance(0);
			return I;
		}
		H.clear();
		pre.clear();
		mD.clear();
		
		
		//boolean[] mark = new boolean[n+1];
		
		/*
		for(int v = 1; v <= n; v++){
			d[v] = new Element(v,infinity);
			pre[v] = -1;
			mark[v] = false;
		}
		*/
		//mark[s] = true;
		pre.put(s, s);
		if(A.get(s) == null){
			System.out.println("Dijkstra.solve(" + s + "," + t + ") failed, A.get(s = " + s + ") is null");
			
			log.close();
			System.exit(-1);
		}
		Iterator it = A.get(s).iterator();
		while(it.hasNext()){
			Arc e = (Arc)it.next();
			int v = e.end;
			Element de = new Element(v,e.w);
			mD.put(v, de);
			pre.put(v, s);
			//System.out.println("Dijkstra(" + s + "," + t + ") -> init pre.put(" + v + "," + s + ")");
			//if(log != null)log.println("Dijkstra(" + s + "," + t + ") -> init pre.put(" + v + "," + s + ")");
			H.insert(de);
			//d[v].key = e.w;
			//pre[v] = s;
			//H.insert(d[v]);
		}
		
		
		//System.out.println("Init H = "); H.print();
		double cost = infinity;// 99999999;
		while(H.size() > 0){
			//System.out.println("LOOP H = "); H.print();
			Element ev = findMin();
			ev.fixed = true;
			int v = ev.id;
			int key = composeKey(s,v);
			//mCost.put(key, ev.key);
			
			//mark[v] = true;
			//System.out.println("DijkstraBinaryHeap::solve, POP u = " + v + ", d = " + ev.key);
			//log.println("DijkstraBinaryHeap::solve, POP u = " + v + ", d = " + ev.key);
			//System.out.println("T.sz = " + H.size() + ", find v = " + v + ", d[v] = " + ev.key);
			//if(log != null)log.println("T.sz = " + H.size() + ", find v = " + v + ", d[v] = " + ev.key);
			if(v == t){
				//System.out.println("Found shortest path from s to s to t, length is d[t] = " + d[t].key);
				cost = ev.key;
				break;
			}
			it = A.get(v).iterator();
			while(it.hasNext()){
				Arc e = (Arc)it.next();
				int u = e.end;
				double w = e.w;
				//if(pre.get(u) == null){// update label of non-fixed vertices
					Element du = mD.get(u);
					if(du == null){
						du = new Element(u,ev.key + e.w);
						mD.put(u, du);
						pre.put(u, v);
						
						//if(u == 17347 || u == 23331){
							//System.out.println("DijkstraBinaryHeap::solve create element of " + u + " with D(" + u + ") = " + du.key);
						//}
						
						//if(log != null)log.println("pre.put(" + u + "," + v);
						H.insert(du);
						//if(u == 17347 || u == 23331) H.print();
					}else if(du.fixed == false){
						if(du.key > ev.key + e.w){
							//du.key = ev.key + e.w;
							H.updateKey(du, ev.key + e.w);
							pre.put(u, v);
							
							//if(u == 17347 || u == 23331){
								//System.out.println("DijkstraBinaryHeap::solve update D(" + u + ") = " + du.key);
							//}
							
							//System.out.println("pre.put(" + u + "," + v);
							//if(log != null)log.println("pre.put(" + u + "," + v);
						}
					}
					/*
					if(d[u].key > d[v].key + w){
						d[u].key = d[v].key + w;
						if(H.contains(d[u])){
							//System.out.println("DecreaseKey of " + u + ", old Key = " + d[u].key + ", new key = " + d[u].key);
							H.decreaseKey(d[u], d[u].key);
						}else{
							//System.out.println("Insert element " + u + ", key = " + d[u].key + " into Heap");
							H.insert(d[u]);
						}
						pre[u] = v;
						
					}
					*/
				//}
			}
			
			//if(v == 834){
				//System.out.println("DijkstraBinaryHeap::solve, D(907) = " + mD.get(907).key);
				//System.exit(-1);
			//}
			//if(v == 23331){
				//System.exit(-1);
			//}
			
		}
		if(cost > infinity-1){
			return null;
			/*
			Itinerary I = new Itinerary(new ArrayList<Integer>());
			I.setDistance(infinity);
			return I;// not found path
			*/
		}
		
		ArrayList<Integer> L = new ArrayList<Integer>();
		// print out the shortest path
		Stack S = new Stack<Integer>();
		int x = t;
		//while(pre.get(x) != s){
		while(true){
			S.push(x);
			x = pre.get(x);
			if(x == s) break;
			//System.out.println("push " + x);
		}
		S.push(s);
		
		while(S.size() > 0){
			int v = (Integer)S.pop();
			//System.out.print(v + " -> ");
			L.add(v);
		}
		Itinerary I = new Itinerary(L);
		I.setDistance(cost);
		return I;
	}
	public int[] getPath(int s, int t){
		Stack S = new Stack<Integer>();
		int x = t;
		while(true){
			S.push(x);
			x = pre.get(x);
			if(x == s) break;
			//System.out.println("push " + x);
		}
		S.push(s);
		int[] path = new int[S.size()];
		int idx = -1;
		while(S.size() > 0){
			int v = (Integer)S.pop();
			idx++;
			path[idx] = v;
			//System.out.print(v + " -> ");
		}
		return path;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//DijkstraBinaryHeap dijkstra = new DijkstraBinaryHeap();
		//dijkstra.readData("/Users/dungpq/tmp/dijkstra.in1");
		//dijkstra.solve();
	}

}

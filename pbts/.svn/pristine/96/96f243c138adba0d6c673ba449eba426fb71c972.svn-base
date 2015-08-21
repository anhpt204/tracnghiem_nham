package pbts.shortestpaths;

import java.util.*;
import java.io.*;

import pbts.entities.Arc;




public class Dijkstra {

	private int n;// number of vertices: 1, 2, ..., n
	private HashSet<Arc>[]	 A;// A[v] is the set of adjacent (outgoing) edges of v
	private int s, t;
	
	private double[] d;// d[v] will be the shortest distance from s to v
	private int[] 	 pre;// pre[v] will be the precedent vertex of v on the shortest path from s to v
	
	public static final double infinity = 10000000;
	public Dijkstra(){
		
	}
	public Dijkstra(int n, HashSet<Arc>[] A, int s, int t){
		this.n = n;
		this.A = A;
		this.s = s;
		this.t = t;
	}
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
	
	public int findMin(HashSet<Integer> T){
		int v_min = -1;
		double min = infinity;
		Iterator it = T.iterator();
		while(it.hasNext()){
			int v = (Integer)it.next();
			if(d[v] < min){
				min = d[v];
				v_min = v;
			}
		}
		return v_min;
	}
	public void solve(){
		s = 1; t = n;
		
		d = new double[n+1];
		pre = new int[n+1];
		
		for(int v = 1; v <= n; v++){
			d[v] = infinity;
			pre[v] = -1;
		}
		Iterator it = A[s].iterator();
		while(it.hasNext()){
			Arc e = (Arc)it.next();
			int v = e.end;
			d[v] = e.w;
			pre[v] = s;
		}
		HashSet<Integer> T = new HashSet<Integer>();
		for(int v = 1; v <= n; v++)
			if(v != s)
				T.add(v);
		
		while(T.size() > 0){
			int v = findMin(T);
			System.out.println("T.sz = " + T.size() + ", find v = " + v + ", d[v] = " + d[v]);
			T.remove(v);
			if(v == t){
				System.out.println("Found shortest path from s to s to t, length is d[t] = " + d[t]);
				break;
			}
			it = A[v].iterator();
			while(it.hasNext()){
				Arc e = (Arc)it.next();
				int u = e.end;
				double w = e.w;
				if(T.contains(u)){
					if(d[u] > d[v] + w){
						d[u] = d[v] + w;
						pre[u] = v;
					}
				}
			}
		}
		
		// print out the shortest path
		Stack S = new Stack<Integer>();
		int x = t;
		while(pre[x] != s){
			S.push(x);
			x = pre[x];
			//System.out.println("push " + x);
		}
		S.push(s);
		
		while(S.size() > 0){
			int v = (Integer)S.pop();
			System.out.print(v + " -> ");
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Dijkstra dijkstra = new Dijkstra();
		dijkstra.readData("/Users/dungpq/tmp/dijkstra.in1");
		dijkstra.solve();
	}

}

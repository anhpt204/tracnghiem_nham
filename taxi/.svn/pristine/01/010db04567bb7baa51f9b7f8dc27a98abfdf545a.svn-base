/**
 * 
 */
package org.matsim.contrib.sarp.util;


/**
 * @author Pham Quang Dung
 *
 */
public class CombinationGenerator
{
	/**
	 * @param args
	 */
	private int k;
	private int n;
	private int sz;// number of combinations
	private int[][] c;
	private int[] x;
	private int count;
	private int[][] d;
	
	public CombinationGenerator(int k, int n){
		//System.out.println("CombinationGeneration(" + k + "," + n + ")");
		this.k = k; this.n = n;
	}
	private void countSize(){
		d = new int[k+1][n+1];
		for(int i = 0; i <= k; i++)
			for(int j = 0; j <= n; j++)
				d[i][j] = -1;
		sz = computeD(k,n);
		//System.out.println("sz = " + sz);
	}
	private int computeD(int i, int j){
		if(i == 0 || i == j) d[i][j] = 1;
		if(d[i][j] < 0){
			d[i][j] = computeD(i-1,j-1) + computeD(i,j-1);
		}
		return d[i][j];
	}
	
	private void retrieve(){
		count++;
		//System.out.print("retrieve count = " + count + ", x = ");
		//for(int i = 1; i <= k ;i++) System.out.print(x[i] + ",");System.out.println();
		for(int i = 0; i < k; i++){
			c[count][i] = x[i+1]-1;
		}
	}
	private void TRY(int i){
		for(int v = x[i-1]+1; v <= n-k+i; v++){
			x[i] = v;
			if(i == k){
				retrieve();
			}else{
				TRY(i+1);
			}
		}
	}
	public void generate(){
		countSize();
		
		x = new int [k+1];
		x[0] = 0;
		c = new int[sz][k];
		
		count = -1;
		//System.out.println("sz = " + sz);
		TRY(1);
	}
	public int size(){ return sz;}
	public int[] get(int idx){
		return c[idx];
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CombinationGenerator C = new CombinationGenerator(4, 7);
		C.generate();
		for(int k = 0; k < C.size(); k++){
			int[] x = C.get(k);
			System.out.print(k + "th: ");
			for(int i = 0; i < x.length; i++)
				System.out.print(x[i] + ",");
			System.out.println();
		}
	}

}

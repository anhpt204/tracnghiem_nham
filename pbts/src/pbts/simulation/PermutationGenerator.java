package pbts.simulation;

public class PermutationGenerator {

	/**
	 * @param args
	 */
	private int	n;
	private int sz;// number of permutation of 0..n-1
	private int[] x;//
	private int[][] p;// p[i] is the ith permutation of 0..n-1
	private boolean[] marked;
	private int count;
	public PermutationGenerator(int n){
		this.n = n;
		sz = 1;
		for(int i = 1; i <= n; i++)
			sz = sz * i;
		x = new int[n];
		marked = new boolean[n];
		p = new int[sz][n];
	}
	private void retrieve(){
		count++;
		for(int i = 0; i < n; i++)
			p[count-1][i] = x[i];
	}
	private void TRY(int k){
		for(int v = 0; v < n; v++)
			if(!marked[v]){
				x[k] = v;
				marked[v] = true;
				if(k == n-1)
					retrieve();
				else
					TRY(k+1);
				marked[v] = false;
			}
	}
	public void generate(){
		for(int v = 0; v < n; v++)
			marked[v] = false;
		count = 0;
		TRY(0);
	}
	public int size(){
		return sz;
	}
	public int[] get(int k){
		return p[k];
	}
	public String getStr(int k){
		String s = "";
		for(int i = 0; i < n; i++)
			s = s + p[k][i] + ",";
		return s;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PermutationGenerator P = new PermutationGenerator(6);
		P.generate();
		for(int k = 0; k < P.size(); k++){
			int[] p = P.get(k);
			System.out.print("P[" + k + "] = ");
			for(int i = 0; i < 6; i++)
				System.out.print(p[i] + ",");
			System.out.println();
		}
	}

}

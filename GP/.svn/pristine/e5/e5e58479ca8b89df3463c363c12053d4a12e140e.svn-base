package Common;

public abstract  class MyRandom  implements Const {

	long		idum	= (long) -12345;
	long		IA		= (long) 16807;
	long		IM		= 2147483647;
	double	AM		= (1.0 / IM);
	long		IQ		= 127773;
	long		IR		= (long) 2836;
	long		NTAB	= 32;
	double	NDIV	= (1.0 + (IM - 1.0) / (double) NTAB);
	double	EPS	= 1.2e-7;
	double	RNMX	= (1.0 - EPS);
	long		iy		= 0;
	long[]	iv		= new long[(int) NTAB];//???
	
	public MyRandom()
	{}
	/*double PVAL(double y){
		if(y == HUGE_VAL) return INFPLUS;
		else {
			if(y == -HUGE_VAL) return -INFMINUS;
			else return y;
		}
	}*/

	protected double PVAL(double y){
		if(y >= HUGE_VAL)
			{// System.out.println("So Lon");
			return INFPLUS;}
		else {
			if(y <= -HUGE_VAL) 
				{
				   // System.out.println("So nho");
					return -INFMINUS;
				}
			
			else return y;
		}
	}
	
	//void Set_Seed(int x){
		//idum = (long) -x;
	//}

	protected void Set_Seed(long x){
		idum = -x;
	}
	
	protected double Next_Double(){
		int j;
		long k;
		double temp;
		//System.out.print("idum3="+getidum());
		if(idum <= 0 || iy == 0)//??? 
		{
			if(-idum < 1) idum = 1;
			else idum = -idum;
			for(j = (int) NTAB + 7; j >= 0; j--) {
				k = (long) (idum / (double) IQ);
				idum = IA * (idum - k * IQ) - IR * k;
				if(idum < 0) idum += IM;
				if(j < NTAB) iv[j] = idum;
			}
			iy = iv[0];
		}
		k = (long) (idum / (double) IQ);
		idum = IA * (idum - k * IQ) - IR * k;
		if(idum < 0) idum += IM;
		j = (int) (iy / NDIV);
		iy = iv[j];
		iv[j] = idum;
		if((temp = AM * iy) > RNMX) return RNMX;
		else return temp;
	}


//    double Next_Double()
//    {    	
//    	Random randomGenerator = new Random();
//    	double temp=randomGenerator.nextDouble();
//    	return temp;
//    	
//    }
    
    /*
	double Next_Gaussian(){
		int		iset	= 0;//???
		double	gset=0;//???
		double fac, rsq, v1, v2;
		if(iset == 0) {
			do {
				v1 = 2.0 * Next_Double() - 1.0;
				v2 = 2.0 * Next_Double() - 1.0;
				rsq = v1 * v1 + v2 * v2;
			} while(rsq >= 1.0 || rsq == 0.0);
			fac = Math.sqrt(-2.0 * Math.log(rsq) / (double) rsq);
			gset = v1 * fac;
			iset = 1;
			return v2 * fac;
		} else {
			iset = 0;
			return gset;
		}
	}

	double Next_Gaussian(double mean, double stdev){
		double x = (double) Next_Gaussian();
		return (double) (x * stdev + mean);
	}
*/

	protected byte Flip(double prob){
		double temp = Next_Double();
		if (temp<=prob) return 1;
		else
			return 0;		
	}
	/*
	byte FlipN(double prob){
		double temp = Next_DoubleN();
		if (temp<=prob) return 1;
		else
			return 0;		
	}
*/
	// return a random integer between lower and upper
	protected int IRandom(int lower, int upper){
		int temp;
		temp = lower + (int) (Next_Double() * (upper - lower + 1));
		return temp;
	}
	/*
	public int IRandomN(int lower, int upper){
		int temp;
		temp = lower + (int) (Next_DoubleN() * (upper - lower + 1));
		return temp;
	}
	*/
}


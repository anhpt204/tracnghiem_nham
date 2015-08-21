package common;

public interface Const {
	public static final int		MAXSTRING	= 60000;				// max of a tring (for _sbuffer)
	public static final byte	TRUE			= 1;
	public static final byte	FALSE			= 0;
	public static final int		MAXNAME		= 6;						// max+1 length of name of a symbol
	public static final int		MAXDEPTH		= 15;					// max size of chromosome
	public static final int		MAXTOUR		= 10;					// max tournement size
	public static final int		MAXATEMPT	= 100;					// max atempt for choosing a site of a chromosome
	public static final int		MAXFUNCTION	= 20;
	public static final int		MAXTERMINAL	= 20;
	public static final int		MAXNODE		= 3000;
	public static final double	VOIDVALUE	= -1523612789.21342;
	
	
	public final double PCROSS = 0.9;
	public final double PMUTATE = 0.1;
	
    public final int TOURSIZE=7;
    public final int MAX_KOZA=5;
    			
    public final int		NUMSIZE	=10;
	public final double		USIM	=0.6;
	public final double		LSIM	=0.0;
	

	public static final int NRUN=5;
	public static final int POPSIZE=500;
    public static final int NUMGEN=20;
    
	
	public static final double	INFPLUS		=  Math.exp(700);
	public static final double	INFMINUS		= -Math.exp(700);
	public static final double	HUGE_VAL		= Math.exp(700);
	
	//SGX + SC
	public static final int NUM_TRIAL = 20;
	public static final double SGXMSC_PRO = 0.3;
	public final int SUBTREE_MAXSIZE = 80;
	public final int SUBTREE_MINSIZE = 2;
	public final int SSGX_TR_MAXDEPTH = 2;
	public final int SGX_TR_MAXDEPTH = 4;
	
	//AGX RDO
	public final int TREELIBSIZE = 1000;
	public final int TREELIB_MAXDEPTH = 4;
	public final double STAR = Double.NaN;
	public final boolean KOZA_MUTATION = true;
	
	// Problems	
	public static final String PROBLEM = "keijzer-4";
	public static final int		NUMVAR	=1;
	public static final int		NUMFITCASE	= 100;
	public static final int		NUMFITTEST	= 100;

//	public static final String PROBLEM = "slump_test_SLUMP";//7 50 53
//	public static final int		NUMVAR	=7;
//	public static final int		NUMFITCASE	= 50;
//	public static final int		NUMFITTEST	= 53;

//	public static final String PROBLEM = "ccpp";//4 200 200
//	public static final int		NUMVAR	=4;
//	public static final int		NUMFITCASE	= 200;
//	public static final int		NUMFITTEST	= 200;

//	public static final String PROBLEM = "winequality-red";//11 250 250
//	public static final int		NUMVAR	=11;
//	public static final int		NUMFITCASE	= 250;
//	public static final int		NUMFITTEST	= 250;

//	public static final String PROBLEM = "winequality-white";//11 300 300
//	public static final int		NUMVAR	=11;
//	public static final int		NUMFITCASE	= 300;
//	public static final int		NUMFITTEST	= 300;

	public final String DATADIR = "./data/";
}

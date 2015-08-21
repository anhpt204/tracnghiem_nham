package agx;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import common.MyRandom;



public class Population extends MyRandom {
	//mang chua quan the cu va moi
	individual[]	oldpop, newpop;												
	int				poplen;			//so ca the											
	double			pcross, pmutate, pcopy;		//xac suat lai ghep va dot bien							
	long				ncross, nmutate;//thong ke so lan lai ghep va so lan dot bien
	static int				gltermcard;//so ham va terminal										
	static int				glfcard;//so ham
	static int				gltcard;//so terminal
	term[]			glterm		= new term[MAXFUNCTION + MAXTERMINAL];//mang luu ham va terminal
	term[]			glfunction	= new term[MAXFUNCTION];//mang luu ham
	term[]			glterminal	= new term[MAXTERMINAL];//mang luu terminal
	sample[]	fitcase			= new sample[NUMFITCASE];//mang luu du lieu dau vao
	sample[]	fittest			= new sample[NUMFITTEST];//mang luu du lieu dau vao
	double[] trainingOutput = new double[NUMFITCASE];
	byte		SuccPredicate	= FALSE;//danh gia thanh cong hay khong?
	int				generation, gen = 0;
	individual[]	bestcurrent;
	double[]			average;
	
	// for AGX and RDO
	private Hashtable<double[], node> treelib = new Hashtable<double[], node>();

	long invertTime;
	long treelibSearchTime;
	long treelibGenerateTime;
	public double[] constructiveRate;
	public double[] semanticDistance;

// **************************************************
//Lop chua du lieu dau vao
	class sample {
		double x[]=new double[NUMVAR];
		double	y;
	}
//Lop chua cac toan tu va terminal 
	class term {
		String	name;
		byte	arity;
	};
// Ham tao cho quan the, dat seed	
	public Population(long seed) {
		super.Set_Seed(seed);//goi ham Set_Seed trong lop Random
		//Khoi tao cac bien dem =0
		gltermcard = 0;
		glfcard = 0;
		gltcard = 0;
		//Khoi tao cac mang chau term
		for(int i = 0; i < MAXFUNCTION + MAXTERMINAL; i++)
			glterm[i] = new term();
		for(int i = 0; i < MAXTERMINAL; i++)
			glterminal[i] = new term();
		for(int i = 0; i < MAXFUNCTION; i++)
			glfunction[i] = new term();
		//goi phuong thuc addterm de them cac ham va terminal vao mang
		AddTerm("add", (byte) 2);
		AddTerm("sub", (byte) 2);
		AddTerm("div", (byte) 2);
		AddTerm("mul", (byte) 2);
		AddTerm("sin", (byte) 1);
		AddTerm("cos", (byte) 1);
		AddTerm("ep", (byte) 1);
		AddTerm("log", (byte) 1);
	//	AddTerm("sqrt", (byte) 1);
	//	AddTerm("sqr", (byte) 1);
		for(int i=0;i<NUMVAR;i++){			
			
    		AddTerm("X"+String.valueOf(i+1), (byte) 0);
		}
		
		//this.PrintTerm();
		//hoan doi ngau nhien cac term 100 lan
		SwapTerm(100);
		//this.PrintTerm();
		//dat so luong quan the
		poplen = POPSIZE;
		//khoi tao quan the moi va cu
		oldpop = new individual[poplen];
		assert (oldpop != null);
		newpop = new individual[poplen];
		assert (newpop != null);
		for(int i = 0; i < poplen; i++) {
			oldpop[i] = new individual();
			newpop[i] = new individual();
		}
		// dat xac suat cho dot bien va lai ghep
		pcross = PCROSS;
		pmutate = PMUTATE;
		// Khoi tao bien thong ke so lan dot bien va lai ghep
		ncross = 0;
		nmutate = 0;
		// Dat so generation
		generation = NUMGEN;
		//khoi tao mang de luu cac doi tuong tot nhat trong moi the he
		bestcurrent = new individual[generation];
		assert (bestcurrent != null);
		for(int i=0; i<generation; i++)
			bestcurrent[i]=new individual();
		//mang de luu cac fitness cua bestcurrent
		average = new double[generation];			
		assert (average != null);
		SuccPredicate = FALSE;
		SetFitCase();//Goi phuong thuc khoi tao du lieu
		SetFitTest();
		
		invertTime = 0;
		//treelibGenerateTime = 0;
		treelibSearchTime = 0;
		
		long tmp = System.currentTimeMillis();
		//System.err.println("generate treelib");
		genTreelibWithDistinctSemantics(1000, 4);
		
		tmp = System.currentTimeMillis() - tmp;
		
		treelibGenerateTime = tmp;
		//System.err.println("OK");
	}
	
	// Phuong thuc them cac function va terminal vao mang
	void AddTerm(String name, byte arity){
		glterm[gltermcard].arity = arity;
		glterm[gltermcard].name = name;
		gltermcard++;
		if(arity == 0) {//truong hop la terminal
			glterminal[gltcard].arity = arity;
			glterminal[gltcard].name = name;
			gltcard++;
		} else {//truongg hop la function
			glfunction[glfcard].arity = arity;
			glfunction[glfcard].name = name;
			glfcard++;
		}
	}
//in ra cac term len man hinh
	void PrintTerm(){
		int i;
		for(i = 0; i < gltermcard; i++) {
			System.out.println("--------TERM " + i + "---------");
			System.out.println("NAME:" + glterm[i].name);
			System.out.println("ARITY:" + (int) glterm[i].arity);
		}
	}
	// Hoan doi cac term times lan
	void SwapTerm(int times){
		int i, j, k;
		term temp;
		assert (gltermcard >= 2);
		for(i = 0; i < times; i++) {
			j = IRandom(0, gltermcard - 1);
			k = IRandom(0, gltermcard - 1);
			temp = glterm[j];
			glterm[j] = glterm[k];
			glterm[k] = temp;
		}
	}

	//Xay dung lop Individual
	class individual {
		node		chrom;	// thuoc tinh chrom cua individual
		int			size, height;	// size and heigh cua individual
		double		branch;			// nhanh cua individual
		double		fitness;	
		double    oldfitness;
		double[] semanticTraining;
		double[] semanticTesting;
		byte evaluated;
		// fitness cua indinvidual
//ham tao cua lop individual
       public individual() {
			size = 0;
			height = 0;
			branch = 0;
			fitness = 0;
			oldfitness=0;
			semanticTraining = new double[NUMFITCASE];
			semanticTesting = new double[NUMFITTEST];
			evaluated=FALSE;
			chrom = new node();
		}
//ham copyIndividual
		public void CopyIndividual(individual t, byte copychrom){
			if(copychrom == TRUE)//truong hop copy den vung nho khac 
				this.chrom = chrom.CopyNode(t.chrom);
			else 
				//truong hop trung vung nho
				this.chrom = t.chrom;			
			this.size = t.size;
			this.height = t.height;
			this.fitness = t.fitness;
			this.branch = t.branch;
			this.oldfitness=t.oldfitness;
			this.semanticTraining = t.semanticTraining.clone();
			this.semanticTesting = t.semanticTesting.clone();
			this.evaluated=t.evaluated;
		}
		//phuong thuc xoa individual
		void DeleteChrom(node t){
			node q, p;
			
			if(t!=null)
			{
			if(t.children==null)
				t=null;
			else
			{
				p=t.children;
				while(p!=null)
				{
					q=p.sibling;
					DeleteChrom(p);
					p=q;
				}
				t=null;
			}
			}
			t=null;
		}
//hien thi mot individual ra man hinh
		void DisplayIndividual(){
			System.out.println("-----------------------------");
			System.out.println("Genotype Structure:");
			//go phuong thuc bien doi cay thanh mot xau
			this.chrom.DisplaySTree(this.chrom);
			System.out.println("Size:" + this.size);
			System.out.println("Height:" + this.height);
			System.out.println("Branching:" + this.branch);
			System.out.println("Fitness:" + this.fitness);
		}
		
		void DisplayIndividualChrom(){
			this.chrom.DisplaySTree(this.chrom);			
		}
		
		public boolean isBetterThan(individual otherInd)
		{
			if (ComputeRF(this) < ComputeRF(otherInd))
				return true;
			else
				return false;
		}
		
		public double getSemanticDistance(individual individual)
		{
			double sd = 0.0;
			
			double[] s1 = ComputeNew(this.chrom);
			double[] s2 = ComputeNew(individual.chrom);
			
			for(int i = 0; i < s1.length; i++)
			{
				if(Math.abs(s1[i]) > 10000
						|| Math.abs(s2[i]) > 10000)
					continue;
				
				sd += Math.abs(s1[i]-s2[i]);
			}
			
			return sd/NUMFITCASE;
		}
		
	}
	
	
//phuong thuc phat trien cay voi do cao toi da ma maxdepth
	node GrowthTreeGen(int maxdepth){
		int i;
		byte a;
		node t, p;
		if(maxdepth == 0) // to the limit then choose a terminal
		{
			i = IRandom(0, gltcard - 1);
			t = new node(glterminal[i].name, VOIDVALUE);
			return t;
		} 
		else // choosing from function and terminal
		 {
			i = IRandom(0, gltermcard - 1);
			t = new node(glterm[i].name, VOIDVALUE);
			if(glterm[i].arity > 0) // if it is function
			{
				t.children = GrowthTreeGen(maxdepth - 1);
				p = t.children;
				p.parent = t;
				
				for(a = 1; a < glterm[i].arity; a++) {
					p.sibling = GrowthTreeGen(maxdepth - 1);
					p = p.sibling;
					p.parent = t;
				}
				p.sibling = null;
			}
			return t;
		}
	}
//phuong thuc phat trien cay day du
node FullTreeGen(int maxdepth){
		int i;
		byte a;
		node t, p;
		if(maxdepth == 0) // to the limit then choose a terminal
		{
			i = IRandom(0, gltcard - 1);
			t = new node(glterminal[i].name, VOIDVALUE);
			return t;
		} 
		else // choosing from function
		{
			i = IRandom(0, glfcard - 1);
			t = new node(glfunction[i].name, VOIDVALUE);
			t.children = FullTreeGen(maxdepth - 1);
			p = t.children;
			p.parent = t;
			for(a = 1; a < glfunction[i].arity; a++) {
				p.sibling = FullTreeGen(maxdepth - 1);
				p = p.sibling;
				p.parent = t;
			}
			p.sibling = null;
			return t;
		}
	}
//Khoi tao quan the dau tien
	void RampedInit(int depth, double percentage)
	{
		int i, j;
		byte k;
		node t, p;
		for(i = 0; i < poplen; i++) {
			// choose randomly from the function set
			j = IRandom(0, glfcard - 1);
			t = new node(glfunction[j].name, VOIDVALUE);
			if(Next_Double() < percentage)// Growth
			{
				t.children = GrowthTreeGen(depth - 1);
				p = t.children;
				p.parent = t;
				for(k = 1; k < glfunction[j].arity; k++) {
					p.sibling = GrowthTreeGen(depth - 1);
					p = p.sibling;
					p.parent = t;
				}
				p.sibling = null;
			} 
			else 
			{
				t.children = FullTreeGen(depth - 1);
				p = t.children;
				p.parent = t;
				for(k = 1; k < glfunction[j].arity; k++) {
					p.sibling = FullTreeGen(depth - 1);
					p = p.sibling;
					p.parent = t;
				}
				p.sibling = null;
			}
			oldpop[i].chrom = t;
			oldpop[i].size = t.GetSizeNode(t, TRUE);
			oldpop[i].height = t.GetHeightNode();
			oldpop[i].branch = t.GetAVGNode(t);
		}
	}
//
	void AdjustFitness(){
		int i;
		for(i = 0; i < poplen; i++)
			oldpop[i].fitness = 1 / (1 + oldpop[i].fitness);
	}
//cho fitness be hon 1
	void NormalizeFitness(){
		double sum = 0;
		int i;
		for(i = 0; i < poplen; i++)
			sum += oldpop[i].fitness;
		for(i = 0; i < poplen; i++)
		{
			oldpop[i].fitness = oldpop[i].fitness / sum;
		//	System.out.print(oldpop[i].fitness+" ");
	}
	}
// Tournement Selection
	int TourSelect(int size){
		int i, j, pos = 0;
		double max = -1000;
		assert (size <= MAXTOUR);
		for(i = 0; i < size; i++) {
			j = IRandom(0, poplen - 1);
			//System.out.print("\n"+j+"="+oldpop[j].fitness);
			if(oldpop[j].fitness > max) {
				max = oldpop[j].fitness;
				pos = j;
			}
		}
		//System.out.print("pos="+oldpop[pos].fitness+"\n");
		return pos;
	}
	
	byte AGX(individual parent1, individual parent2, individual child1, individual child2)
	{
		node cxNode1, cxNode2;
		
		individual temp1 = new individual();
		individual temp2 = new individual();
		
		int cxPoint1, cxPoint2;
		int count = 0;
		
		// get mean semantic of parents
		double[] target = new double[NUMFITCASE];
		double[] s1 = ComputeNew(parent1.chrom);
		double[] s2 = ComputeNew(parent2.chrom);
		for(int i = 0; i < NUMFITCASE; i++)
			target[i] = (s1[i] + s2[i])/2.0;
		
		while(count < MAXATEMPT)
		{
			//System.err.println("count: " + count);
			
			temp1.CopyIndividual(parent1, TRUE);
			temp2.CopyIndividual(parent2, TRUE);
			
			cxPoint1 = IRandom(2, temp1.size);
			cxPoint2 = IRandom(2, temp2.size);
			
			temp1.chrom.LocateNode(cxPoint1, temp1.chrom, null);
			cxNode1 = temp1.chrom._idnode;
			
			temp2.chrom.LocateNode(cxPoint2, temp2.chrom, null);					
			cxNode2 = temp2.chrom._idnode;
			
    		//System.err.println(temp1.chrom.TreeToStringN(temp1.chrom));
    		//System.err.println(cxNode1.TreeToStringN(cxNode1));

    		//System.err.println(temp2.chrom.TreeToStringN(temp2.chrom));
    		//System.err.println(cxNode2.TreeToStringN(cxNode2));

			//parent.chrom.DisplaySTree(parent.chrom);
			//temp.chrom.DisplaySTree(temp.chrom);
			//p.DisplaySTree(p);
			long tmpTime = System.currentTimeMillis();
			
            ArrayList<Double>[] desiredSemantics1 = semanticBackPropagation(target, temp1.chrom, cxNode1);
            ArrayList<Double>[] desiredSemantics2 = semanticBackPropagation(target, temp2.chrom, cxNode2);
            
            tmpTime = System.currentTimeMillis() - tmpTime;
            invertTime += tmpTime;
            
            if (desiredSemantics1 == null || desiredSemantics2 == null)
            	continue;
            
            try {            		

            	tmpTime = System.currentTimeMillis();

            	double[] bestConstants1 = getBestConstant(desiredSemantics1);

            	node newNode1 = getBestNode(desiredSemantics1, target, bestConstants1);
            	
            	double[] bestConstants2 = getBestConstant(desiredSemantics2);

            	node newNode2 = getBestNode(desiredSemantics2, target, bestConstants2);
            	
            	tmpTime = System.currentTimeMillis() - tmpTime;
            	treelibSearchTime += tmpTime;
            	
            	//p2.DisplaySTree(p2);
            	
        		if(newNode1 == null || newNode2==null)
            		continue;
        		        		
        		//make new child1
        		node parentNode1 = cxNode1.parent;
        		node tmp = parentNode1.children;
        		
//        		if(tmp.equals(cxNode1) && ((tmp.sibling==null && cxNode1.sibling == null)||
//        				(tmp.sibling!= null && tmp.sibling.equals(cxNode1.sibling)))) // first child
        		if(tmp == cxNode1)
        		{
        			parentNode1.children = newNode1;
        			
        		}
        		else 
        		{
        			//while(!tmp.sibling.equals(cxNode1))
        			while(tmp.sibling != cxNode1)
        				tmp = tmp.sibling;
        			
        			tmp.sibling = newNode1;
        			
        		}
    			newNode1.sibling = cxNode1.sibling;//.CopyNode(cxNode1.sibling);
        		newNode1.parent = cxNode1.parent;//.CopyNode(cxNode1.parent);

        		//System.err.println(temp1.chrom.TreeToStringN(temp1.chrom));

        		temp1.height = temp1.chrom.GetHeightNode();
        		if(temp1.height <= MAXDEPTH)
        		{
        			child1.CopyIndividual(parent1, FALSE);
        			child1.chrom=null;
        			child1.chrom = temp1.chrom.CopyNode(temp1.chrom);
        			child1.height=temp1.height;
        		}
        		else
        			return FALSE;
    			//System.err.println(child1.chrom.TreeToStringN(child1.chrom));

        		
        		//make new child2
        		node parentNode2 = cxNode2.parent;
        		node tmp2 = parentNode2.children;
        		
//        		if(tmp2.equals(cxNode2) && ((tmp2.sibling == null && cxNode2.sibling==null)||
//        				(tmp2.sibling != null && tmp2.sibling.equals(cxNode2.sibling)))) // first child
        		if(tmp2 == cxNode2)
        		{
        			parentNode2.children = newNode2;

        		}
        		else 
        		{
        			//while(!tmp2.sibling.equals(cxNode2))
        			while(tmp2.sibling != cxNode2)
        				tmp2 = tmp2.sibling;
        			tmp2.sibling = newNode2;
        		}
    			newNode2.sibling = cxNode2.sibling;
        		newNode2.parent = cxNode2.parent;
    			

        		//temp.chrom.DisplaySTree(temp.chrom);
        		//System.err.println(temp1.chrom.TreeToStringN(temp1.chrom));
        		//System.err.println(child1.chrom.TreeToStringN(child1.chrom));
        		
        		//System.err.println(temp2.chrom.TreeToStringN(temp2.chrom));

        		//p=null;
        		temp2.height = temp2.chrom.GetHeightNode();
        		
        		if((temp1.height <= MAXDEPTH) && (temp2.height <= MAXDEPTH)) 
        		{        			
        			child2.CopyIndividual(parent2, FALSE);
        			
//        			child.chrom.DeleteNode(child.chrom);
        			child2.chrom = null;
        			
        			child2.chrom = temp2.chrom;
        			
        			child2.height = temp2.height;
        			
        			child1.size=child1.chrom.GetSizeNode(child1.chrom, TRUE);
        			child2.size=child2.chrom.GetSizeNode(child2.chrom, TRUE);

        			child1.branch=child1.chrom.GetAVGNode(child1.chrom);
        			child2.branch=child2.chrom.GetAVGNode(child2.chrom);

        			return TRUE;
        		}
            			
        	}
        	catch (Exception e) {
        		child1.CopyIndividual(parent1, FALSE);
        		child2.CopyIndividual(parent2, FALSE);
        		return TRUE;

        	}
            
            count++;

		}
		return FALSE;
	}
		

//phuong thuc crossover
	byte SubTreeSwap(individual parent1, individual parent2, individual[] child,int[] cp,node[] nd)
	{
		individual temp1, temp2;// hai bien tam de luu hai parent		
		temp1 = new individual();
		temp2 = new individual();
		int count, crosspoint1, crosspoint2;
		node node1, node2, t;
		node1=new node();node2=new node();t=new node();
		double valuetemp;
		String nametemp;
		count = 0;
		//so lan thu mutation
		while(count < MAXATEMPT) {
			// copy the parents
			temp1.CopyIndividual(parent1, TRUE);
			temp2.CopyIndividual(parent2, TRUE);
			// Chon ngau nhien diem de lai ghep
			crosspoint1 = IRandom(2, temp1.size);
			crosspoint2 = IRandom(2, temp2.size);
			temp1.chrom.LocateNode(crosspoint1, temp1.chrom, null);
			
			node1=temp1.chrom._idnode; 
			temp2.chrom.LocateNode(crosspoint2, temp2.chrom, null);
			node2=temp2.chrom._idnode;
			nametemp = node1.name;
			node1.name = node2.name;
			node2.name = nametemp;
			valuetemp = node1.value;
			node1.value = node2.value;
			node2.value = valuetemp;
			// then swap the children
			t = node1.children;
			node1.children = node2.children;
			node2.children = t;
			// compute the heigh after that
			temp1.height = temp1.chrom.GetHeightNode();
			temp2.height = temp2.chrom.GetHeightNode();
			if((temp1.height <= MAXDEPTH) && (temp2.height <= MAXDEPTH)) {
				temp1.size = temp1.chrom.GetSizeNode(temp1.chrom, TRUE);
				temp1.branch=temp1.chrom.GetAVGNode(temp1.chrom);
				temp2.size=temp2.chrom.GetSizeNode(temp2.chrom, TRUE);
				temp2.branch=temp2.chrom.GetAVGNode(temp2.chrom);
				child[0]=temp1;
				child[1]=temp2;
				cp[0]=crosspoint1;
				cp[1]=crosspoint2;
				nd[0]=node1;
				nd[1]=node2;
				return TRUE;
			} else {
//				temp1.DeleteChrom();
//				temp1=null;				
//				temp2.chrom.DeleteChrom(temp1.chrom);
//				temp2.DeleteChrom();
//				temp2=null;
			}
			count++;
		}
		return FALSE;
	}
	// **************************************************
	//dot bien
	byte ReplaceSubTree(individual child,individual[] copychild, int maxdepth, byte type,int[] mt,node[] nm)
	{
		individual temp = new individual();
		int replacepoint, count;
		node node1=new node(), parentnode1=new node(), t=new node(), p=new node();
		count = 0;
		
		while(count < MAXATEMPT) {
			temp=new individual();
			temp.CopyIndividual(child, TRUE);
			replacepoint = IRandom(2, temp.size);
			temp.chrom.LocateNode(replacepoint, temp.chrom, null);
			
			node1=temp.chrom._idnode;
			parentnode1=temp.chrom._idnodeparent;
			
			if(type == TRUE) // growth
			  t = GrowthTreeGen(maxdepth);
			else t = FullTreeGen(maxdepth);
			nm[0]=t;
		
			p = parentnode1.children;
			if(p == node1) // first child
			parentnode1.children = t;
			else {
				while(p.sibling != node1)
					p = p.sibling;
				p.sibling = t;
			}
			t.sibling = node1.sibling;
//			node1.DeleteNode(node1);
//			temp.DeleteChrom(node1);
			node1=null;
			temp.height = temp.chrom.GetHeightNode();
			
			if(temp.height <= MAXDEPTH) {
//				child.chrom.DeleteNode(child.chrom);
				child.chrom=null;
				child.chrom=temp.chrom;
//				if(status==0)
//				{
//				//  System.out.print("\n------------------------mutation---\n");
//				//  child.chrom.DisplaySTree(temp.chrom);
//				}
				child.height=temp.height;
				child.size=child.chrom.GetSizeNode(child.chrom, TRUE);
				temp.branch=child.chrom.GetAVGNode(child.chrom);
				//copychild[0].CopyIndividual(child, TRUE);
				copychild[0]=child;
				mt[0]=replacepoint;
				return TRUE;
			}
//			temp.chrom.DeleteNode(temp.chrom);
			temp=null;
			count++;
		}
		return FALSE;
	}

//phuong thuc doc du lieu
	void SetFitCase(){
		String csvFile = DATADIR + PROBLEM +".training.in";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = " ";
		
		int i,j;
		
		try {
			 
			br = new BufferedReader(new FileReader(csvFile));
			
			line = br.readLine();//read the first line
			
			i=0;
			while (((line = br.readLine()) != null)&&(i<NUMFITCASE)) {
	 
			        // use comma as separator)
				String[] dataline = line.split(cvsSplitBy);
				fitcase[i]=new sample();
				for(j=0;j<NUMVAR;j++)
				{
					   fitcase[i].x[j]= Double.parseDouble(dataline[j]);
				}
				fitcase[i].y= Double.parseDouble(dataline[NUMVAR]);
				trainingOutput[i] = fitcase[i].y;
				i=i+1;
	 
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
void PrintFitCase()
{
	int i;
	for(i=0;i<NUMFITCASE;i++)
		System.out.print(fitcase[i].x+"|"+fitcase[i].y+"\n");
}
	// ********************************************************************
void SetFitTest(){
	String csvFile = DATADIR + PROBLEM +".testing.in";
	BufferedReader br = null;
	String line = "";
	String cvsSplitBy = " ";
	
	int i,j;
	
	try {
		 
		br = new BufferedReader(new FileReader(csvFile));
		line = br.readLine();
		i=0;
		while (((line = br.readLine()) != null)&&(i<NUMFITTEST)) {
 
		        // use comma as separator)
			String[] dataline = line.split(cvsSplitBy);
			fittest[i]=new sample();
			for(j=0;j<NUMVAR;j++)
			{
				   fittest[i].x[j]= Double.parseDouble(dataline[j]);
			}
			fittest[i].y= Double.parseDouble(dataline[NUMVAR]);
			i=i+1;
 
		}
 
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
double GenerateER(String name)
{
 if(name.equals("ERC"))
   return -1+2*Next_Double();
return VOIDVALUE;
}

//phuong thuc compute cho mot node
double[] ComputeNew(node st){
	node p;
	double[] l;
	double[] r;
	double[] result=new double[NUMFITCASE];
	
	for(int j=0;j<NUMVAR;j++){		
		
		if(st.name.equals("X"+String.valueOf(j+1))){
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]=fitcase[i].x[j];
			}
			return result;
		}
	}
	
	if((st.name == "1")){
		for(int i=0; i<NUMFITCASE; i++)
		{
			result[i]=1;
		}
		return result;
	}
	else if(st.name == "0")
	{
		for(int i=0; i<NUMFITCASE; i++)
		{
			result[i]=0;
		}
		return result;
	}
	else
		if(st.name=="ERC") {
			if(st.value==VOIDVALUE) //if it has not been initialized then initialized and return the value
			    st.value=GenerateER(st.name);      
		//	  st.att[i]=st.value;
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]=st.value; 
			}
			return result;			  
		}	
	else // st.name="EXP"
	{
		p = st.children;
		if(st.name == "add") {
			l = ComputeNew(p);
			r = ComputeNew(p.sibling);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]=PVAL(l[i] + r[i]);
			}
			return result;
		} else if(st.name == "sub") {
			l = ComputeNew(p);
			r = ComputeNew(p.sibling);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]=PVAL(l[i] - r[i]);
			}
			return result;
		} else if(st.name == "mul") {
			l = ComputeNew(p);
			r = ComputeNew(p.sibling);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]=PVAL(l[i] * r[i]);
			}
			return result;
		} else if(st.name == "div") {
			l = ComputeNew(p);
			r = ComputeNew(p.sibling);
			for(int i=0; i<NUMFITCASE; i++)
			{
				if(r[i] == 0) result[i]= 1;
				else result[i]= PVAL(l[i]/r[i]);				
			}
			return result;
		
		} else if(st.name == "sin") {
			l = ComputeNew(p);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]= Math.sin(l[i]);				
			}
			return result;
			
			
		} else if(st.name == "cos") {
			l = ComputeNew(p);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]= Math.cos(l[i]);				
			}
			return result;
			
		} else if(st.name == "sqrt") {
			l = ComputeNew(p);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]= Math.sqrt(Math.abs(l[i]));			
			}
			return result;			
		} else if(st.name == "sqrt") {
			l = ComputeNew(p);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]= PVAL(l[i]*l[i]);			
			}
			return result;
			
		} else if(st.name == "ep") {
			l = ComputeNew(p);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]= PVAL(Math.exp(l[i]));	
			}
			return result;			
		} else {
			l = ComputeNew(p);
			for(int i=0; i<NUMFITCASE; i++)
			{
				result[i]= PVAL(Math.exp(l[i]));
				if(l[i] == 0) result[i]= 0;
				else result[i]= PVAL(Math.log(Math.abs(l[i])));// fido fabs
			}
			return result;	
			
		}
	}
}

double ComputeOne(node st, sample input)
{
	node p;
	
	double l;
	double r;
	double result;//=new double[NUMFITCASE];
	
	for(int j=0;j<NUMVAR;j++)
	{		
		
		if(st.name.equals("X"+String.valueOf(j+1)))
		{
			result=input.x[j];
			return result;
		}
	}
	
	if((st.name == "1"))
	{
		return 1.0;
	}
	else if(st.name == "0")
	{		
		return 0.0;
	}
	else
		if(st.name=="ERC") {
			if(st.value==VOIDVALUE) //if it has not been initialized then initialized and return the value
			    st.value=GenerateER(st.name);      
		//	  st.att[i]=st.value;
			
			return st.value;			  
		}	
	else // st.name="EXP"
	{
		p = st.children;
		if(st.name == "add") {
			l = ComputeOne(p, input);
			r = ComputeOne(p.sibling, input);
			
			result=PVAL(l + r);
			
			return result;
			
		} else if(st.name == "sub") {
			l = ComputeOne(p, input);
			r = ComputeOne(p.sibling, input);
			
			result=PVAL(l - r);
			
			return result;
			
		} else if(st.name == "mul") {
			l = ComputeOne(p, input);
			r = ComputeOne(p.sibling, input);
			
			result=PVAL(l * r);
			
			return result;
			
		} else if(st.name == "div") {
			l = ComputeOne(p, input);
			r = ComputeOne(p.sibling, input);
			if(r == 0) result= 1;
			else result= PVAL(l/r);				

			return result;
		
		} else if(st.name == "sin") {
			l = ComputeOne(p, input);
			result= Math.sin(l);				
			return result;
			
			
		} else if(st.name == "cos") {
			l = ComputeOne(p, input);
			result= Math.cos(l);				
			return result;
			
		} else if(st.name == "sqrt") {
			l = ComputeOne(p, input);
			result= Math.sqrt(Math.abs(l));			
			return result;			
		} else if(st.name == "sqrt") {
			l = ComputeOne(p, input);
			result= PVAL(l*l);			
			return result;
			
		} else if(st.name == "ep") {
			l = ComputeOne(p, input);
			result= PVAL(Math.exp(l));	
			return result;			
		} else {
			l = ComputeOne(p, input);
				result= PVAL(Math.exp(l));
				if(l == 0) result= 0;
				else result= PVAL(Math.log(Math.abs(l)));// fido fabs
			}
			return result;	
			
		}
}

double[] ComputeTest(node st){
	node p;
	double[] l;
	double[] r;
	double[] result=new double[NUMFITTEST];
	
	for(int j=0;j<NUMVAR;j++){		
		
		if(st.name.equals("X"+String.valueOf(j+1))){
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]=fittest[i].x[j];
			}
			return result;
		}
	}
	
	if((st.name == "1")){
		for(int i=0; i<NUMFITTEST; i++)
		{
			result[i]=1;
		}
		return result;
	}
	else if((st.name == "0")){
		for(int i=0; i<NUMFITTEST; i++)
		{
			result[i]=0;
		}
		return result;
	}
	else
		if(st.name=="ERC") {
			if(st.value==VOIDVALUE) //if it has not been initialized then initialized and return the value
			    st.value=GenerateER(st.name);      
		//	  st.att[i]=st.value;
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]=st.value; 
			}
			return result;			  
		}	
	else // st.name="EXP"
	{
		p = st.children;
		if(st.name == "add") {
			l = ComputeTest(p);
			r = ComputeTest(p.sibling);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]=PVAL(l[i] + r[i]);
			}
			return result;
		} else if(st.name == "sub") {
			l = ComputeTest(p);
			r = ComputeTest(p.sibling);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]=PVAL(l[i] - r[i]);
			}
			return result;
		} else if(st.name == "mul") {
			l = ComputeTest(p);
			r = ComputeTest(p.sibling);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]=PVAL(l[i] * r[i]);
			}
			return result;
		} else if(st.name == "div") {
			l = ComputeTest(p);
			r = ComputeTest(p.sibling);
			for(int i=0; i<NUMFITTEST; i++)
			{
				if(r[i] == 0) result[i]= 1;
				else result[i]= PVAL(l[i]/r[i]);				
			}
			return result;
		
		} else if(st.name == "sin") {
			l = ComputeTest(p);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]= Math.sin(l[i]);				
			}
			return result;
			
			
		} else if(st.name == "cos") {
			l = ComputeTest(p);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]= Math.cos(l[i]);				
			}
			return result;
			
		} else if(st.name == "sqrt") {
			l = ComputeTest(p);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]= Math.sqrt(Math.abs(l[i]));			
			}
			return result;			
		} else if(st.name == "sqr") {
			l = ComputeTest(p);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]= PVAL(l[i]*l[i]);			
			}
			return result;
			
		} else if(st.name == "ep") {
			l = ComputeTest(p);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]= PVAL(Math.exp(l[i]));	
			}
			return result;			
		} else {
			l = ComputeTest(p);
			for(int i=0; i<NUMFITTEST; i++)
			{
				result[i]= PVAL(Math.exp(l[i]));
				if(l[i] == 0) result[i]= 0;
				else result[i]= PVAL(Math.log(Math.abs(l[i])));// fido fabs
			}
			return result;	
			
		}
	}
}


	// *********************************************
	//phuong thuc tinh fitness cho mot individual
	double ComputeRF(individual st){
		double sum = 0,t;
		double[] tm;
		int i;
		int hit;
		hit = 0;
		
		tm=ComputeNew(st.chrom);
		st.semanticTraining = tm;
		//st.semanticTesting = ComputeTest(st.chrom);
		
		for(i = 0; i < NUMFITCASE; i++) {
		
			t = PVAL(tm[i]);
			t= Math.abs(t- fitcase[i].y);// fabs
			if(t <= 0.01) hit++;
			sum += t;
		}
		if(hit == NUMFITCASE) SuccPredicate = TRUE;
		return sum;
	}
	
	double ComputeFT(individual st){
		double sum = 0, ft;
		int i;
		double[] tm;
		tm=ComputeTest(st.chrom);
		for(i = 0; i < NUMFITTEST; i++) {
			ft = PVAL(tm[i]);
			ft= Math.abs(ft- fittest[i].y);// fabs			
			sum += ft;
		}		
		return sum;
	}
	
	
//phuong thuc tinh fitness cho tat ca cac individual trong mot quan the
	void ComputeFitness(){
		int i, pos;
		// individual t;
		double min, sum = 0, tm;
		// First Compute Raw fitness
		for(i = 0; i < poplen; i++)
		{
			if(oldpop[i].evaluated==FALSE)
			{   
				tm=ComputeRF(oldpop[i]);
				
				oldpop[i].fitness = tm;
				oldpop[i].oldfitness = tm;
				oldpop[i].evaluated=TRUE;
			}
			else
			{
				oldpop[i].fitness=oldpop[i].oldfitness;
			}
			
			//oldpop[i].DisplayIndividual();
			//System.out.println(oldpop[i].fitness);
		}
		//tim individual co fitness be nhat
		min = oldpop[0].fitness;
		pos = 0;
		sum = 0;
		for(i = 1; i < poplen; i++) {
			if(oldpop[i].fitness < min) {
				min = oldpop[i].fitness;
				pos = i;
			}
			sum += oldpop[i].fitness;
		}
		
		//show the best individual
//		oldpop[pos].DisplayIndividualChrom();
		// copy the best and average
		bestcurrent[gen] = new individual();
		bestcurrent[gen].CopyIndividual(oldpop[pos], TRUE);
		average[gen] = sum /poplen;
		// Third Compute Adjusted fitness
		AdjustFitness();
		// Finally Compute nomarlized fitness
 		NormalizeFitness();
	}
	//in cac population ra file
//	void PrintToFile()
//	{
//		FileInputStream instream = null;
//		PrintStream outstream = null;
//		PrintStream console = System.out;
//		String temp=""; 
//		try {
//			outstream = new PrintStream(new FileOutputStream("c:/result/" + gen + ".txt"));
//			System.setOut(outstream);
//		} catch(Exception e) {
//			System.err.println("Error Occurred.");
//		}
//		
//		for(int ii = 0; ii < poplen; ii++) {			
//			temp="";
//			temp=oldpop[ii].chrom.TreeToStringN(oldpop[ii].chrom);
//			System.out.printf("%s", temp);			
//				System.out.println();
////			}
//		}
//		outstream.close();
//		System.setOut(console);
//	
//	}
	
	 private void genTreelibWithDistinctSemantics(int treelibsize, int depth)
	 {
	        node randomTree; 

	        int i = 0;
	        while(treelib.size() < treelibsize)
	        	
	        {
	        	randomTree = GrowthTreeGen(depth);
	        	double[] semantic = ComputeNew(randomTree);
	        	try
	        	{
	        		//kdtreelib.insert(randomTree.semanticTraining, randomTree);
//	        		kdtreelib.insert(semantic, randomTree);
	        		treelib.put(semantic, randomTree);
	            	i += 1;
	        		
	        	}
	        	
	        	catch(Exception ex)
	        	{
	        		
	        	}
	        	//this.output.systemMessage("size: " + kdtreelib.size());
	        }

	 }
	   
	    public ArrayList<Double> invert(node a, int k, double o, sample input)
	    {
	    	ArrayList<Double> semantics = new ArrayList<Double>();
	    	
	    	if(k==0) 
	    	{
	    		if (a.name == "add")
	    		{
//		        		double[] semanticsChild2 = ComputeNew(a.children.sibling); 
	    			double c2 = ComputeOne(a.children.sibling, input);
	    			semantics.add(o - c2);// = targetSemantics[i] - semanticsChild2[i];
	    			return semantics;
	    		}
	    		if (a.name == "sub")
	    		{
	        		double c2 = ComputeOne(a.children.sibling, input); 

	    			semantics.add(o + c2);
	    			return semantics;
	    		}
	    		if (a.name == "mul")
	    		{
	        		double c2 = ComputeOne(a.children.sibling, input); 

					if(c2 != 0)
						semantics.add(o /c2);
					else 
						if(o==0)
							semantics.add(STAR);
						else				
							semantics.clear();
					
	    			return semantics;
	    		}
	    		if (a.name == "div")
	    		{
	        		double c2 = ComputeOne(a.children.sibling, input); 

					if(Math.abs(c2) != Double.MAX_VALUE)
						semantics.add(o * c2);
					else
						if(o==0)
							semantics.add(STAR);
						else
//							semantics.add(null);
							semantics.clear(); // be empty

	    			return semantics;
	    		}
	    		
	    	}
	    	else //k==1
	    	{
	    		if (a.name == "add")
	    		{
	        		double c1 = ComputeOne(a.children, input); 

	    			semantics.add(o-c1);
	    		
	    			return semantics;
	    		}
	    		if (a.name == "sub")
	    		{
	        		double c1 = ComputeOne(a.children, input); 

	    			semantics.add(c1-o);
	    			return semantics;
	    		}
	    		if (a.name == "mul")
	    		{
	        		double c1 = ComputeOne(a.children, input); 

					if(c1 != 0)
						semantics.add(o/c1);
					else if (o==0) {
						semantics.add(STAR);
						
					}
					else {
						
						semantics.clear();
					}
	    				
	    			return semantics;
	    		}
	    		if (a.name == "div")
	    		{
	        		double c1 = ComputeOne(a.children, input); 

					if(o != 0)
						semantics.add(c1/o);
					else if(c1==0)
						semantics.add(STAR);
					else
						semantics.clear();

	    			return semantics;
	    		}
	    		
	    	}
	    	// exp
	    	if (a.name == "ep")
			{
				if(o >= 0)
					semantics.add(Math.log(o));
				else
					semantics.clear();

				return semantics;
			}
	    	
	    	if (a.name == "log")
			{
				semantics.add(-Math.exp(o));
				semantics.add(Math.exp(o));
				return semantics;
			}

	    	if (a.name == "sin")
			{
	    		if (Math.abs(o) <= 1)
	    		{
	    			semantics.add(Math.asin(o) - 2 * Math.PI);
	    			semantics.add(Math.asin(o));
	    		}
	    		else
	    			semantics.clear();;
	    		
				return semantics;
			}
	    	
	    	if (a.name == "cos")
			{
	    		if (Math.abs(o) <= 1)
	    		{
	    			semantics.add(Math.acos(o) - 2 * Math.PI);
	    			semantics.add(Math.acos(o));
	    		}
	    		else
	    			semantics.clear();
	    		
				return semantics;
			}

	    	if (a.name == "sqrt")
			{
				if (o >= 0)
					semantics.add(o * o);
				else
					semantics.clear();
				
				return semantics;
			}

	    	if (a.name == "sqr")
			{
	    		if (o >= 0)
	    		{
	    			semantics.add(Math.sqrt(o));
	    			semantics.add(-Math.sqrt(o));
	    		}
	    		else {
					semantics.clear();
				}
				return semantics;
			}
	    	
	    	//
	    	
			return semantics;
	    	
	    }

	    /**
	     * return position of a child in the path from a to n
	     * @param a
	     * @param n
	     * @return 0 or 1
	     */
	    private int getPosition(node a, node n)
	    {
	    	node childOfa = n;
	    	while(childOfa.parent != a)
	    		childOfa = childOfa.parent;
	    	
	    	if (a.children == childOfa)
	    		return 0;
	    	else return 1;
	    }
	    
	    /**
	     * get child of node a on the path leading from a to n
	     * @param a
	     * @param a
	     * @return
	     */
	    private node getChild(node a, node n)
	    {
	    	node childOfa = n;
	    	while(childOfa.parent != a)
	    		childOfa = childOfa.parent;
	    	
	    	return childOfa;
	    }
		
		    
		public ArrayList<Double>[] semanticBackPropagation(double[] targetSemantics, node p, node n)
		{
			    	
	    	ArrayList<Double>[] desiredSemantics = new ArrayList[targetSemantics.length];
	    	
	    	for (int i = 0; i < targetSemantics.length; i++)
	    	{
	    		
	    		
	    		double t = targetSemantics[i];
	    		ArrayList<Double> Di = new ArrayList<Double>();
	    		Di.add(t);
	    		
	    		node a = p;
	    		
	    		while(a != n && !Di.isEmpty() && !Di.contains(STAR))
	    		{
	    			int k = getPosition(a, n);
	    			ArrayList<Double> tempD = new ArrayList<Double>();
	    			for(double o: Di)
	    			{
	    				ArrayList<Double> re = invert(a, k, o, fitcase[i]);
	    				tempD.addAll(re);
	    			}
	    			
	    			Di.clear();
	    			Di.addAll(tempD);
	    			a = getChild(a, n);
	    		}
	    		
	    		ArrayList<Double> tmp = new ArrayList<Double>();
	    		tmp.addAll(Di);
	    		
	    		desiredSemantics[i] = tmp;
	    				
	    	}
	    	return desiredSemantics;
		}
		    
		
		private node getBestNode(ArrayList<Double>[] desiredSemantics,
				double[] targetSemantic,
				double[] bestConstants)
		{
			assert desiredSemantics.length == targetSemantic.length;
			
			node bestNode = null;
			double smallestDistance = Double.MAX_VALUE;
			
			Set<double[]> keys = treelib.keySet();
			
			
			for(double[] key: keys)
			{
				double totalDistance = 0;
				int inConsistencies = 0;
				
				for(int i = 0; i < targetSemantic.length; i++)
				{
					ArrayList<Double> Di = desiredSemantics[i];
					
					if (Di.isEmpty())
						continue;
					
					else if(Di.get(0)==null)
					{
						++inConsistencies;
						continue;
					}
					
					double minDij = Double.MAX_VALUE;
					
					for (Double y : Di)
					{
						double diff = Math.abs(y-key[i]); 
						if(minDij > diff)
							minDij = diff;
					}
					
					totalDistance += minDij;
					
				}
				totalDistance += inConsistencies;
				
				if(totalDistance < smallestDistance)
				{
					smallestDistance = totalDistance;
					bestNode = treelib.get(key);
				}
				
			}
			
			if(smallestDistance < bestConstants[1])
				return bestNode;
			else 
			{
				// return ERC node
				node ercNode = new node("ERC", bestConstants[0]);
				return ercNode;
				
			}
			
		}
		
		private double[] getBestConstant(ArrayList<Double>[] desiredSemantics)
		{
			double bestConstant = 0;
			double minDistance = Double.MAX_VALUE;
//			assert desiredSemantics.length == targetSemantic.length;
		
			Set<double[]> keys = treelib.keySet();
			
			
			for(ArrayList<Double> Dk: desiredSemantics)
			{
				if(Dk.contains(null))
					continue;
				
				for(double c: Dk)
				{
					double totalDistance = 0;
								
					for(ArrayList<Double> Di: desiredSemantics)
					{
						if (Di.isEmpty())
							continue;
						
						// get y in Di that minimize abs(y-c)
						double min_y = 0;
						double min_yc = Double.MAX_VALUE;
						
						for (double y: Di)
						{
							double temp = Math.abs(y-c);
							if(temp < min_yc)
							{
								min_yc = temp;
								min_y = y;
							}
						}
						
						totalDistance += min_yc;
					}
					
					if(totalDistance < minDistance)
					{
						minDistance = totalDistance;
						bestConstant = c;
					}
				}
			}
			double[] ret = {bestConstant, minDistance};
			return ret;
		}
	    


//	
//phuong thuc tien hoa
	void Evolution()
	{
		int i, j, l, k;
		//individual[] temp;
		byte _flippc=0,_flippm=0;
//		String _temp1="", _temp2="";
		int[] _cp;
		node[] _nd,_nm;
		ncross=0;
		// Khoi tao quan the dau tien 
		RampedInit(6, 0.5);
		

		constructiveRate = new double[NUMGEN];
		semanticDistance = new double[NUMGEN];

		gen = 0;
		while((gen < generation)) 
		{
			
			ComputeFitness();
			
			// for constructive rate and semantic distance			
			int g_ncross = 0;
			int g_numOfBetter = 0;
			double g_sd = 0;
			
			
			l = 0;			
			while(l < poplen) 
			{
				i = TourSelect(TOURSIZE);
				j = TourSelect(TOURSIZE);
				
				if(Flip(pcross) == 1)
				{
					individual i_temp1 = new individual();
					individual i_temp2 = new individual();
					
					if(AGX(oldpop[i], oldpop[j], i_temp1, i_temp2) == TRUE)
					{
						newpop[l].CopyIndividual(i_temp1,TRUE);
						newpop[l+1].CopyIndividual(i_temp2, TRUE);
							
						newpop[l].evaluated=FALSE;
						newpop[l+1].evaluated=FALSE;
						ncross++;
						i_temp1=new individual();
						i_temp2 = new individual();
						
						// constructive rate
						if(newpop[l].isBetterThan(oldpop[i]))
							g_numOfBetter += 1;
						if(newpop[l+1].isBetterThan(oldpop[j]))
							g_numOfBetter += 1;
						
						// semantic distance
						g_sd += newpop[l].getSemanticDistance(oldpop[i]);
						g_sd += newpop[l+1].getSemanticDistance(oldpop[j]);
						
						g_ncross++;
						
					}
					else
					{
						newpop[l].CopyIndividual(oldpop[i], TRUE);
						newpop[l+1].CopyIndividual(oldpop[j], TRUE);
					}
					
				}
				else 
				{
					newpop[l].CopyIndividual(oldpop[i], TRUE);
					newpop[l+1].CopyIndividual(oldpop[j], TRUE);					
				}
				
				if(KOZA_MUTATION && (Flip(pmutate)==1))
				{					
					individual[] m_individual=new individual[1];
					int[] _mt=new int[1];
					_nm=new node[1];
					
					if(this.ReplaceSubTree(newpop[l], m_individual, 15, TRUE,_mt,_nm)==TRUE)
					{
						newpop[l]=new individual();
						newpop[l].CopyIndividual(m_individual[0], TRUE);
						
						newpop[l].evaluated=FALSE;
						
						nmutate++;
						m_individual=null;
				
					}
				}
				
				
				if(KOZA_MUTATION && (Flip(pmutate)==1))
				{					
					individual[] m_individual1=new individual[1];
					int[] _mt=new int[1];
					_nm=new node[1];
					
					if(this.ReplaceSubTree(newpop[l + 1], m_individual1, 15, TRUE,_mt,_nm)==TRUE)
					{
						newpop[l+1]=new individual();
						newpop[l+1].CopyIndividual(m_individual1[0], TRUE);	
						
						newpop[l+1].evaluated=FALSE;

						nmutate++;
						m_individual1=null;
				
					}
					
					
				}
				
				l += 2;
				
			}
			
			constructiveRate[gen] = g_numOfBetter / (2.0 * g_ncross);
			semanticDistance[gen] = g_sd / (2.0 * g_ncross);

			gen++;
			for(k = 0; k < poplen; k++)
			{
				oldpop[k]=null;
				oldpop[k]=new individual();
				oldpop[k].CopyIndividual(newpop[k], TRUE);
			}
			
			
		}
		
	}
}
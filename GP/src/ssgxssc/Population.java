package ssgxssc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
	byte		SuccPredicate	= FALSE;//danh gia thanh cong hay khong?
	int				generation, gen = 0;
	individual[]	bestcurrent;
	double[]			average;
	
	long searchSubtree;

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
		//AddTerm("sqrt", (byte) 1);
		//AddTerm("sqr", (byte) 1);
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
		
		searchSubtree = 0;
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
		byte evaluated;
		double[] semanticTraning;
		double[] semanticTesting;
		// fitness cua indinvidual
//ham tao cua lop individual
       public individual() {
			size = 0;
			height = 0;
			branch = 0;
			fitness = 0;
			oldfitness=0;
			evaluated=FALSE;
			chrom = new node();
			semanticTraning = new double[NUMFITCASE];
			semanticTesting = new double[NUMFITTEST];
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
			this.evaluated=t.evaluated;
			
			this.semanticTraning = t.semanticTraning.clone();
			this.semanticTesting = t.semanticTesting.clone();
			
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
				for(a = 1; a < glterm[i].arity; a++) {
					p.sibling = GrowthTreeGen(maxdepth - 1);
					p = p.sibling;
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
			for(a = 1; a < glfunction[i].arity; a++) {
				p.sibling = FullTreeGen(maxdepth - 1);
				p = p.sibling;
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
				for(k = 1; k < glfunction[j].arity; k++) {
					p.sibling = GrowthTreeGen(depth - 1);
					p = p.sibling;
				}
				p.sibling = null;
			} 
			else 
			{
				t.children = FullTreeGen(depth - 1);
				p = t.children;
				for(k = 1; k < glfunction[j].arity; k++) {
					p.sibling = FullTreeGen(depth - 1);
					p = p.sibling;
				}
				p.sibling = null;
			}
			oldpop[i].chrom = t;
			oldpop[i].size = t.GetSizeNode(t, TRUE);
			oldpop[i].height = t.GetHeightNode();
			oldpop[i].branch = t.GetAVGNode(t);
			oldpop[i].semanticTraning = ComputeNew(t);
			//oldpop[i].semanticTesting = ComputeTest(t);
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
	
	node getSubtree(individual parent)
	{
		individual temp = parent;
		//temp.CopyIndividual(parent, TRUE);
		
		double[] semanticParent = temp.semanticTraning;
		
		double bestDistance = Double.MAX_VALUE;
		int bestLocation = -1;
		
		for(int i = 0; i < NUM_TRIAL; i++)
		{
			int aPoint = IRandom(1, parent.size);
			
			temp.chrom.LocateNode(aPoint, temp.chrom, null);
			
			node node1=temp.chrom._idnode;
			
			if(node1.GetSizeNode(node1,TRUE)>80||node1.GetSizeNode(node1,TRUE)<2) 
				{continue;}
			
			//if(node1.GetSizeNode(node1,TRUE)<10) continue;
			
			double[] sm = ComputeNew(node1);
			
			double distance = 0;
			
			for(int j = 0; j < NUMFITCASE; j++)
			{
				distance += Math.abs(semanticParent[j] - sm[j]);
				
				if(distance > bestDistance)
					break;
			}
			
			if(distance < bestDistance)
			{
				bestDistance = distance;
				bestLocation = aPoint;
			}
			
		}
		
		
		if(bestLocation > -1)
		{
			temp.chrom.LocateNode(bestLocation, temp.chrom, null);
			return temp.chrom._idnode;
		}
				
		
		return null;
	}
	
	node generateTR(int maxdepth)
	{
        // make template tree: 1 / (1 + exp(-TR))
        //create tree: 1 - TR
        node divNode =  new node("div", VOIDVALUE);
        divNode.children = new node("1", 1);
        
        node addNode =  new node("add", VOIDVALUE);
        divNode.children.sibling = addNode;

        addNode.children = new node("1", 1);
        
        node expNode = new node("ep", VOIDVALUE);
        addNode.children.sibling = expNode;
        	        
        node subNode =  new node("sub", VOIDVALUE);
        expNode.children = subNode;
        
        subNode.children = new node("0", 0);
        
        subNode.children.sibling = GrowthTreeGen(maxdepth);

        return divNode;
	}
	
	node[] makeGeometricTree(node p1, node p2, node TR)
	{
		node addNode = new node("add", VOIDVALUE);
		
		node mulNode1 =  new node("mul", VOIDVALUE);
		addNode.children = mulNode1;
		
		node mulNode2 = new node("mul", VOIDVALUE);
		mulNode1.sibling = mulNode2;
		
		mulNode1.children = TR.CopyNode(TR);
		mulNode1.children.sibling = p1;
		
		node subNode = new node("sub", VOIDVALUE);
		mulNode2.children = subNode;
		subNode.sibling = p2;
		
		subNode.children = new node("1", 1);
		subNode.children.sibling = TR.CopyNode(TR);
		
		node[] retVal = new node[2];
		retVal[0] = addNode.CopyNode(addNode);
		
		mulNode1.children = subNode;
		subNode.sibling = p1;
		mulNode2.children = TR.CopyNode(TR);
		mulNode2.children.sibling = p2;
		
		retVal[1] = addNode.CopyNode(addNode);
		
		return retVal;
	}
	////////////////////////////////////////////////////////////
	
	double ComputeDistance(node node1, node node2)
	{
		double[] snode1 = ComputeNew(node1);
		double[] snode2 = ComputeNew(node2);
		double sumdistance=0;
		for(int j=0;j<NUMFITCASE;j++)
		{
			sumdistance=sumdistance+Math.abs(snode1[j]-snode2[j]);
			//if(sumdistance>bestdistance)
			//	return sumdistance;
			if(sumdistance/NUMFITCASE>USIM) return sumdistance/NUMFITCASE; 
		}
		
		sumdistance=sumdistance/NUMFITCASE;
		return sumdistance;
	}
	
	
	
	// **************************************************
	byte SGXMSSC(individual parent1, individual parent2, individual[] child,int[] cp,node[] nd)
	{
		individual temp1, temp2;// hai bien tam de luu hai parent		
		temp1 = new individual();
		temp2 = new individual();
		int crosspoint1=0;
		int crosspoint2=0;
		double rand = Next_Double();
		double sumdistance;
		
		int count = 0;
		
		while(count < MAXATEMPT)
		{
			temp1.CopyIndividual(parent1, TRUE);
			temp2.CopyIndividual(parent2, TRUE);
			
			boolean sc = true;
			
			if(rand < SGXMSC_PRO)
			{				
				sc = false;
				
				node TR = generateTR(SSGX_TR_MAXDEPTH);
				
				//System.err.println(TR.TreeToStringN(TR));
				
				double[] semanticTrainingTR = ComputeNew(TR);
				//double[] semanticTestingTR = ComputeTest(TR);
				long tmpTime = System.currentTimeMillis();
				
				node subP1 = getSubtree(temp1);
				node subP2 = getSubtree(temp2);
				
				tmpTime = System.currentTimeMillis() - tmpTime;
				
				searchSubtree += tmpTime;
				
				if(subP1 != null && subP2 != null)
				{
					double[] semanticTrainingSubP1 = ComputeNew(subP1);
					//double[] semanticTestingSubP1 = ComputeTest(subP1);
	
					double[] semanticTrainingSubP2 = ComputeNew(subP2);
					//double[] semanticTestingSubP2 = ComputeTest(subP2);
				
				
					node[] children = makeGeometricTree(subP1, subP2, TR);
					
					temp1.chrom = null;
					temp2.chrom = null;
					
					temp1.chrom = children[0];
					temp2.chrom = children[1];
					
					for(int i = 0; i < NUMFITCASE; i++)
					{
						//compute 1 / (1 + exp(-TR))
						double t = semanticTrainingTR[i];
						
						temp1.semanticTraning[i] = t * semanticTrainingSubP1[i] + (1 - t) * semanticTrainingSubP2[i];
						temp2.semanticTraning[i] = (1-t)*semanticTrainingSubP1[i] + t * semanticTrainingSubP2[i];
						
					}
/*					
					for(int i = 0; i < NUMFITTEST; i++)
					{
						double t = semanticTestingTR[i];
						
						temp1.semanticTesting[i] = t * semanticTestingSubP1[i] + (1 - t)*semanticTestingSubP2[i];
						temp2.semanticTesting[i] = (1-t)*semanticTestingSubP1[i] + t * semanticTestingSubP2[i];
					}*/
				}
			}
			else //SSC
			{
				// Chon ngau nhien diem de lai ghep
				//int cpoint1=0,cpoint2=0;
				double bestdistance=Double.MAX_VALUE;
				node node1=null, node2=null;
				//int crosspoint1 = IRandom(2, temp1.size);
				//int crosspoint2 = IRandom(2, temp2.size);
				//temp1.chrom.LocateNode(crosspoint1, temp1.chrom, null);
				//node1=temp1.chrom._idnode; 
				//temp2.chrom.LocateNode(crosspoint2, temp2.chrom, null);
				//node2=temp2.chrom._idnode;
							
				for(int i=0;i<NUMSIZE;i++)
				{	
					crosspoint1 = IRandom(2, temp1.size);
					crosspoint2 = IRandom(2, temp2.size);
					temp1.chrom.LocateNode(crosspoint1, temp1.chrom, null);
					node1=temp1.chrom._idnode; 
					temp2.chrom.LocateNode(crosspoint2, temp2.chrom, null);
					node2=temp2.chrom._idnode;
					
					double[] snode1 = ComputeNew(node1);
					double[] snode2 = ComputeNew(node2);
					sumdistance=0;
					for(int j=0;j<NUMFITCASE;j++)
					{
						sumdistance=sumdistance+Math.abs(snode1[j]-snode2[j]);
						if(sumdistance>USIM) break; 
					}
					
					//sumdistance=ComputeDistance(node1,node2);
					//if(sumdistance<USIM&&sumdistance>LSIM)
					if(sumdistance<USIM && sumdistance>LSIM)
					{
						break;
					}	
					
				}
				
				temp1.chrom.LocateNode(crosspoint1, temp1.chrom, null);
				node1=temp1.chrom._idnode; 
				temp2.chrom.LocateNode(crosspoint2, temp2.chrom, null);
				node2=temp2.chrom._idnode;
				
				
				String nametemp = node1.name;
				node1.name = node2.name;
				node2.name = nametemp;
				double valuetemp = node1.value;
				node1.value = node2.value;
				node2.value = valuetemp;
				// then swap the children
				node t = node1.children;
				node1.children = node2.children;
				node2.children = t;
			}
			
			temp1.height = temp1.chrom.GetHeightNode();
			temp2.height = temp2.chrom.GetHeightNode();
			
			if((temp1.height <= MAXDEPTH) && (temp2.height <= MAXDEPTH)) {
				temp1.size = temp1.chrom.GetSizeNode(temp1.chrom, TRUE);
				temp1.branch=temp1.chrom.GetAVGNode(temp1.chrom);
				temp2.size=temp2.chrom.GetSizeNode(temp2.chrom, TRUE);
				temp2.branch=temp2.chrom.GetAVGNode(temp2.chrom);
				
				
				child[0]=temp1;
				child[1]=temp2;
				
				if(sc)
				{
					child[0].semanticTraning = ComputeNew(temp1.chrom);
					child[1].semanticTraning = ComputeNew(temp2.chrom);
				}
				
				return TRUE;
			} else {}
		
		}
				
		return FALSE;
	}
	// **************************************************
	byte SGMR(individual parent1, double ms, individual[] child,int[] cp,node[] nd)
	{
		individual temp1, temp2;// hai bien tam de luu hai parent		
		temp1 = new individual();
		
		temp1.CopyIndividual(parent1, TRUE);
		//temp2 = new individual();
		
		node TR1 = GrowthTreeGen(SSGX_TR_MAXDEPTH);
		double[] semanticTrainingTR1 = ComputeNew(TR1);
		//double[] semanticTestingTR1 = ComputeTest(TR1);
		
		node TR2 = GrowthTreeGen(SSGX_TR_MAXDEPTH);
		double[] semanticTrainingTR2 = ComputeNew(TR2);
		//double[] semanticTestingTR2 = ComputeTest(TR2);
		
		
		for(int i = 0; i < NUMFITCASE; i++)
		{
			//compute 1 / (1 + exp(-TR))
			double t1= 1.0/(1 + Math.exp(-semanticTrainingTR1[i]));
			double t2= 1.0/(1 + Math.exp(-semanticTrainingTR2[i]));
			
			temp1.semanticTraning[i] = parent1.semanticTraning[i] + ms*(t1-t2);
			
		}
		
		/*for(int i = 0; i < NUMFITTEST; i++)
		{
			double t1= 1.0/(1 + Math.exp(-semanticTestingTR1[i]));
			double t2= 1.0/(1 + Math.exp(-semanticTestingTR2[i]));
			
			temp1.semanticTesting[i] = parent1.semanticTesting[i] + ms * (t1 - t2);
		}*/
		
		child[0]=temp1;
		
		return TRUE;
	}

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
			line = br.readLine();
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
	else if((st.name == "0")){
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
		} else if(st.name == "sqr") {
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
		} else if(st.name == "log") {
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
	return result;
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
	if((st.name == "0")){
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
		} else if(st.name=="log") {
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
	return result;
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
		for(i = 0; i < NUMFITCASE; i++) {
		
			t = PVAL(tm[i]);
			t= Math.abs(t- fitcase[i].y);// fabs
			if(t <= 0.01) hit++;
			sum += t;
		}
		if(hit == NUMFITCASE) SuccPredicate = TRUE;
		return sum;
	}
	
	double ComputeRFMoraglio(individual st){
		double sum = 0,t;
		double[] tm;
		int i;
		int hit;
		hit = 0;
		
		tm=st.semanticTraning;
		
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
	
	double ComputeFTMoraglio(individual st){
		double sum = 0, ft;
		int i;
		double[] tm;
		tm=st.semanticTesting;
		
		for(i = 0; i < NUMFITTEST; i++) {
			ft = PVAL(tm[i]);
			ft= Math.abs(ft- fittest[i].y);// fabs			
			sum += ft;
		}		
		return sum/NUMFITTEST;
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
			{   tm=ComputeRF(oldpop[i]);
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
		// copy the best and average
		bestcurrent[gen] = new individual();
		bestcurrent[gen].CopyIndividual(oldpop[pos], TRUE);
		average[gen] = sum /poplen;
		// Third Compute Adjusted fitness
		AdjustFitness();
		// Finally Compute nomarlized fitness
 		NormalizeFitness();
	}
	
	void ComputeFitnessMoraglio(){
		int i, pos;
		// individual t;
		double min, sum = 0, tm;
		// First Compute Raw fitness
		for(i = 0; i < poplen; i++)
		{
			if(oldpop[i].evaluated==FALSE)
			{   tm=ComputeRFMoraglio(oldpop[i]);
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
//	
//phuong thuc tien hoa
	void Evolution(){
		int i, j, l, k;
		//individual[] temp;
		byte _flippc=0,_flippm=0;
//		String _temp1="", _temp2="";
		int[] _cp;
		node[] _nd,_nm;
		// Khoi tao quan the dau tien 
		RampedInit(6, 0.5);
		
		gen = 0;
		while(gen < generation) {
			
			//ComputeFitness();
			
			ComputeFitnessMoraglio();
			l = 0;			

			while(l < poplen) {
				i = TourSelect(TOURSIZE);
				j = TourSelect(TOURSIZE);

				_flippc=Flip(pcross);

				if(_flippc == 1) {					
					individual[] i_temp=new individual[2];
					_cp=new int[2];
					_nd=new node[2];
					
//					if(SubTreeSwap(oldpop[i], oldpop[j], i_temp,_cp,_nd) == TRUE)
					
					if(SGXMSSC(oldpop[i], oldpop[j], i_temp,_cp,_nd) == TRUE)
					
					{
						newpop[l].CopyIndividual(i_temp[0],FALSE);
						newpop[l+1].CopyIndividual(i_temp[1],TRUE);
						
						newpop[l].evaluated=FALSE;
						newpop[l+1].evaluated=FALSE;
						
						ncross++;
						i_temp=null;
					}
					else
					{						
						newpop[l].CopyIndividual(oldpop[i], TRUE);
						
						newpop[l + 1].CopyIndividual(oldpop[j],TRUE);
						
						i_temp=null;
					} 
				} 
				else
				{
					newpop[l].CopyIndividual(oldpop[i], TRUE);
					newpop[l + 1].CopyIndividual(oldpop[j],TRUE);
					
				}
				// mutation test
				_flippm=Flip(pmutate);
				if(_flippm == 1) {					
					individual[] m_individual=new individual[1];
					int[] _mt=new int[1];
					_nm=new node[1];
					
					if(this.ReplaceSubTree(newpop[l], m_individual, 15, TRUE,_mt,_nm)==TRUE)
					{
//					this.SGMR(newpop[l], 0.1, m_individual, _mt,_nm);
					
					newpop[l]=new individual();
					newpop[l].CopyIndividual(m_individual[0], FALSE);
					
					newpop[l].semanticTraning = ComputeNew(newpop[l].chrom);
					
					newpop[l].evaluated=FALSE;
					
					nmutate++;
					m_individual=null;
				
					}
				}
				
				if(Flip(pmutate) == 1) {
					individual[] m_individual1=new individual[1];
					int[] _mt=new int[1];
					_nm=new node[1];
					
					if(this.ReplaceSubTree(newpop[l + 1], m_individual1, 15, TRUE,_mt,_nm)==TRUE)
					{
					newpop[l+1]=new individual();
					newpop[l+1].CopyIndividual(m_individual1[0], TRUE);	
//					
					newpop[l+1].semanticTraning = ComputeNew(newpop[l+1].chrom);
					
					newpop[l+1].evaluated=FALSE;
//					
					nmutate++;
					m_individual1=null;
				
					}
				}
				
				l += 2;
			}
//			for(int ii = 0; ii < poplen; ii++) {			
//				_temp1="";
//				_temp1=oldpop[ii].chrom.TreeToStringN(newpop[ii].chrom);
//				System.out.printf("%s",ii+" "+ _temp1);			
//				System.out.println();
//			}
//			outstream.close();
//			System.setOut(console);
			gen++;
			for(k = 0; k < poplen; k++)
			{
				oldpop[k]=null;
				oldpop[k]=new individual();
				oldpop[k].CopyIndividual(newpop[k], TRUE);
			}
			//oldpop[poplen/2]= new individual();
			//oldpop[poplen/2].CopyIndividual(bestcurrent[gen-1],TRUE);
			
			
		}
	}
}

package agx;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import common.MyRandom;

import agx.Population;

public class MainAGX extends  MyRandom {
	public MainAGX() {}

	int									i, pos;
	double								min;
	long									StartSeed		= 2000;
	Population							myPop;
	int nrun=NRUN;
	long Seed=0;
	
	double average=0;
	double fittest;
	double averagetest=0;
	double averagesize=0;
	
//	String outDir = "/home/pta/projects/GP/src/AGX/out/";
	String outDir = "./src/agx/out/";
	
	public void MStart()
	{
		
		long time;	
		
		double averInvertTime = 0;
		double averTreelibSearchTime = 0;
		double averTreelibGeneration = 0;
		
		PrintStream f1 = null;
		PrintStream f2 = null;
		PrintStream f3 = null;
		PrintStream f4 = null;
		PrintStream f5 = null;
		
		double[][] fitnessGens = new double[NRUN][NUMGEN];
		double[][] fittestGens = new double[NRUN][NUMGEN];
		double[][] sizeGens = new double[NRUN][NUMGEN];
	
			try 
		{
			f1 = new PrintStream(new File(outDir + PROBLEM + ".all.txt"));
			f2 = new PrintStream(new File(outDir + PROBLEM + ".run.txt"));
			f3 = new PrintStream(new File(outDir + PROBLEM + ".gen.txt"));		
			f4 = new PrintStream(new File(outDir + PROBLEM + ".time.txt"));
			f5 = new PrintStream(new File(outDir + PROBLEM + ".5.txt"));
		} 
		catch(Exception e) 
		{
			System.err.println("Output files error!" + e.getMessage());
	      
		}
		
		
		Seed=StartSeed;
		
		double[] fittests = new double[NRUN];
		int run = 0;
		
		time=System.currentTimeMillis();
		
		// constructive rate and semantic distance
		double[] constructiveRate = new double[NUMGEN];
		double[] semanticDistance = new double[NUMGEN];
		for(int i = 0; i < NUMGEN; i++)
		{
			constructiveRate[i] = 0;
			semanticDistance[i] = 0;
		}

		
		while (Seed<StartSeed+nrun)
		{
			
			System.err.println("Run: " + Seed);

			Seed++;
			
			long starttime = System.currentTimeMillis();
			
			myPop = new Population(Seed);
			myPop.Evolution();

			starttime = System.currentTimeMillis() - starttime;
			
			averInvertTime += myPop.invertTime;
			averTreelibGeneration += myPop.treelibGenerateTime;
			averTreelibSearchTime += myPop.treelibSearchTime;
			
			min = myPop.bestcurrent[0].fitness;
			pos = 0;
			fitnessGens[run][0] = myPop.bestcurrent[0].fitness;
			fittestGens[run][0] = myPop.ComputeFT(myPop.bestcurrent[0]);
			sizeGens[run][0] = myPop.bestcurrent[0].size;

			//constructive rate and semantic distance
			constructiveRate[0] += myPop.constructiveRate[0];
			semanticDistance[0] += myPop.semanticDistance[0];
			
			for(i = 1; i < NUMGEN; i++)
			{
				fitnessGens[run][i] = myPop.bestcurrent[i].fitness;
				fittestGens[run][i] = myPop.ComputeFT(myPop.bestcurrent[i]);
				sizeGens[run][i] = myPop.bestcurrent[i].size;

				if(myPop.bestcurrent[i].fitness < min) 
				{
					min = myPop.bestcurrent[i].fitness;
					pos = i;
			     }
				
				// constructive rate and semantic distance
				constructiveRate[i] += myPop.constructiveRate[i];
				semanticDistance[i] += myPop.semanticDistance[i];
		     }
			
			average+=myPop.bestcurrent[pos].fitness;
			fittest=myPop.ComputeFT(myPop.bestcurrent[pos]);
			fittests[run] = fittest;
			
			myPop.bestcurrent[pos].DisplayIndividualChrom();
			//averagetest+=fittest;
			averagesize+=myPop.bestcurrent[pos].size;
			
			
			f2	.println(myPop.bestcurrent[pos].fitness + " " + fittest 
					+ " " + myPop.bestcurrent[pos].size + " " + starttime + "\n");
			
			run += 1;
	   }
		time=System.currentTimeMillis()-time;
		double timeaverage = time/(double)nrun;
		
		
		average=average/nrun;
		//averagetest=averagetest/nrun;
		averagesize=averagesize/nrun;

		f1.println(average + "\n");
		
		//median on testing
		Arrays.sort(fittests);	
		f1.println(fittests[NRUN/2] + "\n");
		f1.println(averagesize + "\n");
		f1.println(timeaverage + "\n");
		
		//average per runs for each gens
		double[] genAver = new double[NUMGEN];
		for(int g = 0; g < NUMGEN; g++)
		{
			double fn = 0.0;
			double[] ft = new double[NRUN];
			double size = 0.0;
			for(int r = 0; r < NRUN; r++)
			{
				fn += fitnessGens[r][g];
				ft[r] = fittestGens[r][g];
				size += sizeGens[r][g];								
			}
			
			Arrays.sort(ft);
			fn = fn/NRUN;
			size = size/NRUN;
			
			f3.println(fn + " " + ft[NRUN/2] + " " + size + "\n");
			
			// constructive rate and semantic distance
			double consRate = constructiveRate[g] / NRUN;
			double sd = semanticDistance[g]/NRUN;
			f5.println(consRate + " " + sd);
		}
		
		
		averInvertTime = averInvertTime/NRUN;
		averTreelibGeneration = averTreelibGeneration/NRUN;
		averTreelibSearchTime = averTreelibSearchTime/NRUN;
		
		f4.println(averInvertTime 
				+ " " + averTreelibGeneration 
				+ " " + averTreelibSearchTime);
		
		f1.close();
		f2.close();
		f3.close();
		f4.close();
		f5.close();
}


	public static void main(String[] args)throws IOException{
		MainAGX a = new MainAGX();	
		
	
		
		System.out.printf("AGX: "+ PROBLEM + "\n");
		a.MStart();
		System.out.printf("Finish!");
		
		
	}
}

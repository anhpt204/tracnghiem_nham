package ssgxmssc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import ssgxmssc.Population;
import common.MyRandom;

public class MainSGXMMSSC extends  MyRandom {
	public MainSGXMMSSC() {}

	int									i, pos;
	double								min;
	long								StartSeed		= 2000;
	Population							myPop;
	int nrun=NRUN;
	long Seed=0;
	
	double average=0;
	double fittest;
	double averagetest=0;
	double averagesize=0;
	
//	String outDir = "/home/pta/projects/GP/src/SGXMMSSC/out/";
	String outDir = "./src/sgxmmssc/out/";
	public void MStart(){
		
		long time;	
		
		double averSearchSubtreeTime = 0;
		
		PrintStream f1 = null;
		PrintStream f2 = null;
		PrintStream f3 = null;
		PrintStream f4 = null;
		
		double[][] fitnessGens = new double[NRUN][NUMGEN];
		double[][] fittestGens = new double[NRUN][NUMGEN];
		double[][] sizeGens = new double[NRUN][NUMGEN];
	
		try 
		{
			f1 = new PrintStream(new File(outDir + PROBLEM + ".all.txt"));
			f2 = new PrintStream(new File(outDir + PROBLEM + ".run.txt"));
			f3 = new PrintStream(new File(outDir + PROBLEM + ".gen.txt"));
			f4 = new PrintStream(new File(outDir + PROBLEM + ".time.txt"));
		} 
		catch(Exception e) 
		{
			System.err.println("Output files error!");
	      
		}
				
		Seed=StartSeed;
		
		double[] fittests = new double[NRUN];
		int run = 0;
		
		time=System.currentTimeMillis();
		
		
		while (Seed<StartSeed+nrun)
		{
			
			System.err.println("Run: " + Seed);

			Seed++;
			
			long starttime = System.currentTimeMillis();
			
			myPop = new Population(Seed);
			myPop.Evolution();

			starttime = System.currentTimeMillis() - starttime;
			
			averSearchSubtreeTime += myPop.searchSubtree;
			
			min = myPop.bestcurrent[0].fitness;
			pos = 0;
			fitnessGens[run][0] = myPop.bestcurrent[0].fitness;
			fittestGens[run][0] = myPop.ComputeFT(myPop.bestcurrent[0]);
			sizeGens[run][0] = myPop.bestcurrent[0].size;
			
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
		     }
			
			average+=myPop.bestcurrent[pos].fitness;
			fittest=myPop.ComputeFT(myPop.bestcurrent[pos]);
			fittests[run] = fittest;
			
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
		}
		
		
		averSearchSubtreeTime = averSearchSubtreeTime/(double)NRUN;
		
		f4.println(averSearchSubtreeTime);
		
		f1.close();
		f2.close();
		f3.close();
		f4.close();
}


	public static void main(String[] args)throws IOException{
		MainSGXMMSSC a = new MainSGXMMSSC();	
		
		System.out.printf("SGXMMSSC: " + PROBLEM + "\n");
		a.MStart();
		System.out.printf("Finish!");
			
	}
}

/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.summarization;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.omg.CORBA.portable.InputStream;

import ec.util.*;
import ec.*;
import ec.app.summarization.RegressionData;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/* 
 * Regression.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * Regression implements the Koza (quartic) Symbolic Regression problem.
 *
 * <p>The equation to be regressed is y = x^4 + x^3 + x^2 + x, {x in [-1,1]}
 * <p>This equation was introduced in J. R. Koza, GP II, 1994.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.regression.RegressionData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Regression problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int >= 1</font></td>
 <td valign=top>(the size of the training set)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Summarization extends GPProblem implements SimpleProblemForm
    {
    private static final long serialVersionUID = 1;

    ////// Computation of delta error between expected value and provided value



    final static double PROBABLY_ZERO = 1.11E-15;
    final static double BIG_NUMBER = 1.0e15;                // the same as lilgp uses

    /** Returns the error between the result and the expected result of a single
        data point. */
    public int error(double result, int expectedResult)
    {
    	int phishing = 0;
    	if (result > 0)
    		phishing = 1;
        int delta = Math.abs(phishing - expectedResult);

        return delta;
        }

    // parameters
    public static final String P_TESTING_FILE = "testing-file";
    public static final String P_TRAINING_FILE = "training-file";

    public double[] currentValue;

    public static final String P_SIZE = "size";
    public static final String T_SIZE = "testing-size";

    public int currentDoc;
    public int currentSentence;
    
    public int trainingSetSize;
    public int testingSetSize;
    
    // these are read-only during evaluation-time, so
    // they can be just light-cloned and not deep cloned.
    // cool, huh?
    //van ban i, cau j, va cac dac trung
    public double trainingInputs[][][];
    public int trainingOutputs[][];
    
    //Testing
    public double testingInputs[][][];
    public int testingOutputs[][];

    // we'll need to deep clone this one though.
    public RegressionData input;

    public void setup(final EvolutionState state,
        final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);
        
        try
        {
	        List<String> lines = Files.readAllLines(Paths.get("/home/pta/projects/ECJ21/ecj/ec/app/summarization/dataKSE12.txt"), 
	        		Charset.defaultCharset());
	        
	        int num_lines = lines.size();

	        trainingSetSize = 20;
	        testingSetSize = 10;
	        
	        int num_of_features = 4;
	        
	        //van ban i, cau j, cac dac trung
	        trainingInputs = new double[trainingSetSize][][];
	        trainingOutputs = new int[trainingSetSize][];
	        testingInputs = new double[testingSetSize][][];
	        testingOutputs = new int[testingSetSize][];
	    	
	        int out_index = 0;
	    	int currentLine = 0;
	    	while(currentLine < num_lines)
	        {
	        	String[] values = lines.get(currentLine).split(" ");
	        	int doc_i = Integer.parseInt(values[0]);
	        	int num_sentences = Integer.parseInt(values[1]);
	        	int num_sentences_extracted = Integer.parseInt(values[2]);
	        	if(doc_i-1 < trainingSetSize)
	        	{
		        	trainingInputs[doc_i-1] = new double[num_sentences][num_of_features];
		        	trainingOutputs[doc_i-1] = new int[num_sentences_extracted];
		        	out_index = 0;
		        	for(int i = currentLine; i < currentLine + num_sentences; i++)
		        	{
			        	String[] temp = lines.get(i).split(" ");
		        		
		        		for (int j = 0; j < num_of_features; j++)
		        		{
		        			trainingInputs[doc_i-1][i-currentLine][j] = Double.parseDouble(temp[j + 4]);
		        		}
		        		if(Integer.parseInt(temp[8]) == 1)
		        		{
		        			trainingOutputs[doc_i-1][out_index] = i  - currentLine;
		        			out_index += 1;
		        		}
		        	}
	        	}
	        	else //Testing
	        	{
		        	testingInputs[doc_i-trainingSetSize-1] = new double[num_sentences][num_of_features];
		        	testingOutputs[doc_i-trainingSetSize-1] = new int[num_sentences_extracted];
		        	out_index = 0;
		        	for(int i = currentLine; i < currentLine + num_sentences; i++)
		        	{
			        	String[] temp = lines.get(i).split(" ");
		        		
		        		for (int j = 0; j < num_of_features; j++)
		        		{
		        			testingInputs[doc_i-trainingSetSize-1][i-currentLine][j] = Double.parseDouble(temp[j + 4]);
		        		}
		        		if(Integer.parseInt(temp[8]) == 1)
		        		{
		        			testingOutputs[doc_i-trainingSetSize-1][out_index] = i  - currentLine;
		        			out_index += 1;
		        		}
		        	}
	        		
	        	}
	        	currentLine += num_sentences;
	        }

        }
        catch(IOException e)
        {
        	state.output.message(e.getMessage());
        }
        // set up our input -- don't want to use the default base, it's unsafe
        input = (RegressionData) state.parameters.getInstanceForParameterEq(
            base.push(P_DATA), null, RegressionData.class);
        input.setup(state,base.push(P_DATA));
        
        //print training and testing set
        for (int doc = 0; doc < trainingInputs.length; doc++)
        	for(int sen = 0; sen < trainingInputs[doc].length; sen++)
        	{
        		String temp = "" + doc + " " + sen + " "; 
        		for (int i = 0; i < 4; i++)
        			temp += trainingInputs[doc][sen][i] + " ";
        		//temp += trainingOutputs[doc][sen] + " ";
        		System.out.println(temp);
        	}

        for (int doc = 0; doc < testingInputs.length; doc++)
        {
        	for(int sen = 0; sen < testingInputs[doc].length; sen++)
        	{
        		String temp = "" + doc + " " + sen + " "; 
        		for (int i = 0; i < 4; i++)
        			temp += testingInputs[doc][sen][i] + " ";
        		//temp += trainingOutputs[doc][sen] + " ";
        		System.out.println(temp);
        	}
        	//System.out.println(testingOutputs[doc]);
    	}

    }

    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            int hits = 0;
            double sum = 0.0;
            double result;
            for (int y=0;y<trainingSetSize;y++) //for each document
            {
            	currentDoc = y;
            	double scores[] = new double[trainingInputs[y].length];
            	for(int j = 0; j < trainingInputs[y].length; j++) //for each sentences in document y
            	{
            		currentValue = trainingInputs[y][j];
            		((GPIndividual)ind).trees[0].child.eval(
            				state,threadnum,input,stack,((GPIndividual)ind),this);
            		scores[j] = input.x;
            	}
            	//get sentences with minimum scores
            	int num_sens_extracted = trainingOutputs[y].length;
            	int senteces_extracted[] = new int[num_sens_extracted];
            	for (int i = 0; i < num_sens_extracted; i++)
            	{
            		double minscore = scores[0];
            		int min_index = 0;
            		for(int j = 1; j < scores.length; j++)
            			if(scores[j] < minscore)
            			{
            				minscore = scores[j];
            				min_index = j;
            			}
            		scores[min_index] = 100000;
            		senteces_extracted[i] = min_index;
            	}
            	//Tinh F-Score
            	List<Integer> list_ref = new ArrayList<Integer>();
            	for(int i = 0; i < trainingOutputs[y].length; i++)
            		list_ref.add(trainingOutputs[y][i]);
            	
            	HashSet set_ref = new HashSet(list_ref);
            	
            	List<Integer> list_out = new ArrayList<Integer>();
            	for(int i = 0; i < senteces_extracted.length; i++)
            		list_out.add(senteces_extracted[i]);
            	
            	HashSet set_out = new HashSet(list_out);
            	set_ref.retainAll(list_out);
            	double p = (double)(set_ref.size())/list_ref.size();
            	double r = (double)set_ref.size()/list_out.size();
            	double f1 = 0;
            	if (p > 0 || r > 0)
            		f1 = 2 * p * r / (p + r);
            	sum += f1;
            }
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,1 - (float)sum/trainingSetSize);
            ind.evaluated = true;
            }
        }

    public void describe(EvolutionState state, Individual ind, int subpopulation, int threadnum, int log)
    {
    RegressionData input = (RegressionData)(this.input);

    // we do the testing set here
    
    //state.output.println("\n\nPerformance of Best Individual on Testing Set:\n", log);
            
    int hits = 0;
    double sum = 0.0;
    for (int y=0;y<testingInputs.length;y++)
        {
    	currentDoc = y;
    	double scores[] = new double[testingInputs[y].length];
    	for(int j = 0; j < testingInputs[y].length; j++) //for each sentences in document y
    	{
    		currentValue = testingInputs[y][j];
    		((GPIndividual)ind).trees[0].child.eval(
    				state,threadnum,input,stack,((GPIndividual)ind),this);
    		scores[j] = input.x;
    	}
    	//get sentences with minimum scores
    	int num_sens_extracted = testingOutputs[y].length;
    	int senteces_extracted[] = new int[num_sens_extracted];
    	for (int i = 0; i < num_sens_extracted; i++)
    	{
    		double minscore = scores[0];
    		int min_index = 0;
    		for(int j = 1; j < scores.length; j++)
    			if(scores[j] < minscore)
    			{
    				minscore = scores[j];
    				min_index = j;
    			}
    		scores[min_index] = 100000 ^ 10;
    		senteces_extracted[i] = min_index;
    	}
    	//Tinh F-Score
    	List<Integer> list_ref = new ArrayList<Integer>();
    	for(int i = 0; i < testingOutputs[y].length; i++)
    		list_ref.add(testingOutputs[y][i]);
    	
    	HashSet set_ref = new HashSet(list_ref);
    	
    	List<Integer> list_out = new ArrayList<Integer>();
    	for(int i = 0; i < senteces_extracted.length; i++)
    		list_out.add(senteces_extracted[i]);
    	
    	HashSet set_out = new HashSet(list_out);
    	set_ref.retainAll(list_out);
    	double p = (double)(set_ref.size())/list_ref.size();
    	double r = (double)set_ref.size()/list_out.size();
    	double f1 = 0;
    	if (p > 0 || r > 0)
    		f1 = 2 * p * r / (p + r);
    	sum += f1;
        }
                    
    // the fitness better be KozaFitness!
    KozaFitness f = (KozaFitness)(ind.fitness.clone());     // make a copy, we're just printing it out
    f.setStandardizedFitness(state,(float)sum/testingSetSize);
    f.hits = hits;
    state.output.print("" + f.standardizedFitness() + " " + sum, log);
    //f.printFitnessForHumans(state, log);
    }

	@Override
	public void evaluate_RI(EvolutionState state, Individual ind,
			int subpopulation, int threadnum, int[] samples) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public double[] getSemantic(EvolutionState state, GPNode node, int threadnum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getSemanticTesting(EvolutionState state, GPNode node,
			int threadnum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumOfFitcases() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumOfTestcases() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getOutputTraining() {
		// TODO Auto-generated method stub
		return null;
	}
}
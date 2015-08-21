package ec.app.phishing;
import ec.util.*;
import ec.*;
import ec.app.phishing.RegressionData;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import java.io.*;
import java.util.*;

public class Phishing extends GPProblem implements SimpleProblemForm
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


///// Setup

    // parameters
    public static final String P_TESTING_FILE = "testing-file";
    public static final String P_TRAINING_FILE = "training-file";

    public double[] currentValue;
    
    // these are read-only during evaluation-time, so
    // they can be just light-cloned and not deep cloned.
    // cool, huh?
    
    public double[][] trainingInputs;
    public int[] trainingOutputs;
    public double[][] testingInputs;
    public int[] testingOutputs;

    // don't bother cloning the inputs and outputs; they're read-only :-)
    // don't bother cloning the current value, it's only set during evaluation

    public void setup(EvolutionState state, Parameter base)
    {
        // very important, remember this
        super.setup(state,base);

        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof RegressionData))
            state.output.fatal("GPData class must subclass from " + RegressionData.class,
                base.push(P_DATA), null);

        // should we load our x parameters from a file, or generate them randomly?
        InputStream training_file = state.parameters.getResource(base.push(P_TRAINING_FILE), null);
        InputStream testing_file = state.parameters.getResource(base.push(P_TESTING_FILE), null);

        state.output.message("Loading benchmark data from files");
        if ((testing_file == null || training_file == null))            // must provide both
            {
            state.output.fatal("If you don't specify a problem type, you must provide a training file and a testing file",
                (training_file == null ? base.push(P_TRAINING_FILE) : base.push(P_TESTING_FILE)));
            }
        else  // load from files
            {
            try
                {
                int numInputs = 0;
                                    
                // first load the number of input variables
                Scanner scan = new Scanner(training_file);
                if (scan.hasNextInt()) 
                    numInputs = scan.nextInt();
                else state.output.fatal("Number of input variables not provided at beginning of training file ", base.push(P_TRAINING_FILE), null);
                                    
                // Load into an array list each element
                ArrayList input = new ArrayList();
                ArrayList output = new ArrayList();
                while(scan.hasNextDouble())
                    {
                    double[] in = new double[numInputs];
                    double out = 0;
                    for(int i = 0; i < numInputs; i++)
                        {
                        if (scan.hasNextDouble())
                            in[i] = scan.nextDouble();
                        else state.output.fatal("Non-normal number of data points in training file ", base.push(P_TRAINING_FILE), null);
                        }
                    if (scan.hasNextInt())
                        out = scan.nextInt();
                    else state.output.fatal("Non-normal number of data points in training file ", base.push(P_TRAINING_FILE), null);
                    input.add(in);
                    output.add(out);
                    }
                                    
                // dump to arrays
                int len = input.size();
                trainingInputs = new double[len][numInputs];
                trainingOutputs = new int[len];
                for(int i = 0; i < len; i++)
                    {
                    trainingInputs[i] = (double[])(input.get(i));
                    trainingOutputs[i] = ((Double)(output.get(i))).intValue();
                    }

    
                // same thing for testing


                scan = new Scanner(testing_file);
                if (scan.hasNextInt()) 
                    numInputs = scan.nextInt();
                else state.output.fatal("Number of input variables not provided at beginning of testing file ", base.push(P_TESTING_FILE), null);
                                    
                // Load into an array list each element
                input = new ArrayList();
                output = new ArrayList();
                while(scan.hasNextDouble())
                    {
                    double[] in = new double[numInputs];
                    double out = 0;
                    for(int i = 0; i < numInputs; i++)
                        {
                        if (scan.hasNextDouble())
                            in[i] = scan.nextDouble();
                        else state.output.fatal("Non-normal number of data points in testing file ", base.push(P_TESTING_FILE), null);
                        }
                    if (scan.hasNextInt())
                        out = scan.nextInt();
                    else state.output.fatal("Non-normal number of data points in testing file ", base.push(P_TESTING_FILE), null);
                    input.add(in);
                    output.add(out);
                    }
                                    
                // dump to arrays
                len = input.size();
                testingInputs = new double[len][numInputs];
                testingOutputs= new int[len];
                for(int i = 0; i < len; i++)
                    {
                    testingInputs[i] = (double[])(input.get(i));
                    testingOutputs[i] = ((Double)(output.get(i))).intValue();
                    }
                }
            catch (NumberFormatException e)
                {
                state.output.fatal("Some tokens in the file were not numbers.");
                }
            }
                                        
        Parameter param = new Parameter("gp.tc.0.fset");  // we assume we have a single tree
        String pval = state.parameters.getString(param, null);
                
    }


///// Evaluation.  evaluate(...) uses training cases, and describe(...) uses testing cases


    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            RegressionData input = (RegressionData)(this.input);

            int hits = 0;
            double sum = 0.0;
            for (int y=0;y<trainingInputs.length;y++)
                {
                currentValue = trainingInputs[y];
                ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);

                int error = error(input.x, trainingOutputs[y]);
                                
                sum += error;              
                }
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state,(float)sum/trainingInputs.length);
            f.hits = hits;
            ind.evaluated = true;
            }
        }

    //Random Interleaved Sampling evaluation
    public void evaluate_RI(EvolutionState state, Individual ind, int subpopulation, int threadnum, int[] samples)
    {
    if (!ind.evaluated)  // don't bother reevaluating
        {
        RegressionData input = (RegressionData)(this.input);

        int hits = 0;
        double sum = 0.0;
        for (int y=0;y<samples.length;y++)
            {
            currentValue = trainingInputs[samples[y]];
            ((GPIndividual)ind).trees[0].child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);

            int error = error(input.x, trainingOutputs[samples[y]]);
                            
            sum += error;              
            }
            
        // the fitness better be KozaFitness!
        KozaFitness f = ((KozaFitness)ind.fitness);
        f.setStandardizedFitness(state,(float)sum/samples.length);
        f.hits = hits;
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
            currentValue = testingInputs[y];
            ((GPIndividual)ind).trees[0].child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);

            int error = error(input.x, testingOutputs[y]);
                        

            sum += error;              
            }
                        
        // the fitness better be KozaFitness!
        KozaFitness f = (KozaFitness)(ind.fitness.clone());     // make a copy, we're just printing it out
        f.setStandardizedFitness(state,(float)sum/testingInputs.length);
        f.hits = hits;
        state.output.print("" + f.standardizedFitness() + " " + sum, log);
        //f.printFitnessForHumans(state, log);
        }


	@Override
	public double[] getSemantic(EvolutionState state, GPNode node,
			int threadnum) {
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

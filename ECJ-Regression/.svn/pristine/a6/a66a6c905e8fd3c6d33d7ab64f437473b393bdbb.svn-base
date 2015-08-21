/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.pta;
import ec.*;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.gp.*;
import ec.gp.koza.GPKozaDefaults;

/* 
 * CrossoverPipeline.java
 * 
 * Created: Mon Aug 30 19:15:21 1999
 * By: Sean Luke
 */


/**
 * CrossoverPipeline is a GPBreedingPipeline which performs a strongly-typed
 * version of 
 * Koza-style "Subtree Crossover".  Two individuals are selected,
 * then a single tree is chosen in each such that the two trees
 * have the same GPTreeConstraints.  Then a random node is chosen
 * in each tree such that the two nodes have the same return type.
 * If by swapping subtrees at these nodes the two trees will not
 * violate maximum depth constraints, then the trees perform the
 * swap, otherwise, they repeat the hunt for random nodes.
 *
 * <p>The pipeline tries at most <i>tries</i> times to a pair
 * of random nodes BOTH with valid swap constraints.  If it
 * cannot find any such pairs after <i>tries</i> times, it 
 * uses the pair of its last attempt.  If either of the nodes in the pair
 * is valid, that node gets substituted with the other node.  Otherwise
 * an individual invalid node isn't changed at all (it's "reproduced").
 *
 * <p><b>Compatibility with constraints.</b> 
 * Since Koza-I/II only tries 1 time, and then follows this policy, this is
 * compatible with Koza.  lil-gp either tries 1 time, or tries forever.
 * Either way, this is compatible with lil-gp.  My hacked 
 * <a href="http://www.cs.umd.edu/users/seanl/gp/">lil-gp kernel</a>
 * either tries 1 time, <i>n</i> times, or forever.  This is compatible
 * as well.
 *
 * <p>This pipeline typically produces up to 2 new individuals (the two newly-
 * swapped individuals) per produce(...) call.  If the system only
 * needs a single individual, the pipeline will throw one of the
 * new individuals away.  The user can also have the pipeline always
 * throw away the second new individual instead of adding it to the population.
 * In this case, the pipeline will only typically 
 * produce 1 new individual per produce(...) call.

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 2 * minimum typical number of individuals produced by each source, unless tossSecondParent
 is set, in which case it's simply the minimum typical number.

 <p><b>Number of Sources</b><br>
 2

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>tries</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of times to try finding valid pairs of nodes)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>maxdepth</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(maximum valid depth of a crossed-over subtree)</td></tr>
 
 <tr><td valign=top><i>base</i>.<tt>maxsize</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(maximum valid size, in nodes, of a crossed-over subtree)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(first tree for the crossover; if parameter doesn't exist, tree is picked at random)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.1</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(second tree for the crossover; if parameter doesn't exist, tree is picked at random.  This tree <b>must</b> have the same GPTreeConstraints as <tt>tree.0</tt>, if <tt>tree.0</tt> is defined.)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 <font size=-1>classname, inherits and != GPNodeSelector,<br>
 or String <tt>same<tt></font></td>
 <td valign=top>(GPNodeSelector for parent <i>n</i> (n is 0 or 1) If, for <tt>ns.1</tt> the value is <tt>same</tt>, then <tt>ns.1</tt> a copy of whatever <tt>ns.0</tt> is.  Note that the default version has no <i>n</i>)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>toss</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
 <td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to the population?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.koza.xover

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 <td>nodeselect<i>n</i> (<i>n</i> is 0 or 1)</td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class MichaelPipeline extends GPBreedingPipeline
    {
    private static final long serialVersionUID = 1;

    public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final String P_MAXSIZE = "maxsize";
    public static final String P_CROSSOVER = "xover";
    public static final String P_TOSS = "toss";
    //SSC
    public static final String P_SIMSIZE = "simsize";
    public static final String P_LSEN = "lsen";
    public static final String P_USEN = "usen";
    public int simsize;
    public double lsen;
    public double usen;

    public SimpleProblemForm problem;
    
    public static final int INDS_PRODUCED = 2;
    public static final int NUM_SOURCES = 2;
    public static final int NO_SIZE_LIMIT = -1;

    /** How the pipeline selects a node from individual 1 */
    public GPNodeSelector nodeselect1;

    /** How the pipeline selects a node from individual 2 */
    public GPNodeSelector nodeselect2;

    /** Is the first tree fixed?  If not, this is -1 */
    public int tree1;

    /** Is the second tree fixed?  If not, this is -1 */
    public int tree2;

    /** How many times the pipeline attempts to pick nodes until it gives up. */
    public int numTries;

    /** The deepest tree the pipeline is allowed to form.  Single terminal trees are depth 1. */
    public int maxDepth;

    /** The largest tree (measured as a nodecount) the pipeline is allowed to form. */
    public int maxSize;

    /** Should the pipeline discard the second parent after crossing over? */
    public boolean tossSecondParent;

    /** Temporary holding place for parents */
    public GPIndividual parents[];

    public MichaelPipeline() { parents = new GPIndividual[2]; }

    public Parameter defaultBase() { return GPKozaDefaults.base().push(P_CROSSOVER); }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        MichaelPipeline c = (MichaelPipeline)(super.clone());

        // deep-cloned stuff
        c.nodeselect1 = (GPNodeSelector)(nodeselect1.clone());
        c.nodeselect2 = (GPNodeSelector)(nodeselect2.clone());
        c.parents = (GPIndividual[]) parents.clone();

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();
        Parameter p = base.push(P_NODESELECTOR).push("0");
        Parameter d = def.push(P_NODESELECTOR).push("0");

        nodeselect1 = (GPNodeSelector)
            (state.parameters.getInstanceForParameter(
                p,d, GPNodeSelector.class));
        nodeselect1.setup(state,p);

        p = base.push(P_NODESELECTOR).push("1");
        d = def.push(P_NODESELECTOR).push("1");

        if (state.parameters.exists(p,d) &&
            state.parameters.getString(p,d).equals(V_SAME))
            // can't just copy it this time; the selectors
            // use internal caches.  So we have to clone it no matter what
            nodeselect2 = (GPNodeSelector)(nodeselect1.clone());
        else
            {
            nodeselect2 = (GPNodeSelector)
                (state.parameters.getInstanceForParameter(
                    p,d, GPNodeSelector.class));
            nodeselect2.setup(state,p);
            }

        numTries = state.parameters.getInt(base.push(P_NUM_TRIES),
            def.push(P_NUM_TRIES),1);
        if (numTries == 0)
            state.output.fatal("GPCrossover Pipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));

        maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),def.push(P_MAXDEPTH),1);
        if (maxDepth==0)
            state.output.fatal("GPCrossover Pipeline has an invalid maximum depth (it must be >= 1).",base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));

        maxSize = NO_SIZE_LIMIT;
        if (state.parameters.exists(base.push(P_MAXSIZE), def.push(P_MAXSIZE)))
            {
            maxSize = state.parameters.getInt(base.push(P_MAXSIZE), def.push(P_MAXSIZE), 1);
            if (maxSize < 1)
                state.output.fatal("Maximum tree size, if defined, must be >= 1");
            }
        
        tree1 = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0)))
            {
            tree1 = state.parameters.getInt(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0),0);
            if (tree1==-1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }

        tree2 = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push(""+1),
                def.push(P_TREE).push(""+1)))
            {
            tree2 = state.parameters.getInt(base.push(P_TREE).push(""+1),
                def.push(P_TREE).push(""+1),0);
            if (tree2==-1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }
        tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
            def.push(P_TOSS),false);

        //SSC
        simsize = state.parameters.getInt(base.push(P_SIMSIZE), def.push(P_SIMSIZE));
        lsen = state.parameters.getDouble(base.push(P_LSEN), def.push(P_LSEN));
        usen = state.parameters.getDouble(base.push(P_USEN), def.push(P_USEN));
    
        problem = (SimpleProblemForm)(state.evaluator.p_problem.clone());
        }
    
	private double getSimilarity( EvolutionState state, GPNode p1, GPNode p2, int threadnum)
	{
		
		double sum = 0;
		double[] sim1 = problem.getSemantic(state, p1, threadnum);
		double[] sim2 = problem.getSemantic(state, p2, threadnum);
		for (int i = 0; i < sim1.length; i++)
		{
			sum += Math.abs(sim1[i] - sim2[i]);
			
			double temp = sum / sim1.length;
			if (temp > usen)
				return temp;
		}
		return sum/sim1.length;
	}

    /** Returns 2 * minimum number of typical individuals produced by any sources, else
        1* minimum number if tossSecondParent is true. */
    public int typicalIndsProduced()
        {
        return (tossSecondParent? minChildProduction(): minChildProduction()*2);
        }

    /** Returns true if inner1 can feasibly be swapped into inner2's position. */

    public boolean verifyPoints(final GPInitializer initializer,
        final GPNode inner1, final GPNode inner2)
        {
        // first check to see if inner1 is swap-compatible with inner2
        // on a type basis
        if (!inner1.swapCompatibleWith(initializer, inner2)) return false;

        // next check to see if inner1 can fit in inner2's spot
        if (inner1.depth()+inner2.atDepth() > maxDepth) return false;

        // check for size
        // NOTE: this is done twice, which is more costly than it should be.  But
        // on the other hand it allows us to toss a child without testing both times
        // and it's simpler to have it all here in the verifyPoints code.  
        if (maxSize != NO_SIZE_LIMIT)
            {
            // first easy check
            int inner1size = inner1.numNodes(GPNode.NODESEARCH_ALL);
            int inner2size = inner2.numNodes(GPNode.NODESEARCH_ALL);
            if (inner1size > inner2size)  // need to test further
                {
                // let's keep on going for the more complex test
                GPNode root2 = ((GPTree)(inner2.rootParent())).child;
                int root2size = root2.numNodes(GPNode.NODESEARCH_ALL);
                if (root2size - inner2size + inner1size > maxSize)  // take root2, remove inner2 and swap in inner1.  Is it still small enough?
                    return false;
                }
            }

        // checks done!
        return true;
        }


    private boolean checkGeometricUy(double[] semanticChild, double[] semanticParent1, double[] semanticParent2)
    {
    	// Check semanticChild = alpha * semanticParent1 + (1-alpha)*semanticParent2

    	if(semanticChild.length != semanticParent1.length || semanticChild.length != semanticParent2.length
    			|| semanticParent1.length != semanticParent2.length || semanticChild.length==0 ||
    			semanticParent1.length == 0 || semanticParent2.length == 0)
    		return false;
    
		double alpha = (semanticChild[0] - semanticParent2[0]) / (semanticParent1[0] - semanticParent2[0]);
		if (alpha > 0 && alpha < 1)
		{
			for(int i = 1; i < semanticChild.length; i++)
			{
				double alpha1 = (semanticChild[i] - semanticParent2[i]) / (semanticParent1[i] - semanticParent2[i]);

				if (alpha1 != alpha)
					return false;
			}
		}
    	else
    	{
    		alpha = (semanticChild[0] - semanticParent1[0]) / (semanticParent2[0] - semanticParent1[0]);
    		if(alpha > 0 && alpha < 1)
    		{
    			for(int i = 1; i < semanticChild.length; i++)
    			{
    	    		double alpha1 = (semanticChild[i] - semanticParent1[i]) / (semanticParent2[i] - semanticParent1[i]);
    				
    	    		if(alpha1 != alpha )
    	    			return false;
    			}
    		}
    		else
    			return false;
    			
    	}
    	
    	return true;
    }
    
    private boolean checkGeometric(double[] semanticChild, double[] semanticParent1, double[] semanticParent2)
    {
    	// Check semanticChild = alpha * semanticParent1 + (1-alpha)*semanticParent2

    	if(semanticChild.length != semanticParent1.length || semanticChild.length != semanticParent2.length
    			|| semanticParent1.length != semanticParent2.length || semanticChild.length==0 ||
    			semanticParent1.length == 0 || semanticParent2.length == 0)
    		return false;

		double alpha = (semanticChild[0] - semanticParent2[0]) / (semanticParent1[0] - semanticParent2[0]);
		if (alpha >= 0 && alpha <= 1)
		{
			for(int i = 1; i < semanticChild.length; i++)
			{
				alpha = (semanticChild[i] - semanticParent2[i]) / (semanticParent1[i] - semanticParent2[i]);

				if (alpha < 0 || alpha > 1)
					return false;
			}
		}
    	else
    	{
    		alpha = (semanticChild[0] - semanticParent1[0]) / (semanticParent2[0] - semanticParent1[0]);
    		if(alpha >= 0 && alpha <= 1)
    		{
    			for(int i = 1; i < semanticChild.length; i++)
    			{
    	    		alpha = (semanticChild[i] - semanticParent1[i]) / (semanticParent2[i] - semanticParent1[i]);
    				
    	    		if(alpha < 0 || alpha > 1 )
    	    			return false;
    			}
    		}
    		else
    			return false;
    			
    	}
		return true;

    }
    
    private boolean checkChildBetterParent(double[] semanticChild, 
    		double[] semanticParent1, double[] semanticParent2, double[] goal)
    {
    	double fitnessC = 0.0;
    	double fitnessP1 = 0.0;
    	double fitnessP2 = 0.0;
    	
    	for(int i = 0; i < goal.length; i++)
    	{
    		fitnessC += Math.abs(semanticChild[i] - goal[i]);
    		fitnessP1 += Math.abs(semanticParent1[i] - goal[i]);
    		fitnessP2 += Math.abs(semanticParent2[i] - goal[i]);
    	}
    	
    	if(fitnessC < fitnessP1 || fitnessC < fitnessP2)
    		return true;
    	else
    		return false;
    	
    }
    
    
    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

        {
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already



        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                sources[0].produce(2,2,0,subpopulation,parents,state,thread);
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,parents,state,thread);
                }
            
            // at this point, parents[] contains our two selected individuals
            
            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].trees.length))
                // uh oh
                state.output.fatal("GP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].trees.length))
                // uh oh
                state.output.fatal("GP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int t1=0; int t2=0;
            if (tree1==TREE_UNFIXED || tree2==TREE_UNFIXED) 
                {
                do
                    // pick random trees  -- their GPTreeConstraints must be the same
                    {
                    if (tree1==TREE_UNFIXED) 
                        if (parents[0].trees.length > 1)
                            t1 = state.random[thread].nextInt(parents[0].trees.length);
                        else t1 = 0;
                    else t1 = tree1;

                    if (tree2==TREE_UNFIXED) 
                        if (parents[1].trees.length>1)
                            t2 = state.random[thread].nextInt(parents[1].trees.length);
                        else t2 = 0;
                    else t2 = tree2;
                    } while (parents[0].trees[t1].constraints(initializer) != parents[1].trees[t2].constraints(initializer));
                }
            else
                {
                t1 = tree1;
                t2 = tree2;
                // make sure the constraints are okay
                if (parents[0].trees[t1].constraints(initializer) 
                    != parents[1].trees[t2].constraints(initializer)) // uh oh
                    state.output.fatal("GP Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
                }



            // validity results...
            boolean res1 = false;
            boolean res2 = false;
            
            
            // prepare the nodeselectors
            nodeselect1.reset();
            nodeselect2.reset();
            
            
            // pick some nodes
            
            GPNode p1=null;
            GPNode p2=null;
            
            GPNode temp_p1 = null;
            GPNode temp_p2 = null;
            
            // semantic of two parents
            double[] semanticParent1 = problem.getSemantic(state, parents[0].trees[t1].child, thread);
            double[] semanticParent2 = problem.getSemantic(state, parents[1].trees[t2].child, thread);
            boolean geometricSatisfy = false;
            
            for(int x=0;x<numTries;x++)
            {
                // pick a node in individual 1
                p1 = nodeselect1.pickNode(state,subpopulation,thread,parents[0],parents[0].trees[t1]);
                temp_p1 = (GPNode) p1.clone();
                //System.out.println(p1.toStringForHumans());
                // pick a node in individual 2
                p2 = nodeselect2.pickNode(state,subpopulation,thread,parents[1],parents[1].trees[t2]);
                temp_p2 = (GPNode)p2.clone();
            	//Michael's Ideal
            	for (int i = 0; i < this.simsize; i++)
            	{

            		//Get SSC between p1 and p2
                    double ft = getSimilarity(state, temp_p1, temp_p2, thread);
                    //If OK then break
                    //if (ft > this.lsen && ft < this.usen)
                    if(true) // not check ssc condition
                    {
                    	//System.out.println("Parent 1: "); parents[0].trees[0].printTree(state, 0);
                    	//System.out.println("Parent 2: "); parents[1].trees[0].printTree(state, 0);
                    	//System.out.println("Node 1: "); System.out.println(p1.makeLispTree());
                    	//System.out.println("Node 2: "); System.out.println(p2.makeLispTree());

                    	
                    	//break;
                        // check for depth and swap-compatibility limits
                        res1 = verifyPoints(initializer,temp_p2,temp_p1);  // p2 can fill p1's spot -- order is important!
                        if (n-(q-start)<2 || tossSecondParent) res2 = true;
                        else res2 = verifyPoints(initializer,temp_p1,temp_p2);  // p1 can fill p2's spot -- order is important!
                        
                        // did we get something that had both nodes verified?
                        // we reject if EITHER of them is invalid.  This is what lil-gp does.
                        // Koza only has numTries set to 1, so it's compatible as well.
                        if (res1 && res2) 
                        {
                        	//  Temporarily perform crossover
                            GPIndividual temp_child1 = (GPIndividual)(parents[0].lightClone());
                            GPIndividual temp_child2 = null;
                            if (n-(q-start)>=2 && !tossSecondParent) 
                            	temp_child2 = (GPIndividual)(parents[1].lightClone());
                            
                            // Fill in various tree information that didn't get filled in there
                            temp_child1.trees = new GPTree[parents[0].trees.length];
                            if (n-(q-start)>=2 && !tossSecondParent) 
                            	temp_child2.trees = new GPTree[parents[1].trees.length];
                            
                            // at this point, p1 or p2, or both, may be null.
                            // If not, swap one in.  Else just copy the parent.
                            
                            for(int y=0;y<temp_child1.trees.length;y++)
                            {
                                if (y==t1 && res1)  // we've got a tree with a kicking cross position!
                                { 
                                    temp_child1.trees[y] = (GPTree)(parents[0].trees[y].lightClone());
                                    temp_child1.trees[y].owner = temp_child1;
                                    temp_child1.trees[y].child = parents[0].trees[y].child.cloneReplacing(temp_p2,temp_p1); 
                                    temp_child1.trees[y].child.parent = temp_child1.trees[y];
                                    temp_child1.trees[y].child.argposition = 0;
                                    temp_child1.evaluated = false; 
                                }  // it's changed
                                else 
                                {
                                    temp_child1.trees[y] = (GPTree)(parents[0].trees[y].lightClone());
                                    temp_child1.trees[y].owner = temp_child1;
                                    temp_child1.trees[y].child = (GPNode)(parents[0].trees[y].child.clone());
                                    temp_child1.trees[y].child.parent = temp_child1.trees[y];
                                    temp_child1.trees[y].child.argposition = 0;
                                }
                            }
                            
                            if (n-(q-start)>=2 && !tossSecondParent) 
                                for(int y=0;y<temp_child2.trees.length;y++)
                                {
                                    if (y==t2 && res2)  // we've got a tree with a kicking cross position!
                                    { 
                                        temp_child2.trees[y] = (GPTree)(parents[1].trees[y].lightClone());           
                                        temp_child2.trees[y].owner = temp_child2;
                                        temp_child2.trees[y].child = parents[1].trees[y].child.cloneReplacing(temp_p1,temp_p2); 
                                        temp_child2.trees[y].child.parent = temp_child2.trees[y];
                                        temp_child2.trees[y].child.argposition = 0;
                                        temp_child2.evaluated = false; 
                                    } // it's changed
                                    else 
                                    {
                                        temp_child2.trees[y] = (GPTree)(parents[1].trees[y].lightClone());           
                                        temp_child2.trees[y].owner = temp_child2;
                                        temp_child2.trees[y].child = (GPNode)(parents[1].trees[y].child.clone());
                                        temp_child2.trees[y].child.parent = temp_child2.trees[y];
                                        temp_child2.trees[y].child.argposition = 0;
                                    }
                                }

                        	// check geometric property
                            
                            double[] semanticChild1 = problem.getSemantic(state, temp_child1.trees[t1].child, thread);

                            if(checkChildBetterParent(semanticChild1, semanticParent1, semanticParent2, problem.getOutputTraining()))
                        		state.numOfSGX[state.generation][2] += 1;
                            
                            if(checkGeometric(semanticChild1, semanticParent1, problem.getOutputTraining())
                            		|| checkGeometric(semanticChild1, semanticParent2, problem.getOutputTraining()))
                            //if(checkGeometric(semanticChild1, semanticParent1, semanticParent2))
                            {
                            	//state.numOfSGX[state.generation][0] += 1;
                            	geometricSatisfy = true;
//                            	if(checkChildBetterParent(semanticChild1, semanticParent1, semanticParent2, problem.getOutputTraining()))
//                            		state.numOfSGX[state.generation][2] += 1;
                            	break;
                            }
                            else
                            	if (n-(q-start)>=2 && !tossSecondParent)
                            	{
                            		double[] semanticChild2 = problem.getSemantic(state, temp_child2.trees[t2].child, thread);

                            		if(checkChildBetterParent(semanticChild2, semanticParent1, semanticParent2, problem.getOutputTraining()))
                                		state.numOfSGX[state.generation][2] += 1;
                            		
                            		//if(checkGeometric(semanticChild2, semanticParent1, semanticParent2))
                            		if(checkGeometric(semanticChild2, semanticParent1, problem.getOutputTraining())
                            				|| checkGeometric(semanticChild2, semanticParent2, problem.getOutputTraining()))
                            		{
                            			//state.numOfSGX[state.generation][0] += 1;
                            			geometricSatisfy = true;
//                                    	if(checkChildBetterParent(semanticChild2, semanticParent1, semanticParent2, problem.getOutputTraining()))
//                                    		state.numOfSGX[state.generation][2] += 1;

                            			break;
                            		}
                            	}
                        }
                    } // end if ssc satisfy
                    
                    
                	// pick a node in individual 1 
                    temp_p1 = nodeselect1.pickNodeDefaultRandom(state,subpopulation,thread,parents[0],parents[0].trees[t1]);
                    //temp_p1 = nodeselect1.pickNode(state, subpopulation, thread, parents[0], parents[0].trees[t1]);
                    
                    // pick a node in individual 2 
                    temp_p2 = nodeselect2.pickNodeDefaultRandom(state,subpopulation,thread,parents[1],parents[1].trees[t2]);                    
                    //temp_p2 = nodeselect2.pickNode(state,subpopulation,thread,parents[1],parents[1].trees[t2]);                    

            	} //end for simsize

                // check for depth and swap-compatibility limits
                res1 = verifyPoints(initializer,p2,p1);  // p2 can fill p1's spot -- order is important!
                if (n-(q-start)<2 || tossSecondParent) res2 = true;
                else res2 = verifyPoints(initializer,p1,p2);  // p1 can fill p2's spot -- order is important!
                
                // did we get something that had both nodes verified?
                // we reject if EITHER of them is invalid.  This is what lil-gp does.
                // Koza only has numTries set to 1, so it's compatible as well.
                if (res1 && res2) break;
            } // end for numTries
            
            
            if(geometricSatisfy)
            {
            	state.numOfSGX[state.generation][0] += 1;
            	p1 = (GPNode)temp_p1.clone();
            	p2 = (GPNode)temp_p2.clone();
            }
            else
            {
            	state.numOfSGX[state.generation][1] += 1;
            }

            // at this point, res1 AND res2 are valid, OR either res1
            // OR res2 is valid and we ran out of tries, OR neither is
            // valid and we ran out of tries.  So now we will transfer
            // to a tree which has res1 or res2 valid, otherwise it'll
            // just get replicated.  This is compatible with both Koza
            // and lil-gp.
            

            // at this point I could check to see if my sources were breeding
            // pipelines -- but I'm too lazy to write that code (it's a little
            // complicated) to just swap one individual over or both over,
            // -- it might still entail some copying.  Perhaps in the future.
            // It would make things faster perhaps, not requiring all that
            // cloning.

            
            
            // Create some new individuals based on the old ones -- since
            // GPTree doesn't deep-clone, this should be just fine.  Perhaps we
            // should change this to proto off of the main species prototype, but
            // we have to then copy so much stuff over; it's not worth it.
                    
            GPIndividual j1 = (GPIndividual)(parents[0].lightClone());
            GPIndividual j2 = null;
            if (n-(q-start)>=2 && !tossSecondParent) j2 = (GPIndividual)(parents[1].lightClone());
            
            // Fill in various tree information that didn't get filled in there
            j1.trees = new GPTree[parents[0].trees.length];
            if (n-(q-start)>=2 && !tossSecondParent) j2.trees = new GPTree[parents[1].trees.length];
            
            // at this point, p1 or p2, or both, may be null.
            // If not, swap one in.  Else just copy the parent.
            
            for(int x=0;x<j1.trees.length;x++)
                {
                if (x==t1 && res1)  // we've got a tree with a kicking cross position!
                    { 
                    j1.trees[x] = (GPTree)(parents[0].trees[x].lightClone());
                    j1.trees[x].owner = j1;
                    j1.trees[x].child = parents[0].trees[x].child.cloneReplacing(p2,p1); 
                    j1.trees[x].child.parent = j1.trees[x];
                    j1.trees[x].child.argposition = 0;
                    j1.evaluated = false; 
                    }  // it's changed
                else 
                    {
                    j1.trees[x] = (GPTree)(parents[0].trees[x].lightClone());
                    j1.trees[x].owner = j1;
                    j1.trees[x].child = (GPNode)(parents[0].trees[x].child.clone());
                    j1.trees[x].child.parent = j1.trees[x];
                    j1.trees[x].child.argposition = 0;
                    }
                }
            
            if (n-(q-start)>=2 && !tossSecondParent) 
                for(int x=0;x<j2.trees.length;x++)
                    {
                    if (x==t2 && res2)  // we've got a tree with a kicking cross position!
                        { 
                        j2.trees[x] = (GPTree)(parents[1].trees[x].lightClone());           
                        j2.trees[x].owner = j2;
                        j2.trees[x].child = parents[1].trees[x].child.cloneReplacing(p1,p2); 
                        j2.trees[x].child.parent = j2.trees[x];
                        j2.trees[x].child.argposition = 0;
                        j2.evaluated = false; 
                        } // it's changed
                    else 
                        {
                        j2.trees[x] = (GPTree)(parents[1].trees[x].lightClone());           
                        j2.trees[x].owner = j2;
                        j2.trees[x].child = (GPNode)(parents[1].trees[x].child.clone());
                        j2.trees[x].child.parent = j2.trees[x];
                        j2.trees[x].child.argposition = 0;
                        }
                    }
            
            // add the individuals to the population
            inds[q] = j1;
            q++;
            if (q<n+start && !tossSecondParent)
                {
                inds[q] = j2;
                q++;
                }
            }
        return n;
        }
    }

/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.pta;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import ec.*;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.app.regression.func.RegERC;
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
 * @author PTA : SGXMSC (SGXM + SC)
 * @version 1.0 
 */

public class SGXMSCPipeline extends GPBreedingPipeline
    {
    private static final long serialVersionUID = 1;

    public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final String P_MAXSIZE = "maxsize";
    public static final String P_CROSSOVER = "xover";
    public static final String P_TOSS = "toss";
    //SGC
    public static final String P_N_CHILDREN = "n-children";
    public int N_children;
    
    public static final String P_SUBTREE_SIZE_RATE = "subtreesize-rate";
    public double subtree_size_rate;
    
    public static final String P_SGC_PRO = "sgc-pro";
    public double sgc_pro;
    
    public SimpleProblemForm problem;
    public GPFunctionSet fs;
    
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

    
    public GPNode templateTree;
    
    /** Temporary holding place for parents */
    public GPIndividual parents[];

    public SGXMSCPipeline() { parents = new GPIndividual[2]; }

    public Parameter defaultBase() { return GPKozaDefaults.base().push(P_CROSSOVER); }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        SGXMSCPipeline c = (SGXMSCPipeline)(super.clone());

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
      //evaluator = (SimpleEvaluator)state.evaluator;
        problem = (SimpleProblemForm)state.evaluator.p_problem.clone();
        
        
        N_children = state.parameters.getInt(base.push(P_N_CHILDREN),
                def.push(P_N_CHILDREN),1);
        
        //subtree_size_rate = state.parameters.getDouble(base.push(P_SUBTREE_SIZE_RATE), def.push(P_SUBTREE_SIZE_RATE));
        
        sgc_pro = state.parameters.getDouble(base.push(P_SGC_PRO), def.push(P_SGC_PRO));
        
        
        GPInitializer initializer = ((GPInitializer)state.initializer);
        fs = initializer.treeConstraints[0].functionset;
       
        templateTree = (GPNode) (((GPNode[])fs.nodesByName.get("+"))[0]).lightClone();
        GPNode left =  (GPNode) (((GPNode[])fs.nodesByName.get("*"))[0]).lightClone();
        GPNode right = (GPNode) (((GPNode[])fs.nodesByName.get("*"))[0]).lightClone();
        //RegERC ercNode1 = (RegERC)((((GPNode[])fs.nodesByName.get("ERC"))[0]).lightClone());
        // 
        //generate TR node that will be replated by random tree
        //GPNode TRNode = new GPNode();
        //ercNode.constraints = 6;
        //ercNode.children = new GPNode[0];
        
        templateTree.children = new GPNode[2];
        templateTree.children[0] = left;
        templateTree.children[1] = right;
        
        left.children = new GPNode[2];
        left.children[0] = null;
        left.children[1] = null;
        
        right.children = new GPNode[2];
        right.children[0] = null;
        right.children[1] = null;
        
        }
	private double getSimilarity( EvolutionState state, GPNode p1, GPNode p2, int threadnum)
	{
		
		double sum = 0;
		double[] sim1 = problem.getSemantic(state, p1, threadnum);
		double[] sim2 = problem.getSemantic(state, p2, threadnum);
		for (int i = 0; i < sim1.length; i++)
		{
			sum += Math.abs(sim1[i] - sim2[i]);
			
		}
		return sum/sim1.length;
	}

	//return null if do not get one
	private GPNode getSubTree(EvolutionState state, int subpopulation, GPIndividual parent, int tree, int threadnum)
	{
		//if(state.generation < 5)
		//	return parent.trees[tree].child;
		//return (GPNode)parent.trees[tree].child.clone();
		GPNode reVal = null;
		double bestSim = Double.MAX_VALUE;
		int bestPosition = -1;
		GPNode p1 = null;
		double[] sim1 = problem.getSemantic(state, parent.trees[tree].child, threadnum);

		int sizeParent = parent.trees[tree].child.numNodes(GPNode.NODESEARCH_ALL);
		int non_terminals = parent.trees[tree].child.numNodes(GPNode.NODESEARCH_NONTERMINALS);
				
		GPNodeGatherer gatherer = new GPNodeGatherer();
			
		for(int x=0; x<N_children; x++)
        {
			int position = -1;
			if(non_terminals <=0)
				p1 = parent.trees[tree].child;
			else
			{
				position = state.random[threadnum].nextInt(non_terminals);

				parent.trees[tree].child.nodeInPosition(position,
	                    gatherer,
	                    GPNode.NODESEARCH_NONTERMINALS);
				p1 = gatherer.node;
				// nodeselect1.reset();
				// pick a node in individual 1
				// p1 = nodeselect1.pickNode(state,subpopulation,threadnum,parent,parent.trees[tree]);           
			
				//check size
				//int size = p1.numNodes(GPNode.NODESEARCH_ALL);
				//if (size < sizeParent * subtree_size_rate)
				//	continue;
			}
			
            double[] sim2 = problem.getSemantic(state, p1, threadnum);
            double sum = 0.0;
            for(int i = 0; i < sim1.length; i++)
            	sum += Math.abs(sim1[i]-sim2[i]);
            
            sum = sum/sim1.length;
            
            if (sum >= 0 && sum < bestSim)
            {
            	bestSim = sum;
            	bestPosition = position;
            	//reVal = (GPNode)p1.clone();
            }
        }
		//if choose one node
		if(bestPosition > -1)
		{
			parent.trees[tree].child.nodeInPosition(bestPosition,
                    gatherer,
                    GPNode.NODESEARCH_NONTERMINALS);
			reVal = (GPNode) gatherer.node.clone();
		}
		return reVal;
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

        // random select one tree from treelib
        int treeIndex = (new Random()).nextInt(state.maxTreelibSize);
        
        GPNode TR = state.treelib[treeIndex];
        
        //create tree: 1 - TR
        GPNode OneSubTR =  (GPNode) (((GPNode[])fs.nodesByName.get("-"))[0]).lightClone();
        OneSubTR.children = new GPNode[2];

        RegERC oneNode = new RegERC();
        oneNode.constraints = 6;
        oneNode.children = new GPNode[0];
        oneNode.value = 1;
        
        OneSubTR.children[0] = oneNode;
        OneSubTR.children[1] = (GPNode)TR.clone();


        GPInitializer initializer = ((GPInitializer)state.initializer);
		
		//if(state.generation > 9 && state.generation % 5 == 0)
		//	numChildren = 12 + (4 * state.generation)/5;


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
     	    if(state.generation < 0) // 0 means do not run this
     	    {
            	state.numOfSGX[state.generation][1] += 1;
            	
                // validity results...
                boolean res1 = false;
                boolean res2 = false;
                
                
                // prepare the nodeselectors
                nodeselect1.reset();
                nodeselect2.reset();
                
                
                // pick some nodes
                
                GPNode p1=null;
                GPNode p2=null;
                
                for(int x=0;x<numTries;x++)
                {
                    // pick a node in individual 1
                    p1 = nodeselect1.pickNode(state,subpopulation,thread,parents[0],parents[0].trees[0]);
                    
                    // pick a node in individual 2
                    p2 = nodeselect2.pickNode(state,subpopulation,thread,parents[1],parents[1].trees[0]);
                    
                    // check for depth and swap-compatibility limits
                    res1 = verifyPoints(initializer,p2,p1);  // p2 can fill p1's spot -- order is important!
                    if (n-(q-start)<2 || tossSecondParent) res2 = true;
                    else res2 = verifyPoints(initializer,p1,p2);  // p1 can fill p2's spot -- order is important!
                    
                    // did we get something that had both nodes verified?
                    // we reject if EITHER of them is invalid.  This is what lil-gp does.
                    // Koza only has numTries set to 1, so it's compatible as well.
                    if (res1 && res2) break;
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
                    if (x==0 && res1)  // we've got a tree with a kicking cross position!
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
                        if (x==0 && res2)  // we've got a tree with a kicking cross position!
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
     	    else
     	    {
     	    	double rand = state.random[thread].nextDouble();
     	    	
     	    	GPNode subparent1 = null;
     	    	GPNode subparent2 = null;

     	    	//chon cay con
     	    	subparent1 = getSubTree(state, subpopulation, parents[0], 0, thread);

     	    	//chon con 2

     	    	subparent2 = getSubTree(state, subpopulation, parents[1], 0, thread);
        		

     	    	if(rand < sgc_pro && subparent1 != null && subparent2 != null)
     	    	{
     	    		state.numOfSGX[state.generation][0] +=1;
                 	    		
     	    		//child 1
     	    		
     	    		templateTree.children[0].children[0] = (GPNode)TR.clone();
     	    		templateTree.children[0].children[1] = (GPNode)subparent1.clone();
            		
     	    		templateTree.children[1].children[0] = (GPNode)OneSubTR.clone();
     	    		templateTree.children[1].children[1] = (GPNode)subparent2.clone();
     	    		
     	    		
     	    		GPNode child1 = (GPNode)templateTree.clone();           	
            
     	    		GPIndividual j1 = (GPIndividual)(parents[0].lightClone());
            
            // 		Fill in various tree information that didn't get filled in there
     	    		j1.trees = new GPTree[parents[0].trees.length];
            
            // 		at this point, p1 or p2, or both, may be null.
            // 		If not, swap one in.  Else just copy the parent.
            
     	    		for(int x=0;x<j1.trees.length;x++)
     	    		{
     	    			j1.trees[x] = (GPTree)(parents[0].trees[x].lightClone());
     	    			j1.trees[x].owner = j1;
     	    			j1.trees[x].child = child1;
     	    			j1.trees[x].child.parent = j1.trees[x];
     	    			j1.trees[x].child.argposition = 0;
     	    			j1.evaluated = false;            	
     	    		}
            
     	    		// add the individuals to the population
     	    		inds[q] = j1;
     	    		q++;
     	    		
     	    		//child 2
     	    		templateTree.children[0].children[0] = (GPNode)OneSubTR.clone();
     	    		templateTree.children[0].children[1] = (GPNode)subparent1.clone();
            		
     	    		templateTree.children[1].children[0] = (GPNode)TR.clone();
     	    		templateTree.children[1].children[1] = (GPNode)subparent2.clone();
     	    		
     	    		
     	    		GPNode child2 = (GPNode)templateTree.clone();           	

//            		System.out.println(root.makeLispTree());
            
     	    		GPIndividual j2 = (GPIndividual)(parents[0].lightClone());
            
     	    		// 	Fill in various tree information that didn't get filled in there
     	    		j2.trees = new GPTree[parents[0].trees.length];
            
     	    		// at this point, p1 or p2, or both, may be null.
     	    		// If not, swap one in.  Else just copy the parent.
            
     	    		for(int x=0;x<j2.trees.length;x++)
     	    		{
     	    			j2.trees[x] = (GPTree)(parents[0].trees[x].lightClone());
     	    			j2.trees[x].owner = j2;
     	    			j2.trees[x].child = child2;
     	    			j2.trees[x].child.parent = j2.trees[x];
     	    			j2.trees[x].child.argposition = 0;
     	    			j2.evaluated = false;            	
     	    		}
            
     	    		// add the individuals to the population
     	    		if (q<n+start && !tossSecondParent)
     	    		{
     	    			inds[q] = j2;
     	    			q++;
     	    		}
	
	            }
     	    }
        }
    return n;
    }
}

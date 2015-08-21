
package ec.pta;
import java.lang.reflect.Array;
import java.util.ArrayList;

import ec.*;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import ec.app.regression.func.RegERC;
import ec.gp.*;
import ec.gp.koza.GPKozaDefaults;


 /*
 * @author Pham Tuan Anh SGXE + MSSC
 * @version 1.0 
 */

public class SGXEMSSCPipeline extends GPBreedingPipeline
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

    public SGXEMSSCPipeline() { parents = new GPIndividual[2]; }

    public Parameter defaultBase() { return GPKozaDefaults.base().push(P_CROSSOVER); }

    public int numSources() { return NUM_SOURCES; }
    
	GPNodeGatherer gatherer; // = new GPNodeGatherer();


    public Object clone()
        {
        SGXEMSSCPipeline c = (SGXEMSSCPipeline)(super.clone());

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
        
        gatherer = new GPNodeGatherer();
        
        N_children = state.parameters.getInt(base.push(P_N_CHILDREN),
                def.push(P_N_CHILDREN),30);
        
        GPInitializer initializer = ((GPInitializer)state.initializer);
        fs = initializer.treeConstraints[0].functionset;
       
        templateTree = (GPNode) (((GPNode[])fs.nodesByName.get("+"))[0]).lightClone();
        GPNode left =  (GPNode) (((GPNode[])fs.nodesByName.get("*"))[0]).lightClone();
        GPNode right = (GPNode) (((GPNode[])fs.nodesByName.get("*"))[0]).lightClone();
        //RegERC ercNode1 = (RegERC)((((GPNode[])fs.nodesByName.get("ERC"))[0]).lightClone());
        RegERC ercNode = new RegERC();
        ercNode.constraints = 6;
        ercNode.children = new GPNode[0];
        
        templateTree.children = new GPNode[2];
        templateTree.children[0] = left;
        templateTree.children[1] = right;
        
        left.children = new GPNode[2];
        left.children[0] = ercNode;
        left.children[1] = null;
        
        right.children = new GPNode[2];
        RegERC newERCNode = new RegERC();// (RegERC)ercNode.lightClone();
        right.children[0] = (GPNode)ercNode.lightClone();// newERCNode;
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
		double bestSim = 10000;
		int bestPosition = -1;
		GPNode p1 = null;
		double[] sim1 = problem.getSemantic(state, parent.trees[tree].child, threadnum);

		int sizeParent = parent.trees[tree].child.numNodes(GPNode.NODESEARCH_ALL);
		int non_terminals = parent.trees[tree].child.numNodes(GPNode.NODESEARCH_NONTERMINALS);
				
		GPNodeGatherer gatherer = new GPNodeGatherer();
			
		for(int x=0;x<N_children;x++)
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
				int size = p1.numNodes(GPNode.NODESEARCH_ALL);
				if (size < sizeParent/5)
					continue;
			}
			
            double[] sim2 = problem.getSemantic(state, p1, threadnum);
            double sum = 0.0;
            for(int i = 0; i < sim1.length; i++)
            	sum += Math.abs(sim1[i]-sim2[i]);
            
            sum = sum/sim1.length;
            
            if (sum > 0 && sum < bestSim)
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
     	    // 0 - thuc hien SGC tu gen dau tien
     	    if(state.generation < 0)
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
        		

     	    	if(rand < 0.3 && subparent1 != null && subparent2 != null)
     	    	{
     	    		state.numOfSGX[state.generation][0] +=1;
            	
     	    		RegERC ercNode = (RegERC)(templateTree.children[0].children[0]);
     	    		ercNode.resetNode(state, thread);
     	    		templateTree.children[0].children[1] = subparent1;
            		
     	    		double anpha = ercNode.value;
            
     	    		RegERC newERCNode = (RegERC)(templateTree.children[1].children[0]);
     	    		newERCNode.value = 1-anpha;
     	    		templateTree.children[1].children[1] = subparent2;

     	    		GPNode root = (GPNode)templateTree.clone();

//            		System.out.println(root.makeLispTree());
            
     	    		GPIndividual j1 = (GPIndividual)(parents[0].lightClone());
            
            // 		Fill in various tree information that didn't get filled in there
     	    		j1.trees = new GPTree[parents[0].trees.length];
            
            // 		at this point, p1 or p2, or both, may be null.
            // 		If not, swap one in.  Else just copy the parent.
            
     	    		for(int x=0;x<j1.trees.length;x++)
     	    		{
     	    			j1.trees[x] = (GPTree)(parents[0].trees[x].lightClone());
     	    			j1.trees[x].owner = j1;
     	    			j1.trees[x].child = root;
     	    			j1.trees[x].child.parent = j1.trees[x];
     	    			j1.trees[x].child.argposition = 0;
     	    			j1.evaluated = false;            	
     	    		}
            
     	    		// add the individuals to the population
     	    		inds[q] = j1;
     	    		q++;
            	
     	    		ercNode.value = 1-anpha;
     	    		newERCNode.value = anpha;

     	    		GPNode root2 = (GPNode)templateTree.clone();

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
     	    			j2.trees[x].child = root2;
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
     	    	else//mssc
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
     	            int parent1_size = parents[0].trees[0].child.numNodes(GPNode.NODESEARCH_ALL);
     	            int parent2_size = parents[1].trees[0].child.numNodes(GPNode.NODESEARCH_ALL);
     	            
     	            for(int x=0;x<numTries;x++)
     	            {
     	                int position_p1 = -1;
     	                int position_p2 = -1;
     	                
     	                double min_ft = Double.MAX_VALUE;              
     	            	//MSSC
     	            	for (int i = 0; i < 20; i++)
     	            	{
     	    				int position1 = state.random[thread].nextInt(parent1_size);
     	    				int position2 = state.random[thread].nextInt(parent2_size);

     	    				parents[0].trees[0].child.nodeInPosition(position1,
     	    	                    gatherer,
     	    	                    GPNode.NODESEARCH_ALL);
     	    				GPNode temp_p1 = gatherer.node;

     	    				parents[1].trees[0].child.nodeInPosition(position2,
     	    	                    gatherer,
     	    	                    GPNode.NODESEARCH_ALL);
     	    				GPNode temp_p2 = gatherer.node;

//     	            		System.out.println(temp_p1.makeLispTree());
//     	            		System.out.println(temp_p2.makeLispTree());
     	            		//Get SSC between p1 and p2
     	                    double ft = getSimilarity(state, temp_p1, temp_p2, thread);
     	                    //If OK then break
     	                    if (ft > 0 && min_ft > ft)
     	                    {
     	                    	min_ft = ft;
     	                    	position_p1 = position1;
     	                    	position_p2 = position2;
     	                    }
//     	            		System.out.println(p1.makeLispTree());
//     	            		System.out.println(p2.makeLispTree());


//     	                    System.out.println(p1.makeLispTree());
//     	            		System.out.println(p2.makeLispTree());

//     	            		System.out.println(temp_p1.makeLispTree());
//     	            		System.out.println(temp_p2.makeLispTree());

     	            	}
     					parents[0].trees[0].child.nodeInPosition(position_p1,
     		                    gatherer,
     		                    GPNode.NODESEARCH_ALL);
     					p1 = gatherer.node;

     					parents[1].trees[0].child.nodeInPosition(position_p2,
     		                    gatherer,
     		                    GPNode.NODESEARCH_ALL);
     					p2 = gatherer.node;


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
     	    }
        }
    return n;
    }
}

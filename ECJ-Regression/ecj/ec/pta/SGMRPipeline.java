/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.pta;
import java.util.Random;

import ec.*;
import ec.util.*;
import ec.gp.*;
import ec.gp.koza.GPKozaDefaults;

/* 
 * MutationPipeline.java
 * 
 * Created: Tue Oct 12 18:50:56 1999
 * By: Sean Luke
 */

/**
 * MutationPipeline is a GPBreedingPipeline which 
 * implements a strongly-typed version of the 
 * "Point Mutation" operator as described in Koza I.
 * Actually, that's not quite true.  Koza doesn't have any tree depth restrictions
 * on his mutation operator.  This one does -- if the tree gets deeper than
 * the maximum tree depth, then the new subtree is rejected and another one is
 * tried.  Similar to how the Crosssover operator is implemented.
 *
 * <p>Mutated trees are restricted to being <tt>maxdepth</tt> depth at
 * most and at most <tt>maxsize</tt> number of nodes.  If in
 * <tt>tries</tt> attemptes, the pipeline cannot come up with a
 * mutated tree within the depth limit, then it simply copies the
 * original individual wholesale with no mutation.
 *
 * <p>One additional feature: if <tt>equal</tt> is true, then MutationPipeline
 * will attempt to replace the subtree with a tree of approximately equal size.
 * How this is done exactly, and how close it is, is entirely up to the pipeline's
 * tree builder -- for example, Grow/Full/HalfBuilder don't support this at all, while
 * RandomBranch will replace it with a tree of the same size or "slightly smaller"
 * as described in the algorithm.

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 ...as many as the child produces

 <p><b>Number of Sources</b><br>
 1

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

 <tr><td valign=top><i>base</i>.<tt>ns</tt><br>
 <font size=-1>classname, inherits and != GPNodeSelector</font></td>
 <td valign=top>(GPNodeSelector for tree)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>build</tt>.0<br>
 <font size=-1>classname, inherits and != GPNodeBuilder</font></td>
 <td valign=top>(GPNodeBuilder for new subtree)</td></tr>

 <tr><td valign=top><tt>equal</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</td>
 <td valign=top>(do we attempt to replace the subtree with a new one of roughly the same size?)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(tree chosen for mutation; if parameter doesn't exist, tree is picked at random)</td></tr>

 </table>

 <p><b>Default Base</b><br>
 gp.koza.mutate

 <p><b>Parameter bases</b><br>
 <table>

 <tr><td valign=top><i>base</i>.<tt>ns</tt><br>
 <td>nodeselect</td></tr>

 <tr><td valign=top><i>base</i>.<tt>build</tt><br>
 <td>builder</td></tr>

 </table>



 * @author Sean Luke
 * @version 1.0 
 */

public class SGMRPipeline extends GPBreedingPipeline
    {
    private static final long serialVersionUID = 1;

    public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final String P_MAXSIZE = "maxsize";        
    public static final String P_MUTATION = "mutate";
    public static final String P_BUILDER = "build";
    public static final String P_EQUALSIZE = "equal";
    public static final int INDS_PRODUCED = 1;
    public static final int NUM_SOURCES = 1;
    public static final int NO_SIZE_LIMIT = -1;

    /** How the pipeline chooses a subtree to mutate */
    public GPNodeSelector nodeselect;

    /** How the pipeline builds a new subtree */
    public GPNodeBuilder builder;

    /** The number of times the pipeline tries to build a valid mutated
        tree before it gives up and just passes on the original */
    int numTries;
    
    /** The maximum depth of a mutated tree */
    int maxDepth;

    /** The largest tree (measured as a nodecount) the pipeline is allowed to form. */
    public int maxSize;

    /** Do we try to replace the subtree with another of the same size? */
    boolean equalSize;

    /** Is our tree fixed?  If not, this is -1 */
    int tree;
    
    // PTA - mutation step for geometric semantic gp
    double ms;

    public Parameter defaultBase() { return GPKozaDefaults.base().push(P_MUTATION); }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
        {
        SGMRPipeline c = (SGMRPipeline)(super.clone());

        // deep-cloned stuff
        c.nodeselect = (GPNodeSelector)(nodeselect.clone());

        return c;
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        Parameter def = defaultBase();
        Parameter p = base.push(P_NODESELECTOR).push(""+0);
        Parameter d = def.push(P_NODESELECTOR).push(""+0);

        nodeselect = (GPNodeSelector)
            (state.parameters.getInstanceForParameter(
                p,d, GPNodeSelector.class));
        nodeselect.setup(state,p);

        p = base.push(P_BUILDER).push(""+0);
        d = def.push(P_BUILDER).push(""+0);

        builder = (GPNodeBuilder)
            (state.parameters.getInstanceForParameter(
                p,d, GPNodeBuilder.class));
        builder.setup(state,p);

        numTries = state.parameters.getInt(
            base.push(P_NUM_TRIES),def.push(P_NUM_TRIES),1);
        if (numTries ==0)
            state.output.fatal("Mutation Pipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));

        maxDepth = state.parameters.getInt(
            base.push(P_MAXDEPTH),def.push(P_MAXDEPTH),1);
        if (maxDepth==0)
            state.output.fatal("The Mutation Pipeline " + base + "has an invalid maximum depth (it must be >= 1).",base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));

        maxSize = NO_SIZE_LIMIT;
        if (state.parameters.exists(base.push(P_MAXSIZE), def.push(P_MAXSIZE)))
            {
            maxSize = state.parameters.getInt(base.push(P_MAXSIZE), def.push(P_MAXSIZE), 1);
            if (maxSize < 1)
                state.output.fatal("Maximum tree size, if defined, must be >= 1");
            }
        
        equalSize = state.parameters.getBoolean(
            base.push(P_EQUALSIZE),def.push(P_EQUALSIZE),false);

        tree = TREE_UNFIXED;
        if (state.parameters.exists(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0)))
            {
            tree = state.parameters.getInt(base.push(P_TREE).push(""+0),
                def.push(P_TREE).push(""+0),0);
            if (tree==-1)
                state.output.fatal("Tree fixed value, if defined, must be >= 0");
            }
        
        ms = 0.001;
        
        }


    /** Returns true if inner1 can feasibly be swapped into inner2's position */

    public boolean verifyPoints(GPNode inner1, GPNode inner2)
        {
        // We know they're swap-compatible since we generated inner1
        // to be exactly that.  So don't bother.

        // next check to see if inner1 can fit in inner2's spot
        if (inner1.depth()+inner2.atDepth() > maxDepth) return false;

        // check for size
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
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

        // get two trees from treelib
        // random select one tree from treelib
        Random rnd = new Random();
        int treeIndex1 = rnd.nextInt(state.maxTreelibSize);
        int treeIndex2 = rnd.nextInt(state.maxTreelibSize);

        double[] randomSemanticTraining = state.semanticTreelibTraining[treeIndex1];        
        double[] randomSemanticTesting = state.semanticTreelibTesting[treeIndex1];
        
        for(int i = 0; i < randomSemanticTraining.length; i++)
        	randomSemanticTraining[i] -= state.semanticTreelibTraining[treeIndex2][i];
        
        for(int i = 0; i < randomSemanticTesting.length; i++)
        	randomSemanticTesting[i] -= state.semanticTreelibTesting[treeIndex2][i];
        
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did


        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
        {
            GPIndividual i = ((GPIndividual)inds[q]).lightClone();
            
            i.trees[0].semanticTraining = new double[randomSemanticTraining.length];
            i.trees[0].semanticTesting = new double[randomSemanticTesting.length];

            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.trees.length))
                // uh oh
                state.output.fatal("GP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
                

            int t;
            // pick random tree
            if (tree==TREE_UNFIXED)
                if (i.trees.length>1) t = state.random[thread].nextInt(i.trees.length);
                else t = 0;
            else t = tree;

            
            // geometric semantic mutation
            for(int j = 0; j < randomSemanticTraining.length; j++)
            	i.trees[t].semanticTraining[j] += ms * randomSemanticTraining[j];
            
            for(int j = 0; j < randomSemanticTesting.length; j++)
            	i.trees[t].semanticTesting[j] += ms * randomSemanticTesting[j];
            	
            i.evaluated = false;
            
            inds[q] = i;
        }
        return n;
        }
    }

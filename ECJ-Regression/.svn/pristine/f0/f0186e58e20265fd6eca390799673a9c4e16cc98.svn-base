/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.phishing.func;
import ec.*;
import ec.app.phishing.*;
import ec.gp.*;
import ec.util.*;

/* 
 * Add.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Iff extends GPNode
    {
    public String toString() { return "Iff"; }

    public int expectedChildren() { return 3; }

    public void eval(final EvolutionState state,
            final int thread,
            final GPData input,
            final ADFStack stack,
            final GPIndividual individual,
            final Problem problem)
        {
        RegressionData rd = ((RegressionData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        double l = rd.x;

        children[1].eval(state,thread,input,stack,individual,problem);
        double r = rd.x;

        children[2].eval(state,thread,input,stack,individual,problem);
        double t = rd.x;
        if (l > 0)
        	rd.x = r;
        else rd.x = t;
        }
    }





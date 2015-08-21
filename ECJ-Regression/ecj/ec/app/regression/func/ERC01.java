/*
  Copyright 2012 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.regression.func;
import ec.*;
import ec.app.regression.*;
import ec.gp.*;
import ec.util.*;
import java.io.*;



public class ERC01 extends RegERC
    {
    public String name() { return "ERC01"; }

    public void resetNode(final EvolutionState state, final int thread)
        { 
        value = state.random[thread].nextGaussian(); 
        }
    }




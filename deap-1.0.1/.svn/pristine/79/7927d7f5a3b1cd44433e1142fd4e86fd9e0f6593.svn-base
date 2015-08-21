#    This file is part of EAP.
#
#    EAP is free software: you can redistribute it and/or modify
#    it under the terms of the GNU Lesser General Public License as
#    published by the Free Software Foundation, either version 3 of
#    the License, or (at your option) any later version.
#
#    EAP is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#    GNU Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with EAP. If not, see <http://www.gnu.org/licenses/>.
import os
import sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
                
                
import operator
import math
import random
import datetime, time

import numpy
from scipy import linalg

from deap import algorithms
from deap import base
from deap import creator
from deap import tools
from deap import gp
from numpy.f2py.auxfuncs import throw_error

from cmaes import *
from problems import *

# GP settings
POPSIZE = 200
JOBS = 30
NUMGEN = 50
CROSSOVER_RATE = 0.9
MUTATION_RATE = 0.1
TOURSIZE = 3


trainingInputs = []
trainingOutputs = []
testingInputs =[]
testingOutputs=[]
    

# Define new functions
def safeDiv(left, right):
    try:
        return left / right
    except ZeroDivisionError:
        return 1

def AQDiv(left, right):
    return left/(math.sqrt(1 + right*right))

pset = gp.PrimitiveSet("MAIN", NUMVARS)
pset.addPrimitive(operator.add, 2)
pset.addPrimitive(operator.sub, 2)
pset.addPrimitive(operator.mul, 2)
pset.addPrimitive(AQDiv, 2)
# pset.addPrimitive(safeDiv, 2)
# pset.addPrimitive(operator.neg, 1)
pset.addPrimitive(math.cos, 1)
pset.addPrimitive(math.sin, 1)
pset.addEphemeralConstant("rand101", lambda: random.randint(-1,1))
# pset.renameArguments(ARG0='x0')
# pset.renameArguments(ARG1='x1')
# pset.renameArguments(ARG2='x2')
# pset.renameArguments(ARG3='x3')

creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
creator.create("Individual", gp.PrimitiveTree, fitness=creator.FitnessMin)

toolbox = base.Toolbox()
toolbox.register("expr", gp.genHalfAndHalf, pset=pset, min_=2, max_=6)
toolbox.register("individual", tools.initIterate, creator.Individual, toolbox.expr)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)
toolbox.register("compile", gp.compile, pset=pset)

def evalSymbReg(individual):
    # Transform the tree expression in a callable function
    func = toolbox.compile(expr=individual)
    # Evaluate the mean squared error between the expression
    # and the real function : x**4 + x**3 + x**2 + x
#     sqerrors = ((func(x) - x**4 - x**3 - x**2 - x)**2 for x in points)
    error = 0
    for i in xrange(len(trainingInputs)):
        t = func(*trainingInputs[i])
        error += math.fabs(t - trainingOutputs[i])
    return error / len(trainingInputs),

def getSemantic(individual):
    '''
    on training data
    '''
    semantic = []
    # Transform the tree expression in a callable function
    func = toolbox.compile(expr=individual)
    for i in xrange(len(trainingInputs)):
        t = func(*trainingInputs[i])
        semantic.append(t)
        
    return tuple(semantic)
    

#evaluate on testing data
def describe(individual):
    # Transform the tree expression in a callable function
    func = toolbox.compile(expr=individual)
    
    error = 0
    for i in xrange(len(testingInputs)):
        t = func(*testingInputs[i])
        error += math.fabs(t - testingOutputs[i])
        
    return error / len(testingInputs)

    
toolbox.register("evaluate", evalSymbReg)
toolbox.register("select", tools.selTournament, tournsize=TOURSIZE)
toolbox.register("mate", gp.cxOnePoint)
toolbox.decorate("mate", gp.staticLimit(operator.attrgetter('height'),80))
toolbox.register("expr_mut", gp.genFull, min_=0, max_=2)
toolbox.register("mutate", gp.mutUniform, expr=toolbox.expr_mut, pset=pset)
toolbox.decorate("mutate", gp.staticLimit(operator.attrgetter('height'),80))

toolbox.register("getSemantic", getSemantic)
    

def main(problem, job):
    global trainingInputs, trainingOutputs, testingInputs, testingOutputs
    
    random.seed(1000 + job)

    #read training data
    # training and testing file
    dir = '/home/pta/Dropbox/uci/regression/'
    # training and testing file
    train = dir + problem + ".training.in"
    test = dir + problem + ".testing.in"


    lines = open(train).readlines()
    trainingInputs = []
    trainingOutputs = []
     
    for line in lines[1:]:
        xs = line.split()
        trainingInputs.append([float(x) for x in xs[:-1]])
        trainingOutputs.append(float(xs[-1]))
     
    #N = len(trainingOutputs)
         
    # read testing data
    lines = open(test).readlines()
    testingInputs = []
    testingOutputs = []
     
    for line in lines[1:]:
        xs = line.split()
        testingInputs.append([float(x) for x in xs[:-1]])
        testingOutputs.append(float(xs[-1]))

    pop = toolbox.population(n=POPSIZE)
    hof = tools.HallOfFame(1)
    
    stats_fit = tools.Statistics(lambda ind: ind.fitness.values)
    stats_size = tools.Statistics(len)
    mstats = tools.MultiStatistics(fitness=stats_fit, size=stats_size)
    mstats.register("avg", numpy.mean)
    mstats.register("std", numpy.std)
    mstats.register("min", numpy.min)
    mstats.register("max", numpy.max)

    pop, log, best_fitness_each_gen, num_distinct_ind, avg_semantic_distance = algorithms.eaPTA(pop, toolbox, CROSSOVER_RATE, MUTATION_RATE, NUMGEN, stats=mstats,
                                   halloffame=hof, verbose=False)
    
    trainingError = evalSymbReg(hof[0])[0]
    
    testingError = describe(hof[0])
    
    print 'job ', job, ': fitness=', trainingError, '; fittest=',testingError, '; size=',len(hof[0])
    
#     print 'training error: ', trainingError
#     print 'testing error: ', testingError
    # print log
    return job, trainingError, testingError, len(hof[0]), best_fitness_each_gen, num_distinct_ind, avg_semantic_distance


if __name__ == "__main__":
    
   
    for problem in problems:
        print problem
        
        out = open('out/' + problem + ".gp.out", 'wb')
        fitness_runs = []
        fittest_runs=[]
        size_runs = []
        time_runs =[]
        fitness_gens = None
        num_of_distinct_ind = []
        average_semantic_distance = []
        
        for job in xrange(JOBS):
            start_time = datetime.datetime.now()
            j, fitness, fittest, size, best_fitness_each_gen, num_distinct_ind, avg_semantic_distance = main(problem, job)
            
            end_time = datetime.datetime.now()
        
            start_time = time.mktime(start_time.timetuple())*1000
        
            running_time = time.mktime(end_time.timetuple())*1000 - start_time
            
            fitness_runs.append(fitness)
            fittest_runs.append(fittest)
            size_runs.append(size)
            time_runs.append(running_time)
            
            if(fitness_gens == None):
                fitness_gens = best_fitness_each_gen[:]
            else:
                fitness_gens = [u + v for u,v in zip(fitness_gens, best_fitness_each_gen)]
            
            num_of_distinct_ind.append(num_distinct_ind)
            
            average_semantic_distance.append(avg_semantic_distance)
            
            
            
#             out.write(' '.join([str(j), str(fitness), str(fittest), str(size), '\n']))
        out.write(str(numpy.mean(fitness_runs)) + '\n')
        out.write(str(numpy.median(fittest_runs)) + '\n')
        out.write(str(numpy.average(time_runs)) + '\n')
        out.write(str(numpy.average(size_runs)) + '\n')
        out.write(' '.join([str(v/JOBS) for v in fitness_gens]) + '\n')
        out.write(str(numpy.average(num_of_distinct_ind)) + '\n')
        out.write(str(numpy.average(average_semantic_distance)))
            
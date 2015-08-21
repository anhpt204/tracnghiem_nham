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
import matplotlib.pyplot as plt
import networkx as nx
import numpy
from scipy import linalg

from deap import algorithms
from deap import base
from deap import creator
from deap import tools
from deap import gp
from numpy.f2py.auxfuncs import throw_error

from cmaes import *
from numpy.numarray.functions import getTypeObject
from copy import deepcopy
from problems import *



trainingInputs = []
trainingOutputs = []
testingInputs = []
testingOutputs = []

POPSIZE = 1000
# number of clusters
TRAINSAMPLINGRATE = 0.05

# number of new individuals
NEWPOPSIZE = 200
# training and testing file

# GP Settings
JOBS = 30
GP_POPSIZE = 200
NUMGEN = 50
CROSSOVER_RATE = 0.9
MUTATION_RATE = 0.1
TOURSIZE = 3
MIN_DEPTH = 1
MAX_DEPTH = 3
    
    
        
# Define new functions
def safeDiv(left, right):
    try:
        return left / right
    except ZeroDivisionError:
        return 1

def AQDiv(left, right):
    return left/(math.sqrt(1 + right*right))

pset = gp.PrimitiveSet("MAIN", NUMVARS)
pset.addPrimitive(operator.add, 2, name='add')
pset.addPrimitive(operator.sub, 2, name='sub')
pset.addPrimitive(operator.mul, 2, name='mul')
pset.addPrimitive(AQDiv, 2, name='div')
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
toolbox.register("expr", gp.genHalfAndHalf, pset=pset, min_=MIN_DEPTH, max_=MAX_DEPTH)
toolbox.register("individual", tools.initIterate, creator.Individual, toolbox.expr)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)
toolbox.register("compile", gp.compile, pset=pset)


toolbox.register("individual_hpt", tools.initIterate, creator.Individual)

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

#evaluate on training data
def evaluate(best_x, best_individuals, inputs, outputs):
    error = 0.0
    for i in xrange(len(inputs)):
        t = 0.0
        for j in xrange(len(best_individuals)):
            ind = best_individuals[j]
            func = toolbox.compile(expr=ind)            
            t += best_x[j] * func(*inputs[i])
            
        error += math.fabs(t - outputs[i])
        
    return error / len(inputs)

#evaluate on testing data
def describe(individual):
    # Transform the tree expression in a callable function
    func = toolbox.compile(expr=individual)
    
    error = 0
    for i in xrange(len(testingInputs)):
        t = func(*testingInputs[i])
        error += math.fabs(t - testingOutputs[i])
        
    return error / len(testingInputs)
    
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


toolbox.register("evaluate", evalSymbReg)
toolbox.register("select", tools.selTournament, tournsize=TOURSIZE)
toolbox.register("mate", gp.cxOnePoint)
toolbox.decorate("mate", gp.staticLimit(operator.attrgetter('height'),80))
toolbox.register("expr_mut", gp.genFull, min_=0, max_=2)
toolbox.register("mutate", gp.mutUniform, expr=toolbox.expr_mut, pset=pset)
toolbox.decorate("mutate", gp.staticLimit(operator.attrgetter('height'),80))

toolbox.register("getSemantic", getSemantic)


def les(problem):
    global trainingInputs, trainingOutputs, testingInputs, testingOutputs
    
#     1. semantic khac nhau
#     2. khong co 2 hang ve trai = nhau, ve phai khac nhau
#     3. semantic cua 1 tree khong the chi la 1 gia tri duy nhat
    print 'initializing population by solving equations system...'

    random.seed(1000)   

    dir = '/home/pta/Dropbox/uci/regression/'
    # training and testing file
    train = dir + problem + ".training.in"
    test = dir + problem + ".testing.in"
    
    #read training data
    lines = open(train).readlines()

#     sampling_lines = random.sample(lines[1:], NUMTRAINSAMPLES) 
    # store all input and output, we will sample from this for solving equations
    temp_training = []
    
    
    for line in lines[1:]:
        xs = line.split()
        trainingInputs.append([float(x) for x in xs[:-1]])
        
        
        # exact output
        trainingOutputs.append(float(xs[-1]))
    
        temp_training.append([float(x) for x in xs])
        
#         if(len(trainingInputs) == 50):
#             break
        
        
        # add gauss
#         trainingOutputs.append(float(xs[-1]) + random.gauss(0, 0.001))
    
    # read testing data
    lines = open(test).readlines()
    testingInputs = []
    testingOutputs = []
    
    for line in lines[1:]:
        xs = line.split()
        testingInputs.append([float(x) for x in xs[:-1]])
        testingOutputs.append(float(xs[-1]))
        
    #initialize a new population    
    pop = toolbox.population(n=POPSIZE)
    solutions = []

    NUMTRAINSAMPLES = int(len(temp_training) * TRAINSAMPLINGRATE)
    
    for _ in xrange(NEWPOPSIZE):
        # sampling training inputs
        sampling_inputs = random.sample(temp_training, NUMTRAINSAMPLES)
        outputs = [x[-1] for x in sampling_inputs]
        inputs = [x[:-1] for x in sampling_inputs]
        
        semantics = {}
        # get semantics of all individuals
        for ind in pop:
            # Transform the tree expression in a callable function
            func = toolbox.compile(expr=ind)
        
            # get semantic
            semantic = [func(*x) for x in inputs]
        
            # convert to tuple
            semantic = tuple(semantic)
        
            # check if the value is too big
            tooBig = False
            for x in semantic:
                if math.fabs(x) > 10000:
                    tooBig = True
                    break
            if(tooBig):
                continue
        
            # condition 3: values in semantic is not the same value
            ok = False
            for x in semantic[1:]:
                if x != semantic[0]:
                    ok = True
                    break;
            if not ok:
                continue
        
            # condition 1. add to dict to get distinct semantics
            if not semantics.has_key(semantic):
                semantics[semantic] = ind
        
#         print 'tree size: ', len(semantics)    
        
        coNo = False
    
        while True:
            # randomly select a number of individual
            keys = random.sample(semantics.items(), NUMTRAINSAMPLES)
            rows = []
            for _ in xrange(NUMTRAINSAMPLES):
                rows.append([])
        
            individuals = []
            for key, v in keys:
                individuals.append(v)
                for i in xrange(NUMTRAINSAMPLES):
                    rows[i].append(key[i])
                
            
            try:
                # solve Ax = b 
                A = numpy.array(rows)
                b = numpy.array(outputs)
                #using lib from scipy
                x = linalg.solve(A, b)
                
                #using code gauss
                
                # make A[n, n+1]
#                 A = [rows[i] + [trainingOutputs[i]] for i in xrange(len(rows)) ]
#                 x = gauss(A)
#                 x = cmaes(A)
                
                
                for t in x:
                    if math.fabs(t) > 1000:
                        continue
                # co nghiem
                coNo = True
                
                solutions.append([x[:], individuals[:]])
#                 print 'num of solutions: ', len(solutions)
                break
                
                
            except IOError as e:
                print e.strerror
            except:
                print 'vo nghiem'
                
    newpop = []
                
    for solution in solutions:
#         s = getSolution(solution)
        s = getBalanceSolution(solution)
        ind = creator.Individual(gp.PrimitiveTree(s))
        
#         printTree(ind)
        
        newpop.append(ind)
        
    return newpop
#     return gp_run(newpop)
        
#     out = open('out/' + problem + ".les_gp.out", 'wb')
#     
#     print 'GP running...'
#     for job in xrange(JOBS):
#         pop = newpop[:]
#         j, fitness, fittest, size = gp_run(pop, job)
#         
#         out.write(' '.join([str(j), str(fitness), str(fittest), str(size), '\n']))
#         
#     print 'DONE'
            
def printTree(individual):    
    nodes, edges, labels = gp.graph(individual)
    g = nx.Graph()
    g.add_nodes_from(nodes)
    g.add_edges_from(edges)
    pos = nx.graphviz_layout(g, prog="dot")
    
    nx.draw_networkx_nodes(g, pos)
    nx.draw_networkx_edges(g, pos)
    nx.draw_networkx_labels(g, pos, labels)
    plt.show()
   

def getSolution(solution):
    '''
    make new individual (tree) by using geometric method
    @param solutions: list of solution from solving equations
    @return: new population
    '''
    expr = []
    
    addNode = pset.getPrimitive('add')
    mulNode = pset.getPrimitive('mul')
    
    for x, ind in zip(solution[0][:-1], solution[1][:-1]):        
        
        cons = pset.makeConstant(x)
        
        expr = expr + [addNode, mulNode, cons] + ind
        
        
    cons = pset.makeConstant(solution[0][-1])
    expr = expr + [mulNode, cons] + solution[1][-1]
        
    return expr

def getBalanceSolution(solution):
    '''
    make new balance tree by using geometric method
    @param solutions: list of solution from solving equations
    @return: new population
    '''
    
    addNode = pset.getPrimitive('add')
    mulNode = pset.getPrimitive('mul')
    
    old_list = []
    new_list = []

    for x, ind in zip(solution[0], solution[1]):
        cons = pset.makeConstant(x)
        old_list.append([mulNode, cons] + ind)
        
    while len(old_list) > 1:
        start_idx = 0
        if(len(old_list) % 2 == 1):
            new_list.append([addNode] + old_list[0] + old_list[1])
            new_list.append(old_list[2])
            start_idx = 3
        for i in xrange(start_idx, len(old_list), 2):
            new_list.append([addNode] + old_list[i] + old_list[i+1])
            
        old_list[:] = new_list
        new_list=[]
        
    return old_list[0]
                
def gauss(A):
    n = len(A)

    for i in range(0, n):
        # Search for maximum in this column
        maxEl = abs(A[i][i])
        maxRow = i
        for k in range(i+1, n):
            if abs(A[k][i]) > maxEl:
                maxEl = abs(A[k][i])
                maxRow = k

        # Swap maximum row with current row (column by column)
        for k in range(i, n+1):
            tmp = A[maxRow][k]
            A[maxRow][k] = A[i][k]
            A[i][k] = tmp

        # Make all rows below this one 0 in current column
        for k in range(i+1, n):
            c = -A[k][i]/A[i][i]
            for j in range(i, n+1):
                if i == j:
                    A[k][j] = 0
                else:
                    A[k][j] += c * A[i][j]

    # Solve equation Ax=b for an upper triangular matrix A
    x = [0 for i in range(n)]
    for i in range(n-1, -1, -1):
        x[i] = A[i][n]/A[i][i]
        for k in range(i-1, -1, -1):
            A[k][n] -= A[k][i] * x[i]
    return x

    

def gp_run(pop):
    
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
    
    print 'job ', job, ': fitness=', trainingError, '; fittest=',testingError, '; size=',len(hof[0]), '; distinct ind= ', num_distinct_ind
#     print 'training error: ', trainingError
#     print 'testing error: ', testingError
    # print log
    return job, trainingError, testingError, len(hof[0]), best_fitness_each_gen, num_distinct_ind, avg_semantic_distance

def cmaes(A):
    cm = CMAES(A)
    cm.run()

if __name__ == "__main__":
    
    start_time = datetime.datetime.now()
    start_time = time.mktime(start_time.timetuple())*1000

    for problem in problems:
        print problem
        
        out = open('out/' + problem + ".les_gp.out", 'wb')
        fitness_runs = []
        fittest_runs=[]
        size_runs = []
#         time_runs =[]
        fitness_gens = None
        num_of_distinct_ind = []
        average_semantic_distance = []
        
        init_pop = les(problem)
        
        print 'GP running....'
        for job in xrange(JOBS):
            random.seed(1000+job)
                        
            # sampling a new population from initial population
            pop = random.sample(init_pop, GP_POPSIZE)
            gp_pop = [toolbox.clone(ind) for ind in pop]
            #run gp
            j, fitness, fittest, size, best_fitness_each_gen, num_distinct_ind, avg_semantic_distance = gp_run(gp_pop)
            
            
            fitness_runs.append(fitness)
            fittest_runs.append(fittest)
            size_runs.append(size)
#             time_runs.append(running_time)
            
            if(fitness_gens == None):
                fitness_gens = best_fitness_each_gen[:]
            else:
                fitness_gens = [u + v for u,v in zip(fitness_gens, best_fitness_each_gen)]
            
            num_of_distinct_ind.append(num_distinct_ind)
            
            average_semantic_distance.append(avg_semantic_distance)
            
            
        end_time = datetime.datetime.now()
    
        
    
        running_time = time.mktime(end_time.timetuple())*1000 - start_time
            
#             out.write(' '.join([str(j), str(fitness), str(fittest), str(size), '\n']))
        out.write(str(numpy.mean(fitness_runs)) + '\n')
        out.write(str(numpy.median(fittest_runs)) + '\n')
        out.write(str(running_time/JOBS) + '\n')
        out.write(str(numpy.average(size_runs)) + '\n')
        out.write(' '.join([str(v/JOBS) for v in fitness_gens]) + '\n')
        out.write(str(numpy.average(num_of_distinct_ind)) + '\n')
        out.write(str(numpy.average(average_semantic_distance)))
           
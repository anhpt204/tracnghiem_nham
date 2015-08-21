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

# Regularization

#read data for GP
PROBLEM = 'keijzer-6'

ALPHA = 0.001

problems = [
#                 "airfoil_self_noise",   #:5, 
                "ccpp",                 #:4, 
#                 "concrete",             #:8, 
#                 "winequality-red",      #:11,
#                 "winequality-white",    #:11, 
#                 "wpbc",                 #:31,
#                 "casp",                 #:9,
#                 "slump_test_Compressive",#:7,
#                 "slump_test_FLOW",      #:7,
#                 "slump_test_SLUMP",     #:7,

#                 'keijzer-10',
#                 'keijzer-11',
#                 'keijzer-12',
#                 'keijzer-13',
#                 'keijzer-14',
#                 'keijzer-15'
            ]

# number of variable
NUMVARS = 4

# training and testing file
train = PROBLEM + ".training.in"
test = PROBLEM + ".testing.in"

# READ DATA FOR GP

#read training data
# lines = open(train).readlines()
# trainingInputs = []
# trainingOutputs = []
# 
# for line in lines[1:]:
#     xs = line.split()
#     trainingInputs.append([float(x) for x in xs[:-1]])
#     trainingOutputs.append(float(xs[-1]))
# 
# #N = len(trainingOutputs)
#     
# # read testing data
# lines = open(test).readlines()
# testingInputs = []
# testingOutputs = []
# 
# for line in lines[1:]:
#     xs = line.split()
#     testingInputs.append([float(x) for x in xs[:-1]])
#     testingOutputs.append(float(xs[-1]))
    
        
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
toolbox.register("expr", gp.genHalfAndHalf, pset=pset, min_=3, max_=5)
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
        t = func(trainingInputs[i][0])
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
    
toolbox.register("evaluate", evalSymbReg)
toolbox.register("select", tools.selTournament, tournsize=3)
toolbox.register("mate", gp.cxOnePoint)
toolbox.decorate("mate", gp.staticLimit(operator.attrgetter('height'),80))
toolbox.register("expr_mut", gp.genFull, min_=0, max_=2)
toolbox.register("mutate", gp.mutUniform, expr=toolbox.expr_mut, pset=pset)
toolbox.decorate("mutate", gp.staticLimit(operator.attrgetter('height'),80))

def treeFilter(problem, outFolder, KTNo):
#     1. semantic khac nhau
#     2. khong co 2 hang ve trai = nhau, ve phai khac nhau
#     3. semantic cua 1 tree khong the chi la 1 gia tri duy nhat

    # writer
    out = open('out/' + outFolder + "/" + problem + ".out", 'wb')
    
    dir = '/home/pta/Dropbox/uci/regression/'
    # training and testing file
    train = dir + problem + ".training.in"
    test = dir + problem + ".testing.in"
    
    f_errors = open('out/'+outFolder + "/" + problem + ".err", 'wb')
    
    #read training data
    lines = open(train).readlines()
    trainingInputs = []
    trainingOutputs = []
    
    for line in lines[1:]:
        xs = line.split()
        trainingInputs.append([float(x) for x in xs[:-1]])
        
        
        # exact output
#         trainingOutputs.append(float(xs[-1]))
        
        # regularization
        
        output = -ALPHA + random.random() * 2 * ALPHA
        output += float(xs[-1])
        
        trainingOutputs.append(output)
        
#             break
        
        
        # add gauss
#         trainingOutputs.append(float(xs[-1]) + random.gauss(0, 0.001))
    
    N = len(trainingOutputs)
        
    # read testing data
    lines = open(test).readlines()
    testingInputs = []
    testingOutputs = []
    
    for line in lines[1:]:
        xs = line.split()
        testingInputs.append([float(x) for x in xs[:-1]])
        testingOutputs.append(float(xs[-1]))
        
    # start timer
    start_time = datetime.datetime.now()
    #initialize a new population    
    random.seed(1000)    
    pop = toolbox.population(n=5000)
    
    semantics = {}
    # get semantics of all individuals
    for ind in pop:
        # Transform the tree expression in a callable function
        func = toolbox.compile(expr=ind)
        
        # get semantic
        semantic = [func(*x) for x in trainingInputs]
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
        
    print 'tree size: ', len(semantics)    
    # condition 2: rows in left side is difference
    # randomly select N trees (semantics) and check if it satisfy or not
    best_individuals = None
    best_x = None
    best_xi = None
    
    coNo = False
    
    num_trail = 0
    
    # training error and testing error of all solution
#     solution_errors = []
    
    while True:
        num_trail += 1
        if(num_trail == 100):
            break
        
        keys = random.sample(semantics.items(), N)
        rows = []
        for _ in xrange(N):
            rows.append([])
        
        individuals = []
        for key, v in keys:
            individuals.append(v)
            for i in xrange(N):
                rows[i].append(key[i])
                
        # check condition
        temp = {}
        ok = True
        for row in rows:
            row = tuple(row)
            if temp.has_key(row):
                ok = False
                break
        # if ok
        if ok: 
            # write hpt
#             for row in rows:
#                 line = ' '.join([str(v) for v in row]) + "\n"
#                 out.write(line)
                
            try:
                # solve Ax = b 
                A = numpy.array(rows)
                b = numpy.array(trainingOutputs)
                #using lib from scipy
                x = linalg.solve(A, b)
                
                #using code gauss
                
                # make A[n, n+1]
#                 A = [rows[i] + [trainingOutputs[i]] for i in xrange(len(rows)) ]
#                 x = gauss(A)
#                 x = cmaes(A)
                # co nghiem
                coNo = True
                
                # for writing training error and testing error of this solution
                training_error = evaluate(x, individuals, trainingInputs, trainingOutputs)
                testing_error = evaluate(x, individuals, testingInputs, testingOutputs)
                t = ' '.join([str(training_error), str(testing_error), '\n'])
#                 print t
                f_errors.write(t)
                
                if best_x == None:                        
                    best_x = x
                    best_individuals = individuals
                    best_xi = max([math.fabs(t) for t in best_x])
                    
                elif(KTNo):
#                     for v in x:
#                         if math.fabs(v) > 10000:
#                             raise
                    
                    max_xi = max([math.fabs(t) for t in x])
                    if(max_xi < best_xi):
                        best_xi = max_xi
                        best_x = x
                        best_individuals = individuals

                else:
                    break
            except IOError as e:
                print e.strerror
            except:
                print 'vo nghiem'
            
    if coNo:
        print 'No'
        print best_x
        
        finish_time = datetime.datetime.now()
        
        run_time = time.mktime(start_time.timetuple())*1000
        
        run_time = time.mktime(finish_time.timetuple())*1000 - run_time
        
        out.write("runtime: " + str(run_time) + "\n")
        
#                 out.write("Nghiem: \n")
#                 out.write(' '.join([str(v) for v in x]) + "\n")
        
        # evaluate on testing data
        error = 0
        for i in xrange(len(testingInputs)):
            testCase = testingInputs[i]
            t = 0
            for j in xrange(N):
                # Transform the tree expression in a callable function
                func = toolbox.compile(expr=best_individuals[j])
                
                t += best_x[j] * func(*testCase)
                #t += x[j] * rows[i][j]
                
            error += math.fabs(t - testingOutputs[i])
                
        error = error/len(testingInputs)
        print 'testing error: ', error    
                
        out.write("Testing error: " + str(error) + '\n')
               
        # training error
        training_error = 0
        for i in xrange(len(trainingInputs)):
            testCase = trainingInputs[i]
            t = 0
            for j in xrange(N):
                func = toolbox.compile(expr=best_individuals[j])
                t += best_x[j] * func(*testCase)
                
            training_error += math.fabs(t - trainingOutputs[i])
            
        training_error = training_error/len(trainingInputs)
        print 'training error: ', training_error
        
        out.write("training error: " + str(training_error) + "\n")       
                
        # write size
        size = 0
        for indiv in best_individuals:
            size += len(indiv)
        out.write("size: " + str(size))

        
            #break
                
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

    

def main():
    random.seed(318)

    pop = toolbox.population(n=5000)
    hof = tools.HallOfFame(1)
    
    stats_fit = tools.Statistics(lambda ind: ind.fitness.values)
    stats_size = tools.Statistics(len)
    mstats = tools.MultiStatistics(fitness=stats_fit, size=stats_size)
    mstats.register("avg", numpy.mean)
    mstats.register("std", numpy.std)
    mstats.register("min", numpy.min)
    mstats.register("max", numpy.max)

    pop, log = algorithms.eaSimple(pop, toolbox, 0.9, 0.1, 50, stats=mstats,
                                   halloffame=hof, verbose=True)
    
    trainingError = evalSymbReg(hof[0])[0]
    
    testingError = describe(hof[0])
    
    print 'training error: ', trainingError
    print 'testing error: ', testingError
    # print log
    return pop, log, hof

def cmaes(A):
    cm = CMAES(A)
    cm.run()

if __name__ == "__main__":
#     main()

    
    
    
    for problem in problems:
        treeFilter(problem, "AQDivKTNo", True)

#     cmaes([[1, 2, 3],[4, 6, 3]])

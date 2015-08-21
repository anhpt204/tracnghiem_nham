'''
Created on Jul 8, 2015

@author: pta
'''
import abc
import array, random, math, numpy

from deap import base, creator, tools

from document import helper
from _dbus_bindings import Double

class MDSOptimizer(object):
    '''
    Multi-Document Summarization using optimization
    '''
    __metaclass__ = abc.ABCMeta

    def __init__(self, docCollection):
        '''
        Constructor
        '''
        self.multiDocs = docCollection
        self.max_size = 665
        random.seed(1000)
        
    def set_max_size(self, max_size):
        self.max_size = max_size
        
    @abc.abstractmethod
    def solve(self):
        pass
        
class MDSECOptimizer(MDSOptimizer):
    '''
    Multi-Documents Summarization using EC optimization
    '''
    __metaclass__ = abc.ABCMeta
    
    UMIN = -5
    UMAX = 5
    POPSIZE = 50
    GENERATION = 1000
    MUTATION_FACTOR = 0.8
    CROSSOVER_CONSTANT = 0.8
    SELECTION_SIZE = 3
       
    
    creator.create("FitnessMax", base.Fitness, weights=(1.0,))
#     creator.create("Individual", list, fitness=creator.FitnessMax)
    creator.create("Individual", array.array, typecode='d', fitness=creator.FitnessMax, best = None)
    toolbox = base.Toolbox()
    
    def initialize(self, IND_SIZE):
        
        self.toolbox.register("attr_float", random.uniform, self.UMIN, self.UMAX)
        self.toolbox.register("individual", tools.initRepeat, creator.Individual,
                 self.toolbox.attr_float, n=IND_SIZE)
        
        self.toolbox.register("population", tools.initRepeat, list, self.toolbox.individual)
        self.toolbox.register("mutation", self.mutation)
        self.toolbox.register("crossover", self.crossover)
        self.toolbox.register("select", tools.selRandom)## select 3 random ind
        ##toolbox.register("evaluate", benchmarks.griewank)
        self.toolbox.register("evaluate", self.getFitness)
    
        self.pop = self.toolbox.population(n=self.POPSIZE)
        # binary population
        self.b_pop = self.toolbox.map(self._binarize, self.pop)
#         self.hof = tools.HallOfFame(1)
        self.best_so_far = None
        self.best_so_far_b = None #binary
        self.stats = tools.Statistics(lambda ind: ind.fitness.values)
        self.stats.register("avg", numpy.mean)
        self.stats.register("std", numpy.std)
        self.stats.register("min", numpy.min)
        self.stats.register("max", numpy.max)
    
        self.logbook = tools.Logbook()
        self.logbook.header = "gen", "evals", "size", "std", "min", "avg", "max"


    @abc.abstractmethod
    def getFitness(self, ind):
        pass
    
    @abc.abstractmethod
    def mutation(self, v):
        pass
    
    @abc.abstractmethod
    def crossover(self, u, v):
        pass
    
    @abc.abstractmethod
    def getSize(self, bInd):
        '''
        get summarization length of a solution. It should be overwrite when using
        difference data set. For example, with DUC 2004, size is bytes (number of characters),
        but with DUC 2002, it is number of words 
        '''
        pass
    
    def _binarize(self, ind):
        return [1 if random.random() < 1/(1+math.exp(x)) else 0 for x in ind]

    def getSummary(self):
        b_ind = self.best_so_far_b
        sents = [self.multiDocs.rawSentences[i] for i in xrange(self.multiDocs.numOfSentences) if b_ind[i] == 1]
        return sents
    
class MDSDEOptimizer(MDSECOptimizer):
    '''
    Multi-Documents Summarization using Differential Evolution (DE) algorithm
    follow the paper "Multiple multiDocs summarization based on evolutionary 
    optimization algorithm" Alguliev (2012)
    '''
    

    def _f_cover(self, bInd):
        vector_o = self.multiDocs.meanVector
        vector_os = self.multiDocs.getMeanVectorOfSolution(bInd)
        
        cover = helper.cosin(vector_o, vector_os)
        
        t = 0
        for i in xrange(self.multiDocs.numOfSentences):
            if bInd[i] == 1:
                t += helper.cosin(vector_o, self.multiDocs.getSentenceVector(i))
        
        return cover * t
    
    def _f_diver(self, bInd):
        f_diver = 0.0
        for i in xrange(self.multiDocs.numOfSentences-1):
            vector_i = self.multiDocs.getSentenceVector(i)
            for j in xrange(i+1, self.multiDocs.numOfSentences):
                if bInd[i] == 1 and bInd[j] == 1:
                    vector_j = self.multiDocs.getSentenceVector(j)
                    f_diver += helper.cosin(vector_i, vector_j)
        return f_diver
    
    def getFitness(self, bInd):
#         bInd = self._binarize(ind)
        ms = self._f_diver(bInd)
        if ms == 0:
            return 0,
        return self._f_cover(bInd) / ms,
    
    def mutation(self, v):
        # We must clone everything to ensure independence
        p1, p2, p3 = [self.toolbox.clone(ind) for ind in self.toolbox.select(self.pop, 3)]
        f = self.MUTATION_FACTOR
                
        v = [v1 + f * (v2 - v3) for v1,v2,v3 in zip(p1, p2, p3)]
        return self.boundaryConstraint(v)
        
    def boundaryConstraint(self, v):
        for i in xrange(len(v)):
            if (v[i]< self.UMIN):
                v[i]=2*self.UMIN -v[i]
            elif (v[i] > self.UMAX): 
                v[i]=2*self.UMAX - v[i]
        return v

    def getCR(self, u):
        return self.CROSSOVER_CONSTANT

    def crossover(self, u, v):
        cr = self.getCR(u)
        
        size = len(u)
        k = random.randrange(size)
        for i in xrange(size):
            if i == k or random.random() <= cr:
                u[i] = v[i]
        return u
    
    @abc.abstractmethod
    def getSize(self, bInd):
        '''
        get summarization length of a solution = number of terms 
        '''
#         bInd = self._binarize(ind)
         
        solution_size = 0
         
        for i in xrange(len(bInd)):
            if bInd[i] == 1:
                solution_size += len(self.multiDocs.rawListWordSentences[i])
         
        return solution_size
        
    def getBetter(self, ind1, ind2, bInd1, bInd2):
        '''
        get the better between two individuals 
        '''
        ind1_size = self.getSize(bInd1)
        ind2_size = self.getSize(bInd2)
        
        if ind1_size <= self.max_size and ind2_size > self.max_size:
            # solution 1 is feasible and solution 2 is infeasible
            return ind1        
        elif ind1_size > self.max_size and ind2_size <= self.max_size:
            # solution 1 is infeasible and solution 2 is feasible
            return ind2
        elif ind1_size <= self.max_size and ind2_size <= self.max_size:
            # two solutions are feasible
            return ind1 if ind1.fitness.getValues()[0] > ind2.fitness.getValues()[0] else ind2
        else: # two solutions are infeasible
            return ind1 if ind1_size < ind2_size else ind2
        
    def getBetter_b(self, ind1, ind2, bInd1, bInd2):
        '''
        get the better between two individuals 
        '''
        ind1_size = self.getSize(bInd1)
        ind2_size = self.getSize(bInd2)
        
        if ind1_size <= self.max_size and ind2_size > self.max_size:
            # solution 1 is feasible and solution 2 is infeasible
            return bInd1        
        elif ind1_size > self.max_size and ind2_size <= self.max_size:
            # solution 1 is infeasible and solution 2 is feasible
            return bInd2
        elif ind1_size <= self.max_size and ind2_size <= self.max_size:
            # two solutions are feasible
            return bInd1 if ind1.fitness.getValues()[0] > ind2.fitness.getValues()[0] else bInd2
        else: # two solutions are infeasible
            return bInd1 if ind1_size < ind2_size else bInd2
    
    def evolve(self):
        IndSize = self.multiDocs.numOfSentences
        
        self.initialize(IndSize)
        self.best_gen = []
        self.best_gen_b = [] # binary
        self.worst_gen = []
        self.best_so_far = None
        
        # evaluate population
        fitnesses = self.toolbox.map(self.toolbox.evaluate, self.b_pop)
        for ind, fit in zip(self.pop, fitnesses):
            ind.fitness.values = fit
        
#         if self.hof is not None:
#             self.hof.update(self.pop)
            
        # get worst of gen
        worst_i = self.pop[0]
        best_i = self.pop[0]
        best_i_b = self.b_pop[0]
        for i in xrange(1, self.POPSIZE):
            better = self.getBetter_b(best_i, self.pop[i], best_i_b, self.b_pop[i])
            if better == self.b_pop[i]:
                best_i = self.pop[i]
                best_i_b = better
                
            if worst_i.fitness.dominates(self.pop[i].fitness):
                worst_i = ind
                
            
        self.worst_gen.append(self.toolbox.clone(worst_i))
        self.best_gen.append(self.toolbox.clone(best_i))
        self.best_gen_b.append(self.toolbox.clone(best_i_b))
        self.best_so_far = self.toolbox.clone(best_i)
        self.best_so_far_b = self.toolbox.clone(best_i_b)
        
        
        print self.getSize(self.best_so_far)
        # get best of gen
#         self.best_gen.append(self.hof[0])
            
        record = self.stats.compile(self.pop) if self.stats else {}
        self.logbook.record(gen=0, evals=len(self.pop), size=self.getSize(self.best_so_far_b), **record)
        print self.logbook.stream
        
        self.gen = 0
        
        for gen in xrange(1, self.GENERATION):
            self.gen = gen-1
            newpop = []
            for ind in self.pop:
                u = self.toolbox.clone(ind)
                v = self.toolbox.clone(ind)
                v = self.toolbox.mutation(v)
                z = self.toolbox.crossover(u, v)
                del z.fitness.values
                newpop.append(z)
                
            # update population
            b_newpop = self.toolbox.map(self._binarize, newpop)
            fitnesses = self.toolbox.map(self.toolbox.evaluate, b_newpop)
            for (i, ind), fit in zip(enumerate(newpop), fitnesses):
                ind.fitness.values = fit
                
                self.pop[i] = self.getBetter(self.pop[i], newpop[i], self.b_pop[i], b_newpop[i])
            
            self.b_pop = map(self._binarize, self.pop)
            # get best and worst of gen
            best_i = newpop[0]
            worst_i = newpop[0]
            best_i_b = b_newpop[0]
            for i in xrange(1, self.POPSIZE):
                better = self.getBetter_b(best_i, self.pop[i], best_i_b, self.b_pop[i])
                if better == self.b_pop[i]:
                    best_i = newpop[i]
                    best_i_b = better
                
                
                if worst_i.fitness.dominates(self.pop[i].fitness):
                    worst_i = ind
                    
            self.best_gen.append(self.toolbox.clone(best_i))
            self.best_gen_b.append(self.toolbox.clone(best_i_b))
            self.worst_gen.append(self.toolbox.clone(worst_i))
            self.best_so_far_b = self.toolbox.clone(self.getBetter_b(self.best_so_far, best_i, self.best_so_far_b, best_i_b))
            if self.best_so_far_b == best_i_b:
                self.best_so_far = best_i
            # update hof
#             if self.hof is not None:
#                 self.hof.update(newpop)
                
            record = self.stats.compile(newpop) if self.stats else {}
            self.logbook.record(gen=gen, evals=len(newpop), size=self.getSize(self.best_so_far_b), **record)
            print self.logbook.stream
            
        return self.best_so_far, self.getSize(self.best_so_far_b)
    
    def solve(self):
        best_ind, size = self.evolve()
        solution = self.getSummary()
        
#         print 'size: ', self.getSize(self.best_so_far_b)
#         print solution
        return solution, size
        
        
    
class MDSSaDEOptimizer(MDSDEOptimizer):
    '''
    Self-adaptive DE algorithm
    '''
    def mutation(self, v):
        U_p1 = self.toolbox.select(self.pop, 1)[0]
        while U_p1 == v:
            U_p1 = self.toolbox.select(self.pop, 1)[0]
            
        f = math.exp(-2 * self.gen / self.GENERATION)

        v = [p + (1-f) * (gbest - p1) + f * (best - p1) for p,p1,gbest,best in zip(v, U_p1, self.best_so_far, self.best_gen[self.gen])]
        
        return self.boundaryConstraint(v)
    
    def getCR(self, u):
        f_best = self.best_gen[self.gen].fitness.getValues()[0]
        f_u = u.fitness.getValues()[0]
        f_worst = self.worst_gen[self.gen].fitness.getValues()[0]
        if f_best == f_worst:
            return self.CROSSOVER_CONSTANT
        
        RD = (f_best - f_u) / (f_best - f_worst)
        
        ms = 1 + math.tanh(1 * RD)
        if ms == 0:
            return self.CROSSOVER_CONSTANT
        
        return 2 * math.tanh(2 * RD) / ms
        
        


    
    
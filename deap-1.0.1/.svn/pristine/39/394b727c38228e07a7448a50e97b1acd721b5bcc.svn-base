'''
Created on Jan 29, 2015

@author: pta
'''
import numpy
import math

from deap import algorithms
from deap import base
from deap import benchmarks
from deap import cma
from deap import creator
from deap import tools



class CMAES:
    '''
    classdocs
    '''


    def __init__(self, ar):
        '''
        Constructor
        
        '''
        self.A = ar
        
    def fitness(self, individual):
        fit = 0.0
        for row in self.A:
            t = 0
            for i in xrange(len(individual)):
                t += individual[i] * row[i]
            fit += math.fabs(t - row[-1])
            
        return fit / len(individual),
        
        
    def run(self):
        # Problem size
        N=len(self.A)

        creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
        creator.create("Individual", list, fitness=creator.FitnessMin)

        toolbox = base.Toolbox()
#         toolbox.register("evaluate", benchmarks.rastrigin)
        toolbox.register("evaluate", self.fitness)
        
        # The cma module uses the numpy random number generator
        numpy.random.seed(128)
    
        # The CMA-ES algorithm takes a population of one individual as argument
        # The centroid is set to a vector of 5.0 see http://www.lri.fr/~hansen/cmaes_inmatlab.html
        # for more details about the rastrigin and other tests for CMA-ES    
        strategy = cma.Strategy(centroid=[5.0]*N, sigma=5.0, lambda_=10*N)
        toolbox.register("generate", strategy.generate, creator.Individual)
        toolbox.register("update", strategy.update)

        hof = tools.HallOfFame(1)
        stats = tools.Statistics(lambda ind: ind.fitness.values)
        stats.register("avg", numpy.mean)
        stats.register("std", numpy.std)
        stats.register("min", numpy.min)
        stats.register("max", numpy.max)
        #logger = tools.EvolutionLogger(stats.functions.keys())
   
        # The CMA-ES algorithm converge with good probability with those settings
#         algorithms.eaGenerateUpdate(toolbox, ngen=250, stats=stats, halloffame=hof)
    
        logbook = tools.Logbook()
        logbook.header = ['gen', 'nevals'] + (stats.fields if stats else [])
        ngen = 200
        
        for gen in xrange(ngen):
            # Generate a new population
            population = toolbox.generate()
            # Evaluate the individuals
            fitnesses = toolbox.map(toolbox.evaluate, population)
            for ind, fit in zip(population, fitnesses):
                ind.fitness.values = fit
        
            if hof is not None:
                hof.update(population)
        
            # Update the strategy with the evaluated individuals
            toolbox.update(population)
            
            record = stats.compile(population) if stats is not None else {}
            logbook.record(gen=gen, nevals=len(population), **record)
#             if verbose:
            print logbook.stream
            # print "Best individual is %s, %s" % (hof[0], hof[0].fitness.values)
            
        return hof[0]
    
    
if __name__ == "__main__":
    cmaes = CMAES([[1, 2, 3], [2, 4, 6]])
    
    cmaes.run()
        
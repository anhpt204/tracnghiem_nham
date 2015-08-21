#    This file is part of DEAP.
#
#    DEAP is free software: you can redistribute it and/or modify
#    it under the terms of the GNU Lesser General Public License as
#    published by the Free Software Foundation, either version 3 of
#    the License, or (at your option) any later version.
#
#    DEAP is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#    GNU Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with DEAP. If not, see <http://www.gnu.org/licenses/>.

import array
import random
import json

import numpy

from deap import algorithms
from deap import base
from deap import creator
from deap import tools

from problem import CTPProblem

# load problem
data_path = '/home/pta/projects/ctp/data_ctp/kroA-13-12-75-1.ctp'
problem = CTPProblem()
problem.load_data(data_path)

# ignore depot
IND_SIZE = problem.num_of_nodes + problem.num_of_obligatory_nodes -1

creator.create("FitnessMin", base.Fitness, weights=(-1.0,))
creator.create("Individual", array.array, typecode='i', fitness=creator.FitnessMin)

toolbox = base.Toolbox()

# Attribute generator
toolbox.register("indices", random.sample, range(1,IND_SIZE+1), IND_SIZE)

# Structure initializers
toolbox.register("individual", tools.initIterate, creator.Individual, toolbox.indices)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)

def split(tour):
    '''
    basic splitting algorithm 'tour splitting algorithms for vehicle routing problem' - Prins
    '''
    t = len(tour)
    # V[j] = cost of shortest path from node 0 to node j
    V = []
    # predec[j] predecessor of tour[j] 
    predec = [-1]*(t+1)
    # initialize
    V.append(None)
    for _ in xrange(t):
        V.append(10**10)
    
    V[0] = 0
    predec[0] = 0
    
    for i in xrange(1, t):
        j = i
        load = 0
        node_i = tour[i]
        cost = 0
            
        while True:
            node_j = tour[j]
            load += problem.nodes[node_i].load
                        
            if i == j:
                cost = problem.nodes[0].cost_dict[node_i] \
                + problem.nodes[node_i].visited_cost \
                + problem.nodes[node_i].cost_dict[0]
            
            else:
                cost = cost - problem.nodes[tour[j-1]].cost_dict[0] \
                + problem.nodes[tour[j-1]].cost_dict[node_j] \
                + problem.nodes[node_j].visited_cost \
                + problem.nodes[node_j].cost_dict[0]
                
            if cost <= problem.vehicle_capacity and load <= problem.max_nodes_per_route and V[i-1] + cost < V[j]:
                V[j] = V[i-1] + cost
                predec[j] = i-1
                
            j += 1
            
            if j >= t or load > problem.max_nodes_per_route or cost > problem.vehicle_capacity:
                break
              
    return V[t], predec
                
                
        
        
# evaluate solution
def eval(individual):
    
    # get tour
    covering_set = set()
    tour = []
    for node_id in individual:
        covered_customers = problem.get_set_of_customers_covered_by(node_id)
        
        if covered_customers.issubset(covering_set):
            continue
        
        # update tour
        tour.append(node_id)
        
        # update covering set
        covering_set = covering_set.union(covered_customers)
        
        if len(covering_set) == problem.num_of_customers:
            break
        
    # split tour and return total cost
    
    best_cost, backtrack = split(tour)
        
    return best_cost,

toolbox.register("mate", tools.cxPartialyMatched)
toolbox.register("mutate", tools.mutShuffleIndexes, indpb=0.05)
toolbox.register("select", tools.selTournament, tournsize=3)
toolbox.register("evaluate", eval)

def main():
    random.seed(169)

    pop = toolbox.population(n=300)

    hof = tools.HallOfFame(1)
    stats = tools.Statistics(lambda ind: ind.fitness.values)
    stats.register("avg", numpy.mean)
    stats.register("std", numpy.std)
    stats.register("min", numpy.min)
    stats.register("max", numpy.max)
    
    algorithms.eaSimple(pop, toolbox, 0.7, 0.2, 40, stats=stats, 
                        halloffame=hof)
    
    return pop, stats, hof

if __name__ == "__main__":
    main()

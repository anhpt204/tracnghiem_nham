'''
Created on Mar 17, 2015

@author: pta
'''

import random
import copy
from utils import *

from optimizer.costCalculator import routeCost


def two_exchange(i, j, route):
    '''
    @return: new route by making this operator at customers i-th and j-th
    @param i: i-th customer
    @param j: j-th customer
    @param route: a route
    '''
    result = copy.deepcopy(route)
        
    # to ensure we always have p1<p2        
    if j<i:
        i, j = j, i
     
    # now reverse the tour segment between p1 and p2   
    result[i+1:j+1] = reversed(result[i+1:j+1])
    
    return result

def get_subneighborhood_two_exchange(costMatrix, c, routingPlan):
    '''
    @param c: a customer id
    @param routingPlan: the solution, a list of routes, each route is a list of customers
    @return: list of all neighborhoods using two_exchange operator
    '''
    result = []
    for route_i in xrange(len(routingPlan.routes)):
        route = routingPlan.routes[route_i]
        # if c is in a route
        if route.customes.count(c) > 0:
            # get index of c
            i = route.customers.index(c)
            # make two_exchange with another customer in this route
            for j in xrange(1, len(route.customers)): # start from 1 because 0 is deport
                if(i != j):
                    # deep copy the solution                    
                    new_solution = copy.deepcopy(routingPlan)
                    new_solution.routes[route_i].customers = two_exchange(i, j, route.customers)
                    
                    old_route_i_cost = new_solution.routes[route_i].cost
                    new_route_i_cost = routeCost(costMatrix, new_solution.routes[route_i].customers)
                    
                    new_solution.routes[route_i].cost = new_route_i_cost
                    
                    new_solution.cost += new_route_i_cost - old_route_i_cost
                    
                    result.append(copy.deepcopy(new_solution))
                    
    return result
    


def relocation(i, route_i, j, route_j):
    result1 = route_i[:] # make a copy
    result2 = route_j[:]
    
    # relocation: place i after j
    cust_i = result1.pop(i)
    result2.insert(j+1, cust_i)

    return result1, result2

def exchange(i, route_i, j, route_j):
    result1 = route_i[:] # make a copy
    result2 = route_j[:]
    
    # exchange
    result1[i], result2[j] = result2[j], result1[i]
    
    return result1, result2

def crossover(i, route_i, j, route_j):
    result1 = route_i[:] # make a copy
    result2 = route_j[:]
        
    # crossover by exchanging suc(i) and suc(j)
    result1[i+1], result2[j+1] = result2[j+1], result1[i+1]
    
    return result1, result2

def get_neighborhood(costMatrix, o, c, routingPlan):
    '''
    @param o: an operator applied on two routes
    @param c: a customer
    @param routingPlan: a solution
    
    @return: all neighborhoods of this solution (routingPlan) using an operator o with customer c
    '''
    result = []
    
    # get route that contains customer c and index of c in this route
    route_contains_c = None
    index_of_c = None
    index_of_route_containing_c = None
    for route_i in xrange(len(routingPlan.routes)):
        route = routingPlan.routes[route_i]
        if(route.customers.count(c) > 0):
            route_contains_c = route
            index_of_c = route.costomers.index(c)
            index_of_route_containing_c = route_i
            break
    
    # get all neighborhoods by applying operator o
    for route_i in xrange(len(routingPlan.routes)):
        if route_i != index_of_route_containing_c:
            route = routingPlan.routes[route_i]
            for i in xrange(len(route.customers)):
                new_solution = copy.deepcopy(routingPlan)
                new_solution.routes[route_i].customers = o(index_of_c, route_contains_c.costomers, i, route.customers)
                
                old_route_i_cost = new_solution.routes[route_i].cost
                new_route_i_cost = routeCost(costMatrix, new_solution.routes[route_i].customers)
                
                new_solution.routes[route_i].cost = new_route_i_cost
                new_solution.cost += new_route_i_cost - old_route_i_cost
                
                result.append(copy.deepcopy(new_solution))
            

if __name__ == '__main__':
    route1 = [1, 2, 3, 4, 5]
    route2 = [6, 7, 8, 9, 10]
    
    print 'two_exchange: ',  two_exchange(1, 4, route1)
    print 'relocation: ', relocation(1, route1, 3, route2)
    print 'exchange: ', exchange(1, route1, 3, route2)
    print 'crossover: ', crossover(1, route1, 3, route2)
    
    
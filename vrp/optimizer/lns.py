'''
Created on Mar 17, 2015

@author: pta

Large Neighborhood Search
'''

import random
from route import route, routingPlan
from operator import attrgetter

def getInitialSolution(setting):
    random.seed(1000)
    # randomly choose number of routes
    numOfRoutes = setting.numOfVehicles #random.randint(1, setting.numOfVehicles)
    #initialize routes
    paths = []
    for _ in xrange(numOfRoutes):
        paths.append([setting.requests[0]])
        
    for req in setting.requests:
        # if is depot
        if req.requestId == 0:
            continue
        # add this req to a random route
        i = random.randint(0, numOfRoutes-1)
        paths[i].append(req)

    routes = []
    for i in xrange(numOfRoutes):
        r = route.Route(setting=setting, path=paths[i])
        routes.append(r)
        
    return routingPlan.RoutingPlan(setting=setting, routes=routes)

def solution_compare(solution1, solution2):
    if solution1.betterThan(solution2):
        return 1
    else:
        return -1

def getCustomers(solution):
    '''
    get all customers of a solution
    '''
    custs = []
    for r in solution.routes:
        for cust in r.path[1:]:
            custs.append(cust)
    return custs

   
def selectCustomers(setting, solution, n, teta=2):
    '''
    select customers for LNS
    @param solution: current solution
    @param n: maximum number of selected customers
    @param teta: deterministic factor
    '''
    customers = getCustomers(solution)
    num_customers = len(customers)
    
    S = [random.choice(customers)]
    
    # compute relatedness(c, c_i)
    for _ in xrange(1, n):
        c = random.choice(S)
        tmp_customers = []
        for r in solution.routes:
            v = 1
            if r.contain(c):
                v = 0
            for c_i in r.path[1:]:
                if c_i.requestId != c.requestId:
                    tmp_customers.append(c_i)
                    c_i.relatedness = 1 / (v + setting.normalizedCost[c.requestId][c_i.requestId])
        
        # sorting respect to relatedness
        tmp_customers.sort(key=attrgetter('relatedness'))
        
        # select a customer
        r = random.random() * teta * (num_customers-len(S))
        r = int(r)
        
        S.append(tmp_customers[r])
        
    return S
        
            
def DFSExplore(solution, S, best_so_far):
    '''
    @param solution: current solution
    @param S: the set of customers to insert
    @param best_so_far: the best solution so far
    
    @return: best_so_far solution
    '''
    if len(S) == 0:
        if solution.betterThan(best_so_far):
            best_so_far = solution.clone()
    else:
        # choose farthest visit
        set_best_neighbor = []
        set_neighbors = []
        for c in S:
            tmp_solution = solution.clone()
            neighbors = tmp_solution.getSubNeighborhoods_Relocation(c)
            best_neighbor = neighbors[0]
            for neighbor in neighbors[1:]:
                if neighbor.betterThan(best_neighbor):
                    best_neighbor = neighbor
            
            set_best_neighbor.append(best_neighbor)
            set_neighbors.append(neighbors)
                
        # get the worst
        idx_selected = 0
        for i in xrange(1, len(S)):
            if set_best_neighbor[idx_selected].betterThan(set_best_neighbor[i]):
                idx_selected = i
        
        c = S[idx_selected]
        # remove c from S
        S.pop(idx_selected)
        
        # neighbors by insert c to solution
        neighbors = set_neighbors[idx_selected]
        
        # sorting neighbors respect to their objective function
        neighbors.sort(solution_compare)
        
        for neighbor in neighbors:
            S_c = S[:]
            if bound(neighbor, S_c).betterThan(best_so_far):
                DFSExplore(neighbor, S_c, best_so_far)
        
    
def bound(partial_solution, S):
    '''
    get lower bound
    @return: best solution when insert S into partial_solution
    '''
    

def largeNeighborhoodSearch(setting, initSolution, maxSearches=10, maxIterations=10, maxRelocation=3):
    solution = initSolution.clone()
    for l in xrange(maxSearches):
        for n in xrange(1, maxRelocation+1):
            i = 1
            while i < maxIterations:
                i = i + 1
                S = selectCustomers(setting, solution, n)
                min_solution = getBestSolution(solution, S)
                if min_solution.betterThan(solution):
                    solution = min_solution.clone()
                    i = 1
                    
                    
    
if __name__ == '__main__':
    pass
'''
Created on Mar 18, 2015

@author: pta
'''

import math
from sys import float_info

# Function which calculates the euclidean distance between two points
def euclideanDistance(v1, v2):
    # use Zip to iterate over the two vectors
    sum =0.0
    for coord1,coord2 in zip(v1,v2):
        sum += pow((coord1-coord2), 2)
    
    return math.sqrt(sum)

# Function that evaluates the total length of a path
def routeCost(costMatrix, route):
    totalCost = 0.0
    size = len(route)
    for index in range(size):
        # select the consecutive point for calculating the segment length
        if index == size-1 : 
            # This is because in order to complete the 'tour' we need to reach the starting point
            point2 = route[0] 
        else: # select the next point
            point2 = route[index+1]
            
        totalCost +=  costMatrix(route[index], point2)
    
    return totalCost

# minimal delay, Pascal Van Henterick (2002)
def mdl(routingPlan):
    
    # get routes with minimal length
    min_len = len(routingPlan.routes[0])
    min_route = None
    for route in routingPlan.routes[1:]:
        if len(route) < min_len:
            min_len = len(route)
            min_route = route

    # calculate cost
#     cost = 0
#     for r_i in r:
#         for cust in r_i:
        # calculate mdl(i, r, ro)
        
    
def mdl_relocation(i, r, routes, setting):
    '''
    @param i: a customer id
    @param r: a route that contains i
    @param routes: = solution.routes \ r
    @param setting: setting of the system
    @return:- 0 if i can be relocated on another route 
            - max_float if i cannot be relocated without violating the capacity constraints of the vehicle
            - the minimal time window violations induced by relocating i fater a customer j on another route
    '''
    result = float_info.max
    for route in routes:
        if route.load + setting.customers[i].demand <= setting.veh_capacity:
            result = 0
            break
    
    if result == 0:
        
    return 0
    
def mdl_tw_violation(i, r, routes, setting):
    '''
    @return: the minimal time window violations induced by relocating i fater a customer j on another route

    '''
    
    
if __name__ == '__main__':
    pass
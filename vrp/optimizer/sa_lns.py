'''
Created on Mar 25, 2015

@author: pta
'''
import random
from route import route, routingPlan

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

def routeMinimize(setting, timeLimit, startingTemperature, temperatureLimit, maxIterations=5):
    
    initialRoute = getInitialSolution(setting)
    time = 0
    while time < timeLimit:
        solution = initialRoute.clone() 
        t = startingTemperature
        while time < timeLimit and t > temperatureLimit:
            for _ in xrange(maxIterations):
                neighbors = solution.getSubNeighborhoods()
#                 print neighbors[0].betterThan(neighbors[1])
                neighbors.sort(solution_compare)
#                 print neighbors[0].betterThan(neighbors[1])

if __name__ == '__main__':
    
    pass
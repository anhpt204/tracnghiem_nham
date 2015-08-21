'''
Created on Mar 19, 2015

@author: pta
'''
import random

class RoutingPlan:    

    def __init__(self, setting, routes=[], cost=None):
        self.setting = setting
        self.routes = routes
        self.cost = cost
    
    def clone(self):
        newRoutingPlan = RoutingPlan(self.setting)
        newRoutingPlan.cost = self.cost
        newRoutingPlan.routes =[]
        
        for route in self.routes:
            newRoutingPlan.routes.append(route.clone())
        
        return newRoutingPlan
    
    def isFeasible(self):
        '''
        check if this solution is feasible or not
        a solution is feasible if all routes are feasible
        '''
        for route in self.routes:
            if not route.isFeasible():
                return False
        return True
    
    def getSize(self):
        size = 0
        for route in self.routes:
            if len(route.path) > 1:
                size += 1
        return size
    
    def getNumServicedRequests(self):
        temp = 0
        for route in self.routes:
            temp += len(route.path)-1  # -1 for deport
            
        return temp
    
    def updateCost(self):
        self.cost = 0
        for route in self.routes:
            self.cost += route.cost
            
    
    def getMinimalDelay(self):
        
        # get shortest route
        shortest_route = self.routes[0]
        min_path = len(shortest_route.path)
        routeIdxContainRequest = 0
        
        for i in xrange(1, len(self.routes)):
            route = self.routes[i]
            if len(route.path) < min_path:
                min_path = len(route.path)
                shortest_route = route
                routeIdxContainRequest = i
        
    
    def evaluate(self):
        self.updateCost()
        #return self.getSize(), -self.getNumServicedRequests()
        return - self.getSize(), self.cost
    
    def betterThan(self, otherRoutingPlan):
        '''
        @return: True if better (the smaller, the better), else False
        '''
        e1 = self.evaluate()
        e2 = otherRoutingPlan.evaluate()
        
        if e1[0] < e2[0]:
            return True
        elif e1[0] > e2[0]:
            return False
        else:
            if e1[1] < e2[1]:
                return True
            else:
                return False
        
    def getSubNeighborhoods_twoExchange(self):
        # randomly select a request
        request = random.Random().sample(self.setting.requests, 1)
        
        # get the route that contain this selected request
        routeIdxContainRequest = None
        requestIdx = None
        for i in xrange(len(self.routes)):
            route = self.routes[i]
            for idx in xrange(1, len(route.path)):
                # if this route contain the request
                if route.path[idx] == request.requestId:
                    routeIdxContainRequest = i
                    requestIdx = idx
                    break
        if(routeIdxContainRequest == None):
            return []
        
        # make subneighborboods
        subNeighborhoods = []
        selectedRoute = self.routes[routeIdxContainRequest].clone()
        for j in xrange(1, len(selectedRoute.path)):
            if j == requestIdx:
                continue
            # make a new neighbor by cloning old one
            neighbor = self.clone()
            # apply two exchange operator to the route that contain the selected request
            neighbor.routes[routeIdxContainRequest].twoExchange(requestIdx, j)
            subNeighborhoods.append(neighbor)
            
        return subNeighborhoods
    
    def getSubNeighborhoods_twoRouteOperator(self):
        '''
        get all neighbors
        '''
        operators = ['relocation', 'exchange', 'crossover']
        
        # randomly select a request
        request = random.choice(self.setting.requests)
        # randomly select an operator
        operator = random.choice(operators)
        # get the route that contain this selected request
        routeIdxContainRequest = None
        requestIdx = None
        for i in xrange(len(self.routes)):
            route = self.routes[i]
            found = False
            for idx in xrange(1, len(route.path)):
                # if this route contain the request
                if route.path[idx].requestId == request.requestId:
                    routeIdxContainRequest = i
                    requestIdx = idx
                    found = True
                    
                    break
            if found:
                break
            
        if(routeIdxContainRequest == None):
            return []
        
        # make subneighborboods
        subNeighborhoods = []
        selectedRoute = self.routes[routeIdxContainRequest].clone()
        for i in xrange(len(self.routes)):
            if len(self.routes[i].path) <=1 :
                continue
            
            for j in xrange(1, len(self.routes[i].path)):
                if self.routes[i].path[j].requestid == request.requestId:
                    continue
                # make a neighbor by cloning this
                neighbor = self.clone()
                # apply relocation operator
                if operator == 'relocation':
                    neighbor.routes[routeIdxContainRequest].relocation(requestIdx, neighbor.routes[i], j)
                elif operator == 'exchange':
                    neighbor.routes[routeIdxContainRequest].exchange(requestIdx, neighbor.routes[i], j)
                else:
                    neighbor.routes[routeIdxContainRequest].crossover(requestIdx, neighbor.routes[i], j)

                subNeighborhoods.append(neighbor)

        return subNeighborhoods
        
    def getSubNeighborhoods_Relocation(self, request):
        '''
        get all neighbors by using relocation operator
        '''
        routeIdxContainRequest = None
        requestIdx = None
        for i in xrange(len(self.routes)):
            route = self.routes[i]
            found = False
            for idx in xrange(1, len(route.path)):
                # if this route contain the request
                if route.path[idx].requestId == request.requestId:
                    routeIdxContainRequest = i
                    requestIdx = idx
                    found = True
                    
                    break
            if found:
                break
            
        if(routeIdxContainRequest == None):
            return []
        
        # make subneighborboods
        subNeighborhoods = []
        selectedRoute = self.routes[routeIdxContainRequest].clone()
        for i in xrange(len(self.routes)):
            if len(self.routes[i].path) <=1 :
                continue
            
            for j in xrange(1, len(self.routes[i].path)):
                if self.routes[i].path[j].requestid == request.requestId:
                    continue
                # make a neighbor by cloning this
                neighbor = self.clone()
                # apply relocation operator
                neighbor.routes[routeIdxContainRequest].relocation(requestIdx, neighbor.routes[i], j)

                subNeighborhoods.append(neighbor)

        return subNeighborhoods
    
    
    def getSubNeighborhoods(self):
        operator = random.randint(0, 3)
        if operator == 0:
            return self.getSubNeighborhoods_twoExchange()
        else:
            return self.getSubNeighborhoods_twoRouteOperator()
        
    
    def mdl_relocation(self, requestIdx, routeIdxContainRequest):
        '''
        check if a request can be relocated to another route
        '''
                
        for i in xrange(len(self.routes)):
            if i == routeIdxContainRequest or len(self.routes[i].path) <=1 :
                continue
            
            for j in xrange(1, len(self.routes[i].path)):
                # make a neighbor by cloning this
                neighbor = self.clone()
                # apply relocation operator
                neighbor.routes[routeIdxContainRequest].relocation(requestIdx, neighbor.routes[i], j)
                
                if neighbor.routes[i].isFeasible():
                    return True
                

        return False
    
        
if __name__ == '__main__':
    pass
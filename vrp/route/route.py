'''
Created on Mar 19, 2015

@author: pta
'''
from oauthlib.uri_validate import reserved
from duplicity.path import Path

class Route:
    '''
    a route = (o, c_1, c_2, ..., c_n)
    '''
    
    def __init__(self, setting, path=[], departureTime=None, earlistServiceTime=None, 
                 earlistArrivalTime=None, latestArrivalTime=None, cost=None, demand=None):
        self.setting = setting
        # path, part[0] = deport
        if(len(path) == 0):
            self.path = [0]
        else:
            self.path = path 
        # departure time for each request on this route
        self.earliestDepartureTime = [0]
        # earlist service time for each request on this route
        self.earlistStartOfServiceTime = [0]
        # earlist arrival time of this route
        self.earlistReturnTime = 0
        # latest arrival time for each request on this route
        self.latestArrivalTime = [setting.requests[0].tw[1]]
        self.latestDepartureTime = []
        
        self.cost = cost
        self.demand = demand
        
        if len(path) > 1:
            self.update(1)
        
    def clone(self):
        newRoute = Route(self.setting)
        newRoute.path = self.path[:]
        newRoute.earliestDepartureTime = self.departureTime[:]
        newRoute.earlistStartOfServiceTime = self.earlistStartOfServiceTime[:]
        newRoute.earlistReturnTime = self.earlistReturnTimeTime
        newRoute.latestArrivalTime = self.latestArrivalTime[:]
        newRoute.latestDepartureTime = self.latestDepartureTime[:]
        newRoute.demand = self.demand
        newRoute.cost = self.cost
        
        return newRoute
        
    def contain(self, request):
        '''
        @return: True if this route contain the request, otherwise return False
        '''
        for r in self.path[1:]:
            if r.requestId == request.requestId:
                return True
        return False
        
    def twoExchange(self, i, j):
        # make sure i < j
        if i > j:
            i, j = j, i
        
        temp = self.path[i+1:j+1]
        temp.reverse()
        self.path[i+1:j+1] = temp[:]
            
        self.update(i)
    
    def relocation(self, i, anotherRoute, j):
        request_i = self.path.pop(i)
        anotherRoute.path.insert(j+1, request_i)
        
        self.update(i)
        anotherRoute.update(j+1)
        
    def exchange(self, i, anotherRoute, j):
        self.path[i], anotherRoute.path[j] = anotherRoute.path[j], self.path[i]
        
        self.update(i)
        anotherRoute.update(j)
        
    def crossover(self, i, anotherRoute, j):
        self.path[i+1:], anotherRoute.path[j+1:] = anotherRoute.path[j+1:], self.path[i+1:]
        
        self.update(i+1)
        anotherRoute.update(j+1)
        
        
    def update(self, start_idx):
        '''
        update: - departure time
                - earliest service time
                - earliest arrival time
                #- latest arrival time
                - cost and demand
        @param start_idx: index that have been changed
        '''
        pathLength = len(self.path)
         
        # if route is empty
        if(pathLength <= 1):
            return

        if start_idx == 1:
            self.earliestDepartureTime = [0]*pathLength
            self.earlistStartOfServiceTime = [0]*pathLength
            self.earlistReturnTime = 0
            self.latestArrivalTime = [0]*pathLength
            self.latestDepartureTime = [0]*pathLength
            
            self.earliestDepartureTime[0] = self.setting.requests[0].tw[0]
            self.latestArrivalTime[0] = self.setting.requests[0].tw[1]
        
        else:
            self.earliestDepartureTime[start_idx:] = [0]* (pathLength-start_idx)
            self.earlistStartOfServiceTime[start_idx:] = [0]*(pathLength-start_idx)
            self.latestArrivalTime[start_idx:] = [0]*(pathLength-start_idx)
            self.latestDepartureTime[start_idx:] = [0]*(pathLength-start_idx)
            
        
#         print len(self.departureTime), len(self.earlistServiceTime)
        
        for i in xrange(start_idx, pathLength):
            req_i = self.path[i]
            pre_req_i = self.path[i-1]
            
            if i == pathLength-1:
                suc_req_i = self.path[0]
            else:
                suc_req_i = self.path[i+1]
            
            # deparure time
            self.earliestDepartureTime[i] = max(self.earliestDepartureTime[i-1] + self.setting.costMatrix[pre_req_i.requestId][req_i.requestId], 
                                        req_i.tw[0]) + req_i.serviceTime
         
            # earliest service time
            self.earlistStartOfServiceTime[i] = max(self.earliestDepartureTime[i-1] + self.setting.costMatrix[pre_req_i.requestId][req_i.requestId],
                                             req_i.tw[0])
            
        self.latestArrivalTime = [0]*pathLength
        self.latestDepartureTime = [0]*pathLength
        
        self.latestArrivalTime[0] = self.setting.requests[0].tw[1]
        
        for i in xrange(pathLength-1, 0, -1):
            r_i = self.path[i]
            i_plus = i + 1
            if i_plus == pathLength:
                i_plus = 0

            # latest arrival time
            self.latestArrivalTime[i] = min(self.latestArrivalTime[i_plus] 
                                            - self.setting.costMatrix[r_i.requestId][self.path[i_plus].requestId] - r_i.serviceTime,
                                            r_i.tw[1])
            
            self.latestDepartureTime[i] = self.latestArrivalTime[i_plus] - self.setting.costMatrix[r_i.requestId][self.path[i_plus].requestId]
        
        if len(self.path > 1):
            self.earlistReturnTime = self.earliestDepartureTime[-1] + self.setting.costMatrix[self.path[-1].requestId][0]
        else:
            self.earlistReturnTime = self.setting.requests[0].tw[0]
        
        self.calCostAndDemand()
        
    def calCostAndDemand(self):
        self.cost = 0
        self.demand = 0
        pathLength = len(self.path)
        
        # if route is empty
        if(pathLength <= 1):
            self.cost = 0
            return
        
        for i in xrange(1, pathLength):
            req_i = self.path[i]
            pre_req_i = self.path[i-1]
            
            self.demand += req_i.demand
            self.cost += self.setting.costMatrix[pre_req_i.requestId][req_i.requestId]
            
        self.cost += self.setting.costMatrix[self.path[-1].requestId][0]
    
    def isFeasible(self):
        '''
        check if this route is feasible or not
        
        @return: True if this route is feasible, False otherwise
        '''
        
        if self.demand > self.setting.vehicleCapacity:
            return False
        
        if self.earlistStartOfServiceTime > self.setting.requests[0].tw[1]:
            return False
        
        # check time window of each request
        for i in xrange(1, len(self.earlistStartOfServiceTime)):
            if self.earlistStartOfServiceTime[i] > self.path[i].tw[1]:
                return False
            
        return True
    
if __name__ == '__main__':
    route1 = Route(setting=None, path=[0, 1, 2, 3, 4, 5])
    route2 = Route(setting=None, path=[0, 6, 7, 8, 9, 10])
    route3 = route1.clone()
    
    
#     route1.twoExchange(0, 6)
#     route1.relocation(1, route2, 3)
#     route1.exchange(1, route2, 3)
#     route1.crossover(1, route2, 3)
    
    print route1.path
    print route3.path
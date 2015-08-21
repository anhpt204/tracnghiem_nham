'''
Created on Mar 19, 2015

@author: pta
'''
import math

class Setting:
    def __init__(self, requests, numOfVehicles, vehicleCapacity):
        # deport = requests[0]
        self.requests = requests
        # number of vehicles
        self.numOfVehicles = numOfVehicles
#         self.deport = deport
        self.vehicleCapacity = vehicleCapacity
                
        self.costMatrix = []
        self.normalizedCost = []
        
        self.calCostMatrix()
            
    
    def calCostMatrix(self):
        self.costMatrix = []
        n = len(self.requests)
        
        max_dist = 0
        
        for i in xrange(n):
            self.costMatrix.append([0]*n)
            self.normalizedCost.append([0]*n)
        
        #calculate euclidean distance
        for i in xrange(n):
            for j in xrange(i+1, n):
                dist = math.pow(self.requests[i].x - self.requests[j].x, 2)
                dist += math.pow(self.requests[i].y - self.requests[j].y, 2)
                dist = math.sqrt(dist)
                
                id_i = self.requests[i].requestId
                id_j = self.requests[j].requestId
                self.costMatrix[id_i][id_j], self.costMatrix[id_j][id_i] = dist, dist
                
                if dist > max_dist:
                    max_dist = dist
        
        # calculate relatedness
        for i in xrange(n):
            self.normalizedCost[i] = [self.costMatrix[i][j]/max_dist for j in xrange(n)]
        
if __name__ == '__main__':
    pass
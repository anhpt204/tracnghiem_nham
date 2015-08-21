'''
Created on Mar 25, 2015

@author: pta
'''
PROBLEM = '/home/pta/projects/vrp/problems/c1_2_1.txt'

from data import request, settings
from optimizer import sa_lns

def getSettings():
    lines = open(PROBLEM, 'r').readlines()
    
    xs = lines[0].split()
    numOfVehicles = int(xs[0])
    vehCapacity = int(xs[1])
    
    requests = []
    for line in lines[1:]:
        xs = [ int(x) for x in line.split() ]
        
        req = request.Request(xs)
        
        requests.append(req)
    
    setting = settings.Setting(requests=requests, numOfVehicles=numOfVehicles, vehicleCapacity=vehCapacity)
    return setting 

if __name__ == '__main__':
    
    setting = getSettings()
    #sa_lns.getInitialSolution(setting)
    sa_lns.routeMinimize(setting, 10, 10, 5, 5)
    
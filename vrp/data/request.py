'''
Created on Mar 19, 2015

@author: pta
'''

class Request:
    
    def __init__(self, xs): #requestId, x, y, demand, tw, serviceTime):
        self.requestId = xs[0]
        self.x = xs[1]
        self.y = xs[2]
        self.demand = xs[3]
        self.tw = xs[4:6]
        self.serviceTime = xs[6]
        # template field for sorting
        self.relatedness = 0

if __name__ == '__main__':
    pass
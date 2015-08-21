'''
Created on Jul 7, 2015

@author: pta
'''
import math

def cosin(vector_i, vector_j):
    '''
    calculate cosin between two sentence vectors
    '''
    t = 0.0
    for term in vector_i.keys():
        if vector_j.has_key(term):
            t += vector_i[term] * vector_j[term]
                 
    t1 = sum([w*w for w in vector_i.values()])   
    t2 = sum([w*w for w in vector_j.values()])
    
    if t1 * t2 == 0:
#         print vector_i
#         print vector_j
        return 1
        
    return t / math.sqrt(t1 * t2)


if __name__ == '__main__':
    pass
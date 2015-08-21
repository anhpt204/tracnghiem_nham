#!/usr/bin/python
# -*- coding: utf-8 -*-

import datetime

from collections import namedtuple
from operator import attrgetter
from audioop import reverse
from datetime import timedelta
Item = namedtuple("Item", ['index', 'value', 'weight', 'ratio'])


def solve_it(input_data):
    # Modify this code to run your optimization algorithm

    # parse the input
    lines = input_data.split('\n')

    firstLine = lines[0].split()
    item_count = int(firstLine[0])
    capacity = int(firstLine[1])

    items = []

    for i in range(1, item_count+1):
        line = lines[i]
        parts = line.split()
        items.append(Item(i-1, int(parts[0]), int(parts[1]), float(parts[0])/float(parts[1])))

    # Branch and Bound algorithms
    # check if exist a feasible solution

    taken = [0]*len(items)
    value = 0
    weight = 0

    best_value = 0
    best_solution = []

    exist_feasible_solution = False
    for item in items:
        if item.weight <= capacity:
            exist_feasible_solution = True
            break
        
    exact_solution = True
    
    start_time = datetime.datetime.now()
    
    
    if exist_feasible_solution:
        for item in items:
            weight += item.weight
            if weight > capacity:
                exact_solution = False
                
        if exact_solution:
            taken = [1]*len(items)
            best_value = weight
        else:
            # branch and bound
            
            # get bound
#             bound1 = 0
            bound = 0
            weight = 0
            # sort items by ratio

            for item in items:
                bound += item.value

#             items_sorted = sorted(items, key=attrgetter('ratio'), reverse = True)
#   
#             bounded_items = {}
#               
#             for item in items_sorted:
#                 if(weight + item.weight <= capacity):
#                     weight += item.weight
#                     bound += item.value
#                     bounded_items[item.index] = item.value
#                 else:
#                     temp_weight = capacity - weight
#                     temp = (temp_weight * item.value) / item.weight
#                     bound += temp
#                     bounded_items[item.index] = temp
#                     break
            
            Node = namedtuple("Node", ['x', 'value', 'room', 'estimate'])

            
            print 'bound: ', bound
            
            root = Node([], 0, capacity, bound)
            nodes = [root]
            
            items = sorted(items, key=attrgetter('ratio'), reverse = True)

            while len(nodes) != 0:
                node = nodes.pop()
                
                if(node.room < 0 or node.estimate < best_value):
                    continue
                
                i = len(node.x)                    
                
                if(i == len(items)):
                    if node.value > best_value:
                        best_value = node.value
                        best_solution = node.x
                        print "best solution: ", best_value
                else:
                
                    left = Node(node.x + [1], node.value + items[i].value, node.room-items[i].weight, node.estimate)
                    
#                     new_estimate = node.estimate
#                     if(bounded_items.has_key(items[i].index)):
#                         new_estimate -= bounded_items[items[i].index]

                    new_estimate = node.estimate - items[i].value


                    right = Node(node.x + [0], node.value, node.room, new_estimate)
                    
                    nodes.append(right)
                    nodes.append(left)
                    
                    current_time = datetime.datetime.now()
                    runtime = current_time - start_time
                    
                    if(runtime.total_seconds() >= 4*3600):
                        break
                    # for best first search
#                     nodes = sorted(nodes, key=attrgetter('estimate'))
                    
            taken = [0]* len(items)   
            for i in xrange(len(best_solution)):
                if best_solution[i] == 1:
                    taken[items[i].index] = 1     
                
                    
                 

    
    # prepare the solution in the specified output format
    output_data = str(best_value) + ' ' + str(0) + '\n'
    output_data += ' '.join(map(str, taken))
    return output_data

    
if __name__ == '__main__':
    file_location = './data/ks_50_0'
    input_data_file = open(file_location, 'r')
    input_data = ''.join(input_data_file.readlines())
    input_data_file.close()

    print solve_it(input_data)



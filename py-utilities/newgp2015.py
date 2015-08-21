'''
Created on Oct 29, 2014

@author: tuananh
'''
import os, csv, sys

# import matplotlib.pyplot as plt
import numpy as np

problems = [
#             "r1",
#             "r2",
#             "r3",
#             "keijzer-1",
#             "keijzer-4",
#             "keijzer-6",
#             "keijzer-7",
#             "keijzer-8",
#             "keijzer-9",
#             "keijzer-10",
#             "keijzer-11",
#             "keijzer-12",
#             "keijzer-13",
#             "keijzer-14",
#             "keijzer-15",
            
            #"casp",
            #"slump_test_FLOW",
            #"slump_test_Compressive",
#             "slump_test_SLUMP",
               
            "airfoil_self_noise",
            "ccpp", 
            "concrete", 
#             "winequality-red",
#             "winequality-white", 
            #"wpbc"
            ]

class_problems = [
                  "EEGEyeState",
                  "breast-cancer-wisconsin", #AGX
                  "data_banknote_authentication",
                  "haberman",
                  "magic04",
                  "wdbc"
                  ]

algs = [
        "safeDivKoKTNo", 
        "safeDivKTNo",
        "AQDivKoKTNo",
        "AQDivKTNo",
#        "safeDivRegu",
#        "safeDivRegu"
        ]

root_dir = "/home/pta/projects/deap-1.0.1/pta/out"

output_dir = "/home/pta/projects/deap-1.0.1/pta/out/"

def getAll():
    '''
    Get data from files *.all
    '''
    
    out_fitness = os.path.join(output_dir, "fitness.csv")
    out_fittest = os.path.join(output_dir, "fittest.csv")
    out_time = os.path.join(output_dir, "time.csv")
    out_size = os.path.join(output_dir, "size.csv")
    
    
    fitness_writer = csv.writer(open(out_fitness, 'w'), quoting=csv.QUOTE_ALL)
    fittest_writer = csv.writer(open(out_fittest, 'w'), quoting=csv.QUOTE_ALL)
    time_writer = csv.writer(open(out_time, 'w'), quoting=csv.QUOTE_ALL)
    size_writer = csv.writer(open(out_size, 'w'), quoting=csv.QUOTE_ALL)
    
    row_header1 = [" "]
    row_header2 = [" "]
    #row_header3 = [" "]
    for alg in algs:
        row_header1.append(alg)
            
    fitness_writer.writerow(row_header1)
    fittest_writer.writerow(row_header1)
    time_writer.writerow(row_header1)
    size_writer.writerow(row_header1)
     
    
    for p in problems:   
        fitness_row = []
        fittest_row = []
        time_row = []
        size_row = []
        
        file_name = p + ".out"
        
        fitness_row.append(p)
        fittest_row.append(p)
        time_row.append(p)
        size_row.append(p)
        
        for alg in algs:
            file_path = os.path.join(root_dir, alg, file_name)
            print file_path
            
            lines = open(file_path).readlines()
            if(len(lines) == 0):
                break
            
            time_row.append(lines[0].split(':')[1])
            fittest_row.append(lines[1].split(':')[1])
            fitness_row.append(lines[2].split(':')[1])
            size_row.append(lines[3].split(':')[1])

        time_writer.writerow(time_row)
        fittest_writer.writerow(fittest_row)
        fitness_writer.writerow(fitness_row)
        size_writer.writerow(size_row)    
                

        
        
# def vehinh(problem, index):
#     file_name = problem + ".gen"
#     ys = []
#     
#     algs = ["SC", 
#             #'SGXM', 
#             "SGXMSC", "AGX", "RDO"]
#     
#     for alg in algs:
#         y = [alg]
#         config = configs[alg][0]
#         file_path = os.path.join(root_dir, alg, "out", config, file_name)
#         lines = open(file_path).readlines()
#         for line in lines:
#             if(len(line) > 5):
#                 vs = line.split()
#                 y.append(float(vs[index]))
#         print len(y)
#         ys.append(y)
#     
#     x = [i for i in xrange(100)]
#     
#     print len(x)
#     for y in ys:
#         plt.plot(x, y[1:], label=y[0])
#             
#     
#     plt.legend(loc='upper left')
#     #dashes = [10, 5, 100, 5] # 10 points on, 5 off, 100 on, 5 off
#     #line.set_dashes(dashes)
#     plt.grid(True)
#     
#     plt.xlabel('Generations')
#     plt.ylabel('Size')
#     plt.title(problem)
# 
#     plt.show()
    
if __name__ == '__main__':
    
    getAll()
    
    #vehinh("slump_test_SLUMP", 0)
#     vehinh("keijzer-4", 2)
    
    print "DONE"

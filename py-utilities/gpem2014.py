'''
Created on Oct 29, 2014

@author: tuananh
'''
import os, csv, sys

import matplotlib.pyplot as plt
import numpy as np

problems = [
            #"keijzer-1",
            "keijzer-4",
            "keijzer-6",
            #"keijzer-7",
            #"keijzer-8",
            #"keijzer-9",
            #"keijzer-10",
            "keijzer-11",
            "keijzer-12",
            #"keijzer-13",
            "keijzer-14",
            "keijzer-15",
            
            #"casp",
            #"slump_test_FLOW",
            #"slump_test_Compressive",
            "slump_test_SLUMP",
               
            #"airfoil_self_noise",
            "ccpp", 
            #"concrete", 
            "winequality-red",
            "winequality-white", 
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
        "SC", 
        "SSC", "MSSC",
        "SGXMSSC", "SGXMMSSC",
        "SGXM", 
        "SGXMSC", 
        "AGX", "RDO"
        ]

configs = {
            "SC":["pop500", "pop1000"],
            "SSC":["pop500"],
            "MSSC":["pop500"],           
            "SGXM": ["pop500", "pop2000"],
            "SGXMSC":["pop500", "pop1000"],
            "SGXMSSC":["pop500"],
            "SGXMMSSC":["pop500"],
            "AGX":["lib500", "lib1000"],
            "RDO":["lib500", "lib1000"]          
#            "SGXMSC":["tune-0.3-10", "tune-0.3-15", "tune-0.3-20", "tune-0.3-25", "tune-0.3-30",
#                      "tune-0.1-20", "tune-0.2-20", "tune-0.4-20", "tune-0.5-20"], 
           }

root_dir = "/home/tuananh/Documents/projects/GParray/src/"

output_dir = "/home/tuananh/Documents/projects/GParray/out/"

def getAll(file_name, row_number):
    '''
    Get data from files *.all
    '''
    
    output_file = os.path.join(output_dir, file_name)
    print output_file
    
    out = csv.writer(open(output_file, 'w'), quoting=csv.QUOTE_ALL)
    
    row_header1 = [" "]
    row_header2 = [" "]
    #row_header3 = [" "]
    for alg in algs:
        row_header1.append(alg)
        #each alg has two config
        row_header1.append(" ")
        for config in configs[alg]:
            row_header2.append(config)
     #       row_header3 += ["ACC", "TP", "TN", "FP", "FN"]
            
    out.writerow(row_header1)
    out.writerow(row_header2)
    #out.writerow(row_header3)
     
    row = []
    for p in problems:        
        file_name = p + ".all"
        
        print file_name
        
        row.append(p)        
        for alg in algs:
            for config in configs[alg]:
                file_path = os.path.join(root_dir, alg, "out", config, file_name)
                lines = open(file_path).readlines()
                row.append(lines[row_number])
        out.writerow(row)
        row = []
    
                
def getAllClassification(file_name, row_number):
    '''
    Get classification result from files *.all
    '''
    
    output_file = os.path.join(output_dir, file_name)
    out = csv.writer(open(output_file, 'w'), quoting=csv.QUOTE_ALL)
    
    row_header1 = [" "]
    row_header2 = [" "]
    row_header3 = [" "]
    for alg in algs:
        row_header1 += [alg] +[" ", " ", " ", " "]*len(configs[alg]) + [" "]
        for config in configs[alg]:
            row_header2 += [config, " ", " ", " ", " "]
            row_header3 += ["ACC", "TP", "TN", "FP", "FN"]
            
    out.writerow(row_header1)
    out.writerow(row_header2)
    out.writerow(row_header3)
     
    row = []
    for p in class_problems:        
        file_name = p + ".all"
        
        #print file_name, 
        
        row.append(p)        
        for alg in algs:
            #print alg, 
            
            for config in configs[alg]:
                file_path = os.path.join(root_dir, alg, "out", config, file_name)
                lines = open(file_path).readlines()
                vs = lines[row_number].split()
                
                ts = float(vs[1]) + float(vs[2])
                ms = float(vs[1]) + float(vs[2]) + float(vs[3]) + float(vs[4])
                vs[0] = ts/ms
                
                row += vs
                
        out.writerow(row)
        row = []
        
def getTime():
    '''
    Get data from files *.all
    '''
    algs = {
            "AGX":["lib500", "lib1000"],
            "RDO":["lib500", "lib1000"],
            "SGXMSC":["pop500", "pop1000"]
            }
    
    file_name = "time.csv"
    output_file = os.path.join(output_dir, file_name)
    out = csv.writer(open(output_file, 'w'), quoting=csv.QUOTE_ALL)
    
    row_header1 = [" "]
    row_header2 = [" "]
    #row_header3 = [" "]
    for alg in algs:
        row_header1 += [alg] + [" "]*(len(configs[alg]) * 3 - 1)
        for config in configs[alg]:
            row_header2 += [config, " ", " "]
            
    out.writerow(row_header1)
    out.writerow(row_header2)
    #out.writerow(row_header3)
     
    row = []
    for p in problems:        
        file_name = p + ".time"
        
        print file_name
        
        row.append(p)        
        for alg in algs:
            for config in configs[alg]:
                file_path = os.path.join(root_dir, alg, "out", config, file_name)
                lines = open(file_path).readlines()
                vs = lines[0].split()
                row += vs
        out.writerow(row)
        row = []

def get5(problem):
    '''
    Get data from files *.5
    '''
    file_name = problem + ".5"
    
    output_file = os.path.join(output_dir, file_name + ".csv")
    out = csv.writer(open(output_file, 'w'), quoting=csv.QUOTE_ALL)
    
    row_header1 = []
    row_header2 = []
    #row_header3 = [" "]
    for alg in algs:
        row_header1.append(alg)
        #each alg has two config
        row_header1 += [" "]* (len(configs[alg]) * 2 -1)
        for config in configs[alg]:
            row_header2.append(config)
            row_header2.append(" ")
     #       row_header3 += ["ACC", "TP", "TN", "FP", "FN"]
            
    out.writerow(row_header1)
    out.writerow(row_header2)
    out.writerow([])
    out.writerow([])
    #out.writerow(row_header3)
     
    rows = []
    for i in xrange(100):
        rows.append([])
    
         
    for alg in algs:
        for config in configs[alg]:
            file_path = os.path.join(root_dir, alg, "out", config, file_name)
            lines = open(file_path).readlines()
        
            for i in xrange(len(lines)):        
                rows[i] += lines[i].split()
    for row in rows:
        out.writerow(row)
            
def makeConfigFile():
    config_problems = {
            "keijzer-1":[1, 100, 100],
            "keijzer-4": [1, 100, 100],
            "keijzer-6": [1, 100, 100],
            #"keijzer-7": [1, 100, 100],
            "keijzer-8": [1, 100, 100],
            "keijzer-9": [1, 100, 100],
            "keijzer-10": [2, 100, 100],
            "keijzer-11": [2, 100, 100],
            "keijzer-12": [2, 100, 100],
            "keijzer-13": [2, 100, 100],
            "keijzer-14": [2, 100, 100],
            "keijzer-15": [2, 100, 100],
            
            "casp": [9, 100, 100],
            "slump_test_FLOW": [7, 50, 53],
            "slump_test_Compressive": [7, 50, 53],
            "slump_test_SLUMP": [7, 50, 53],
               
            "airfoil_self_noise": [5, 100, 100],
            "ccpp": [4, 200, 200], 
            "concrete": [8, 200, 200], 
            "winequality-red": [11, 250, 250],
            "winequality-white": [11, 300, 300], 
            "wpbc": [31, 100, 98]
            
            }
    
    for k,v in config_problems.items():
        file_name = k + ".param"
        f = open(file_name, 'wb')
        
        lines = []
        lines.append("NUMFITCASE="+ str(v[1]))
        lines.append("NUMFITTEST=" + str(v[2]))
        lines.append("NUMVAR=" + str(v[0]))
        lines.append("NRUN=50")
        lines.append("POPSIZE=500")
        lines.append("NUMGEN=100")
        lines.append("TREELIB_SIZE=500")
        
        lines = [line+"\n" for line in lines]
        
        f.writelines(lines)
        
        
def vehinh(problem, index):
    file_name = problem + ".gen"
    ys = []
    
    algs = ["SC", 
            #'SGXM', 
            "SGXMSC", "AGX", "RDO"]
    
    for alg in algs:
        y = [alg]
        config = configs[alg][0]
        file_path = os.path.join(root_dir, alg, "out", config, file_name)
        lines = open(file_path).readlines()
        for line in lines:
            if(len(line) > 5):
                vs = line.split()
                y.append(float(vs[index]))
        print len(y)
        ys.append(y)
    
    x = [i for i in xrange(100)]
    
    print len(x)
    for y in ys:
        plt.plot(x, y[1:], label=y[0])
            
    
    plt.legend(loc='upper left')
    #dashes = [10, 5, 100, 5] # 10 points on, 5 off, 100 on, 5 off
    #line.set_dashes(dashes)
    plt.grid(True)
    
    plt.xlabel('Generations')
    plt.ylabel('Size')
    plt.title(problem)

    plt.show()
    
if __name__ == '__main__':
    #makeConfigFile();

    #getAll("fitness.csv", 0)
    #getAll("fittest.csv", 2)
    #getAll("size.csv", 4)
    #getAll("time.csv", 6)
    
    #TIME
    #getTime()
    
    #CLASSIFICATION
#    root_dir = "/home/tuananh/Documents/projects/GPClassification/src/"
#    getAllClassification("cfitness.csv", 0)
#    getAllClassification("cfittest.csv", 2)
    #getAll("csize.csv", 4)
    #getAll("ctime.csv", 6)
    
    #CHILDREN - PARENT
    #root_dir = "/home/tuananh/Documents/projects/GPChildrenParent/src/"
    #for problem in problems:
    #    get5(problem)
    
    
    #vehinh("slump_test_SLUMP", 0)
    vehinh("keijzer-4", 2)
    
    print "DONE"
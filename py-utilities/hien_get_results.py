'''
Created on Aug 16, 2013

@author: hanu
'''
import glob, csv, codecs, os


def get_average_values(dir, alg, problem):
    
    
    input_dir = os.path.join(dir, alg, problem)
    
    result_file = os.path.join(dir, alg, problem + '.average.csv')
    
    fs = csv.writer(open(result_file, 'w'), quoting=csv.QUOTE_ALL)
    
    files = glob.glob(input_dir + '/*.stat')

    vals = [[0]*26]*50
    
    num_runs = len(files)
    
    for file in files:
    
        print file

        lines = codecs.open(file).readlines()
        
        for i in xrange(len(lines)):
        
            line = lines[i].replace('[', '').replace(']', '')
            
            temp = [float(v) for v in line.strip().split()]
            
#            if temp[-1] < 1000:
                
#                num_runs[i] += 1
                                                
            vals[i] = [x + y for x, y in zip(vals[i], temp)]
    
    # for return
    retval = []
    for i in xrange(len(vals)):
        row = [float(v)/num_runs for v in vals[i]]
        retval.append(row)
        fs.writerow(row)
        
    return retval

if __name__ == '__main__':
    
    dir = 'D:\\pta\\PPSN-2014\\X100%\\3'
    
    algs = [
            #"sc",
#                "sgc.30-0.1", 
#                    "sgc.30-0.2", 
                    #"sgc.30-0.3",
#                    "sgc.30-0.4",
#                    "sgc.30-0.5",
#                    "sgc.30-0.6", 
#                    "sgc.30-0.7",
#                    "sgc.30-0.8",
#                    "sgc.30-0.9", 
                   "ssc", 
                    #"mssc", 
                 #"lgx",
                 
                 #"gtc",
            #"lgsc"
            #"michael_5",
            #"michael_12",
            #"michael_30",
            #"michael_50",
            "sgc.30-0.3",
            "sgcxm",
            "gsgp",
            "sgxe"

            ]
    
    problems = [
                #"koza-1", 
                #"koza-2", 
                #"koza-3",
                #"keijzer-10",
                #"keijzer-11", 
                #"keijzer-12", 
                #"keijzer-13", 
                #"keijzer-14",# "keijzer-15",
                #"korns-10", 
                #"korns-11", 
                #"korns-12"
                #"nguyen-2", 
                #"nguyen-3"
                #"nguyen-4",
                #"nguyen-5", 
                #"nguyen-6", 
                #"nguyen-7", 
                #"nguyen-8", 
                #"nguyen-9"
                                
                #"keijzer-1",
                #"keijzer-2",
                #"keijzer-3",
                #"keijzer-4",
                #"keijzer-6",
                #"keijzer-7",
                #"keijzer-8",
                #"keijzer-9",

                #"keijzer-10",
                #"keijzer-11", 
                
                #"keijzer-12", 
                
                #"keijzer-14", 
                #"keijzer-15",
                #"korns-10", "korns-11", 
                #"korns-12",
                "sextic",
                "septic",
                "nonic",
                "r1",
                "r2",
                "r3"
                #"nguyen-10",
                #"keijzer-1",
                #"keijzer-4",
                #"keijzer-10",
                #"keijzer-11",
                #"keijzer-12",
                #"keijzer-13",
                #"keijzer-14",
                #"keijzer-15"
                #"septic",
                #"nonic",
                #"r1",
                #"r2",
                #"r3",
                #"vladislavleva-1",
                #"vladislavleva-3",
                #"pollen",
#                "bodyfat",
                #"census",
                #"concrete",
#                "forest",
#                "mg",
#                "no",
#                "sr7n",
#                "wdbc",
                #"winequality",
#                "sr2n",
#                "sr8",
#                "sr10n",
                  #"multiplexer-11",
                  #"multiplexer-6",
                  #"parity-5",
                  #"parity-6",
                  #"parity-7"
                  #"phishing",
                ]
    
    for problem in problems:
        #sc (size, fitness), lgx(size, fitness), gsc (numofgsc, numofsc, size, fitness), lgsc(numofgsc, numoflgx, size, fitness)
        data = []
        for i in xrange(50):
            data.append([0]*29)
        for alg in algs:
            result = get_average_values(dir, alg, problem)
            for i in xrange(len(result)):
                if(alg =='sc'):  
                    #print data[i]              
                    data[i][0] = result[i][7] # size
                    data[i][1] = result[i][19] # fitness
                    data[i][2] = result[i][24] # fittest
                    #print data[i]
                elif(alg=='ssc'):
                    data[i][4] = result[i][7] # size
                    data[i][5] = result[i][19] # fitness
                    data[i][6] = result[i][24] # fittest
                elif(alg=='sgc.30-0.3'):
                    data[i][8] = result[i][1] # B
                    data[i][9] = result[i][2] # C
                    data[i][10] = result[i][7] # size
                    data[i][11] = result[i][19] # fitness
                    data[i][12] = result[i][24] #fittest
                elif(alg=='sgcxm'):
                    data[i][14] = result[i][1] # B
                    data[i][15] = result[i][2] # C
                    data[i][16] = result[i][7] # size
                    data[i][17] = result[i][19] # fitness
                    data[i][18] = result[i][24] # fittest
                elif(alg=='sgxe'):
                    data[i][20] = result[i][1] # B
                    data[i][21] = result[i][25] # size
                    data[i][22] = result[i][19] # fitness
                    data[i][23] = result[i][24] # fittest
                elif(alg=='gsgp'):
                    data[i][25] = result[i][1] # B
                    data[i][26] = result[i][25] # size
                    data[i][27] = result[i][19] # fitness
                    data[i][28] = result[i][24] # fittest

        #write data to file
        result_file = os.path.join(dir, problem + '.csv')
    
        fs = csv.writer(open(result_file, 'w'), quoting=csv.QUOTE_ALL)
        for row in data:
            fs.writerow(row)
                
                
            
        
        
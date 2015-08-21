'''
Created on Jul 17, 2015

@author: pta
'''
from os import listdir
from os.path import join
import numpy
# from summarizer.multiDocs import DUC04DESummarizer, DUC04SaDESummarizer
from summarizer.multiStepOptimizer import MultiStepOptimizer
from summarizer.duc2007 import DUC07SaDESummarizer, DUC07DESummarizer
from summarizer.duc2004 import DUC04SaDESummarizer


text_dir = '/home/pta/projects/nlp/data/DUC2004/testdata/'
out_dir = '/home/pta/projects/nlp/data/DUC2004/systems/'
    
problems = listdir(text_dir)

def oneStepOptimizing():
    
    
    for problem in problems[1:2]:
        print problem
        doc_folder = join(text_dir, problem)

#         ducSummarizer = DUC04DESummarizer()       
        ducSummarizer = DUC07SaDESummarizer()
        
        summary = ducSummarizer.summarize(doc_folder)

        out_file = join(out_dir, problem + ".SaDE")
        open(out_file, 'w').writelines([line + '\n' for line in summary])
        
def multiStepOptimizing():
    
    sizes = []
    for problem in problems:
        doc_folder = join(text_dir, problem)

        

        # summarizer
#         ducSummarizer = DUC07DESummarizer()       
#         ducSummarizer = DUC07SaDESummarizer()

        ducSummarizer = DUC04SaDESummarizer()
        
        optimizer = MultiStepOptimizer()
        summary, size = optimizer.solve(ducSummarizer, doc_folder)

        sizes.append(size)
        
        out_file = join(out_dir, problem + ".MulSaDE")
        open(out_file, 'w').writelines([line + '\n' for line in summary])
    
    sizes.append(numpy.average(sizes))
    size_file = join(out_dir, 'size.out')
    open(size_file, 'w').writelines([str(x) + '\n' for x in sizes])
    

        
if __name__ == '__main__':
    
#     oneStepOptimizing()
    
    multiStepOptimizing()
    

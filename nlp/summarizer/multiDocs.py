'''
Created on Jul 7, 2015

@author: pta
'''

import abc
import random, math, numpy, array
from os import listdir
from os.path import isfile, join


from document import helper
from deap import creator, tools, base
from tokenizer.ETokenizer import ETokenizer
from document.DUCDocument import DUC04Document
from document.DocumentCollection import DocumentCollection
from summarizer.optimizer import MDSDEOptimizer, MDSSaDEOptimizer
    
class MultiDocsSummarizer(object):
    '''
    classdocs
    '''
    __metaclass__ = abc.ABCMeta
    

    def __init__(self, max_size=250):
        '''
        Constructor
        '''
        self.multiDocs = None
        self.tokenizer = None
        self.optimizer = None

        self.max_size = max_size
    
    @abc.abstractmethod    
    def getTokenizer(self):
        pass
    
    @abc.abstractmethod
    def getDocumentCollection(self, document_collection_dir):
        pass
    
    @abc.abstractmethod
    def getOptimizer(self):
        pass
    
    @abc.abstractmethod
    def getIndexSelectedSentences(self):
        pass
    
    def renewMultiDocs(self, indexSelectedSentences):
        self.multiDocs = self.multiDocs.renewFromSummary(indexSelectedSentences)
        self.optimizer = self.getOptimizer()
        
                
    def summarize(self, document_collection_dir):
        self.tokenizer = self.getTokenizer()
        self.multiDocs = self.getDocumentCollection(document_collection_dir)    
        self.optimizer = self.getOptimizer()
        
        return self.optimizer.solve()
    

class DUCSummarizer(MultiDocsSummarizer):
    '''
    multi-document summarization for DUC Data
    '''
    def getTokenizer(self):
        return ETokenizer()
        
        


if __name__ == "__main__":
        
    pass

    
    

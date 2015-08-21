'''
Created on Jul 7, 2015

@author: pta
'''
import abc


class Document(object):
    '''
    abstract class for document
    '''
    # declares class as an abstract class
#     __metaclass__ = abc.ABCMeta

    def __init__(self, tokenizer, title='', abstract='', content=''):
        '''
        Constructor
        '''
        if(tokenizer is None):
            raise Exception('tokenizer can not be None')
        
        self.tokenizer = tokenizer
        
        self.title = title
        self.abstract = abstract
        self.content = content
        
        self.rawSentences = []
        self.wordFreq = {}
        
        self.sentences = []
        
#     @abc.abstractmethod
    def getData(self, path_to_file):
        '''
        must be implemented in concrete classes
        goal is get title, abstract and content
        '''
        pass
    
    def parseDocument(self, path_to_file):
        self.getData(path_to_file)
        self.rawSentences = self.tokenizer.getSentences(self.content)
        self._getSentences()
        
    def _getSentences(self):
        '''
        get sentences in form of list of stemmed words that are not in stopword list
        '''
        self.sentences = []
        self.rawListWordSentences = []
        
        for raw_sent in self.rawSentences:
            rawWords = self.tokenizer.getRawWords(raw_sent)
            self.rawListWordSentences.append(rawWords)
            ws = self.tokenizer.getWords(raw_sent)
            self.sentences.append(ws)
            
            # get word frequency
            for w in ws:
                if self.wordFreq.has_key(w):
                    self.wordFreq[w] += 1
                else:
                    self.wordFreq[w] = 1
                            
        
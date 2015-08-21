'''
Created on Jul 7, 2015

@author: pta
'''

from nltk.tokenize import sent_tokenize, word_tokenize
import string

class Tokenizer(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        if self.__class__ is Tokenizer:
            raise TypeError('abstract class can not be instantiated!')
        
    
    def getSentences(self, text):
        sentences = sent_tokenize(text)
        return [sent.replace('\n','').replace('\t','') for sent in sentences if len(sent) > 10]
    
    def getRawWords(self, text):
        '''
        get all words, not implement removing stopword and stemming
        '''
        return word_tokenize(text.lower().translate(None, string.punctuation))
    
    def getWords(self, text):
        '''
        get all words that are not in stopwords list and are stemmed
        '''
        raise NotImplementedError()
    
    def removeStopWords(self, words):
        raise NotImplementedError()
    
    def stemming(self, words):
        raise NotImplementedError()
    
    
        
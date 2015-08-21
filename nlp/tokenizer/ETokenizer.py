#!/usr/bin/python
# -*- coding: utf-8 -*-
'''
Created on Jul 7, 2015

@author: pta
'''
import string
from tokenizer import Tokenizer
from nltk.corpus import stopwords
from nltk.stem import porter
from nltk.tokenize import word_tokenize

class ETokenizer(Tokenizer.Tokenizer):
    '''
    Tokenizer for english documents
    '''
#     def __init__(self):
#         super.__init__()

    def removeStopWords(self, words):
        return [word for word in words if word not in stopwords.words('english')]
    
    def stemming(self, words):
        stemmer = porter.PorterStemmer()
        return [stemmer.stem(word) for word in words]

    def getWords(self, text):
        words = word_tokenize(text.lower().translate(None, string.punctuation))
        
        return self.stemming(self.removeStopWords(words))


if __name__ == '__main__':
    eTokenizer = ETokenizer()
    text = '''
    Sentence boundary disambiguation (SBD), also known as sentence breaking, 
    is the problem in natural language processing of deciding where sentences 
    begin and end. Often natural language processing tools require their input
     to be divided into sentences for a number of reasons. However sentence 
     boundary identification is challenging because punctuation marks 
     are often ambiguous. For example, a period may denote an abbreviation, 
     decimal point, an ellipsis, or an email address – not the end of a 
     sentence. About 47% of the periods in the Wall Street Journal corpus 
     denote abbreviations. As well, question marks and exclamation marks 
     may appear in embedded quotations, emoticons, computer code, and slang. 
     Languages like Japanese and Chinese have unambiguous sentence-ending 
     markers.
    '''
    print eTokenizer.getSentences(text)
#     eTokenizer.getWords()
    
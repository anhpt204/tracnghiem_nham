'''
Created on Jul 7, 2015

@author: pta
'''
import math
from document import helper

class DocumentCollection(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor
        '''
        self.sentences = []
        self.rawSentences = []
        self.rawListWordSentences = []
        self.numOfSentences = 0
        self.cosinMatrix = []
        self.meanVector = []
        self.multiDocs = None
        
    def setDocuments(self, documents):
        self.multiDocs = documents
        
        # all sentence of this collection
        self.sentences = [sent for document in documents for sent in document.sentences]
        self.rawSentences = [raw_sent for document in documents for raw_sent in document.rawSentences]
        self.rawListWordSentences = [sent for document in documents for sent in document.rawListWordSentences]
        self.numOfSentences = len(self.sentences)
        
        # term frequency of each sentence
        self._tfSentences = []
        
        self._isf = {}
        
        self._getIFAndISF()
        self._getTFISFVectors()
        self._calCosinMatrix()
        self.calMeanVector()
         
        
    def renewFromSummary(self, indexSelectedSentences):
        newMultiDocs = DocumentCollection()
        
        newMultiDocs.numOfSentences = len(indexSelectedSentences)
        
        newMultiDocs.cosinMatrix = [[0]*newMultiDocs.numOfSentences for _ in xrange(newMultiDocs.numOfSentences)]
        newMultiDocs.tfisfVectors = []
        
        for i in xrange(newMultiDocs.numOfSentences):
            idx = indexSelectedSentences[i]
            newMultiDocs.sentences.append(self.sentences[idx])
            newMultiDocs.rawSentences.append(self.rawSentences[idx])
            newMultiDocs.rawListWordSentences.append(self.rawListWordSentences[idx])
            
            # update cosin matrix
            for j in xrange(i+1, newMultiDocs.numOfSentences):
                jdx = indexSelectedSentences[j]
                newMultiDocs.cosinMatrix[i][j] = newMultiDocs.cosinMatrix[j][i] = self.cosinMatrix[idx][jdx]
                
            # tfisf vector
            newMultiDocs.tfisfVectors.append(self.tfisfVectors[idx])
            newMultiDocs.calMeanVector()
            
        return newMultiDocs
            
    def _getIFAndISF(self):
        '''
        ISF = Inverse Sentence Frequency
        '''
        self._isf = {}
        self._tfSentences = []
        # for each sentence
        for i in xrange(self.numOfSentences):
            self._tfSentences.append({})
            sent = self.sentences[i]
            # get term frequency in each sentence
            tf = self._tfSentences[i]
            for term in sent:
                if tf.has_key(term):
                    tf[term] += 1
                else:
                    tf[term] = 1
            # update ISF = number of sentence contain a term
            for term in tf.keys():
                if self._isf.has_key(term):
                    self._isf[term] += 1
                else:
                    self._isf[term] = 1
                    
        # calculate _isf = log(n/n_k)
        for term, f in self._isf.items():
            isf = math.log(1.0 * self.numOfSentences / f)
            self._isf[term] = isf
                    
    def _getTFISFVectors(self):
        '''
        get sentence vectors using tf-_isf cheme
        '''
        self.tfisfVectors = []
        for i in xrange(self.numOfSentences):
            self.tfisfVectors.append({})
            tfisf = self.tfisfVectors[i]
            tf = self._tfSentences[i]
            terms = tf.keys()
            for term in terms:
                tfisf[term] = tf[term] * self._isf[term]
                
    def getSentenceVector(self, index):
        return self.tfisfVectors[index]
                
    def _calCosinMatrix(self):
        '''
        calculate cosin similarity matrix between sentences
        '''
        self.cosinMatrix = [[0]*self.numOfSentences for _ in xrange(self.numOfSentences)]
        
        for i in xrange(self.numOfSentences-1):
            vector_i = self.tfisfVectors[i]
            for j in xrange(i+1, self.numOfSentences):
                vector_j = self.tfisfVectors[j]

                # calculate cosin
                self.cosinMatrix[i][j] = self.cosinMatrix[j][i] = helper.cosin(vector_i, vector_j)
                
    
    def calMeanVector(self):
        self.meanVector = {}
        for vector in self.tfisfVectors:
            for k, v in vector.items():
                if self.meanVector.has_key(k):
                    self.meanVector[k] += v
                else:
                    self.meanVector[k] = v
        # calculate average
        for k,v in self.meanVector.items():
            self.meanVector[k] = 1.0 * v / self.numOfSentences
            
    def getMeanVectorOfSolution(self, ind):
        meanVector = {}
        solution_len = 0
        for i in xrange(self.numOfSentences):
            if ind[i] == 1: # sentence i is selected
                solution_len += 1
                vector = self.tfisfVectors[i]
                for k,v in vector.items():
                    if meanVector.has_key(k):
                        meanVector[k] += v
                    else:
                        meanVector[k] = v
        # calculate average
        for k,v in meanVector.items():
            meanVector[k] = 1.0 * v / solution_len
            
        return meanVector
    
    
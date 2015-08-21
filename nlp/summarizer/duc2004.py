'''
Created on Aug 3, 2015

@author: pta
'''
from summarizer.multiDocs import DUCSummarizer
from os import listdir
from os.path import isfile, join
from document.DUCDocument import DUC04Document
from document.DocumentCollection import DocumentCollection
from summarizer.optimizer import MDSDEOptimizer, MDSSaDEOptimizer

class DUC04DEOptimizer(MDSDEOptimizer):


    def getSize(self, bInd):
        
        solution_size = 0
         
        for i in xrange(len(bInd)):
            if bInd[i] == 1:
                solution_size += sum([len(word) for word in self.multiDocs.rawListWordSentences[i]])
         
        return solution_size

class DUC04SaDEOptimizer(MDSSaDEOptimizer):

    
    def getSize(self, bInd):
        solution_size = 0
         
        for i in xrange(len(bInd)):
            if bInd[i] == 1:
                solution_size += sum([len(word) for word in self.multiDocs.rawListWordSentences[i]])
         
        return solution_size


class DUC04DESummarizer(DUCSummarizer):
    '''
    DE algorithm for DUC 2004 dataset
    '''
    def getDocumentCollection(self, document_collection_dir):
        files = [join(document_collection_dir, file_name) for file_name in listdir(document_collection_dir)]
        files = [f for f in files if isfile(f)]
        
        if files == None or len(files) == 0:
            raise Exception('error in path to folder containing data')
        
        docCollection = []
        for file in files:
            doc = DUC04Document(self.tokenizer)
            doc.parseDocument(file)
            docCollection.append(doc)
        
        multiDocs = DocumentCollection()
        multiDocs.setDocuments(docCollection)
        
        return multiDocs
    
    
    def getIndexSelectedSentences(self):
        return [i for i in xrange(self.multiDocs.numOfSentences) if self.optimizer.best_so_far_b[i] == 1]
    
    def getOptimizer(self):
        optimizer = DUC04DEOptimizer(self.multiDocs)
        optimizer.set_max_size(665)
        return optimizer

class DUC04SaDESummarizer(DUC04DESummarizer):
    '''
    Self-adaptive DE algorithm for DUC 2004 dataset
    '''
    def getOptimizer(self):
        optimizer = DUC04SaDEOptimizer(self.multiDocs)
        optimizer.set_max_size(665)
        return optimizer
    
if __name__ == '__main__':
    text_dir = '/home/pta/projects/nlp/data/DUC2004/testdata/'
    out_dir = '/home/pta/projects/nlp/data/DUC2004/systems/'
    
    problems = listdir(text_dir)
    
    for problem in problems:
        print problem
        doc_folder = join(text_dir, problem)

#         ducSummarizer = DUC04DESummarizer()       
        ducSummarizer = DUC04SaDESummarizer()
        
        summary = ducSummarizer.summarize(doc_folder)

        out_file = join(out_dir, problem + ".DE")
        open(out_file, 'w').write(summary)
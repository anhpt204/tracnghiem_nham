'''
Created on Aug 3, 2015

@author: pta
'''
from summarizer.multiDocs import DUCSummarizer
from os import listdir
from os.path import isfile, join
from document.DUCDocument import DUC04Document, DUC07Document
from document.DocumentCollection import DocumentCollection
from summarizer.optimizer import MDSDEOptimizer, MDSSaDEOptimizer

class DUC07DESummarizer(DUCSummarizer):
    '''
    DE algorithm for DUC 2007 dataset
    '''
    def getDocumentCollection(self, document_collection_dir):
        files = [join(document_collection_dir, file_name) for file_name in listdir(document_collection_dir)]
        files = [f for f in files if isfile(f)]
        
        if files == None or len(files) == 0:
            raise Exception('error in path to folder containing data')
        
        docCollection = []
        for file in files:
            doc = DUC07Document(self.tokenizer)
            doc.parseDocument(file)
            docCollection.append(doc)
        
        multiDocs = DocumentCollection()
        multiDocs.setDocuments(docCollection)
        
        return multiDocs
    
    def getIndexSelectedSentences(self):
        return [i for i in xrange(self.multiDocs.numOfSentences) if self.optimizer.best_so_far_b[i] == 1]
    
    def getOptimizer(self):
        return MDSDEOptimizer(self.multiDocs)

class DUC07SaDESummarizer(DUC07DESummarizer):
    '''
    Self-adaptive DE algorithm for DUC 2004 dataset
    '''
    def getOptimizer(self):
        return MDSSaDEOptimizer(self.multiDocs)
    
if __name__ == '__main__':
    text_dir = '/home/pta/projects/nlp/data/DUC2007/testdata/'
    out_dir = '/home/pta/projects/nlp/data/DUC2007/systems/'
    
    problems = listdir(text_dir)
    
    for problem in problems[:1]:
        print problem
        doc_folder = join(text_dir, problem)

        ducSummarizer = DUC07DESummarizer()       
#     ducSummarizer = DUC04SaDESummarizer()
        
        summary = ducSummarizer.summarize(doc_folder)

        out_file = join(out_dir, problem + ".DE")
        open(out_file, 'w').write(summary)
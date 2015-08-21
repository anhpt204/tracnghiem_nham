'''
Created on Jul 7, 2015

@author: pta
'''
from xml.etree import ElementTree as ET

from document import Document
from bs4 import BeautifulSoup
import re

class DUC04Document(Document.Document):
    '''
    concrete class for DUC 2004 document
    '''

        
    def getData(self, path_to_file):
#         tree = ET.parse(path_to_file)
#         self.content = tree.find('TEXT').text.strip()
        rex = re.compile('<TEXT>(.*?)</TEXT>', re.DOTALL)
        text = open(path_to_file, 'r').read()
        
        match = rex.search(text)
        
        self.content = match.groups()[0].strip()
#         print self.content
        
        
class DUC07Document(Document.Document):
    '''
    concrete class for DUC 2007 document
    '''
    
    def getData(self, path_to_file):
        rex = re.compile('<TEXT>(.*?)</TEXT>', re.DOTALL)
        text = open(path_to_file, 'r').read()
        
        match = rex.search(text)
        
        text = match.groups()[0].strip()
        text = text.replace('<P>', '').replace('</P>','')
        
        self.content = text

        

if __name__ == '__main__':
    
    # DUC 2004
#     tree = ET.parse('/home/pta/projects/nlp/data/DUC2004/testdata/d31043t/APW19981015.0163')
    parser = ET.XMLParser()
#     parser.
    tree = ET.parse('/home/pta/projects/nlp/data/DUC2007/testdata/D0739I/NYT20000724.0361')
    
    content = tree.find('TEXT').text.strip()
    
    
    
    print content
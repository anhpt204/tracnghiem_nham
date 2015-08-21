'''
Created on Aug 6, 2015

@author: pta
'''
import glob
from tokenizer.ETokenizer import ETokenizer

systems_summary_dir = '/home/pta/projects/nlp/data/DUC2007/models/'

def getAveSize():
    algs = ['MulDE', 'MulSaDE', 'DE', 'SaDE']
    tokenizer = ETokenizer()
    size = 0
    lines = []
    for alg in algs:
        files = glob.glob(systems_summary_dir + '*.' + alg)
        for file in files:
            text = open(file).read()
            words = tokenizer.getRawWords(text)
            size += sum([len(word) for word in words])
            
        size = size / (len(files))
        
        lines.append(alg + ' ' + str(size) + '\n')
        
    open('size.txt', 'w').writelines(lines)
    
if __name__ == '__main__':
    getAveSize()
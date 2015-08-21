'''
Created on Apr 18, 2013

@author: pta
'''
from bs4 import BeautifulSoup
import glob
import os
import re

dirs = [x[0] for x in os.walk('/home/pta/Documents/googlesearch/')]
#urls = []
f = open('/home/pta/projects/PhishingSpider/PhishingSpider/spiders/whitelisturls.txt', 'w')
for dir in dirs[1:]:
    print dir
    files = glob.glob(dir + '/*.htm')
    
    for file in files:
        soup = BeautifulSoup(open(file, 'r'))
        
        #print soup.prettify()
        
        for h3 in soup.findAll('h3', attrs={'class':'r'}):
            print h3
            a = h3.findAll('a')
            for a in h3.findAll('a', href = True):
                print a['href']
                f.write(a['href'] + '\n')


'''
Created on Apr 18, 2013

@author: pta
'''
from bs4 import BeautifulSoup

xmlfile = '/home/pta/projects/PhishingSpider/PhishingSpider/spiders/verified_online.xml'
output = open('/home/pta/projects/PhishingSpider/PhishingSpider/spiders/blacklisturls.txt', 'w')

soup = BeautifulSoup(open(xmlfile, 'r').read(1024*10000))
entries = soup.findAll('entry')
for entry in entries:
    if entry.url.string:
        print entry.url.string
        output.write(entry.url.string + '\n')

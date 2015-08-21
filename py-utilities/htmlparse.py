'''
Created on Apr 18, 2013

@author: pta
'''
from bs4 import BeautifulSoup
import glob
import codecs
import random

htmlfiles = glob.glob('/home/pta/projects/PhishingSpider/PhishingSpider/spiders/blacklistdata/*.txt')
output = open('/home/pta/projects/PhishingSpider/PhishingSpider/spiders/blacklistdata/features.txt', 'w')

#samplefiles = random.sample(htmlfiles, 1500)
succ = 0
tempDict = {}
for file in htmlfiles:
    ps = []
    print succ
    #file = samplefiles[n]
    #file = '/home/pta/projects/PhishingSpider/PhishingSpider/spiders/whitelistdata/1143.txt'
    try:
        soup = BeautifulSoup(open(file, 'r'))
        #print soup.prettify()
        # number of forms
        ps.append(len(soup.findAll('form')))
        #- number of input fields
        ps.append(len(soup.findAll('input')))    
        #- number of text fields
        ps.append(len(soup.findAll('input', attrs={'type':'text'})))
        #- number of password fields
        ps.append(len(soup.findAll('input', attrs={'type':'password'})))
        #- number of hidden fields
        ps.append(len(soup.findAll('input', attrs={'type':'hidden'})))    
        #- other fields: all input elements that are not in 4 classes above: radio button, check box
    
        #- number of internal links in page: check <a> tag scan <img> tag link to this page
        #- number of external links in page: check <a> tag, scan <img> link to other page
        #- number of internal links that use https protocol
        #- number of external links that use https protocol
        #- number of internal image links that use https protocol
        #- number of external image links that use https protocol
        num_internal_img_link = 0
        num_external_img_link = 0
        num_link_https = 0
        num_img_link_https = 0
        num_external_link = 0
        atags = soup.findAll('a')
        for atag in atags:
            imgtag = atag.find('img')
            if imgtag:
                if imgtag['src'].startswith('http'):
                    num_external_img_link += 1
                else:
                    num_internal_img_link += 1
                if imgtag['src'].startswith('https'):
                    num_img_link_https += 1
                # find https links
            if atag.find('href'):
                if atag['href'].startswith('https'):
                    num_link_https += 1
                if atag['href'].startswith('http'):
                    num_external_link += 1
        ps.append(num_internal_img_link)
        ps.append(num_external_img_link)
        ps.append(num_link_https)
        ps.append(num_img_link_https)
        #- other links: number of links included by other html tag: <link> tag
        linktags = soup.findAll('link')
        ps.append(len(linktags))
        #- number of external links and external images
        ps.append(num_external_link + num_external_img_link)
        #- number of links on the page that are refer to a resource on a trusted site (using whitelist google)
        #- number of Java Script tag
        script_tags = soup.findAll('script')
        ps.append(len(script_tags))
    
        key = tuple(ps)
        if tempDict.has_key(key):
            continue
        else:
            tempDict[key] = 1
        #phishing = 1 and legitimate = 0
        ps.append(1)
        t = ' '.join([str(p) for p in ps])
        output.write(t + '\n')
        succ += 1
        if succ == 1000:
            break
    except:
        continue
print 'DONE'
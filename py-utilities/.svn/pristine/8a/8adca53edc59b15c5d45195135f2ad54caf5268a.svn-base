'''
Created on Apr 23, 2013

@author: pta
'''
files = ('/home/pta/projects/ECJ20/ecj20/ec/app/myexperiences/phishing_data/whitelist.txt',
         '/home/pta/projects/ECJ20/ecj20/ec/app/myexperiences/phishing_data/blacklist.txt')

outfiles = ('/home/pta/projects/ECJ20/ecj20/ec/app/myexperiences/phishing_data/nwhitelist.txt',
         '/home/pta/projects/ECJ20/ecj20/ec/app/myexperiences/phishing_data/nblacklist.txt')

maxvalues = [0]*12 # 12 features

#check whether an item in blacklist is in whitelist or not and otherwise
blacklist = {}
whitelist={}

#get max value for each feature

for i in xrange(len(files)):
    file = files[i]
    lines = open(file, 'r').readlines()
    out = open(outfiles[i], 'w')
    for line in lines:
        feas = [int(v) for v in line.split(' ')]
        key = tuple(feas[:12])
        if i == 0 and not whitelist.has_key(key):
            whitelist[key] = 0
        elif i == 1:
            if whitelist.has_key(key):
                whitelist.pop(key)
                continue
            else:
                blacklist[key] = 1
                
        maxvalues = [v  if v >= u else u for u, v in zip(maxvalues, feas) ]

#normalize
for i in xrange(len(files)):
    #file = files[i]
    #lines = open(file, 'r').readlines()
    out = open(outfiles[i], 'w')
    if i == 0:
        for key in whitelist.keys():
            temp = [float(v)/u if u > 0 else u for u, v in zip(maxvalues, key)] + [0]
            temp = ' '.join([str(t) for t in temp]) + '\n'
            out.write(temp)
    else:
        for key in blacklist.keys():
            temp = [float(v)/u if u > 0 else u for u, v in zip(maxvalues, key)] + [1]
            temp = ' '.join([str(t) for t in temp]) + '\n'
            out.write(temp)
        
    
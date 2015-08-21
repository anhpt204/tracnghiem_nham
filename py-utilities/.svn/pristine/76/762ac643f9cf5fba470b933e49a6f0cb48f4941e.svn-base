'''
Created on Aug 15, 2013

@author: hanu
'''
X_file = '/home/hanu/pta/projects/py-utilities/X-template.java'
lines = open(X_file).readlines()

N = 1

for i in xrange(1, N):
    new_file = '/home/hanu/pta/projects/py-utilities/X' + str(i) + '.java'
    
    lines[25] = 'public class X' + str(i) + ' extends GPNode'
    lines[27] = lines[27].replace('x1', 'X'+str(i))
    lines[53] = lines[53].replace('1', str(i))
    lines[54] = lines[54].replace('0', str(i-1))
    
    open(new_file, 'w').writelines(lines)
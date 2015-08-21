'''
Created on Aug 30, 2013

@author: pta
'''


#functions = ['Add', 'Sub', 'Mul', 'Div', 'Sin', 'Cos', 'Exp', 'Log']
#functions = {'Add':2, 'Sub':2, 'Mul':2, 'Div':2}
functions = {'Add':2, 'Sub':2, 'Mul':2, 'Div':2, 'ERC':0}


NUM_OF_VARS = 50
FS_NAME = 'AddSubMulDivERC'

# hien_benchmark.params
FS_SIZE = 21 # cong 1 cho moi lan them moi

output = 'fs.txt'
fs = open(output, 'w')
# size
fs_size = 'gp.fs.size = ' + str(FS_SIZE)
lines = [fs_size]

# terminals
terminals = ['X' + str(i) for i in xrange(1, NUM_OF_VARS+1)]

terms = functions.keys() + terminals

fs_size = len(terms)

lines.append('gp.fs.{0} = ec.gp.GPFunctionSet'.format(FS_SIZE-1))
lines.append('gp.fs.{0}.name = '.format(FS_SIZE-1) + FS_NAME)
lines.append('gp.fs.{0}.size = '.format(FS_SIZE-1) + str(fs_size))

i = 0
for f in functions.keys():   
    lines.append('gp.fs.{0}.func.{1} = ec.app.regression.func.{2}'.format(FS_SIZE-1, i, f))
    lines.append('gp.fs.{0}.func.{1}.nc = nc{2}'.format(FS_SIZE-1, i, functions[f]))
    i += 1

j = i-1

for i in xrange(1, len(terminals) + 1):
    lines.append('gp.fs.{0}.func.{1} = ec.app.regression.func.X{2}'.format(FS_SIZE-1, i + j, i))
    lines.append('gp.fs.{0}.func.{1}.nc = nc0'.format(FS_SIZE-1, i+j))

lines = [line + '\n' for line in lines]  

fs.writelines(lines)
    
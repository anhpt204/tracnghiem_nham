from nsga2 import*
from deap import tools

ind1 = toolbox.individual()
ind2 = toolbox.individual()

ind1.fitness.values = toolbox.evaluate(ind1)
ind2.fitness.values = toolbox.evaluate(ind2)

import pdb
pdb.set_trace()
print(tools.sortLogNondominated([ind1, ind2], k=2))

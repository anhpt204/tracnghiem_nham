'''
Created on Aug 17, 2015

@author: pta
'''

class CTPNode():
    '''
    problem definition is by paper "The generalized Covering Salesman Problem" - Bruce Golden
    '''
    def __init__(self, id, visited_cost=0, coverage_demand=1, load=1):
        # id of node
        self.id = id
        
        # list of nodes that covered by this node
        self.cover_list = []
        
        # cost to go from this node to other node. It is a dict in form of {node_id:cost}
        self.cost_dict = {}
        
        # fixed cost associated with visiting this node
        self.visited_cost = visited_cost
        
        # a solution is feasible if this node is covered at least coverage_demand times
        self.coverage_demand = coverage_demand
        
        self.load = load
        
        
class CTPProblem():
    def __init__(self, vehicle_capacity=100000):
        self.nodes = []
        self.num_of_vehicles = 1
        self.max_nodes_per_route=0
        self.num_of_obligatory_nodes = 0
        self.num_of_nodes = None 
        self.num_of_customers = None
        self.vehicle_capacity = vehicle_capacity
        
    def load_data(self, data_path):
        lines = open(data_path, 'r').readlines()
        
        self.num_of_nodes, self.num_of_customers, self.num_of_obligatory_nodes, self.max_nodes_per_route = [int(x) for x in lines[0].split()]
        
        # initialize nodes
        for i in xrange(self.num_of_nodes + self.num_of_obligatory_nodes):
            node = CTPNode(id, visited_cost=0, coverage_demand=0)
            self.nodes.append(node)
            
        # load cost matrix
        i = 1
        line = lines[i]
        while not line.isspace():
            xs = line.split()
            id1, id2 = [int(x) for x in xs[:2]]
            distance = float(xs[-1])
            self.nodes[id1].cost_dict[id2] = distance
            self.nodes[id2].cost_dict[id1] = distance
            
            i += 1
            line = lines[i]
        
        # load covering matrix
        i += 1
        for line in lines[i:]:
            xs = [int(x) for x in line.split()]
            for i in xrange(self.num_of_customers):
                if xs[i+1] == 1:
                    self.nodes[xs[0]].cover_list.append(i)
                    
    def get_set_of_customers_covered_by(self, node_id):
        '''
        get a set of customers that covered by a node (node_id)
        '''
        return set(self.nodes[node_id].cover_list)

if __name__ == '__main__':
    data_path = '/home/pta/projects/ctp/data_ctp/kroA-13-12-75-1.ctp'
    problem = CTPProblem()
    problem.load_data(data_path)
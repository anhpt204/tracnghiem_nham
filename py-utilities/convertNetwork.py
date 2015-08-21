'''
Created on Apr 14, 2015

convert Dung's format network to MATSim format network

@author: pta
'''

# import xml.etree.ElementTree as et
import lxml.etree as et
import xml.dom.minidom
import random
import datetime
from xml.dom import minidom
from lxml.html.builder import LINK

def networkConvert():
    input_file = '/home/pta/projects/py-utilities/trafficData/SanFranciscoRoad-connected-contracted-5.txt'
    output_file = '/home/pta/projects/py-utilities/trafficData/SanFrancisco.xml'
    
    lines = open(input_file).readlines()
    
    root = et.Element('network')
    nodes = et.SubElement(root, "nodes")
    links = et.SubElement(root, "links")
    
    linkId = 1
    nodeId = 1
    isNode = True
    for line in lines[:-1]:
        if line.strip() == '-1':
            isNode = False
            continue
        
        xs = line.split()
        
        if isNode:
            nodeId += 1
#             if nodeId >= 15:
#                 continue
            
            et.SubElement(nodes, 'node', id=xs[0], x=xs[1], y=xs[2])
        else:
            # meters per second
            speed = random.randint(30, 50) * 10 / 36 # *1000 / 3600
            # link capacity = number of vehicles per hour
            capacity = random.randint(3000, 7000)
            
            link = et.SubElement(links, 'link', id=str(linkId))
#             , to=xs[1], 
#             length=xs[2], freespeed=str(speed), permlanes="1")
            link.set('from', xs[0])
            link.set('to', xs[1])
            link.set('length', xs[2])
            link.set('capacity', str(capacity))
            link.set('freespeed', str(speed))
            link.set('permlanes', '1')
            
            linkId += 1
#             if linkId == 15:
#                 break
            
    
    f = open(output_file, 'wb')
    f.write('<?xml version="1.0" ?>\n')
    f.write('<!DOCTYPE network SYSTEM "http://www.matsim.org/files/dtd/network_v1.dtd">\n')
    tree = et.ElementTree(root)
    
    
    f.write(et.tostring(tree, pretty_print=True))
#     f.write(minidom.parseString(et.tostring(tree)))

    f.close()


#     tree.write(output_file, pretty_print=True)

def convertPopulation():
    input_file = '/home/pta/projects/py-utilities/trafficData/request_day_1.txt'
    output_file = '/home/pta/projects/py-utilities/trafficData/request_day_1.xml'
    
    lines = open(input_file).readlines()[1:-1]
    
    population = et.Element('population')
    
    
    for line in lines:
#         print line
        xs = line.split()
        
        id = 'Person-' if random.gauss(0,1) > 0.5 else 'Parcel-' 
        id += xs[0]
        callTime = xs[1]
        pickupLocation = xs[2]
        deliveryLocation = xs[3]
        
        earlyPickup = str(datetime.timedelta(seconds=int(xs[4])))
        latePickup = str(datetime.timedelta(seconds=int(xs[5])))
        earlyDeliv = str(datetime.timedelta(seconds=int(xs[6])))
        lateDeliv = str(datetime.timedelta(seconds=int(xs[7])))
        
        if earlyPickup.find('day') > 0 or latePickup.find('day') > 0 or earlyDeliv.find('day')>0 or lateDeliv.find('day')>0:            
            continue
        
        maxDistance = xs[8]
        maxNbStops = xs[9]
                
        person = et.SubElement(population, "person", id=id)
        plan = et.SubElement(person, 'plan', selected='yes')
        
        pickup_act = et.SubElement(plan, 'act', type='pickup location')        
        pickup_act.set('link', pickupLocation)
        pickup_act.set('start_time', earlyPickup)
        pickup_act.set('end_time', latePickup)
        
        leg = et.SubElement(plan, 'leg', mode='taxi')
        
        route = et.SubElement(leg, 'route', type="generic")
        route.set('start_link', pickupLocation)
        route.set('end_link', deliveryLocation)
        route.set('trav_time', "undefined")
        route.set('distance',"NaN")
        
        delivery_act = et.SubElement(plan, 'act', type='delivery location')
        delivery_act.set('link', deliveryLocation)
        delivery_act.set('start_time',earlyDeliv)
        delivery_act.set('end_time',lateDeliv)
    


    f = open(output_file, 'wb')
    f.write('<?xml version="1.0" encoding="utf-8"?>\n')
    f.write('<!DOCTYPE population SYSTEM "http://www.matsim.org/files/dtd/population_v5.dtd">\n')
    tree = et.ElementTree(population)
    
    
    f.write(et.tostring(tree, pretty_print=True))
#     f.write(minidom.parseString(et.tostring(tree)))

    f.close()
if __name__ == '__main__':
    
#     networkConvert()
    
    
    convertPopulation()
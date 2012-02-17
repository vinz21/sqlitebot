#code modified from examples provided by John Eriksson at http://arainyday.se/projects/python/AStar/

#import pygame
#from pygame.locals import *

from time import time

import sys,random
#import AStar,NodeInfo
import AStar

#myNodeInfo = NodeInfo.wayNodeList()

class AStarExample:

    #myNodeInfo = NodeInfo()


    wayNodeList = [[485,10],[388,14],[485,18],[317,21],[343,22],[439,23],[343,31],[420,31],[343,39],[419,39],[503,41],[536,41],[268,45],[411,46],[442,46],[323,48],[380,50],[449,50],[459,50],[468,50],[442,53],[406,55],[382,57],[406,60],[234,61],[280,61],[309,62],[503,62],[540,62],[400,65],[439,65],[448,65],[134,67],[426,69],[383,70],[234,74],[321,74],[214,75],[290,75],[415,77],[121,78],[150,78],[508,78],[382,79],[255,80],[324,81],[196,82],[289,82],[152,84],[382,84],[415,84],[130,86],[324,86],[110,88],[117,88],[152,89],[195,89],[279,94],[308,94],[485,94],[532,94],[267,95],[273,95],[174,102],[374,104],[400,104],[338,106],[322,111],[357,112],[373,112],[174,114],[485,115],[236,116],[533,116],[173,119],[28,121],[38,121],[400,122],[320,124],[465,124],[470,124],[340,128],[167,129],[90,130],[28,133],[320,133],[340,133],[142,134],[465,136],[485,136],[533,136],[155,137],[267,140],[307,140],[401,141],[307,146],[142,147],[155,147],[167,147],[219,147],[180,148],[503,148],[312,152],[155,156],[180,156],[218,156],[186,157],[401,158],[503,158],[387,159],[487,164],[311,165],[387,166],[90,168],[55,169],[142,169],[186,169],[505,170],[125,172],[311,172],[387,172],[91,173],[304,173],[141,175],[322,176],[90,179],[130,179],[186,181],[252,181],[26,184],[165,184],[89,185],[243,185],[159,186],[426,187],[452,187],[506,187],[355,188],[390,188],[289,189],[322,189],[51,191],[89,191],[281,191],[243,194],[88,198],[271,199],[479,199],[488,199],[496,199],[281,201],[322,201],[304,202],[243,203],[452,203],[506,203],[87,204],[158,204],[471,204],[51,211],[87,211],[238,211],[270,213],[304,213],[471,214],[111,216],[104,217],[158,220],[117,221],[169,221],[243,221],[269,221],[38,223],[69,223],[100,223],[325,225],[489,225],[271,226],[169,231],[26,233],[36,233],[69,233],[55,234],[117,235],[51,239],[169,241],[157,242],[488,242],[58,244],[236,245],[257,245],[297,245],[117,249],[58,254],[118,254],[488,258],[119,260],[236,262],[327,263],[62,264],[156,264],[120,265],[242,267],[462,270],[121,271],[384,274],[232,276],[418,277],[122,279],[222,281],[434,282],[156,285],[144,289],[413,289],[434,289],[88,291],[99,291],[110,291],[327,293],[144,295],[434,295],[384,296],[99,301],[144,301],[439,301],[127,302],[170,310],[183,310],[196,310],[62,311],[87,311],[168,316],[197,318],[222,318],[248,322],[260,322],[327,323],[88,324],[127,324],[166,324],[382,324],[183,330],[182,338],[248,339],[300,339],[351,339],[414,341],[88,346],[104,346],[300,351],[88,353],[103,353],[248,353],[293,353],[222,355],[229,357],[248,365],[292,365],[230,366]]

    def initMap(self,w,h):
        #myNodeInfo = NodeInfo()
        self.mapdata = []
        self.mapw = w
        self.maph = h
        #self.startpoint = [1,1]
        #self.endpoint = [w-2,h-2]
        #self.startpoint = [320,10]
	#print random.randint(0,100)
        self.startpoint = [int(sys.argv[1]),int(sys.argv[2])]             
	#print self.startpoint[0]
        self.endpoint = [int(sys.argv[3]),int(sys.argv[4])]             
	#print self.endpoint[0]
        #self.endpoint = [0,0]
        #self.endpoint = [random.choice(AStarExample.wayNodeList)]
        way_endpoint = [random.choice(AStarExample.wayNodeList)]
        #print way_endpoint[0][0]
        
	#self.endpoint[0] = way_endpoint[0][0]
        #self.endpoint[1] = way_endpoint[0][1]

        #print self.endpoint[0]
        #print self.endpoint[1]
 
        size = w*h
        for i in range(size):
            self.mapdata.append(1)

        self.mapdata[(self.startpoint[1]*w)+self.startpoint[0]] = 5
        self.mapdata[(self.endpoint[1]*w)+self.endpoint[0]] = 6


    def findPath(self):
        
        astar = AStar.AStar(AStar.SQ_MapHandler(self.mapdata,self.mapw,self.maph))
        start = AStar.SQ_Location(self.startpoint[0],self.startpoint[1])
        end = AStar.SQ_Location(self.endpoint[0],self.endpoint[1])

        s = time()
        p = astar.findPath(start,end)
        e = time()

        if not p:
            print "No path found!"
        else:
            #print "Found path in %d moves and %f seconds." % (len(p.nodes),(e-s))
            self.pathlines = []
            self.pathlines.append((start.x*16+8,start.y*16+8))
            for n in p.nodes:
                #self.pathlines.append((n.location.x*16+8,n.location.y*16+8))
                print "%d,%d" % (n.location.x, n.location.y)
            #self.pathlines.append((end.x*16+8,end.y*16+8))
            
    def mainLoop(self):
    
	#self.initMap(34,30)
	self.initMap(551,384)        
        self.findPath()


def main():
    g = AStarExample()
    g.mainLoop()

 
#this calls the 'main' function when this script is executed
if __name__ == '__main__': main()            

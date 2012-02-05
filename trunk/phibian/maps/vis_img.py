import png, array, sys, math

#this script 'paints' a map with RGB scheme - red = high visibility(shows as yellow on the map), green = direction open, blue = direction specific

def my_range(start, end, step):
  while start<=end:
    yield start
    start += step

def los_check(x,y,angle):
  #print "x,y,angle:%d,%d,%d" % (x,y,angle)

  los_start = 1
  los_step = 1
  sight = 0

  for los_dist in my_range(los_start,range_limit,los_step): 
    new_x = int(float(x)+float(los_dist)*float(3)*math.cos(math.radians(angle)))
    new_y = int(float(y)+float(los_dist)*math.sin(math.radians(angle)))
    #print "t,new_xy,pix:%d,%d,%d,%d" % (angle,new_x,new_y,pix_list[new_y][new_x])
    if pix_list[new_y][new_x] == 0:
      break
    else:
      sight = sight+1

  return sight

##########################################
'''
print "sin:%f" % math.sin(math.radians(90))
print "cos:%f" % math.cos(math.radians(90))
print "sin:%f" % math.sin(math.radians(120))
print "cos:%f" % math.cos(math.radians(120))
print "sin:%f" % math.sin(math.radians(150))
print "cos:%f" % math.cos(math.radians(150))
'''

#sys.exit(0)

f = open('vis_map.png', 'wb')      # binary mode is important
w = png.Writer(551,384)

reader = png.Reader(filename='phibian_map_1.png')
l = reader.read()
#width, height, pixels, metadata = reader.read()
#print width,height,pixels,metadata

pixels = list(l[2])
#print pixels[0]

#create cleaner 0/255 map - pix_list 
pix_list = [[0 for col in range(2200)] for row in range(384)]
#create initial vis map - same as clean map - pix_list_vis
pix_list_vis = [[0 for col in range(2200)] for row in range(384)]
for y in range(0,383):
  p_row = []
  p_row_vis = []
  for x in my_range(0,2200,4):
    if pixels[y][x] == 57 and pixels[y][x+1] == 57 and pixels[y][x+2] == 54:
      p_row.append(0)
      p_row.append(0)
      p_row.append(0)
      p_row_vis.append(0)
      p_row_vis.append(0)
      p_row_vis.append(0)
    else:
      p_row.append(255)
      p_row.append(255)
      p_row.append(255)
      p_row_vis.append(255)
      p_row_vis.append(255)
      p_row_vis.append(255)
     
  pix_list[y] = p_row  
  pix_list_vis[y] = p_row_vis 


#visibility coloring

sight_max = 0
sight_min = 2000
direction_max = 0

#config per run
range_limit = 20
sight_range = 240
direction_range = 120
theta_step = 30

#print "test:%d" % pix_list[120][243]


for y in range(0,383):
  #for x in my_range(240,300,3):
  for x in my_range(0,1650,3):
    if pix_list[y][x] == 255:

      sight = 0
      direction = 0
      direction_avg = 0
      step_count = 0

      for theta in my_range(0,179,theta_step):
	step_count = step_count + 1
	sight_1 = los_check(x,y,theta)
	sight_2 = los_check(x,y,theta+180)
	sight = sight+sight_1+sight_2
        direction = direction + math.fabs(sight_1 - sight_2)
	direction_avg = direction/step_count
	#print "t,s1,s2,d:%d,%d,%d,%f" % (theta,sight_1,sight_2,direction)

      if sight > sight_max:
        sight_max = sight
      if sight < sight_min:
        sight_min = sight
      if direction > direction_max:
        direction_max = direction

      sight_color = int((float(sight)/float(sight_range))*float(210))+45 
      #sight_color = int((float(sight)/float(sight_range))*float(255)) 
      color_r = int(sight_color*(1-(float(direction)/float(direction_range)))) 
      pix_list_vis[y][x] = color_r

      color_g = int(255*(1-(float(direction_avg)/float(range_limit)))) 
      pix_list_vis[y][x+1] = color_g

      #color_b = int(sight_color*(float(direction)/float(direction_range))) 
      color_b = int(255*(float(direction)/float(direction_range))) 
      pix_list_vis[y][x+2] = color_b
      #print "color_r,b:%d,%d" % (color_r,color_b)

  print "y,sight_max,sight_min,dir_max:%d,%d,%d,%d" % (y,sight_max,sight_min,direction_max)

#range = 400,theta=30 1016,852 
#range = 100,theta=30 1277,984
#range = 20,theta=30 240,120
print "sight_max:%d" % sight_max

w.write(f,pix_list_vis)
f.close()



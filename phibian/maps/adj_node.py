# this script should produce a text list of waypoints(navfile.py) and adjacent waypoints(adjfile.py) from a given marked up waypoint image produced by block_img.py
# at the moment I'm getting an error on the input png file I think related to the metadata for the png file produced by block_img.py, 
# might be able to get around this by importing the png via some other package and exporting with the possible missing metadata parts

import png, array, sys, math

def box_check(x,y,box_size,color=255):
  #print "x,y,angle,color:%d,%d,%d,%d" % (x,y,box_size,color)

  found = 0
  #print "y:%d,%d" % (y-box_size,y+box_size)

  for box_y in range(y-box_size,y+box_size+1):
    for box_x in range(-box_size,box_size+1):
      #if x+box_x*3 > 150 and x+box_x*3 < 159:
      #print "box_check:%d,%d,%d" % (x*3+box_x*3,box_y,pix_list[box_y][x*3+box_x*3])
      if pix_list[box_y][x*3+box_x*3] != color and color == 255:
        found = 1
        break
      if pix_list[box_y][x*3+box_x*3] == color and color != 255:
        #if color == 254:
          #print "found"
        found = 1
        break

  return found



def los_check_pts(loc_begin_x,loc_begin_y,loc_end_x,loc_end_y,color=255):
  #print "x,y,x2,y2:%d,%d,%d,%d,%d" % (loc_begin_x,loc_begin_y,loc_end_x,loc_end_y,color)

  distance = int(math.sqrt((loc_begin_x-loc_end_x)**2+(loc_begin_y-loc_end_y)**2))
  #print "distance:%.2f" % distance

  angle = math.atan2((loc_end_y-loc_begin_y),(loc_end_x-loc_begin_x));
  #print "angle:%.2f" % math.degrees(angle)
  
  los_start = 1
  los_step = 1
  found = 0
  sight = 0

  for los_dist in range(los_start,distance,los_step): 
    new_x = int(float(loc_begin_x)+float(los_dist)*math.cos(angle))
    new_y = int(float(loc_begin_y)+float(los_dist)*math.sin(angle))
    #print "t,new_xy,pix:%d,%d,%d,%d" % (angle,new_x,new_y,pix_list[new_y][new_x])
    #print "new_xy:%d,%d" % (new_x,new_y)

    #test any non-255 color
    if color == 255 and box_check(new_x,new_y,1,color):
      found = 1
      break
    #test for specific color, black = 0
    elif color != 255 and box_check(new_x,new_y,1,color):
      found = 1
      break
    else:
      sight = sight+1

  return found


########################

png_width = 551
png_height = 384

f = open('block_map.png', 'wb')      # binary mode is important
w = png.Writer(png_width,png_height)

#reader = png.Reader(filename='phibian_map_1.png')
reader = png.Reader(filename='block_map.png')
l = reader.read()
#width, height, pixels, metadata = reader.read()
#print width,height,pixels,metadata

pixels = list(l[2])
#print pixels[0]

sys.exit(0)

navpoints = []
adjpoints = []

navpoints_csv_str = ""
navpoints_str = "["
adjpoints_str = "["

pix_list = [[0 for col in range((png_width-1)*4)] for row in range(png_height)]
for y in range(0,383):
  p_row = []
  #for x in range(0,(png_width-1)*4,4):
  for x in range(0,(png_width-1)*3,3):
    if pixels[y][x] == 0 and pixels[y][x+1] == 0 and pixels[y][x+2] == 0:
    #if pixels[y][x] == 57 and pixels[y][x+1] == 57 and pixels[y][x+2] == 54:
      p_row.extend([0,0,0])
    elif pixels[y][x] == 254 and pixels[y][x+1] == 0 and pixels[y][x+2] == 0:
      navpoints.extend([x/3,y])
      navpoints_csv_str = navpoints_csv_str + str(x/3) + ',' + str(y) + ',\n'
      navpoints_str = navpoints_str + '[' + str(x/3) + ',' + str(y) + '],'
    else:
      p_row.extend([255,255,255])

  pix_list[y] = p_row  

navpoints_str = navpoints_str[:-1]
navpoints_str = navpoints_str + ']'   

####
for loc_begin in range(0,len(navpoints),2):
  adjpoints_str = adjpoints_str + '['   
  for loc_end in range(0,len(navpoints),2):
    if loc_begin == loc_end:
      continue
    print "loc_begin:%d,%d" % (navpoints[loc_begin],navpoints[loc_begin+1])
    print "loc_end:%d,%d" % (navpoints[loc_end],navpoints[loc_end+1])
    if not los_check_pts(navpoints[loc_begin],navpoints[loc_begin+1],navpoints[loc_end],navpoints[loc_end+1],0):
      #print "not blocked" 
      adjpoints_str = adjpoints_str + 'SQ_Location(' + str(navpoints[loc_end]) + ',' + str(navpoints[loc_end+1]) + '),'
    
  adjpoints_str = adjpoints_str[:-1]
  adjpoints_str = adjpoints_str + '],'   

#adjpoints_str = adjpoints_str[:-1]
adjpoints_str = adjpoints_str + ']'   

#print "nav_str:%s" % navpoints_str
#print "adj_str:%s" % adjpoints_str

nav_csvfile = open("navfile.csv","w")
nav_csvfile.write(navpoints_csv_str)
nav_csvfile.close()
navfile = open("navfile.py","w")
navfile.write(navpoints_str+'\n')
navfile.close()
adjfile = open("adjfile.py","w")
adjfile.write(adjpoints_str+'\n')
adjfile.close()


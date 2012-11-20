# this script
# reads a png file pixels into an internal array
# subdivides that array a set of block_size rectangles doing los(line-of-sight) checks 
# colors center and side-point waynodes(red pixels) to those blocks

import png, array, sys, math, random

def my_range(start, end, step):
  while start<=end:
    yield start
    start += step

def box_check(x,y,box_size,color=255):
  #print "x,y,angle,color:%d,%d,%d,%d" % (x,y,box_size,color)

  found = 0
  #print "y:%d,%d" % (y-box_size,y+box_size)
  
  for box_y in my_range(y-box_size,y+box_size,1):
    for box_x in my_range(-box_size,box_size,1):
      #debug
      #if x+box_x*3 > 150 and x+box_x*3 < 159:
      #  print "box_check:%d,%d,%d" % (x+box_x*3,box_y,pix_list[box_y][x+box_x*3])
      if pix_list[box_y][x+box_x*3] != color and color == 255:
        found = 1
        break
      if pix_list[box_y][x+box_x*3] == color and color != 255:
        #if color == 254: #254 = waypoint_rgb_r_value
          #print "found"
        found = 1
        break

  return found


def los_check(x,y,angle,color=255):
  #print "x,y,angle,color:%d,%d,%d,%d" % (x,y,angle,color)

  #config begin - units in pixels for los(line-of-sight) check for start,stop(range_limit) and step

  range_limit = 400
  los_start = 1
  los_step = 1

  #config end
  rgb_length = 3;

  sight = 0

  for los_dist in my_range(los_start,range_limit,los_step): 
    new_x = int(float(x)+float(los_dist)*float(rgb_length)*math.cos(math.radians(angle)))
    new_y = int(float(y)+float(los_dist)*math.sin(math.radians(angle)))
    #print "t,new_xy,pix:%d,%d,%d,%d" % (angle,new_x,new_y,pix_list[new_y][new_x])
    #test any non-black color
    if pix_list[new_y][new_x] != color and color == 255:
      break
    #test for specific color, black = 0
    elif pix_list[new_y][new_x] == color and color != 255:
      break
    else:
      sight = sight+1

  return sight

def try_rect(x,y,min_width):
  #print "try_rect:x,y:%d,%d" % (x,y)
  global pix_list
  rect_width = los_check(x,y,0)
  if rect_width < min_width: 
    return 

  p_rect.extend([x,y])
  
  print "try_rect:x,y,rect_width:%d,%d,%d" % (x,y,rect_width)
  rect_y = y

  color_r = random.randint(60,200)
  color_g = random.randint(60,200)
  color_b = random.randint(60,200)
 
  #print "rect_width,r,g,g:%d,%d,%d,%d" % (rect_width,color_r,color_g,color_b)

  rect_y_size = 0
  #if min_width >= 10:
    
  while los_check(x,rect_y,0) >= rect_width and rect_y < png_height-1 and rect_y_size < png_height-1:    
    #print "los_check,rect_width:%d,%d" % (los_check(x,rect_y,0),rect_width)
    
    for rect_x in range(rect_width+1):
      #print "x,rect_x,rect_y:%d,%d,%d" % (x,rect_x,rect_y)
      if pix_list[rect_y][x+rect_x*rgb_length] == 255:
        pix_list[rect_y][x+rect_x*rgb_length] = color_r
        pix_list[rect_y][x+rect_x*rgb_length+1] = color_g
        pix_list[rect_y][x+rect_x*rgb_length+2] = color_b
    rect_y = rect_y + 1
    rect_y_size = rect_y_size + 1

  p_rect.extend([x+(rect_width+1)*rgb_length,rect_y])
   

#######################################

#sys.exit(0)

#config start
#for input file 'phibian_map_1.png'
png_width = 551
png_height = 384
rgba_length = 4; 
rgb_length = 3; 

#below RGB values are what the script sees as filled or unavailable map pixels
find_rgb_r_value = 57
find_rgb_g_value = 57
find_rgb_b_value = 54

#waypoints currently drawn as red dot RGB pixel value (254,0,0)
waypoint_rgb_r_value = 254
#config end

f = open('block_map.png', 'wb')      # binary mode is important
w = png.Writer(png_width,png_height)

reader = png.Reader(filename='phibian_map_1.png')
l = reader.read()
#width, height, pixels, metadata = reader.read()
#print width,height,pixels,metadata

pixels = list(l[2])
#print pixels[0]

pix_list = [[0 for col in range((png_width-1)*rgba_length)] for row in range(png_height)]
pix_list_vis = [[0 for col in range((png_width-1)*rgba_length)] for row in range(png_height)]
for y in range(0,png_height-1):
  p_row = []
  p_row_vis = []
  for x in my_range(0,(png_width-1)*rgba_length,rgba_length):
    if pixels[y][x] == find_rgb_r_value and pixels[y][x+1] == find_rgb_g_value and pixels[y][x+2] == find_rgb_b_value:
      p_row.extend([0,0,0])
      p_row_vis.extend([0,0,0])
    else:
      #print '%d,' % 0,
      #sys.stdout.write("0,")
      p_row.extend([255,255,255])
      p_row_vis.extend([255,255,255])
     
  pix_list[y] = p_row  
  pix_list_vis[y] = p_row_vis 


#block coloring

#print "test:%d" % pix_list[120][243]

p_rect = []


#config - the following block_sizes array will iterate over the map space with given block_sizes from largest to smallest - below sizes might need adjusting depending on the size or spaces within a given map
block_sizes = [50,16,6]

for block_size in (block_sizes):
  for y in range(0,png_height-1):
    for x in my_range(0,(png_width-1)*rgb_length,rgb_length):
      if pix_list[y][x] == 255:
        try_rect(x,y,block_size)

    print "y:%d" % (y)

  print "block_size:%d" % (block_size)

#add centerpoints
for x in my_range(0,len(p_rect)-1,rgba_length):
  print "x,y:%d,%d,%d,%d" % (p_rect[x],p_rect[x+1],p_rect[x+2],p_rect[x+3])
  x_left = p_rect[x]
  x_right = p_rect[x+2]
  y_top = p_rect[x+1]
  y_bottom = p_rect[x+3]

  x_center = x_left + int((x_right/3.0-x_left/3.0)/2.0)*3
  y_center = y_top + int((y_bottom-y_top)/2.0)
  print "x,y:%d,%d,%d" % (x_center,y_center,x_center % 3)

  #center check wall closer
  if not box_check(x_center,y_center,3,0) and not box_check(x_center,y_center,5,waypoint_rgb_r_value):
    pix_list[y_center][x_center] = waypoint_rgb_r_value
    pix_list[y_center][x_center+1] = 0
    pix_list[y_center][x_center+2] = 0

  #print "los:%d" % (los_check(x_center-5*3,y_top-1,0,0))

  #add side connecting waynodes
  box_size = 5
  if not box_check(x_center,y_top,box_size,0) and not box_check(x_center,y_top,box_size,waypoint_rgb_r_value):
    pix_list[y_top][x_center] = waypoint_rgb_r_value
    pix_list[y_top][x_center+1] = 0
    pix_list[y_top][x_center+2] = 0

  if not box_check(x_center,y_bottom,box_size,0) and not box_check(x_center,y_bottom,box_size,waypoint_rgb_r_value):
    pix_list[y_bottom-1][x_center] = waypoint_rgb_r_value
    pix_list[y_bottom-1][x_center+1] = 0
    pix_list[y_bottom-1][x_center+2] = 0

  if not box_check(x_left,y_center,box_size,0) and not box_check(x_left,y_center,box_size,waypoint_rgb_r_value):
    pix_list[y_center][x_left] = waypoint_rgb_r_value
    pix_list[y_center][x_left+1] = 0
    pix_list[y_center][x_left+2] = 0
    
  if not box_check(x_right,y_center,box_size,0) and not box_check(x_right,y_center,box_size,waypoint_rgb_r_value):
    pix_list[y_center][x_right-3] = waypoint_rgb_r_value
    pix_list[y_center][x_right+1-3] = 0
    pix_list[y_center][x_right+2-3] = 0



#draw image
w.write(f,pix_list)
f.close()


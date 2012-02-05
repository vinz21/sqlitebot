import png, array, sys, math, random

def my_range(start, end, step):
  while start<=end:
    yield start
    start += step

def los_check(x,y,angle):
  #print "x,y,angle:%d,%d,%d" % (x,y,angle)

  range_limit = 400
  los_start = 1
  los_step = 1
  sight = 0

  for los_dist in my_range(los_start,range_limit,los_step): 
    new_x = int(float(x)+float(los_dist)*float(3)*math.cos(math.radians(angle)))
    new_y = int(float(y)+float(los_dist)*math.sin(math.radians(angle)))
    #print "t,new_xy,pix:%d,%d,%d,%d" % (angle,new_x,new_y,pix_list[new_y][new_x])
    if pix_list[new_y][new_x] != 255:
      break
    else:
      sight = sight+1

  return sight

def try_rect(x,y):
  #print "try_rect:x,y:%d,%d" % (x,y)
  global pix_list
  rect_width = los_check(x,y,0)
  if rect_width < 10:
    return 
  #try to gaurantee space is vertical vs horizontal
  ''' #currently experimenting
  if rect_width < 20: 
    if try_wide_rect(x,y,rect_width) == 1:  
      return
  '''

  print "try_rect:x,y,rect_width:%d,%d,%d" % (x,y,rect_width)
  rect_y = y

  color_r = random.randint(60,254)
  color_g = random.randint(60,254)
  color_b = random.randint(60,254)
 
  #print "rect_width,r,g,g:%d,%d,%d,%d" % (rect_width,color_r,color_g,color_b)

  rect_y_size = 0 
  while los_check(x+5,rect_y,0) >= rect_width-5 and rect_y < 383 and rect_y_size < 400:
    #print "los_check,rect_width:%d,%d" % (los_check(x,rect_y,0),rect_width)
    
    for rect_x in range(rect_width+1):
      #print "x,rect_x,rect_y:%d,%d,%d" % (x,rect_x,rect_y)
      if pix_list[rect_y][x+rect_x*3] == 255:
        pix_list[rect_y][x+rect_x*3] = color_r
        pix_list[rect_y][x+rect_x*3+1] = color_g
        pix_list[rect_y][x+rect_x*3+2] = color_b
    rect_y = rect_y + 1
    rect_y_size = rect_y_size + 1
   
def try_wide_rect(x,y,rect_width):
  rect_width_try = 0
  for test_y in range(1,10):
    rect_width_try = rect_width_try + los_check(x+rect_width*3,y+test_y,180) #left space check
    rect_width_try = rect_width_try + los_check(x+rect_width*3,y+test_y,0) #right space check

  if rect_width_try > 100:
    return 1
  else:
    return 0


#######################################

#sys.exit(0)

f = open('block_map.png', 'wb')      # binary mode is important
w = png.Writer(551,384)

reader = png.Reader(filename='phibian_map_1.png')
l = reader.read()
#width, height, pixels, metadata = reader.read()
#print width,height,pixels,metadata

pixels = list(l[2])
#print pixels[0]

pix_list = [[0 for col in range(2200)] for row in range(384)]
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
      #print '%d,' % 0,
      #sys.stdout.write("0,")
      p_row.append(255)
      p_row.append(255)
      p_row.append(255)
      p_row_vis.append(255)
      p_row_vis.append(255)
      p_row_vis.append(255)
     
  pix_list[y] = p_row  
  pix_list_vis[y] = p_row_vis 


#block coloring

#print "test:%d" % pix_list[120][243]

for y in range(0,383):
  for x in my_range(0,1650,3):
    if pix_list[y][x] == 255:
      try_rect(x,y)

  print "y:%d" % (y)

w.write(f,pix_list)
f.close()


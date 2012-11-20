import png, array, sys

# this script adds a red dot waypoint grid to the existing map image
# for a better optimized waypoint distribution see script block_img.py

def my_range(start, end, step):
  while start<=end:
    yield start
    start += step


f = open('node_map.png', 'wb')      # binary mode is important
w = png.Writer(551,384)

reader = png.Reader(filename='phibian_map_1.png')
l = reader.read()
#width, height, pixels, metadata = reader.read()
#print width,height,pixels,metadata

pixels = list(l[2])
#print pixels[0]

pix_list = [[0 for col in range(2200)] for row in range(384)]
for y in range(0,383):
  p_row = []
  for x in my_range(0,2200,4):
    if pixels[y][x] == 57 and pixels[y][x+1] == 57 and pixels[y][x+2] == 54:
      p_row.append(0)
      p_row.append(0)
      p_row.append(0)
    else:
      p_row.append(255)
      p_row.append(255)
      p_row.append(255)
     
  pix_list[y] = p_row  

#now add waypoint grid

y_step = 10
x_step = 40

for y in my_range(0,383,y_step):
  p_row = []
  i = 0
  for x in my_range(0,1650,30):
    #print y,x
    pix_list[y][x] = 255
    pix_list[y][x+1] = 0
    pix_list[y][x+2] = 0
    x_lookup = i*x_step
    #print x_lookup
    #print '%d' % (pixels[y][x_lookup])
    if pixels[y][x_lookup] == 248:
      print '%d,%d' % (x/3,y)

    i = i+1


#print pix_list
w.write(f,pix_list)
f.close()



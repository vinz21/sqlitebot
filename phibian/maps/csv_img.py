import png, array, sys

#this script converts a map with wall locations - RGB = 57,57,54 - to a 0(open) or 1(closed/wall) CSV file

def my_range(start, end, step):
  while start<=end:
    yield start
    start += step

reader = png.Reader(filename='phibian_map_2.png')
l = reader.read()
pixels = list(l[2])
for y in range(0,383):
  for x in my_range(0,2200,4):
    if pixels[y][x] == 57 and pixels[y][x+1] == 57 and pixels[y][x+2] == 54:
      #print '%d,' % pixels[0][x+3],
      #print '%d,' % 1,
      sys.stdout.write("1,")
    else:
      #print '%d,' % 0,
      sys.stdout.write("0,")
  sys.stdout.write("\n") 


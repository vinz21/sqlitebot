#!/bin/perl

use strict;

##############

open(FILE_CONFIG,".\\config.ini");

#config-set vars
my $map_name;
my $map_type;
my $x;
my $y;
my $z;
my $light_density;
my $wall_density;
my $end_buffer;
my $wall_width;
my $wall_rand_length;
my $wall_min_length;
my $wall_rand_height;
my $wall_min_height;
my $wall_buffer;
my $vault_texture;
my $wall_texture_1;
my $wall_texture_2;
my $wall_texture_3;
my $wall_texture_4;
my $path_option;
my $pathnode_grid_distance;
my $player_start_max;
my $player_start_density;
my $player_start_buffer;
my $pickup_list;


foreach my $line (<FILE_CONFIG>) {
  if ($line =~ /^#/ || $line =~ /^\s/) { next; }
  chomp($line);
  my @setting = split('=',$line);
  print @setting[0]."=".@setting[1]."\n";

  if (@setting[0] eq 'map_name') { $map_name = @setting[1]; }  
  if (@setting[0] eq 'map_type') { $map_type = @setting[1]; }    
  if (@setting[0] eq 'map_size_x') { $x = @setting[1]; }
  if (@setting[0] eq 'map_size_y') { $y = @setting[1]; }
  if (@setting[0] eq 'map_size_z') { $z = @setting[1]; }
  if (@setting[0] eq 'light_density') { $light_density = @setting[1]; }
  if (@setting[0] eq 'wall_density') { $wall_density = @setting[1]; }
  if (@setting[0] eq 'end_buffer') { $end_buffer = @setting[1]; } 
  if (@setting[0] eq 'wall_width') { $wall_width = @setting[1]; }
  if (@setting[0] eq 'wall_rand_length') { $wall_rand_length = @setting[1]; }
  if (@setting[0] eq 'wall_min_length') { $wall_min_length = @setting[1]; }    
  if (@setting[0] eq 'wall_rand_height') { $wall_rand_height = @setting[1]; }
  if (@setting[0] eq 'wall_min_height') { $wall_min_height = @setting[1]; }
  if (@setting[0] eq 'wall_buffer') { $wall_buffer = @setting[1]; }  
  if (@setting[0] eq 'vault_texture') { $vault_texture = @setting[1]; }
  if (@setting[0] eq 'wall_texture_1') { $wall_texture_1 = @setting[1]; }
  if (@setting[0] eq 'wall_texture_2') { $wall_texture_2 = @setting[1]; }  
  if (@setting[0] eq 'wall_texture_3') { $wall_texture_3 = @setting[1]; }
  if (@setting[0] eq 'wall_texture_4') { $wall_texture_4 = @setting[1]; }
  if (@setting[0] eq 'path_option') { $path_option = @setting[1]; } 
  if (@setting[0] eq 'pathnode_grid_distance') { $pathnode_grid_distance = @setting[1]; }    
  if (@setting[0] eq 'player_start_max') { $player_start_max = @setting[1]; }
  if (@setting[0] eq 'player_start_density') { $player_start_density = @setting[1]; }
  if (@setting[0] eq 'player_start_buffer') { $player_start_buffer = @setting[1]; }
  if (@setting[0] eq 'pickup_list') { $pickup_list = @setting[1]; }  
}

close(FILE_CONFIG);

my %HoH = ();
my %HoH_pickups = ();

###############################################################
# BaseRoom

my $room_declare = <<"END_OF_LIST";
Begin Actor Class=LevelInfo Name=LevelInfo0
    Summary=LevelSummary'myLevel.LevelSummary'
    CameraLocationDynamic=(X=500,Y=-500,Z=300)
    CameraLocationTop=(X=500.000000,Y=300.000000,Z=40000.000000)
    CameraLocationFront=(X=500.000000,Y=300.000000,Z=40000.000000)
    CameraLocationSide=(X=300.000000,Y=300.000000,Z=40000.000000)
    CameraRotationDynamic=(Yaw=572)
    DefaultGameType="BonusPack.$map_type"
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',iLeaf=-1)
    Tag="LevelInfo"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
End Actor

Begin Actor Class=Brush Name=Brush0
    MainScale=(SheerAxis=SHEER_ZX)
    PostScale=(SheerAxis=SHEER_ZX)
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="Brush"
    Group="Cube"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=4.000000)
    Begin Brush Name=Brush
       Begin PolyList
          Begin Polygon Link=0
             Origin   +00000.000000,+00000.000000,+00000.000000
             Normal   -00001.000000,+00000.000000,+00000.000000
             TextureU +00000.000000,+00001.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +00000.000000,+00000.000000,+00000.000000
             Vertex   +00000.000000,+00000.000000,+$z.000000
             Vertex   +00000.000000,+$y.000000,+$z.000000
             Vertex   +00000.000000,+$y.000000,+00000.000000
          End Polygon
          Begin Polygon Link=1
             Origin   +00000.000000,+$y.000000,+00000.000000
             Normal   +00000.000000,+00001.000000,+00000.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +00000.000000,+$y.000000,+00000.000000
             Vertex   +00000.000000,+$y.000000,+$z.000000
             Vertex   +$x.000000,+$y.000000,+$z.000000
             Vertex   +$x.000000,+$y.000000,+00000.000000
          End Polygon
          Begin Polygon Link=2
             Origin   +$x.000000,+$y.000000,+00000.000000
             Normal   +00001.000000,+00000.000000,+00000.000000
             TextureU +00000.000000,-00001.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +$x.000000,+$y.000000,+00000.000000
             Vertex   +$x.000000,+$y.000000,+$z.000000
             Vertex   +$x.000000,+00000.000000,+$z.000000
             Vertex   +$x.000000,+00000.000000,+00000.000000
          End Polygon
          Begin Polygon Link=3
             Origin   +$x.000000,+00000.000000,+00000.000000
             Normal   +00000.000000,-00001.000000,+00000.000000
             TextureU -00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +$x.000000,+00000.000000,+00000.000000
             Vertex   +$x.000000,+00000.000000,+$z.000000
             Vertex   +00000.000000,+00000.000000,+$z.000000
             Vertex   +00000.000000,+00000.000000,+00000.000000
          End Polygon
          Begin Polygon Link=4
             Origin   +00000.000000,+$y.000000,+$z.000000
             Normal   +00000.000000,+00000.000000,+00001.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00001.000000,+00000.000000
             Vertex   +00000.000000,+$y.000000,+$z.000000
             Vertex   +00000.000000,+00000.000000,+$z.000000
             Vertex   +$x.000000,+00000.000000,+$z.000000
             Vertex   +$x.000000,+$y.000000,+$z.000000
          End Polygon
          Begin Polygon Link=5
             Origin   +00000.000000,+00000.000000,+00000.000000
             Normal   +00000.000000,+00000.000000,-00001.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,-00001.000000,+00000.000000
             Vertex   +00000.000000,+00000.000000,+00000.000000
             Vertex   +00000.000000,+$y.000000,+00000.000000
             Vertex   +$x.000000,+$y.000000,+00000.000000
             Vertex   +$x.000000,+00000.000000,+00000.000000
          End Polygon
       End PolyList
    End Brush
    Brush=Model'myLevel.Brush'
End Actor

Begin Actor Class=DefaultPhysicsVolume Name=DefaultPhysicsVolume0
    Priority=-1000000
    bNoDelete=True
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="DefaultPhysicsVolume"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
End Actor

Begin Actor Class=Brush Name=Brush1
    CsgOper=CSG_Subtract
    MainScale=(SheerAxis=SHEER_ZX)
    PostScale=(SheerAxis=SHEER_ZX)
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="Brush"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=4.000000)
    Begin Brush Name=Model9
       Begin PolyList
          Begin Polygon Texture=$vault_texture Link=0
             Origin   +00000.000000,+00000.000000,+00000.000000
             Normal   -00001.000000,+00000.000000,+00000.000000
             TextureU +00000.000000,+00001.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +00000.000000,+00000.000000,+00000.000000
             Vertex   +00000.000000,+00000.000000,+$z.000000
             Vertex   +00000.000000,+$y.000000,+$z.000000
             Vertex   +00000.000000,+$y.000000,+00000.000000
          End Polygon
          Begin Polygon Texture=$vault_texture Link=1
             Origin   +00000.000000,+$y.000000,+00000.000000
             Normal   +00000.000000,+00001.000000,+00000.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +00000.000000,+$y.000000,+00000.000000
             Vertex   +00000.000000,+$y.000000,+$z.000000
             Vertex   +$x.000000,+$y.000000,+$z.000000
             Vertex   +$x.000000,+$y.000000,+00000.000000
          End Polygon
          Begin Polygon Texture=$vault_texture Link=2
             Origin   +$x.000000,+$y.000000,+00000.000000
             Normal   +00001.000000,+00000.000000,+00000.000000
             TextureU +00000.000000,-00001.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +$x.000000,+$y.000000,+00000.000000
             Vertex   +$x.000000,+$y.000000,+$z.000000
             Vertex   +$x.000000,+00000.000000,+$z.000000
             Vertex   +$x.000000,+00000.000000,+00000.000000
          End Polygon
          Begin Polygon Texture=$vault_texture Link=3
             Origin   +$x.000000,+00000.000000,+00000.000000
             Normal   +00000.000000,-00001.000000,+00000.000000
             TextureU -00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +$x.000000,+00000.000000,+00000.000000
             Vertex   +$x.000000,+00000.000000,+$z.000000
             Vertex   +00000.000000,+00000.000000,+$z.000000
             Vertex   +00000.000000,+00000.000000,+00000.000000
          End Polygon
          Begin Polygon Texture=$vault_texture Link=4
             Origin   +00000.000000,+$y.000000,+$z.000000
             Normal   +00000.000000,+00000.000000,+00001.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00001.000000,+00000.000000
             Vertex   +00000.000000,+$y.000000,+$z.000000
             Vertex   +00000.000000,+00000.000000,+$z.000000
             Vertex   +$x.000000,+00000.000000,+$z.000000
             Vertex   +$x.000000,+$y.000000,+$z.000000
          End Polygon
          Begin Polygon Texture=$vault_texture Link=5
             Origin   +00000.000000,+00000.000000,+00000.000000
             Normal   +00000.000000,+00000.000000,-00001.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,-00001.000000,+00000.000000
             Vertex   +00000.000000,+00000.000000,+00000.000000
             Vertex   +00000.000000,+$y.000000,+00000.000000
             Vertex   +$x.000000,+$y.000000,+00000.000000
             Vertex   +$x.000000,+00000.000000,+00000.000000
          End Polygon
       End PolyList
    End Brush
    Brush=Model'myLevel.Model9'
End Actor
END_OF_LIST


###############################################################
# Lights

my $light_declare;
my $light_max = int($x*$y/$light_density); #based on $random_y
#print "light_max:$light_max\n";

#######

for(my $i = 0; $i < $light_max; $i++) {

my $random_z = int(rand(100));
my $light_z = 250+$random_z;

my $random_y = int(rand($y-200)+200); 
my $random_x = int(rand($x-200)+200);

#my $light_saturation = int(rand(255));
my $light_saturation = 255;
my $light_brightness = int(rand(64));
#my $light_hue = int(rand(255));
my $light_hue = 0;
my $light_radius = int(rand(64));


$light_declare .= <<"END_OF_LIST";
Begin Actor Class=Light Name=Light$i
    LightSaturation=$light_saturation
    LightBrightness=$light_brightness
    LightHue=$light_hue
    LightRadius=$light_radius
    bLightChanged=True
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="Light"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=$random_x,Y=$random_y,Z=$light_z)
End Actor
END_OF_LIST

}

###############################################################
# Walls

#note walls+buffer always marked on map first, lights don't collide and other pickups reference marks

my $wall_declare;
my $wall_max = int($x*$y/$wall_density);

#create wall file for reference by gnuplot,etc
open(FILE_WALL,">ut_walls.txt");

for(my $i = 2; $i < $wall_max; $i++) {

my $x_loc = int(rand($x));
my $y_loc = int(rand($y-$end_buffer*2)+$end_buffer);

my $z_loc = 0;

my $x_wall;
my $y_wall;

my $wall_type = int(rand(2));

#horizontal wall
if ($wall_type == 1) {
  $x_wall = int(rand($wall_rand_length)+$wall_min_length);
  $y_wall = $wall_width;
}
#vertical wall
else {
  $x_wall = $wall_width;
  $y_wall = int(rand($wall_rand_length)+$wall_min_length);
}

#variable wall height
my $z_wall = int(rand($wall_rand_height)+$wall_min_height);

#FIX: this currently works, but susceptible to running out of memory based on final hash size
#use spatialite or distance function

my $buffer = $wall_buffer; #buffer area around walls,etc
for (my $x_mark=$x_loc-$buffer; $x_mark<$x_loc+$x_wall+$buffer; $x_mark++) {
  for (my $y_mark=$y_loc-$buffer; $y_mark<$y_loc+$y_wall+$buffer; $y_mark++) {
    $HoH{$x_mark}{$y_mark} = 1;
  }
}

my $texture_type = '';
my $texture_choice = int(rand(4)+1);
if ($texture_choice == 1) { $texture_type = $wall_texture_1;}
if ($texture_choice == 2) { $texture_type = $wall_texture_2;}
if ($texture_choice == 3) { $texture_type = $wall_texture_3;}
if ($texture_choice == 4) { $texture_type = $wall_texture_4;}

$wall_declare .= <<"END_OF_LIST";
Begin Actor Class=Brush Name=Brush$i
    CsgOper=CSG_Add
    MainScale=(SheerAxis=SHEER_ZX)
    PostScale=(SheerAxis=SHEER_ZX)
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',iLeaf=-1)
    Tag="Brush"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=$x_loc,Y=$y_loc,Z=$z_loc)
    Begin Brush Name=Model81
       Begin PolyList
          Begin Polygon Texture=$texture_type
             Origin   +00000.000000,+00000.000000,+00000.000000
             Normal   -00001.000000,+00000.000000,+00000.000000
             TextureU +00000.000000,+00001.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +00000.000000,+00000.000000,+00000.000000
             Vertex   +00000.000000,+00000.000000,+$z_wall
             Vertex   +00000.000000,+$y_wall,+$z_wall
             Vertex   +00000.000000,+$y_wall,+00000.000000
          End Polygon
          Begin Polygon Texture=$texture_type
             Origin   +00000.000000,+$y_wall,+00000.000000
             Normal   +00000.000000,+00001.000000,+00000.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +00000.000000,+$y_wall,+00000.000000
             Vertex   +00000.000000,+$y_wall,+$z_wall
             Vertex   +$x_wall,+$y_wall,+$z_wall
             Vertex   +$x_wall,+$y_wall,+00000.000000
          End Polygon
          Begin Polygon Texture=$texture_type
             Origin   +$x_wall,+$y_wall,+00000.000000
             Normal   +00001.000000,+00000.000000,+00000.000000
             TextureU +00000.000000,-00001.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +$x_wall,+$y_wall,+00000.000000
             Vertex   +$x_wall,+$y_wall,+$z_wall
             Vertex   +$x_wall,+00000.000000,+$z_wall
             Vertex   +$x_wall,+00000.000000,+00000.000000
          End Polygon
          Begin Polygon Texture=$texture_type
             Origin   +$x_wall,+00000.000000,+00000.000000
             Normal   +00000.000000,-00001.000000,+00000.000000
             TextureU -00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00000.000000,-00001.000000
             Vertex   +$x_wall,+00000.000000,+00000.000000
             Vertex   +$x_wall,+00000.000000,+$z_wall
             Vertex   +00000.000000,+00000.000000,+$z_wall
             Vertex   +00000.000000,+00000.000000,+00000.000000
          End Polygon
          Begin Polygon Texture=$texture_type
             Origin   +00000.000000,+$y_wall,+$z_wall
             Normal   +00000.000000,+00000.000000,+00001.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,+00001.000000,+00000.000000
             Vertex   +00000.000000,+$y_wall,+$z_wall
             Vertex   +00000.000000,+00000.000000,+$z_wall
             Vertex   +$x_wall,+00000.000000,+$z_wall
             Vertex   +$x_wall,+$y_wall,+$z_wall
          End Polygon
          Begin Polygon Texture=$texture_type
             Origin   +00000.000000,+00000.000000,+00000.000000
             Normal   +00000.000000,+00000.000000,-00001.000000
             TextureU +00001.000000,+00000.000000,+00000.000000
             TextureV +00000.000000,-00001.000000,+00000.000000
             Vertex   +00000.000000,+00000.000000,+00000.000000
             Vertex   +00000.000000,+$y_wall,+00000.000000
             Vertex   +$x_wall,+$y_wall,+00000.000000
             Vertex   +$x_wall,+00000.000000,+00000.000000
          End Polygon
       End PolyList
    End Brush
    Brush=Model'myLevel.Model81'

End Actor
END_OF_LIST

######
#this describes the 4 line segments of the wall
my $x_loc_2 = $x_loc+$x_wall;
my $y_loc_2 = $y_loc+$y_wall;

#reverse x so plots like unrealed version of map display
$x_loc = $x-$x_loc;
$x_loc_2 = $x-$x_loc_2;

print FILE_WALL "$x_loc $y_loc\n$x_loc $y_loc_2\n\n$x_loc $y_loc_2\n$x_loc_2 $y_loc_2\n\n$x_loc_2 $y_loc_2\n$x_loc_2 $y_loc\n\n$x_loc_2 $y_loc\n$x_loc $y_loc\n\n";

} 

close(FILE_WALL);

###############################################################
# Paths

my $i = -1;
my $path_declare = '';

=comment
#random path placement
while ($i < $wall_max) {

my $random_y = int(rand($y-200)+200); 
my $random_x = int(rand($x-200)+200);

if ($HoH{$random_x}{$random_y} == 1) { next; }
else { $i++; }
=cut

if ($path_option eq 'true') {

my $node_distance=$pathnode_grid_distance;
for (my $x_mark=200; $x_mark<$x-200; $x_mark+=$node_distance) {
  for (my $y_mark=200; $y_mark<$y-200; $y_mark+=$node_distance) {

if ($HoH{$x_mark}{$y_mark} == 1) {

  #saturate grid square with 4 sub-pathnodes
  if ($HoH{$x_mark+$node_distance/4}{$y_mark+$node_distance/4} != 1) {
    $i++;
    $path_declare .= drop_pathnode($i,$x_mark+$node_distance/4,$y_mark+$node_distance/4);
  }
  if ($HoH{$x_mark-$node_distance/4}{$y_mark+$node_distance/4} != 1) {
    $i++;
    $path_declare .= drop_pathnode($i,$x_mark-$node_distance/4,$y_mark+$node_distance/4);
  }
  if ($HoH{$x_mark+$node_distance/4}{$y_mark-$node_distance/4} != 1) {
    $i++;
    $path_declare .= drop_pathnode($i,$x_mark+$node_distance/4,$y_mark-$node_distance/4);
  }
  if ($HoH{$x_mark-$node_distance/4}{$y_mark-$node_distance/4} != 1) {
    $i++;
    $path_declare .= drop_pathnode($i,$x_mark-$node_distance/4,$y_mark-$node_distance/4);
  }

}
else {
  $i++;
  $path_declare .= drop_pathnode($i,$x_mark,$y_mark);
} #else
} #for y
} #for x
} #if ($path_option eq 'true')

###############################################################
# PlayerStarts

=comment
#fixed player starts in map opposing corners
my $x_start_1 = $x-100;
my $y_start_1 = $y-100;

my $player_start_declare .= <<"END_OF_LIST";
Begin Actor Class=PlayerStart Name=PlayerStart0
    Base=LevelInfo'myLevel.LevelInfo0'
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="PlayerStart"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=100.000000,Y=100.000000,Z=50.000000)
End Actor
Begin Actor Class=PlayerStart Name=PlayerStart1
    Base=LevelInfo'myLevel.LevelInfo0'
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="PlayerStart"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=$x_start_1,Y=$y_start_1,Z=50.000000)
End Actor

END_OF_LIST
=cut

if ($player_start_max eq '') { $player_start_max = int($x*$y/$player_start_density); }
#print "player_start_max:$player_start_max\n";

###############################################################
# Pickups
my $i = -1;
my $pickup_declare;
#below array is type,count,buffer - make sure that map is large enough to accomodate count,buffer dynamic(high count,lower buffer)
#my @array_pickups = qw($pickup_list PlayerStart $player_start_max $player_start_buffer);
my @array_pickups = split(/\s+/,$pickup_list);
push(@array_pickups,'PlayerStart');
push(@array_pickups,$player_start_max);
#push(@array_pickups,$player_start_buffer);

#print "pickup_list:".@array_pickups."\n";

#create wall file for reference by gnuplot,etc
open(FILE_START,">ut_player_start.txt");
open(FILE_HEALTH,">ut_health.txt");
open(FILE_WEAPON,">ut_weapon.txt");
open(FILE_AMMO,">ut_ammo.txt");

while (@array_pickups) {

my $pickup_type = shift @array_pickups;
my $pickup_count = shift @array_pickups;
#my $pickup_buffer = shift @array_pickups;
my $pickup_buffer = 30;

print "$pickup_type $pickup_count $pickup_buffer\n";

while ($pickup_count > 0) {

my $random_y = int(rand($y-200)+200); 
my $random_x = int(rand($x-200)+200);

if ($HoH{$random_x}{$random_y} == 1) { next; }
if ($HoH_pickups{$pickup_type}{$random_x}{$random_y} == 1) { next; }

$pickup_count--;
$i++;
#print "i:$i\n";

#FIX: this currently works, but susceptible to running out of memory based on final hash size
#use spatialite or distance function

#mark square buffer area around pickup as taken
for (my $x_mark=$random_x-$pickup_buffer; $x_mark<$random_x+$pickup_buffer; $x_mark++) {
  for (my $y_mark=$random_y-$pickup_buffer; $y_mark<$random_y+$pickup_buffer; $y_mark++) {
    $HoH_pickups{$pickup_type}{$x_mark}{$y_mark} = 1;
    #print "$pickup_type:$x_mark:$y_mark\n";
  }
}

#reverse x for unreal edit display
my $reverse_random_x = $x-$random_x;

my $class = '';
my $type = '';
my $base = '';
my $z_pickup = 0;
my $static_mesh = '';
my $light_hue = '';

if ($pickup_type eq 'RocketLauncher' || $pickup_type eq 'FlakCannon' || $pickup_type eq 'LinkGun' || $pickup_type eq 'SniperRifle' || $pickup_type eq 'Minigun') {
  $class = 'NewWeaponBase';
  $type = "WeaponType=Class'XWeapons.$pickup_type'";
  $base = 'myPickupBase';
  $z_pickup = 4;
  $static_mesh = "StaticMeshInstance=StaticMeshInstance'myLevel.StaticMeshInstance$i";
  $light_hue = 29;  
}

if ($pickup_type eq 'AssaultAmmoPickup' || $pickup_type eq 'RocketAmmoPickup' || $pickup_type eq 'FlakAmmoPickup' || $pickup_type eq 'LinkAmmoPickup' || $pickup_type eq 'SniperAmmoPickup' || $pickup_type eq 'MinigunAmmoPickup') {
  $class = $pickup_type;
  $type = "";
  $base = 'myMarker';
  $z_pickup = 20;
  $light_hue = 244;
}

if ($pickup_type eq 'NewHealthCharger') {
  $class = $pickup_type;
  $type = "";
  $base = 'myPickupBase';
  $z_pickup = 4;
  $static_mesh = "StaticMeshInstance=StaticMeshInstance'myLevel.StaticMeshInstance$i";  
  $light_hue = 148;  
}

if ($pickup_type eq 'MiniHealthPack') {
  $class = $pickup_type;
  $type = "";
  $base = 'myMarker';
  $z_pickup = 20;
  $light_hue = 148;  
}

if ($pickup_type eq 'PlayerStart') {
  $class = $pickup_type;
  $z_pickup = 50;
  $light_hue = 90;
}

#####

if ($pickup_type eq 'PlayerStart') {

$pickup_declare .= <<"END_OF_LIST";
Begin Actor Class=$class Name=$class$pickup_count
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="$class"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=$random_x,Y=$random_y,Z=$z_pickup)
End Actor
Begin Actor Class=Spotlight Name=Spotlight$pickup_count
    LightHue=$light_hue
    LightSaturation=16
    LightBrightness=255.000000
    LightCone=16
    LightRadius=32.000000    
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="Spotlight"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=$random_x,Y=$random_y,Z=422)
    Rotation=(Pitch=-16160,Yaw=16564)
End Actor
END_OF_LIST

print FILE_START "$reverse_random_x $random_y\n";

}
else {

$pickup_declare .= <<"END_OF_LIST";
Begin Actor Class=$class Name=$class$pickup_count
    $type
    myMarker=InventorySpot'myLevel.InventorySpot$i'
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="$class"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=$random_x,Y=$random_y,Z=$z_pickup)
    $static_mesh
End Actor
Begin Actor Class=InventorySpot Name=InventorySpot$i
    $base=$class\'myLevel.$class$pickup_count'
    Base=LevelInfo'myLevel.LevelInfo0'
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="InventorySpot"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=$random_x,Y=$random_y,Z=50)
End Actor
Begin Actor Class=Spotlight Name=Spotlight$pickup_count
    LightHue=$light_hue
    LightSaturation=16
    LightBrightness=255.000000
    LightCone=16
    LightRadius=32.000000
    Level=LevelInfo'myLevel.LevelInfo0'
    Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
    Tag="Spotlight"
    PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
    Location=(X=$random_x,Y=$random_y,Z=422)
    Rotation=(Pitch=-16160,Yaw=16564)
End Actor
END_OF_LIST

if ($pickup_type =~ /Health/) { print FILE_HEALTH "$reverse_random_x $random_y\n"; }
if ($class eq 'NewWeaponBase') { print FILE_WEAPON "$reverse_random_x $random_y\n"; }
if ($pickup_type =~ /Ammo/) { print FILE_AMMO "$reverse_random_x $random_y\n"; }

}

} #while ($pickup_count > 0)
} #while (@array_pickups)

close(FILE_START);
close(FILE_HEALTH);
close(FILE_WEAPON);
close(FILE_AMMO);

###############################################################
# Finish

open(FILE_MAP,".\\ut_template.t3d");

my $template;
foreach my $line (<FILE_MAP>) {
  $template .= $line;
}

$template =~ s/<ROOM>/$room_declare/g;
$template =~ s/<LIGHT>/$light_declare/g;
$template =~ s/<PATH>/$path_declare/g;
$template =~ s/<PICKUP>/$pickup_declare/g;
$template =~ s/<WALL>/$wall_declare/g;


#print $template."\n";

open (FILE_OUT,">$map_name");
print FILE_OUT $template;
close FILE_OUT;

exit 0;


###############################################################
#Subroutines

sub drop_pathnode {

my ($i,$x,$y) = @_;

my $ret_string = <<"END_OF_LIST";
Begin Actor Class=PathNode Name=PathNode$i
      Base=LevelInfo'myLevel.LevelInfo0'
      Level=LevelInfo'myLevel.LevelInfo0'
      Region=(Zone=LevelInfo'myLevel.LevelInfo0',ZoneNumber=1)
      Tag="PathNode"
      PhysicsVolume=DefaultPhysicsVolume'myLevel.DefaultPhysicsVolume0'
      Location=(X=$x,Y=$y,Z=50.000000)
End Actor
END_OF_LIST

return $ret_string;
}

#!/usr/local/bin/perl

use strict;
use DBI;


#change the $dbname database path, $x_size of the map, $graph_name and $sql as needed to generate the output text file for gnuplot graph

my $dbname = 'sample.db';
my $x_size = 8000;

my $graph_name = 'last_seen';
#my $graph_name = 'bot_kills';
#my $graph_name = 'bot_killed';

open(OUTFILE,">$graph_name.txt");

my $dbh = DBI->connect("dbi:SQLite:dbname=$dbname","","",{RaiseError => 0}) or die "Couldn't open dbfile.";
$dbh->func(60000, 'busy_timeout'); #timeout to wait in milliseconds, 60000 = 60 sec

my ($sth,$sql);

$sql = qq{ select location from last_seen; }; 
#$sql = qq{ select killer_location from kill_score where killer_name <> 'john'; }; 
#$sql = qq{ select killed_location from kill_score where killed_name <> 'john'; }; 

#print $sql."\n";
$sth = $dbh->prepare($sql);
die "Couldn't prepare" unless defined $sth;
$sth->execute();
#print "ERROR:".$sth->err."\n" if $sth->err;
while (my ($location) = $sth->fetchrow_array) {
my ($x,$y,$z) = split(/,/,$location);

#reverse x so maps same as unrealed
$x = $x_size-$x;
if ($x == $x_size) { $x = ''; }

print OUTFILE "$x $y\n";
}

close(OUTFILE);

exit 0;



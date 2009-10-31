use strict;
use DBI;

my $dbname = 'c:\sqlitebot\sample.db';

my $dbh = DBI->connect("dbi:SQLite:dbname=$dbname", "", "",
                    { RaiseError => 1, AutoCommit => 1 });
if(!defined $dbh) {die "Cannot connect to database!\n";}
my ($sql,$sth);

#endless loop
while (1 == 1) {

$sql = qq{ select count(*) from bot where available = 1; };
$sth = $dbh->prepare( $sql );
$sth->execute();
my ($bot_slots_available) = $sth->fetchrow_array;

print "bot_slots_available:$bot_slots_available\n";

if ($bot_slots_available > 0) {
if ( ! fork() ) {
    system("javaw.exe -jar c:\\sqlitebot\\sqlitebot.jar");
    exit 0;
}
}

sleep 5;

}


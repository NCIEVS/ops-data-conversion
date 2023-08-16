#!/usr/bin/perl
## Rob Wynne, LMCO
##
## Generate content for a master mapping file
## of the LexGrid XML form.

use strict;
open( my $mapfile, $ARGV[0] ) or die "Couldn't open map file!\n";
my $source = $ARGV[1] or die "Second argument is source\n";
my $target = $ARGV[2] or die "Third argument is target\n";


while(<$mapfile>) {
  my $line = $_;
  chomp($line);
  my @vals = split('\t', $line);
  my $sourceCode = $vals[0];
  my $targetCode = $vals[1];
  $targetCode =~ s/(.*)\s*\r?\n?/$1/;
  print "        <lgRel:source sourceEntityCodeNamespace=\"$source\" sourceEntityCode=\"$sourceCode\">\n";
  print "          <lgRel:target targetEntityCodeNamespace=\"$target\" targetEntityCode=\"$targetCode\">\n";
  print "            <lgRel:associationQualification associationQualifier=\"rel\">\n";
  print "              <lgRel:qualifierText>SY</lgRel:qualifierText>\n";
  print "            </lgRel:associationQualification>\n";
  print "            <lgRel:associationQualification associationQualifier=\"score\">\n";
  print "              <lgRel:qualifierText>1</lgRel:qualifierText>\n";
  print "            </lgRel:associationQualification>\n";
  print "          </lgRel:target>\n";
  print "        </lgRel:source>\n";
}

# print "     </lgRel:associationPredicate>\n";
# print "   </relations>\n";

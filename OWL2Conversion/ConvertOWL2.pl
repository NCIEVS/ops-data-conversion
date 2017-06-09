# Convert a Thesaurus OWL1 byName export from
# Protege 3.4 (NCIEditTab 1.5.0)
# to OWL2 byName.
#
# Robert Wynne, Medical Science and Computing 2016
#
#

use strict;
open( my $owlfile, $ARGV[0]) or die "Couldn't open OWL file!\n";
open my $out, '>', "$ARGV[0]-Step12345.owl" or die "Couldn't create output file!\n";
open my $form, '>', "$ARGV[0]-format.owl" or die "Couldn't create output file!\n";
my $xsdName = "ncicp";
my @associations = ("Role_Has_Domain","Has_CDRH_Parent","Has_NICHD_Parent","Has_Data_Element","Role_Has_Range","Role_Has_Parent","Qualifier_Applies_To","Has_Salt_Form","Has_Free_Acid_Or_Base_Form","Has_Target","Concept_In_Subset","Is_Related_To_Endogenous_Product","Related_to_Genetic_Biomarker","Related_To_Genetic_Biomarker");
my @lists = ("Concept_Status","Contributing_Source","Def_Curator","Extensible_List","FDA_Table","Neoplastic_Status","Project_Name","Semantic_Type","TVS_Location","Publish_Value_Set");
my %specials;
my %propToQuals;

my @owlLines = <$owlfile>;
my $owlLineCount = @owlLines;
my $i;
my $j;
my $k;
my $highestCode = 376;
# my %externalNamespaceProperties = ("http://purl.org/dc/elements/1.1/date" => "dc:date",
# #                                   "http://protege.stanford.edu/plugins/owl/protege/readOnly" => "protege:readOnly",
#                                    "http://protege.stanford.edu/plugins/owl/protege#readOnly" => "protege1:readOnly",
#                                    "http://protege.stanford.edu/plugins/owl/protege/defaultLanguage" => "protege:defaultLanguage",
# );

$specials{"FULL_SYN"} = "term-name";
$specials{"DEFINITION"} = "def-definition";
$specials{"ALT_DEFINITION"} = "def-definition";
$specials{"GO_Annotation"} = "go-term";
$specials{"Maps_To"} = "Target_Term";

$propToQuals{"FULL_SYN"} = ["term-name","term-group","term-source","source-code","subsource-name"];
$propToQuals{"DEFINITION"} = ["def-definition","def-source","Definition_Reviewer_Name","Definition_Review_Date","attr"];
$propToQuals{"ALT_DEFINITION"} = ["def-definition","def-source","Definition_Reviewer_Name","Definition_Review_Date","attr"];
$propToQuals{"GO_Annotation"} = ["go-id","go-term","go-evi","go-source","source-date"];
$propToQuals{"Maps_To"} = ["Target_Term","Relationship_to_Target","Target_Term_Type","Target_Code","Target_Terminology"];

print "Step 0: Format complex properties inconsistencies...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
      if( $owlLines[$i] =~ /^\s*<(FULL_SYN|DEFINITION|ALT_DEFINITION|GO_Annotation|Maps_To) rdf:datatype="http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#XMLLiteral"$/ ) {
#        print "Removing $owlLines[$i]\n";
        $owlLines[$i] =~ s/rdf:datatype="http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#XMLLiteral"/rdf:parseType="Literal"/;
      }
      elsif( $owlLines[$i] =~ /.*<\/(FULL_SYN|DEFINITION|ALT_DEFINITION|GO_Annotation|Maps_To)>.*/ ) {
        my $propName = $1;
        $owlLines[$i] =~ s/&lt;!\[CDATA\[//g;
        $owlLines[$i] =~ s/\]\]&gt;//g;
        foreach(@{$propToQuals{$propName}}) {
          my $qualName = $_;
          $owlLines[$i] =~ s/&lt;(ncicp:$qualName)&gt;/<$1>/;
          $owlLines[$i] =~ s/&lt;\/(ncicp:$qualName)&gt;/<\/$1>/;
        }
      }

}

print "Printing output...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  print $form $owlLines[$i];
}

print "Step 1: Remove double-typing and ranges...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /\s\s<owl:(DatatypeProperty|AnnotationProperty|ObjectProperty).*\">/ ) {
    my $type = $1;
    my $first = $i;
    my $second = 0;
    my $third = 0;
    $i++;
    while( $owlLines[$i] !~ /\s\s<\/owl:(DatatypeProperty|AnnotationProperty|ObjectProperty)>/ ) {
      if( $owlLines[$i] =~ /\s\s\s\s<rdf:type rdf:resource="http:\/\/www.w3.org\/2002\/07\/owl#(DatatypeProperty|AnnotationProperty|ObjectProperty)"\/>/ ) {
        $second = $i;
      }
      if( $owlLines[$i] =~ /.*<rdfs:range rdf:resource="http:\/\/www.w3.org.*"\/>.*/ ) {
          $owlLines[$i] = q{};
      }
      $i++;
    }
    $third = $i;
    if( $second == 0 && $type eq "ObjectProperty" ) {
      #do nothing
    }
    else {
      $owlLines[$first] =~ s/(ObjectProperty|DatatypeProperty)/AnnotationProperty/;
      $owlLines[$third] =~ s/(ObjectProperty|DatatypeProperty)/AnnotationProperty/;
      $owlLines[$second] = q{};
    }
  }
}

print "Step 2: User-defined datatypes...\n";
foreach(@lists) {
  my $list = $_;
  my @range = ();
  my $insert;
  for( $i = 0; $i < $owlLineCount; $i++ ) {
    if( $owlLines[$i] =~ /\s*<owl:AnnotationProperty rdf:about="http:\/\/ncicb.nci.nih.gov\/xml\/owl\/EVS\/Thesaurus.owl#($list)">/ ) {
      while( $owlLines[$i] !~ /\s*<\/owl:AnnotationProperty>/ ) {
        #TODO: Consider invoking the map instantion here on the anonymous class
        # Add a while loop to do this, then add another (here on in the byCode) to convert to the
        # corresponding values
        if( $owlLines[$i] =~ /\s*<rdfs:range>/ ) {
          while( $owlLines[$i] !~ /\s*<\/rdfs:range>/ ) {
            if( $owlLines[$i] =~ /\s*>(.*)<\/rdf:first>/ ) {
              #print "Adding $1\n";
              push @range, $1;
            }
            $owlLines[$i] = q{};
            $i++;
          }
          $owlLines[$i] = "    <rdfs:range rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#$list-enum\"/>\n";
        }
        $i++;
      }
      $owlLines[$i] .= "  <rdfs:Datatype rdf:about=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#$list-enum\">\n";
      $owlLines[$i] .= "      <owl:equivalentClass>\n";
      $owlLines[$i] .= "          <rdfs:Datatype>\n";
      $owlLines[$i] .= "              <owl:oneOf>\n";
      my $space = 18;
      for( $j=0; $j < @range; $j++) {
        for( $k=0; $k < $space; $k++ ) {
          $owlLines[$i] .= " ";
        }
        $owlLines[$i] .= "<rdf:Description>\n";
        $space += 4;
        for( $k=0; $k < $space; $k++ ) {
          $owlLines[$i] .= " ";
        }
        $owlLines[$i] .= "<rdf:type rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#List\"/>\n";
        for( $k=0; $k < $space; $k++ ) {
          $owlLines[$i] .= " ";
        }
        $owlLines[$i] .= "<rdf:first>$range[$j]</rdf:first>\n";
        for( $k=0; $k < $space; $k++ ) {
          $owlLines[$i] .= " ";
        }
        if( $j + 1 == @range ) {
          $owlLines[$i] .= "<rdf:rest rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"/>\n";
        }
        else {
          $owlLines[$i] .= "<rdf:rest>\n";
          $space += 4;
        }
      }
      for( $j=0; $j < @range; $j++ ) {
        $space -= 4;
        for( $k=0; $k < $space; $k++ ) {
          $owlLines[$i] .= " ";
        }
        $owlLines[$i] .= "</rdf:Description>\n";
        $space -= 4;
        if( $j + 1 == @range ) {
          #do nothing
        }
        else {
          for( $k=0; $k < $space; $k++ ) {
            $owlLines[$i] .= " ";
          }
          $owlLines[$i] .= "</rdf:rest>\n";
        }
      }
      $owlLines[$i] .= "              </owl:oneOf>\n";
      $owlLines[$i] .= "          </rdfs:Datatype>\n";
      $owlLines[$i] .= "      </owl:equivalentClass>\n";
      $owlLines[$i] .= "  </rdfs:Datatype>\n";
    }
  }
}


print "Step 3: Convert associations to anyURI...\n";
foreach(@associations) {
  my $association = $_;
  for( $i = 0; $i < $owlLineCount; $i++ ) {
    if( $owlLines[$i] =~ /\s<owl:AnnotationProperty rdf:about=".*#$association">.*/ ) {
      $owlLines[$i] .= "    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#anyURI\"/>\n";
    }
    if( $owlLines[$i] =~ /\s\s\s\s<$association rdf:resource="(.*)"\/>/ ) {
      #do nothing
    }
    elsif( $owlLines[$i] =~ /\s*<$association>/ ) {
      $owlLines[$i] = q{};
      $i++;
      if( $owlLines[$i] =~ /\s*<owl:Class rdf:about="(.*)"\/>/ ) {
        $owlLines[$i] = "    <$association rdf:resource=\"$1\"/>\n";
        $owlLines[++$i] = q{};
      }
    }
  }
}

print "Step 4: Convert complex properties to owl:Axioms...\n";
# $specials{"FULL_SYN"} = "term-name";
# $specials{"DEFINITION"} = "def-definition";
# $specials{"ALT_DEFINITION"} = "def-definition";
# $specials{"GO_Annotation"} = "go-id";
# $specials{"Maps_To"} = "Target_Term";
#
# $propToQuals{"FULL_SYN"} = ["term-name","term-group","term-source","source-code","subsource-name"];
# $propToQuals{"DEFINITION"} = ["def-definition","def-source","Definition_Reviewer_Name","Definition_Review_Date","attr"];
# $propToQuals{"ALT_DEFINITION"} = ["def-definition","def-source","Definition_Reviewer_Name","Definition_Review_Date","attr"];
# $propToQuals{"GO_Annotation"} = ["go-id","go-term","go-evi","go-source","source-date"];
# $propToQuals{"Maps_To"} = ["Target_Term","Relationship_to_Target","Target_Term_Type","Target_Code","Target_Terminology"];

for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /(\s*)<owl:(Class|AnnotationProperty|ObjectProperty|DeprecatedClass) rdf:about=".*#(.*)">.*/ ) {
    my $space = $1;
    my $type = $2;
    my $className = $3;
    my @complexProperties = ();
    my %keepers = ();
    while( $owlLines[$i] !~ /^$space<\/owl:$type>.*/ ) {
      if( $owlLines[$i] =~ /^\s*<(FULL_SYN|DEFINITION|ALT_DEFINITION|GO_Annotation|Maps_To) rdf:parseType="Literal"$/ ) {
#        print "Removing $owlLines[$i]\n";
        $owlLines[$i] = q{};
      }
      elsif( $owlLines[$i] =~ /.*<\/(FULL_SYN|DEFINITION|ALT_DEFINITION|GO_Annotation|Maps_To)>.*/ ) {
#        print "Processing $i\n";
        my $propName = $1;
        push @complexProperties, $owlLines[$i];
        if( $owlLines[$i] =~ /.*<ncicp:$specials{$propName}>(.*)<\/ncicp:$specials{$propName}>.*/ ) {
          my $value = $1;
          if( !exists $keepers{$propName} ) {
            push @{$keepers{$propName}}, $value;
          }
          else {
            my $found = "false";
            foreach( @{$keepers{$propName}} ) {
              if( $_ eq $value ) {
                $found = "true";
              }
            }
            if( $found eq "false" ) {
              push @{$keepers{$propName}}, $value;
            }
          }
          $owlLines[$i] = q{};
        }
        else {
          print "Unconverted Axiom in $className: $owlLines[$i]\n";
        }
      }
      $i++;
    }
    $i--;
    for my $key ( sort keys %keepers ) {
      for(@{$keepers{$key}}) {
        my $value = $_;
#         $owlLines[$i] .= "    <$specials{$key} rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">$value</$specials{$key}>\n";
        $owlLines[$i] .= "    <$key>$value</$key>\n";
      }
    }
    foreach(@complexProperties) {
      my $complexProperty = $_;
      if( $complexProperty =~ /.*<\/(FULL_SYN|DEFINITION|ALT_DEFINITION|GO_Annotation|Maps_To)>.*/ ) {
        my $propName = $1;
        my %quals = ();
        my $target = q{};
        my $property = q{};
        foreach(@{$propToQuals{$propName}}) {
          my $qualName = $_;
          if( $complexProperty =~ /.*<ncicp:$qualName>(.*)<\/ncicp:$qualName>.*/ ) {
            my $qualVal = $1;
            if( $qualName ne $specials{$propName} ) {
              $quals{$qualName} = $qualVal;
            }
            else {
               $target = $qualVal;
#              $property = $qualName;
               $property = $propName;
            }
          }
        }
        $owlLines[$i+1] .= "  <owl:Axiom>\n";
        for my $key ( sort keys %quals ) {
          $owlLines[$i+1] .= "    <$key>$quals{$key}</$key>\n";
        }
        $owlLines[$i+1] .= "    <owl:annotatedTarget>$target</owl:annotatedTarget>\n";
        $owlLines[$i+1] .= "    <owl:annotatedSource rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#$className\"/>\n";
        $owlLines[$i+1] .= "    <owl:annotatedProperty rdf:resource=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#$property\"/>\n";
        $owlLines[$i+1] .= "  </owl:Axiom>\n";
      }
    }

  }
  if( $owlLines[$i] =~ /<\/owl:Ontology>/ ) {
    for my $key ( sort keys %propToQuals ) {
      if( $key ne "ALT_DEFINITION" ) {
        for(@{$propToQuals{$key}}) {
          my $value = $_;
          $highestCode++;

          $owlLines[$i] .= "  <owl:AnnotationProperty rdf:about=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#$value\">\n".
                           "    <rdfs:label>$value<\/rdfs:label>\n". #TODO: This will require a map on these guys to remove the underscore and/or hyphens associated: Check P1.5.0
                           "    <code>P$highestCode<\/code>\n".
                           "    <Preferred_Name>$value<\/Preferred_Name>\n".
                           "  <\/owl:AnnotationProperty>\n";
        }
      }
    }
    ## external properties such as
    ##   dc:date, protege:readOnly, protege:defaultLanguage
    ## Special thanks to Gabor Szabo (http://perlmaven.com/perl-hashes)
    ## for the proper hard-coding approach when using strict.
#     my @keys = keys %externalNamespaceProperties;
#     for my $key (@keys) {
#           $owlLines[$i] .= "  <owl:AnnotationProperty rdf:about=\"$key\">\n".
#                                    "    <code>$externalNamespaceProperties{$key}<\/code>\n";
# 
#           if( $key =~ /.*readOnly.*/ ) {
#               $owlLines[$i] .= "    <rdfs:range rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\"/>\n";
#           }
# 
#           $owlLines[$i] .= "  <\/owl:AnnotationProperty>\n";
#     }
  }
}

# print "Step 5: Convert fake role groups...\n";
# for( $i=0; $i < $owlLineCount; $i++ ) {
#   if( $owlLines[$i] =~ /\s*<owl:unionOf rdf:parseType="Collection">.*/ ) {
#     my $openClass;
#     my $openIntersection;
#     my $closedClass;
#     my $closedIntersection;
#      while( $owlLines[$i] !~ /\s*<\/owl:unionOf>.*/ ) {
#        if( $owlLines[$i] =~ /\s*<owl:Class>.*/ ) {
#        }
#        elsif( $owlLines[$i] =~ / / ) {
#        }
#        elsif( $owlLines[$i] =~ / / ) {
#        }
# #       print "IN\n";
#
#      }
#   }
# }

print "Printing output...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  print $out $owlLines[$i];
}
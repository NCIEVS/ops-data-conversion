## Robert Wynne, Medical Science and Computing
## 
## October 25, 2016:
##
## Convert an NCIT byName OWL2 file
## (from P5 RDF/XML Syntax output) to the byCode format
## Debug log is enabled by default
##
## November 15, 2016:
## Some unnecessary poritions commented out

use strict;
use LWP::Simple;
use URI;
my $url=URI->new($ARGV[0]);
print $url->path();

print "\nBeginning to initialize...\n";

open (my $owlfile, $url->path()) or die "Couldn't open OWL file";
##open( my $owlfile, $ARGV[0] ) or die "Couldn't open OWL file!\n";
open my $out, '>', $url->path."-byCode.owl" or die "Couldn't create output file!\n";
open my $debug, '>', "debug.txt" or die "Couldn't create debug file!\n";

my @owlLines = <$owlfile>;
my $owlLineCount = @owlLines;
my %map;
my $i;

print "\nInitialized...\n";
print "\nCollecting Code Map...\n";

print $debug "Looking at:\n";

for($i = 0; $i < $owlLineCount; $i++) {
  if( $owlLines[$i] =~ /(.*)<owl:(Class|DeprecatedClass|DatatypeProperty|FunctionalProperty|ObjectProperty|AnnotationProperty) rdf:about="(.*)?#(.*)">.*/ ) {
    my $space = $1;
    my $type = $2;
    my $namespace = $3;
    my $name = $4;

#     my $isFakeCode = "false";

    print $debug "$name\n";

#     if( $elementWithoutAttributes ne "" ) {
#       $isFakeCode = "true";
#     }

    my $code = "";
    my $adHocCode = "";

    $i++;
    while( $owlLines[$i] !~ /$space<\/owl:$type>.*\n/ ) {
      if( $owlLines[$i] =~ /.*<owl:equivalentClass>.*\n/ ) {
        while( $owlLines[$i] !~ /.*<\/owl:equivalentClass>.*\n/ ) {
          $i++;
        }
      }
      if( $owlLines[$i] =~ /.*<rdfs:subClassOf>.*/ ) {
        while( $owlLines[$i] !~ /.*<\/rdfs:subClassOf>.*/ ) {
          $i++;
        }
      }
      if( $owlLines[$i] =~ /.*<code.*>(.*)<\/code>.*\n/ ) {
        $code = $1;
      }

      $i++;
    }
    if( $code eq "" ) {
      print "WARNING: No code for concept $name\n";
    }
    else {
      $map{$name} = $code;
    }
  }
}

print "\nMap built (will print to debug.txt if enabled)\n";

for my $key ( sort keys %map ) {
        my $value = $map{$key};
        print $debug "$key => $value\n";
}

print "\nTransforming to Code...\n";

for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /(.*)<owl:(Class|DeprecatedClass|DatatypeProperty|FunctionalProperty|ObjectProperty|AnnotationProperty) rdf:about="(.*)?#(.*)"(\/)?>.*\n/ ) {
    my $space = $1;
    my $type = $2;
    my $namespace = $3;
    my $name = $4;
    my $elementWithoutAttributes = $5;

      if( exists $map{$name} ) {
        $owlLines[$i] = $space."<owl:".$type." rdf:about=\"$namespace#".$map{$name}."\"$elementWithoutAttributes>\n";
      }
      else {
        print "WARNING: Unconverted class $name\n";
      }
  }
  elsif( $owlLines[$i] =~ /(.*)<owl:Axiom>(.*)/ ) {
    while( $owlLines[$i] !~ /(.*)<\/owl:Axiom>(.*)/ ) {
      if( $owlLines[$i] =~ /(.*)<([A-Za-z_0-9\-]*)>(.*)<\/([A-Za-z0-9_\-]*)>.*/ ) {
        my $begin = $1;
        my $qual = $2;
        my $val = $3;

        if( exists $map{$qual} ) {
          $owlLines[$i] = $begin."<$map{$qual}>".$val."</$map{$qual}>\n";
        }
        else {
          print "WARNING: Unconverted axiom annotation $qual\n";
        }
      }
      elsif( $owlLines[$i] =~ /(.*)<owl:(annotatedSource|annotatedProperty) rdf:resource="(.*)?#(.*)"\/>(.*)/ ) {
        my $begin = $1;
        my $instance = $2;
        my $namespace = $3;
        my $name = $4;
        my $end = $5;
        
        if( exists $map{$name} ) {
          $owlLines[$i] = "$begin<owl:$instance rdf:resource=\"$namespace#$map{$name}\"/>\n";
        }
        else {
          print "WARNING: Unconverted axiom instance $map{$name} on line $i\n";
        }
      }
      $i++;
    }
  }
  elsif( $owlLines[$i] =~ /(.*)<code.*>(.*)<\/code>(.*)/ ) {
    my $begin = $1;
    my $codeVal = $2;
    my $end = $3;
    $owlLines[$i] = "$begin<NHC0>$codeVal</NHC0>\n";
  }
#   elsif( $owlLines[$i] =~ /(.*)<code .*/ ) {
#     ## do nothing
#   }
#   elsif( $owlLines[$i] =~ /(.*)<rdf:first>(.*)<\/rdf:first>(.*\n)/ ) {
#     my $space = $1;
#     my $name = $2;
#     my $end = $3;
#     if( exists $map{$name} ) {
#       $owlLines[$i] = $space."<rdf:first>".$map{$name}."<\/rdf:first>".$end;
#     }
#   }
  elsif( $owlLines[$i] =~ /(.*)<rdfs:label rdf:datatype.*>(.*)(<\/rdfs:label>.*\n)/ ) {
    $owlLines[$i] = "$1<rdfs:label>$2$3";
  }
#        <Concept_In_Subset rdf:datatype="http://www.w3.org/2001/XMLSchema#anyURI">http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#FDA_UNII_Code_Terminology</Concept_In_Subset>
  elsif( $owlLines[$i] =~ /(.*)<([A-Za-z0-9_\-]+) rdf:resource="(.*)#(.*)"\/>.*/ ) {
    my $space = $1;
    my $assocName = $2;
    my $namespace = $3;
    my $className = $4;

    if( exists $map{$assocName} ) {
      if( exists $map{$className} ) {
        $owlLines[$i] = "$space<$map{$assocName} rdf:resource=\"$namespace#$map{$className}\"/>\n";
      }
      else {
        print "WARNING: Missing code for class name $className\n";
      }
    }
    else {
      print "WARNING: Missing code for association name $assocName\n";
    }
  }
  elsif( $owlLines[$i] =~ /(.*)(<.*)="#(.*)("\/>.*\n)/ ) {
    my $space = $1;
    my $beginning = $2;
    my $name = $3;
    my $end = $4;
    if( exists $map{$name} ) {
      $owlLines[$i] = $space.$beginning."=\"#".$map{$name}.$end;
    }
    else {
      print "WARNING: Unconverted axiom \n\t$owlLines[$i]";
    }
  }

#   elsif( $owlLines[$i] =~ /(.*)(<([A-Z0-9a-z_]+) rdf:datatype=\"http.*#anyURI\">)(.*)?#(.*)(<[A-Z0-9a-z_]+\/>.*\n)/ ) {
#     my $space = $1;
#     my $beginning = $2;
#     my $namespace = $3;
#     my $value = $4;
#     my $end = $5;
#     if( exists $map{$value} ) {
#         $owlLines[$i] = "$space$beginning$namespace#$map{$value}$end";
#     }
#     else {
#       print "WARNING: Unconverted association with value $value\n";
#     }
#   }
  elsif( $owlLines[$i] =~ /(.*)<([A-Za-z_0-9\-]*) (rdf:datatype=\".*\")>(.*)<\/([A-Za-z0-9_\-]*)>(.*\n)/ ) {
    my $beginning = $1;
    my $property = $2;
    my $datatype = $3;
    my $filler = $4;
    my $propertyEnd = $5;  ## Really though, this should be the same as $property.
    my $end = $6;
    if( exists $map{$property} ) {
      $owlLines[$i] = "$beginning<$map{$property}>$filler</$map{$property}>$end";
    }
    else {
      print "WARNING: Unconverted \"property\" \n\t$owlLines[$i]";
    }
  }
#   elsif( $owlLines[$i] =~ /(.*<rdfs:subClassOf.*>)(.*)(<\/rdfs:subClassOf>.*\n)/ ) {
#     my $beginning = $1;
#     my $subClass = $2;
#     my $end = $3;
#     if( exists $map{$subClass} ) {
#       $owlLines[$i] = $beginning.$map{$subClass}.$end;
#     }
#   }
  elsif( $owlLines[$i] =~ /(.*)<(owl|rdf|rdfs):(domain|range|subClassOf|disjointWith|annotatedSource|annotatedProperty|onProperty|someValuesFrom|subPropertyOf|Description) (rdf:about|rdf:resource)=\"(.*)#(.*)(\"\/>.*\n)/) {
    my $space = $1;
    my $prefix = $2;
    my $construct = $3;
    my $typeSpec = $4;
    my $namespace = $5;
    my $fragment = $6;
    my $end = $7;
    if( exists $map{$fragment} ) {
      $owlLines[$i] = $space."<$prefix:$construct $typeSpec=\"$namespace#$map{$fragment}$end";
    }
  }
  elsif( $owlLines[$i] =~ /(.*)<([A-Za-z_\-]+)>(.*)<\/([A-Za-z_\-]+)>.*/ ) {
    my $space = $1;
    my $tagName = $2;
    my $value = $3;
    my $tagName2 = $4;
    if( exists $map{$tagName} ) {
      $owlLines[$i] = "$space<$map{$tagName}>$value<\/$map{$tagName}>\n";
    }
    else {
      print "WARNING: Unconverted axiom \n\t$owlLines[$i]";
    }
  }
# # RWW - Keep code property
# #   elsif( $owlLines[$i] =~ /.*<code.*<\/code>.*\n/ ) {
# #     $owlLines[$i] = "";
# #   }
#   elsif( $owlLines[$i] =~ /(.*)(<.*)="#(.*)("\/>.*\n)/ ) {
#     my $space = $1;
#     my $beginning = $2;
#     my $name = $3;
#     my $end = $4;
#     if( exists $map{$name} ) {
#       $owlLines[$i] = $space.$beginning."=\"#".$map{$name}.$end;
#     }
#     else {
#       print "WARNING: Unconverted axiom \n\t$owlLines[$i]";
#     }
#   }
#   ## RWW 091006 - complex properties
#   elsif( $owlLines[$i] =~ /(\s*>.*ncicp:Complex.*><\/)([A-Za-z0-9_-]*)(.*\n)/ ) {
#     my $beginning = $1;
#     my $property = $2;
#     my $end = $3;
#     if( exists $map{$property} ) {
#       $owlLines[$i] = $beginning.$map{$property}.$end;
#     }
#   }
#   elsif( $owlLines[$i] =~ /(.*<)([A-Za-z_0-9-]*)(.*<\/)([A-Za-z0-9_-]*)(>.*\n)/ ) {
#     my $beginning = $1;
#     my $property = $2;
#     my $filler = $3;
#     my $propertyEnd = $4;  ## Really though, this should be the same as $property.
#     my $end = $5;
#     if( exists $map{$property} ) {
#       $owlLines[$i] = $beginning.$map{$property}.$filler.$map{$property}.$end;
#     }
#   }
#   elsif( $owlLines[$i] =~ /(\s*>.*<\/)([A-Za-z0-9_-]*)(.*\n)/ ) {
#     my $beginning = $1;
#     my $property = $2;
#     my $end = $3;
#     if( exists $map{$property} ) {
#       $owlLines[$i] = $beginning.$map{$property}.$end;
#     }
#   }
#   elsif( $owlLines[$i] =~ /(\S*<\/)([A-Za-z_0-9-]*)(.*\n)/ ) {
#     my $beginning = $1;
#     my $property = $2;
#     my $end = $3;
#     if( exists $map{$property} ) {
#       $owlLines[$i] = $beginning.$map{$property}.$end;
#     }
#   }
#   elsif( $owlLines[$i] =~ /[^>]/ ) {
#     if( $owlLines[$i] =~ /(.*<)([A-Za-z_0-9-]*)(.*\n)/ ) {
#       my $beginning = $1;
#       my $property = $2;
#       my $end = $3;
#       if( exists $map{$property} ) {
#         $owlLines[$i] = $beginning.$map{$property}.$end;
#       }
#     }
#   }
#   else {
#     ##do nothing
#   }
}

## Convert association values
# for( $i = 0; $i < $owlLineCount; $i++ ) {
#  print "Trying at $i\n";
#   if( $owlLines[$i] =~ /.*<A(1-9)(0-9)*.*/ ) {
#     "  I see $owlLines[$i]\n";
#   }
#   if( $owlLines[$i] =~ /(.*)<A(0-9)+>(.*)#(.*)<\/A(0-9)+>.*/ ) {
#     print "INSPAM\n";
#     my $space = $1;
#     my $assocCode = $2;
#     my $namespace = $3;
#     my $value = $4;
#     my $assocCode2 = 5;
#     if( exists $map{$value} ) {
#         print "INSPAM2\n";
#         $owlLines[$i] = "$space<A$assocCode>$namespace#$map{$value}</A$assocCode2>\n";
#     }
#     else {
#       print "WARNING: Unconverted association with value $value at line $i\n";
#     }
#   }
# }

print "\nFinished Transforming, printing output...\n";

# Uncomment me to remove datatype bloat.
# Note: This was tried for a load, but didn't seem to help
# peformance or representation in LexBIG
#
# for( $i = 0; $i < $owlLineCount; $i++ ) {
#   if( $owlLines[$i] =~ /(.*)( rdf:datatype=".*")\n/ ) {
#     print $debug $owlLines[$i];
#     $owlLines[$i] = $1."\n";
#   }
#   elsif( $owlLines[$i] =~ /(.*)( rdf:datatype=".*")(.*)\n/ ) {
#     print $debug $owlLines[$i];
#     $owlLines[$i] = $1.$3."\n";
#   }
# }

## Create output
for( $i = 0; $i < $owlLineCount; $i++ ) {
  print $out $owlLines[$i];
}

print "\nByCode conversion complete.\n";

# for my $key ( sort keys %map ) {
#         my $value = $map{$key};
#         print $debug "$key => $value\n";
# }
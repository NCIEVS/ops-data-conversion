We will be downloading Mouse Anatomy from ftp://ftp.informatics.jax.org/pub/reports/adult_mouse_anatomy.obo
Chebi from ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi.obo
Go from ftp://ftp.geneontology.org/pub/go/ontology-archive/
ZFin from http://obo.cvs.sourceforge.net/viewvc/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/zebrafish_anatomy.obo

Steps
* Check file and grep needed field.  See if it has been updated.
* Download file and put somewhere
* Examine each file and determine if edits are needed (holds_over_chain, disjoint_from)
* Transfer processed file to /rawdata into proper folder
* Update MF 
* Update metadata.xml
* Notify user that listed vocabs are ready to load


* Load file using MF and PF, if present
* Load metadata
* Activate scheme and change tag to "PRODUCTION"
* Deactivate previous scheme and remove tag.
* Test scheme
* Check if any other vocabs need loaded.  Go to Load if so
* If no new vocabs, optimize index
We download the complete dataset from ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc_complete_set.txt.gz
on the first business day of the month.

We should then compare the http://www.genenames.org/data/gdlw_columndef.html page to one on file, to see if they
have added any columns (work still to be done)

We then process in through the HGNCtoOWL script.

Steps
1. download zip and put somewhere
2. extract zip to get flat file
3. run flat file through HGNCtoOWL
4. Update MF
5. Load OWL into LexEVS using MF and PF
6. Update metadata.xml
7. Load metadata
8. Activate scheme and change tag to "PRODUCTION"
9. Deactivate previous scheme and remove tag.
10. Test scheme
11. Optimize index

Steps to verify structure
1. check columndef page to see if it has changed
2. if changes detected, notify Ops so they can updated HGNCtoOWL config
3. DO NOT download new columndef page to overwrite.  Continue to pester Ops until they do so, so they don't forget config





Special consideration - check to see if the header page has been updated.  
Read page and generate a hash. Save that hash locally.  Check page before downloading for changes.
SHA256 to generate fixed-length strings?


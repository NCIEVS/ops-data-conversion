This command is fun to validate the resulting:

python -c "import sys, xml.dom.minidom as d; d.parse(sys.argv[1])" NCIt-HGNC-16.10e-October2016.xml

# Credit to: "Gringo Suave" http://stackoverflow.com/questions/1142438/quick-way-to-validate-xml-identify-point-of-brokenness
# No output means you've got it right :-)    

Master file comes from Liz in Excel.  Of the form:

C101621	HGNC:30500
C101656	HGNC:10000
C101603	HGNC:10074
C101727	HGNC:9887
C101572	HGNC:30660
C101644	HGNC:8824
C101560	HGNC:10810
C101612	HGNC:25321
...

Create:

NCIt-HGNC-YY.MMx-[HGNCMonthYY].xml

NOTE:

If after load the Mappings Concept Details tab doesn't show information, there is a chance bad carriage returns exist after the HGNC codes (ie HGNC:1234^M).
To remove these in vi, use the following find and replace command  :%s/\r//
To remove these through perl "perl -p -e 's/\r$//' < winfile.txt > unixfile.txt"
    OPTION - rather than use perl, you can edit the file in vi and do "set ff=unix", then save



Other possibility - trailing spaces in C code and/or HGNC code:  tr -d ' ' < input.txt > output.txt

C:\evsops\scripts\Mapping\HGNC>perl makeMapXML2.pl hugoAdditions_YYMMDD.txt NCI_Thesaurus HGNC > NCIt-HGNC-YY.MMx-[HGNCMonthYY].xml-temp

Add the header and footer (listed below)
cat header.txt NCIt-HGNC-YY.MMx-[HGNCMonthYY].txt footer.txt > NCIt-HGNC-YY.MMx-[HGNCMonthYY].xml


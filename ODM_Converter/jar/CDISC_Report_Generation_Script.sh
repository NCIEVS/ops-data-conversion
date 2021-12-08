#!/bin/bash

#
# Script to run CDISC reporting processes
# 20211015: initial script
#

#
# Prerequisites
# CDISC_HOME must have subfolders Previous and Current.  Previous folder must contain the CDISC files
# released from the previous quarter.  CDISC_HOME must also contain the INPUT_OWL e.g.
# (Thesaurus-210920-21.09c.owl).  https://github.com/NCIEVS/ops-data-conversion must also be cloned into
# the CDISC_HOME directory.
#


CDISC_HOME="C:/ncievs-code/cdisc-reporting-code/"
INPUT_OWL="Thesaurus-211025-21.10d.owl"
PUBLICATION_DATE="2021-10-25"
echo $CDISC_HOME
cd $CDISC_HOME
rm -rf $CDISC_HOME/Current/*

echo "Step 1. Generate CDISC txt and xls reports:"


#   a. CDISC ADaM Terminology (C81222)
        java -jar cdiscreportgenerator.jar $INPUT_OWL C81222
#   b. CDISC CDASH Terminology (C77527)
        java -jar cdiscreportgenerator.jar $INPUT_OWL C77527
#   c. CDISC Glossary Terminology (C67497)
        java -jar cdiscreportgenerator.jar $INPUT_OWL C67497
#   d. CDISC Protocol Terminology (C132298)
       java -jar cdiscreportgenerator.jar $INPUT_OWL C132298
#   e. CDISC QRS Terminology (C120166)
       java -jar cdiscreportgenerator.jar $INPUT_OWL C120166
#   f. CDISC SDTM Terminology (C66830)
       java -jar cdiscreportgenerator.jar $INPUT_OWL C66830
#   g. CDISC SEND Terminology (C77526)
       java -jar cdiscreportgenerator.jar $INPUT_OWL C77526
#   h. CDISC Define-XML Terminology (C77526)
       java -jar cdiscreportgenerator.jar $INPUT_OWL C165634


echo "Step 2: Format .xls reports for ODM Converter processing:"
#   a. CDISC ADaM Terminology (C81222)
	cp "CDISC ADaM Terminology.xls" "ADaM Terminology.xls"
	java -jar cdiscexcelutils.jar "ADaM Terminology.xls"     
#   b. CDISC CDASH Terminology (C77527)
	cp "CDISC CDASH Terminology.xls" "CDASH Terminology.xls"
	java -jar cdiscexcelutils.jar "CDASH Terminology.xls"     
#   c. CDISC Glossary Terminology (C67497)
	cp "CDISC Glossary Terminology.xls" "Glossary Terminology.xls"
	java -jar cdiscexcelutils.jar "Glossary Terminology.xls"     
#   d. CDISC Protocol Terminology (C132298)
	cp "CDISC Protocol Terminology.xls" "Protocol Terminology.xls"
	java -jar cdiscexcelutils.jar "Protocol Terminology.xls"     
#   e. CDISC QRS Terminology (C120166)
	cp "CDISC QRS Terminology.xls" "QRS Terminology.xls"
	java -jar cdiscexcelutils.jar "QRS Terminology.xls"     
#   f. CDISC SDTM Terminology (C66830)
	cp "CDISC SDTM Terminology.xls" "SDTM Terminology.xls"
	java -jar cdiscexcelutils.jar "SDTM Terminology.xls"     
#   g. CDISC SEND Terminology (C77526)
	cp "CDISC SEND Terminology.xls" "SEND Terminology.xls"
	java -jar cdiscexcelutils.jar "SEND Terminology.xls"     
#   h. CDISC Define-XML Terminology (C165634)
	cp "CDISC Define-XML Terminology.xls" "Define-XML Terminology.xls"
	java -jar cdiscexcelutils.jar "Define-XML Terminology.xls"     

echo "Step 3. Generate CDISC pairing reports:"
#   a. CDISC ADaM Terminology (C81222)
       java -jar cdiscpairing.jar $INPUT_OWL C81222
#   f. CDISC SDTM Terminology (C66830)
       java -jar cdiscpairing.jar $INPUT_OWL C66830
#   g. CDISC SEND Terminology (C77526)
       java -jar cdiscpairing.jar $INPUT_OWL C77526
		
echo "Step 4. Generate CDISC diff reports:"
#	a. CDISC ADaM Terminology (C81222)
if [ -e "Previous/ADaM Terminology.txt" ]; then
	java -jar cdiscversiondiff.jar "CDISC ADaM Terminology.txt" "Previous/ADaM Terminology.txt" $PUBLICATION_DATE "ADaM Terminology Changes.txt"
else 
	echo "Previous/ADaM Terminology.txt does not exist"
fi 
		
#	b. CDISC CDASH Terminology (C77527)
if [ -e "Previous/CDASH Terminology.txt" ]; then
	java -jar cdiscversiondiff.jar "CDISC CDASH Terminology.txt" "Previous/CDASH Terminology.txt" $PUBLICATION_DATE "CDASH Terminology Changes.txt"
else 
	echo "Previous/CDASH Terminology.txt does not exist"
fi 

#	c. CDISC Glossary Terminology (C67497)
if [ -e "Previous/CDISC Glossary.txt" ]; then
	java -jar cdiscversiondiff.jar "CDISC Glossary Terminology.txt" "Previous/CDISC Glossary.txt" $PUBLICATION_DATE "CDISC Glossary Changes.txt"
else 
	echo "Previous/CDISC Glossary.txt does not exist"
fi 

#	d. CDISC Protocol Terminology (C132298)
if [ -e "Previous/Protocol Terminology.txt" ]; then
	java -jar cdiscversiondiff.jar "CDISC Protocol Terminology.txt" "Previous/Protocol Terminology.txt" $PUBLICATION_DATE "Protocol Terminology Changes.txt"
else 
	echo "Previous/Protocol Terminology.txt does not exist"
fi 
		
#	e. CDISC QRS Terminology (C120166)
if [ -e "Previous/QRS Terminology.txt" ]; then
	java -jar cdiscversiondiff.jar "CDISC QRS Terminology.txt" "Previous/QRS Terminology.txt" $PUBLICATION_DATE "QRS Terminology Changes.txt"
else 
	echo "Previous/QRS Terminology.txt does not exist"
fi 

#	f. CDISC SDTM Terminology (C66830)
if [ -e "Previous/SDTM Terminology.txt" ]; then
	java -jar cdiscversiondiff.jar "CDISC SDTM Terminology.txt" "Previous/SDTM Terminology.txt" $PUBLICATION_DATE "SDTM Terminology Changes.txt"
else 
	echo "Previous/SDTM Terminology.txt does not exist"
fi 

#	g. CDISC SEND Terminology (C77526)
if [ -e "Previous/SEND Terminology.txt" ]; then
	java -jar cdiscversiondiff.jar "CDISC SEND Terminology.txt" "Previous/SEND Terminology.txt" $PUBLICATION_DATE "SEND Terminology Changes.txt"
else 
	echo "Previous/SEND Terminology.txt does not exist"
fi 

#	h. CDISC Define-XML Terminology (C165634)
if [ -e "Previous/Define-XML Terminology.txt" ]; then
	java -jar cdiscversiondiff.jar "CDISC Define-XML Terminology.txt" "Previous/Define-XML Terminology.txt" $PUBLICATION_DATE "Define-XML Terminology Changes.txt"
else 
	echo "Previous/Define-XML Terminology.txt does not exist"
fi 
		
echo "Step 5. Run ODM Conversion:"
#   Cleanup first
	rm -f $CDISC_HOME/opts-data-conversion/ODM_Converter/in/*.xls
	rm -rf $CDISC_HOME/opts-data-conversion/ODM_Converter/build/out/*
	
	#	a. CDISC ADaM Terminology (C81222)
	cp "ADaM Terminology.xls" "ops-data-conversion/ODM_Converter/in/ADaM Terminology.xls"
	cp "CDISC ADaM Terminology.txt" "ops-data-conversion/ODM_Converter/in/ADaM Terminology.txt"
	#	b. CDISC CDASH Terminology (C77527)
	cp "CDASH Terminology.xls" "ops-data-conversion/ODM_Converter/in/CDASH Terminology.xls"
	cp "CDISC CDASH Terminology.txt" "ops-data-conversion/ODM_Converter/in/CDASH Terminology.txt"
	#	c. CDISC Glossary Terminology (C67497)
	cp "Glossary Terminology.xls" "ops-data-conversion/ODM_Converter/in/Glossary Terminology.xls"
	cp "CDISC Glossary Terminology.txt" "ops-data-conversion/ODM_Converter/in/Glossary Terminology.txt"
	#	d. CDISC Protocol Terminology (C132298)
	cp "Protocol Terminology.xls" "ops-data-conversion/ODM_Converter/in/Protocol Terminology.xls"
	cp "CDISC Protocol Terminology.txt" "ops-data-conversion/ODM_Converter/in/Protocol Terminology.txt"
	#	e. CDISC QRS Terminology (C120166)
	cp "QRS Terminology.xls" "ops-data-conversion/ODM_Converter/in/QRS Terminology.xls"
	cp "CDISC QRS Terminology.txt" "ops-data-conversion/ODM_Converter/in/QRS Terminology.txt"
	#	f. CDISC SDTM Terminology (C66830)
	cp "SDTM Terminology.xls" "ops-data-conversion/ODM_Converter/in/SDTM Terminology.xls"
	cp "CDISC SDTM Terminology.txt" "ops-data-conversion/ODM_Converter/in/SDTM Terminology.txt"
	#	g. CDISC SEND Terminology (C77526)
	cp "SEND Terminology.xls" "ops-data-conversion/ODM_Converter/in/SEND Terminology.xls"
	cp "CDISC SEND Terminology.txt" "ops-data-conversion/ODM_Converter/in/SEND Terminology.txt"
	#	h. CDISC Define-XML Terminology (C165634)
	cp "Define-XML Terminology.xls" "ops-data-conversion/ODM_Converter/in/Define-XML Terminology.xls"
	cp "CDISC Define-XML Terminology.txt" "ops-data-conversion/ODM_Converter/in/Define-XML Terminology.txt"
	
	cd $CDISC_HOME/ops-data-conversion/ODM_Converter
	C:/apache/apache-ant-1.10.9/bin/ant > ant.log
	cp -r $CDISC_HOME/ops-data-conversion/ODM_Converter/build/out/* $CDISC_HOME/Current
	cd $CDISC_HOME/ops-data-conversion
	
echo "Step 6. Move files and clean-up:"	
	#	a. CDISC ADaM Terminology (C81222)
	mv $CDISC_HOME/*ADaM*.txt $CDISC_HOME/Current/ADaM	
	mv $CDISC_HOME/ADaM_paired*.xlsx $CDISC_HOME/Current/ADaM
	rm -f $CDISC_HOME/*ADaM*.xls
	cp $CDISC_HOME/ct-schema.owl $CDISC_HOME/Current/ADaM
	cp $CDISC_HOME/meta-model-schema.owl $CDISC_HOME/Current/ADaM
	cd $CDISC_HOME/Current/ADaM
	zip "ADaM Terminology.OWL.zip" "ADaM Terminology.owl" ct-schema.owl meta-model-schema.owl
	if ! [ -e "$CDISC_HOME/Current/ADaM/Archive" ]; then
		mkdir $CDISC_HOME/Current/ADaM/Archive
	fi 
	cp $CDISC_HOME/Current/ADaM/"ADaM Terminology.OWL.zip" $CDISC_HOME/Current/ADaM/Archive/"ADaM Terminology $PUBLICATION_DATE.OWL.zip"
	cp $CDISC_HOME/Current/ADaM/"ADaM Terminology.xls" $CDISC_HOME/Current/ADaM/Archive/"ADaM Terminology $PUBLICATION_DATE.xls"
	cp $CDISC_HOME/Current/ADaM/"ADaM Terminology.txt" $CDISC_HOME/Current/ADaM/Archive/"ADaM Terminology $PUBLICATION_DATE.txt"
	cp $CDISC_HOME/Current/ADaM/"ADaM Terminology.pdf" $CDISC_HOME/Current/ADaM/Archive/"ADaM Terminology $PUBLICATION_DATE.pdf"
	cp $CDISC_HOME/Current/ADaM/"ADaM Terminology.html" $CDISC_HOME/Current/ADaM/Archive/"ADaM Terminology $PUBLICATION_DATE.html"
	cp $CDISC_HOME/Current/ADaM/"ADaM Terminology.odm.xml" $CDISC_HOME/Current/ADaM/Archive/"ADaM Terminology $PUBLICATION_DATE.odm.xml"
	cd $CDISC_HOME

	#	b. CDISC CDASH Terminology (C77527)
	mv $CDISC_HOME/*CDASH*.txt $CDISC_HOME/Current/SDTM	
	rm -f $CDISC_HOME/*CDASH*.xls
	cp $CDISC_HOME/ct-schema.owl $CDISC_HOME/Current/SDTM
	cp $CDISC_HOME/meta-model-schema.owl $CDISC_HOME/Current/SDTM
	cd $CDISC_HOME/Current/SDTM
	zip "CDASH Terminology.OWL.zip" "CDASH Terminology.owl" ct-schema.owl meta-model-schema.owl
	if ! [ -e "$CDISC_HOME/Current/SDTM/Archive" ]; then
		mkdir $CDISC_HOME/Current/SDTM/Archive
	fi 
	cp $CDISC_HOME/Current/SDTM/"CDASH Terminology.OWL.zip" $CDISC_HOME/Current/SDTM/Archive/"CDASH Terminology $PUBLICATION_DATE.OWL.zip"
	cp $CDISC_HOME/Current/SDTM/"CDASH Terminology.xls" $CDISC_HOME/Current/SDTM/Archive/"CDASH Terminology $PUBLICATION_DATE.xls"
	cp $CDISC_HOME/Current/SDTM/"CDASH Terminology.txt" $CDISC_HOME/Current/SDTM/Archive/"CDASH Terminology $PUBLICATION_DATE.txt"
	cp $CDISC_HOME/Current/SDTM/"CDASH Terminology.pdf" $CDISC_HOME/Current/SDTM/Archive/"CDASH Terminology $PUBLICATION_DATE.pdf"
	cp $CDISC_HOME/Current/SDTM/"CDASH Terminology.html" $CDISC_HOME/Current/SDTM/Archive/"CDASH Terminology $PUBLICATION_DATE.html"
	cp $CDISC_HOME/Current/SDTM/"CDASH Terminology.odm.xml" $CDISC_HOME/Current/SDTM/Archive/"CDASH Terminology $PUBLICATION_DATE.odm.xml"
	cd $CDISC_HOME

	#	c. CDISC Glossary Terminology (C67497)
	mv $CDISC_HOME/*Glossary*.txt $CDISC_HOME/Current/Glossary
	rm -f $CDISC_HOME/*Glossary*.xls
	cp $CDISC_HOME/ct-schema.owl $CDISC_HOME/Current/Glossary
	cp $CDISC_HOME/meta-model-schema.owl $CDISC_HOME/Current/Glossary
	cd $CDISC_HOME/Current/Glossary
	zip "CDISC Glossary.OWL.zip" "CDISC Glossary.owl" ct-schema.owl meta-model-schema.owl
	if ! [ -e "$CDISC_HOME/Current/Glossary/Archive" ]; then
		mkdir $CDISC_HOME/Current/Glossary/Archive
	fi 
	cp $CDISC_HOME/Current/Glossary/"CDISC Glossary.OWL.zip" $CDISC_HOME/Current/Glossary/Archive/"CDISC Glossary $PUBLICATION_DATE.OWL.zip"
	cp $CDISC_HOME/Current/Glossary/"Glossary Terminology.xls" $CDISC_HOME/Current/Glossary/Archive/"CDISC Glossary $PUBLICATION_DATE.xls"
	cp $CDISC_HOME/Current/Glossary/"Glossary Terminology.txt" $CDISC_HOME/Current/Glossary/Archive/"CDISC Glossary $PUBLICATION_DATE.txt"
	cp $CDISC_HOME/Current/Glossary/"Glossary Terminology.xls" $CDISC_HOME/Current/Glossary/"CDISC Glossary.xls"
	cp $CDISC_HOME/Current/Glossary/"Glossary Terminology.txt" $CDISC_HOME/Current/Glossary/"CDISC Glossary.txt"
	cp $CDISC_HOME/Current/Glossary/"CDISC Glossary.pdf" $CDISC_HOME/Current/Glossary/Archive/"CDISC Glossary $PUBLICATION_DATE.pdf"
	cp $CDISC_HOME/Current/Glossary/"CDISC Glossary.html" $CDISC_HOME/Current/Glossary/Archive/"CDISC Glossary $PUBLICATION_DATE.html"
	cp $CDISC_HOME/Current/Glossary/"CDISC Glossary.odm.xml" $CDISC_HOME/Current/Glossary/Archive/"CDISC Glossary $PUBLICATION_DATE.odm.xml"
	cd $CDISC_HOME
	
	#	d. CDISC Protocol Terminology (C132298)
	mv $CDISC_HOME/*Protocol*.txt $CDISC_HOME/Current/Protocol	
	rm -f $CDISC_HOME/*Protocol*.xls
	cp $CDISC_HOME/ct-schema.owl $CDISC_HOME/Current/Protocol
	cp $CDISC_HOME/meta-model-schema.owl $CDISC_HOME/Current/Protocol
	cd $CDISC_HOME/Current/Protocol
	zip "Protocol Terminology.OWL.zip" "Protocol Terminology.owl" ct-schema.owl meta-model-schema.owl
	if ! [ -e "$CDISC_HOME/Current/Protocol/Archive" ]; then
		mkdir $CDISC_HOME/Current/Protocol/Archive
	fi 
	cp $CDISC_HOME/Current/Protocol/"Protocol Terminology.OWL.zip" $CDISC_HOME/Current/Protocol/Archive/"Protocol Terminology $PUBLICATION_DATE.OWL.zip"
	cp $CDISC_HOME/Current/Protocol/"Protocol Terminology.xls" $CDISC_HOME/Current/Protocol/Archive/"Protocol Terminology $PUBLICATION_DATE.xls"
	cp $CDISC_HOME/Current/Protocol/"Protocol Terminology.txt" $CDISC_HOME/Current/Protocol/Archive/"Protocol Terminology $PUBLICATION_DATE.txt"
	cp $CDISC_HOME/Current/Protocol/"Protocol Terminology.pdf" $CDISC_HOME/Current/Protocol/Archive/"Protocol Terminology $PUBLICATION_DATE.pdf"
	cp $CDISC_HOME/Current/Protocol/"Protocol Terminology.html" $CDISC_HOME/Current/Protocol/Archive/"Protocol Terminology $PUBLICATION_DATE.html"
	cp $CDISC_HOME/Current/Protocol/"Protocol Terminology.odm.xml" $CDISC_HOME/Current/Protocol/Archive/"Protocol Terminology $PUBLICATION_DATE.odm.xml"
	cd $CDISC_HOME
	
	#	e. CDISC QRS Terminology (C120166)
	mv $CDISC_HOME/*QRS*.txt $CDISC_HOME/Current/QRS	
	rm -f $CDISC_HOME/*QRS*.xls
	cp $CDISC_HOME/ct-schema.owl $CDISC_HOME/Current/QRS
	cp $CDISC_HOME/meta-model-schema.owl $CDISC_HOME/Current/QRS
	cd $CDISC_HOME/Current/QRS
	zip "QRS Terminology.OWL.zip" "QRS Terminology.owl" ct-schema.owl meta-model-schema.owl
	if ! [ -e "$CDISC_HOME/Current/QRS/Archive" ]; then
		mkdir $CDISC_HOME/Current/QRS/Archive
	fi 
	cp $CDISC_HOME/Current/QRS/"QRS Terminology.OWL.zip" $CDISC_HOME/Current/QRS/Archive/"QRS Terminology $PUBLICATION_DATE.OWL.zip"
	cp $CDISC_HOME/Current/QRS/"QRS Terminology.xls" $CDISC_HOME/Current/QRS/Archive/"QRS Terminology $PUBLICATION_DATE.xls"
	cp $CDISC_HOME/Current/QRS/"QRS Terminology.txt" $CDISC_HOME/Current/QRS/Archive/"QRS Terminology $PUBLICATION_DATE.txt"
	cp $CDISC_HOME/Current/QRS/"QRS Terminology.pdf" $CDISC_HOME/Current/QRS/Archive/"QRS Terminology $PUBLICATION_DATE.pdf"
	cp $CDISC_HOME/Current/QRS/"QRS Terminology.html" $CDISC_HOME/Current/QRS/Archive/"QRS Terminology $PUBLICATION_DATE.html"
	cp $CDISC_HOME/Current/QRS/"QRS Terminology.odm.xml" $CDISC_HOME/Current/QRS/Archive/"QRS Terminology $PUBLICATION_DATE.odm.xml"
	cd $CDISC_HOME
	
	#	f. CDISC SDTM Terminology (C66830)
	mv $CDISC_HOME/*SDTM*.txt $CDISC_HOME/Current/SDTM	
	mv $CDISC_HOME/SDTM_paired*.xlsx $CDISC_HOME/Current/SDTM
	rm -f $CDISC_HOME/*SDTM*.xls
	cp $CDISC_HOME/ct-schema.owl $CDISC_HOME/Current/SDTM
	cp $CDISC_HOME/meta-model-schema.owl $CDISC_HOME/Current/SDTM
	cd $CDISC_HOME/Current/SDTM
	zip "SDTM Terminology.OWL.zip" "SDTM Terminology.owl" ct-schema.owl meta-model-schema.owl
	if ! [ -e "$CDISC_HOME/Current/SDTM/Archive" ]; then
		mkdir $CDISC_HOME/Current/SDTM/Archive
	fi 
	cp $CDISC_HOME/Current/SDTM/"SDTM Terminology.OWL.zip" $CDISC_HOME/Current/SDTM/Archive/"SDTM Terminology $PUBLICATION_DATE.OWL.zip"
	cp $CDISC_HOME/Current/SDTM/"SDTM Terminology.xls" $CDISC_HOME/Current/SDTM/Archive/"SDTM Terminology $PUBLICATION_DATE.xls"
	cp $CDISC_HOME/Current/SDTM/"SDTM Terminology.txt" $CDISC_HOME/Current/SDTM/Archive/"SDTM Terminology $PUBLICATION_DATE.txt"
	cp $CDISC_HOME/Current/SDTM/"SDTM Terminology.pdf" $CDISC_HOME/Current/SDTM/Archive/"SDTM Terminology $PUBLICATION_DATE.pdf"
	cp $CDISC_HOME/Current/SDTM/"SDTM Terminology.html" $CDISC_HOME/Current/SDTM/Archive/"SDTM Terminology $PUBLICATION_DATE.html"
	cp $CDISC_HOME/Current/SDTM/"SDTM Terminology.odm.xml" $CDISC_HOME/Current/SDTM/Archive/"SDTM Terminology $PUBLICATION_DATE.odm.xml"
	cd $CDISC_HOME
	
	#	g. CDISC SEND Terminology (C77526)
	mv $CDISC_HOME/*SEND*.txt $CDISC_HOME/Current/SEND	
	mv $CDISC_HOME/SEND_paired*.xlsx $CDISC_HOME/Current/SEND
	rm -f $CDISC_HOME/*SEND*.xls
	cp $CDISC_HOME/ct-schema.owl $CDISC_HOME/Current/SEND
	cp $CDISC_HOME/meta-model-schema.owl $CDISC_HOME/Current/SEND
	cd $CDISC_HOME/Current/SEND
	zip "SEND Terminology.OWL.zip" "SEND Terminology.owl" ct-schema.owl meta-model-schema.owl
	if ! [ -e "$CDISC_HOME/Current/SEND/Archive" ]; then
		mkdir $CDISC_HOME/Current/SEND/Archive
	fi 
	cp $CDISC_HOME/Current/SEND/"SEND Terminology.OWL.zip" $CDISC_HOME/Current/SEND/Archive/"SEND Terminology $PUBLICATION_DATE.OWL.zip"
	cp $CDISC_HOME/Current/SEND/"SEND Terminology.xls" $CDISC_HOME/Current/SEND/Archive/"SEND Terminology $PUBLICATION_DATE.xls"
	cp $CDISC_HOME/Current/SEND/"SEND Terminology.txt" $CDISC_HOME/Current/SEND/Archive/"SEND Terminology $PUBLICATION_DATE.txt"
	cp $CDISC_HOME/Current/SEND/"SEND Terminology.pdf" $CDISC_HOME/Current/SEND/Archive/"SEND Terminology $PUBLICATION_DATE.pdf"
	cp $CDISC_HOME/Current/SEND/"SEND Terminology.html" $CDISC_HOME/Current/SEND/Archive/"SEND Terminology $PUBLICATION_DATE.html"
	cp $CDISC_HOME/Current/SEND/"SEND Terminology.odm.xml" $CDISC_HOME/Current/SEND/Archive/"SEND Terminology $PUBLICATION_DATE.odm.xml"
	cd $CDISC_HOME
	
	#	h. CDISC Define-XML Terminology (C165634)
	mv $CDISC_HOME/*Define-XML*.txt $CDISC_HOME/Current/Define-XML	
	rm -f $CDISC_HOME/*Define-XML*.xls
	cp $CDISC_HOME/ct-schema.owl $CDISC_HOME/Current/Define-XML
	cp $CDISC_HOME/meta-model-schema.owl $CDISC_HOME/Current/Define-XML
	cd $CDISC_HOME/Current/Define-XML
	zip "Define-XML Terminology.OWL.zip" "Define-XML Terminology.owl" ct-schema.owl meta-model-schema.owl
	if ! [ -e "$CDISC_HOME/Current/Define-XML/Archive" ]; then
		mkdir $CDISC_HOME/Current/Define-XML/Archive
	fi 
	cp $CDISC_HOME/Current/Define-XML/"Define-XML Terminology.OWL.zip" $CDISC_HOME/Current/Define-XML/Archive/"Define-XML Terminology $PUBLICATION_DATE.OWL.zip"
	cp $CDISC_HOME/Current/Define-XML/"Define-XML Terminology.xls" $CDISC_HOME/Current/Define-XML/Archive/"Define-XML Terminology $PUBLICATION_DATE.xls"
	cp $CDISC_HOME/Current/Define-XML/"Define-XML Terminology.txt" $CDISC_HOME/Current/Define-XML/Archive/"Define-XML Terminology $PUBLICATION_DATE.txt"
	cp $CDISC_HOME/Current/Define-XML/"Define-XML Terminology.pdf" $CDISC_HOME/Current/Define-XML/Archive/"Define-XML Terminology $PUBLICATION_DATE.pdf"
	cp $CDISC_HOME/Current/Define-XML/"Define-XML Terminology.html" $CDISC_HOME/Current/Define-XML/Archive/"Define-XML Terminology $PUBLICATION_DATE.html"
	cp $CDISC_HOME/Current/Define-XML/"Define-XML Terminology.odm.xml" $CDISC_HOME/Current/Define-XML/Archive/"Define-XML Terminology $PUBLICATION_DATE.odm.xml"
	cd $CDISC_HOME
	
	rm -f $CDISC_HOME/pairedTermData_1.txt
	rm -f $CDISC_HOME/metadata_1.txt
	rm -f $CDISC_HOME/readme.txt

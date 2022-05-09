#!/bin/bash
CDISC_HOME="C:/ncievs-code/cdisc-reporting-code/"
INPUT_OWL="ThesaurusInferred_forTS.owl"
PUBLICATION_DATE="2021-10-14"
echo $CDISC_HOME
cd $CDISC_HOME

echo "Step 1. Generate CDISC txt and xls reports:"
#   a. CDISC SEND Terminology (C77526)
        java -jar cdiscreportgenerator.jar $INPUT_OWL C77526

echo "Step 2: Format .xls reports for ODM Converter processing:"
#   a. CDISC SEND Terminology (C77526)
	cp "CDISC SEND Terminology.xls" "SEND Terminology.xls"
	java -jar cdiscexcelutils.jar "SEND Terminology.xls"     

echo "Step 3. Generate CDISC pairing reports:"
#   a. CDISC SEND Terminology (C77526)
        java -jar cdiscpairing.jar $INPUT_OWL C77526

echo "Step 4. Generate CDISC diff reports:"
#	a. CDISC SEND Terminology (C77526)
		java -jar cdiscversiondiff.jar "CDISC SEND Terminology.txt" "Previous/SEND Terminology.txt" $PUBLICATION_DATE "SEND Terminology Changes.txt"
		
echo "Step 5. Run ODM Conversion:"
#   Cleanup first
	rm -f $CDISC_HOME/opts-data-conversion/ODM_Converter/in/*.xls
	rm -rf $CDISC_HOME/opts-data-conversion/ODM_Converter/build/out/*
#	a. CDISC SEND Terminology (C77526)
	cp "SEND Terminology.xls" "ops-data-conversion/ODM_Converter/in/SEND Terminology.xls"	
	cp "CDISC SEND Terminology.txt" "ops-data-conversion/ODM_Converter/in/SEND Terminology.txt"
	cd $CDISC_HOME/ops-data-conversion/ODM_Converter
	C:/apache/apache-ant-1.10.9/bin/ant > ant.log
	cp -r $CDISC_HOME/ops-data-conversion/ODM_Converter/build/out/* $CDISC_HOME/Current
	cd $CDISC_HOME/ops-data-conversion

echo "Step 6. Move files and clean-up:"		
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
	
rm -f $CDISC_HOME/pairedTermData_1.txt
rm -f $CDISC_HOME/metadata_1.txt
rm -f $CDISC_HOME/readme.txt
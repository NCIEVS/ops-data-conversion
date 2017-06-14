#!/bin/bash
# Script to grab NCIt from anonymous ftp server, process it according to NCBO and repload for NCBO to use

# make sure the task scheduler runs from the right Dir
cd '/d/evs-projects/ops/NCBO'

# VARIABLES AND INITIALIZATION
downloadURL='ftp://ftp1.nci.nih.gov/pub/cacore/EVS/NCI_Thesaurus/'
indexFile='NCIT_Index.html'
vocabFile='vocab-list.txt'
uploadHost=ncicbftp2.nci.nih.gov
uploadDIR='cacore/EVS/upload'
user1='enter-username-here'
password1='enter-password-here'
notifyuser='enter-email-here-if-interested'
mDate="`echo $(date +%Y%m%d)`"
uploadName='NCITNCBO.owl'

if [ ! -e $vocabFile ]
then
	echo "Vocabulary tracking file needs to be created."
	echo "Execute 'create-tracking-file.sh' and follow directions there."
	echo "Script exiting.  Please rerun when tracking file is available."
	exit 0
fi

# START SCRIPT PROPER
echo "SCRIPT RUN ON $mDate"


# CHECK THE DOWNLOAD SITE FOR UPDATES
# Get the listing of files in the NCIt directory
echo "CHECKING FOR NEW FILES"
if [ -e $indexFile ]
then 
	rm $indexFile
fi
wget -O $indexFile -o ncit-index-$mDate.log $downloadURL


# EXTRACT THE URL FOR THE VOCABULARY BY_CODE FROM THE INDEX.HTML
# if there are multiple vocabs listed in the ftp site, only a *new* vocab is returned & acted on
vocabURL=`grep "OWL.zip\">Thesaurus_" $indexFile | grep -v --file=$vocabFile | awk -F 'href=\"|\">' '{print $2}' `

# HAS THE VOCAB/URL ALREADY BEEN DOWNLOADED? IF YES THEN EXIT
if [ -z $vocabURL ]
then 
	echo "No new Files to process, exiting..."
	exit 0
fi


echo "FILE $vocabURL FOUND AND WILL BE PROCESSED"
echo "RETRIEVING FILE"
vocabName=`echo $vocabURL | awk 'BEGIN { FS = "/" } ; { print $NF }' ` 
wget -O $vocabName -o ncit-download-$mDate.log $vocabURL

# UPDATE VOCABULARY NAME LIST
echo $vocabName >> $vocabFile

echo "PROCESSING FILE"

echo "Unzipping $vocabName"
gunzip -S .zip $vocabName
rootName=`echo $vocabName | sed -e 's/.zip//'`

echo "Reformatting complex properties"
"$JAVA_HOME"/bin/java -jar ./ncbo_ncit.jar $PWD/$rootName $PWD/ComplexProperties_byCode.txt
mv $rootName-NCBO.owl $uploadName

#   mail -v -s "NCI Thesaurus downloaded" $notifyuser


echo "UPLOADING FILE"
mFtp=""
if [ $OSTYPE = 'msys' ]
then
mFtp='/c/Windows/System32/ftp'
else
mFtp='ftp'
fi
$mFtp -n $uploadHost <<ENDSCRIPT1
quote USER $user1
quote PASS $password1
cd $uploadDIR
put $uploadName
quit
ENDSCRIPT1

# CLEAN THE URL FILE MONTHLY AFTER THE UPLOAD, grep HAS EITHER A NUMBER 
# LIMIT OR A BUG IN -v WHEN USED WITH PATTERNS FROM -f

echo "Cleaning URL list file"
a=( $( cat $vocabFile ) )
rm $vocabFile
for (( i=0; i<=$((${#a[@]} - 1)); i++ ))
do
  grep -q ${a[$i]} $indexFile 
  if [ $? -eq 0 ]
  then 
  	echo "${a[$i]}" >> $vocabFile
  fi
done


echo "SCRIPT COMPLETED"
exit 0

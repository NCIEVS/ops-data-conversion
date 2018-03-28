
#To format the ncitconcept_history file, run the formatting program
./formatHistory.sh /path/to/ncitconcept_history /path/to/output.txt

#In order to generate the monthly history, grep for the latest month.

#Example:
cat cumulative_history.txt | grep May-17 > monthly_history.txt

#To do the subset history, grep out the modify and the buggy preretire

Example: 
grep -vwE "(modify|MODIFY|preretire)" cumulative_history.txt > subset_cumulative_history.txt
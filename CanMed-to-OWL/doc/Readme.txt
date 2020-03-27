This takes two csv files from SEER CanMED and turns them into a single OWL file for load into the terminology server

File 1 NDC : Download from https://seer.cancer.gov/oncologytoolbox/canmed/ndconc/?&_export
File 2 HCPCS : Download from https://seer.cancer.gov/oncologytoolbox/canmed/hcpcs/?&_export

Run the jar passing in 4 parameters
1. Location of saved NDC csv file
2. Location of saved HCPCS csv file
3. Location to store new OWL file
4. Version number to assign the OWL file Ex: March2020

Example run
java -jar CanMed-to-OWL-1.0-jar-with-dependencies.jar -n <ndc.csv location url> -h <hcpcs.csv location url> -t <owl output location url> -v <version>


java -jar CanMed-to-OWL-1.0-jar-with-dependencies.jar -n file:///path/ndconc_results.csv -h file:///path/hcpcs_results.csv -t file:///path/canmed.owl -v MonthYY


package gov.nih.nci.evs.ndfrt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

// https://wiki.nci.nih.gov/display/EVS/NDFRT+Processing

/**
 * The Class NDFRT_File_Process.
 */
public class NDFRT_File_Process {

	/** The username to the private FTP site. */
	private String private_username = "";

	/** The password to the private FTP site. */
	private String private_password = "";

	/** The username to the public FTP site. */
	private String public_username = "";

	/** The public to the private FTP site. */
	private String public_password = "";

	/** The path to the NDFRT files to be processed. */
	private String filePath = "";

	/** The ftp site. */
	private String ftpSite = "ncicbftp2.nci.nih.gov";

	/** The public directory. */
	private String publicDirectory = "/cacore/EVS/NDF-RT";

	/** The public archive directory. */
	private String publicArchiveDirectory = "/cacore/EVS/NDF-RT/Archive";

	/** The fda directory. */
	private String fdaDirectory = "/cacore/EVS/FDA/ndfrt";

	/** The fda archive directory. */
	private String fdaArchiveDirectory = "/cacore/EVS/FDA/ndfrt/Archive";

	/** The date formatted with dots between sections. */
	private String dateDot = "";

	/** The date formatted with dashes between sections. */
	private String dateDash = "";

	/**
	 * Whether to actually upload to the FTP site. If debug=true, then don't
	 * upload.
	 */
	private boolean debug = true;

	/**
	 * Instantiates a new NDFRT_File_Process. Pass in properties file with FTP
	 * usernames & passwords
	 * 
	 * @param access_config
	 *            the access_config
	 */
	public NDFRT_File_Process(String access_config) {
		// Example
		// C:/Users/user/NDFRT_File_Processing/conf/access.properties
		Properties props = new Properties();
		try {
			FileInputStream instream = new FileInputStream(access_config);
			props.load(instream);
			instream.close();
		} catch (FileNotFoundException e) {
			System.out.println("No " + access_config + " found");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Problem reading " + access_config);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Unexpected error reading " + access_config);
			System.exit(0);
		}

		this.private_username = props.getProperty("private_username");
		this.private_password = props.getProperty("private_password");
		this.public_username = props.getProperty("public_username");
		this.public_password = props.getProperty("public_password");
		this.filePath = props.getProperty("filepath");
		if (listFiles(this.filePath)) {
			getDate();
			processForOWL();
			process77();
			processInferred();
			processTDE();
			processSPL();
		} else {
			System.exit(0);
		}
	}

	/**
	 * Gets the date from the fileName of the SPL file. This file chosen because
	 * it has the date isolated between underscores
	 * 
	 * @return the date
	 */
	private void getDate() {
		File directory = new File(this.filePath);
		File[] contents = directory.listFiles();
		for (int i = 0; i < contents.length; i++) {
			String fileName = contents[i].getName();
			// upload the byName file to the private ftp
			if (fileName.contains("SPL")) {
				// upload
				String date = fileName.substring(fileName.indexOf("_2") + 1,
						fileName.indexOf("_SPL"));
				dateDot = date;
				dateDash = date.replace(".", "-");
			}
		}

	}

	/**
	 * The main method.
	 * 
	 * @param Pass
	 *            in the location of the access.properties
	 */
	public static void main(String[] args) {
		// Assuming, for now, that all the relevant files have been downloaded
		// from Apelon and placed in a central folder

		// Files of interest
		// SPL ProjectDownloads
		// TDE=NDFRT_Public_YYYY.MM.DD_TDE.zip
		// byName=NDFRT_Public_YYYY.MM.DD_TDE_ByName.zip
		// inferred=NDFRT_Public_YYYY.MM.DD_TDE_inferred.zip
		// SPL=NDFRT_YYYY.MM.DD_SPL.zip
		// NDFRT Downloads
		// full=77_full_YYYY.MM.DD.13AA_bin.zip
		// diff=77_diff_YYYY.MM.DD.13AA_bin.zip

		if (args.length < 1) {
			System.out.println("Must input config file location");
			System.exit(0);
		}

		NDFRT_File_Process nfp = new NDFRT_File_Process(args[0]);

	}

	/**
	 * Upload the byName file for Alameda to use in OWL processing.
	 */
	private void processForOWL() {
		// copy byName to the private ftp at /evs/upload/NDFRT
		// Notify Alameda and Nels that it is available
		// NOTE: This up loads to the private FTP, as opposed to all the other
		// files which
		// get loaded to the public FTP. That is why it does not go through the
		// cleanFTP and uploadToPublicFTP methods.
		FTPClient client = new FTPClient();
		try {

			client.connect(this.ftpSite);

			client.login(this.private_username, this.private_password);
			client.changeWorkingDirectory("/evs/upload/NDFRT");
			client.setBufferSize(1024 * 1024);
			client.enterLocalPassiveMode();
			client.setFileType(FTP.BINARY_FILE_TYPE);
			//
			// // clean out the old files
			FTPFile[] ftp = client.listFiles();
			int reply = client.getReplyCode();
			if (FTPReply.isPositiveCompletion(reply)) {
				for (int i = 0; i < ftp.length; i++) {
					if (ftp[i].getName().contains("ByName")) {

						System.out.println("Deleting " + ftp[i].getName());
						if (!debug) {
							client.deleteFile(ftp[i].getName());
						}
					}
				}
			} else {
				// client.disconnect();
				System.out
						.println("FTP server failed to list existing files. Deletion of upload file for OWL not completed");

			}

			// upload the new file
			File directory = new File(this.filePath);
			File[] contents = directory.listFiles();
			for (int i = 0; i < contents.length; i++) {
				String fileName = contents[i].getName();
				// upload the byName file to the private ftp
				if (fileName.contains("ByName")) {
					// upload
					String uploadName = this.filePath + "/" + fileName;
					System.out.println("Uploading " + uploadName);

					FileInputStream fis = new FileInputStream(uploadName);
					if (!debug) {
						client.storeUniqueFile(fileName, fis);
					}
					// add a copy to the archives
					client.changeWorkingDirectory("/evs/upload/NDFRT/Archive");
					if (!debug) {
						client.storeUniqueFile(fileName, fis);
					}
					fis.close();
				}
			}

			client.disconnect();
			System.out.println("Upload for OWL Processing complete.");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Upload the raw TDE file to public FTP.
	 */
	private void uploadRawTDE() {
		// rename TDE to NDFRT_Public_All.zip.
		// upload to public FTP at /cacore/EVS/NDF-RT
		// create a copy and name "NDFRT_Public_All YYYY-MM-DD.zip
		// upload to public FTP at /cacore/EVS/NDF-RT/archive
		// FTPClient client = new FTPClient();
		String newFileName = "NDFRT_Public_All";
		try {
			// clean out the old files
			cleanFTP("Public_All", this.publicDirectory);

			// upload the new file
			File directory = new File(this.filePath);
			File[] contents = directory.listFiles();
			for (int i = 0; i < contents.length; i++) {
				String fileName = contents[i].getName();
				// upload the byName file to the private ftp
				if (fileName.contains("_TDE.zip")) {
					// upload
					String uploadName = this.filePath + "/" + fileName;
					String ftpTargetName = newFileName + ".zip";
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicDirectory);

					// upload a dated copy to archive
					ftpTargetName = newFileName + " " + dateDash + ".zip";
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicArchiveDirectory);
				}
			}

			// client.disconnect();
			System.out.println("Upload for Raw TDE complete.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Process the TDE file. Unzip and extract Release Notes. Process and upload
	 * Unzip and extract XML. Process and upload Unzip and extract NUI. Process
	 * and upload
	 * 
	 */
	private void processTDE() {
		// upload the unaltered zip file
		uploadRawTDE();

		// Unzip the TDE file
		String unzipFilePath = this.filePath + "/TDE_unzip";
		File directory = new File(this.filePath);
		File[] contents = directory.listFiles();
		for (int i = 0; i < contents.length; i++) {
			String fileName = contents[i].getName();
			// upload the byName file to the private ftp

			if (fileName.contains("_TDE.zip")) {
				this.unzipIt(filePath + "/" + fileName, unzipFilePath);
			}
		}

		// Extract NDFRT_Public_Edition_Release_Notes_YYYY.MM.DD.txt
		// create a copy and name "NDF-RT Public Edition Release Notes.txt"
		try {
			String newFileName = "NDF-RT Public Edition Release Notes";

			// clean out the old files
			cleanFTP("Public Edition Release Notes", this.publicDirectory);
			cleanFTP("Public Edition Release Notes", this.fdaDirectory);

			// upload the new file
			directory = new File(unzipFilePath);
			contents = directory.listFiles();
			for (int i = 0; i < contents.length; i++) {
				String fileName = contents[i].getName();
				// upload the byName file to the private ftp
				if (fileName.contains("Public_Edition_Release_Notes")) {
					// upload
					String uploadName = unzipFilePath + "/" + fileName;
					String ftpTargetName = newFileName + ".txt";

					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicDirectory);
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.fdaDirectory);

					// create a copy and name it
					// "NDF-RT Public Edition Release Notes YYYY-MM-DD.txt"
					// upload to public FTP at /cacore/EVS/FDA/ndfrt
					// upload another copy to /cacore/EVS/FDA/ndfrt/archive

					// Prepare to upload dated file
					ftpTargetName = newFileName + " " + dateDash + ".txt";
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicDirectory);
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.fdaDirectory);

					// add a copy to the archives
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicArchiveDirectory);
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.fdaArchiveDirectory);
				}
			}
			// client.disconnect();
			System.out.println("Upload for Release Notes complete.");
		} catch (Exception e) {
			System.out.println("Error uploading Release Notes");
			e.printStackTrace();
		}

		// The unzip will create a directory NDFRT_Public_YYYY.MM.DD
		//
		String subDirectory = "NDFRT_Public_" + dateDot;
		String unzipSubDirectory = unzipFilePath + "/" + subDirectory;
		// Extract NDFRT_Public_YYYY.MM.DD.xml from zip file
		// zip NDFRT_Public_YYYY.MM.DD.xml into NDF-RT_XML.zip
		// upload zip to public FTP at /cacore/EVS/NDF-RT

		try {
			String xmlFile = "NDFRT_Public_" + dateDot + "_TDE.xml";
			String newFileName = "NDF-RT_XML.zip";
			zipIt(unzipSubDirectory + "/" + xmlFile, unzipSubDirectory + "/"
					+ newFileName);

			// clean out the old files
			cleanFTP("NDF-RT_XML.zip", this.publicDirectory);

			// upload the new file
			directory = new File(unzipSubDirectory);
			contents = directory.listFiles();
			for (int i = 0; i < contents.length; i++) {
				String fileName = contents[i].getName();
				// upload the byName file to the private ftp
				if (fileName.contains("NDF-RT_XML.zip")) {
					// upload
					String uploadName = unzipSubDirectory + "/" + fileName;
					String ftpTargetName = newFileName;

					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicDirectory);

					// create a copy and name it "NDF-RT_XML YYYY-MM-DD.zip"
					// upload zip to public FTP at /cacore/EVS/NDF-RT/archive
					// delete previous month's zip file from public FTP at
					// /cacore/EVS/NDF-RT

					// add a copy to the archives
					ftpTargetName = newFileName + " " + dateDash + ".zip";
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicArchiveDirectory);

				}
			}
			// client.disconnect();
			System.out.println("Upload for XML file complete.");

		} catch (Exception e) {
			System.out.println("Error uploading XML file");
			e.printStackTrace();
		}
		System.out.println("Uploading NDFRT_RT_XML.zip complete");

		// Extract NDFRT_Public_YYYY.MM.DD_NUI.txt
		String nuiFile = "NDFRT_Public_" + dateDot + "_NUI.txt";
		String newNUIName = "NDF-RT.txt";
		processNUI(new File(this.filePath + "/TDE_Unzip/NDFRT_Public_"
				+ this.dateDot), nuiFile, newNUIName);

		// upload to public FTP at /cacore/EVS/NDF-RT
		cleanFTP(newNUIName, this.publicDirectory);

		String uploadName = unzipFilePath + "/NDFRT_Public_" + this.dateDot
				+ "/" + newNUIName;
		uploadToPublicFTP(uploadName, newNUIName, this.publicDirectory);
		uploadToPublicFTP(uploadName,
				newNUIName.replace(".txt", " " + this.dateDash + ".txt"),
				this.publicArchiveDirectory);
		// create a copy and name it "NDF-RT YYYY-MM-DD.txt"
		// upload to public FTP at /cacore/EVS/NDF-RT/Archive

	}

	/**
	 * Process nui.
	 * 
	 * @param dir
	 *            the location of the NUI file
	 * @param file
	 *            the name of the NUI file
	 * @param outputFile
	 *            the name to use for the output file
	 */
	private void processNUI(File dir, String file, String outputFile) {
		// check that each line has 2 fields using
		// "gawk -F"\t" 'NF != 2' *_NUI.txt"
		// Sort the lines and rearrange using
		// "sort  -f  *_NUI.txt  | gawk  -F"\t" 'BEGIN { OFS = "\t" } { print $2, $1 }' > NDF-RT.txt"
		// convert " [" to "/t["

		FileReader inputFile = null;
		BufferedReader buff = null;
		TreeMap<String, String> map = new TreeMap<String, String>(
				String.CASE_INSENSITIVE_ORDER);
		try {
			inputFile = new FileReader(dir.getPath() + "/" + file);
			buff = new BufferedReader(inputFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else {
					try {
						String[] tokens = line.split("\t");
						map.put(tokens[0], tokens[1]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				inputFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		try {
			String nuiFile = new String(dir.getPath() + "/" + outputFile);
			PrintWriter pw = new PrintWriter(nuiFile);
			for (String key : map.navigableKeySet()) {
				pw.println(map.get(key) + "\t" + key.replace(" [", "\t["));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Clean ftp.
	 * 
	 * @param fileName
	 *            the file to be deleted
	 * @param directory
	 *            the FTP directory to delete the files from
	 */
	private void cleanFTP(String fileName, String directory) {
		try {
			FTPClient client = new FTPClient();
			client.connect(this.ftpSite);
			client.login(this.public_username, this.public_password);
			client.changeWorkingDirectory(directory);
			client.setBufferSize(1024 * 1024);
			client.enterLocalPassiveMode();

			// delete files containing the fileName from the appropriate
			// directory
			FTPFile[] ftp = client.listFiles();
			int reply = client.getReplyCode();
			if (FTPReply.isPositiveCompletion(reply)) {
				for (int i = 0; i < ftp.length; i++) {
					if (ftp[i].getName().contains(fileName)) {
						System.out.println("Deleting " + ftp[i].getName());
						if (!debug) {
							client.deleteFile(ftp[i].getName());
						}
					}
				}
			} else {
				System.out
						.println("FTP server failed to list existing files. Deletion of "
								+ fileName + " not completed");
			}
			client.disconnect();
		} catch (Exception e) {
			System.out.println("Error deleting file " + fileName);
			e.printStackTrace();
		}
	}

	/**
	 * Upload to public ftp.
	 * 
	 * @param uploadName
	 *            the name of the file to be uploaded
	 * @param ftpTargetName
	 *            the name to be assigned to the file on the FTP site
	 * @param directory
	 *            the FTP directory for upload
	 */
	private void uploadToPublicFTP(String uploadName, String ftpTargetName,
			String directory) {
		FTPClient client = new FTPClient();
		try {
			client.connect(this.ftpSite);
			client.login(this.public_username, this.public_password);
			client.changeWorkingDirectory(directory);
			client.setBufferSize(1024 * 1024);
			client.enterLocalPassiveMode();
			client.setFileType(FTP.BINARY_FILE_TYPE);
			System.out.println("Uploading " + uploadName + " to " + directory
					+ "/" + ftpTargetName);
			FileInputStream fis = new FileInputStream(uploadName);
			if (!debug) {
				client.storeUniqueFile(ftpTargetName, fis);
			}
		} catch (Exception e) {
			System.out.println("Error uploading file " + uploadName);
			e.printStackTrace();
		}
	}

	/**
	 * Process and upload the inferred TDE file.
	 */
	private void processInferred() {
		// Process NDFRT_Public_YYYY.MM.DD_TDE_inferred.zip
		String newFileName = "NDF-RT_XML_Inferred";

		try {

			// clean out the old files
			cleanFTP("NDF-RT_XML_Inferred", this.publicDirectory);

			// Get list of files from directory and pull out the one marked
			// TDE_Inferred
			File directory = new File(this.filePath);
			File[] contents = directory.listFiles();
			for (int i = 0; i < contents.length; i++) {
				String fileName = contents[i].getName();
				// upload the byName file to the private ftp
				if (fileName.contains("TDE_inferred")) {
					// upload
					String uploadName = this.filePath + "/" + fileName;
					String ftpTargetName = newFileName + ".zip";

					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicDirectory);

					// add a copy to the archives
					ftpTargetName = newFileName + " " + dateDash + ".zip";
					uploadToPublicFTP(uploadName, ftpTargetName,
							this.publicArchiveDirectory);

				}
			}

			System.out.println("Upload for TDE_Inferred complete.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Process and upload the 77_diff and 77_full.
	 */
	private void process77() {
		// delete previous month's 77 files from public FTP at
		// /cacore/EVS/NDF-RT
		// FTPClient client = new FTPClient();
		try {
			// clean out the old files
			cleanFTP("77_diff", this.publicDirectory);
			cleanFTP("77_full", this.publicDirectory);

			// upload the new file
			File directory = new File(this.filePath);
			File[] contents = directory.listFiles();
			for (int i = 0; i < contents.length; i++) {
				String fileName = contents[i].getName();
				// upload the byName file to the private ftp
				if (fileName.startsWith("77_")) {
					// upload
					String uploadName = this.filePath + "/" + fileName;
					uploadToPublicFTP(uploadName, fileName,
							this.publicDirectory);

					// add a copy to the archives
					uploadToPublicFTP(uploadName, fileName,
							this.publicArchiveDirectory);

				}
			}

			System.out.println("Upload for 77 complete.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Process the SPL zip. Extract, process and upload the 3 files:
	 * 
	 * CS to file "StructuralClass.txt" and .xls moa to file
	 * "MechanismOfAction.txt" and .xls pe to file "PhysiologicEffect.txt" and
	 * .xls
	 */
	private void processSPL() {
		// Unzip the SPL file
		// Extract the following 3 files from SPL
		// cs = NDFRT_YYYY.MM.DD_cs_nui.txt
		// moa = NDFRT_YYYY.MM.DD_moa_nui.txt
		// pe = NDFRT_YYYY.MM.DD_pe.nui.txt
		// Write each of these to a new file as follows
		// Header "NDF-RT Code\tNDF-RT Name"
		// write each line, converting "|" to "\t"
		// Name the CS file "StructuralClass.txt"
		// Name the moa file "MechanismOfAction.txt"
		// Name the pe file "PhysiologicEffect.txt"
		// upload all three text files to public ftp at /cacore/EVS/FDA/ndfrt
		// Create copies of each file and change the suffix to a date
		// "* YYYY-MM-DD.txt"
		// upload all three dated files to public ftp at
		// /cacore/EVS/FDA/ndfrt/archive
		// Create an Excel spreadsheet for each file with a frozen, pale blue
		// header line
		// upload all three excel files to public ftp at /cacore/EVS/FDA/ndfrt
		// Create copies of each excel and change the suffix to a date
		// "* YYYY-MM-DD.xls"
		// upload all three dated excel files to public ftp at
		// /cacore/EVS/FDA/ndfrt/archive

		String unzipFilePath = this.filePath;
		File directory = new File(this.filePath);
		File[] contents = directory.listFiles();
		File splDirectory;
		File[] splContents;
		boolean successful = false;
		for (int i = 0; i < contents.length; i++) {
			String fileName = contents[i].getName();

			if (fileName.contains("_SPL.zip")) {
				this.unzipIt(filePath + "/" + fileName, unzipFilePath);
				splDirectory = new File(this.filePath + "/"
						+ fileName.replace(".zip", ""));
				splContents = splDirectory.listFiles();
				for (int j = 0; j < splContents.length; j++) {
					String splOutputFileName;
					String splFile = splContents[j].getName();
					if (splFile.endsWith("cs_nui.txt")) {
						splOutputFileName = "StructuralClass";
						generateSPL(splDirectory, splFile, splOutputFileName);
					} else if (splFile.endsWith("pe_nui.txt")) {
						splOutputFileName = "PhysiologicEffect";
						generateSPL(splDirectory, splFile, splOutputFileName);
					} else if (splFile.endsWith("moa_nui.txt")) {
						splOutputFileName = "MechanismOfAction";
						generateSPL(splDirectory, splFile, splOutputFileName);
					}
				}

				cleanFTP("MechanismOfAction", this.fdaDirectory);
				cleanFTP("PhysiologicEffect", this.fdaDirectory);
				cleanFTP("StructuralClass", this.fdaDirectory);

				splContents = splDirectory.listFiles();
				for (int j = 0; j < splContents.length; j++) {
					String splPath = splContents[j].getPath();
					String splFile = splContents[j].getName();
					if (!splFile.contains("nui.txt")) {
						String extension = splFile.substring(splFile
								.indexOf("."));
						String filename = splFile.substring(0,
								splFile.indexOf("."));
						String archiveFile = filename + " " + this.dateDash
								+ extension;
						uploadToPublicFTP(splPath, splFile, this.fdaDirectory);
						uploadToPublicFTP(splPath, archiveFile,
								this.fdaArchiveDirectory);
					}
				}

				successful = true;
			}
		}
		if (!successful) {
			System.out.println("Could not find the SPL zip file.");
		}
	}

	/**
	 * Takes the SPL file and replaces pipe delimiter with tab. Adds header
	 * 
	 * @param dir
	 *            the directory where the SPL file is located
	 * @param file
	 *            the name of the SPL file
	 * @param outputFile
	 *            the name of the output file
	 */
	public void generateSPL(File dir, String file, String outputFile) {
		FileReader inputFile = null;
		BufferedReader buff = null;
		TreeMap<String, String> map = new TreeMap<String, String>(
				String.CASE_INSENSITIVE_ORDER);
		try {
			inputFile = new FileReader(dir.getPath() + "/" + file);
			buff = new BufferedReader(inputFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else {
					try {
						String[] tokens = line.split("\\|");
						map.put(tokens[1], tokens[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				inputFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		try {
			String splFile = new String(dir.getPath() + "/" + outputFile
					+ ".txt");
			PrintWriter pw = new PrintWriter(splFile);
			pw.println("NDF-RT Code\tNDF-RT Name");
			for (String key : map.navigableKeySet()) {
				pw.println(map.get(key) + "\t" + key);
			}
			pw.close();
			generateSPLExcel(map, splFile.replace(".txt", ".xls"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Generate excel from SPL file.
	 * 
	 * @param map
	 *            the hashmap of the SPL file
	 * @param outputFile
	 *            the name of the output excel file
	 */
	public void generateSPLExcel(TreeMap<String, String> map, String outputFile) {
		try {
			File file = new File(outputFile);
			FileOutputStream out = new FileOutputStream(file);
			HSSFWorkbook wb = new HSSFWorkbook();
			String sheetName = outputFile.substring(
					outputFile.indexOf("/") + 1, outputFile.indexOf(".xls"));
			Sheet sheet = wb.createSheet(sheetName);
			Row r = null;
			Cell c = null;
			HSSFCellStyle cs = wb.createCellStyle();
			HSSFCellStyle cs2 = wb.createCellStyle();
			Font f = wb.createFont();
			Font f2 = wb.createFont();
			f.setFontHeightInPoints((short) 10);
			f2.setFontHeightInPoints((short) 10);
			f2.setBoldweight(Font.BOLDWEIGHT_BOLD);
			cs.setFont(f);
			cs2.setFont(f2);
			cs2.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
			cs2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			int rownum = 0;
			r = sheet.createRow(rownum);
			c = r.createCell(0);
			c.setCellValue("NDF-RT Code");
			c.setCellStyle(cs2);
			c = r.createCell(1);
			c.setCellValue("NDF-RT Name");
			c.setCellStyle(cs2);
			sheet.createFreezePane(0, 1, 0, 1);

			for (String key : map.navigableKeySet()) {
				rownum++;
				int cellnum = 0;
				r = sheet.createRow(rownum);
				c = r.createCell(cellnum);
				c.setCellValue(map.get(key));
				c.setCellStyle(cs);
				c = r.createCell(++cellnum);
				c.setCellValue(key);
				c.setCellStyle(cs);
			}
			sheet.autoSizeColumn(0);
			sheet.autoSizeColumn(1);

			wb.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in FileOutputStream");
		}
	}

	/**
	 * Compare the number of files in the given folder to the expected input.
	 * 
	 * @param folder, the path of the input folder
	 * @return true, if number of files in folder matches the expected list
	 */
	private boolean listFiles(String folder) {
		File directory = new File(folder);
		File[] contents = directory.listFiles();
		if (contents.length == 6) {
			return true;
		} else if (contents.length > 6) {
			System.out
					.println("Too many files in directory.  Should only be 6 files");
		} else {
			System.out
					.println("Not enough files in directory.  Should be 6 files");
		}
		System.out.println("NDFRT_Public_YYYY.MM.DD_TDE.zip");
		System.out.println("NDFRT_Public_YYYY.MM.DD_TDE_ByName.zip");
		System.out.println("NDFRT_Public_YYYY.MM.DD_TDE_inferred.zip");
		System.out.println("NDFRT_YYYY.MM.DD_SPL.zip");
		System.out.println("77_full_YYYY.MM.DD.13AA_bin.zip");
		System.out.println("77_diff_YYYY.MM.DD.13AA_bin.zip");
		return false;
	}

	/**
	 * Unzip a given file to the specified location.
	 * 
	 * @param zipFile
	 *            the zip file
	 * @param outputFolder
	 *            the output folder
	 */
	public void unzipIt(String zipFile, String outputFolder) {
		System.out.println("Unzipping " + zipFile);
		try {
			int BUFFER = 2048;
			File file = new File(zipFile);

			ZipFile zip = new ZipFile(file);
			String newPath = outputFolder;

			new File(newPath).mkdir();
			Enumeration zipFileEntries = zip.entries();

			// Process each entry
			while (zipFileEntries.hasMoreElements()) {
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();

				File destFile = new File(newPath, currentEntry);

				File destinationParent = destFile.getParentFile();

				// create the parent directory structure if needed
				destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					BufferedInputStream is = new BufferedInputStream(
							zip.getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte data[] = new byte[BUFFER];

					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos,
							BUFFER);

					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}

			}
			System.out.println("Unzip complete");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Zip it.
	 * 
	 * @param inputFile
	 *            the input file
	 * @param zipFile
	 *            the zip file
	 * @throws Exception
	 *             the exception
	 */
	public void zipIt(String inputFile, String zipFile) throws Exception {
		byte[] buffer = new byte[1024];
		System.out.println("Zipping " + inputFile);
		FileOutputStream inputFileOS = new FileOutputStream(zipFile);
		ZipOutputStream zipFileOS = new ZipOutputStream(inputFileOS);

		FileInputStream inFileReader = new FileInputStream(inputFile);
		zipFileOS.putNextEntry(new ZipEntry(inputFile));

		int length;

		while ((length = inFileReader.read(buffer)) > 0) {
			zipFileOS.write(buffer, 0, length);
		}

		zipFileOS.closeEntry();
		inFileReader.close();
		zipFileOS.close();
		System.out.println("File " + zipFile + " created");

	}
}

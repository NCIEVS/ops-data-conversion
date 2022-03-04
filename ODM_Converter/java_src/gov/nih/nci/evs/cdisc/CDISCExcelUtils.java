package gov.nih.nci.evs.cdisc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.text.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.hpsf.SummaryInformation;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

 /**
  * @author EVS Team
  * @version 1.0
  *
  *          Modification history Initial implementation kim.ong@nih.gov
  *
 */

public class CDISCExcelUtils {
	static String APPLICATION_NAME = "Application Name";
	static String AUTHOR = "Author";
	static String CHARACTER_COUNT = "Character Count";
	static String COMMENTS = "Comments";
	static String CREATE_DATE_TIME = "Create Date Time"; //java.util.Date createDateTime
	static String EDIT_TIME = "Edit Time"; //long time
	static String KEYWORDS = "Keywords";
	static String LAST_AUTHOR = "Last Author";
	static String LAST_PRINTED = "Last Printed"; //java.util.Date lastPrinted
	static String LAST_SAVE_DATE_TIME  = "Last Save Date Time"; //java.util.Date time
	static String PAGE_COUNT  = "Page Count"; //int pageCount
	static String REV_NUMBER  = "Rev Number"; //java.lang.String revNumber
	static String SECURITY  = "Security"; //int security
	static String SUBJECT  = "Subject";
	static String TEMPLATE  = "Template";
	static String THUMBNAIL  = "Thumnail"; //byte[] thumbnail
	static String TITLE  = "Title";
	static String WORD_COUNT  = "Word Count";

    private static String SUMMARY_DATA_AUTHOR = "SUMMARY_DATA_AUTHOR";
    private static String SUMMARY_DATA_KEYWORDS = "SUMMARY_DATA_KEYWORDS";
    private static String SUMMARY_DATA_TITLE = "SUMMARY_DATA_TITLE";
    private static String SUMMARY_DATA_SUBJECT = "SUMMARY_DATA_SUBJECT";

    private HSSFWorkbook workbook = null;
    private String timestamp = getToday();

	public CDISCExcelUtils(String filename) {
		HSSFSheet sheet = null;
		HSSFRow row = null;
		HSSFCell cell = null;
		this.workbook = loadWorkbook(filename);
	}

	public CDISCExcelUtils(String filename, String timestamp) {
		HSSFSheet sheet = null;
		HSSFRow row = null;
		HSSFCell cell = null;
		this.workbook = loadWorkbook(filename);
		this.timestamp = timestamp;
	}


	public String getToday() {
		return getToday("yyyy-MM-dd");
	}

	public String getToday(String format) {
		java.util.Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

    public int getNumberOfRows(HSSFSheet sheet) {
		int rowTotal = sheet.getLastRowNum();
		if ((rowTotal > 0) || (sheet.getPhysicalNumberOfRows() > 0)) {
			rowTotal++;
		}
		return rowTotal;
	}

    public int getNumberOfRows(Sheet sheet) {
		int rowTotal = sheet.getLastRowNum();
		if ((rowTotal > 0) || (sheet.getPhysicalNumberOfRows() > 0)) {
			rowTotal++;
		}
		return rowTotal;
	}

    public int getNumberOfColumns(HSSFSheet sheet) {
		int numberOfCells = 0;
		try {
			Iterator rowIterator = sheet.rowIterator();
			/**
			* Escape the header row *
			*/
			if (rowIterator.hasNext()) {
				Row headerRow = (Row) rowIterator.next();
				//get the number of cells in the header row
				numberOfCells = headerRow.getPhysicalNumberOfCells();
			}
		} catch (Exception ex) {
            ex.printStackTrace();
		}
		return numberOfCells;
	}

	public void setAutoFilter(HSSFSheet sheet, char lastColumnChar) {
		int numRows = getNumberOfRows(sheet);
		sheet.setAutoFilter(CellRangeAddress.valueOf("A1:" + lastColumnChar + numRows));
	}

	public void setAutoFilter(Sheet sheet, char lastColumnChar) {
		int numRows = getNumberOfRows(sheet);
		sheet.setAutoFilter(CellRangeAddress.valueOf("A1:" + lastColumnChar + numRows));
	}

/*
	private static String getCellData(Cell cell) {
		String value = null;
		if (cell == null) {
			return null;
		}
		switch (cell.getCellType()) {
			case HSSFCell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			case HSSFCell.CELL_TYPE_FORMULA:
				value = cell.getCellFormula();
				break;
			case HSSFCell.CELL_TYPE_NUMERIC:
				HSSFDataFormatter dataFormatter = new HSSFDataFormatter();
				value = dataFormatter.formatCellValue(cell);
				break;
			case HSSFCell.CELL_TYPE_BLANK:
				value = null;
				break;
			case HSSFCell.CELL_TYPE_ERROR:
				value = "#ERROR#";
				break;
		}
		return value;
	}
*/
    private static String getCellData(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case BOOLEAN:
                System.out.print(cell.getBooleanCellValue());
                Boolean bool_obj = cell.getBooleanCellValue();
                boolean bool = Boolean.valueOf(bool_obj);
                return "" + bool;

            case STRING:
                return (cell.getRichStringCellValue().getString());

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return ("" + cell.getDateCellValue());
                } else {
                    return ("" + cell.getNumericCellValue());
                }

            case FORMULA:
                return(cell.getCellFormula().toString());

            case BLANK:
                return "";

            default:
                return "";
        }
    }

	public String reformat(String xlsfile) {
		String outputfile = xlsfile;
		int sheetIndex = 0;
		boolean status = false;
		FileOutputStream fileOut = null;
		try {
			InputStream inp = new FileInputStream(xlsfile);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheetAt(sheetIndex);
			HSSFRow row;
			HSSFCell cell;
/*
c.	Set the column widths (there are specific parameters to follow for each column)
i.	A-B are width of 8
ii.	C is a width of 12
iii.	D-F is a width of 35
iv.	G is a width of 64
v.	H is a width of 35
*/

			sheet.setColumnWidth(0, 8 * 256);   //A
			sheet.setColumnWidth(1, 8 * 256);   //B
			sheet.setColumnWidth(2, 12 * 256);  //C
			sheet.setColumnWidth(3, 35 * 256);  //D
			sheet.setColumnWidth(4, 35 * 256);  //E
			sheet.setColumnWidth(5, 35 * 256);  //F
			sheet.setColumnWidth(6, 64 * 256);  //G
			sheet.setColumnWidth(7, 35 * 256);  //H

            int n = xlsfile.lastIndexOf(".");
			String sheetName = xlsfile.substring(0,n);
			sheetName = sheetName.replace("_", " ");
			sheetName = sheetName + " " + timestamp;
			sheetName = sheetName.replace("Define-XML", "Def-XML");
			wb.setSheetName(sheetIndex, sheetName);

			Font font= wb.createFont();
			//font.setFontHeightInPoints((short)10);
			font.setFontName("Arial");
			//font.setColor(IndexedColors.WHITE.getIndex());
			font.setBold(false);
			font.setItalic(false);

			Iterator rows = sheet.rowIterator();
			int row_num = 0;
			while (rows.hasNext())
			{
				StringBuffer buf = new StringBuffer();
				row = (HSSFRow) rows.next();
				row.setHeight((short)-1);
				if (row_num == 0) {
					row.setHeight((short)950);
				}

				cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				boolean costlistRow = false;
				String costlistCode = getCellData(cell);

				if (costlistCode == null || costlistCode.compareTo("null") == 0 || costlistCode.compareTo("") == 0) {
					costlistRow = true;
				}

				for(int i=0; i<row.getLastCellNum(); i++) {
                    cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
 				    CellStyle cellStyle = cell.getCellStyle();
					cellStyle.setWrapText(true);   //Wrapping text
					if (row_num == 0) {
						cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
					} else {
						cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
					}
					if (costlistRow) {
						cellStyle.setFillBackgroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
						cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
						cellStyle.setFont(font);
				    }
					cellStyle.setBorderTop(BorderStyle.THIN);
					cellStyle.setBorderBottom(BorderStyle.THIN);
					cellStyle.setBorderLeft(BorderStyle.THIN);
					cellStyle.setBorderRight(BorderStyle.THIN);
					cell.setCellStyle(cellStyle);
				}
				row_num++;
			}

			setAutoFilter(sheet, 'H');
			fileOut = new FileOutputStream(outputfile);
			wb.write(fileOut);
			status = true;

		} catch (Exception ex) {
			ex.printStackTrace();

		} finally {
			try {
				fileOut.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return outputfile;
	}

    public HSSFWorkbook loadWorkbook(String filename) {
		HSSFWorkbook workbook = null;
		SummaryInformation summaryInfo = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filename);

			workbook = new HSSFWorkbook(fis);
			return workbook;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (Exception ex) {

			}
		}
		return workbook;
	}

	public void setAuthor(String author) {
		this.workbook.createInformationProperties();
		SummaryInformation summaryInfo =
		this.workbook.getSummaryInformation();
		summaryInfo.setAuthor(author);
	}

	public void setTitle(String title) {
		this.workbook.createInformationProperties();
		SummaryInformation summaryInfo =
		this.workbook.getSummaryInformation();
		summaryInfo.setTitle(title);
	}

	public void setKeywords(String kewords) {
		this.workbook.createInformationProperties();
		SummaryInformation summaryInfo =
		this.workbook.getSummaryInformation();
		summaryInfo.setKeywords(kewords);
	}

	public void setSubject(String subject) {
		this.workbook.createInformationProperties();
		SummaryInformation summaryInfo =
		this.workbook.getSummaryInformation();
		summaryInfo.setSubject(subject);
	}

	public void setComments(String comments) {
		this.workbook.createInformationProperties();
		SummaryInformation summaryInfo =
		this.workbook.getSummaryInformation();
		summaryInfo.setComments(comments);
	}


	public void setMetadata(String title, String author, String subject, String keywords, String comments) {
		this.workbook.createInformationProperties();
		SummaryInformation summaryInfo =
		this.workbook.getSummaryInformation();
		if (title != null) summaryInfo.setTitle(title);
		if (author != null) summaryInfo.setAuthor(author);
		if (subject != null) summaryInfo.setSubject(subject);
		if (keywords != null) summaryInfo.setKeywords(keywords);
		if (comments != null) summaryInfo.setComments(comments);
	}

	public void dumpMetadata() {
		SummaryInformation summaryInfo = this.workbook.getSummaryInformation();
		System.out.println("Title: " + summaryInfo.getTitle() + "\n" +
		                   "Author: " + summaryInfo.getAuthor() + "\n" +
		                   "Subject: " + summaryInfo.getSubject() + "\n" +
		                   "Keywords: " + summaryInfo.getKeywords() + "\n" +
		                   "Comments: " + summaryInfo.getComments());
	}

	public void saveWorkbook(String filename) {
		OutputStream os = null;
		File file = null;
		try {
			file = new File(filename);
			os = new FileOutputStream(file);
			this.workbook.write(os);
		}
		catch(IOException ioEx) {
			ioEx.printStackTrace();
		}
		finally {
			try {
				if(os != null) {
					os.close();
					os = null;
				}
			}
			catch(IOException ioEx) {
				ioEx.printStackTrace();
			}
		}
	}

	public void main(String[] args) {
		long ms = System.currentTimeMillis();
		String xlsfile = args[0];
		String timestamp = args[1];
		String newfile = reformat(xlsfile);
		int n = xlsfile.lastIndexOf(".");
		String title = xlsfile.substring(0, n);
		String author = "NCI-EVS";

		CDISCExcelUtils cdiscExcelUtils = new CDISCExcelUtils(newfile, timestamp);
		cdiscExcelUtils.setMetadata(title, author, title, title, null);
		cdiscExcelUtils.saveWorkbook(xlsfile);
		System.out.println("Total reformat run time (ms): " + (System.currentTimeMillis() - ms));
	}
}


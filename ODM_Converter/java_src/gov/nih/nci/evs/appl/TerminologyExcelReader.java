
package gov.nih.nci.evs.appl;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import java.io.FileInputStream;
import java.io.File;
import java.util.*;

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

class TerminologyExcelReader extends ExcelReader {

	public String terminologyModel = null;
	public String terminologyShortModel = null;
	public String terminologyType = null;
	public String terminologyDate = null;


	public TerminologyExcelReader() {
		super();
	}

    public static Vector parseData(String line, char delimiter) {
		if(line == null) return null;
		Vector w = new Vector();
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (c == delimiter) {
				w.add(buf.toString());
				buf = new StringBuffer();
			} else {
				buf.append(c);
			}
		}
		w.add(buf.toString());
		return w;
	}

	public void read(File file) {
		FileInputStream is = null;
		POIFSFileSystem fs = null;
		HSSFWorkbook workbook = null;
		int sheetCount = 0;
		HSSFSheet sheet = null;
		String sheetName = null;

		try {
			is = new FileInputStream(file);
			fs = new POIFSFileSystem(is);
			workbook = new HSSFWorkbook(fs);
			sheetCount = workbook.getNumberOfSheets();
			sheet = workbook.getSheetAt(1);
			sheetName = sheet.getSheetName();
		} catch (Exception ex) {
		}

			Vector parts = parseData(sheetName, ' ');

			if (parts.size() != 3) {
				throw new RuntimeException("Expected sheet name in form '<type> Terminology <date>' but found '" + sheetName + "' instead");
			}

			terminologyModel = (String) parts.elementAt(0);
			terminologyModel = terminologyModel.trim();
			terminologyShortModel = (String) parts.elementAt(1);
			terminologyShortModel = terminologyShortModel.trim();


			terminologyType = "Controlled Terminology";
			terminologyDate = (String) parts.elementAt(2);
			terminologyDate = terminologyDate.trim();

        try {
			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("OK");
	}
}


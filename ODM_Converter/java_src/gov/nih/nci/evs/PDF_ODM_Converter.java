package gov.nih.nci.evs;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Phrase;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class PDF_ODM_Converter {


	
	public static void main(String[] args) {
//		new PDF_Sandbox().createPdf(RESULT);
		try {
			if (args.length==2){
			new PDF_ODM_Converter().XMLConvert(args[0], args[1]);
			pageNumber(args[0], args[1]);
				}
			else
			{
				System.out.println("Two parameters required <input.html> <output.pdf>");
			}
		} catch (DocumentException e) {
			System.out.println("Error trying to parse html into PDF");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error trying to read or write files");
			e.printStackTrace();
		}
		
		
	}

	
	public void XMLConvert(String inputFile, String outputFile) throws DocumentException, IOException {
//		Document document = new Document();
//		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(inputFile+".pdf"));
//		document.open();
//		XMLWorkerHelper.getInstance().parseXHtml(writer, document, new FileInputStream(inputFile));
//		document.close();
		
	}
	
	
	private static void pageNumber(String inputFile, String outputFile){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Document document = new Document();
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			document.open();
			XMLWorkerHelper.getInstance().parseXHtml(writer, document, new FileInputStream(inputFile));
			document.close();
			

			PdfReader reader = new PdfReader(baos.toByteArray());
			PdfStamper stamper =  new PdfStamper(reader, new FileOutputStream(outputFile));
			int n1 = reader.getNumberOfPages();
			for (int i=0;i<n1;){
//				page = copy.getImportedPage(reader1, ++i);
				PdfContentByte contentByte = stamper.getUnderContent(++i);
				Phrase pagePhrase = new Phrase(String.format("page %d of %d",i,n1));
				ColumnText.showTextAligned(contentByte, Element.ALIGN_CENTER, pagePhrase, 297.5f, 28, 0);
//				stamp.alterContents();
				
//				copy.addPage(page);
			}
			stamper.close();
			reader.close();
			
		} catch (IOException e) {
			System.out.println("Unable to read or write file");
			e.printStackTrace();
		} catch (DocumentException e) {
			System.out.println("Unable to create pdf output");
			e.printStackTrace();
		}
	}
}

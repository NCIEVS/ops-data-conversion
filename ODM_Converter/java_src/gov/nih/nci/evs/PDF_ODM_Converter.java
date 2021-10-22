package gov.nih.nci.evs;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;



import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

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

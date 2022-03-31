package gov.nih.nci.evs.cdisc;

import gov.nih.nci.evs.test.utils.AssertExcelFiles;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CDISCReportGeneratorTest {
  // Run this test with -DcurrentReportsDirectory=<PATH_TO_CURRENT_REPORT_FILES\>
  private static final String THESAURUS_220314_22_03_B_FILE =
      System.getProperty("THESAURUS_220314_22_03_B");

  @ParameterizedTest
  @CsvSource({
    "CDISC ADaM Terminology,C81222",
    "CDISC Glossary Terminology,C67497",
    "CDISC CDASH Terminology,C77527",
    "CDISC Define-XML Terminology,C165634",
    "CDISC QRS Terminology,C120166",
    "CDISC Protocol Terminology,C132298",
    "CDISC SDTM Terminology,C66830",
    "CDISC SEND Terminology,C77526"
  })
  public void testGenerate(String subSource, String subSourceCode) throws IOException {
    // The expected files for this test were generated using Thesaurus-220314-22.03b.owl file. This
    // test is only valid with that file. Not adding to this project as the file is quite large
    CDISCReportGenerator generator = new CDISCReportGenerator(THESAURUS_220314_22_03_B_FILE);
    AssertExcelFiles assertExcelFiles = new AssertExcelFiles();
    generator.run(subSourceCode);
    String textFileName = format("%s.txt", subSource);
    String xlsFileName = format("%s.xls", subSource);

    File textFile = new File(textFileName);
    File excelFile = new File(xlsFileName);

    assertTrue(textFile.exists());
    assertTrue(excelFile.exists());
    assertThat(new File(textFileName))
        .hasContent(
            IOUtils.toString(
                getClass().getResourceAsStream(format("/fixtures/report_files/%s", textFileName)),
                Charset.defaultCharset()));

    assertExcelFiles.assertLegacyExcel(
        new FileInputStream(xlsFileName),
        this.getClass().getResourceAsStream(format("/fixtures/report_files/%s", xlsFileName)));
    // We want to retain the files for troubleshooting when the test fails
    textFile.delete();
    excelFile.delete();
  }
}

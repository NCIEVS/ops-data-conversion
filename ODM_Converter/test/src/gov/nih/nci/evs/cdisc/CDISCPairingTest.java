package gov.nih.nci.evs.cdisc;

import gov.nih.nci.evs.test.utils.AssertExcelFiles;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;

public class CDISCPairingTest {
  // Run this test with --DTHESAURUS_220314_22_03_B_FILE=<PATH_TO_THESAURUS_220314_22_03_B_FILE\>
  private static final String THESAURUS_220314_22_03_B_FILE =
      System.getProperty("THESAURUS_220314_22_03_B_FILE");

  @ParameterizedTest
  @CsvSource({"ADaM,C81222", "SDTM,C66830", "SEND,C77526"})
  public void testGenerate(String subSource, String subSourceCode) throws IOException {
    CDISCPairing pairing = new CDISCPairing(THESAURUS_220314_22_03_B_FILE);
    pairing.run(subSourceCode, CDISCPairing.DATA_SOURCE_NCIT_OWL);
    File actual = new File(getActualPairingReportFilename(subSource));
    AssertExcelFiles assertExcelFiles = new AssertExcelFiles();
    assertExcelFiles.assertExcel(
        this.getClass()
            .getResourceAsStream(
                String.format(
                    "/fixtures/pairing-files/%s", getExpectedPairingReportFilename(subSource))),
        new FileInputStream(actual));
    // We want to retain the files for troubleshooting when the test fails
    actual.delete();
  }

  private String getExpectedPairingReportFilename(String subSource) {
    return subSource + "_paired_view_2022-03-25.xlsx";
  }

  private String getActualPairingReportFilename(String subSource) {
    return new StringBuilder(subSource)
        .append("_paired_view_")
        .append(LocalDate.now().toString().replace("-", "_"))
        .append(".xlsx")
        .toString();
  }

  @AfterEach
  public void cleanup() {
    new File("metadata_1.txt").delete();
    new File("pairedTermData_1.txt").delete();
    new File("readme.txt").delete();
  }

  private static class CDISCExcelUtilsAssert extends AssertExcelFiles {
    @Override
    public void assertCell(Cell expectedCell, Cell actualCell, int rowIndex, int cellIndex) {
      super.assertCell(expectedCell, actualCell, rowIndex, cellIndex);
      String expectedStyle = stripFillId(expectedCell.getCellStyle());
      String actualStyle = stripFillId(actualCell.getCellStyle());
      Assertions.assertThat(expectedStyle).isEqualTo(actualStyle);
    }

    /**
     * For some reason the fillId is different between the excel sheets even though there is no
     * visible differences in the color of the cell. So ignoring that field when doing the
     * comparison
     */
    private String stripFillId(CellStyle style) {
      return ((XSSFCellStyle) style)
          .getCoreXf()
          .toString()
          .replaceAll("fillId=\"[0-9]+\"", "fillId=\"\"");
    }
  }
}

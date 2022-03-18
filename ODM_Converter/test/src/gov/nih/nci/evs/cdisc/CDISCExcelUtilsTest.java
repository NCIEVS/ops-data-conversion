package gov.nih.nci.evs.cdisc;

import gov.nih.nci.evs.test.utils.AssertExcelFiles;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class CDISCExcelUtilsTest {
  @ParameterizedTest
  @CsvSource({
    "ADaM Terminology",
    "Glossary Terminology",
    "CDASH Terminology",
    "Define-XML Terminology",
    "Protocol Terminology",
    "QRS Terminology",
    "SDTM Terminology",
    "SEND Terminology"
  })
  public void test_generate_odm_xml(String concept) throws IOException {
    // Ideally this would be created in a temp directory created by Junit. But the formatter program
    // expects this file at the context root
    String templateExcelFileName = "%s%s.xls";
    String outFile = format(templateExcelFileName, concept, "");
    try {
      // Make a copy as the
      IOUtils.copy(
          new FileInputStream(
              format(
                  "test/resources/fixtures/excel-utils/%s",
                  format(templateExcelFileName, concept, "-input"))),
          new FileOutputStream(outFile));
      CDISCExcelUtils cdiscExcelUtils = new CDISCExcelUtils(outFile, "2022-03-14");
      cdiscExcelUtils.reformat(outFile);
      AssertExcelFiles assertExcelFiles = new CDISCExcelUtilsAssert();
      assertExcelFiles.assertLegacyExcel(
          new FileInputStream(
              format(
                  "test/resources/fixtures/excel-utils/%s",
                  format(templateExcelFileName, concept, "-expected"))),
          new FileInputStream(outFile));
    } finally {
      new File(outFile).delete();
    }
  }

  private static class CDISCExcelUtilsAssert extends AssertExcelFiles {
    private final int[] EXPECTED_WIDTHS = {
      8 * 256, 8 * 256, 12 * 256, 35 * 256, 35 * 256, 35 * 256, 64 * 256, 35 * 256
    };

    @Override
    public void assertSheet(Sheet expectedSheet, Sheet actualSheet) {
      super.assertSheet(expectedSheet, actualSheet);
      IntStream.range(0, 8)
          .forEach(
              index ->
                  assertThat(actualSheet.getColumnWidth(index))
                      .as("Cell width for index %d", index)
                      .isEqualTo(expectedSheet.getColumnWidth(index))
                      .isEqualTo(EXPECTED_WIDTHS[index]));
    }

    @Override
    public void assertRow(Row expectedRow, Row actualRow, int rowIndex) {
      super.assertRow(expectedRow, actualRow, rowIndex);
      if (rowIndex == 0) {
        assertThat(expectedRow.getHeight()).isEqualTo(actualRow.getHeight()).isEqualTo((short) 950);
      }
    }

    @Override
    public void assertCell(Cell expectedCell, Cell actualCell, int rowIndex, int cellIndex) {
      super.assertCell(expectedCell, actualCell, rowIndex, cellIndex);
      boolean isParent =
          cellIndex == 1
              && (actualCell.getCellTypeEnum() == CellType.BLANK
                  || actualCell.getStringCellValue() == null
                  || "".equalsIgnoreCase(actualCell.getStringCellValue()));
      assertCellStyle(actualCell, rowIndex, isParent);
    }

    private void assertCellStyle(Cell cell, int rowIndex, boolean isParent) {
      CellStyle cellStyle = cell.getCellStyle();
      assertThat(cellStyle.getWrapText()).isTrue();
      assertThat(cellStyle.getVerticalAlignmentEnum())
          .isEqualTo(rowIndex == 0 ? VerticalAlignment.CENTER : VerticalAlignment.TOP);
      Workbook workBook = cell.getSheet().getWorkbook();
      Font font = workBook.getFontAt(cellStyle.getFontIndex());
      if (isParent) {
        assertThat(font.getFontName()).isEqualTo("Arial");
        assertThat(font.getBold()).isFalse();
        assertThat(font.getItalic()).isFalse();

        assertThat(cellStyle.getFillBackgroundColor())
            .isEqualTo(IndexedColors.LIGHT_TURQUOISE.getIndex());
        assertThat(cellStyle.getFillForegroundColor())
            .isEqualTo(IndexedColors.LIGHT_TURQUOISE.getIndex());
      }
      assertThat(cellStyle.getBorderTopEnum()).isEqualTo(BorderStyle.THIN);
      assertThat(cellStyle.getBorderBottomEnum()).isEqualTo(BorderStyle.THIN);
      assertThat(cellStyle.getBorderLeftEnum()).isEqualTo(BorderStyle.THIN);
      assertThat(cellStyle.getBorderRightEnum()).isEqualTo(BorderStyle.THIN);
    }
  }
}

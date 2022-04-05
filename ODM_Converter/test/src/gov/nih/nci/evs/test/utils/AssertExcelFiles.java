package gov.nih.nci.evs.test.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertExcelFiles {
  public AssertExcelFiles() {}

  public void assertLegacyExcel(InputStream expectedFile, InputStream actualFile)
      throws IOException {
    assertWorkbook(new HSSFWorkbook(expectedFile), new HSSFWorkbook(actualFile));
  }

  public void assertExcel(InputStream expectedFile, InputStream actualFile) throws IOException {
    assertWorkbook(new XSSFWorkbook(expectedFile), new XSSFWorkbook(actualFile));
  }

  public void assertWorkbook(Workbook expectedWorkbook, Workbook actualWorkbook) {
    int expectedNumberOfSheets = expectedWorkbook.getNumberOfSheets();
    int actualNumberOfSheets = actualWorkbook.getNumberOfSheets();
    assertThat(expectedNumberOfSheets).isEqualTo(actualNumberOfSheets);

    IntStream.range(0, expectedNumberOfSheets)
        .forEach(
            index ->
                assertSheet(expectedWorkbook.getSheetAt(index), actualWorkbook.getSheetAt(index)));
  }

  public void assertSheet(Sheet expectedSheet, Sheet actualSheet) {
    assertThat(expectedSheet.getSheetName()).startsWith(actualSheet.getSheetName());
    assertThat(expectedSheet.getPhysicalNumberOfRows())
        .isEqualTo(actualSheet.getPhysicalNumberOfRows());
    Iterator<Row> expectedRows = expectedSheet.rowIterator();
    Iterator<Row> actualRows = actualSheet.rowIterator();
    IntStream.range(0, expectedSheet.getPhysicalNumberOfRows())
        .forEach(index -> assertRow(expectedRows.next(), actualRows.next(), index));
  }

  public void assertRow(Row expectedRow, Row actualRow, int rowIndex) {
    assertThat(expectedRow.getPhysicalNumberOfCells())
        .isEqualTo(actualRow.getPhysicalNumberOfCells());
    IntStream.range(0, expectedRow.getPhysicalNumberOfCells())
        .forEach(
            columnIndex ->
                assertCell(
                    expectedRow.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK),
                    actualRow.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK),
                    rowIndex,
                    columnIndex));
  }

  public void assertCell(Cell expectedCell, Cell actualCell, int rowIndex, int cellIndex) {
    assertThat(expectedCell.getCellTypeEnum()).isEqualTo(actualCell.getCellTypeEnum());
    if (expectedCell.getCellTypeEnum() == CellType.STRING) {
      assertThat(expectedCell.getStringCellValue()).isEqualTo(actualCell.getStringCellValue());
    } else if (expectedCell.getCellTypeEnum() == CellType.NUMERIC) {
      assertThat(expectedCell.getNumericCellValue()).isEqualTo(actualCell.getNumericCellValue());
    } else if (expectedCell.getCellTypeEnum() == CellType.BOOLEAN) {
      assertThat(expectedCell.getNumericCellValue()).isEqualTo(actualCell.getNumericCellValue());
    }
  }
}

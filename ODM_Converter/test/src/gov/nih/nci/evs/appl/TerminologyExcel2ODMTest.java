package gov.nih.nci.evs.appl;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class TerminologyExcel2ODMTest {

  @TempDir public File outFolder;

  @ParameterizedTest
  @CsvSource({
    "ADaM Terminology.xls,ADaM Terminology.odm.xml",
    "CDISC Glossary.xls,CDISC Glossary.odm.xml",
    "CDASH Terminology.xls,CDASH Terminology.odm.xml",
    "Define-XML Terminology.xls,Define-XML Terminology.odm.xml",
    "Protocol Terminology.xls,Protocol Terminology.odm.xml",
    "SDTM Terminology.xls,SDTM Terminology.odm.xml",
    "SEND Terminology.xls,SEND Terminology.odm.xml"
  })
  public void test_generate_odm_xml(String sourceXls, String expectedXml) throws IOException {
    String outFile = Paths.get(outFolder.getAbsolutePath(), "out.xml").toString();
    TerminologyExcel2ODM terminologyExcel2ODM =
        new TerminologyExcel2ODM(format("test/resources/fixtures/%s", sourceXls), outFile);
    terminologyExcel2ODM.generate_odm_xml();
    Diff myDiff =
        DiffBuilder.compare(
                IOUtils.resourceToString(
                    format("/fixtures/%s", expectedXml), Charset.defaultCharset()))
            .checkForSimilar()
            .withTest(IOUtils.toString(new FileInputStream(outFile), Charset.defaultCharset()))
            .withDifferenceEvaluator(new IgnoreAttributeDifferenceEvaluator(getIgnoreAttributes()))
            .build();
    printDifferences(myDiff);
    Assertions.assertFalse(myDiff.hasDifferences());
  }

  private void printDifferences(Diff diff) {
    for (Difference difference : diff.getDifferences()) {
      System.out.println(difference.toString());
    }
  }

  private Map<String, List<String>> getIgnoreAttributes() {
    Map<String, List<String>> ignoreAttributeMap = new HashMap<>();
    List<String> ignoreAttributes = new ArrayList<>();
    ignoreAttributes.add("CreationDateTime");
    ignoreAttributeMap.put("ODM", ignoreAttributes);
    return ignoreAttributeMap;
  }
}

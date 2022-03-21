package gov.nih.nci.evs.cdisc;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class ReportValidatorTest {
  private static final String currentReportsDirectory =
      System.getProperty("currentReportsDirectory");
  private static final File schemaFile =
      new File("build/out/schema/controlledterminology1-2-0.xsd");

  /** Does basic schema validation for the ODM XML files */
  @ParameterizedTest
  @CsvSource({
    "ADaM,ADaM Terminology.odm.xml",
    "CDISC_Glossary,Glossary Terminology.odm.xml",
    "Define-XML,Define-XML Terminology.odm.xml",
    "Protocol,Protocol Terminology.odm.xml",
    "QRS,QRS Terminology.odm.xml",
    "SDTM,CDASH Terminology.odm.xml",
    "SDTM,SDTM Terminology.odm.xml",
    "SEND,SEND Terminology.odm.xml"
  })
  public void validateOdmXml(String concept, String odmXmlFile) throws SAXException, IOException {
    Source xmlFile =
        new StreamSource(Paths.get(currentReportsDirectory, concept, odmXmlFile).toFile());
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(schemaFile);
    Validator validator = schema.newValidator();
    validator.validate(xmlFile);
  }

  /** Validates that there are no non-ASCII characters in the ODM XML files */
  @ParameterizedTest
  @CsvSource({
    "ADaM,ADaM Terminology.odm.xml",
    "CDISC_Glossary,Glossary Terminology.odm.xml",
    "Define-XML,Define-XML Terminology.odm.xml",
    "Protocol,Protocol Terminology.odm.xml",
    "QRS,QRS Terminology.odm.xml",
    "SDTM,CDASH Terminology.odm.xml",
    "SDTM,SDTM Terminology.odm.xml",
    "SEND,SEND Terminology.odm.xml"
  })
  public void validateNonAsciiOdmXml(String concept, String odmXmlFile) throws IOException {
    File fXmlFile = Paths.get(currentReportsDirectory, concept, odmXmlFile).toFile();
    CharsetDecoder decoder = StandardCharsets.US_ASCII.newDecoder();
    decoder.decode(ByteBuffer.wrap(IOUtils.toByteArray(new FileInputStream(fXmlFile))));
  }

  /**
   * This is to validate a specific issue with the OWL files generated with Windows based systems.
   * The namespace in the OWL files had a path to the local file system appended to it. This
   * validation is to ensure that issue is fixed
   */
  @ParameterizedTest
  @CsvSource({
    "ADaM,ADaM Terminology.owl",
    "CDISC_Glossary,Glossary Terminology.owl",
    "Define-XML,Define-XML Terminology.owl",
    "Protocol,Protocol Terminology.owl",
    "QRS,QRS Terminology.owl",
    "SDTM,CDASH Terminology.owl",
    "SDTM,SDTM Terminology.owl",
    "SEND,SEND Terminology.owl"
  })
  public void validateOwlNamespace(String concept, String owlFile)
      throws ParserConfigurationException, IOException, SAXException {
    File fXmlFile = Paths.get(currentReportsDirectory, concept, owlFile).toFile();
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(fXmlFile);
    NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
    String conceptNamespace = getConceptNamespace(owlFile);
    assertThat(attributes.getNamedItem("xmlns").getNodeValue()).isEqualTo(conceptNamespace);
    assertThat(attributes.getNamedItem("xml:base").getNodeValue())
        .isEqualTo(conceptNamespace.replace("#", ""));
  }

  private String getConceptNamespace(String owlFile) {
    String baseName = FilenameUtils.getBaseName(owlFile);
    return format("http://rdf.cdisc.org/%s#", baseName.toLowerCase(Locale.ROOT).replace(" ", "-"));
  }
}

/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.extractor;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import net.coru.kloadgen.extractor.impl.SchemaExtractorImpl;
import net.coru.kloadgen.model.FieldValueMapping;
import net.coru.kloadgen.testutil.FileHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static net.coru.kloadgen.util.SchemaRegistryKeyHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({
    WiremockResolver.class,
    WiremockUriResolver.class
})
class SchemaExtractorTest {

  private final FileHelper fileHelper = new FileHelper();
  private final SchemaExtractor schemaExtractor = new SchemaExtractorImpl();

  @BeforeEach
  public void setUp() {
    File file = new File("src/test/resources");
    String absolutePath = file.getAbsolutePath();
    JMeterUtils.loadJMeterProperties(absolutePath + "/kloadgen.properties");
    JMeterContext jmcx = JMeterContextService.getContext();
    jmcx.setVariables(new JMeterVariables());
    JMeterUtils.setLocale(Locale.ENGLISH);
  }

  @Test
  @DisplayName("Should extract simple Record")
  void testFlatPropertiesListSimpleRecord(@Wiremock WireMockServer server) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList(
        "avroSubject"
    );

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("Name", "string"),
            new FieldValueMapping("Age", "int")
        );
  }

  @Test
  @DisplayName("Should extract Array of Record")
  void testFlatPropertiesListArrayRecord(@Wiremock WireMockServer server) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList("users");

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("Users[].id", "long"),
            new FieldValueMapping("Users[].name", "string")
        );
  }

  @Test
  @DisplayName("Should extract Map of Record")
  void testFlatPropertiesListMapArray(@Wiremock WireMockServer server) throws IOException, RestClientException {
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_URL, "http://localhost:" + server.port());
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_USERNAME_KEY, "foo");
    JMeterContextService.getContext().getProperties().put(SCHEMA_REGISTRY_PASSWORD_KEY, "foo");

    Pair<String, List<FieldValueMapping>> fieldValueMappingList = schemaExtractor.flatPropertiesList("arrayMap");

    assertThat(fieldValueMappingList.getRight())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("name", "string"),
            new FieldValueMapping("values[][:]", "string-map-array")
        );
  }

  @Test
  @DisplayName("Should extract Embedded Record")
  void testFlatPropertiesEmbeddedAvros() throws IOException {
    File testFile = fileHelper.getFile("/avro-files/embedded-avros-example-test.avsc");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));
    assertThat(fieldValueMappingList)
        .hasSize(4)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("fieldMySchema.testInt_id", "int"),
            new FieldValueMapping("fieldMySchema.testLong", "long"),
            new FieldValueMapping("fieldMySchema.fieldString", "string"),
            new FieldValueMapping("timestamp", "long")
        );
  }

  @Test
  @DisplayName("Should extract Optional Map with Array/Record")
  void testFlatPropertiesOptionalMapArray() throws IOException {

    File testFile = fileHelper.getFile("/avro-files/testOptionalMap.avsc");

    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));

    assertThat(fieldValueMappingList)
        .hasSize(8)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("mapOfString[:]", "string-map"),
            new FieldValueMapping("arrayOfString[]", "string-array"),
            new FieldValueMapping("arrayOfMap[][:]", "string-map-array"),
            new FieldValueMapping("mapOfArray[:][]", "int-array-map"),
            new FieldValueMapping("mapOfArrayOfRecord[:][].name", "string"),
            new FieldValueMapping("mapOfArrayOfRecord[:][].age", "int"),
            new FieldValueMapping("arrayOfMapOfRecord[][:].name", "string"),
            new FieldValueMapping("arrayOfMapOfRecord[][:].age", "int")
        );
  }

  @Test
  @DisplayName("Should extract Map of Record")
  void testFlatPropertiesMap() throws IOException {
    File testFile = fileHelper.getFile("/avro-files/testMap.avsc");
    List<FieldValueMapping> fieldValueMappingList = schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));
    assertThat(fieldValueMappingList)
        .hasSize(9)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("theMap[:][].otherType.addTypeId", "string"),
            new FieldValueMapping("theMap[:][].otherType.name", "string"),
            new FieldValueMapping("theMap[:][].otherType.otherField", "string"),
            new FieldValueMapping("theMap[:][].addAmount", "bytes_decimal"),
            new FieldValueMapping("theMap[:][].addCode", "string"),
            new FieldValueMapping("theMap[:][].metadataMap[:]", "string-map"),
            new FieldValueMapping("theMap[:][].metadataArray[]", "string-array"),
            new FieldValueMapping("theMap[:][].metadataMapMap[:][:]", "string-map-map"),
            new FieldValueMapping("theMap[:][].metadataArrayArray[][]", "string-array-array")
        );
  }

  @Test
  @DisplayName("Should extract Logical times")
  void testFlatPropertiesLogicalTypes () throws IOException {

    File testFile = fileHelper.getFile("/avro-files/testLogicalTypes.avsc");

    List<FieldValueMapping> fieldValueMappingList =
      schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));

    assertThat(fieldValueMappingList)
      .hasSize(10)
      .containsExactlyInAnyOrder(
          new FieldValueMapping("Date", "int_date"),
          new FieldValueMapping("TimeMillis", "int_time-millis"),
          new FieldValueMapping("TimeMicros", "long_time-micros"),
          new FieldValueMapping("TimestampMillis", "long_timestamp-millis"),
          new FieldValueMapping("TimestampMicros", "long_timestamp-micros"),
          new FieldValueMapping("LocalTimestampMillis", "long_local-timestamp-millis"),
          new FieldValueMapping("LocalTimestampMicros", "long_local-timestamp-micros"),
          new FieldValueMapping("UUID","string_uuid"),
          new FieldValueMapping("Decimal","bytes_decimal"),
          new FieldValueMapping("DecimalFixed","fixed_decimal")
      );
  }

  @Test
  @DisplayName("Should extract Optional Array")
  void testFlatPropertiesOptionalArray () throws IOException {

    File testFile = fileHelper.getFile("/avro-files/issue.avsc");

    List<FieldValueMapping> fieldValueMappingList =
        schemaExtractor.flatPropertiesList(schemaExtractor.schemaTypesList(testFile, "AVRO"));

    assertThat(fieldValueMappingList)
        .hasSize(3)
        .containsExactlyInAnyOrder(
            new FieldValueMapping("mainObject.arrayValue[].optional1", "string"),
            new FieldValueMapping("mainObject.arrayValue[].optional2", "string"),
            new FieldValueMapping("mainObject.arrayValue[].optional3", "string")
        );
  }

}